package com.bin.mylibrary.aty;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.view.View;
import android.widget.Toast;


import com.bin.mylibrary.R;
import com.bin.mylibrary.base.BaseAty;
import com.bin.mylibrary.utils.LogUtils;
import com.cfca.mobile.hke.sipedit.SipEditText;
import com.cfca.mobile.hke.sipkeyboard.SipResult;
import com.cfca.mobile.log.CodeException;

import cn.com.cfca.sdk.hke.Callback;
import cn.com.cfca.sdk.hke.HKEException;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.bin.mylibrary.base.BaseApplication.hkeWithPasswordApi;


public class InputPasswordAty extends BaseAty {
    private SipEditText set_password;
    private SipEditText set_repassword;


    @NonNull
    @Override
    protected int getView() {
        return R.layout.activity_input_password;
    }

    @Override
    public void initView(Bundle parms) {

    }

    @Override
    public void doBusiness(Context mContext) {
        set_password = findViewById(R.id.set_password);
        set_repassword = findViewById(R.id.set_repassword);
        if (getIntent() != null) {
            if (getIntent().getStringExtra("random") != null) {
                set_password.setServerRandom(getIntent().getStringExtra("random"));
                set_repassword.setServerRandom(getIntent().getStringExtra("random"));
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    public void colse(View view) {
        Intent intent = new Intent();
        setResult(RESULT_OK, intent);
        finish();
    }

    public void onCLick(View view) {
        SipResult sipResult = null;
        SipResult sipResult2 = null;
        try {
            sipResult = set_password.getEncryptData();
            sipResult2 = set_repassword.getEncryptData();
        } catch (CodeException e) {
            e.printStackTrace();
        }
        if (sipResult == null) {
            Toast.makeText(this, "请输入的PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        if (sipResult2 == null) {
            Toast.makeText(this, "请输入确认的PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (!set_password.inputEqualsWith(set_repassword)) {
                Toast.makeText(this, "请确认两次输入的PIN码一致", Toast.LENGTH_LONG).show();
                return;
            }

        } catch (CodeException e) {
            e.printStackTrace();
        }
        hkeWithPasswordApi.setPassword(sipResult.getEncryptInput(), sipResult.getEncryptRandomNum(), new Callback<Void>() {
            @Override
            public void onResult(Void aVoid) {
                SweetAlertDialog s = new SweetAlertDialog(InputPasswordAty.this, SweetAlertDialog.SUCCESS_TYPE);
                s.setContentText("设置PIN码成功!")
                        .setConfirmText("确定")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                                LogUtils.e("设置PIN码成功 -->", "原因" + "点击了确定");
                                Intent intent = new Intent();
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        })
                        .show();
            }

            @Override
            public void onError(HKEException e) {
                String content = "";
                if (e.toString().contains("]")) {
                    content = e.toString().substring(e.toString().indexOf("]") + 1);
                } else {
                    content = e.toString();
                }
                LogUtils.e("设置PIN码失败  -->", "原因" + e.toString());
                SweetAlertDialog s = new SweetAlertDialog(InputPasswordAty.this, SweetAlertDialog.ERROR_TYPE);
                s.setContentText(content + "!")
                        .setConfirmText("确定")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
            }
        });
    }
}
