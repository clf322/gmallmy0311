//package com.atguigu.gmallmy0311.cart.controller;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import com.alibaba.fastjson.JSON;
//import com.atguigu.gmallmy0311.bean.CartInfo;
//import com.atguigu.gmallmy0311.bean.SkuInfo;
//import com.atguigu.gmallmy0311.config.CookieUtil;
//import com.atguigu.gmallmy0311.service.ManageService;
//import org.springframework.stereotype.Component;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.awt.print.Book;
//import java.util.ArrayList;
//import java.util.List;
//
//@Component
//public class CartCookieHandler11 {
////定义购物车的名字
//    private String cookieCartName="CART";
//    //设置cookie的过期时间
//    private   int  COOKIE_CART_MAXAGE=7*24*3600;
//
//    @Reference
//   private ManageService manageService;
//    public void addToCart(HttpServletRequest request, HttpServletResponse response, String skuId, String userId, Integer skuNum) {
////判断cooKie中是否有购物车，如果有可能有中文，需要序列化；
//        String cartJson = CookieUtil.getCookieValue(request, cookieCartName, true);
//        List<CartInfo> cartInfoList  = new ArrayList<>();
//         boolean ifExist=false;
//         if (cartJson!=null){
//             cartInfoList = JSON.parseArray(cartJson, CartInfo.class);
//             for (CartInfo cartInfo : cartInfoList) {
//                    if (cartInfo.getSkuId().equals(skuId)){
//                        cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
//                        //价格设置
//                        cartInfo.setSkuPrice(cartInfo.getCartPrice());
//                        ifExist=true;
//                        break;
//                    }
//             }
//
//         }
//         if (!ifExist){
//             //把商品信息取出来，新增到购物车
//             SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//             CartInfo cartInfo = new CartInfo();
//             cartInfo.setSkuId(skuId);
//             cartInfo.setCartPrice(skuInfo.getPrice());
//             cartInfo.setSkuName(skuInfo.getSkuName());
//             cartInfo.setImgUrl(skuInfo.getSkuDefaultImg());
//             cartInfo.setUserId(userId);
//             cartInfo.setSkuNum(skuNum);
//             cartInfoList.add(cartInfo);
//         }
//        // 把购物车写入cookie
//        String newCartJson  = JSON.toJSONString(cartInfoList);
//        CookieUtil.setCookie(request,response,cookieCartName,newCartJson,COOKIE_CART_MAXAGE,true);
//
//    }
//
//    public List<CartInfo> getCartList(HttpServletRequest request) {
//        String cartJson  = CookieUtil.getCookieValue(request, cookieCartName, true);
//        List<CartInfo> cartInfoList  = JSON.parseArray(cartJson, CartInfo.class);
//
//        return  cartInfoList;
//    }
//
//    public void deleteCartCookie(HttpServletRequest request, HttpServletResponse response) {
//        CookieUtil.deleteCookie(request,response,cookieCartName);
//
//
//    }
//}
