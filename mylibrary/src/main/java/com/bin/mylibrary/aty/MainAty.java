package com.bin.mylibrary.aty;

import android.Manifest;
import android.content.ContentUris;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.View.OnLongClickListener;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.alibaba.fastjson.JSON;
import com.bin.mylibrary.R;
import com.bin.mylibrary.activity.CaptureActivity;
import com.bin.mylibrary.base.BaseApplication;
import com.bin.mylibrary.base.BaseWebAty;
import com.bin.mylibrary.entity.MessageEvent;
import com.bin.mylibrary.interfaces.JavascriptInterfaceImpl;
import com.bin.mylibrary.utils.AndroidBug5497Workaround;
import com.bin.mylibrary.utils.AnimationUtil;
import com.bin.mylibrary.utils.Glide4Engine;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.view.CoolIndicatorLayout;
import com.bin.mylibrary.view.FloatingDraftButton;
import com.bin.mylibrary.view.SmartRefreshWebLayout;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.just.agentweb.AgentWebUIControllerImplBase;
import com.just.agentweb.BaseIndicatorView;
import com.just.agentweb.IWebLayout;
import com.just.agentweb.WebViewClient;
import com.zhihu.matisse.Matisse;
import com.zhihu.matisse.MimeType;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;
import org.greenrobot.eventbus.ThreadMode;

import java.io.File;

import cn.pedant.SweetAlert.SweetAlertDialog;


/**
 * App主界面
 * Created by zhaolei on 2019/05/30.
 */

public class MainAty extends BaseWebAty {
    /**
     * 日志输出标志
     **/
    protected final String TAG = this.getClass().getSimpleName();
    public WebView mWebView;
    // js与webview交互接口
    private JavascriptInterfaceImpl javascriptInterface;

    // 相机权限取得申请返回码
    private static final int PHOTO_REQUEST_CAMERA = 1;
    // 扫一扫返回码
    private static final int REQUEST_OPEN_QRCODE = 6;

    //获取到文件返回码
    private static final int OPEN_FILE_RESULT_QRCODE = 1009;

    // 文件权限取得申请返回码
    private static final int OPEN_FILE_QRCODE = 1008;

    //判断是否能返回
    public static boolean isBack = true;

//    private boolean isPdf = false;

    private FloatingDraftButton floatingDraftButton;

    private FloatingActionButton fab_refresh;
    private FloatingActionButton fab_back;
    private FloatingActionButton fab_speak;

    // 调用相机取得对照原相片返回结果
    private static final int REQUEST_CODE_HEADCAMERA = 1002;
    // 调用活体检测并人脸对比后返回结果
    private static final int REQUEST_CODE_FACEREG = 1003;
    // 调用相机取得对照原相片申请返回码
    private static final int HEADCAMERA_REQUEST = 1001;

    protected static final int OPEN_PHOTO_ALBUM = 1100;

    //获取唯一标识去的申请返回码
    private static final int GAIN_RESULT = 1111;

    private SweetAlertDialog loadDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        EventBus.getDefault().register(this);
        loadDialog = new SweetAlertDialog(MainAty.this, SweetAlertDialog.PROGRESS_TYPE);
        loadDialog.setContentText("请稍后...").show();
        setTheme(R.style.Theme_AppCompat_DayNight_NoActionBar);//恢复原有的样式
        setContentView(R.layout.main_layout);
        AndroidBug5497Workaround.assistActivity(this);
        initView();
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO) != PackageManager.PERMISSION_GRANTED) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                requestPermissions(new String[]{Manifest.permission.CAMERA,
                        Manifest.permission.READ_EXTERNAL_STORAGE,
                        Manifest.permission.READ_PHONE_STATE,
                        Manifest.permission.RECORD_AUDIO}, 00000000);
            }
        }

    }

    private void initView() {
        floatingDraftButton =findViewById(R.id.floatingActionButton);
        fab_refresh = findViewById(R.id.fab_refresh);
        fab_back = findViewById(R.id.fab_back);
        fab_speak = findViewById(R.id.fab_speak);
        floatingDraftButton.registerButton(fab_refresh);
        floatingDraftButton.registerButton(fab_back);
        floatingDraftButton.registerButton(fab_speak);
        // 接口添加
        mWebView = mAgentWeb.getWebCreator().getWebView();
        javascriptInterface = new JavascriptInterfaceImpl(this, mWebView);
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
        fab_back.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (javascriptInterface != null) {
                    javascriptInterface.finish();
                    AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
                }
            }
        });
        fab_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AnimationUtil.slideButtons(MainAty.this, floatingDraftButton);
                startActivity(new Intent(MainAty.this, SpeakTestAty.class));
            }
        });
    }

    @Subscribe(threadMode = ThreadMode.MAIN)
    public void Event(MessageEvent messageEvent) {
        mAgentWeb.getUrlLoader().loadUrl(baseUrl + "/messageNoticeDetail_uservue?id=" + messageEvent.getMsgId());
    }

    @Override
    protected void onDestroy() {
        if (EventBus.getDefault().isRegistered(this)) {
            EventBus.getDefault().unregister(this);
        }
        super.onDestroy();
    }

    public @Nullable
    AgentWebUIControllerImplBase getAgentWebUIController() {
        return new AgentWebUIControllerImplBase() {
            @Override
            public void onMainFrameError(WebView view, int errorCode, String description, String failingUrl) {
                LogUtils.e(TAG,"errorCode"+errorCode+"description"+description+"failingUrl"+failingUrl);
                if(errorCode==-2){
                    view.loadUrl("about:blank");
                    showToastLong("请打开网络!!!");
                }else{
                    javascriptInterface.delUserinfo();
                    view.loadUrl(baseUrl);
                }

            }

        };
//        return null;
    }

    @Nullable
    @Override
    protected WebViewClient getWebViewClient() {
        return new WebViewClient() {
            @Override
            public void onPageStarted(WebView view, String url, Bitmap favicon) {
//                HttpRequest.POST(MainAty.this, url, new Parameter(), new ResponseListener() {
//                    @Override
//                    public void onResponse(String response, Exception error) {
//                        if("".equals(response)){
//                            javascriptInterface.delUserinfo();
//                            view.loadUrl(baseUrl);
//                        }
//                    }
//                });
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

        };
    }

    /**
     * 重写webview 的长按响应、实现图片长按查看
     **/
    private OnLongClickListener onLongClickListener = new OnLongClickListener() {

        @Override
        public boolean onLongClick(View view) {
            final WebView.HitTestResult hitTestResult = mWebView.getHitTestResult();
            // 如果是图片类型或者是带有图片链接的类型
            if (hitTestResult.getType() == WebView.HitTestResult.IMAGE_TYPE ||
                    hitTestResult.getType() == WebView.HitTestResult.SRC_IMAGE_ANCHOR_TYPE) {

                // 弹出保存图片的对话框
                SweetAlertDialog s = new SweetAlertDialog(MainAty.this, SweetAlertDialog.NORMAL_TYPE);
                s.setContentText("查看图片")
                        .setCancelText("取消")
                        .setConfirmText("确定")
                        .showCancelButton(true)
                        .setTitleText("提示")
                        .setConfirmClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                imgClick(hitTestResult);
                                sweetAlertDialog.dismiss();
                            }
                        })
                        .setCancelClickListener(new SweetAlertDialog.OnSweetClickListener() {
                            @Override
                            public void onClick(SweetAlertDialog sweetAlertDialog) {
                                sweetAlertDialog.dismiss();
                            }
                        }).show();
                return true;
            }
            return false;
        }
    };

    /**
     * 图片的点击事件
     */

    public void imgClick(WebView.HitTestResult hitTestResult) {
//        取得图片base64
        String imgurl = hitTestResult.getExtra();
//        if (!imgurl.contains("base64")) {
//            LogUtils.e(TAG, "onLongClick: 不包含base64图片数据为:" + imgurl);
//            Log.e("imgurl-->", "" + imgurl);
            // 跳往图片展示界面
            // 传递图片地址
            BaseApplication.setOnLongClickPhotoBase64(imgurl);
            startActivity(new Intent(MainAty.this, ImageViewAty.class));
//        } else {
//            imgurl = imgurl.substring(imgurl.indexOf("base64,") + 7);
//            LogUtils.e(TAG, "onLongClick: 图片数据为:" + imgurl);
//            // 跳往图片展示界面
//            Intent intent = new Intent(MainAty.this, ImageViewAty.class);
//            // 保存图片数据
//            BaseApplication.setPicBase64(imgurl);
//            intent.putExtra("imgurl",imgurl);
//            startActivity(intent);
//        }
    }

    /**
     * 重写申请权限操作返回值的方法
     **/
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case PHOTO_REQUEST_CAMERA:
                if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    // 权限申请成功，扫一扫
                    Intent intent = new Intent(this, CaptureActivity.class);
                    intent.putExtra(CaptureActivity.KEY_INPUT_MODE, CaptureActivity.INPUT_MODE_QR);
                    startActivityForResult(intent, REQUEST_OPEN_QRCODE);
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
                    Matisse.from(MainAty.this)
                            .choose(MimeType.ofImage())
                            .countable(true)
                            .maxSelectable(9)
                            .restrictOrientation(ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED)
                            .thumbnailScale(0.85f)
                            .theme(R.style.Matisse_Zhihu)
                            .imageEngine(new Glide4Engine())
                            .forResult(OPEN_PHOTO_ALBUM);
                } else {
                    Toast.makeText(this, "无相机调用权限，扫一扫功能无法使用，", Toast.LENGTH_SHORT).show();
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


    /**
     * 重写取得活动返回值的方法
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            // 从WebviewActivity返回
            case 1:
                // 需要继续跳转业务画面 TODO
                if (resultCode == RESULT_OK) {
                    String ywlx = intent.getStringExtra("ywlx");
                    Log.d(TAG, ywlx);
                }
                break;
            // 二维码扫码返回值
            case REQUEST_OPEN_QRCODE:
                if (resultCode == RESULT_OK) {
                    // 取得扫码结果messageEvent
                    String content = intent.getStringExtra("sn");
                    LogUtils.e(TAG, "扫一扫返回成功！扫码结果为：" + content);
                    // 结果返回页面
                    mWebView.post(() -> {
                        mWebView.loadUrl("javascript:setOpenCameraQRcode('" + content + "')");
                    });
                }
                break;
            case REQUEST_CODE_HEADCAMERA:
                // 面部识别用对照原照片返回
                if (resultCode == RESULT_OK) {
                    // 取得返回结果
                    String result = intent.getStringExtra("result");
                    // 刷新页面对比原照片
                    javascriptInterface.getMotoHeadPhoto(result);
                }
                break;
            case REQUEST_CODE_FACEREG:
                // 活体识别并人脸对比结果返回
                if (resultCode == RESULT_OK) {
                    // 取得返回结果
                    String result = intent.getStringExtra("result");
                    if ("deleteSuccess".equals(result)) {
                        javascriptInterface.deletePhoto();
                    }
                    // 返回结果返到页面上
                    javascriptInterface.doReg(result);
                }
                break;
            // 多选相册回调
            case OPEN_PHOTO_ALBUM:
                if (resultCode == RESULT_OK) {
                    javascriptInterface.openPhotoAlbumResult(Matisse.obtainPathResult(intent));
                }
                break;
            //选择文件回调
            case OPEN_FILE_RESULT_QRCODE:
                if (resultCode == RESULT_OK) {
                    Uri uri = intent.getData();
                    if (uri != null) {
                        String path = getPath(this, uri);
                        if (path != null) {
                            File file = new File(path);
                            if (file.exists()) {
                                String upLoadFilePath = file.toString();
                                String upLoadFileName = file.getName();
                                javascriptInterface.openFileAlbumResult(upLoadFilePath, upLoadFileName);
                            }
                        }
                    }

                }
                break;
            default:
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
}

