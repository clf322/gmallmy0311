package com.atguigu.gmallmy0311.list.controller;


import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.*;
import com.atguigu.gmallmy0311.service.ListService;
import com.atguigu.gmallmy0311.service.ManageService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.persistence.Id;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

@Controller
public class ListController {
    @Reference
    ListService listService;
    @Reference
    private ManageService manageService;


//  @RequestMapping("list.html")
//     public String getList  (SkuLsParams skuLsParams){
//
//      SkuLsResult search=listService.search(skuLsParams);
//        return JSON.toJSONString(search);
//
//     }

    @RequestMapping("list.html")
    public String getList(SkuLsParams skuLsParams, Model model) {
// 设置每页显示的条数
        skuLsParams.setPageSize(2);
        //根据参数返回sku列表
        SkuLsResult skuLsResult = listService.search(skuLsParams);
        model.addAttribute("totalPages", skuLsResult.getTotalPages());
        model.addAttribute("pageNo",skuLsParams.getPageNo());

        model.addAttribute("skuLsInfoList", skuLsResult.getSkuLsInfoList());
        // 从结果中取出平台属性值列表
        List<String> attrValueIdList = skuLsResult.getAttrValueIdList();
        List<BaseAttrInfo> attrList = manageService.getAttrList(attrValueIdList);
        // 已选的属性值列表\
        List<BaseAttrValue> baseAttrValuesList  = new ArrayList<>();
        String urlParam = makeUrlParam(skuLsParams);
        for (Iterator<BaseAttrInfo> iterator = attrList.iterator(); iterator.hasNext(); ) {
            BaseAttrInfo baseAttrInfo = iterator.next();
            List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
            for (BaseAttrValue baseAttrValue : attrValueList) {
                if (skuLsParams.getValueId() != null && skuLsParams.getValueId().length > 0) {
                    for (String valueId : skuLsParams.getValueId()) {
                        //选中的属性值 和 查询结果的属性值
                        if (valueId.equals(baseAttrValue.getId())) {
                       iterator.remove();
                            // 构造面包屑列表
                            BaseAttrValue baseAttrValueSelected  = new BaseAttrValue();
                            baseAttrValueSelected.setValueName(baseAttrInfo.getAttrName()+":"+baseAttrValue.getValueName());
                            // 去除重复数据
                            String makeUrlParam  = makeUrlParam(skuLsParams, valueId);
                            baseAttrValueSelected.setUrlParam(makeUrlParam);
                        baseAttrValuesList.add(baseAttrValueSelected);

                        }


                    }


                }


            }

        }
        // 保存面包屑清单
         model.addAttribute("baseAttrValuesList",baseAttrValuesList);
        model.addAttribute("keyword",skuLsParams.getKeyword());
        model.addAttribute("urlParam",urlParam);
        model.addAttribute("attrList", attrList);
        // 获取sku属性值列表
        List<SkuLsInfo> skuLsInfoList= skuLsResult.getSkuLsInfoList();
        model.addAttribute("skuLsInfoList",skuLsInfoList);
        return "list";
    }

    private String makeUrlParam(SkuLsParams skuLsParam,String... excludeValueIds) {
        String urlParam="";
        if (skuLsParam.getKeyword()!=null&&skuLsParam.getKeyword().length()>0){
            urlParam+="keyword="+skuLsParam.getKeyword();
        }
         if (skuLsParam.getCatalog3Id()!=null&&skuLsParam.getCatalog3Id().length()>0){
             if (urlParam.length()>0){
                 urlParam+="&";
            }
             urlParam+="catalog3Id="+skuLsParam.getCatalog3Id();
         }

// 构造属性参数
        if (skuLsParam.getValueId()!=null&&skuLsParam.getValueId().length>0){
            for (int i = 0; i < skuLsParam.getValueId().length; i++) {
                String valueId = skuLsParam.getValueId()[i];
                if (excludeValueIds!=null&&excludeValueIds.length>0){
                    String excludeValueId = excludeValueIds[0];
                    if (excludeValueId.equals(valueId)){
                        // 跳出代码，后面的参数则不会继续追加【后续代码不会执行】
// 不能写break；如果写了break；其他条件则无法拼接！

                        continue;
                    }
                }

               if (urlParam.length()>0){
                   urlParam+="&";
               }
                urlParam+="valueId="+valueId;
            }

        }

        return  urlParam;
    }













}
