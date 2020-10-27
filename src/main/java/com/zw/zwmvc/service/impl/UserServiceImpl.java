package com.zw.zwmvc.service.impl;

import com.zw.zwmvc.annotation.ZwService;
import com.zw.zwmvc.service.api.UserService;

@ZwService("UserServiceImpl")
public class UserServiceImpl implements UserService {

    public String query(String name, String age) {
        return "hello " + name +", your age is " + age;
    }
}
