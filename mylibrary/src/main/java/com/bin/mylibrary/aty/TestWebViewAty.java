package com.bin.mylibrary.aty;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bin.mylibrary.R;
import com.bin.mylibrary.activity.CaptureActivity;
import com.bin.mylibrary.base.BaseWebAty;
import com.bin.mylibrary.interfaces.JavascriptInterfaceImpl;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.view.CoolIndicatorLayout;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.BaseIndicatorView;


public class TestWebViewAty extends BaseWebAty {
    // webviewLayout
    private LinearLayout wwLout;
    // webview
    private AgentWeb webview;
    private WebView mview;
    // js与webview交互接口
    private JavascriptInterfaceImpl javascriptInterface;
    // 返回标志，判断是返回上个网页还是返回Activity
    private boolean rflg = false;
    //设置PIN码的返回码
    private static final int REQUEST_PIN = 1005;
    //验证PIN码的返回码
    private static final int REQUEST_CHECK_PIN = 1006;
    //修改PIN码的返回码
    private static final int REQUEST_UPDATE_PIN = 1007;
    // 相机权限取得申请返回码
    private static final int OPEN_CAMERA_QRCODE = 617;
    // 扫一扫打开相机返回码
    private static final int REQUEST_OPEN_QRCODE = 6;


    private String url = "http://192.168.100.108/YZTDemo/Default.aspx";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getIntent() != null) {
            if (getIntent().getStringExtra("url") != null) {
                url = getIntent().getStringExtra("url");
            }
        }
        LogUtils.e(TAG, url);
        setContentView(R.layout.activity_test_web_view);
        javascriptInterface = new JavascriptInterfaceImpl(TestWebViewAty.this, mWebView);
    }

    @Nullable
    @Override
    protected String getUrl() {
        return url;
    }

    @Override
    protected void buildAgentWeb() {
        webType = 0;
        super.buildAgentWeb();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case OPEN_CAMERA_QRCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请成功，扫一扫
                    Intent intent = new Intent(this, CaptureActivity.class);
                    intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
                    startActivityForResult(intent, REQUEST_OPEN_QRCODE);
                } else {
                    Toast.makeText(this, "无相机调用权限，扫一扫功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
    }

    @NonNull
    @Override
    protected ViewGroup getAgentWebParent() {
        return (ViewGroup) this.findViewById(R.id.webviewLayout);
    }

    @NonNull
    @Override
    protected BaseIndicatorView getIndicatorView() {
        return new CoolIndicatorLayout(this);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            //设置完PIN码的回调
            case REQUEST_PIN:
                if (resultCode == RESULT_OK) {
                    Log.e("TestWebViewAty", "设置完PIN码的回调 -->" + "getPassword()");
                    javascriptInterface.getPassword();
                }
                break;
            //验证完PIN码的回调
            case REQUEST_CHECK_PIN:
                if (resultCode == RESULT_OK) {
                    String content = intent.getStringExtra("password");
                    String random = intent.getStringExtra("random");
                    javascriptInterface.setSignMethod(content, random);
                }
                break;
//            //修改完PIN码的回调
//            case REQUEST_UPDATE_PIN:
//                if (resultCode == RESULT_OK) {
//                    String oldPassword = intent.getStringExtra("oldPassword");
//                    String oldRandom = intent.getStringExtra("oldRandom");
//                    String newPassword = intent.getStringExtra("newPassword");
//                    String newRandom = intent.getStringExtra("newRandom");
//                    javascriptInterface.updatePINMethod(oldPassword, oldRandom, newPassword, newRandom);
//                }
//                break;
            //打开相机扫一扫返回值
            case REQUEST_OPEN_QRCODE:
                if (resultCode == RESULT_OK) {
                    String content = intent.getStringExtra("sn");
                    LogUtils.e(TAG, "扫一扫返回成功！扫码结果为：" + content);
                    WebView mview = webview.getWebCreator().getWebView();
                    mview.loadUrl("javascript:setOpenCameraQRcode('" + content + "')");
                }
                break;
            default:
        }
        super.onActivityResult(requestCode, resultCode, intent);
    }
}
