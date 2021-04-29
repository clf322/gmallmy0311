//package com.atguigu.gmallmy0311.cart.controller;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import com.atguigu.gmallmy0311.bean.CartInfo;
//import com.atguigu.gmallmy0311.bean.SkuInfo;
//import com.atguigu.gmallmy0311.config.LoginRequire;
//import com.atguigu.gmallmy0311.service.CartService;
//import com.atguigu.gmallmy0311.service.ManageService;
//import org.apache.http.HttpResponse;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.stereotype.Controller;
//import org.springframework.web.bind.annotation.RequestMapping;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.util.ArrayList;
//import java.util.List;
//
//@Controller
//public class CartController00 {
//    @Reference
//    private CartService cartService;
//    @Autowired
//    private CartCookieHandler cartCookieHandler;
//    @Reference
//    private ManageService manageService;
//
//  @RequestMapping("addToCart")
//  @LoginRequire(autoRedirect = false)
//     public String  addToCart (HttpServletRequest request, HttpServletResponse response){
//      // 获取userId，skuId，skuNum  怎么获取？
//      String skuId = request.getParameter("skuId");
//      String skuNum = request.getParameter("skuNum");
//      String  userId  = (String)request.getAttribute("userId");
//// 判断用户是否登录
//      if (userId!=null){
//          // 说明用户登录
//          cartService.addToCart(skuId,userId,Integer.parseInt(skuNum));
//      }else {
//          // 说明用户没有登录没有登录放到cookie中
//      cartCookieHandler.addToCart(request,response,skuId,userId,Integer.parseInt(skuNum));
//
//      }
//     //取得sku信息对象
//      SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//    request.setAttribute("skuInfo",skuInfo);
//    request.setAttribute("skuNum",skuNum);
//          return "success";
//
//  }
//    @RequestMapping("cartList")
//    //@LoginRequire(autoRedirect = false)
//    public String  cartList  (HttpServletRequest request,HttpServletResponse response){
//        // 判断用户是否登录，登录了从redis中，redis中没有，从数据库中取
//        // 没有登录，从cookie中取得
//        List<CartInfo> cartList = new ArrayList<>();
//        String userId = (String) request.getAttribute("userId");
//        if (userId!=null){
//
//            // 从cookie中查找购物车
//      List<CartInfo>    cartListFromCookie =    cartCookieHandler.getCartList(request);
//
//            if (cartListFromCookie!=null&&cartListFromCookie.size()>0){
//                // 开始合并
//                cartService.mergeToCartList(cartListFromCookie,userId);
//                // 删除cookie中的购物车
//                cartCookieHandler.deleteCartCookie(request,response);
//
//            }else{
//             cartList= cartService.getCartList(userId);
//
//            }
//            request.setAttribute("cartList",cartList);
//
//        }else{
//            cartList =   cartCookieHandler.getCartList(request);
//
//        }
//        request.setAttribute("cartList",cartList);
//        return "cartList";
//    }
//
//
//
//}
