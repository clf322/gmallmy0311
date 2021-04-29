package com.atguigu.gmallmy0311.passport.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmallmy0311.bean.UserInfo;
import com.atguigu.gmallmy0311.passport.util.JwtUtil;
import com.atguigu.gmallmy0311.service.UserInfoService;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.Map;

@Controller
public class PassportController {
   @Value("${token.key}")
    String signKey;
@Reference
     UserInfoService userInfoService;




    @RequestMapping("verify")
    @ResponseBody
    public String verify  (HttpServletRequest request){
        //1.取值token 和salt
        String token=request.getParameter("token");
        String salt =request.getParameter("salt");
    //2.转码token
        Map<String,Object> map=JwtUtil.decode(token,signKey,salt );
        if (map!=null){
        //3.从map中取出userId
            String userId=(String) map.get("userId");
            //4.检查redis信息
            UserInfo userInfo=userInfoService.verify(userId);
              if(userInfo!=null){
                return  "success";
              }

        }
        return  "fail";



    }





    @RequestMapping("login")
@ResponseBody
  public String login (HttpServletRequest request, UserInfo userInfo){
//1.取得地址
    String remoteAddr=request.getHeader("X-forwarded-for");
    if (userInfo!=null){
        UserInfo loginUser  = userInfoService.login(userInfo);
       if (loginUser==null){
      return  "fail";
       }else{
// 生成token
           Map map=new HashMap();
           map.put("userId",loginUser.getId());
           map.put("nickName", loginUser.getNickName());
           String token=JwtUtil.encode(signKey,map,remoteAddr);
           return token;

       }

    }
  return  "fail";
  }










    @RequestMapping("index")
    public String  index  (HttpServletRequest request){
        String originUrl = request.getParameter("originUrl");
        // 保存上
        request.setAttribute("originUrl",originUrl);
        return "index";
    }
    @Test
    public void  test01(){
        String key = "atguigu";
        String ip="192.168.81.138";
        Map map = new HashMap();
        map.put("userId","1001");
        map.put("nickName","marry");
        String token =  JwtUtil.encode(key,map,ip);
        System.out.println("token:"+token);
        Map<String, Object> decode = JwtUtil.decode(token, key, "192.168.81.138");
        System.out.println("userId:"+decode.get("userId"));
        System.out.println("nickName:"+decode.get("nickName"));

    }
















}
