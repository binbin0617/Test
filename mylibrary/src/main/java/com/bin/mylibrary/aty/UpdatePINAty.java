package com.bin.mylibrary.aty;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;

import com.bin.mylibrary.R;
import com.bin.mylibrary.base.BaseAty;
import com.cfca.mobile.hke.sipedit.SipEditText;
import com.cfca.mobile.hke.sipkeyboard.SipResult;
import com.cfca.mobile.log.CodeException;

import cn.com.cfca.sdk.hke.Callback;
import cn.com.cfca.sdk.hke.HKEException;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static com.bin.mylibrary.base.BaseApplication.hkeWithPasswordApi;


public class UpdatePINAty extends BaseAty {
    private SipEditText oldPassword;
    private SipEditText newPassword;
    private SipEditText newRePassword;


    @NonNull
    @Override
    protected int getView() {
        return R.layout.activity_update_pin;
    }

    @Override
    public void initView(Bundle parms) {
    }

    @Override
    public void doBusiness(Context mContext) {
        oldPassword = findViewById(R.id.old_password);
        newPassword = findViewById(R.id.new_password);
        newRePassword = findViewById(R.id.new_repassword);
        if (getIntent() != null) {
            if (getIntent().getStringExtra("random") != null) {
                oldPassword.setServerRandom(getIntent().getStringExtra("random"));
                newPassword.setServerRandom(getIntent().getStringExtra("random"));
                newRePassword.setServerRandom(getIntent().getStringExtra("random"));
            }
        }
    }

    public void colse(View view) {
        finish();
    }

    public void onCLick(View view) {
        SipResult sipResult = null;
        SipResult sipResult2 = null;
        SipResult sipResult3 = null;
        try {
            sipResult = oldPassword.getEncryptData();
            sipResult2 = newPassword.getEncryptData();
            sipResult3 = newRePassword.getEncryptData();
        } catch (CodeException e) {
            e.printStackTrace();
        }
        if (sipResult == null) {
            Toast.makeText(this, "请输入旧的PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        if (sipResult2 == null) {
            Toast.makeText(this, "请输入新的PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        if (sipResult3 == null) {
            Toast.makeText(this, "请输入确认的PIN码", Toast.LENGTH_LONG).show();
            return;
        }
        try {
            if (!newPassword.inputEqualsWith(newRePassword)) {
                Toast.makeText(this, "请确认两次输入的PIN码一致", Toast.LENGTH_LONG).show();
                return;
            }

        } catch (CodeException e) {
            e.printStackTrace();
        }
        hkeWithPasswordApi.changePassword(sipResult.getEncryptInput(), sipResult.getEncryptRandomNum(),
                sipResult2.getEncryptInput(), sipResult2.getEncryptRandomNum(), new Callback<Void>() {
                    @Override
                    public void onResult(Void aVoid) {
                        SweetAlertDialog s = new SweetAlertDialog(UpdatePINAty.this, SweetAlertDialog.SUCCESS_TYPE);
                        s.setContentText("更新PIN码成功!")
                                .setConfirmText("确定")
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
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
                        Log.e("更新PIN码失败  -->", "原因" + e.toString());
                        SweetAlertDialog s = new SweetAlertDialog(UpdatePINAty.this, SweetAlertDialog.ERROR_TYPE);
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
