package com.atguigu.gmallmy0311.service;


import com.atguigu.gmallmy0311.bean.UserAddress;
import com.atguigu.gmallmy0311.bean.UserInfo;

import java.util.List;

//业务逻辑层
public interface UserInfoService {
    /**
     * 查询所有用户数据
     * @return
     */
    List<UserInfo> findAll();
    /**
     *
     * @param name
     * @return
     */
    UserInfo getUserInfoByName(String name);
    /**
     *
     * @param userInfo
     * @return
     */
    List<UserInfo> getUserInfoListByName(UserInfo userInfo);
    /**
     *
     * @param userInfo
     * @return
     */
    List<UserInfo> getUserInfoListByNickName(UserInfo userInfo);
    // int ,boolean, void

    void addUser(UserInfo userInfo);

    void updateUser(UserInfo userInfo);

    void updateUserByName(String name, UserInfo userInfo);
    void  delUser(UserInfo userInfo);

   public  List<UserAddress> getUserAddressByUserId(UserAddress userAddress);

    public List<UserAddress> getUserAddressByUserId(String userId);
    public UserInfo  login  (UserInfo  userInfo);

    UserInfo verify(String userId);
}
