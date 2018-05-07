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
