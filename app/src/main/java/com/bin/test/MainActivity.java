package com.bin.test;


import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import com.bin.mylibrary.aty.MainAty;
import com.bin.mylibrary.base.BaseWebAty;
import com.bin.mylibrary.utils.LogUtils;

public class MainActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        String data = "{\"bmmc\":null,\"yhlx\":\"Teacher\",\"message\":\"成功\",\"userid\":\"1398\",\"isSuccess\":\"1\",\"schoolName\":\"天津神州浩天科技有限公司\",\"rybh\":\"1398\",\"schoolid\":\"4\",\"loginUrl\":\"http://114.115.181.185:8081\",\"ccp\":\"0\",\"uname\":\"董斌斌\",\"userName\":\"董斌斌\",\"bmbh\":null}";
        setUserinfo(data);
    }

    public void click(View view) {
//        BaseWebAty.init("http://114.115.181.185:8080/wsyy/index.html");
        BaseWebAty.init("http://114.115.204.49/htAppWeb2");
        startActivity(new Intent(this, MainAty.class));
    }

    public void setUserinfo(String data) {
        LogUtils.e("userinfoTest", "data   =" + data);
        SharedPreferences.Editor editor = this.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE).edit();
        editor.putString("data", data);
        editor.commit();
    }
}
