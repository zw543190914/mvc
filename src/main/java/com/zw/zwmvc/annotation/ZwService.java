package com.zw.zwmvc.annotation;

import java.lang.annotation.*;

/**
 * @author 镜中水月
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface ZwService {
    String value() default "";
}
