package com.atguigu.gmallmy0311.order.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.*;
import com.atguigu.gmallmy0311.bean.enums.OrderStatus;
import com.atguigu.gmallmy0311.bean.enums.ProcessStatus;
import com.atguigu.gmallmy0311.config.LoginRequire;
import com.atguigu.gmallmy0311.service.CartService;
import com.atguigu.gmallmy0311.service.ManageService;
import com.atguigu.gmallmy0311.service.OrderService;
import com.atguigu.gmallmy0311.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Controller
public class OrderController {

    @Reference
    private CartService cartService;

    @Reference
    private UserInfoService userInfoService;

    @Reference
    private  OrderService orderService;
    @Reference
    ManageService manageService;

    @RequestMapping("orderSplit")
    @ResponseBody
    public String orderSplit  (HttpServletRequest request){
   String orderId=request.getParameter("orderId");
          String wareSkuMap=request.getParameter("wareSkuMap");
   //定义订单集合
        List<OrderInfo> subOrderInfoList=orderService.splitOrder(orderId,wareSkuMap);
   List<Map> wareMapList=new ArrayList<>();
        for (OrderInfo  orderInfo  : subOrderInfoList) {
         Map map= orderService.initWareOrder(orderInfo);
         wareMapList.add(map);
        }
    return JSON.toJSONString(wareMapList);

    }





@RequestMapping(value = "submitOrder",method = RequestMethod.POST)
@LoginRequire
    public String  submitOrder(OrderInfo orderInfo,HttpServletRequest request ){
    String  userId  = (String) request.getAttribute("userId");
    // 检查tradeCode
    String tradeNo = request.getParameter("tradeNo");
    boolean flag=  orderService.checkTradeCode(userId,tradeNo);
       if (!flag){
           request.setAttribute("errMsg","该页面已失效，请重新结算!");
           return "tradeFail";
       }

    // 初始化参数
    orderInfo.setOrderStatus(OrderStatus.UNPAID);
    orderInfo.setProcessStatus(ProcessStatus.UNPAID);
   orderInfo.sumTotalAmount();
   orderInfo.setUserId(userId);
    // 校验，验价



    List<OrderDetail> orderDetailList = orderInfo.getOrderDetailList();
    List<OrderDetail> newOrderDetailList = new ArrayList<>();
    for (OrderDetail orderDetail : orderDetailList) {
        // 从订单中去购物skuId，数量
        boolean result =  orderService.checkStock(orderDetail.getSkuId(), orderDetail.getSkuNum());
           if (!result){
               request.setAttribute("errMsg",orderDetail.getSkuName()+"商品库存不足，请重新下单！");

               return "tradeFail";
           }
        SkuInfo skuInfo = manageService.getSkuInfo(orderDetail.getSkuId());
           if (skuInfo!=null){
               if (skuInfo.getId().equals(orderDetail.getSkuId())){
                   if (skuInfo.getPrice()!=orderDetail.getOrderPrice()){
                       orderDetail.setOrderPrice(skuInfo.getPrice());

                   }

               }

           }
        newOrderDetailList.add(orderDetail);


    }
   // orderDetailList = newOrderDetailList;

    orderInfo.setOrderDetailList(newOrderDetailList);
   orderInfo.sumTotalAmount();
    System.out.println(orderInfo);
    // 保存
    String orderId= orderService.saveOrder(orderInfo);
    // 删除tradeNo
    orderService.delTradeNo(userId);
//重定向
    return "redirect://payment.gmall.com/index?orderId="+orderId;

}




    @RequestMapping(value = "trade",method = RequestMethod.GET)
@LoginRequire
    public String  tradeInit  (HttpServletRequest request){

    String  userId = (String)request.getAttribute("userId");
    // 得到选中的购物车列表
    List<CartInfo> cartCheckedList= cartService.getCartCheckedList(userId);
    // 收货人地址
    List<UserAddress> userAddressList  = userInfoService.getUserAddressByUserId(userId);
   request.setAttribute("userAddressList",userAddressList );
    // 订单信息集合
    ArrayList<OrderDetail> orderDetailList = new ArrayList<>(cartCheckedList.size());
    for (CartInfo cartInfo : cartCheckedList) {
        OrderDetail orderDetail = new OrderDetail();
        orderDetail.setSkuId(cartInfo.getSkuId());
        orderDetail.setSkuName(cartInfo.getSkuName());
        orderDetail.setImgUrl(cartInfo.getImgUrl());
        orderDetail.setSkuNum(cartInfo.getSkuNum());
        orderDetail.setOrderPrice(cartInfo.getCartPrice());
         orderDetailList.add(orderDetail);

    }
    request.setAttribute("orderDetailList",orderDetailList);
    OrderInfo orderInfo = new OrderInfo();
    orderInfo.setOrderDetailList(orderDetailList);
    orderInfo.sumTotalAmount();
    request.setAttribute("totalAmount",orderInfo.getTotalAmount());
    String tradeNo =orderService.getTradeNo(userId);
    request.setAttribute("tradeCode",tradeNo);

    return  "trade";

}







    @RequestMapping("trade")
    @ResponseBody
   public List<UserAddress> trade  (String userId){

  return userInfoService.getUserAddressByUserId(userId);


   }








}
