package com.atguigu.gmallmy0311.user.controller;

import com.atguigu.gmallmy0311.bean.UserInfo;
import com.atguigu.gmallmy0311.service.UserInfoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

@Controller
public class UserController {

    @Autowired
    private UserInfoService userInfoService;



    @RequestMapping("findAll")
    @ResponseBody
   public  List<UserInfo> findAll(){
       return userInfoService.findAll();
   }

    @RequestMapping("addUser")
    @ResponseBody
    public  void addUser(UserInfo userInfo){
       userInfo.setLoginName("adminStr");
       userInfo.setPasswd("666666");
         userInfoService.addUser(userInfo);
         //获取添加之后的主键
        System.out.println(userInfo.getId());
    }


    @RequestMapping("updById")
    @ResponseBody
    public  String updById(UserInfo userInfo){
        userInfo.setName("0218优秀");
        userInfo.setId("5");
        userInfoService.updateUser(userInfo);
        //获取添加之后的主键
       System.out.println(userInfo.getId());
        return  "OK";
    }



}
