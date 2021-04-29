package com.atguigu.gmallmy0311.user.service.Impl;

import com.alibaba.dubbo.config.annotation.Service;
import com.alibaba.fastjson.JSON;
import com.atguigu.gmallmy0311.bean.UserAddress;
import com.atguigu.gmallmy0311.bean.UserInfo;
import com.atguigu.gmallmy0311.config.RedisUtil;
import com.atguigu.gmallmy0311.service.UserInfoService;
import com.atguigu.gmallmy0311.user.mapper.UserAddressMapper;
import com.atguigu.gmallmy0311.user.mapper.UserInfoMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.DigestUtils;
import redis.clients.jedis.Jedis;
import tk.mybatis.mapper.entity.Example;

import java.util.List;
@Service
public class UserInfoServiceImpl implements UserInfoService {
   @Autowired
    UserInfoMapper userInfoMapper;
   @Autowired
    UserAddressMapper userAddressMapper;

   @Autowired
 private   RedisUtil redisUtil;

    public String userKey_prefix="user:";
    public String userinfoKey_suffix=":info";
    public int userKey_timeOut=60*60*24;

    public UserInfo verify(String userId){
      //1.去缓存中查询是否有redis
        Jedis jedis = redisUtil.getJedis();
        String key=userKey_prefix+userId+userinfoKey_suffix;
        String userJson  = jedis.get(key);
//2.延长时效
        jedis.expire(key,userKey_timeOut);
       if (userJson!=null){
           UserInfo userInfo = JSON.parseObject(userJson, UserInfo.class);
           return userInfo;

       }
         return null;
    }





   @Override
     public UserInfo  login  (UserInfo  userInfo){
         //1.转换成Md5的格式
         String  password  = DigestUtils.md5DigestAsHex(userInfo.getPasswd().getBytes());
         userInfo.setPasswd(password);
         //2.通过密码来查找用户
         UserInfo info  = userInfoMapper.selectOne(userInfo);
        if (info!=null){
            // 3.获得到redis ,将用户存储到redis中
            Jedis jedis = redisUtil.getJedis();
            jedis.setex(userKey_prefix+info.getId()+userinfoKey_suffix,userKey_timeOut,
                    JSON.toJSONString(info));
               jedis.close();
               //4.返回对象
            return  info;

        }

         return  null;
     }






    @Override
    public List<UserInfo> findAll() {
        return userInfoMapper.selectAll();
    }

    @Override
    public UserInfo getUserInfoByName(String name) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
          return  userInfoMapper.selectOneByExample(example);


    }

    @Override
    public List<UserInfo> getUserInfoListByName(UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",userInfo.getName());
      return   userInfoMapper.selectByExample(example);

    }

    @Override
    public List<UserInfo> getUserInfoListByNickName(UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andLike("nickName","%"+userInfo.getNickName()+"%");
      return   userInfoMapper.selectByExample(example);

    }

    @Override
    public void addUser(UserInfo userInfo) {
        userInfoMapper.insertSelective(userInfo);
    }

    @Override
    public void updateUser(UserInfo userInfo) {
        userInfoMapper.updateByPrimaryKey(userInfo);
    }

    @Override
    public void updateUserByName(String name, UserInfo userInfo) {
        Example example = new Example(UserInfo.class);
        example.createCriteria().andEqualTo("name",name);
        userInfoMapper.updateByExampleSelective(userInfo, example);
    }

    @Override
    public void delUser(UserInfo userInfo) {

        userInfoMapper.delete(userInfo);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(UserAddress userAddress) {
        return userAddressMapper.select(userAddress);
    }

    @Override
    public List<UserAddress> getUserAddressByUserId(String userId) {
      Example example=new Example(UserAddress.class);
        example.createCriteria().andEqualTo("userId",userId);
        return  userAddressMapper.selectByExample(example);
    }


}
