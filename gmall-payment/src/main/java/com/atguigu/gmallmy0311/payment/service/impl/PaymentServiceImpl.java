package com.atguigu.gmallmy0311.payment.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.alipay.api.AlipayApiException;
import com.alipay.api.AlipayClient;
import com.alipay.api.request.AlipayTradeQueryRequest;
import com.alipay.api.request.AlipayTradeRefundRequest;
import com.alipay.api.response.AlipayTradeQueryResponse;
import com.alipay.api.response.AlipayTradeRefundResponse;
import com.atguigu.gmallmy0311.bean.OrderInfo;
import com.atguigu.gmallmy0311.bean.PaymentInfo;
import com.atguigu.gmallmy0311.bean.enums.PaymentStatus;
import com.atguigu.gmallmy0311.config.ActiveMQUtil;
import com.atguigu.gmallmy0311.payment.config.AlipayConfig;
import com.atguigu.gmallmy0311.payment.mapper.PaymentInfoMapper;
import com.atguigu.gmallmy0311.service.OrderService;
import com.atguigu.gmallmy0311.service.PaymentService;
import com.atguigu.gmallmy0311.util.HttpClient;
import com.github.wxpay.sdk.WXPayUtil;
import org.apache.activemq.ScheduledMessage;
import org.apache.activemq.command.ActiveMQMapMessage;
import org.apache.activemq.command.ActiveMQTextMessage;
import org.apache.ibatis.annotations.ResultType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import tk.mybatis.mapper.entity.Example;

import javax.jms.*;
import javax.xml.transform.Result;
import java.util.HashMap;
import java.util.Map;


@Service
public class PaymentServiceImpl implements PaymentService {
    @Autowired
    PaymentInfoMapper paymentInfoMapper;

    @Reference
    private OrderService orderService;

    @Autowired
    AlipayClient alipayClient;

    @Autowired
    ActiveMQUtil activeMQUtil;

    // 服务号Id
    @Value("${appid}")
    private String appid;
    // 商户号Id
    @Value("${partner}")
    private String partner;
    // 密钥
    @Value("${partnerkey}")
    private String partnerkey;


    @Override
    public boolean checkPayment(PaymentInfo paymentInfoQuery) {
        // 查询当前的支付信息

        PaymentInfo paymentInfo = getPaymentInfo(paymentInfoQuery);
        if (paymentInfo.getPaymentStatus() == PaymentStatus.PAID || paymentInfo.getPaymentStatus() == PaymentStatus.ClOSED) {

            return true;
        }
        AlipayTradeQueryRequest request = new AlipayTradeQueryRequest();
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", paymentInfo.getOutTradeNo());
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeQueryResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            if ("TRADE_SUCCESS".equals(response.getTradeStatus()) || "TRADE_FINISHED".equals(response.getTradeStatus())) {
                //IpId
                System.out.println("支付成功");
                //改支付状态
                PaymentInfo paymentInfoUpd = new PaymentInfo();
                paymentInfoUpd.setPaymentStatus(PaymentStatus.PAID);
                updatePaymentInfo(paymentInfo.getOutTradeNo(), paymentInfoUpd);
                sendPaymentResult(paymentInfo, "success");
                return true;

            } else {
                System.out.println("支付失败");
                return false;
            }

        } else {
            System.out.println("支付失败");
            return false;
        }


    }

    @Override
    public void sendPaymentResult(PaymentInfo paymentInfo, String result) {
        Connection connection = activeMQUtil.getConnection();

        try {
            connection.start();
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            // 创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            ActiveMQMapMessage activeMQMapMessage = new ActiveMQMapMessage();
            activeMQMapMessage.setString("orderId", paymentInfo.getOrderId());
            activeMQMapMessage.setString("result", result);
            // 发送消息
            producer.send(activeMQMapMessage);
            // 提交
            session.commit();
            // 关闭
            producer.close();
            session.close();
            connection.close();


        } catch (JMSException e) {
            e.printStackTrace();
        }


    }

    @Override
    public void savePaymentInfo(PaymentInfo paymentInfo) {
        paymentInfoMapper.insertSelective(paymentInfo);

    }

    @Override
    public PaymentInfo getPaymentInfo(PaymentInfo paymentInfo) {
        return paymentInfoMapper.selectOne(paymentInfo);
    }

    @Override
    public void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfo) {
        Example example = new Example(PaymentInfo.class);
        example.createCriteria().andEqualTo("outTradeNo", out_trade_no);
        paymentInfoMapper.updateByExampleSelective(paymentInfo, example);


    }

//
//    AlipayClient alipayClient = new DefaultAlipayClient("https://openapi.alipay.com/gateway.do","app_id","your private_key","json","GBK","alipay_public_key");
//    AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
//request.setBizContent("{" +
//        "    \"out_trade_no\":\"20150320010101001\"," +
//        "    \"trade_no\":\"2014112611001004680073956707\"," +
//        "    \"refund_amount\":200.12," +
//        "    \"refund_reason\":\"正常退款\"," +
//        "    \"out_request_no\":\"HZ01RF001\"," +
//        "    \"operator_id\":\"OP001\"," +
//        "    \"store_id\":\"NJ_S_001\"," +
//        "    \"terminal_id\":\"NJ_T_001\"" +
//        "  }");
//    AlipayTradeRefundResponse response = alipayClient.execute(request);
//if(response.isSuccess()){
//        System.out.println("调用成功");
//    } else {
//        System.out.println("调用失败");
//    }
//


    @Override
    public boolean refund(String orderId) {
        AlipayTradeRefundRequest request = new AlipayTradeRefundRequest();
        // 根据orderId 查询OrderInfo
        OrderInfo orderInfo = orderService.getOrderInfo(orderId);
        HashMap<String, Object> map = new HashMap<>();
        map.put("out_trade_no", orderInfo.getOutTradeNo());
        map.put("refund_amount", orderInfo.getTotalAmount());
        map.put("out_request_no", "HZ01RF001271");
        //"    \"out_request_no\":\"HZ01RF001\"," +
        request.setBizContent(JSON.toJSONString(map));
        AlipayTradeRefundResponse response = null;
        try {
            response = alipayClient.execute(request);
        } catch (AlipayApiException e) {
            e.printStackTrace();
        }
        if (response.isSuccess()) {
            System.out.println("调用成功");
            return true;
        } else {
            System.out.println("调用失败");
            return false;
        }

    }

    @Override
    public Map createNative(String orderId, String total_fee) {
        //1.创建参数
        HashMap<String, String> param = new HashMap<>();
        param.put("appid", appid);//公众号
        param.put("mch_id", partner);//商户号
        param.put("nonce_str", WXPayUtil.generateNonceStr());//随机字符串
        param.put("body", "尚硅谷");//商品描述
        param.put("out_trade_no", orderId);//商户订单号
        param.put("total_fee", total_fee);//总金额（分）
        param.put("spbill_create_ip", "127.0.0.1");//IP
        param.put("notify_url", "http://order.gmall.com/trade");//回调地址(随便写)
        param.put("trade_type", "NATIVE");//交易类型
        //2.生成要发送的xml
        String xmlParam = null;
        try {
            xmlParam = WXPayUtil.generateSignedXml(param, partnerkey);
            System.out.println(xmlParam);
            HttpClient client = new HttpClient("https://api.mch.weixin.qq.com/pay/unifiedorder");
            client.setHttps(true);
            client.setXmlParam(xmlParam);
            client.post();
            //3.获得结果
            String result = client.getContent();
            System.out.println(result);
            Map<String, String> resultMap = WXPayUtil.xmlToMap(result);
            HashMap<String, String> map = new HashMap<>();
            map.put("code_url", resultMap.get("code_url"));
            map.put("total_fee", total_fee);//总额
            map.put("out_trade_no", orderId);//订单号
            return map;

        } catch (Exception e) {
            e.printStackTrace();
            return new HashMap();
        }

    }

    /**
     * 延迟队列反复调用
     *
     * @param outTradeNo 单号
     * @param delaySec   延迟秒
     * @param checkCount 几次
     */

    @Override
    public void sendDelayPaymentResult(String outTradeNo, int delaySec, int checkCount) {
        Connection connection = activeMQUtil.getConnection();
        try {
            connection.start();
            //创建会话
            Session session = connection.createSession(true, Session.SESSION_TRANSACTED);
            //创建队列
            Queue paymentResultQueue = session.createQueue("PAYMENT_RESULT_CHECK_QUEUE");
            MessageProducer producer = session.createProducer(paymentResultQueue);
            MapMessage mapMessage = new ActiveMQMapMessage();
            mapMessage.setString("outTradeNo", outTradeNo);
            mapMessage.setInt("delaySec", delaySec);
            mapMessage.setInt("checkCount", checkCount);
            //设置延迟多少时间
            mapMessage.setLongProperty(ScheduledMessage.AMQ_SCHEDULED_DELAY, delaySec * 1000);
            producer.send(mapMessage);
            session.commit();
            producer.close();
            session.close();
            connection.close();
        } catch (JMSException e) {
            e.printStackTrace();
        }


    }
    // 关闭支付信息
    @Override
    public void closePayment(String id) {
        Example example = new Example(PaymentInfo.class);
          example.createCriteria().andEqualTo("paymentStatus",PaymentStatus.ClOSED);
        PaymentInfo paymentInfo = new PaymentInfo();
        paymentInfo.setPaymentStatus(PaymentStatus.ClOSED);
        paymentInfoMapper.updateByExampleSelective(paymentInfo,example);
    }


}
