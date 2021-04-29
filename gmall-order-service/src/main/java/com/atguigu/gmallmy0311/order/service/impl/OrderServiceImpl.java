package com.atguigu.gmallmy0311.order.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.OrderDetail;
import com.atguigu.gmallmy0311.bean.OrderInfo;
import com.atguigu.gmallmy0311.bean.enums.ProcessStatus;
import com.atguigu.gmallmy0311.config.ActiveMQUtil;
import com.atguigu.gmallmy0311.config.RedisUtil;
import com.atguigu.gmallmy0311.order.mapper.OrderDetailMapper;
import com.atguigu.gmallmy0311.order.mapper.OrderInfoMapper;
import com.atguigu.gmallmy0311.service.OrderService;
import com.atguigu.gmallmy0311.service.PaymentService;
import com.atguigu.gmallmy0311.util.HttpClientUtil;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Async;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.jms.Queue;
import java.util.*;

@Service
public class OrderServiceImpl implements OrderService {
    @Autowired
    private OrderInfoMapper orderInfoMapper;
    @Autowired
    private OrderDetailMapper orderDetailMapper;


    @Reference
    PaymentService paymentService;
    @Autowired
    RedisUtil redisUtil;

    @Autowired
    ActiveMQUtil activeMQUtil;

    // 生成流水号
    public String getTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = UUID.randomUUID().toString();
        jedis.setex(tradeNoKey, 10 * 60, tradeCode);
        jedis.close();
        return tradeCode;

    }


    // 验证流水号
    public boolean checkTradeCode(String userId, String tradeCodeNo) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        String tradeCode = jedis.get(tradeNoKey);
        jedis.close();
        if (tradeCode != null && tradeCode.equals(tradeCodeNo)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public boolean checkStock(String skuId, Integer skuNum) {
        String result = HttpClientUtil.doGet("http://www.gware.com/hasStock?skuId=" + skuId + "&num=" + skuNum);
        if ("1".equals(result)) {
            return true;
        } else {
            return false;
        }

    }

    @Override
    public OrderInfo getOrderInfo(String orderId) {

        OrderInfo orderInfo = orderInfoMapper.selectByPrimaryKey(orderId);
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        // 将orderDetai 放入orderInfo 中
        if (orderInfo != null) {
            orderInfo.setOrderDetailList(orderDetailMapper.select(orderDetail));

        }

        return orderInfo;
    }

    @Override
    public OrderDetail getOrderDetailByOrderId(String orderId) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setOrderId(orderId);
        orderDetailMapper.selectOne(orderDetail);
        return orderDetailMapper.selectOne(orderDetail);
    }

    @Override
    public void updateOrderStatus(String orderId, ProcessStatus processStatus) {
        OrderInfo orderInfo = new OrderInfo();
        orderInfo.setId(orderId);
        orderInfo.setProcessStatus(processStatus);
        orderInfo.setOrderStatus(processStatus.getOrderStatus());
        orderInfoMapper.updateByPrimaryKeySelective(orderInfo);

    }

    @Override
    public void sendOrderStatus(String orderId) {
        Connection connection = activeMQUtil.getConnection();
        String orderJson = initWareOrder(orderId);
        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            Queue order_result_queue = session.createQueue("ORDER_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(order_result_queue);
            ActiveMQTextMessage textMessage = new ActiveMQTextMessage();
            textMessage.setText(orderJson);
            producer.send(textMessage);
            session.commit();
            session.close();
            producer.close();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }

    }

    // 扫描过期订单方法
    @Override
    public List<OrderInfo> getExpiredOrderList() {
        Example example = new Example(OrderInfo.class);
        example.createCriteria().andLessThan("expireTime", new Date()).andEqualTo("processStatus", ProcessStatus.UNPAID);
        return orderInfoMapper.selectByExample(example);

    }

    @Override
    @Async
    public void execExpiredOrder(OrderInfo orderInfo) {
        updateOrderStatus(orderInfo.getId(), ProcessStatus.CLOSED);
//付款信息
        paymentService.closePayment(orderInfo.getId());


    }

    @Override
    public List<OrderInfo> splitOrder(String orderId, String wareSkuMap) {
        List<OrderInfo> subOrderInfoList = new ArrayList<>();
        //先查询原始订单
        OrderInfo orderInfoOrigin = getOrderInfo(orderId);
        //2.wareSkuMap 反序列化
        List<Map> maps = JSON.parseArray(wareSkuMap, Map.class);
        //3.遍历拆单方案
        if (maps!=null && maps.size()>0){


        for (Map map : maps) {
            String warId = (String) map.get("wareId");
            List<String> skuIds = (List<String>) map.get("skuIds");
            //4.生成订单主表，从原始订单赋值，新的订单号，父订单
            OrderInfo subOrderInfo = new OrderInfo();
            BeanUtils.copyProperties(orderInfoOrigin, subOrderInfo);
            subOrderInfo.setId(null);
           //5.原来主订单，订单主表中的订单状态标志为拆单
           subOrderInfo.setWareId(warId); 
        //6.明细表，根据拆单方案中的skuids进行pipei，得到那个的子订单
            List<OrderDetail> orderDetailList = orderInfoOrigin.getOrderDetailList();
            List<OrderDetail> subOrderDetailList=new ArrayList<>();
            if (orderDetailList!=null && orderDetailList.size()>0) {
                for (OrderDetail orderDetail : orderDetailList) {
                    for (String skuId : skuIds) {
                        if (skuId.equals(orderDetail.getSkuId())) {
                            orderDetail.setId(null);
                            orderDetail.setOrderId(orderInfoOrigin.getId());
                            subOrderDetailList.add(orderDetail);
                        }


                    }


                }
            }
           subOrderInfo.setOrderDetailList(subOrderDetailList);
           subOrderInfo.sumTotalAmount();
           subOrderInfo.setParentOrderId(orderId);
           //7.保存到数据库中
            saveOrder(subOrderInfo);
            subOrderInfoList.add(subOrderInfo);
        }
        }
       updateOrderStatus(orderId,ProcessStatus.SPLIT);
//8返回一个新生成的子订单列表

        return subOrderInfoList;
    }

    private String initWareOrder(String orderId) {
        OrderInfo orderInfo = getOrderInfo(orderId);
        Map map = initWareOrder(orderInfo);
        return JSON.toJSONString(map);


    }

    public Map initWareOrder(OrderInfo orderInfo) {
        Map<String, Object> map = new HashMap<>();
        map.put("orderId", orderInfo.getId());
        map.put("consignee", orderInfo.getConsignee());
        map.put("consigneeTel", orderInfo.getConsigneeTel());
        map.put("orderComment", orderInfo.getOrderComment());
        map.put("orderBody", orderInfo.getTradeBody());
        map.put("deliveryAddress", orderInfo.getDeliveryAddress());
        map.put("paymentWay", "2");
        map.put("wareId", orderInfo.getWareId());
        // 组合json
        List detailList = new ArrayList();

        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            Map detailMap = new HashMap();
            detailMap.put("skuId", orderDetail.getSkuId());
            detailMap.put("skuName", orderDetail.getSkuName());
            detailMap.put("skuNum", orderDetail.getSkuNum());
            detailList.add(detailMap);

        }
        map.put("details", detailList);
        return map;
    }

    // 删除流水号
    public void delTradeNo(String userId) {
        Jedis jedis = redisUtil.getJedis();
        String tradeNoKey = "user:" + userId + ":tradeCode";
        jedis.del(tradeNoKey);
        jedis.close();
    }


    @Override
    @Transactional
    public String saveOrder(OrderInfo orderInfo) {
        // 设置创建时间
        orderInfo.setCreateTime(new Date());
        // 设置失效时间
        Calendar calendar = Calendar.getInstance();
        calendar.add(Calendar.DATE, 1);
        orderInfo.setExpireTime(calendar.getTime());
        // 生成第三方支付编号
        String outTradeNo = "ATGUIGU" + System.currentTimeMillis() + "" + new Random().nextInt(1000);
        orderInfo.setOutTradeNo(outTradeNo);
        orderInfoMapper.insertSelective(orderInfo);
        // 插入订单详细信息
        List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
        for (OrderDetail orderDetail : orderDetailList) {
            orderDetail.setOrderId(orderInfo.getId());
            orderDetailMapper.insertSelective(orderDetail);

        }
// 为了跳转到支付页面使用。支付会根据订单id进行支付。
        String orderId = orderInfo.getId();

        return orderId;
    }
}
