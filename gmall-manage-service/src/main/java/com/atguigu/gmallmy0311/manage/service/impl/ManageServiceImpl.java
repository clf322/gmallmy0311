package com.atguigu.gmallmy0311.manage.service.impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.*;
import com.atguigu.gmallmy0311.config.RedisUtil;
import com.atguigu.gmallmy0311.manage.constant.ManageConst;
import com.atguigu.gmallmy0311.manage.mapper.*;
import com.atguigu.gmallmy0311.service.ManageService;
import org.apache.commons.lang3.StringUtils;
import org.redisson.Redisson;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.exceptions.JedisConnectionException;

import javax.persistence.Id;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class ManageServiceImpl implements ManageService {

    @Autowired
    BaseCatalog1Mapper baseCatalog1Mapper;
    @Autowired
    BaseCatalog2Mapper baseCatalog2Mapper;

    @Autowired
    BaseCatalog3Mapper baseCatalog3Mapper;

    @Autowired
    BaseAttrInfoMapper baseAttrInfoMapper;

    @Autowired
    BaseAttrValueMapper baseAttrValueMapper;

    @Autowired
    SpuInfoMapper spuInfoMapper;

    @Autowired
    BaseSaleAttrMapper baseSaleAttrMapper;
    @Autowired
    SpuSaleAttrMapper spuSaleAttrMapper;

    @Autowired
    SpuSaleAttrValueMapper spuSaleAttrValueMapper;

    @Autowired
    SpuImageMapper spuImageMapper;

    @Autowired
    SkuInfoMapper skuInfoMapper;

    @Autowired
    SkuImageMapper skuImageMapper;
    @Autowired
    SkuAttrValueMapper skuAttrValueMapper;
    @Autowired
    SkuSaleAttrValueMapper skuSaleAttrValueMapper;

     @Autowired
    RedisUtil redisUtil;
    @Override
    public List<BaseCatalog1> getCatalog1() {
        return baseCatalog1Mapper.selectAll();

    }

    @Override
    public List<BaseCatalog2> getCatalog2(String catalog1Id) {
        BaseCatalog2 baseCatalog2 = new BaseCatalog2();
        baseCatalog2.setCatalog1Id(catalog1Id);

        return baseCatalog2Mapper.select(baseCatalog2);

    }

    @Override
    public List<BaseCatalog3> getCatalog3(String catalog2Id) {
        BaseCatalog3 baseCatalog3 = new BaseCatalog3();
        baseCatalog3.setCatalog2Id(catalog2Id);
        return baseCatalog3Mapper.select(baseCatalog3);

    }

//    @Override
//    public List<BaseAttrInfo> getAttrList1(String catalog3Id) {
//        BaseAttrInfo baseAttrInfo = new BaseAttrInfo();
//        baseAttrInfo.setCatalog3Id(catalog3Id);
//        return   baseAttrInfoMapper.select(baseAttrInfo);
//
//    }

    @Override
    public List<BaseAttrInfo> getAttrList(String catalog3Id) {

        return baseAttrInfoMapper.getBaseAttrInfoListByCatalog3Id(Long.parseLong(catalog3Id));

    }


    @Override
    @Transactional
    public void saveAttrInfo(BaseAttrInfo baseAttrInfo) {
        //如果有ID
        if (baseAttrInfo.getId() != null && baseAttrInfo.getId().length() > 0) {
            baseAttrInfoMapper.updateByPrimaryKeySelective(baseAttrInfo);
        } else {


            baseAttrInfoMapper.insertSelective(baseAttrInfo);

        }
//          int i=1;
//          int j=0;
//          i=i/j;
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(baseAttrInfo.getId());
        baseAttrValueMapper.delete(baseAttrValue);
        //取属性值集合
        if (baseAttrInfo.getAttrValueList() != null && baseAttrInfo.getAttrValueList().size() > 0) {
            for (BaseAttrValue attrValue : baseAttrInfo.getAttrValueList()) {
                // attrValue .setId(null);
                attrValue.setAttrId(baseAttrInfo.getId());
                baseAttrValueMapper.insertSelective(attrValue);
            }

        }

    }

    @Override
    public List<BaseAttrValue> getAttrValueList(String attrId) {

//        BaseAttrInfo baseAttrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
//
//        List<BaseAttrValue> attrValueList = baseAttrInfo.getAttrValueList();
//
//        return attrValueList;
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        baseAttrValue.setAttrId(attrId);
        return baseAttrValueMapper.select(baseAttrValue);
    }

    @Override
    public BaseAttrInfo getAttrInfo(String attrId) {
        BaseAttrInfo attrInfo = baseAttrInfoMapper.selectByPrimaryKey(attrId);
        // 创建属性值对象
        BaseAttrValue baseAttrValue = new BaseAttrValue();
        // 根据attrId字段查询对象
        baseAttrValue.setAttrId(attrInfo.getId());
        List<BaseAttrValue> attrValueList = baseAttrValueMapper.select(baseAttrValue);
        attrInfo.setAttrValueList(attrValueList);
        return attrInfo;
    }

    @Override
    public List<SpuInfo> getspuList(SpuInfo spuInfo) {

        return spuInfoMapper.select(spuInfo);

    }

    @Override
    public List<BaseSaleAttr> getbaseSaleAttrList() {
        return baseSaleAttrMapper.selectAll();

    }

    @Override
    @Transactional
    public void saveSpuInfo(SpuInfo spuInfo) {
//判断是否存在id如存在则修改
        if (spuInfo.getId() != null && spuInfo.getId().length() > 0) {
            spuInfoMapper.updateByPrimaryKeySelective(spuInfo);
        } else {
            //不存在执行添加
            spuInfo.setId(null);
            spuInfoMapper.insertSelective(spuInfo);
        }
        //图片表里先删除后插入
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuInfo.getId());
        spuImageMapper.delete(spuImage);
        // 保存数据，先获取数据
        List<SpuImage> spuImageList = spuInfo.getSpuImageList();
        if (spuImageList != null && spuImageList.size() > 0) {
            for (SpuImage image : spuImageList) {
                image.setSpuId(spuInfo.getId());
                spuImageMapper.insert(image);

            }
        }

        // 销售属性 删除，插入
        SpuSaleAttr spuSaleAttr = new SpuSaleAttr();
        spuSaleAttr.setSpuId(spuInfo.getId());
        spuSaleAttrMapper.delete(spuSaleAttr);
        // 销售属性值 删除，插入
        SpuSaleAttrValue spuSaleAttrValue = new SpuSaleAttrValue();
        spuSaleAttrValue.setSpuId(spuInfo.getId());
        spuSaleAttrValueMapper.delete(spuSaleAttrValue);
        // 获取数据
        List<SpuSaleAttr> spuSaleAttrList = spuInfo.getSpuSaleAttrList();
        if (spuSaleAttrList != null && spuSaleAttrList.size() > 0) {
            for (SpuSaleAttr saleAttr : spuSaleAttrList) {

                saleAttr.setSpuId(spuInfo.getId());
                spuSaleAttrMapper.insert(saleAttr);
                for (SpuSaleAttrValue saleAttrValue : saleAttr.getSpuSaleAttrValueList()) {
                    saleAttrValue.setId(null);
                    saleAttrValue.setSpuId(spuInfo.getId());
                    spuSaleAttrValueMapper.insert(saleAttrValue);

                }

            }
        }


    }

    @Override
    public List<SpuImage> getSpuImageList(String spuId) {
        SpuImage spuImage = new SpuImage();
        spuImage.setSpuId(spuId);
        return spuImageMapper.select(spuImage);

    }

    @Override
    public List<SpuSaleAttr> getSpuSaleAttrList(String spuId) {
        return spuSaleAttrMapper.selectSpuSaleAttrList(Long.parseLong(spuId));
    }

    @Override
    @Transactional
    public void saveSkuInfo(SkuInfo skuInfo) {
        if (skuInfo.getId() == null || skuInfo.getId().length() == 0) {
            skuInfo.setId(null);
            skuInfoMapper.insertSelective(skuInfo);
        } else {
            skuInfoMapper.updateByPrimaryKeySelective(skuInfo);
        }
        //SkuInfoMapper
        // SkuImageMapper
        //SkuAttrValueMapper
        //SkuSaleAttrValueMapper
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuInfo.getId());
        skuImageMapper.delete(skuImage);

        List<SkuImage> skuImageList = skuInfo.getSkuImageList();
        if (skuImageList != null && skuImageList.size() > 0) {
            for (SkuImage image : skuImageList) {
                if (image.getId()!=null&&image.getId().length()==0){
                    image.setId(null);
                }
                image.setSkuId(skuInfo.getId());
              //  System.out.println();
                skuImageMapper.insertSelective(image);

            }

        }
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuInfo.getId());
        skuAttrValueMapper.delete(skuAttrValue);
        List<SkuAttrValue> skuAttrValueList = skuInfo.getSkuAttrValueList();
    if (skuAttrValueList!=null&&skuAttrValueList.size()>0){
        for (SkuAttrValue attrValue : skuAttrValueList) {
            if (attrValue.getId()!=null&&attrValue.getId().length()==0){
                attrValue.setId(null);
            }
            attrValue.setSkuId(skuInfo.getId());
            skuAttrValueMapper.insertSelective(attrValue);
        }

    }
        SkuSaleAttrValue skuSaleAttrValue = new SkuSaleAttrValue();
        skuSaleAttrValue.setSkuId(skuInfo.getId());
        skuSaleAttrValueMapper.delete(skuSaleAttrValue);
        List<SkuSaleAttrValue> skuSaleAttrValueList = skuInfo.getSkuSaleAttrValueList();
        if (skuSaleAttrValueList!=null&&skuSaleAttrValueList.size()>0){
            for (SkuSaleAttrValue saleAttrValue  : skuSaleAttrValueList) {
                if (saleAttrValue.getId()!=null&&saleAttrValue.getId().length()==0){
                    saleAttrValue.setId(null);
                }
                saleAttrValue.setSkuId(skuInfo.getId());
                skuSaleAttrValueMapper.insertSelective(saleAttrValue);
            }
        }


    }

    @Override
    public SkuInfo getSkuInfo(String skuId) {

        return getSkuInfoRedisson(skuId);

        }

    private SkuInfo getSkuInfoRedisson(String skuId) {
   //业务代码
        SkuInfo skuInfo=null;
        RLock lock=null;
        Jedis jedis=null;

        try{
            //创建config
            Config config=new Config();
            config.useSingleServer().setAddress("redis://192.168.81.138:6379");
            RedissonClient redisson = Redisson.create(config);

             //测试redis String
            jedis=redisUtil.getJedis();
            //定义Key

            String userKey=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX;
            if (jedis.exists(userKey)){
                //获取缓存中的数据
                String userJson=jedis.get(userKey);
                if (!StringUtils.isEmpty(userJson)){
                  skuInfo=JSON.parseObject(userJson,SkuInfo.class);
                  return skuInfo;

                }



            }else {
                lock= redisson.getLock("my-lock");
                lock.lock(10, TimeUnit.SECONDS);
                // 从数据库查询数据
                skuInfo = getSkuInfoDB(skuId);
                // 将数据放入缓存
                // jedis.set(userKey,JSON.toJSONString(skuInfo));
               jedis.setex(userKey,ManageConst.SKUKEY_TIMEOUT,JSON.toJSONString(skuInfo));
                return  skuInfo;
            }


        }catch (Exception e) {
            e.printStackTrace();
        } finally {
            if (jedis!=null){
                jedis.close();
            }
            if (lock!=null){
                lock.unlock();
            }

        }
        // 从db走！
        return getSkuInfoDB(skuId);


    }


    public SkuInfo getSkuInfoRedist(String skuId) {
        SkuInfo skuInfo = null;
        Jedis jedis=null;
        try{
            jedis = redisUtil.getJedis();
            // 定义key
            String skuInfoKey = ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKUKEY_SUFFIX; //key= sku:skuId:info

            String skuJson = jedis.get(skuInfoKey);

            if (skuJson==null || skuJson.length()==0){
                // 没有数据 ,需要加锁！取出完数据，还要放入缓存中，下次直接从缓存中取得即可！
                System.out.println("没有命中缓存");
                // 定义key user:userId:lock
                String skuLockKey=ManageConst.SKUKEY_PREFIX+skuId+ManageConst.SKULOCK_SUFFIX;
                // 生成锁
                String lockKey  = jedis.set(skuLockKey, "OK", "NX", "PX", ManageConst.SKULOCK_EXPIRE_PX);
                if ("OK".equals(lockKey)){
                    System.out.println("获取到了分布式锁！");
                    // 从数据库中取得数据
                    skuInfo = getSkuInfoDB(skuId);
                    // 将是数据放入缓存
                    // 将对象转换成字符串
                    String skuRedisStr = JSON.toJSONString(skuInfo);
                    jedis.setex(skuInfoKey,ManageConst.SKUKEY_TIMEOUT,skuRedisStr);

//                    jedis.close();
                    return skuInfo;
                }else {
                    System.out.println("等待！");
                    // 等待
                    Thread.sleep(1000);
                    // 自旋
                    return getSkuInfo(skuId);
                }
            }else{
                // 有数据
                skuInfo = JSON.parseObject(skuJson, SkuInfo.class);
                System.out.println("命中了缓存！");

//                jedis.close();
                return skuInfo;
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            if (jedis!=null){
                jedis.close();
            }
        }
        // 从数据库返回数据
        return getSkuInfoDB(skuId);
    }


    private SkuInfo getSkuInfoDB(String skuId) {
        SkuInfo skuInfo;
        skuInfo = skuInfoMapper.selectByPrimaryKey(skuId);
       // List<SkuImage> skuImageList = getSkuImageBySkuId(skuId);
        skuInfo.setSkuImageList(getSkuImageBySkuId(skuId));
        SkuAttrValue skuAttrValue = new SkuAttrValue();
        skuAttrValue.setSkuId(skuId);
        List<SkuAttrValue> skuAttrValueList = skuAttrValueMapper.select(skuAttrValue);
        // 将查询出来所有商品平台属性值赋给对象
        skuInfo.setSkuAttrValueList(skuAttrValueList);

        return  skuInfo;
    }

    public List<SkuImage> getSkuImageBySkuId(String skuId) {
        SkuImage skuImage = new SkuImage();
        skuImage.setSkuId(skuId);
        return skuImageMapper.select(skuImage);
    }


    @Override
    public List<SpuSaleAttr> getSpuSaleAttrListCheckBySku(SkuInfo skuInfo) {
        return   spuSaleAttrMapper.selectSpuSaleAttrListCheckBySku(skuInfo.getId(),skuInfo.getSpuId());


    }

    @Override
    public List<SkuSaleAttrValue> getSkuSaleAttrValueListBySpu(String spuId) {
        List<SkuSaleAttrValue> skuSaleAttrValueList  = skuSaleAttrValueMapper.selectSkuSaleAttrValueListBySpu(spuId);
        return skuSaleAttrValueList ;
    }

    @Override
    public List<BaseAttrInfo> getAttrList(List<String> attrValueIdList) {
        String attrValueIds  = StringUtils.join(attrValueIdList.toArray(), ",");
        List<BaseAttrInfo> baseAttrInfoList =  baseAttrInfoMapper.selectAttrInfoListByIds(attrValueIds);
        return baseAttrInfoList;
    }


}
