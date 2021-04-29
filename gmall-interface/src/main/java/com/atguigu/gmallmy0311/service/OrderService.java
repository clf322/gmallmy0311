package com.atguigu.gmallmy0311.service;

import com.atguigu.gmallmy0311.bean.OrderDetail;
import com.atguigu.gmallmy0311.bean.OrderInfo;
import com.atguigu.gmallmy0311.bean.enums.ProcessStatus;

import java.util.List;
import java.util.Map;

public interface OrderService {
    public  String  saveOrder(OrderInfo orderInfo);

    String getTradeNo(String userId);

    void delTradeNo(String userId);

    boolean checkTradeCode(String userId, String tradeNo);


    boolean checkStock(String skuId, Integer skuNum);

    OrderInfo getOrderInfo(String orderId);

       OrderDetail getOrderDetailByOrderId(String orderId);

    void updateOrderStatus(String orderId, ProcessStatus paid);

    void sendOrderStatus(String orderId);

    List<OrderInfo> getExpiredOrderList();

     void execExpiredOrder(OrderInfo orderInfo);

    List<OrderInfo> splitOrder(String orderId, String wareSkuMap);

    Map initWareOrder(OrderInfo orderInfo);
}
