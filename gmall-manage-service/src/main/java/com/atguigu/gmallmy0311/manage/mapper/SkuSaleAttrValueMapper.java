package com.atguigu.gmallmy0311.manage.mapper;

import com.atguigu.gmallmy0311.bean.SkuSaleAttrValue;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface SkuSaleAttrValueMapper extends Mapper<SkuSaleAttrValue> {
    public List<SkuSaleAttrValue> selectSkuSaleAttrValueListBySpu (String spuId);

}
