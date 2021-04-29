package com.atguigu.gmallmy0311.service;

import com.atguigu.gmallmy0311.bean.*;

import java.util.List;

public interface ManageService {
    public List<BaseCatalog1> getCatalog1();
    public List<BaseCatalog2> getCatalog2(String catalog1Id);
    public List<BaseCatalog3> getCatalog3(String catalog2Id);
    public List<BaseAttrInfo> getAttrList(String catalog3Id);

   void saveAttrInfo(BaseAttrInfo baseAttrInfo);

    List<BaseAttrValue> getAttrValueList(String attrId);
    BaseAttrInfo getAttrInfo(String attrId);


    List<SpuInfo> getspuList(SpuInfo spuInfo);

    List<BaseSaleAttr> getbaseSaleAttrList();

    void saveSpuInfo( SpuInfo spuInfo);

    // 根据spuId获取spuImage中的所有图片列表
    List<SpuImage> getSpuImageList(String spuId);

    List<SpuSaleAttr> getSpuSaleAttrList(String spuId);

    void saveSkuInfo(SkuInfo skuInfo);

    SkuInfo getSkuInfo(String skuId);

    List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo);

    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId);


    List<BaseAttrInfo> getAttrList(List<String> attrValueIdList);
}
