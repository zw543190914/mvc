package com.zw.zwmvc.annotation;

import java.lang.annotation.*;

/**
 * @author 镜中水月
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZwAutowired {
    String value() default "";
}
