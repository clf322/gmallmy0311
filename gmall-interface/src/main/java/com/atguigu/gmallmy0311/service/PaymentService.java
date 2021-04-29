package com.atguigu.gmallmy0311.service;

import com.atguigu.gmallmy0311.bean.PaymentInfo;

import java.util.Map;

public interface PaymentService {

    boolean checkPayment(PaymentInfo paymentInfoQuery);

    public void sendPaymentResult(PaymentInfo paymentInfo,String result);
    void savePaymentInfo(PaymentInfo paymentInfo);

    PaymentInfo getPaymentInfo(PaymentInfo paymentInfo);

    void updatePaymentInfo(String out_trade_no, PaymentInfo paymentInfoUpd);

    boolean refund(String orderId);

    Map createNative(String s, String s1);

    public void sendDelayPaymentResult(String outTradeNo,int delaySec ,int checkCount);

    public void closePayment(String id) ;
}

