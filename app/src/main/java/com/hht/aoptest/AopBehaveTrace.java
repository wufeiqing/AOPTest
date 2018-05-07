package com.hht.aoptest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author：wufq on 2018/5/7 15:20
 * Email：wufeiqing@@honghe-tech.com
 *
 * @TODO:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AopBehaveTrace {
    String value();
}
