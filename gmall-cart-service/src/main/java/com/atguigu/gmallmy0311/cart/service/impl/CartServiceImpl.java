package com.atguigu.gmallmy0311.cart.service.impl;

import com.alibaba.dubbo.config.annotation.Reference;
import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;

import com.atguigu.gmallmy0311.bean.CartInfo;
import com.atguigu.gmallmy0311.bean.SkuInfo;
import com.atguigu.gmallmy0311.cart.constant.CartConst;
import com.atguigu.gmallmy0311.cart.mapper.CartInfoMapper;
import com.atguigu.gmallmy0311.config.RedisUtil;
import com.atguigu.gmallmy0311.service.CartService;
import com.atguigu.gmallmy0311.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import redis.clients.jedis.Jedis;

import java.util.*;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    private CartInfoMapper cartInfoMapper;

    @Reference
    private ManageService manageService;

    @Autowired
    private RedisUtil redisUtil;

    // 表示登录时添加购物车
    @Override
    public void addToCart(String skuId, String userId, Integer skuNum) {

        /*
        1.  判断购物车中是否有该商品
            select * from cartInfo where userId = ? and skuId = ?
        2.  有： 数量相加
        3.  没有：直接添加到数据库
        4.  放入redis中
        mysql 与 redis 如何进行同步？
            在添加购物车的时候，直接添加到数据库并添加到redis！
         */
        CartInfo cartInfo = new CartInfo();
        cartInfo.setUserId(userId);
        cartInfo.setSkuId(skuId);
        CartInfo cartInfoExist = cartInfoMapper.selectOne(cartInfo); // 2

        // 获取jedis 
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        /*
            用那种数据类型 hash jedis.hset(key,field,value)
            key=user:userId:cart
            field=skuId
            value=cartInfoValue
          */
        if (cartInfoExist!=null){
            // 数量相加 skuNum = 1
            cartInfoExist.setSkuNum(cartInfoExist.getSkuNum()+skuNum); // 3
            // 给实时价格初始化值
            cartInfoExist.setSkuPrice(cartInfoExist.getCartPrice());
            // cartInfoExist更新到数据库
            cartInfoMapper.updateByPrimaryKeySelective(cartInfoExist);
            // 放入redis
            // jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));
            
        }else {
            // 直接添加到数据库 , 获取skuInfo 信息。添加到cartInfo 中！
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();

            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setUserId(userId);
            cartInfo1.setSkuNum(skuNum);
            // 添加到数据库
            cartInfoMapper.insertSelective(cartInfo1);
            cartInfoExist = cartInfo1;
            // 放入redis
            // jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfo1));
        }

        // 放入redis
        jedis.hset(cartKey,skuId, JSON.toJSONString(cartInfoExist));

        // 购物车是否有过期时间？

        // 设置过期时间？跟用户的过期时间一致
        // 获取用户的key
        String userKey = CartConst.USER_KEY_PREFIX+userId+CartConst.USERINFOKEY_SUFFIX;
        // 剩余过期时间
        Long ttl = jedis.ttl(userKey);
        // 赋值给购物车
        jedis.expire(cartKey,ttl.intValue());

        jedis.close();


    }

    @Override
    public List<CartInfo> getCartList(String userId) {

        List<CartInfo> cartInfoList = new ArrayList<>();
        /*
            1.  获取jedis
            2.  从redis 中获取数据
            3.  如果有：将redis 数据返回
            4.  如果没有：从数据库查询{查询购物车中的实时价格}，并放入redis
         */
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;
        // 获取数据
        //        jedis.hgetAll()
        //        jedis.hvals()
        List<String> cartList = jedis.hvals(cartKey);
        if (cartList!=null && cartList.size()>0){
            for (String cartJson : cartList) {
                //cartJson 转换为对象
                CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
                // 添加购物车数据
                cartInfoList.add(cartInfo);
            }
            // 查询的时候，按照更新的时间倒序！
            cartInfoList.sort(new Comparator<CartInfo>() {
                @Override
                public int compare(CartInfo o1, CartInfo o2) {
                    // compareTo str1 = abc str2 =abcd
                    return o1.getId().compareTo(o2.getId());
                }
            });
            return cartInfoList;
        }else {
            // 从数据库中获取数据
            cartInfoList = loadCartCache(userId);
            return cartInfoList;
        }
    }

    @Override
    public List<CartInfo> mergeToCartList(List<CartInfo> cartListCK, String userId) {
        // 获取数据库中的数据
        List<CartInfo> cartInfoListDB = cartInfoMapper.selectCartListWithCurPrice(userId);
        // 合并条件 skuId 相同的时候合并
        for (CartInfo cartInfoCK : cartListCK) {
            // 声明一个boolean 类型遍历
            boolean isMatch = false;
            // 有相同的数据直接更新到数据
            for (CartInfo cartInfoDB : cartInfoListDB) {
                if (cartInfoCK.getSkuId().equals(cartInfoDB.getSkuId())){
                    // 数量相加
                    cartInfoDB.setSkuNum(cartInfoCK.getSkuNum()+cartInfoDB.getSkuNum());
                    // 更新
                    cartInfoMapper.updateByPrimaryKeySelective(cartInfoDB);

                    isMatch  = true;
                }
            }
            // 未登录的数据在数据库中没有，则直接插入数据库
            if (!isMatch){
                // 未登录的时候的userId 为null
                cartInfoCK.setUserId(userId);
                cartInfoMapper.insertSelective(cartInfoCK);
            }
        }
        // 最后再查询一次更新之后，新添加的所有数据
        List<CartInfo> cartInfoList = loadCartCache(userId);
        for (CartInfo cartInfo : cartInfoList) {

            for (CartInfo info : cartListCK) {
                if (cartInfo.getSkuId().equals(info.getSkuId())){
// 只有被勾选的才会进行更改
                    if (info.getIsChecked().equals("1")){
                     cartInfo.setIsChecked(info.getIsChecked());
                        // 更新redis中的isChecked
                        checkCart(cartInfo.getSkuId(),info.getIsChecked(),userId);


                    }

                }


            }
            
            
        }

        
        return cartInfoList;
    }

    @Override
    public void addToCartRedis(String skuId, String userKey, int skuNum) {

        /*
        1.  先获取所有的数据
        2.  判断是否有相同的数据 skuId
        3.  有：数量相加
        4.  无：直接添加redis

        hgetAll ();
        如何获取userKey
         */

        Jedis jedis = redisUtil.getJedis();
        // 定义key
        String cartKey = CartConst.USER_KEY_PREFIX+userKey+ CartConst.USER_CART_KEY_SUFFIX;
        Map<String, String> map = jedis.hgetAll(cartKey);
        //        for (String s : map.keySet()) {
        //            if (s.equals(skuId)){
        //
        //            }
        //        }
        //   有：数量相加
        String cartInfoJson = map.get(skuId);
        if (StringUtils.isNotEmpty(cartInfoJson)){
            // 添加到redis value=cartInfo字符串
            // 从缓存中获取数据 ： value --- cartinfo  --- getSkuNum + skuNum
            CartInfo cartInfo = JSON.parseObject(cartInfoJson, CartInfo.class);
            cartInfo.setSkuNum(cartInfo.getSkuNum()+skuNum);
            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo));
        }else {
            SkuInfo skuInfo = manageService.getSkuInfo(skuId);
            CartInfo cartInfo1 = new CartInfo();
            cartInfo1.setSkuId(skuId);
            cartInfo1.setCartPrice(skuInfo.getPrice());
            cartInfo1.setSkuPrice(skuInfo.getPrice());
            cartInfo1.setSkuName(skuInfo.getSkuName());
            cartInfo1.setImgUrl(skuInfo.getSkuDefaultImg());
            cartInfo1.setSkuNum(skuNum);
            jedis.hset(cartKey,skuId,JSON.toJSONString(cartInfo1));
        }
        jedis.close();


    }

    @Override
    public List<CartInfo> getCartListRedis(String userKey) {

        //创建 jedis
        Jedis jedis = null;

        try {
            jedis = redisUtil.getJedis();

            //定义 key
            String cartKey = CartConst.USER_KEY_PREFIX + userKey + CartConst.USER_CART_KEY_SUFFIX;

            //获取redis中的所有商品信息
            List<String> hvals = jedis.hvals(cartKey);

            //定义一个集合 来存储商品信息对象
            List<CartInfo> cartInfoList = new ArrayList<>();

            //判断如果商品信息 不为空的话
            if (hvals != null && hvals.size() > 0) {

                //遍历商品信息值
                for (String hval : hvals) {

                    //将字符串的值转换为 购物车对象
                    CartInfo cartInfo = JSON.parseObject(hval, CartInfo.class);

                    //将购物车对象放入封装后的集合中
                    cartInfoList.add(cartInfo);
                }
            }

            //进行排序  因为这里数据库没有时间字段 所以我们使用id进行排序
            cartInfoList.sort((s1,s2)->s1.getSkuId().compareTo(s2.getSkuId()));

            return  cartInfoList;

        } catch (Exception e) {
            e.printStackTrace();
        }finally {
            if (jedis != null) {
                jedis.close();
            }
        }
        return  null;
    }

    @Override
    public void checkCart(String skuId, String isChecked, String userId) {
        // 更新购物车中的isChecked标志
        Jedis jedis = redisUtil.getJedis();
        // 取得购物车中的信息
        String userCartKey  = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CART_KEY_SUFFIX;
        String cartJson  = jedis.hget(userCartKey, skuId);
        // 将cartJson 转换成对象
        CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
        cartInfo.setIsChecked(isChecked);
        String cartCheckdJson  = JSON.toJSONString(cartInfo);
         jedis.hset(userCartKey,skuId,cartCheckdJson);
        // 新增到已选中购物车
        String userCheckedKey  = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
     if (isChecked.equals("1")){
        jedis.hset(userCheckedKey,skuId,cartCheckdJson);
     }else{
         jedis.hdel(userCheckedKey,skuId);
     }
      jedis.close();

    }

    @Override
    public List<CartInfo> getCartCheckedList(String userId) {
        // 获得redis中的key
        String userCheckedKey  = CartConst.USER_KEY_PREFIX + userId + CartConst.USER_CHECKED_KEY_SUFFIX;
        Jedis jedis = redisUtil.getJedis();
        List<String> cartCheckedList  = jedis.hvals(userCheckedKey);
        List<CartInfo> newCartList = new ArrayList<>();
        for (String cartJson  : cartCheckedList) {
            CartInfo cartInfo = JSON.parseObject(cartJson, CartInfo.class);
           newCartList.add(cartInfo);
        }
        return newCartList;
    }


    /**
     * 根据userId查询数据并放入缓存
     * @param userId
     * @return
     */
    private List<CartInfo> loadCartCache(String userId) {
        /*
        1.  根据userId 查询一下当前商品的实时价格：
            cartInfo.skuPrice = skuInfo.price
        2.  将查询出来的数据集合放入缓存！
         */
        List<CartInfo> cartInfoList = cartInfoMapper.selectCartListWithCurPrice(userId);
        /* 面试javaScript   =  ==  ===  */
        if (cartInfoList==null || cartInfoList.size()==0){
            return null;
        }
        // 获取jedis
        Jedis jedis = redisUtil.getJedis();
        // 定义key user:userId:cart
        String cartKey = CartConst.USER_KEY_PREFIX+userId+ CartConst.USER_CART_KEY_SUFFIX;

//        for (CartInfo cartInfo : cartInfoList) {
//            // 每次放一条数据
//            jedis.hset(cartKey,cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
//        }

        HashMap<String, String> map = new HashMap<>();
        for (CartInfo cartInfo : cartInfoList) {
            map.put(cartInfo.getSkuId(),JSON.toJSONString(cartInfo));
        }
        // 将map 放入缓存
        jedis.hmset(cartKey,map);
        // hgetAll -- map
        return cartInfoList;
    }
}
