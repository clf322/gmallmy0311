package com.atguigu.gmallmy0311.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmallmy0311.bean.SkuInfo;
import com.atguigu.gmallmy0311.bean.SkuLsInfo;
import com.atguigu.gmallmy0311.service.ListService;
import com.atguigu.gmallmy0311.service.ManageService;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.BeansException;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.InvocationTargetException;

@CrossOrigin
@RestController
public class SkuManageController {
    @Reference
    ManageService manageService;

    @Reference
    ListService listService;

    @RequestMapping("saveSkuInfo")
    public String saveSkuInfo(@RequestBody SkuInfo skuInfo) {
        if (skuInfo!=null){
            manageService.saveSkuInfo(skuInfo);
        }

       return  "ok";
    }


    @RequestMapping(value = "onSale",method = RequestMethod.GET)
   public String onSale (String skuId){
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
        SkuLsInfo skuLsInfo = new SkuLsInfo();

            BeanUtils.copyProperties(skuInfo,skuLsInfo);

        listService.saveSkuLsInfo(skuLsInfo);

          return "ok";
    }






}
