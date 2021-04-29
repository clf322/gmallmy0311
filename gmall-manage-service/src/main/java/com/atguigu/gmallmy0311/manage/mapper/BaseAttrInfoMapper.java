package com.atguigu.gmallmy0311.manage.mapper;

import com.atguigu.gmallmy0311.bean.BaseAttrInfo;
import org.apache.ibatis.annotations.Param;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

public interface BaseAttrInfoMapper extends Mapper<BaseAttrInfo> {

    // 根据三级分类id查询属性表
    List<BaseAttrInfo> getBaseAttrInfoListByCatalog3Id(Long catalog3Id);


    List<BaseAttrInfo> selectAttrInfoListByIds(@Param("valueIds")String valueIds);
}
