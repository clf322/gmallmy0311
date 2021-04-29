package com.atguigu.gmallmy0311.service;

import com.atguigu.gmallmy0311.bean.SkuLsInfo;
import com.atguigu.gmallmy0311.bean.SkuLsParams;
import com.atguigu.gmallmy0311.bean.SkuLsResult;

public interface ListService {
    public void saveSkuLsInfo(SkuLsInfo skuLsInfo);

    public SkuLsResult search(SkuLsParams skuLsParams);
    public void incrHotScore(String skuId);

}
