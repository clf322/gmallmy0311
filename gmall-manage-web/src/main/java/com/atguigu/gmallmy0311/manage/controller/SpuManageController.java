package com.atguigu.gmallmy0311.manage.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.atguigu.gmallmy0311.bean.BaseSaleAttr;
import com.atguigu.gmallmy0311.bean.SpuImage;
import com.atguigu.gmallmy0311.bean.SpuInfo;
import com.atguigu.gmallmy0311.bean.SpuSaleAttr;
import com.atguigu.gmallmy0311.service.ManageService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
@RestController
@CrossOrigin
public class SpuManageController {

@Reference
ManageService manageService;
    @RequestMapping("spuList")
    public List<SpuInfo> getspuList(SpuInfo spuInfo ){

        return   manageService.getspuList(spuInfo);

    }




    @RequestMapping("saveSpuInfo")
    public String  saveSpuInfo( @RequestBody SpuInfo spuInfo){

        manageService.saveSpuInfo(spuInfo);
        return  "OK";
    }




//    @RequestMapping("spuSaleAttrList")
//    public List<SpuSaleAttr>  spuSaleAttrList( String spuId){
//
//
//        List<SpuSaleAttr> spuSaleAttrList= manageService.getSpuImageList(spuId);
//       return  spuSaleAttrList;
//    }

    @RequestMapping("spuImageList")
    public List<SpuImage>  spuImageList(String spuId){

     return    manageService.getSpuImageList(spuId);

    }

    @RequestMapping("spuSaleAttrList")
    @ResponseBody
    public List<SpuSaleAttr> getspuSaleAttrList(String spuId){
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrList(spuId);
        return  spuSaleAttrList;
    }








}
