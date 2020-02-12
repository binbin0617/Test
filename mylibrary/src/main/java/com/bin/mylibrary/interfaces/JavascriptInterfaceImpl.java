package com.bin.mylibrary.interfaces;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.BitmapFactory;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;
import android.webkit.JavascriptInterface;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.core.app.NotificationCompat;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.bin.mylibrary.R;
import com.bin.mylibrary.activity.CaptureActivity;
import com.bin.mylibrary.aty.CheckPasswordAty;
import com.bin.mylibrary.aty.InputPasswordAty;
import com.bin.mylibrary.aty.MainAty;
import com.bin.mylibrary.aty.TestWebViewAty;
import com.bin.mylibrary.aty.UpdatePINAty;
import com.bin.mylibrary.base.BaseApplication;
import com.bin.mylibrary.base.BaseWebAty;
import com.bin.mylibrary.cossphoto.CompressImageTask;
import com.bin.mylibrary.cossphoto.ImageConfig;
import com.bin.mylibrary.entity.UsersInfo;
import com.bin.mylibrary.entity.VerifyP7;
import com.bin.mylibrary.utils.FileUtil;
import com.bin.mylibrary.utils.Glide4Engine;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.utils.RequestMa;
import com.bin.mylibrary.utils.Utils;
import com.google.gson.Gson;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import org.json.JSONException;
import org.ksoap2.SoapFault;
import org.ksoap2.serialization.SoapObject;
import org.ksoap2.serialization.SoapSerializationEnvelope;
import org.ksoap2.transport.HttpTransportSE;
import org.xmlpull.v1.XmlPullParserException;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import cn.com.cfca.sdk.hke.Callback;
import cn.com.cfca.sdk.hke.HKEException;
import cn.com.cfca.sdk.hke.HKEWithPasswordApi;
import cn.com.cfca.sdk.hke.data.AuthenticateInfo;
import cn.com.cfca.sdk.hke.data.CFCACertificate;
import cn.pedant.SweetAlert.SweetAlertDialog;

import static android.content.Context.NOTIFICATION_SERVICE;
import static cn.pedant.SweetAlert.SweetAlertDialog.PROGRESS_TYPE;
import static com.bin.mylibrary.base.BaseApplication.hkeWithPasswordApi;

/**
 * 自定义的Android代码和JavaScript代码之间的桥梁类
 * 参照自http://www.cnblogs.com/kuangbiao/p/5246492.html
 */
public class JavascriptInterfaceImpl extends RequestMa {
    private String WSDL_URI_FACE = "http://114.116.53.84:10001/Face/FaceCheck.ashx";
    private final String TAG = JavascriptInterfaceImpl.class.getSimpleName();
    // 是否取得了百度调用token
    private boolean hasGotToken = false;
    // 弹出通知用通知管理
    NotificationManager mNotificationManager;
    // 与参照例子不同，这里用activity作为参数
    private Activity mActivity;
    private WebView mWebView;
    private Handler mHandler;
    // 相机权限取得申请返回码
    private static final int PHOTO_REQUEST_SAOYISAO = 1;
    // 扫一扫打开相机返回码
    private static final int REQUEST_OPEN_QRCODE = 6;
    // 身份证相机权限取得申请返回码
    private static final int PHOTO_REQUEST_SFZ = 801;
    // 银行卡相机权限取得申请返回码
    private static final int PHOTO_REQUEST_YHK = 802;
    // 通用票据相机权限取得申请返回码
    private static final int PHOTO_REQUEST_TYPJ = 803;
    // 百度语音合成权限取得申请返回码
    private static final int BAIDU_TTS_REQUEST = 804;
    // 身份证相机结果取得返回码
    private static final int REQUEST_CODE_CAMERA = 901;
    // 银行卡相机结果取得返回码
    private static final int REQUEST_CODE_BANKCARD = 902;
    // 通用票据相机结果取得返回码
    private static final int REQUEST_CODE_RECEIPT = 903;

    // 调用相机取得对照原相片返回结果
    private static final int REQUEST_CODE_HEADCAMERA = 1002;
    // 调用活体检测并人脸对比后返回结果
    private static final int REQUEST_CODE_FACEREG = 1003;
    // 调用相机取得对照原相片申请返回码
    private static final int HEADCAMERA_REQUEST = 1001;

    //设置PIN码的返回码
    private static final int REQUEST_PIN = 1005;
    //验证PIN码的返回码
    private static final int REQUEST_CHECK_PIN = 1006;
    //修改PIN码的返回码
    private static final int REQUEST_UPDATE_PIN = 1007;

    // 相机权限取得申请返回码
    private static final int OPEN_CAMERA_QRCODE = 617;

    // 文件权限取得申请返回码
    private static final int OPEN_FILE_QRCODE = 1008;
    //获取到文件返回码
    private static final int OPEN_FILE_RESULT_QRCODE = 1009;

    //获取唯一标识去的申请返回码
    private static final int GAIN_RESULT = 1111;

    //设置是否删除对比源
    public static boolean isDelFace = false;


    //交易信息
    private String businessText;
    private boolean isUpdatePIN = false;

    //获取当前设备的唯一id
    private String deviceId;
    //地址
    private String url;

    //webservices 调用所需要的参数
//    private String WSDL_URI = "http://192.168.100.223:8080/YZT/HKELocalSignAndVerify";   测试服务器
    private String WSDL_URI = "http://114.116.52.167:443/YZT/HKELocalSignAndVerify";
    //    private String WSDL_URI = "http://192.168.100.156/YZT/HKELocalSignAndVerify";   磊哥搭建的服务器
    private String namespace = "http://service.szht.com/";
    //组装签名数据接口
    private String getUserAutherInfoMethod = "getUserAutherInfo";
    private String soapUserAction = "http://114.116.52.167:443/YZT/HKELocalSignAndVerify/getUserAutherInfo";
    private String AuthResult = "";
    //使用机构证书对数据P1签名
    private String getP1SignValueMethod = "getP1SignValue";
    private String soapP1Action = "http://114.116.52.167:443/YZT/HKELocalSignAndVerify/getP1SignValue";
    //验证签名结果
    private String verifyP7SignMethod = "verifyP7Sign";
    private String soapP7Action = "http://114.116.52.167:443/YZT/HKELocalSignAndVerify/verifyP7Sign";
    //对组装数据的签名结果
    private String p1Result = "";
    //对组装Json报文的签名结果
    private String p1Result2 = "";
    //组装JSON报文接口返回结果
    private String jsonResult = "";
    //验证签名结果P7
    private boolean p7Result;

    //负责界面之间跳转的loading
    private SweetAlertDialog loadDialog;
    //极光用的参数
    public static int sequence = 1;

    private List<String> photoList;

    /**
     * Instantiate the interface and set the context
     */
    public JavascriptInterfaceImpl(Activity activity, WebView webView) {
        mActivity = activity;
        mWebView = webView;
        mHandler = new Handler(Looper.getMainLooper());
        photoList = new ArrayList<>();
        // 通知管理初期化
        mNotificationManager = (NotificationManager) mActivity.getSystemService(NOTIFICATION_SERVICE);
    }

    /**
     * Show a toast from the web page
     **/
    // 如果target 大于等于API 17，则需要加上如下注解
    @JavascriptInterface
    public void showToast(String toast, int flg) {
        // 短toast
        if (flg == 1) {
            Toast.makeText(mActivity, toast, Toast.LENGTH_SHORT).show();
        } else {
            // 长toast
            Toast.makeText(mActivity, toast, Toast.LENGTH_LONG).show();
        }
    }

    /**
     * js用跳转活动页面方法
     *
     * @param action   跳转action名
     * @param category 跳转category名
     * @param ywlx     业务类型
     **/
    @JavascriptInterface
    public void toActivity(String action, String category, String ywlx, int result) {
        // 画面（活动）跳转
        Intent intent = new Intent(action);
        intent.addCategory(category);
        intent.putExtra("ywlx", ywlx);
        mActivity.startActivityForResult(intent, result);
    }

    /**
     * 同步方法
     *
     * @return
     */
    @JavascriptInterface
    public String syncExec() {
        return "hello android";
    }

    /**
     * 异步方法
     *
     * @param msg
     * @param callbackId
     */
    @JavascriptInterface
    public void asyncExec(final String msg, final String callbackId) {
        new Thread() {
            @Override
            public void run() {
                SystemClock.sleep(5 * 1000);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        String url = "javascript:" + callbackId + "('" + msg
                                + " from android " + "')";
                        mWebView.loadUrl(url);
                    }
                });
            }
        }.start();
    }

    /**
     * 保存用户信息
     */
//    @JavascriptInterface
//    public String setUserinfo(String data) {
////        String dataTrim = data.replace(" ", "");
//        // 保存用户信息到手机本地
//        LogUtils.e(TAG, "data   =" + data);
//        SharedPreferences.Editor editor = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE).edit();
//        editor.putString("data", data);
//        editor.commit();
//        setTags(data);
//        mActivity.startActivity(new Intent(mActivity, MainAty.class));
//        SharedPreferences dataInfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
//        String userStr = dataInfo.getString("data", "");
//        com.alibaba.fastjson.JSONObject user = null;
//        if (!"".equals(userStr.trim())) {
//            // 用户信息不能为空
//            user = JSON.parseObject(userStr);
//            BaseWebAty.baseUrl = user.getString("loginUrl") + "/htAppWeb2";
//        }
//        finish();
//        LogUtils.e(TAG, "setUserinfo");
//        return "success";
//    }

    /**
     * 取得用户信息
     */
    @JavascriptInterface
    public String getUserinfo() {
        // 取得本地用户信息
        SharedPreferences dataInfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
        LogUtils.e(TAG, dataInfo.getString("data", ""));
        return dataInfo.getString("data", "");
    }

    /**
     * 取得用户信息异步
     */
    @JavascriptInterface
    public void getUserinfoSync() {
        // 取得本地用户信息
        SharedPreferences dataInfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
        final String finalUrl = "javascript:setUserInfo('" + dataInfo.getString("data", "") + "')";
        mWebView.post(() -> mWebView.loadUrl(finalUrl));
    }

    /**
     * 清空用户信息
     */
    @JavascriptInterface
    public String delUserinfo() {
        // 清空本地用户信息
        deleteTags();
        SharedPreferences.Editor editor = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE).edit();
        editor.putString("data", "");
        editor.commit();
        mActivity.startActivity(new Intent(mActivity, MainAty.class));
        BaseWebAty.baseUrl = "http://114.115.204.49/htAppWeb2";
        finish();
        return "success";
    }

    /**
     * 用户登录以后通过学校Id以及人员身份设置对应的Tags（标签）
     *
     * @param data
     */
    public void setTags(String data) {
//        Set<String> tags = new HashSet<>();
//        Gson gson = new Gson();
//        SetTagsBean setTagsBean = gson.fromJson(data, SetTagsBean.class);
//        tags.add(setTagsBean.getSchoolid());
//        LogUtils.e(TAG, "getSchoolid   == " + setTagsBean.getSchoolid());
//        tags.add(setTagsBean.getYhlx());
//        LogUtils.e(TAG, "getYhlx   == " + setTagsBean.getYhlx());
//        TagAliasOperatorHelper.TagAliasBean tagAliasBean = new TagAliasOperatorHelper.TagAliasBean();
//        tagAliasBean.action = ACTION_SET;
//        sequence++;
//        tagAliasBean.tags = tags;
//        TagAliasOperatorHelper.getInstance().handleAction(mActivity.getApplicationContext(), sequence, tagAliasBean);
    }

    /**
     * 用户退出登录以后通过学校Id以及人员身份删除对应的Tags（标签）
     */
    public void deleteTags() {
//        Set<String> tags = new HashSet<>();
//        SharedPreferences dataInfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
//        String data = dataInfo.getString("data", "");
//        LogUtils.e(TAG, "data   == " + data);
//        Gson gson = new Gson();
//        SetTagsBean setTagsBean = gson.fromJson(data, SetTagsBean.class);
//        tags.add(setTagsBean.getSchoolid());
//        tags.add(setTagsBean.getYhlx());
//        TagAliasOperatorHelper.TagAliasBean tagAliasBean = new TagAliasOperatorHelper.TagAliasBean();
//        tagAliasBean.action = ACTION_DELETE;
//        sequence++;
//        tagAliasBean.tags = tags;
//        TagAliasOperatorHelper.getInstance().handleAction(mActivity.getApplicationContext(), sequence, tagAliasBean);
    }

    /**
     * 页面跳转
     * 主要用于跨模块跳转
     */
    @JavascriptInterface
    public void jumpTo(String jsonStr) {
        LogUtils.e(TAG, "jumpTo     " + jsonStr);
        // 取得本地用户信息
        SharedPreferences dataInfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
        String userStr = dataInfo.getString("data", "");
        if (!"".equals(userStr.trim())) {
            // 用户信息不能为空
            JSONObject user = JSON.parseObject(userStr);
            if (user.getString("rybh") == null || "".equals(user.getString("rybh").trim())) {
                // 人员编号为空
                JSONObject object = new JSONObject();
                object.put("m", "error");
                object.put("msg", "登录信息失效,请重新登录。");
            } else {
                // 参数取得
                JSONObject info = JSON.parseObject(jsonStr);
                // 判断是否需要显示悬浮按钮 by3
//                String by3 = info.getString("by3");
//                if ("1".equals(by3)) {
                // 显示悬浮按钮
//                    MainActivity.isShow = true;
//                    MainAty.isShow = true;
//                } else {
                // 关闭悬浮按钮
//                    MainActivity.isShow = false;
//                    MainAty.isShow = false;
//                }
                // 页面跳转
                final String finalUrl = info.getString("url");
                mWebView.post(() -> mWebView.loadUrl(finalUrl));
            }
        } else {
            // 用户信息为空时报错
            JSONObject object = new JSONObject();
            object.put("m", "error");
            object.put("msg", "登录信息失效,请重新登录。");
        }
    }

    /**
     * 调用sweetalert
     */
    @JavascriptInterface
    public void callSweetalert(String jsondata) {
        com.alibaba.fastjson.JSONObject sadInfo = com.alibaba.fastjson.JSONObject.parseObject(jsondata);
        // 先取得当前可能存在的弹窗
        SweetAlertDialog checkDialog = BaseApplication.getSweetAlertDialog();
        if ("load".equals(sadInfo.getString("m"))) {
            // 先判断当前是否已有弹窗存在，如果有则不作处理,防止重复弹窗
            if (checkDialog != null) {
                return;
            }
            SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
            pDialog.getProgressHelper().setBarColor(Color.parseColor("#A5DC86"));
            pDialog.setTitleText(sadInfo.getString("msg"));
            pDialog.setCancelable(false);
            pDialog.show();
            BaseApplication.setSweetAlertDialog(pDialog);
        } else if ("showText".equals(sadInfo.getString("m"))) {
            // 先判断当前是否已有弹窗存在，如果有则关闭,防止重复弹窗
            if (checkDialog != null) {
                checkDialog.dismiss();
            }
            SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.NORMAL_TYPE);
            // 标题不显示
            // pDialog.setTitleVisibility(View.GONE);
            pDialog.setContentText(sadInfo.getString("msg"));
            pDialog.setConfirmText("确定");
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                }
            });
            pDialog.show();
        } else if ("warning".equals(sadInfo.getString("m"))) {
            // 先判断当前是否已有弹窗存在，如果有则关闭,防止重复弹窗
            if (checkDialog != null) {
                checkDialog.dismiss();
            }
            SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE);
            // pDialog.setTitleVisibility(View.GONE);
            pDialog.setContentText(sadInfo.getString("msg"));
            pDialog.setConfirmText("确定");
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                }
            });
            pDialog.show();
        } else if ("error".equals(sadInfo.getString("m"))) {
            // 先判断当前是否已有弹窗存在，如果有则关闭,防止重复弹窗
            if (checkDialog != null) {
                checkDialog.dismiss();
            }
            SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE);
            // pDialog.setTitleVisibility(View.GONE);
            pDialog.setContentText(sadInfo.getString("msg"));
            pDialog.setConfirmText("确定");
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                }
            });
            pDialog.show();
        } else if ("confirm".equals(sadInfo.getString("m"))) {
            // 先判断当前是否已有弹窗存在，如果有则关闭,防止重复弹窗
            if (checkDialog != null) {
                checkDialog.dismiss();
            }
            SweetAlertDialog pDialog = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE);
            // pDialog.setTitleVisibility(View.GONE);
            pDialog.setContentText(sadInfo.getString("msg"));
            pDialog.setConfirmText("确定");
            pDialog.setCancelText("取消");
            final String mcbconfirm = sadInfo.getString("mcbconfirm");
            final String mcbcancel = sadInfo.getString("mcbcancel");
            // 确定方法回调
            pDialog.setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    if (!TextUtils.isEmpty(mcbconfirm)) {
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                final String url = "javascript:" + mcbconfirm;
                                mWebView.loadUrl(url);
                            }
                        });
                    }
                }
            });
            // 取消方法回调
            pDialog.setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                @Override
                public void onClick(SweetAlertDialog sweetAlertDialog) {
                    sweetAlertDialog.dismiss();
                    if (!TextUtils.isEmpty(mcbcancel)) {
                        mWebView.post(new Runnable() {
                            @Override
                            public void run() {
                                final String url = "javascript:" + mcbcancel;
                                mWebView.loadUrl(url);
                            }
                        });
                    }
                }
            });
            pDialog.show();
        } else if ("close".equals(sadInfo.getString("m"))) {
            SweetAlertDialog pDialog = BaseApplication.getSweetAlertDialog();
            BaseApplication.setSweetAlertDialog(null);
            if (pDialog != null) {
                pDialog.dismiss();
            }
        }
    }

    /**
     * 调用扫一扫
     */
    @JavascriptInterface
    public void scanQrcode() {
        // 取得相机权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.VIBRATE}, PHOTO_REQUEST_SAOYISAO);
            } else {
                // 权限已经取得的情况下调用
                // 调用扫一扫
                Intent intent = new Intent(mActivity, CaptureActivity.class);
                intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
                mActivity.startActivityForResult(intent, REQUEST_OPEN_QRCODE);
            }
        } else {
            Intent intent = new Intent(mActivity, CaptureActivity.class);
            intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
            mActivity.startActivityForResult(intent, REQUEST_OPEN_QRCODE);
        }
    }

    /**
     * 返回上一页
     */
    @JavascriptInterface
    public void finish() {
        mWebView.post(() -> {
            // 返回首页
            if (mActivity instanceof BaseWebAty) {
                mWebView.loadUrl(((BaseWebAty) mActivity).baseUrl);
            }
        });
    }

    /**
     * js用弹出通知
     *
     * @param tzxxListStr 通知信息数组
     **/
    @JavascriptInterface
    public void callNotification(String tzxxListStr) {
        // 读取本地文件取得用户信息
        final SharedPreferences loaclUserinfo = mActivity.getSharedPreferences("userinfoTest", Context.MODE_PRIVATE);
        // 字符串转数组
        List<com.alibaba.fastjson.JSONObject> list = JSON.parseArray(tzxxListStr, com.alibaba.fastjson.JSONObject.class);
        // 循环呼出通知
        for (com.alibaba.fastjson.JSONObject obj : list) {
            // id
            final String id = obj.getString("id");
            // 标题
            String bt = obj.getString("bt");
            // 内容
            String nr = obj.getString("nr");
            // 判断系统版本根据版本不一样，通知方法也不一样
            if (Build.VERSION.SDK_INT >= 26) {
                // 26及以上版本
                // 弹出通知信息
                Intent intent = new Intent(mActivity, MainAty.class);
                PendingIntent pintent = PendingIntent.getActivity(mActivity, 0, intent, 0);
                int importance = NotificationManager.IMPORTANCE_LOW;
                @SuppressLint("WrongConstant") NotificationChannel mChannel = new NotificationChannel("htapp", "htapp", importance);
                mNotificationManager.createNotificationChannel(mChannel);
                Notification notification = new Notification.Builder(mActivity, id).setContentTitle("Title")
                        .setSmallIcon(R.drawable.main_launcher)
                        .setLargeIcon(BitmapFactory.decodeResource(mActivity.getResources(), R.drawable.main_launcher))
                        .setContentTitle(bt)
                        .setContentText(nr)
                        .setChannelId("htapp")
                        .setContentIntent(pintent)
                        .build();
                mNotificationManager.notify(Integer.valueOf(id), notification);
                Log.d(TAG, "版本26以上通知发送,id:" + id);
            } else {
                // 24及以下版本
                // 弹出通知信息 todo 测试
                NotificationCompat.Builder builder = new NotificationCompat.Builder(mActivity)
                        .setSmallIcon(R.drawable.main_launcher)
                        .setContentTitle(bt)
                        .setContentText(nr)
                        .setDefaults(Notification.DEFAULT_SOUND);
                Notification notify = builder.build();
                mNotificationManager.notify(Integer.valueOf(id), notify);
                Log.d(TAG, "版本24以下通知发送,id:" + id);
            }

            // TODO okhttp调用
//			// TODO 更新数据库，确保一条信息只通知一次
            String url = BaseWebAty.baseUrl + "/saveInformationRead";
//            HttpRequest.POST(mActivity, url, new Parameter().add("uid", id)
//                    .add("rybh", loaclUserinfo.getString("uuid", null)).add("flg", "2"), new ResponseListener() {
//                @Override
//                public void onResponse(String response, Exception error) {
//                    if (error == null) {
//                        LogUtils.e(TAG, "信息发送状态更新成功!");
//                    } else {
//                        LogUtils.e(TAG, "通知发送状态更新失败!" + error.toString());
//                    }
//                }
//            });
//			StringRequest stringRequest = new StringRequest(Request.Method.POST, url, new Response.Listener<String>() {
//				@Override
//				public void onResponse(String response) {
//					// 更新成功
//					Log.d(TAG, "信息发送状态更新成功。");
//				}
//			}, new Response.ErrorListener() {
//				@Override
//				public void onErrorResponse(VolleyError error) {
//					// 调用异常
//					LogUtils.e("Volley调用异常", "通知发送状态更新失败:" + error.toString());
//				}
//			}) {
//				/**
//				 * 返回含有POST或PUT请求的参数的Map
//				 */
//				@Override
//				protected Map<String, String> getParams() throws AuthFailureError {
//					Map<String, String> paramMap = new HashMap<>();
//					// 参数设置
//					// id 信息id
//					paramMap.put("uid", id);
//					// uuid 人员uuid
//					paramMap.put("rybh", loaclUserinfo.getString("uuid", null));
//					// flg 信息发送状态
//					paramMap.put("flg", "2");
//					return paramMap;
//				}
//			};
        }
    }

    /**
     * 取得baidu的图片识别token
     */
//    @JavascriptInterface
//    public void getBaiduToken() {
//        OCR.getInstance(mActivity).initAccessToken(new OnResultListener<AccessToken>() {
//            @Override
//            public void onResult(AccessToken accessToken) {
//                String token = accessToken.getAccessToken();
//                hasGotToken = true;
//            }
//
//            @Override
//            public void onError(OCRError error) {
//                error.printStackTrace();
//                showToast("licence方式获取token失败" + error.getMessage(), 1);
//            }
//        }, mActivity.getApplicationContext());
//    }

    private boolean checkTokenStatus() {
        if (!hasGotToken) {
            Toast.makeText(mActivity.getApplicationContext(), "token还未成功获取", Toast.LENGTH_LONG).show();
        }
        return hasGotToken;
    }

    /**
     * 身份证识别调用
     */
    @JavascriptInterface
    public void sfzCamera(String type) {
        // 取得相机权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 传递type参数
                ((BaseWebAty) mActivity).setType(type);
                // 取得调用镜头、文件操作权限
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PHOTO_REQUEST_SFZ);
            } else {
                // 权限已经取得的情况下调用
                toSfzCamera(type);
            }
        } else {
            toSfzCamera(type);
        }
    }

//    /**
//     * 身份证识别页面跳转
//     */
    public void toSfzCamera(String type) {
//        // 判断是否已取得token
//        if (!checkTokenStatus()) {
//            return;
//        }
//        //  初始化本地质量控制模型,释放代码在onDestory中
//        //  调用身份证扫描必须加上 intent.putExtra(HeadCameraActivity.KEY_NATIVE_MANUAL, true); 关闭自动初始化和释放本地模型
//        CameraNativeHelper.init(mActivity, OCR.getInstance(mActivity).getLicense(),
//                new CameraNativeHelper.CameraNativeInitCallback() {
//                    @Override
//                    public void onError(int errorCode, Throwable e) {
//                        String msg;
//                        switch (errorCode) {
//                            case CameraView.NATIVE_SOLOAD_FAIL:
//                                msg = "加载so失败，请确保apk中存在ui部分的so";
//                                break;
//                            case CameraView.NATIVE_AUTH_FAIL:
//                                msg = "授权本地质量控制token获取失败";
//                                break;
//                            case CameraView.NATIVE_INIT_FAIL:
//                                msg = "本地质量控制";
//                                break;
//                            default:
//                                msg = String.valueOf(errorCode);
//                        }
//                        LogUtils.e(TAG, "onError: 本地质量控制初始化错误，错误原因： " + msg);
//                    }
//                });
//        // 根据type判断
//        if ("shootSfzA".equals(type)) {
//            // 身份证正面拍照
//            Intent intent = new Intent(mActivity, CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                    Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
//            mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
//        } else if ("shootSfzB".equals(type)) {
//            // 身份证反面拍照
//            Intent intent = new Intent(mActivity, CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                    Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
//            mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
//        } else if ("scanSfzA".equals(type)) {
//            // 身份证正面扫描
//            Intent intent = new Intent(mActivity, CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                    Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE,
//                    true);
//            // KEY_NATIVE_MANUAL设置了之后CameraActivity中不再自动初始化和释放模型
//            // 请手动使用CameraNativeHelper初始化和释放模型
//            // 推荐这样做，可以避免一些activity切换导致的不必要的异常
//            intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL,
//                    true);
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_FRONT);
//            mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
//        } else if ("scanSfzB".equals(type)) {
//            // 身份证反面扫描
//            Intent intent = new Intent(mActivity, CameraActivity.class);
//            intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                    Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//            intent.putExtra(CameraActivity.KEY_NATIVE_ENABLE,
//                    true);
//            // KEY_NATIVE_MANUAL设置了之后CameraActivity中不再自动初始化和释放模型
//            // 请手动使用CameraNativeHelper初始化和释放模型
//            // 推荐这样做，可以避免一些activity切换导致的不必要的异常
//            intent.putExtra(CameraActivity.KEY_NATIVE_MANUAL,
//                    true);
//            intent.putExtra(CameraActivity.KEY_CONTENT_TYPE, CameraActivity.CONTENT_TYPE_ID_CARD_BACK);
//            mActivity.startActivityForResult(intent, REQUEST_CODE_CAMERA);
//        } else {
//            // type参数有问题
//            LogUtils.e(TAG, "toSfzCamera: 身份证识别参数有问题。");
//        }
    }

    /**
     * 银行卡取景框相机调用
     */
    @JavascriptInterface
    public void yhkCamera() {
        // 取得相机权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 取得调用镜头、文件操作权限
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PHOTO_REQUEST_YHK);
            } else {
                // 权限已经取得的情况下调用
                toYhkCamera();
            }
        } else {
            toYhkCamera();
        }
    }

    /**
     * 银行卡识别界面呼出
     */
    public void toYhkCamera() {
        if (!checkTokenStatus()) {
            return;
        }
//        Intent intent = new Intent(mActivity, CameraActivity.class);
//        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
//                CameraActivity.CONTENT_TYPE_BANK_CARD);
//        mActivity.startActivityForResult(intent, REQUEST_CODE_BANKCARD);
    }

    /**
     * 通用票据取景框相机调用
     */
    @JavascriptInterface
    public void typjCamera() {
        // 取得相机权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                // 取得调用镜头、文件操作权限
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.WRITE_EXTERNAL_STORAGE}, PHOTO_REQUEST_TYPJ);
            } else {
                // 权限已经取得的情况下调用
                toTypjCamera();
            }
        } else {
            toTypjCamera();
        }
    }

    /**
     * 通用票据识别界面呼出
     */
    public void toTypjCamera() {
        if (!checkTokenStatus()) {
            return;
        }
//        Intent intent = new Intent(mActivity, CameraActivity.class);
//        intent.putExtra(CameraActivity.KEY_OUTPUT_FILE_PATH,
//                Utils.getSaveFile(mActivity.getApplication()).getAbsolutePath());
//        intent.putExtra(CameraActivity.KEY_CONTENT_TYPE,
//                CameraActivity.CONTENT_TYPE_GENERAL);
//        mActivity.startActivityForResult(intent, REQUEST_CODE_RECEIPT);
    }

    /**
     * 拍摄对比原照片
     */
    @JavascriptInterface
    public void getPhoto() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, HEADCAMERA_REQUEST);
            } else {
//                // 设置面部图片保存参数
//                setFaceDetecetConfig();
//                Intent intent = new Intent(mActivity, FaceDetectExpActivity.class);
//                // 跳转采集界面
//                mActivity.startActivityForResult(intent, REQUEST_CODE_HEADCAMERA);
            }
        } else {
//            // 设置面部图片保存参数
//            setFaceDetecetConfig();
//            Intent intent = new Intent(mActivity, FaceDetectExpActivity.class);
//            // 跳转采集界面
//            mActivity.startActivityForResult(intent, REQUEST_CODE_HEADCAMERA);
        }
    }

    private void setFaceDetecetConfig() {
//        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
//        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
//        // 模糊度范围
//        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS);
//        // 光照范围
//        config.setBrightnessValue(60f);
//        // 裁剪人脸大小
//        config.setCropFaceValue(600);
//        // 人脸角度范围
//        config.setHeadPitchValue(FaceEnvironment.VALUE_HEAD_PITCH);
//        config.setHeadRollValue(FaceEnvironment.VALUE_HEAD_ROLL);
//        config.setHeadYawValue(FaceEnvironment.VALUE_HEAD_YAW);
//        // 最小检测人脸
//        config.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
//        //
//        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
//        // 人脸遮挡范围
//        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION);
//        // 是否进行适量检测
//        config.setCheckFaceQuality(true);
//        // 人脸检测使用线程数
//        config.setFaceDecodeNumberOfThreads(2);
//        // 是否开启提示音
//        config.setSound(true);
//        FaceSDKManager.getInstance().setFaceConfig(config);
    }

    /**
     * 页面展示用取得对比原照片base64
     */
    @JavascriptInterface
    public void getMotoHeadPhoto(String result) {
        // 判断结果
        if ("success".equals(result)) {
            // 取得本地对比元头像数据
            String filePath = mActivity.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                    File.separator + "image.dat";
            String hp = "";
            try {
                hp = Utils.fileRead(filePath);
                LogUtils.e(TAG, hp);
            } catch (Exception e) {
                e.printStackTrace();
                LogUtils.e(TAG, "取得文件失敗" + e.toString());
                // 异步执行方法、返回页面
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(url);
                    }
                });
                // 直接返回、不再继续执行
                // 异步执行方法、带着图片信息返回页面
                final String url = "javascript:setMHP('1','data:image/png;base64," + hp + "')";
                mWebView.post(new Runnable() {
                    @Override
                    public void run() {
                        mWebView.loadUrl(url);
                    }
                });
                return;
            }
            // 异步执行方法、带着图片信息返回页面
            final String url = "javascript:setMHP('2','data:image/png;base64," + hp + "')";
            mWebView.post(new Runnable() {
                @Override
                public void run() {
                    mWebView.loadUrl(url);
                }
            });
        } else {
            // 弹窗通知照片取得失败
            org.json.JSONObject object = new org.json.JSONObject();
            try {
                object.put("m", "warning");
                object.put("msg", "\\n照片拍摄失败！请再次拍摄\\n");
            } catch (JSONException e) {
                e.printStackTrace();
            }
            callSweetalert(object.toString());
        }
    }

    /**
     * 调用baidu的活体识别库进行识别
     */
    @JavascriptInterface
    public void faceReg() {
        // 设置参数
//        setFaceConfig();
//        // 跳转检测界面
//        Intent intent = new Intent(mActivity, FaceLivenessExpActivity.class);
//        mActivity.startActivityForResult(intent, REQUEST_CODE_FACEREG);
    }

    private void setFaceConfig() {
//        FaceConfig config = FaceSDKManager.getInstance().getFaceConfig();
//        // SDK初始化已经设置完默认参数（推荐参数），您也根据实际需求进行数值调整
//        // 根据需求添加活体动作
//        BaseApplication.livenessList.clear();
//        // 眨眼
//        BaseApplication.livenessList.add(LivenessTypeEnum.Eye);
////		// 张嘴
////		Myapplication.livenessList.add(LivenessTypeEnum.Mouth);
////		// 抬头
////		Myapplication.livenessList.add(LivenessTypeEnum.HeadUp);
////		// 低头
////		Myapplication.livenessList.add(LivenessTypeEnum.HeadDown);
////		// 左转
////		Myapplication.livenessList.add(LivenessTypeEnum.HeadLeft);
////		// 右转
////		Myapplication.livenessList.add(LivenessTypeEnum.HeadRight);
////		// 摇头
////		Myapplication.livenessList.add(LivenessTypeEnum.HeadLeftOrRight);
//        // 设置活体动作
//        config.setLivenessTypeList(BaseApplication.livenessList);
//        // 设置活体动作是否随机
//        config.setLivenessRandom(BaseApplication.isLivenessRandom);
//        // 模糊度范围
//        config.setBlurnessValue(FaceEnvironment.VALUE_BLURNESS);
//        // 光照范围
//        config.setBrightnessValue(FaceEnvironment.VALUE_BRIGHTNESS);
//        // 裁剪人脸大小
//        config.setCropFaceValue(FaceEnvironment.VALUE_CROP_FACE_SIZE);
//        // 人脸角度范围
//        config.setHeadPitchValue(FaceEnvironment.VALUE_HEAD_PITCH);
//        config.setHeadRollValue(FaceEnvironment.VALUE_HEAD_ROLL);
//        config.setHeadYawValue(FaceEnvironment.VALUE_HEAD_YAW);
//        // 最小检测人脸
//        config.setMinFaceSize(FaceEnvironment.VALUE_MIN_FACE_SIZE);
//        //
//        config.setNotFaceValue(FaceEnvironment.VALUE_NOT_FACE_THRESHOLD);
//        // 人脸遮挡范围
//        config.setOcclusionValue(FaceEnvironment.VALUE_OCCLUSION);
//        // 是否进行适量检测
//        config.setCheckFaceQuality(true);
//        // 人脸检测使用线程数
//        config.setFaceDecodeNumberOfThreads(2);
//        // 是否开启提示音
//        config.setSound(true);
//
//        FaceSDKManager.getInstance().setFaceConfig(config);
    }

    /**
     * 返回人脸对比结果
     *
     * @param result
     */
    public void doReg(String result) {
        // 异步执行方法、带着人脸对比结果返回页面
        final String url = "javascript:doRegReturn('" + result + "')";
        mWebView.post(() -> mWebView.loadUrl(url));

    }

    /**
     * 人脸对比结果成功以后删除原照片
     */
    public void deletePhoto() {
        final String url = "javascript:setMHP('3','')";
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.loadUrl(url);
            }
        });

    }

    /**
     * 清除浏览器缓存
     */
    @JavascriptInterface
    public void clearCache() {
        mWebView.post(new Runnable() {
            @Override
            public void run() {
                mWebView.clearCache(true);
                Log.d(TAG, "缓存清理成功 ");
                showToast("缓存清理成功。", 2);
            }
        });
    }

    /**
     * 跳转微信小程序
     */
    @JavascriptInterface
    public void toWx() {
        // TODO
//		String appId = "wx1e79f13a81635ce1"; // 填应用AppId
//		IWXAPI api = WXAPIFactory.createWXAPI(mActivity, appId);
//
//		WXLaunchMiniProgram.Req req = new WXLaunchMiniProgram.Req();
//		req.userName = "gh_0738886b39c7"; // 填小程序原始id
//		req.path = "pages/index/index?key1=123&key2=神州浩天";//拉起小程序页面的可带参路径，不填默认拉起小程序首页
//		req.miniprogramType = WXLaunchMiniProgram.Req.MINIPROGRAM_TYPE_PREVIEW;// 可选打开 开发版，体验版和正式版
//		api.sendReq(req);
    }

    /**
     * 保存信息
     */
    @JavascriptInterface
    public String saveData(String key, String data) {
        LogUtils.e(TAG, "key" + key + "data" + data);
        if ("userinfoTest".equals(key)) {
            // 不能跟用户信息冲突
            return "error";
        } else {
            // 保存信息到手机本地
            SharedPreferences.Editor editor = mActivity.getSharedPreferences(key, Context.MODE_PRIVATE).edit();
            editor.putString("data", data);
            editor.commit();
            LogUtils.e(TAG, "success");
            return "success";
        }
    }

    /**
     * 取得信息
     */
    @JavascriptInterface
    public void getData(String key) {
//        if ("userinfoTest".equals(key)) {
//            // 不能跟用户信息冲突
//            return "error";
//        } else {
//            // 取得本地信息
//            SharedPreferences dataInfo = mActivity.getSharedPreferences(key, Context.MODE_PRIVATE);
//            return dataInfo.getString("data", "");
//        }
        LogUtils.e(TAG, "key" + key);
        if ("userinfoTest".equals(key)) {
            // 不能跟用户信息冲突
            url = "javascript:setData('" + key + "','" + "error" + "')";
            // 调用页面方法
            final String finalUrl = url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.post(() -> mWebView.evaluateJavascript(finalUrl, s -> {
                    // nothing
                }));
            } else {
                mWebView.post(() -> mWebView.loadUrl(finalUrl));
            }
        } else {
            // 取得本地信息
            SharedPreferences dataInfo = mActivity.getSharedPreferences(key, Context.MODE_PRIVATE);
            LogUtils.e(TAG, "data" + dataInfo.getString("data", ""));
            url = "javascript:setData('" + key + "','" + dataInfo.getString("data", "") + "')";
            // 调用页面方法
            final String finalUrl = url;
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.post(() -> mWebView.evaluateJavascript(finalUrl, s -> {
                    // nothing
                }));
            } else {
                mWebView.post(() -> mWebView.loadUrl(finalUrl));
            }
        }
    }

    /**
     * 调用扫一扫
     */
    @JavascriptInterface
    public void saoyisao() {
        openCameraQRcode();
    }

    /**
     * 云证书下载流程
     */


    /**
     * dbb  获取服务器随机数
     */
    public void getCFCA(String json) {
        // 取得获取设备唯一标识权限
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.READ_PHONE_STATE}, GAIN_RESULT);
            } else {
                // 权限已经取得的情况下调用
                deviceId = getLocaldeviceId(mActivity);
            }
        } else {
            deviceId = getLocaldeviceId(mActivity);
        }
        Gson gson = new Gson();
        UsersInfo usersInfo = gson.fromJson(json, UsersInfo.class);
        if (hkeWithPasswordApi == null) {
            hkeWithPasswordApi = HKEWithPasswordApi.getInstance();
        }
        hkeWithPasswordApi.requestHKEServerRandom(usersInfo.getName(), usersInfo.getIdType(),
                usersInfo.getIdNo(), usersInfo.getPhoneNo(), deviceId, new Callback<String>() {
                    @Override
                    public void onResult(String s1) {
                        if (usersInfo.getData() != null) {
                            businessText = usersInfo.getData().toString();
                            jsonResult = usersInfo.getData().toString();
                        }
                        requestUserAuthAsync(s1, usersInfo.getIdType(), usersInfo.getIdNo(), usersInfo.getPhoneNo());
                        LogUtils.e("获取到随机数  ", "随机数为-->" + s1 + "<--");
                        // 调用页面方法
//                        url = "javascript:setCFCAResult('" + s1 + "')";
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
//                                // nothing
//                            }));
//                        } else {
//                            mWebView.post(() -> mWebView.loadUrl(url));
//                        }
                    }

                    @SuppressLint("ObsoleteSdkInt")
                    @Override
                    public void onError(HKEException e) {
                        loadDialog.dismiss();
                        LogUtils.e("获取失败", "HKEException-->" + e.toString() + "<--");
                        String result = "获取失败！";
//                        url = "javascript:setCFCAResult('" + result + "')";
                        // 调用页面方法
//                        final String finalUrl = url;
//                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//                            mWebView.post(() -> mWebView.evaluateJavascript(finalUrl, s -> {
//                                // nothing
//                            }));
//                        } else {
//                            mWebView.post(() -> mWebView.loadUrl(finalUrl));
//                        }
                    }
                });
    }

    /**
     * 通过网络请求组装签名数据
     */
    private String requestUserAuth(String random, String idType, String idNo, String phone) throws SoapFault {
        SoapObject request = new SoapObject(namespace, getUserAutherInfoMethod);
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        request.addProperty("random", random);
        request.addProperty("idType", idType);
        request.addProperty("idNo", idNo);
        request.addProperty("phoneNo", phone);
        //创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)-
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
        envelope.bodyOut = request;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = false;//由于是.net开发的webservice，所以这里要设置为true
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI);
        try {
            httpTransportSE.call(soapUserAction, envelope);//调用
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        String name = "";
        if (envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            name = result.getProperty(0).toString();
            LogUtils.e("requestUserAuth  -->", "返回值-->" + name + "<--");
        } else {
            LogUtils.e("requestUserAuth  -->", "无返回" + "<--");
        }
        return name;
    }

    /**
     * 开启子线程进行网络请求组装签名数据
     */
    @SuppressLint("StaticFieldLeak")
    private void requestUserAuthAsync(String random, String idType, String idNo, String phone) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    AuthResult = requestUserAuth(random, idType, idNo, phone);
                } catch (SoapFault soapFault) {
                    soapFault.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                requestP1Async();
                super.onPostExecute(s);
            }
        }.execute();
    }

    /**
     * 使用机构证书对数据进行P1签名
     */
    public String getP1SignValue(String result1) throws SoapFault {
        SoapObject request = new SoapObject(namespace, getP1SignValueMethod);
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        request.addProperty("orgSignContent", result1);
        //创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
        envelope.bodyOut = request;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = false;//由于是.net开发的webservice，所以这里要设置为true
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI);
        try {
            httpTransportSE.call(soapP1Action, envelope);//调用
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        String name = "";
        if (envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            name = result.getProperty(0).toString();
            LogUtils.e("getP1SignValue  ", "返回值  -->" + name + "<--");
        } else {
            LogUtils.e("getP1SignValue  ", "无返回" + "<--");
        }
        return name;
    }

    /**
     * 开启子线程进行第二个网络请求使用机构证书对数据进行P1签名
     */
    @SuppressLint("StaticFieldLeak")
    public void requestP1Async() {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    p1Result = getP1SignValue(AuthResult);
                } catch (SoapFault soapFault) {
                    soapFault.printStackTrace();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s) {
                authSignture();
                super.onPostExecute(s);
            }
        }.execute();
    }

    /**
     * 进行用户身份认证  有PIN签名
     */
    private void authSignture() {
        hkeWithPasswordApi.authenticateWithServerSignature(p1Result, new Callback<AuthenticateInfo>() {
            @Override
            public void onResult(AuthenticateInfo authenticateInfo) {
                LogUtils.e(TAG, "authenticateWithServerSignature" + authenticateInfo.toString());
                if (authenticateInfo.getCertificates().size() == 0) {
                    loadDialog.dismiss();
                    SweetAlertDialog s = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE);
                    s.setContentText("您尚未下载证书，是否现在下载？")
                            .setCancelText("取消")
                            .setConfirmText("确定")
                            .showCancelButton(true)
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    sweetAlertDialog.dismiss();
                                    SweetAlertDialog sweetAlertDialog1 = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
                                    sweetAlertDialog1
                                            .setContentText("正在下载证书,请稍后...")
                                            .showCancelButton(true).show();
                                    hkeWithPasswordApi.downloadCertificate(new Callback<CFCACertificate>() {
                                        @Override
                                        public void onResult(CFCACertificate cfcaCertificate) {
                                            sweetAlertDialog1.dismiss();
                                            SweetAlertDialog s = new SweetAlertDialog(mActivity, SweetAlertDialog.SUCCESS_TYPE);
                                            s.setContentText("下载证书成功!")
                                                    .setConfirmText("确定")
                                                    .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                                        @Override
                                                        public void onClick(SweetAlertDialog sweetAlertDialog) {
                                                            sweetAlertDialog.dismiss();
                                                            url = "javascript:cancel_loading()";
                                                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                                                mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                                                                    // nothing
                                                                }));
                                                            } else {
                                                                mWebView.post(() -> mWebView.loadUrl(url));
                                                            }
                                                        }
                                                    })
                                                    .show();
                                            String Base64 = cfcaCertificate.getContentBase64();
                                            String CN = cfcaCertificate.getSubjectCN();
                                            String DN = cfcaCertificate.getSubjectDN();
                                            LogUtils.e("HKE", "Base64  -->" + Base64);
                                            LogUtils.e("HKE", "CN  -->" + CN);
                                            LogUtils.e("HKE", "DN:  -->" + DN);
                                            LogUtils.e("HKE", "证书类型1 -->" + cfcaCertificate.getCert().toString());
                                        }

                                        @Override
                                        public void onError(HKEException e) {
                                            sweetAlertDialog1.dismiss();
                                            LogUtils.e("HKE  -->", "证书下载失败");
                                        }
                                    });
                                }
                            })
                            .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sDialog) {
                                    sDialog.dismiss();
                                    url = "javascript:cancel_loading()";
                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                        mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                                            // nothing
                                        }));
                                    } else {
                                        mWebView.post(() -> mWebView.loadUrl(url));
                                    }
                                }
                            })
                            .show();
                } else {
                    CFCACertificate cfcaCertificate = authenticateInfo.getCertificates().get(0);
                    LogUtils.e("HKE  -->", "证书KEY -->" + cfcaCertificate.getKeyUsage().toString());
                    LogUtils.e("HKE  -->", "证书类型 1-->" + cfcaCertificate.getCert().toString());
                    if (authenticateInfo.getPinState() == 0) {
                        loadDialog.dismiss();

//                        requestJsonAsync(businessText, "", "");   无PIN码签名
                    } else if (authenticateInfo.getPinState() == 1) {
                        loadDialog.dismiss();
                        SweetAlertDialog s = new SweetAlertDialog(mActivity, SweetAlertDialog.WARNING_TYPE);
                        s.setContentText("您尚未设置PIN码，是否现在设置？")
                                .setCancelText("取消")
                                .setConfirmText("确定")
                                .showCancelButton(true)
                                .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        Intent intent = new Intent(mActivity, InputPasswordAty.class);
                                        intent.putExtra("random", authenticateInfo.getPinServerRandom());
                                        mActivity.startActivityForResult(intent, REQUEST_PIN);
                                    }
                                })
                                .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                    @Override
                                    public void onClick(SweetAlertDialog sweetAlertDialog) {
                                        sweetAlertDialog.dismiss();
                                        url = "javascript:cancel_loading()";
                                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                                            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                                                // nothing
                                            }));
                                        } else {
                                            mWebView.post(() -> mWebView.loadUrl(url));
                                        }
                                    }
                                })
                                .show();
                    } else if (authenticateInfo.getPinState() == 2) {
                        if (isUpdatePIN) {
                            loadDialog.dismiss();
                            Intent intent = new Intent(mActivity, UpdatePINAty.class);
                            intent.putExtra("random", authenticateInfo.getPinServerRandom());
                            mActivity.startActivityForResult(intent, REQUEST_UPDATE_PIN);
                        } else {
                            loadDialog.dismiss();
                            Intent intent = new Intent(mActivity, CheckPasswordAty.class);
                            intent.putExtra("random", authenticateInfo.getPinServerRandom());
                            mActivity.startActivityForResult(intent, REQUEST_CHECK_PIN);
                        }
                    }
                }
            }

            @Override
            public void onError(HKEException e) {
                loadDialog.dismiss();
                LogUtils.e("身份认证失败  -->", "原因" + e.toString());
            }
        });

    }

    /**
     * 获取到密码、随机数、签名报文、机构证书对Json签名值  然后安卓本地调用云证通方法签名
     *
     * @param password
     * @param random
     */
    public void signMessageWithBusinessMessage(String password, String random) {
        hkeWithPasswordApi.signMessageWithBusinessMessage(jsonResult, p1Result2, password, random, new Callback<String>() {
            @Override
            public void onResult(String s1) {
                LogUtils.e("BusinessMessage  -->", "签名成功" + s1 + "");
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                try {
                    jsonObject.put("state", "1");
                    jsonObject.put("result", s1);
                } catch (JSONException e) {
                    e.printStackTrace();
                }
                if (loadDialog != null) {
                    loadDialog.dismiss();
                }
                url = "javascript:signMessageWithBusinessMessage_callBack('" + jsonObject.toString() + "')";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                        // nothing
                    }));
                } else {
                    mWebView.post(() -> mWebView.loadUrl(url));
                }
            }

            @Override
            public void onError(HKEException e) {
                if (loadDialog != null) {
                    loadDialog.dismiss();
                }
                org.json.JSONObject jsonObject = new org.json.JSONObject();
                try {
                    jsonObject.put("state", "0");
                    if (e.toString().contains("]")) {
                        jsonObject.put("result", e.toString().substring(e.toString().indexOf("]") + 1));
                    } else {
                        jsonObject.put("result", e.toString());
                    }
                } catch (JSONException e1) {
                    e1.printStackTrace();
                }
                new SweetAlertDialog(mActivity, SweetAlertDialog.ERROR_TYPE)
                        .setTitleText("提示")
                        .setContentText(jsonObject.toString() + "!")
                        .setConfirmText("确定")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .show();
                url = "javascript:signMessageWithBusinessMessage_callBack('" + jsonObject.toString() + "')";
                LogUtils.e("BusinessMessage  -->", "签名失败" + jsonObject.toString() + "");
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                        // nothing
                    }));
                } else {
                    mWebView.post(() -> mWebView.loadUrl(url));
                }

            }
        });
    }

    /**
     * 开启子线程进行第三个网络请求验证签名结果
     */
    @SuppressLint("StaticFieldLeak")
    public void requestP7AsyncJson(String p7ResultStr) {
        new AsyncTask<String, Integer, String>() {
            @Override
            protected String doInBackground(String... strings) {
                try {
                    p7Result = verifyP7SignMethod(p7ResultStr);
                } catch (SoapFault soapFault) {
                    soapFault.printStackTrace();
                    LogUtils.e("requestP7AsyncJson 错误 ", "-->" + soapFault.toString() + "<--");
                } catch (Exception e) {
                    e.printStackTrace();
                    LogUtils.e("requestP7AsyncJson 错误 ", "-->" + e.toString() + "<--");
                }
                return null;
            }

            @Override
            protected void onPostExecute(String s1) {
                if (p7Result) {
                    org.json.JSONObject jsonObject = new org.json.JSONObject();
                    try {
                        jsonObject.put("state", "1");
                        jsonObject.put("result", "验签成功");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    url = "javascript:verifyP7Sign_callBack('" + jsonObject.toString() + "')";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                            // nothing
                        }));
                    } else {
                        mWebView.post(() -> mWebView.loadUrl(url));
                    }
                } else {
                    org.json.JSONObject jsonObject = new org.json.JSONObject();
                    try {
                        jsonObject.put("state", "0");
                        jsonObject.put("result", "验签失败");
                    } catch (JSONException e1) {
                        e1.printStackTrace();
                    }
                    url = "javascript:verifyP7Sign_callBack('" + jsonObject.toString() + "')";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                        mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                            // nothing
                        }));
                    } else {
                        mWebView.post(() -> mWebView.loadUrl(url));
                    }
                }
                super.onPostExecute(s1);
            }
        }.execute();
    }

    /**
     * 验证签名结果调用的webServices
     *
     * @param p7Result
     * @return
     * @throws SoapFault
     */
    public boolean verifyP7SignMethod(String p7Result) throws SoapFault {
        SoapObject request = new SoapObject(namespace, verifyP7SignMethod);
        // 设置需调用WebService接口需要传入的两个参数mobileCode、userId
        request.addProperty("p7SignValue", p7Result);
        request.addProperty("orgContent", businessText);
        //创建SoapSerializationEnvelope 对象，同时指定soap版本号(之前在wsdl中看到的)
        SoapSerializationEnvelope envelope = new SoapSerializationEnvelope(SoapSerializationEnvelope.VER11);
        envelope.bodyOut = request;//由于是发送请求，所以是设置bodyOut
        envelope.dotNet = false;//由于是.net开发的webservice，所以这里要设置为true
        envelope.setOutputSoapObject(request);
        HttpTransportSE httpTransportSE = new HttpTransportSE(WSDL_URI);
        try {
            httpTransportSE.call(soapP7Action, envelope);//调用
        } catch (IOException e) {
            e.printStackTrace();
        } catch (XmlPullParserException e) {
            e.printStackTrace();
        }
        boolean name = true;
        if (envelope.getResponse() != null) {
            SoapObject result = (SoapObject) envelope.bodyIn;
            name = Boolean.parseBoolean(result.getProperty(0).toString());
            LogUtils.e("verifyP7SignMethod  ", "返回值  -->" + name + "<--");
        } else {
            LogUtils.e("verifyP7SignMethod  返回值  -->  ", "无返回" + "<--");
        }
        return name;
    }

    /**
     * 验证完密码的回调  客户端进行签名
     *
     * @param password
     * @param random
     */
    @SuppressLint("StaticFieldLeak")
    public void setSignMethod(String password, String random) {
        if (!("").equals(password) && !("").equals(random)) {
            loadDialog = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
            loadDialog.setContentText("请稍候...").show();
            new AsyncTask<String, Integer, String>() {
                @Override
                protected String doInBackground(String... strings) {
                    try {
                        p1Result2 = getP1SignValue(jsonResult);
                    } catch (SoapFault soapFault) {
                        soapFault.printStackTrace();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return null;
                }

                @Override
                protected void onPostExecute(String s) {
                    signMessageWithBusinessMessage(password, random);
                    super.onPostExecute(s);
                }
            }.execute();
        } else {
            url = "javascript:cancel_loading()";
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                    // nothing
                }));
            } else {
                mWebView.post(() -> mWebView.loadUrl(url));
            }
        }

    }

    /**
     * 设置完密码的回调  将设置完密码加密后的结果返回服务器
     * 开启子线程进行第二个网络请求使用机构证书对组装后的JSON报文进行P1签名
     */
    public void getPassword() {
        url = "javascript:cancel_loading()";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                // nothing
            }));
        } else {
            mWebView.post(() -> mWebView.loadUrl(url));
        }

    }

    /**
     * 获取机器唯一标识
     *
     * @param context
     * @return
     */
    public String getLocaldeviceId(Context context) {
        TelephonyManager tm = (TelephonyManager) context
                .getSystemService(Context.TELEPHONY_SERVICE);
        @SuppressLint("MissingPermission")
        String deviceId = tm.getDeviceId();
        if (deviceId == null
                || deviceId.trim().length() == 0) {
            deviceId = String.valueOf(System
                    .currentTimeMillis());
        }
        return deviceId;
    }

    /**
     * dbb  跳转新的界面
     */
    @JavascriptInterface
    public void downloadCFCA(String json) {
//        mWebView.post(() -> mWebView.loadUrl("http://114.115.181.185:8080/htAppWeb2"));
//        openFile();
//        openPhotoAlbum();
        Intent intent = new Intent(mActivity, TestWebViewAty.class);
        mActivity.startActivityForResult(intent, REQUEST_PIN);
    }

    public void openFile() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 取得相机权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_FILE_QRCODE);
            } else {
                Intent intent = new Intent();
                intent.setAction(Intent.ACTION_GET_CONTENT);
                intent.setType("*/*");
                mActivity.startActivityForResult(intent, OPEN_FILE_RESULT_QRCODE);
            }
        } else {
            Intent intent = new Intent();
            intent.setAction(Intent.ACTION_GET_CONTENT);
            intent.setType("*/*");
            mActivity.startActivityForResult(intent, OPEN_FILE_RESULT_QRCODE);
        }
    }

    public void openFileAlbumResult(String fliePath, String fileName) {
        LogUtils.e(TAG, "名字：" + fileName + "路径：" + fliePath);
//        photoList.addAll(mList);
//        List<String> baseList = new ArrayList<>();
//        for (int i = 0; i < mList.size(); i++) {
//            try {
//                baseList.add(FileUtil.imageToBase64(mList.get(i)));
//            } catch (Exception e) {
//                e.printStackTrace();
//            }
//        }
//        Gson g = new Gson();
//        String json = g.toJson(baseList);
//        url = "javascript:getFileResult('" + json + "')";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                // nothing
            }));
        } else {
            mWebView.post(() -> mWebView.loadUrl(url));
        }
    }

    /**
     * dbb  修改PIN码
     */
    @JavascriptInterface
    public void changePassword(String json) {
        loadDialog = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
        loadDialog.setContentText("请稍候...").show();
        isUpdatePIN = true;
        getCFCA(json);
    }

    /**
     * dbb  有PIN码签名
     */
    @JavascriptInterface
    public void signMessageWithBusinessMessage(String json) {
        loadDialog = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
        loadDialog.setContentText("请稍候...").show();
        isUpdatePIN = false;
        getCFCA(json);
    }

    /**
     *
     */
    @JavascriptInterface
    public void verifyP7Sign(String json) {
        isUpdatePIN = false;
        Gson gson = new Gson();
        VerifyP7 verifyP7 = gson.fromJson(json, VerifyP7.class);
        if (verifyP7.getData() != null) {
            businessText = verifyP7.getData();
        }
        if (verifyP7.getResult() != null) {
            requestP7AsyncJson(verifyP7.getResult());
        }

    }

    /**
     * 移除loading
     */
    public void removeLoadDialog() {
        loadDialog.dismiss();
    }


    /**
     * 调用扫一扫
     */
    @JavascriptInterface
    public void openCameraQRcode() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 取得相机权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.CAMERA, Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_CAMERA_QRCODE);
            } else {
                // 调用扫一扫
                Intent intent = new Intent(mActivity, CaptureActivity.class);
                intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
                mActivity.startActivityForResult(intent, REQUEST_OPEN_QRCODE);
            }
        } else {
            // 权限已经取得的情况下调用
            Intent intent = new Intent(mActivity, CaptureActivity.class);
            intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
            mActivity.startActivityForResult(intent, REQUEST_OPEN_QRCODE);
        }
    }

    /**
     * 删除面部识别对比源
     */
    @JavascriptInterface
    public void delPhoto() {
        isDelFace = true;
        faceReg();
        LogUtils.e(TAG, "我进来了delPhoto");
    }

    @JavascriptInterface
    public void openPhotoAlbum() {
        if (ContextCompat.checkSelfPermission(mActivity, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(mActivity, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                mActivity.requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE, Manifest.permission.READ_EXTERNAL_STORAGE}, OPEN_PHOTO_ALBUM);
            } else {
                // 打开相册
                Matisse.from(mActivity)
                        .choose(MimeType.ofImage())
                        .countable(true)
                        .maxSelectable(9)
                        .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                        .thumbnailScale(0.85f)
                        .theme(R.style.Matisse_Zhihu)
                        .imageEngine(new Glide4Engine())
                        .forResult(OPEN_PHOTO_ALBUM);
            }
        } else {
            // 打开相册
            Matisse.from(mActivity)
                    .choose(MimeType.ofImage())
                    .countable(true)
                    .maxSelectable(9)
                    .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                    .thumbnailScale(0.85f)
                    .theme(R.style.Matisse_Zhihu)
                    .imageEngine(new Glide4Engine())
                    .forResult(OPEN_PHOTO_ALBUM);
        }

    }

    public void openPhotoAlbumResult(List<String> mList) {
//        photoList.clear();
//        photoList.addAll(mList);
        loadDialog = new SweetAlertDialog(mActivity, PROGRESS_TYPE);
        loadDialog.setContentText("请稍候...").show();
        List<String> baseList = new ArrayList<>();
//        for (int i = 0; i < mList.size(); i++) {
//            baseList.add(FileUtil.bitmapToJpegBase64(BitmapFactory.decodeFile(mList.get(i)),100));
//        }
//        Gson g = new Gson();
//        String json = g.toJson(baseList);
//        loadDialog.dismiss();
//        url = "javascript:getPhotoAlbum('" + json + "')";
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
//            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
//                // nothing
//            }));
//        } else {
//            mWebView.post(() -> mWebView.loadUrl(url));
//        }

        List<ImageConfig> imgList = new ArrayList<>();
        for (int i = 0; i < mList.size(); i++) {
            imgList.add(ImageConfig.getDefaultConfig(mList.get(i)));
        }

        CompressImageTask.get().compressImages(mActivity,imgList , new CompressImageTask.OnImagesResult() {
            @Override
            public void startCompress() {
                //压缩前可以选择加载LoadingView
            }
            @Override
            public void resultFilesSucceed(List<File> fileList) {
                for (int i = 0; i < fileList.size(); i++) {
                    try {
//                        baseList.add(FileUtil.encodeBase64File(fileList.get(i)));
                        baseList.add(FileUtil.bitmapToJpegBase64(BitmapFactory.decodeFile(fileList.get(i).getPath()),100));
                    } catch (Exception e) {
                        e.printStackTrace();
                        loadDialog.dismiss();
                        showToast("图片加密失败！",1);
                    }
                    LogUtils.e(TAG,"当前压缩文件的大小"+fileList.get(i).length()/1024+"kb");
                }
                Gson g = new Gson();
                String json = g.toJson(baseList);
                loadDialog.dismiss();
                url = "javascript:getPhotoAlbum('" + json + "')";
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                    mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                        // nothing
                    }));
                } else {
                    mWebView.post(() -> mWebView.loadUrl(url));
                }
            }

            @Override
            public void resultFilesError() {
                //失败回调
                loadDialog.dismiss();
                showToast("图片压缩失败！",1);
            }
        });
    }

    /**
     * 加载Pdf文件
     */
    @JavascriptInterface
    public void readPdf(String pdfUrl) {
        LogUtils.e(TAG, pdfUrl);
        mWebView.post(() -> mWebView.loadUrl("file:///android_asset/pdfjs/web/viewer.html?file=" + pdfUrl));
    }

}
