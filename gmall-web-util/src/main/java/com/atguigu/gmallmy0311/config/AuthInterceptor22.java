//package com.atguigu.gmallmy0311.config;
//
//
//import com.alibaba.fastjson.JSON;
//import com.atguigu.gmallmy0311.util.HttpClientUtil;
//import io.jsonwebtoken.impl.Base64UrlCodec;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.stereotype.Component;
//import org.springframework.web.method.HandlerMethod;
//import org.springframework.web.servlet.ModelAndView;
//import org.springframework.web.servlet.handler.HandlerInterceptorAdapter;
//
//import javax.servlet.http.HttpServletRequest;
//import javax.servlet.http.HttpServletResponse;
//import java.io.UnsupportedEncodingException;
//import java.net.URLEncoder;
//import java.util.Map;
//
//@Component
//public class AuthInterceptor extends HandlerInterceptorAdapter{
//
//    // 表示进入控制器之前
//    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
//        // http://item.gmall.com/35.html?newToken=eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
//        // 表示登录成功之后 获取newToken
//        String token = request.getParameter("newToken");
//        // 当token 不为空的时候，将token 放入cookie中
//        if (token!=null){
//            CookieUtil.setCookie(request,response,"token",token,WebConst.COOKIE_MAXAGE,false);
//        }
//        //http://item.gmall.com/33.html 当用户访问其他业务模块的时候，此时没有newToken ,但是cookie 有可能存在了token
//        if (token==null){
//            // cookie 有可能存在了token
//            token = CookieUtil.getCookieValue(request,"token",false);
//        }
//        // 从token中获取用户昵称！
//        if (token!=null){
//            // 获取nickName
//            Map map = getUserMapByToken(token);
//            String nickName = (String) map.get("nickName");
//            // 保存nickName
//            request.setAttribute("nickName",nickName);
//        }
//
//        // 判断当前控制器上是否有注解LoginRequire
//        HandlerMethod handlerMethod = (HandlerMethod) handler;
//        // 看方法上是否有注解
//        LoginRequire methodAnnotation = handlerMethod.getMethodAnnotation(LoginRequire.class);
//
//        if (methodAnnotation!=null){
//            // 获取到的注解
//            // 认证：用户是否登录的认证调用PassPortController中verify 控制器
//            // http://passport.atguigu.com/verify?token=xxx&salt=xxx
//            // http://passport.atguigu.com/verify
//            // 如何获取salt
//            String salt = request.getHeader("X-forwarded-for");
//            String result = HttpClientUtil.doGet(WebConst.VERIFY_ADDRESS + "?token=" + token + "&salt=" + salt);
//            // 判断执行结果
//            if ("success".equals(result)){
//                // 保存一下userId
//                Map map = getUserMapByToken(token);
//                String userId = (String) map.get("userId");
//                // 保存nickName
//                request.setAttribute("userId",userId);
//                return true;
//            }else {
//                // 什么清空下必须登录！
//                if (methodAnnotation.autoRedirect()){
//                    // 必须登录！
//                    // http://passport.atguigu.com/index?originUrl=http%3A%2F%2Fitem.gmall.com%2F35.html
//                    // 获取浏览器的url
//                    String requestURL  = request.getRequestURL().toString();
//                    System.out.println("requestURL:"+requestURL); // http://item.gmall.com/34.html/
//                    // 对url 进行转码
//                    String encodeURL  = URLEncoder.encode(requestURL, "UTF-8");
//                    System.out.println("encodeURL:"+encodeURL); // http%3A%2F%2Fitem.gmall.com%2F35.html
//
//                    // 重定向到登录页面 http://passport.atguigu.com/index
//                    response.sendRedirect(WebConst.LOGIN_ADDRESS+"?originUrl="+encodeURL);
//
//                    return false;
//                }
//            }
//
//        }
//
//        return true;
//    }
//    // 获取map集合
//    private Map getUserMapByToken(String token) {
//        // eyJhbGciOiJIUzI1NiJ9.eyJuaWNrTmFtZSI6IkF0Z3VpZ3UiLCJ1c2VySWQiOiIxIn0.XzRrXwDhYywUAFn-ICLJ9t3Xwz7RHo1VVwZZGNdKaaQ
//        // map属于第二部分 可以使用JWTUtil 工具类 ，base64编码
//        // 获取token 中第二部分 数据
//        String tokenUserInfo  = StringUtils.substringBetween(token, ".");
//        // 创建base64 对象
//        Base64UrlCodec base64UrlCodec = new Base64UrlCodec();
//        // 返回字节数组
//        byte[] decode = base64UrlCodec.decode(tokenUserInfo);
//        // 将字节数组转化为String
//        String tokenJson =null;
//        try {
//            tokenJson = new String(decode,"UTF-8");
//        } catch (UnsupportedEncodingException e) {
//            e.printStackTrace();
//        }
//
//        return JSON.parseObject(tokenJson,Map.class);
//
//    }
//
//    // 表示进入控制器之后，返回试图之前
//    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
//    }
//    // 返回试图之后
//    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
//    }
//}
