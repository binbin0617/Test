package com.bin.mylibrary.aty;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.ClipData;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.net.Uri;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.FloatingActionButton;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.KeyEvent;
import android.view.OrientationEventListener;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.*;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.ocr.sdk.model.BankCardResult;
import com.bin.mylibrary.R;
import com.bin.mylibrary.base.BaseWebAty;
import com.bin.mylibrary.cossphoto.CompressImageTask;
import com.bin.mylibrary.cossphoto.ImageConfig;
import com.bin.mylibrary.entity.MessageEvent;
import com.bin.mylibrary.entity.PhotoQRCode;
import com.bin.mylibrary.interfaces.JavascriptInterfaceImpl;
import com.bin.mylibrary.utils.AndroidBug5497Workaround;
import com.bin.mylibrary.utils.AnimationUtil;
import com.bin.mylibrary.utils.FileUtil;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.utils.MagnifierView;
import com.bin.mylibrary.utils.RecognizeService;
import com.bin.mylibrary.utils.Utils;
import com.bin.mylibrary.view.CoolIndicatorLayout;
import com.bin.mylibrary.view.FloatingDraftButton;
import com.bin.mylibrary.view.SmartRefreshWebLayout;
import com.google.gson.Gson;
import com.just.agentweb.BaseIndicatorView;
import com.just.agentweb.IWebLayout;
import com.just.agentweb.WebViewClient;
import com.leo.libqrcode.decode.ZbarDecodeUtil;
import com.zhihu.matisse.Matisse;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;



/**
 * App主界面
 * Created by zhaolei on 2019/05/30.
 */

public class MainAty extends BaseWebAty implements SensorEventListener {
    public ImageView img;
    private RelativeLayout rl_main;
    /**
     * 日志输出标志
     **/
    protected final String TAG = this.getClass().getSimpleName();
    public WebView mWebView;
    // js与webview交互接口
    private JavascriptInterfaceImpl javascriptInterface;
    public RelativeLayout main_layout;
    private ValueCallback<Uri> uploadFile;
    private ValueCallback<Uri[]> uploadFiles;
    //判断是否能返回
    public static boolean isBack = true;

    private FloatingDraftButton floatingDraftButton;

    private FloatingActionButton fab_refresh;
    private FloatingActionButton fab_back;
    private FloatingActionButton fab_speak;
    private FloatingActionButton fab_exit;

    private FloatingActionButton fab_fdj;

    private SweetAlertDialog loadDialog;
    //传感器服务
    private SensorManager mSensorManager;
    private CameraOrientationListener orientationListener;
    /**
     * 当前屏幕旋转角度
     */
    private int mOrientation = 0;
    /**
     * 当前屏幕手持角度
     */
    private int mHandheldAngle = 0;

    private Uri uri;//照片uri
    private Uri uri2;//拍照识别二维码照片uri
    /**
     * 判断放大镜的显示隐藏
     */
    private boolean isShowMagnifier = true;

    /**
     * 放大镜的类
     */
    private MagnifierView mv;
    /**
     * 二维码扫描保存的图片
     */
    private String base64PhotoQRCode;

    public File saveCameraSavePath1;
    public File saveCameraSavePath2;

    private Uri imageUri;

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putSerializable("saveCameraSavePath1", saveCameraSavePath1);
        outState.putSerializable("saveCameraSavePath2", saveCameraSavePath2);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        if (null != savedInstanceState) {
            saveCameraSavePath1 = (File) savedInstanceState.getSerializable("saveCameraSavePath1");
            saveCameraSavePath2 = (File) savedInstanceState.getSerializable("saveCameraSavePath2");
        }
        super.onCreate(savedInstanceState);
        EventBus.getDefault().register(this);
        loadDialog = new SweetAlertDialog(MainAty.this, SweetAlertDialog.PROGRESS_TYPE);
        loadDialog.setTitleText("请稍后...")
                .show();
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);//恢复原有的样式
        setContentView(R.layout.main_layout);
        AndroidBug5497Workaround.assistActivity(this);
        initView();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED
                ||ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_SETTINGS) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO,
                        Manifest.permission.WRITE_SETTINGS}, 00000000);
            }
        } else {
//            if (DeviceUtils.isEMUI()) {
//                //初始化华为推送
//                HMSAgent.init(this);
//                HMSAgent.connect(this, new ConnectHandler() {
//                    @Override
//                    public void onConnect(int rst) {
//                        LogUtils.e("====connect======>>>", "" + rst);
//                    }
//                });
//                HMSAgent.Push.getToken(new GetTokenHandler() {
//                    @Override
//                    public void onResult(int rst) {
//                        LogUtils.e("====getToken=====>>>", "" + rst);
//                    }
//                });
//            }
//            if (DeviceUtils.isMIUI()) {
////            初始化小米push推送服务
//                if (shouldInit()) {
//                    MiPushClient.registerPush(this, APP_ID, APP_KEY);
//                }
//                LoggerInterface newLogger = new LoggerInterface() {
//                    @Override
//                    public void setTag(String tag) {
//                        // ignore
//                    }
//
//                    @Override
//                    public void log(String content, Throwable t) {
//                        Log.d(TAG, content, t);
//                    }
//
//                    @Override
//                    public void log(String content) {
//                        Log.d(TAG, content);
//                    }
//                };
//                Logger.setLogger(this, newLogger);
//            }
        }
//        String userStr = javascriptInterface.getUserinfo();
//        if (userStr != null) {
//            if (!userStr.equals("")) {
//                try {
//                    Gson gson = new Gson();
//                    SetTagsBean setTagsBean = gson.fromJson(userStr, SetTagsBean.class);
////                    LoginEvent lEvent = new LoginEvent("android", true);
////                    lEvent.addKeyValue("人员编号-学校ID", setTagsBean.getRybh() + "-" + setTagsBean.getSchoolid());
////                    JAnalyticsInterface.onEvent(getApplicationContext(), lEvent);
//
//                    Map<String, String> music = new HashMap<String, String>();
//                    music.put("rybh_school", setTagsBean.getRybh().trim() + "_" + setTagsBean.getSchoolid().trim());//自定义参数：音乐类型，值：流行
//                    MobclickAgent.onEvent(this, "rybh_schoolid", music);
//                    LogUtils.e(TAG,"Umeng统计成功");
//
//                } catch (Exception e) {
//                    e.printStackTrace();
//                }
//            }
//        }
    }

    public List getScreenDirection() {
        List mList = new ArrayList();
        mList.add(mOrientation);
        mList.add(mHandheldAngle);
        return mList;
    }


    private void initView() {
        floatingDraftButton = findViewById(R.id.floatingActionButton);
        fab_refresh = findViewById(R.id.fab_refresh);
        fab_back = findViewById(R.id.fab_back);
        fab_speak = findViewById(R.id.fab_speak);
        fab_exit = findViewById(R.id.fab_exit);
        fab_fdj = findViewById(R.id.fab_fdj);
        main_layout = findViewById(R.id.main_layout);
        img = findViewById(R.id.img);
        rl_main = findViewById(R.id.rl_main);
        floatingDraftButton.registerButton(fab_refresh);
        floatingDraftButton.registerButton(fab_back);
        floatingDraftButton.registerButton(fab_speak);
        floatingDraftButton.registerButton(fab_exit);
        floatingDraftButton.registerButton(fab_fdj);
        // 接口添加
        mWebView = mAgentWeb.getWebCreator().getWebView();
        javascriptInterface = new JavascriptInterfaceImpl(this, mWebView);
        javascriptInterface.getBaiduToken();
        mWebView.addJavascriptInterface(javascriptInterface, "Android");
        // 设置长按监听
        mWebView.setOnLongClickListener(onLongClickListener);
        // 允许debug TODO 上架时不允许debug
        mWebView.setWebContentsDebuggingEnabled(true);
        floatingDraftButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
            }
        });
        fab_refresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mSmartRefreshLayout != null) {
                    mSmartRefreshLayout.setEnableRefresh(true);
                    mSmartRefreshLayout.autoRefresh();
                    AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
                }
            }
        });
//        fab_back.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                if (javascriptInterface != null) {
//                    javascriptInterface.finish();
//                    AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
//                }
//            }
//        });
        fab_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
                startActivity(new Intent(MainAty.this, SpeakTestAty.class));
            }
        });
//        fab_exit.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                javascriptInterface.delUserinfo();
//            }
//        });
        mv = new MagnifierView.Builder(MainAty.this)
                .intiLT(200, 500)
                .viewWH(500, 500)
                .scale(2f)
                .alpha(50)
                .color("#1296eb")
                .build();
//        fab_fdj.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
//                if (isShowMagnifier) {
//                    fab_fdj.setImageResource(R.mipmap.icon_fdj_red);
//                    mv.startViewToRoot();
//                    isShowMagnifier = false;
//                } else {
//                    fab_fdj.setImageResource(R.mipmap.icon_fdj_white);
//                    isShowMagnifier = true;
//                    mv.closeViewToRoot();
//                }
//            }
//        });
    }


    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        if (messageEvent.getUrl() != null) {
            mAgentWeb.getUrlLoader().loadUrl(messageEvent.getUrl());
        } else {
            mAgentWeb.getUrlLoader().loadUrl(baseUrl + "/messageNoticeDetail_uservue?id=" + messageEvent.getMsgId());
        }
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        if (mv != null) {
            fab_fdj.setImageResource(R.mipmap.icon_fdj_white);
            isShowMagnifier = true;
            mv.closeViewToRoot();
        }
        if (this.mAgentWeb != null) {
            mAgentWeb.destroy();
        }
        super.onDestroy();
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (!floatingDraftButton.isDraftable()) {
            AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
        }
    }

//    @Nullable
//    @Override
//    protected com.just.agentweb.WebChromeClient getWebChromeClient() {
//        return new com.just.agentweb.WebChromeClient() {
//            // For Android 3.0+
//            public void openFileChooser(ValueCallback uploadMsg, String acceptType) {
//                Log.i("test", "openFileChooser 1");
//                MainAty.this.uploadFile = uploadFile;
//                openFileChooseProcess();
//            }
//
//            // For Android < 3.0
//            public void openFileChooser(ValueCallback<Uri> uploadMsgs) {
//                Log.i("test", "openFileChooser 2");
//                MainAty.this.uploadFile = uploadFile;
//                openFileChooseProcess();
//            }
//
//            // For Android  > 4.1.1
//            public void openFileChooser(ValueCallback<Uri> uploadMsg, String acceptType, String capture) {
//                Log.i("test", "openFileChooser 3");
//                MainAty.this.uploadFile = uploadFile;
//                if (("image/*").equals(acceptType)) {
//                    takePhoto();
//                } else {
//                    openFileChooseProcess();
//                }
//            }
//
//            // For Android  >= 5.0
//            public boolean onShowFileChooser(WebView webView,
//                                             ValueCallback<Uri[]> filePathCallback,
//                                             WebChromeClient.FileChooserParams fileChooserParams) {
//                Log.i("test", "openFileChooser 4:" + filePathCallback.toString());
//                MainAty.this.uploadFiles = filePathCallback;
//                String[] acceptTypes = fileChooserParams.getAcceptTypes();
//                LogUtils.e(TAG, acceptTypes[0]);
//                if (("image/*").equals(acceptTypes[0])) {
//                    takePhoto();
//                } else {
//                    openFileChooseProcess();
//                }
//                return true;
//            }
//
//            /**
//             * 点击取消的回调
//             */
//            class ReOnCancelListener implements
//                    DialogInterface.OnCancelListener {
//
//                @Override
//                public void onCancel(DialogInterface dialogInterface) {
//                    if (uploadFile != null) {
//                        uploadFile.onReceiveValue(null);
//                        uploadFile = null;
//                    }
//                    if (uploadFiles != null) {
//                        uploadFiles.onReceiveValue(null);
//                        uploadFiles = null;
//                    }
//                }
//            }
//
//            /**
//             * 拍照或选择相册
//             */
//            private void takePhoto() {
//                AlertDialog.Builder alertDialog = new AlertDialog.Builder(MainAty.this);
//                alertDialog.setTitle("选择");
//                alertDialog.setOnCancelListener(new ReOnCancelListener());
//                alertDialog.setItems(new CharSequence[]{"相机", "相册"},
//                        new DialogInterface.OnClickListener() {
//                            @Override
//                            public void onClick(DialogInterface dialog, int which) {
//                                if (which == 0) {
//                                    File fileUri = new File(Environment.getExternalStorageDirectory().getPath() + "/" + SystemClock.currentThreadTimeMillis() + ".jpg");
//                                    imageUri = Uri.fromFile(fileUri);
//                                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
//                                        imageUri = FileProvider.getUriForFile(MainAty.this, "com.szht.htappfuture", fileUri);//通过FileProvider创建一个content类型的Uri
//                                    } else {
//                                        imageUri = Uri.fromFile(fileUri);
//                                    }
//                                    Intent intent = new Intent("android.media.action.IMAGE_CAPTURE");
//                                    intent.putExtra(MediaStore.EXTRA_OUTPUT, imageUri);
//                                    startActivityForResult(intent, 0);
//                                } else {
//                                    Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//                                    i.addCategory(Intent.CATEGORY_OPENABLE);
//                                    i.setType("image/*");
//                                    MainAty.this.startActivityForResult(Intent.createChooser(i, "File Browser"), 0);
//                                }
//                            }
//                        });
//                alertDialog.show();
//            }
//
//        };
//    }
//
//    private void openFileChooseProcess() {
//        Intent i = new Intent(Intent.ACTION_GET_CONTENT);
//        i.addCategory(Intent.CATEGORY_OPENABLE);
//        i.setType("*/*");
//        startActivityForResult(Intent.createChooser(i, "test"), 0);
//    }

    @Nullable
    @Override
    protected WebViewClient getWebViewClient() {
        return new WebViewClient() {

            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
                fab_fdj.setImageResource(R.mipmap.icon_fdj_white);
                mv.closeViewToRoot();
                isShowMagnifier = true;
//                if (NetStateUtils.isNetworkConnected(MainAty.this)) {
//                    HttpRequest.POST(MainAty.this, url, new Parameter(), new ResponseListener() {
//                        @Override
//                        public void onResponse(String response, Exception error) {
//                            if ("".equals(response)) {
////                                javascriptInterface.delUserinfo();
//                                view.loadUrl(baseUrl);
//                            }
//                        }
//                    });
//                }
                super.onPageStarted(view, url, favicon);
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                if (loadDialog != null) {
                    loadDialog.dismiss();
                }
                super.onPageFinished(view, url);
            }

            @Override
            public void onLoadResource(WebView view, String url) {
                if ((baseUrl + "/workComponent_uservue").equals(view.getUrl())
                        || (baseUrl + "/login_uservue").equals(view.getUrl())) {
                    isBack = false;
                } else {
                    isBack = true;
                }
                super.onLoadResource(view, url);
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
//                super.onReceivedSslError(view, handler, error);
                //handler.cancel();// super中默认的处理方式，WebView变成空白页
                if (handler != null) {
                    handler.proceed();//忽略证书的错误继续加载页面内容，不会变成空白页面
                }
            }

        };
    }

    /**
     * 重写webview 的长按响应、实现图片长按查看
     **/
    private OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
//            final WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
//            // 如果是图片类型或者是带有图片链接的类型
//            if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
//                    hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {
//
//                // 弹出保存图片的对话框
//                SweetAlertDialog s = new SweetAlertDialog(MainAty.this, SweetAlertDialog.NORMAL_TYPE);
//                s.setContentText("查看图片")
//                        .setCancelText("取消")
//                        .setConfirmText("确定")
//                        .showCancelButton(true)
//                        .setTitleText("提示")
//                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                imgClick(hitTestResult);
//                                sweetAlertDialog.dismiss();
//                            }
//                        })
//                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
//                            @Override
//                            public void onClick(SweetAlertDialog sweetAlertDialog) {
//                                sweetAlertDialog.dismiss();
//                            }
//                        }).show();
//                return true;
//            }
            return false;
        }
    };

//    /**
//     * 图片的点击事件
//     */
//
//    public void imgClick(WebView.HitTestResult hitTestResult) {
//        String imgurl = hitTestResult.getExtra();
//        BaseApplication.setOnLongClickPhotoBase64(imgurl);
//        startActivity(new Intent(MainAty.this, ImageViewActivity.class));
//    }

    /**
     * 重写申请权限操作返回值的方法
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PHOTO_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    javascriptInterface.scanQrcode();
                } else {
                    Toast.makeText(this, "无相机调用权限，扫一扫功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case HEADCAMERA_REQUEST:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请成功,调用前摄像头拍照扫一扫返回成功
                    javascriptInterface.getPhoto();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case OPEN_FILE_QRCODE:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请成功,调用选择文件
                    javascriptInterface.openFile();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case GAIN_RESULT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请成功,调用前摄像头拍照
                    javascriptInterface.removeLoadDialog();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case OPEN_PHOTO_ALBUM:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    javascriptInterface.openPhotoAlbum();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case PHOTO_REQUEST_YHK:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    javascriptInterface.toYhkCamera();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case REQUEST_OPEN_DOCUMENT:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    javascriptInterface.openDocument();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            case OPEN_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    javascriptInterface.openCamera();
                } else {
                    Toast.makeText(this, "权限不足，功能无法使用，", Toast.LENGTH_SHORT).show();
                }
                break;
            default:
        }
    }

    @Override
    protected void buildAgentWeb() {
        webType = 1;
        super.buildAgentWeb();
    }

    @Nullable
    @Override
    protected String getUrl() {
        SharedPreferences dataInfo = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        String userStr = dataInfo.getString("data", "");
        com.alibaba.fastjson.JSONObject user = null;
        if (!"".equals(userStr.trim())) {
            // 用户信息不能为空
            user = JSON.parseObject(userStr);
            BaseWebAty.baseUrl = user.getString("loginUrl") + "/htAppWeb2";
        } else {
            BaseWebAty.baseUrl = "http://114.115.204.49/htAppWeb2";
//            BaseWebAty.baseUrl = "http://192.168.100.156/htAppWeb2";

        }
        return baseUrl;
    }

    @NonNull
    @Override
    protected ViewGroup getAgentWebParent() {
        return (ViewGroup) this.findViewById(R.id.main_layout);
    }

    @NonNull
    @Override
    protected BaseIndicatorView getIndicatorView() {
        return new CoolIndicatorLayout(this);
    }

    @Nullable
    @Override
    protected IWebLayout getWebLayout() {
        return this.mSmartRefreshWebLayout = new SmartRefreshWebLayout(this);
    }

    private void onActivityResultAboveL(int requestCode, int resultCode, Intent data) {
        if (requestCode != 0 || uploadFiles == null) {
            return;
        }
        Uri[] results = null;
        if (resultCode == Activity.RESULT_OK) {
            if (data == null) {
                results = new Uri[]{imageUri};
            } else {
                String dataString = data.getDataString();
                ClipData clipData = data.getClipData();
                if (clipData != null) {
                    results = new Uri[clipData.getItemCount()];
                    for (int i = 0; i < clipData.getItemCount(); i++) {
                        ClipData.Item item = clipData.getItemAt(i);
                        results[i] = item.getUri();
                    }
                }
                if (dataString != null)
                    results = new Uri[]{Uri.parse(dataString)};
            }
        }
        uploadFiles.onReceiveValue(results);
        uploadFiles = null;
    }

    /**
     * 重写取得活动返回值的方法
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        if (resultCode == RESULT_OK) {
            switch (requestCode) {
                case 0:
                    if (null == uploadFile && null == uploadFiles) return;
                    Uri result = intent == null || resultCode != RESULT_OK ? null : intent.getData();
                    if (uploadFiles != null) {
                        onActivityResultAboveL(requestCode, resultCode, intent);
                    } else if (uploadFile != null) {
                        uploadFile.onReceiveValue(result);
                        uploadFile = null;
                    }
                    break;
                //设置完PIN码的回调
                case REQUEST_PIN:
                    Log.e("MainAty", "设置完PIN码的回调 -->" + "getPassword()");
                    javascriptInterface.getPassword();
                    break;
                //验证完PIN码的回调
                case REQUEST_CHECK_PIN:
                    Log.e("MainAty", "验证完PIN码的回调 -->" + "getPassword()");
                    String password = intent.getStringExtra("password");
                    String random = intent.getStringExtra("random");
                    javascriptInterface.setSignMethod(password, random);
                    break;
                // 从WebviewActivity返回
                case 1:
                    // 需要继续跳转业务画面 TODO
                    String ywlx = intent.getStringExtra("ywlx");
                    Log.d(TAG, ywlx);
                    break;
                // 二维码扫码返回值
                case REQUEST_OPEN_QRCODE:
                    // 取得扫码结果messageEvent
                    String content = intent.getStringExtra("sn");
                    LogUtils.e(TAG, "扫一扫返回成功！扫码结果为：" + content);
                    // 结果返回页面
                    mWebView.post(() -> {
                        mWebView.loadUrl("javascript:setOpenCameraQRcode('" + content + "')");
                    });
                    break;
                case REQUEST_CODE_HEADCAMERA:
                    // 面部识别用对照原照片返回
                    // 取得返回结果
                    String faceResult = intent.getStringExtra("result");
                    // 刷新页面对比原照片
                    javascriptInterface.getMotoHeadPhoto(faceResult);
                    break;
                case REQUEST_CODE_FACEREG:
                    // 活体识别并人脸对比结果返回
                    // 取得返回结果
                    String result2 = intent.getStringExtra("result");
                    if ("deleteSuccess".equals(result2)) {
                        javascriptInterface.deletePhoto();
                    }
                    // 返回结果返到页面上
                    javascriptInterface.doReg(result2);
                    break;
                // 选择拍照回调
                case OPEN_CAMERA_REQUEST:
                    if (saveCameraSavePath1 == null) {
                        return;
                    }
                    if (saveCameraSavePath1.equals("")) {
                        return;
                    }
                    String photoPath = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        photoPath = String.valueOf(saveCameraSavePath1);
                    } else {
                        uri = Uri.fromFile(saveCameraSavePath1);
                        photoPath = uri.getEncodedPath();
                    }
                    List mList = new ArrayList();
                    mList.add(photoPath);
                    javascriptInterface.openPhotoAlbumResult(mList);
                    break;
                //选择图片识别二维码回调
                case OPEN_CAMERA_QRREQUEST:
//                Uri sourceUri = intent.getData();
                    if (saveCameraSavePath2 == null) {
                        return;
                    }
                    if (saveCameraSavePath2.equals("")) {
                        return;
                    }
                    String photoPath2 = "";
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
                        photoPath2 = String.valueOf(saveCameraSavePath2);
                    } else {
                        uri2 = Uri.fromFile(saveCameraSavePath2);
                        photoPath2 = uri2.getEncodedPath();
                    }
                    List<ImageConfig> imgList = new ArrayList<>();
                    imgList.clear();
                    imgList.add(ImageConfig.getDefaultConfig(photoPath2));
                    CompressImageTask.get().compressImages(MainAty.this, imgList, new CompressImageTask.OnImagesResult() {
                        @Override
                        public void startCompress() {
                        }

                        @Override
                        public void resultFilesSucceed(List<File> fileList) {
                            String path = fileList.get(0).getPath();
                            base64PhotoQRCode = FileUtil.bitmapToJpegBase64(BitmapFactory.decodeFile(path), 100);
                            String decode = ZbarDecodeUtil.decode(path);
                            handleQrCode(decode);
                        }

                        @Override
                        public void resultFilesError() {
                        }
                    });
                    break;
                //选择文件回调
                case OPEN_DOCUMENT:
                    if ("success".equals(intent.getStringExtra("success"))) {
                        javascriptInterface.openDocumentResult();
                    }
                    break;
                //打开银行卡识别回掉
                case REQUEST_CODE_BANKCARD:
                    RecognizeService.recBankCard(this, Utils.getSaveFile(getApplicationContext()).getAbsolutePath(), new RecognizeService.ServiceBankCardListener() {
                        @Override
                        public void onResult(BankCardResult result) {
                            showToastLong(result.toString());
                            javascriptInterface.openCameraToBankCardResult(result);
                        }
                    });
                    break;
                // 多选相册回调
                case OPEN_PHOTO_ALBUM:
                    javascriptInterface.openPhotoAlbumResult(Matisse.obtainPathResult(intent));
                    break;
                default:
                    break;
            }
        } else if (resultCode == RESULT_CANCELED) {
            LogUtils.e(TAG, "选择照片返回" + uploadFile + uploadFiles);
            if (null != uploadFile) {
                uploadFile.onReceiveValue(null);
                uploadFile = null;
            }
            if (null != uploadFiles) {
                uploadFiles.onReceiveValue(null);
                uploadFiles = null;
            }

        }
    }


    /**
     * 处理图片二维码解析的数据
     *
     * @param result
     */
    public void handleQrCode(String result) {
        String url;
        String json = "";
        PhotoQRCode photoQRCode = new PhotoQRCode();
        photoQRCode.setPhoto(base64PhotoQRCode);
        PhotoQRCode.Qrcode qrcode = new PhotoQRCode.Qrcode();
        qrcode.setQrcode(result);
        List<PhotoQRCode.Qrcode> list = new ArrayList<>();
        list.add(qrcode);
        photoQRCode.setQrcodeArray(list);
        Gson g = new Gson();
        json = g.toJson(photoQRCode);
        url = "javascript:setPAndQResult('" + json + "')";
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.post(() -> mWebView.evaluateJavascript(url, s -> {
                // nothing
            }));
        } else {
            mWebView.post(() -> mWebView.loadUrl(url));
        }
    }

    /**
     * 重写返回事件
     **/
    private long startTime = 0;//创建一个初始数，为了记录点击次数

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK) {
//            isPdf = false;
            if (isBack) {
                if (mAgentWeb.handleKeyEvent(keyCode, event)) {
                    return true;
                }
            } else {
                if ((System.currentTimeMillis() - startTime) > 2000) {
                    Toast.makeText(getApplicationContext(), "再按一次后退键退出程序", Toast.LENGTH_SHORT).show();
                    startTime = System.currentTimeMillis();
                } else {
                    SweetAlertDialog sweetAlertDialog = new SweetAlertDialog(this, SweetAlertDialog.NORMAL_TYPE);
                    sweetAlertDialog
                            .setTitleText("提示")
                            .setCancelText("取消")
                            .setConfirmText("确定")
                            .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                                @Override
                                public void onClick(SweetAlertDialog sweetAlertDialog) {
                                    finish();
                                }
                            })
                            .setContentText("您是否退出程序")
                            .show();
                }
                return false;
            }
        }
        return super.onKeyDown(keyCode, event);
    }


    /**
     * 返回值设置
     **/
    private void setReturnData() {
        Intent intent = new Intent();
        intent.putExtra("ywlx", "CX");
        setResult(RESULT_OK, intent);
    }

    /**
     * 重写再次启动方法
     **/
    @Override
    protected void onRestart() {
        super.onRestart();
    }

    public String getPath(final Context context, final Uri uri) {

        final boolean isKitKat = Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT;

        // DocumentProvider
        if (isKitKat && DocumentsContract.isDocumentUri(context, uri)) {
            // ExternalStorageProvider
            if (isExternalStorageDocument(uri)) {
                final String docId = DocumentsContract.getDocumentId(uri);
//                Log.i(TAG,"isExternalStorageDocument***"+uri.toString());
//                Log.i(TAG,"docId***"+docId);
//                以下是打印示例：
//                isExternalStorageDocument***content://com.android.externalstorage.documents/document/primary%3ATset%2FROC2018421103253.wav
//                docId***primary:Test/ROC2018421103253.wav
                final String[] split = docId.split(":");
                final String type = split[0];

                if ("primary".equalsIgnoreCase(type)) {
                    return Environment.getExternalStorageDirectory() + "/" + split[1];
                }
            }
            // DownloadsProvider
            else if (isDownloadsDocument(uri)) {
//                Log.i(TAG,"isDownloadsDocument***"+uri.toString());
                final String id = DocumentsContract.getDocumentId(uri);
                final Uri contentUri = ContentUris.withAppendedId(
                        Uri.parse("content://downloads/public_downloads"), Long.valueOf(id));

                return getDataColumn(context, contentUri, null, null);
            }
            // MediaProvider
            else if (isMediaDocument(uri)) {
//                Log.i(TAG,"isMediaDocument***"+uri.toString());
                final String docId = DocumentsContract.getDocumentId(uri);
                final String[] split = docId.split(":");
                final String type = split[0];

                Uri contentUri = null;
                if ("image".equals(type)) {
                    contentUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
                } else if ("video".equals(type)) {
                    contentUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
                } else if ("audio".equals(type)) {
                    contentUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
                }

                final String selection = "_id=?";
                final String[] selectionArgs = new String[]{split[1]};

                return getDataColumn(context, contentUri, selection, selectionArgs);
            }
        }
        // MediaStore (and general)
        else if ("content".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"content***"+uri.toString());
            return getDataColumn(context, uri, null, null);
        }
        // File
        else if ("file".equalsIgnoreCase(uri.getScheme())) {
//            Log.i(TAG,"file***"+uri.toString());
            return uri.getPath();
        }
        return null;
    }

    /**
     * Get the value of the data column for this Uri. This is useful for
     * MediaStore Uris, and other file-based ContentProviders.
     *
     * @param context       The context.
     * @param uri           The Uri to query.
     * @param selection     (Optional) Filter used in the query.
     * @param selectionArgs (Optional) Selection arguments used in the query.
     * @return The value of the _data column, which is typically a file path.
     */
    public String getDataColumn(Context context, Uri uri, String selection,
                                String[] selectionArgs) {

        Cursor cursor = null;
        final String column = "_data";
        final String[] projection = {column};

        try {
            cursor = context.getContentResolver().query(uri, projection, selection, selectionArgs,
                    null);
            if (cursor != null && cursor.moveToFirst()) {
                final int column_index = cursor.getColumnIndexOrThrow(column);
                return cursor.getString(column_index);
            }
        } finally {
            if (cursor != null)
                cursor.close();
        }
        return null;
    }


    public boolean isExternalStorageDocument(Uri uri) {
        return "com.android.externalstorage.documents".equals(uri.getAuthority());
    }

    public boolean isDownloadsDocument(Uri uri) {
        return "com.android.providers.downloads.documents".equals(uri.getAuthority());
    }

    public boolean isMediaDocument(Uri uri) {
        return "com.android.providers.media.documents".equals(uri.getAuthority());
    }

    @Override
    public void onSensorChanged(SensorEvent event) {

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    /**
     * 当方向改变时，将调用侦听器onOrientationChanged(int)
     */
    private class CameraOrientationListener extends OrientationEventListener {

        private int mCurrentNormalizedOrientation;
        private int mRememberedNormalOrientation;

        public CameraOrientationListener(Context context) {
            super(context, SensorManager.SENSOR_DELAY_NORMAL);
        }

        @Override
        public void onOrientationChanged(final int orientation) {
            Log.i(TAG, "当前屏幕手持角度:" + orientation + "°");
            if (orientation != ORIENTATION_UNKNOWN) {
                mCurrentNormalizedOrientation = normalize(orientation);
            }

            runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mHandheldAngle = mCurrentNormalizedOrientation;
                }
            });
        }

        private int normalize(int degrees) {
            if (degrees > 315 || degrees <= 45) {
                return 0;
            }

            if (degrees > 45 && degrees <= 135) {
                return 90;
            }

            if (degrees > 135 && degrees <= 225) {
                return 180;
            }

            if (degrees > 225 && degrees <= 315) {
                return 270;
            }

            throw new RuntimeException("The physics as we know them are no more. Watch out for anomalies.");
        }

        /**
         * 记录方向
         */
        public void rememberOrientation() {
            mRememberedNormalOrientation = mCurrentNormalizedOrientation;
        }

        /**
         * 获取当前方向
         *
         * @return
         */
        public int getRememberedNormalOrientation() {
            return mRememberedNormalOrientation;
        }

    }

    private boolean shouldInit() {
        ActivityManager am = ((ActivityManager) getSystemService(Context.ACTIVITY_SERVICE));
        List<ActivityManager.RunningAppProcessInfo> processInfos = am.getRunningAppProcesses();
        String mainProcessName = getApplicationInfo().processName;
        int myPid = android.os.Process.myPid();
        for (ActivityManager.RunningAppProcessInfo info : processInfos) {
            if (info.pid == myPid && mainProcessName.equals(info.processName)) {
                return true;
            }
        }
        return false;
    }


    /**
     * 指纹需要的方法  回调
     */
    //指纹识别成功
    public void setDoBiometricsResult() {
        javascriptInterface.setDoBiometricsResult();
    }

    //点击其他按钮
    public void otherTxtMethord() {
        javascriptInterface.otherTxtMethord();
    }

}