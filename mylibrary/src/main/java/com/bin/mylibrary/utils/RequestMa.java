package com.szht.htappfuture.interfaces;

public interface RequestMa {
    // 相机权限取得申请返回码
    int PHOTO_REQUEST_CAMERA = 1;
    // 调用拍照取得权限返回码
    int OPEN_REQUEST_CAMERA = 2;
    //调用拍照取得图片返回码
    int OPEN_CAMERA_REQUEST = 3;
    //调用拍照取得图片二维码返回码
    int OPEN_CAMERA_QRREQUEST = 4;
    // 扫一扫打开相机返回码
    int REQUEST_OPEN_QRCODE = 6;
    // 相机权限取得申请返回码
    int OPEN_CAMERA_QRCODE = 617;
    // 身份证相机权限取得申请返回码
    int PHOTO_REQUEST_SFZ = 801;
    // 银行卡相机权限取得申请返回码
    int PHOTO_REQUEST_YHK = 802;
    // 通用票据相机权限取得申请返回码
    int PHOTO_REQUEST_TYPJ = 803;
    // 百度语音合成权限取得申请返回码
    int BAIDU_TTS_REQUEST = 804;
    // 身份证相机结果取得返回码
    int REQUEST_CODE_CAMERA = 901;
    // 银行卡相机结果取得返回码
    int REQUEST_CODE_BANKCARD = 902;
    // 通用票据相机结果取得返回码
    int REQUEST_CODE_RECEIPT = 903;
    // 调用相机取得对照原相片申请返回码
    int HEADCAMERA_REQUEST = 1001;
    // 调用相机取得对照原相片返回结果
    int REQUEST_CODE_HEADCAMERA = 1002;
    // 调用活体检测并人脸对比后返回结果
    int REQUEST_CODE_FACEREG = 1003;
    //设置PIN码的返回码
    int REQUEST_PIN = 1005;
    //修改PIN码的返回码
    int REQUEST_UPDATE_PIN = 1007;
    //验证PIN码的返回码
    int REQUEST_CHECK_PIN = 1006;
    // 文件权限取得申请返回码
    int OPEN_FILE_QRCODE = 1008;
    //获取到文件返回码
    int OPEN_FILE_RESULT_QRCODE = 1009;
    // 相机权限取得申请返回码
    int OPEN_PHOTO_ALBUM = 1100;
    //申请权限打开文档
    int REQUEST_OPEN_DOCUMENT = 1101;
    //打开文档获取地址返回值
    int OPEN_DOCUMENT = 1102;
    //获取唯一标识去的申请返回码
    int GAIN_RESULT = 1111;

    /**
     * 语音的请求地址
     */
    String speakUrl = "http://114.115.204.49/htAppWeb2/getHelpList";
//    String speakUrl = "http://192.168.100.156/htAppWeb2/getHelpList";

    //webservices 调用所需要的地址
    String WSDL_URI = "http://114.116.52.167:443/YZT/HKELocalSignAndVerify";
//     String WSDL_URI = "http://192.168.100.223:8080/YZT/HKELocalSignAndVerify";   测试服务器

    //     String WSDL_URI = "http://192.168.100.156/YZT/HKELocalSignAndVerify";   磊哥搭建的服务器
    //组装签名数据接口
    String soapUserAction = "/getUserAutherInfo";
    //使用机构证书对数据P1签名
    String soapP1Action = "/getP1SignValue";
    //验证签名结果
    String soapP7Action = "/verifyP7Sign";
}
