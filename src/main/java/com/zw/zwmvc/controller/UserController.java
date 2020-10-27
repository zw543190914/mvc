package com.zw.zwmvc.controller;

import com.zw.zwmvc.annotation.ZwAutowired;
import com.zw.zwmvc.annotation.ZwController;
import com.zw.zwmvc.annotation.ZwRequestMapping;
import com.zw.zwmvc.annotation.ZwRequestParam;
import com.zw.zwmvc.service.api.UserService;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

@ZwController
@ZwRequestMapping("/user")
public class UserController {

    @ZwAutowired("UserServiceImpl")
    private UserService userService;

    @ZwRequestMapping("/query")
    //http://localhost:8080/zw/user/query?name=zz&age=ww
    public void query(HttpServletRequest request,HttpServletResponse response,
                        @ZwRequestParam("name") String name, @ZwRequestParam("age") String age){
        try {
            System.out.println(name);
            System.out.println(age);
            PrintWriter writer = response.getWriter();
            String result = userService.query(name, age);
            System.out.println("query result == " + result);
            writer.write(result);
        } catch (IOException e) {
            e.printStackTrace();
        }

    }
}
