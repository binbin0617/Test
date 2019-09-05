package com.bin.mylibrary.base;

import android.app.Application;


import java.util.Map;

import cn.com.cfca.sdk.hke.HKEServiceType;
import cn.com.cfca.sdk.hke.HKEWithPasswordApi;
import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * 参照自http://blog.csdn.net/qq_21397217/article/details/52703920
 * Created by zhaolei on 2019/05/30.
 */

public class BaseApplication extends Application {

    /**
     * 日志输出标志
     **/
    protected final String TAG = this.getClass().getSimpleName();

    // sweetalertdialog
    public static SweetAlertDialog sweetAlertDialog;

    // 保存拍照调用的照片
    public static String photoBase64;
    // 展示用图片
    public static String picBase64;

    //云证通参数
    public static String APPID = "APP_SHENZHOUHAOTIAN";
    public static String bankID = "SHENZHOUHAOTIAN";
    //云证通  类
    public static HKEWithPasswordApi hkeWithPasswordApi;

    //存放云证通状态码
    private static Map<String, String> stateMap;

    // 百度人脸识别相关
//    public static String licenseID = "htappFuture2-face-android";
//    public static String licenseFileName = "idl-license.face-android";
//    public static String apiKey = "oLZOctDLwBvcOe3ytgVsA2oX";
//    public static String secretKey = "6LZ7TUgReSYeWG82WugaDp6mnLuz5odp";
//    public static List<LivenessTypeEnum> livenessList = new ArrayList<LivenessTypeEnum>();
//    public static boolean isLivenessRandom = false;

    public static String getOnLongClickPhotoBase64() {
        return onLongClickPhotoBase64;
    }

    public static void setOnLongClickPhotoBase64(String onLongClickPhotoBase64) {
        BaseApplication.onLongClickPhotoBase64 = onLongClickPhotoBase64;
    }

    public static String onLongClickPhotoBase64;

    @Override
    public void onCreate() {
        super.onCreate();
        // 百度活体识别初始化sdk
//        FaceSDKManager.getInstance().initialize(this, licenseID, licenseFileName);
        // 百度人脸服务token取得
//        initAccessToken();
        initHKE();
//        JPushInterface.init(getApplicationContext());
//        Bugly.init(getApplicationContext(), "2853450716", false);
    }

//    private void initAccessToken() {
//        APIService.getInstance().init(this);
//        // 用ak，sk获取token, 调用在线api，如：注册、识别等。为了ak、sk安全，建议放您的服务器，
//        APIService.getInstance().initAccessTokenWithAkSk(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken result) {
//                Log.i("百度人脸识别", "AccessToken->" + result.getAccessToken());
//            }
//
//            @Override
//            public void onError(FaceException error) {
//                Log.e("百度人脸识别", "AccessTokenError:" + error);
//                error.printStackTrace();
//
//            }
//        }, apiKey, secretKey);
//    }

    /**
     * @return sweetalertdialog
     */
    public static SweetAlertDialog getSweetAlertDialog() {
        return sweetAlertDialog;
    }

    public static void setSweetAlertDialog(SweetAlertDialog sweetAlertDialog) {
        BaseApplication.sweetAlertDialog = sweetAlertDialog;
    }

    public static String getPhotoBase64() {
        return photoBase64;
    }

    public static void setPhotoBase64(String photoBase64) {
        BaseApplication.photoBase64 = photoBase64;
    }

    public static String getPicBase64() {
        return picBase64;
    }

    public static void setPicBase64(String picBase64) {
        BaseApplication.picBase64 = picBase64;
    }

    private void initHKE() {
        //android 初始化云证通
//        HKEApi.initialize(getApplicationContext(),bankID , APPID, HKEServiceType.TEST);
        HKEWithPasswordApi.initialize(getApplicationContext(), bankID, APPID, HKEServiceType.TEST);
        hkeWithPasswordApi = HKEWithPasswordApi.getInstance();
    }

}
