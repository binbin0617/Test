package com.bin.mylibrary.aty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bin.mylibrary.R;
import com.bin.mylibrary.base.BaseAty;
import com.cfca.mobile.hke.sipedit.SipEditText;
import com.cfca.mobile.hke.sipkeyboard.SipResult;
import com.cfca.mobile.log.CodeException;


public class CheckPasswordAty extends BaseAty {
    private SipEditText set_password;

    @NonNull
    @Override
    protected int getView() {
        return R.layout.activity_check_password;
    }

    @Override
    public void initView(Bundle parms) {
    }

    @Override
    public void doBusiness(Context mContext) {
        set_password = findViewById(R.id.set_password);
        if (getIntent() != null) {
            if (getIntent().getStringExtra("random") != null) {
                set_password.setServerRandom(getIntent().getStringExtra("random"));
            }
        }
    }

    public void colse(View view) {
        Intent intent = new Intent();
        intent.putExtra("password", "");
        intent.putExtra("random", "");
        setResult(RESULT_OK, intent);
        finish();
    }
    @Override
    public void onBackPressed() {
        Intent intent = new Intent();
        intent.putExtra("password", "");
        intent.putExtra("random", "");
        setResult(RESULT_OK, intent);
        super.onBackPressed();
    }

    public void onCLick(View view) {
        SipResult sipResult = null;
        try {
            sipResult = set_password.getEncryptData();
        } catch (CodeException e) {
            e.printStackTrace();
        }
        if (sipResult == null) {
            Toast.makeText(this, "请输入PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        Intent intent = new Intent();
        intent.putExtra("password", sipResult.getEncryptInput());
        intent.putExtra("random", sipResult.getEncryptRandomNum());
        setResult(RESULT_OK, intent);
        finish();
    }
}
