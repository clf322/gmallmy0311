package com.atguigu.gmallmy0311.controller;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.SkuInfo;
import com.atguigu.gmallmy0311.bean.SkuSaleAttrValue;
import com.atguigu.gmallmy0311.bean.SpuSaleAttr;
import com.atguigu.gmallmy0311.config.LoginRequire;
import com.atguigu.gmallmy0311.service.ListService;
import com.atguigu.gmallmy0311.service.ManageService;

import org.springframework.stereotype.Controller;

import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;

@Controller
public class ItemController {

    @Reference
    private ManageService manageService;

    @Reference
    ListService listService;

//    @RequestMapping("{skuId}.html")
//    public String skuInfoPage(@PathVariable(value = "skuId") String skuId){
//        return "item";
//    }

//    @RequestMapping("{skuId}.html")
//    public String  skuInfoPage  (@PathVariable(value = "skuId") String skuId, Model model){
//        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//        model.addAttribute("skuInfo",skuInfo);
//        return "item";
//
//    }


    @LoginRequire(autoRedirect = false)
   @RequestMapping("/{skuId}.html")//HttpServletRequest request
    public String getSkuInfo(@PathVariable("skuId") String skuId,Model model ){
        // 存储基本的skuInfo信息
        SkuInfo skuInfo = manageService.getSkuInfo(skuId);

        // 存储 spu，sku数据
        // 获取销售属性结果集：
        List<SpuSaleAttr> spuSaleAttrList = manageService.getSpuSaleAttrListCheckBySku(skuInfo);
        List<SkuSaleAttrValue> skuSaleAttrValueList  = manageService.getSkuSaleAttrValueListBySpu(skuInfo.getSpuId());
        String key="";
        HashMap <String,String> map=new HashMap<>();
        for (int i = 0; i < skuSaleAttrValueList.size(); i++) {
            SkuSaleAttrValue skuSaleAttrValue = skuSaleAttrValueList.get(i);
            if (key.length()>0){
                key+="|";
            }
            key+=skuSaleAttrValue.getSaleAttrValueId();
            if ((i+1)==skuSaleAttrValueList.size()||!skuSaleAttrValue.getSkuId().equals(skuSaleAttrValueList.get(i+1).getSkuId())){
               map.put(key,skuSaleAttrValue.getSkuId());
               key="";
            }

        }
        String valuesSkuJson  = JSON.toJSONString(map);
     //   request.setAttribute("valuesSkuJson",valuesSkuJson );
      model.addAttribute("valuesSkuJson",valuesSkuJson);
      //  request.setAttribute("spuSaleAttrList",spuSaleAttrList);
       model.addAttribute("spuSaleAttrList",spuSaleAttrList);

      model.addAttribute("skuInfo",skuInfo);
        System.out.println("skuInfo==============="+skuInfo);
       // request.setAttribute("skuInfo",skuInfo);
       listService.incrHotScore(skuId);
        return "item";
    }






}
