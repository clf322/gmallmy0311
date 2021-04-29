package com.atguigu.gmallmy0311.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;

import com.atguigu.gmallmy0311.bean.CartInfo;
import com.atguigu.gmallmy0311.bean.SkuInfo;
import com.atguigu.gmallmy0311.config.CookieUtil;
import com.atguigu.gmallmy0311.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;

// 操作未登录的数据
@Component
public class CartCookieHandler {
    // 定义购物车名称
    private String cookieCartName = "CART";
    // 设置cookie 过期时间
    private int COOKIE_CART_MAXAGE=7*24*3600;

    @Reference
    private ManageService manageService;


    /**
     *
     * @param request
     * @param response
     * @param skuId
     * @param userId
     * @param skuNum
     */
    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, int skuNum) {
        /*
            1.  判断购物车中是否有该商品 通过skuId 去cookie 中循环比较
            2.  有： 数量相加
            3.  没有：直接添加到cookie
         */
        // 获取购物车中的所有数据
        // 应该很多条数据
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);

        List<CartInfo> cartInfoList = new ArrayList<>();
        // 定义一个 boolean 类型的变量
        boolean ifExist=false;
        if (StringUtils.isNotEmpty(cookieValue)){
            cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
            // 通过skuId 去cookie 中循环比较
            for (CartInfo cartInfo : cartInfoList) {
                if (cartInfo.getSkuId().equals(skuId)){
                    // 数量相加
                    cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
                    cartInfo.setSkuPrice(cartInfo.getCartPrice());
                    ifExist = true;
                }
            }
        }
        // 不存在！
        if (!ifExist){
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo = new CartInfo();

            cartInfo.setSkuId(skuId);
            cartInfo.setCartPrice(skuInfo.getPrice());
            cartInfo.setSkuPrice(skuInfo.getPrice());
            cartInfo.setSkuName(skuInfo.getSkuName());
            cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());

            cartInfo.setUserId(userId);
            cartInfo.setSkuNum(skuNum);
            // 将cartInfo  放入集合
            cartInfoList.add(cartInfo);
        }
        // 写入cookie
        CookieUtil.setCookie(request,response,cookieCartName,JSON.toJSONString(cartInfoList),COOKIE_CART_MAXAGE,true);
    
    }

    /**
     * 获取购物车数据
     * @param request
     * @return
     */
    public List<CartInfo> getCartList(HttpServletRequest request) {
        List<CartInfo> cartInfoList = new ArrayList<>();
        String cookieValue = CookieUtil.getCookieValue(request, cookieCartName, true);
        if (StringUtils.isNotEmpty(cookieValue)){
           cartInfoList = JSON.parseArray(cookieValue, CartInfo.class);
        }
        return cartInfoList;
    }

    /**
     *  删除数据
     * @param request
     * @param response
     */
    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
        CookieUtil.deleteCookie(request,response,cookieCartName);
    }

    public void checkCart(HttpServletRequest request, HttpServletResponse response, String skuId, String isChecked) {
//  取出购物车中的商品
        List<CartInfo> cartList = getCartList(request);
        // 循环比较
        for (CartInfo cartInfo : cartList) {
            if (cartInfo.getSkuId().equals(skuId)){
                cartInfo.setIsChecked(isChecked);
            }

        }
        // 保存到cookie
        String newCartJson  = JSON.toJSONString(cartList);
        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);


    }
}
