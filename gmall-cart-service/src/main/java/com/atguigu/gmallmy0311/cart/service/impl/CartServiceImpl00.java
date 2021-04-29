//package com.atguigu.gmallmy0311.cart.service.impl;
//
//import com.alibaba.dubbo.config.annotation.Reference;
//import com.alibaba.dubbo.config.annotation.Service;
//import com.alibaba.fastjson.JSON;
//import com.atguigu.gmallmy0311.bean.CartInfo;
//import com.atguigu.gmallmy0311.bean.SkuInfo;
//import com.atguigu.gmallmy0311.cart.constant.CartConst;
//import com.atguigu.gmallmy0311.cart.mapper.CartInfoMapper;
//import com.atguigu.gmallmy0311.config.RedisUtil;
//import com.atguigu.gmallmy0311.service.CartService;
//import com.atguigu.gmallmy0311.service.ManageService;
//import org.apache.commons.lang3.StringUtils;
//import org.springframework.beans.factory.annotation.Autowired;
//import redis.clients.jedis.Jedis;
//
//import java.util.*;
//
//@Service
//public class CartServiceImpl00 implements CartService {
//
//    @Autowired
//    private CartInfoMapper cartInfoMapper;
//    @Reference
//    private ManageService manageService;
//
//    @Autowired
//    RedisUtil redisUtil;
//
//
//
//
//    public void addToCartRedis(String skuId, String userKey, int skuNum) {
//  /*
//        1.  先获取所有的数据
//        2.  判断是否有相同的数据 skuId
//        3.  有：数量相加
//        4.  无：直接添加redis
//
//        hgetAll ();
//        如何获取userKey
//         */
//        Jedis jedis = redisUtil.getJedis();
//        // 定义key
//        String cartKey = CartConst.USER_KEY_PREFIX + userKey + CartConst.USER_CART_KEY_SUFFIX;
//        Map<String, String> map = jedis.hgetAll(cartKey);
//        //        for (String s : map.keySet()) {
//        //            if (s.equals(skuId)){
//        //
//        //            }
//        //        }
//        //   有：数量相加
//        String cartInfoJson = map.get(skuId);
//    if (StringUtils.isNotEmpty(cartInfoJson)){
//        // 添加到redis value=cartInfo字符串
//        // 从缓存中获取数据 ： value --- cartinfo  --- getSkuNum + skuNum
//        CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
//        cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
//            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));
//
//    }else{
//        SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//        CartInfo cartInfo1 = new CartInfo();
//        cartInfo1.setSkuId(skuId);
//        cartInfo1.setCartPrice(skuInfo.getPrice());
//        cartInfo1.setSkuPrice(skuInfo.getPrice());
//        cartInfo1.setSkuName(skuInfo.getSkuName());
//        cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
//        cartInfo1.setSkuNum(skuNum);
//        jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo1));
//
//
//    }
//
//    jedis.close();
//
//    }
//
//
//
//    @Override
//    public void addToCart(String skuId, String userId, Integer skuNum) {
////  先查cart中是否
//        CartInfo cartInfo = new CartInfo();
//        cartInfo.setSkuId(skuId);
//        cartInfo.setUserId(userId);
//        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo);
//        if (cartInfoExist != null) {
//// 更新商品数量
//            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum() + skuNum);
//// 给实时价格赋值
//            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
//            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
//        } else {
//            //如果不存在，保存购物车
//            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
//
//            CartInfo cartInfo1 = new CartInfo();
//            cartInfo1.setSkuId(skuId);
//            cartInfo1.setCartPrice(skuInfo.getPrice());
//            cartInfo1.setSkuPrice(skuInfo.getPrice());
//            cartInfo1.setSkuName(skuInfo.getSkuName());
//            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
//            cartInfo1.setUserId(userId);
//            cartInfo1.setSkuNum(skuNum);
//            //插入到数据库
//            cartInfoMapper.insertSelective(cartInfo1);
//            cartInfoExist = cartInfo1;
//        }
////构建key user:userId:cart
//        String userCartKey = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
//        // 准备取数据
//        Jedis jedis = redisUtil.getJedis();
//// 将对象序列化
//        String cartJson  = JSON.toJSONString(cartInfoExist);
//     jedis.hset(userCartKey,skuId,cartJson);
//     //更新购物车的过期时间user:userid:cart
//         String userInfoKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
//         Long ttl=jedis.ttl(userInfoKey);
//         jedis.expire(userCartKey,ttl.intValue());
//          jedis.close();
//
//    }
//
//    @Override
//    public List<CartInfo> getCartList(String userId) {
//        // 从redis中取得，
//        Jedis jedis = redisUtil.getJedis();
//        String userCartKey  = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
//        List<String> cartJsons  = jedis.hvals(userCartKey);
//    if (cartJsons!=null&&cartJsons.size()>0){
//        List<CartInfo> cartInfoList  = new ArrayList<>();
//        for (String  cartJson  : cartJsons) {
//            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
//            cartInfoList.add(cartInfo);
//        }
//        // 排序
//        cartInfoList.sort(new Comparator<CartInfo>(){
//
//
//            @Override
//            public int compare(CartInfo o1, CartInfo o2) {
//
//                return  Long.compare(Long.parseLong(o2.getId()),Long.parseLong(o1.getId()));
//
//
//            }
//        });
//
//        return  cartInfoList;
//    }else {
//        // 从数据库中查询，其中cart_price 可能是旧值，所以需要关联sku_info 表信息
//        List<CartInfo> cartInfoList  = loadCartCache(userId);
//        return cartInfoList;
//    }
//
//
//    }
//
//    @Override
//    public List<CartInfo> loadCartCache(String userId) {
//        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
//    if (cartInfoList==null&&cartInfoList.size()==0){
//        return  null;
//    }
//  String userCartKey=CartConst.USER_KEY_PREFIX+userId+CartConst.USER_CART_KEY_SUFFIX;
//        Jedis jedis = redisUtil.getJedis();
//        Map<String, String> map  = new HashMap<>(cartInfoList.size());
//        for (CartInfo  cartInfo  : cartInfoList) {
//            String cartJson = JSON.toJSONString(cartInfo);
//            // key 都是同一个，值会产生重复覆盖！
//            map.put(cartInfo.getSkuId(),cartJson);
//
//        }
//        // 将java list - redis hash
//            jedis.hmset(userCartKey,map);
//          jedis.close();
//        return cartInfoList;
//    }
//
//    @Override
//    public List<CartInfo> mergeToCartList(List<CartInfo> cartListFromCookie, String userId) {
//        List<CartInfo> cartInfoListDB  = cartInfoMapper.selectCartListWithCurPrice(userId);
//// 循环开始匹配
//        for (CartInfo cartInfoCk  : cartListFromCookie) {
//            boolean isMatch =false;
//            for (CartInfo cartInfoDB  : cartInfoListDB) {
//                if (cartInfoDB.getSkuId().equals(cartInfoCk.getSkuId())){
//                    cartInfoDB.setSkuNum(cartInfoCk.getSkuNum()+cartInfoDB.getSkuNum());
//                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);
//                    isMatch=true;
//                }
//            }
//            if (!isMatch){
//                cartInfoCk.setUserId(userId);
//                cartInfoMapper.insertSelective(cartInfoCk);
//            }
//        }
//        // 从新在数据库中查询并返回数据
//        List<CartInfo> cartInfoList  = loadCartCache(userId);
//        return cartInfoList;
//    }
//}
