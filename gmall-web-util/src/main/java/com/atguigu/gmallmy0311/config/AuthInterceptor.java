package com.atguigu.gmallmy0311.config;

import com.alibaba.fastjson.JSON;

import com.atguigu.gmallmy0311.util.HttpClientUtil;
import io.jsonwebtoken.impl.Base64UrlCodec;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

@Component
public class AuthInterceptor extends HandlerInterceptorAdapter {
    public  boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)throws  Exception{
                 //1.获取token值
        String token = request.getParameter("newToken");
        //把token保存到cookie
        if(token!=null){
            CookieUtil.setCookie(request,response,"token",token, WebConst.COOKIE_MAXAGE,false);
        }
      if (token==null){
            token =  CookieUtil.getCookieValue(request,"token",false);
      }

      if (token!=null){
//读取token
          Map map=getUserMapByToken(token);
          String  nickName = (String) map.get("nickName");
          request.setAttribute("nickName",nickName);

      }
        // 判断当前控制器上是否有注解LoginRequire
        HandlerMethod handlerMethod=(HandlerMethod)handler;
        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
     if (methodAnnotation!=null){
         // 获取到的注解
         // 认证：用户是否登录的认证调用PassPortController中verify 控制器
         // http://passport.atguigu.com/verify?token=xxx&salt=xxx
         // http://passport.atguigu.com/verify
         // 如何获取salt
        String salt = request.getHeader("X-forwarded-for");
         String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token+ "&salt=" + salt);
         // 判断执行结果
         if ("success".equals(result)){
             // 保存一下userId
             Map map = getUserMapByToken(token);
             String userId = (String)map.get("userId");
             // 保存nickName
             request.setAttribute("userId",userId);
             return true;
         }else {
// 什么清空下必须登录！
             if (methodAnnotation.autoRedirect()){
                 // 必须登录！
                 // http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F35.html
                 // 获取浏览器的url
                 String requestURL = request.getRequestURL().toString();
                 System.out.println("requestURL:"+requestURL);
                 // 对url 进行转码
                 String encodeURL = URLEncoder.encode(requestURL, "UTF-8");
                 System.out.println( "encodeURL:"+encodeURL);
                 // 重定向到登录页面 http://passport.atguigu.com/index
                 response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
                 return  false;
             }

         }


     }

        return true;


    }

    private Map getUserMapByToken(String token) {
        String tokenUserInfo  = StringUtils.substringBetween(token, ".");
        Base64UrlCodec  base64UrlCodec  = new Base64UrlCodec();
        byte[] tokenBytes  = base64UrlCodec.decode(tokenUserInfo);
          String tokenJson =null;
        try {
            tokenJson  = new String(tokenBytes,"UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }

        Map map = JSON.parseObject(tokenJson, Map.class);
         return map;

    }
    // 表示进入控制器之后，返回试图之前
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
    }
    // 返回试图之后
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
    }

}
