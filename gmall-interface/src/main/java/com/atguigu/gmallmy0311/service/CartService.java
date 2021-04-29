package com.atguigu.gmallmy0311.service;


import com.atguigu.gmallmy0311.bean.CartInfo;

import java.util.List;

public interface CartService {

    // 返回值，参数列表

    /**
     *
     * @param skuId 商品Id
     * @param userId 用户Id
     * @param skuNum 商品 数量
     */
    void  addToCart(String skuId,String userId,Integer skuNum);

    /**
     *
     * 根据userId 查询购物车数据
     * @param userId
     * @return
     */
    List<CartInfo> getCartList(String userId);

    /**
     * 合并购物车
     * @param cartListCK
     * @param userId
     * @return
     */
    List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId);

    /**
     *
     * @param skuId
     * @param userKey
     * @param i
     */
    void addToCartRedis(String skuId, String userKey, int i);

    public List<CartInfo> getCartListRedis(String userKey);

    void checkCart(String skuId, String isChecked, String userId);

    List<CartInfo> getCartCheckedList(String userId);
}
