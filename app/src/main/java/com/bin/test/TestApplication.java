package com.bin.test;

import android.util.Log;

import com.baidu.idl.face.platform.FaceSDKManager;
import com.bin.mylibrary.base.BaseApplication;
import com.bin.mylibrary.faceReg.APIService;
import com.bin.mylibrary.faceReg.AccessToken;
import com.bin.mylibrary.faceReg.FaceException;
import com.bin.mylibrary.faceReg.OnResultListener;

public class TestApplication extends BaseApplication {
    //百度人脸识别
    public static String licenseID = "testSDK1-face-android";
    public static String licenseFileName = "idl-license.face-android";
    //百度银行卡识别
    public static String apiKey = "KNl0IEBtZpolOcuxoqsB8GdI";
    public static String secretKey = "QitzX4fYG7AEup9D72gV9Wc8spiqZzPs";
    @Override
    public void onCreate() {
        super.onCreate();
        //百度人脸识别
        FaceSDKManager.getInstance().initialize(this, licenseID, licenseFileName);
        //百度银行卡识别
        initAccessToken();
    }
    public void initAccessToken() {
        APIService.getInstance().init(this);
        // 用ak，sk获取token, 调用在线api，如：注册、识别等。为了ak、sk安全，建议放您的服务器，
        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
            @Override
            public void onResult(com.bin.mylibrary.faceReg.AccessToken result) {
                Log.i("百度银行卡识别", "AccessToken->" + result.getAccessToken());
            }

            @Override
            public void onError(FaceException error) {
                Log.e("百度银行卡识别", "AccessTokenError:" + error);
                error.printStackTrace();
            }
        }, apiKey, secretKey);
    }
}
