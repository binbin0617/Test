package com.bin.mylibrary.base;

import android.app.Application;
import android.util.Log;

import com.baidu.idl.face.platform.FaceSDKManager;
import com.baidu.idl.face.platform.LivenessTypeEnum;
import com.baidu.ocr.sdk.model.AccessToken;
import com.bin.mylibrary.faceReg.APIService;
import com.bin.mylibrary.faceReg.FaceException;
import com.bin.mylibrary.faceReg.OnResultListener;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
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
    //保存文档地址和名字的集合
    public static Map<String,String> fileMap;
    // 展示用图片
    public static String picBase64;

    //上传文档json
    public static String documentBase64;
    //云证通参数
    public static String APPID = "APP_SHENZHOUHAOTIAN";
    public static String bankID = "SHENZHOUHAOTIAN";
    //云证通  类
    public static HKEWithPasswordApi hkeWithPasswordApi;

    //存放云证通状态码
    private static Map<String, String> stateMap;

    // 百度人脸识别相关
    public static List<LivenessTypeEnum> livenessList = new ArrayList<LivenessTypeEnum>();
    public static boolean isLivenessRandom = false;

    //小米推送
    public static final String APP_ID = "2882303761518172712";
    public static final String APP_KEY = "5481817267712";

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
        fileMap = new HashMap<>();
        // 百度活体识别初始化sdk
        // 百度人脸服务token取得
        initHKE();
//        JPushInterface.init(getApplicationContext());
//        JAnalyticsInterface.init(getApplicationContext());
//        JAnalyticsInterface.setDebugMode(true);
//        Bugly.init(getApplicationContext(), "2853450716", false);
//        Beta.initDelay = 1*1000;


        // 初始化SDK
//        UMConfigure.init(this, "5e16d0294ca3572fd500003d", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
////        // 选用AUTO页面采集模式
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
//        // 支持在子进程中统计自定义事件
//        UMConfigure.setProcessEvent(true);
//        UMConfigure.setLogEnabled(true);
//
//        MobclickAgent.setDebugMode(true);
//        // 初始化SDK
//        UMConfigure.init(this, "5e16d0294ca3572fd500003d", "Umeng", UMConfigure.DEVICE_TYPE_PHONE, null);
//        // 选用AUTO页面采集模式
//        MobclickAgent.setPageCollectionMode(MobclickAgent.PageMode.AUTO);
    }


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

    private void initState() {
        stateMap = new HashMap<>();
        stateMap.put("", "");
    }
}
