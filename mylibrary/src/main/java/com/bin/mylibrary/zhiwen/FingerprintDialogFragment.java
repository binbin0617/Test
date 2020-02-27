package com.bin.mylibrary.zhiwen;

import android.annotation.TargetApi;
import android.app.DialogFragment;
import android.content.Context;
import android.hardware.fingerprint.FingerprintManager;
import android.os.Bundle;
import android.os.CancellationSignal;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;


import com.bin.mylibrary.R;
import com.bin.mylibrary.aty.MainAty;
import com.bin.mylibrary.entity.BiometricsEntity;

import javax.crypto.Cipher;

@TargetApi(23)
public class FingerprintDialogFragment extends DialogFragment {
    private FingerprintManager fingerprintManager;

    private CancellationSignal mCancellationSignal;

    private Cipher mCipher;


    public void setBiometricsEntity(BiometricsEntity biometricsEntity) {
        this.biometricsEntity = biometricsEntity;
    }

    private BiometricsEntity biometricsEntity;

    private MainAty mActivity;

    private TextView errorMsg;

    private TextView error_msg2;

    private TextView otherTxt;

    private View otherXian;

    private int errorNum;

    /**
     * 标识是否是用户主动取消的认证。
     */
    private boolean isSelfCancelled;

    public void setCipher(Cipher cipher) {
        mCipher = cipher;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        mActivity = (MainAty) getActivity();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        fingerprintManager = getContext().getSystemService(FingerprintManager.class);
        setStyle(DialogFragment.STYLE_NORMAL, android.R.style.Theme_Material_Light_Dialog);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fingerprint_dialog, container, false);
        errorMsg = v.findViewById(R.id.error_msg);
        TextView cancel = v.findViewById(R.id.cancel);
        otherTxt = v.findViewById(R.id.other);
        otherXian = v.findViewById(R.id.other_xian);
        error_msg2 = v.findViewById(R.id.error_msg2);
        otherTxt.setText(biometricsEntity.getCallbackTitle());
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                stopListening();
            }
        });
        otherTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
                mActivity.otherTxtMethord();
            }
        });

        return v;
    }

    @Override
    public void onResume() {
        super.onResume();
        // 开始指纹认证监听
        startListening(mCipher);
    }

    @Override
    public void onPause() {
        super.onPause();
        // 停止指纹认证监听
        stopListening();
    }

    private void startListening(Cipher cipher) {
        isSelfCancelled = false;
        mCancellationSignal = new CancellationSignal();
        fingerprintManager.authenticate(new FingerprintManager.CryptoObject(cipher), mCancellationSignal, 0, new FingerprintManager.AuthenticationCallback() {
            @Override
            public void onAuthenticationError(int errorCode, CharSequence errString) {
                if (!isSelfCancelled) {
                    errorMsg.setText(errString);
                    error_msg2.setVisibility(View.VISIBLE);
                    error_msg2.setText("指纹识别功能被锁定，请锁上手机屏幕输入密码解锁此功能");
                    otherTxt.setVisibility(View.VISIBLE);
                    otherXian.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onAuthenticationHelp(int helpCode, CharSequence helpString) {
                errorMsg.setText(helpString);
            }

            @Override
            public void onAuthenticationSucceeded(FingerprintManager.AuthenticationResult result) {
                dismiss();
                errorNum = 0;
                mActivity.setDoBiometricsResult();
            }

            @Override
            public void onAuthenticationFailed() {
                errorNum += 1;
                errorMsg.setText("指纹认证失败，请再试一次");
                if (errorNum >= 3) {
                    otherTxt.setVisibility(View.VISIBLE);
                    otherXian.setVisibility(View.VISIBLE);
                    error_msg2.setVisibility(View.VISIBLE);
                    error_msg2.setText("过多失败会导致功能被锁");
                }
            }
        }, null);
    }

    private void stopListening() {
        if (mCancellationSignal != null) {
            mCancellationSignal.cancel();
            mCancellationSignal = null;
            isSelfCancelled = true;
        }
    }
}
