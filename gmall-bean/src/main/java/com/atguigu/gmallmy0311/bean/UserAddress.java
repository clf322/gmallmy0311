package com.atguigu.gmallmy0311.bean;

import lombok.Data;

import javax.persistence.Column;
import javax.persistence.Id;
import java.io.Serializable;

@Data
    public class UserAddress implements Serializable {
        @Column
        @Id
        private String id;
        @Column
        private String userAddress;
        @Column
        private String userId;
        @Column
        private String consignee;
        @Column
        private String phoneNum;
        @Column
        private String isDefault;
    }



