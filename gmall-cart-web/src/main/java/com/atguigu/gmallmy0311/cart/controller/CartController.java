package com.atguigu.gmallmy0311.cart.controller;

import com.alibaba.dubbo.config.annotation.Reference;

import com.atguigu.gmallmy0311.bean.CartInfo;
import com.atguigu.gmallmy0311.bean.SkuInfo;
import com.atguigu.gmallmy0311.config.LoginRequire;
import com.atguigu.gmallmy0311.service.CartService;
import com.atguigu.gmallmy0311.service.ManageService;
import org.apache.catalina.servlet4preview.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Controller
public class CartController {

    @Reference
    private CartService cartService;

    @Autowired
    private CartCookieHandler cartCookieHandler;

    @Reference
    private ManageService manageService;

    // 记录未登录的userId 给UUID
    private String userKey;






@RequestMapping("toTrade")
@LoginRequire(autoRedirect = true)
   public String  toTrade  (HttpServletRequest request,HttpServletResponse response){
    String  userId = (String) request.getAttribute("userId");
    List<CartInfo> cookieHandlerCartList  = cartCookieHandler.getCartList(request);
         if (cookieHandlerCartList!=null&&cookieHandlerCartList.size()>0){
             cartService.mergeToCartList(cookieHandlerCartList,userId);
             cartCookieHandler.deleteCartCookie(request,response);
         }
    return "redirect://trade.gmall.com/trade";

}




@RequestMapping("checkCart")
   @ResponseBody
@LoginRequire(autoRedirect=false )
       public void checkCart (HttpServletRequest request,HttpServletResponse response){
    String skuId = request.getParameter("skuId");
    String isChecked = request.getParameter("isChecked");
    String  userId = (String)request.getAttribute("userId");
    //如果用户已经登录
      if (userId!=null){
          cartService.checkCart(skuId,isChecked,userId);

      }else{
          //如果未登录
          cartCookieHandler.checkCart(request,response,skuId,isChecked);

      }

}






    // 如何判断当前是否登录！获取userId
    // 控制器是谁？ item.html 找！
    @RequestMapping("addToCart")
    @LoginRequire(autoRedirect = false)
    public String addToCart(HttpServletRequest request, HttpServletResponse response){

        // 应该将对应的商品信息做一个保存
        // 调用服务层将商品数据添加到redis ，mysql
        // 获取userId
        String userId = (String) request.getAttribute("userId");
        // 获取购买的数量，商品Id
        String skuNum = request.getParameter("skuNum");
        String skuId = request.getParameter("skuId");

        if (userId!=null){
            // 登录状态添加购物车
            cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
        }else {
            // 未登录添加购物车！放入cookie 中！
             cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
//            userKey= getUUID(request,response);
//            // 添加
//            // 将其放入cookie
//            Cookie cookie = new Cookie("user-key",userKey);
//            // 将cookie 写给客户端
//            response.addCookie(cookie);
          cartService.addToCartRedis(skuId,userKey,Integer.parseInt(skuNum));
        }

        // 通过skuId查询skuInfo
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        request.setAttribute("skuNum",skuNum);

        request.setAttribute("skuInfo",skuInfo);

        return "success";
    }

//    private String getUUID(HttpServletRequest request,HttpServletResponse response) {
//        // 获取cookie中的UUID
//        Cookie[] cookies = request.getCookies();
//        boolean isMatch = false;
//        if (cookies != null && cookies.length > 0) {
//
//            for (Cookie cks : cookies) {
//                if (cks.getName().equals("user-key")) {
//                    userKey = cks.getValue();
//                    isMatch = true;
//                }
//            }
//        }
//            if (!isMatch) {
//                // 记录未登录的userId 给UUID
//                userKey = UUID.randomUUID().toString().replace("-", "");
//
//            }
//
//        return userKey;
//    }
//
//
//








    private String  getUUID(HttpServletRequest request, HttpServletResponse response) {
        //获取cookie中的uuid  如果没有进行下一步添加
        Cookie[] cookies = request.getCookies();

        //判断cookie中是否有数据
        if (cookies != null && cookies.length > 0) {

            //进行遍历获取其中的值
            for (Cookie cks : cookies) {

                //判断获取到的cookie中的数据名称是否等于我们放进去的key
                if (cks.getName().equals("user-key")){

                    //如果相等 取值进行赋值 说明是同一个人使用多次【如果不同的人应该不同的key】
                    userKey = cks.getValue();
                } else {
                    //记录未登录的 userId 给UUID
                    userKey = UUID.randomUUID().toString().replace("-","");
                }
            }
        }
        return userKey;
    }


    @RequestMapping("cartList")
    @LoginRequire(autoRedirect = false)
    public String cartList(HttpServletRequest request,HttpServletResponse response){

        List<CartInfo> cartInfoList = new ArrayList<>();
        // 获取userId
        String userId = (String) request.getAttribute("userId");
        if (userId!=null){
            // 先看未登录购物车中是否有数据
            List<CartInfo> cartListCK = cartCookieHandler.getCartList(request);
            if (cartListCK!=null && cartListCK.size()>0){
                // 合并购物车
                cartInfoList = cartService.mergeToCartList(cartListCK,userId);
                // 删除未登录数据
                cartCookieHandler.deleteCartCookie(request,response);
            }else {
                // redis-mysql
                cartInfoList = cartService.getCartList(userId);
            }
        }else {
            // cookie
            cartInfoList = cartCookieHandler.getCartList(request);
//            //cartInfoList = cartService.getCartList(userId);
//            userKey= getUUID(request,response);
//            cartInfoList=  cartService.getCartListRedis(userKey);

        }
        // 保存购物车集合
        request.setAttribute("cartList",cartInfoList);
        return "cartList";
    }
}
