package com.bin.mylibrary.base;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bin.mylibrary.utils.LogUtils;


public abstract class BaseAty extends AppCompatActivity {

    /**
     * 语音的请求地址
     */
    protected String speakUrl = "http://114.115.204.49/htAppWeb2/getHelpList";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        Bundle bundle = getIntent().getExtras();
        initView(bundle);
        super.onCreate(savedInstanceState);
        setContentView(getView());
        doBusiness(this);
    }

    /**
     * 设置布局
     *
     * @return
     */
    protected abstract @NonNull
    int getView();

    /**
     * 初始化以及接收值
     */
    public abstract void initView(Bundle parms);

    /**
     * [业务操作]
     *
     * @param mContext
     */
    public abstract void doBusiness(Context mContext);

    /**
     * [页面跳转]
     *
     * @param activity
     */
    protected void startAty(Class<?> activity) {
        startActivity(new Intent(BaseAty.this, activity));
    }

    /**
     * [携带数据的页面跳转]
     *
     * @param clz
     * @param bundle
     */
    public void startAty(Class<?> clz, Bundle bundle) {
        Intent intent = new Intent();
        intent.setClass(this, clz);
        if (bundle != null) {
            intent.putExtras(bundle);
        }
        startActivity(intent);
    }

    /**
     * [ 简化  Toast  短提示 ]
     *
     * @param msg
     */
    protected void showToastShort(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * [ 简化  Toast  长提示 ]
     *
     * @param msg
     */
    protected void showToastLong(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

    protected void logShow(String TAG, String msg) {
        LogUtils.e(TAG, msg);
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }


}
