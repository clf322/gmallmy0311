package com.atguigu.gmallmy0311.cart.mapper;

import com.atguigu.gmallmy0311.bean.CartInfo;
import tk.mybatis.mapper.common.Mapper;

import java.util.List;

//public interface CartInfoMapper extends Mapper<CartInfo> {
//
//
//    List<CartInfo> selectCartListWithCurPrice(String userId);
//}

public interface CartInfoMapper extends Mapper<CartInfo> {
    /**
     * 根据userId 查询数据
     * @param userId
     * @return
     */
    List<CartInfo> selectCartListWithCurPrice(String userId);
}
