package com.sky.anno;

import com.sky.enumeration.OperationType;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.Target;
import java.lang.reflect.Method;

import static java.lang.annotation.RetentionPolicy.RUNTIME;

/**
 * 自定义注解，用于标识需要自动填充的字段
 */
@Target({ElementType.METHOD})
@Retention(value = RUNTIME)// 运行时生效
public @interface AutoFill {
    //用于标识需要自动填充的字段
    OperationType value();
}
