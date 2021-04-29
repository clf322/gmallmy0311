package com.atguigu.gmallmy0311.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmallmy0311.bean.*;
import com.atguigu.gmallmy0311.service.ManageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.lang.reflect.Method;
import java.util.List;

@Controller
@CrossOrigin
public class ManageController {
       @Reference
       ManageService manageService;

    @RequestMapping(value = "index")
   public String index  (){
        return  "index";
   }

@RequestMapping("getCatalog1")
@ResponseBody
  public List<BaseCatalog1> getCatalog1(){
   return   manageService.getCatalog1();
}


    @RequestMapping("getCatalog2")
    @ResponseBody
    public List<BaseCatalog2> getCatalog2(String catalog1Id){
       return    manageService.getCatalog2(catalog1Id);
    }


    @RequestMapping("getCatalog3")
    @ResponseBody
    public List<BaseCatalog3> getCatalog3(String catalog2Id){
        return    manageService.getCatalog3(catalog2Id);
    }

    @RequestMapping("attrInfoList")
    @ResponseBody
    public List<BaseAttrInfo> attrInfoList(String catalog3Id){
       return   manageService.getAttrList(catalog3Id);
    }

    @RequestMapping("saveAttrInfo")
    @ResponseBody
    public void saveAttrInfo(@RequestBody BaseAttrInfo baseAttrInfo ){
            manageService.saveAttrInfo(baseAttrInfo);
    }



    @RequestMapping("getAttrValueList")
    @ResponseBody
    public List<BaseAttrValue> getAttrValueList(String attrId ){

    BaseAttrInfo baseAttrInfo= manageService.getAttrInfo(attrId);
        return  baseAttrInfo.getAttrValueList();
    }

    @RequestMapping("baseSaleAttrList")
    @ResponseBody
    public List<BaseSaleAttr> getbaseSaleAttrList( ){
        return   manageService.getbaseSaleAttrList();

    }





}
