package com.zw.zwmvc.servlet;

import com.alibaba.fastjson.JSON;
import com.zw.zwmvc.annotation.*;
import com.zw.zwmvc.controller.UserController;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.File;
import java.io.IOException;
import java.lang.annotation.Annotation;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.URI;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DispatcherServlet extends HttpServlet {

    List<String> classNameList = new ArrayList<String>();

    Map<String,Object> beans = new HashMap<String, Object>();

    Map<String,Object> handerMap = new HashMap<String, Object>();

    @Override
    public void init(ServletConfig config){
        // tomcat 启动实例化 IOC
        basePackageScan("com.zw.zwmvc");
        // 实例化 classNameList
        doInstance();

        doAutowired();
        
        doUrlMapping();
    }

    private void doUrlMapping() {
        for (Map.Entry<String,Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(ZwController.class)){
                String classMapping = clazz.getAnnotation(ZwRequestMapping.class).value();
                Method[] methods = clazz.getMethods();
                for (Method method : methods) {
                    if (method.isAnnotationPresent(ZwRequestMapping.class)){
                        String methodMapping = method.getAnnotation(ZwRequestMapping.class).value();
                        String requestPath = classMapping + methodMapping;
                        handerMap.put(requestPath,method);
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }

        }
    }

    private void doAutowired() {
        for (Map.Entry<String,Object> entry : beans.entrySet()){
            Object instance = entry.getValue();
            Class<?> clazz = instance.getClass();
            if (clazz.isAnnotationPresent(ZwController.class)){
                Field[] fields = clazz.getDeclaredFields();
                for (Field field : fields) {
                    if (field.isAnnotationPresent(ZwAutowired.class)){
                        ZwAutowired annotation = field.getAnnotation(ZwAutowired.class);
                        // @Autowired 的名称
                        String key = annotation.value();
                        Object object = beans.get(key);
                        field.setAccessible(true);
                        try {
                            field.set(instance,object);
                        } catch (IllegalAccessException e) {
                            e.printStackTrace();
                        }
                    } else {
                        continue;
                    }
                }
            } else {
                continue;
            }
        }
    }

    private void doInstance() {
        for (String className : classNameList) {
            String cn = className.replace(".class", "");
            System.out.println(cn);
            try {
                Class<?> clazz = Class.forName(cn);
                if (clazz.isAnnotationPresent(ZwController.class)){
                    // controller-控制类
                    Object instance = clazz.newInstance();
                    ZwRequestMapping annotation = clazz.getAnnotation(ZwRequestMapping.class);
                    String value = annotation.value();
                    beans.put(value,instance);
                } else if (clazz.isAnnotationPresent(ZwService.class)){
                    // service-服务类
                    Object instance = clazz.newInstance();
                    ZwService annotation = clazz.getAnnotation(ZwService.class);
                    String value = annotation.value();
                    beans.put(value,instance);
                } else {
                    continue;
                }

            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (InstantiationException e) {
                e.printStackTrace();
            }

        }

        System.out.println("beans==" + JSON.toJSONString(beans));

    }

    private void basePackageScan(String basePackage){
        // 扫描编译的类路径  com.zw -> D:/zw...
        URL url = this.getClass().getClassLoader().getResource("/" + basePackage.replaceAll("\\.","/"));
        // 获取到工作空间文件
        assert url != null;

        String fileStr = url.getFile();
        File file = new File(fileStr);
        String[] filesStr = file.list();
        for (String path : filesStr) {
            File filePath = new File(fileStr + path);
            if (filePath.isDirectory()){
                basePackageScan(basePackage + "." + path);
            } else {
                // com.zw...UserService.class
                classNameList.add(basePackage + "." + filePath.getName());
            }
        }
        System.out.println("classNameList==" + JSON.toJSONString(classNameList));
    }
    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {
        this.doPost(req, resp);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        // /zw-mvc   /user/query
        String uri = request.getRequestURI();
        System.out.println("doPost.uri == " + uri);
        String contextPath = request.getContextPath();
        // /zw-mvc
        System.out.println("doPost.contextPath == " + contextPath);
        String methodKey = uri.replace(contextPath, "");
        System.out.println("doPost.methodKey == " + methodKey);

        Method method = (Method)handerMap.get(methodKey);
        UserController userController = (UserController)beans.get("/" + methodKey.split("/")[1]);
        System.out.println("doPost.method == " + method.getName());
        Object[] args = hand(request, response, method);
        System.out.println("doPost.args == " + args.toString());

        try {
            Object invoke = method.invoke(userController, args);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    private static Object[] hand(HttpServletRequest request, HttpServletResponse response,Method method){
        Class<?>[] parameterTypes = method.getParameterTypes();
        Object[] args = new Object[parameterTypes.length];
        int arg = 0;
        int index = 0;
        for (Class<?> paraClazz : parameterTypes) {
            if (HttpServletRequest.class.isAssignableFrom(paraClazz)){
                args[arg ++] = request;
            }
            if (HttpServletResponse.class.isAssignableFrom(paraClazz)){
                args[arg ++] = response;
            }
            Annotation[] parameterAnnotation = method.getParameterAnnotations()[index];
            if (parameterAnnotation.length > 0){
                for (Annotation annotation : parameterAnnotation) {
                    if (ZwRequestParam.class.isAssignableFrom(annotation.getClass())){
                        ZwRequestParam requestParam = (ZwRequestParam) annotation;
                        args[arg++] = request.getParameter(requestParam.value());
                    }
                }
            }
            index ++;
        }
        return args;
    }
}
