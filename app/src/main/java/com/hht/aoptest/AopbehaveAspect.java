package com.hht.aoptest;

import android.util.Log;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.aspectj.lang.reflect.MethodSignature;

/**
 * Author：wufq on 2018/5/7 15:37
 * Email：
 *
 * @TODO:
 */
@Aspect
public class AopbehaveAspect {
    private static final String TAG = "wufq";

    /**
     * 切点
     */
    @Pointcut("execution(@com.hht.aoptest.AopBehaveTrace  * *(..))")
    public void respectBehave() {

    }

    /**
     * 切面
     * @param point
     * @return
     * @throws Throwable
     */
    @Around("respectBehave()")
    public Object dealWithPoint(ProceedingJoinPoint point) throws Throwable {
        //before
        MethodSignature methodSignature = (MethodSignature) point.getSignature();
        AopBehaveTrace behaviorTrace = methodSignature.getMethod().getAnnotation(AopBehaveTrace.class);
        String contentType = behaviorTrace.value();
        Log.i(TAG, contentType + "......");
        //doing
        Object object = null;
        try {
            object = point.proceed();
        } catch (Exception e) {

        }
        //after

        return object;
    }


}
