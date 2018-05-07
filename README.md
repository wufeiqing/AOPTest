一、前述

    我们知道Java语言是面向对象的语言，有继承、多态、封装等相关概念，在项目实战中主要做到的是功能的模块化，模块与模块间低耦合，这是面向对象的核心思想。Android系统的framework层有四大服务，分别为ActivityManagerService、WindowManagerService、PowerManagerService和PackageManagerService,它们分别负责各自的功能模块，模块与模块之前基本没什么关联，这就是低耦合高内聚的表现。针对面向对象的这一特点，我们提出一个讨论方案，即是当我们要在每个模块加一个打印功能时，可能大家会在ActivityManagerService中加一句代码:Log.i("tag",......);然后WindowManagerService中也加同样的代码，这样我们就违背了代码设计的单一原则，我们该如何解决面向对象的这一痛点呢？这时AOP(面向切面编程)就派上用场了。下面先用代码来描述面向对象的这个实际问题。

二、面向对像的痛点

    下面是一个Activity界面，代码如下:

package com.hht.aoptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button wxBtn, zfbBtn, ylBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        wxBtn = (Button) findViewById(R.id.btn_wx);
        zfbBtn = (Button) findViewById(R.id.btn_zfb);
        ylBtn = (Button) findViewById(R.id.btn_yl);
        wxBtn.setOnClickListener(this);
        zfbBtn.setOnClickListener(this);
        ylBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_wx://微信功能
                wxFunction();
                break;
            case R.id.btn_zfb://支付宝功能
                zfbFunction();
                break;
            case R.id.btn_yl://银联功能
                ylFunction();
                break;
        }
    }

    private void ylFunction() {
        Log.i(TAG,"银联功能......");
    }

    private void zfbFunction() {
        Log.i(TAG,"支付宝功能......");
    }

    private void wxFunction() {
        Log.i(TAG,"微信功能......");
    }
}
    从上述代码我们可以看出，有三个按钮的点击事件，每个事件里各自打印自己的类型。其实我们每个按钮对应一个模块，是个独立的功能，但是每个功能模块里都调用了日志打印的功能，这违背了单一原则。接下来我们用AOP来解决这个问题。

三、面向切面编程

1、配置AOP开发的环境

    我们用到了AOP框架，它是AspectJ,在其官网上下载其对应的jar包，并放在Android studio的libs目录下。另外我们需要配置Maven,具体配置代码如下，当然它的配置是在build.gradle下的：

import org.aspectj.bridge.IMessage
import org.aspectj.bridge.MessageHandler
import org.aspectj.tools.ajc.Main
buildscript {
    repositories {
        mavenCentral()
    }
    dependencies {
        classpath 'org.aspectj:aspectjtools:1.8.9'
        classpath 'org.aspectj:aspectjweaver:1.8.9'
    }
}

apply plugin: 'com.android.application'

repositories {
    mavenCentral()
}

android {
    compileSdkVersion 25
    buildToolsVersion '25.0.2'
    defaultConfig {
        applicationId "com.hht.aoptest"
        minSdkVersion 15
        targetSdkVersion 25
        versionCode 1
        versionName "1.0"
        testInstrumentationRunner "android.support.test.runner.AndroidJUnitRunner"
    }
    buildTypes {
        release {
            minifyEnabled false
            proguardFiles getDefaultProguardFile('proguard-android.txt'), 'proguard-rules.pro'
        }
    }
}

final def log = project.logger
final def variants = project.android.applicationVariants

variants.all { variant ->
    if (!variant.buildType.isDebuggable()) {
        log.debug("Skipping non-debuggable build type '${variant.buildType.name}'.")
        return;
    }

    JavaCompile javaCompile = variant.javaCompile
    javaCompile.doLast {
        String[] args = ["-showWeaveInfo",
                         "-1.8",
                         "-inpath", javaCompile.destinationDir.toString(),
                         "-aspectpath", javaCompile.classpath.asPath,
                         "-d", javaCompile.destinationDir.toString(),
                         "-classpath", javaCompile.classpath.asPath,
                         "-bootclasspath", project.android.bootClasspath.join(File.pathSeparator)]
        log.debug "ajc args: " + Arrays.toString(args)

        MessageHandler handler = new MessageHandler(true);
        new Main().run(args, handler);
        for (IMessage message : handler.getMessages(null, true)) {
            switch (message.getKind()) {
                case IMessage.ABORT:
                case IMessage.ERROR:
                case IMessage.FAIL:
                    log.error message.message, message.thrown
                    break;
                case IMessage.WARNING:
                    log.warn message.message, message.thrown
                    break;
                case IMessage.INFO:
                    log.info message.message, message.thrown
                    break;
                case IMessage.DEBUG:
                    log.debug message.message, message.thrown
                    break;
            }
        }
    }
}



dependencies {
    compile fileTree(include: ['*.jar'], dir: 'libs')
    androidTestCompile('com.android.support.test.espresso:espresso-core:2.2.2', {
        exclude group: 'com.android.support', module: 'support-annotations'
    })
    compile 'com.android.support:appcompat-v7:25.0.0'
    testCompile 'junit:junit:4.12'
    compile files('libs/aspectjrt.jar')
}
    上面就是build.gradle的详细配置。

2、AOP编程

    1）、我们新建一个类AopBehaveTrace,其代码如下：

package com.hht.aoptest;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * Author：wufq on 2018/5/7 15:20
 * Email：
 *
 * @TODO:
 */
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.CLASS)
public @interface AopBehaveTrace {
    String value();
}
    从上面的代码可以看出，我们用到了注解并且是针对方法的，其实它是一个切点类，用来标记我们从哪里开始切入。

    2）、新建一个切面类AopbehaveAspect,其代码如下:



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
    3)、我们在修改下MainActivity.java文件，修改成如下所示：

package com.hht.aoptest;

import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private static final String TAG = MainActivity.class.getSimpleName();
    private Button wxBtn, zfbBtn, ylBtn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        initView();
    }

    private void initView() {
        wxBtn = (Button) findViewById(R.id.btn_wx);
        zfbBtn = (Button) findViewById(R.id.btn_zfb);
        ylBtn = (Button) findViewById(R.id.btn_yl);
        wxBtn.setOnClickListener(this);
        zfbBtn.setOnClickListener(this);
        ylBtn.setOnClickListener(this);
    }

    @Override
    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btn_wx://微信功能
                wxFunction();
                break;
            case R.id.btn_zfb://支付宝功能
                zfbFunction();
                break;
            case R.id.btn_yl://银联功能
                ylFunction();
                break;
        }
    }

    @AopBehaveTrace(value = "银联功能")
    private void ylFunction() {
//        Log.i(TAG,"银联功能......");
    }
    @AopBehaveTrace(value = "支付宝功能")
    private void zfbFunction() {
//        Log.i(TAG,"支付宝功能......");
    }
    @AopBehaveTrace(value = "微信功能")
    private void wxFunction() {
//        Log.i(TAG,"微信功能......");
    }
}
    到这为止，我的编码工用就完成了，我们Run Android Application运行，依次点击微信、支付宝、银联按钮，再查看下log日志，控制台打印如下:



    结果跟之前的一模一样，同时我们看MainActivity.java的关键代码：

@AopBehaveTrace(value = "银联功能")
    private void ylFunction() {
//        Log.i(TAG,"银联功能......");
    }
    @AopBehaveTrace(value = "支付宝功能")
    private void zfbFunction() {
//        Log.i(TAG,"支付宝功能......");
    }
    @AopBehaveTrace(value = "微信功能")
    private void wxFunction() {
//        Log.i(TAG,"微信功能......");
    }
这里就把打印日志的功能都注释掉了，把日志打印的功能放到AopBehaveAspect中的切面去做，解决了面向对象的痛点。


四、总结

欢迎各们网友、技术爱好者批评指正，同时你觉得写得不错，欢迎点赞及好评！谢谢！



