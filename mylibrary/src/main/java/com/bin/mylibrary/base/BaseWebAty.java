package com.bin.mylibrary.base;

import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.Build;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;


import androidx.annotation.ColorInt;
import androidx.annotation.LayoutRes;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.bin.mylibrary.R;
import com.bin.mylibrary.aty.MainAty;
import com.bin.mylibrary.interfaces.JavascriptInterfaceImpl;
import com.bin.mylibrary.interfaces.PermissionListener;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.view.ErrorLayoutEntity;
import com.bin.mylibrary.view.SmartRefreshWebLayout;
import com.just.agentweb.AgentWeb;
import com.just.agentweb.AgentWebUIControllerImplBase;
import com.just.agentweb.BaseIndicatorView;
import com.just.agentweb.DefaultWebClient;
import com.just.agentweb.IWebLayout;
import com.just.agentweb.PermissionInterceptor;
import com.just.agentweb.WebChromeClient;
import com.just.agentweb.WebViewClient;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;
import com.scwang.smartrefresh.layout.api.RefreshLayout;
import com.scwang.smartrefresh.layout.listener.OnRefreshListener;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseWebAty extends AppCompatActivity {
    protected AgentWeb mAgentWeb;
    protected WebView mWebView;
    private JavascriptInterfaceImpl mJavascriptInterfaceImpl;
    public static final String TAG = MainAty.class.getSimpleName();
    private ErrorLayoutEntity mErrorLayoutEntity;
    protected int webType;  // 0 正常  1 下拉刷新  2  可以上下滑动
    protected SmartRefreshWebLayout mSmartRefreshWebLayout = null;
    //    private TextView tv_click;
    private PermissionListener mlistener;
    protected SmartRefreshLayout mSmartRefreshLayout;

    /**
     * 身份证识别用type
     **/
    private static String type = null;

    public static String getType() {
        return type;
    }

    public static void setType(String type) {
        BaseWebAty.type = type;
    }


    /**
     * 首页访问url
     **/
    public static String baseUrl = "http://114.115.204.49/htAppWeb2";
    public static String baseUrlStatic = "http://114.115.204.49/htAppWeb2";

    /**
     * 磊本地地址
     */
//    public static String baseUrl = "http://192.168.100.156/htAppWeb2";


    @Override
    public void setContentView(@LayoutRes int layoutResID) {
        super.setContentView(layoutResID);
        buildAgentWeb();
    }


    @Override
    public void setContentView(View view) {
        super.setContentView(view);
        buildAgentWeb();
    }

    @SuppressLint("JavascriptInterface")
    protected void buildAgentWeb() {
        if (getAgentWebParent() != null) {
            LogUtils.e("Main2", "我进来了");
            setWebView();
            setWebViewType();
        }
    }

    /**
     * 判断当前webView是哪种类型的
     * 0 正常  1 下拉刷新  2  可以上下滑动
     */
    public void setWebViewType() {
        // 允许debug
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            mWebView.setWebContentsDebuggingEnabled(true);
        }
        if (webType == 0) {

        } else if (webType == 1) {
            mSmartRefreshLayout = (SmartRefreshLayout) this.mSmartRefreshWebLayout.getLayout();
            final WebView mWebView = this.mSmartRefreshWebLayout.getWebView();
            mSmartRefreshLayout.setEnableRefresh(false);
            mSmartRefreshLayout.setOnRefreshListener(new OnRefreshListener() {
                @Override
                public void onRefresh(RefreshLayout refreshlayout) {
                    mAgentWeb.getUrlLoader().reload();
                    mSmartRefreshLayout.postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            mSmartRefreshLayout.setEnableRefresh(false);
                            mSmartRefreshLayout.finishRefresh();
                        }
                    }, 1000);
                }
            });
//            mSmartRefreshLayout.autoRefresh();
        } else if (webType == 2) {
            addBGChild((FrameLayout) mAgentWeb.getWebCreator().getWebParentLayout());
        }
    }

    public static void init(String url){
        baseUrlStatic = url;
    }

    /**
     * 设置WebView相关属性以及打开webView和js的交互
     */
    private void setWebView() {
        mErrorLayoutEntity = new ErrorLayoutEntity();
        mAgentWeb = AgentWeb.with(this)
                .setAgentWebParent(getAgentWebParent(), new ViewGroup.LayoutParams(-1, -1))
                .setCustomIndicator(getIndicatorView())
                .setWebChromeClient(getWebChromeClient())
                .setWebViewClient(getWebViewClient())
                .setWebView(getWebView())
                .setPermissionInterceptor(getPermissionInterceptor())
                .setWebLayout(getWebLayout())
                .setAgentWebUIController(getAgentWebUIController())
                    .interceptUnkownUrl()
                .setOpenOtherPageWays(getOpenOtherAppWay())
                .setMainFrameErrorView(mErrorLayoutEntity.layoutRes, mErrorLayoutEntity.reloadId)
                .setSecurityType(AgentWeb.SecurityType.STRICT_CHECK)
                .createAgentWeb()
                .ready()
                .go(baseUrlStatic);
//                .go(url);
        WebSettings ws = mAgentWeb.getWebCreator().getWebView().getSettings();
        ws.setUseWideViewPort(true);
        ws.setAllowFileAccess(true);
        ws.setAllowFileAccessFromFileURLs(true);
        ws.setAllowUniversalAccessFromFileURLs(true);
        ws.setLoadWithOverviewMode(true);
        // 接口添加
        mWebView = mAgentWeb.getWebCreator().getWebView();
        mJavascriptInterfaceImpl = new JavascriptInterfaceImpl(this, mWebView);
        mWebView.addJavascriptInterface(mJavascriptInterfaceImpl, "Android");
        mWebView.getSettings().setJavaScriptEnabled(true);
    }


    /**
     * 设置webView下层的界面
     *
     * @param frameLayout
     */
    protected void addBGChild(FrameLayout frameLayout) {
        TextView mTextView = new TextView(frameLayout.getContext());
        mTextView.setText("技术由 神州浩天 提供");
        mTextView.setTextSize(16);
        mTextView.setTextColor(getResources().getColor(R.color.text_wx));
        frameLayout.setBackgroundColor(getResources().getColor(R.color.back_wx));
        FrameLayout.LayoutParams mFlp = new FrameLayout.LayoutParams(-2, -2);
        mFlp.gravity = Gravity.CENTER_HORIZONTAL;
        final float scale = frameLayout.getContext().getResources().getDisplayMetrics().density;
        mFlp.topMargin = (int) (15 * scale + 0.5f);
        frameLayout.addView(mTextView, 0, mFlp);
    }

    /**
     * 权限申请
     *
     * @param permissions 待申请的权限集合
     * @param listener    申请结果监听事件
     */
    protected void requestRunTimePermission(String[] permissions, PermissionListener listener) {
        this.mlistener = listener;

        //用于存放为授权的权限
        List<String> permissionList = new ArrayList<>();
        //遍历传递过来的权限集合
        for (String permission : permissions) {
            //判断是否已经授权
            if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
                //未授权，则加入待授权的权限集合中
                permissionList.add(permission);
            }
        }

        //判断集合
        if (!permissionList.isEmpty()) {  //如果集合不为空，则需要去授权
            ActivityCompat.requestPermissions(this, permissionList.toArray(new String[permissionList.size()]), 1);
        } else {  //为空，则已经全部授权
            listener.onGranted();
        }
    }

    /**
     * 权限申请结果
     *
     * @param requestCode  请求码
     * @param permissions  所有的权限集合
     * @param grantResults 授权结果集合
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        switch (requestCode) {
            case 1:
                if (grantResults.length > 0) {
                    //被用户拒绝的权限集合
                    List<String> deniedPermissions = new ArrayList<>();
                    //用户通过的权限集合
                    List<String> grantedPermissions = new ArrayList<>();
                    for (int i = 0; i < grantResults.length; i++) {
                        //获取授权结果，这是一个int类型的值
                        int grantResult = grantResults[i];

                        if (grantResult != PackageManager.PERMISSION_GRANTED) { //用户拒绝授权的权限
                            String permission = permissions[i];
                            deniedPermissions.add(permission);
                        } else {  //用户同意的权限
                            String permission = permissions[i];
                            grantedPermissions.add(permission);
                        }
                    }
                    if (deniedPermissions.isEmpty()) {  //用户拒绝权限为空
                        mlistener.onGranted();
                    } else {  //不为空
                        //回调授权成功的接口
                        mlistener.onDenied(deniedPermissions);
                        //回调授权失败的接口
                        mlistener.onGranted(grantedPermissions);
                    }
                }
                break;
            default:
                break;
        }
    }


    /**
     * 跟随 Activity Or Fragment 生命周期 ， 释放 CPU 更省电 。
     */
    @Override
    protected void onPause() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onPause();
        }
        super.onPause();

    }

    @Override
    protected void onResume() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onResume();
        }

        super.onResume();
    }

    @Override
    protected void onDestroy() {
        if (mAgentWeb != null) {
            mAgentWeb.getWebLifeCycle().onDestroy();
        }
        super.onDestroy();
    }

    /**
     * 设置空布局
     *
     * @return
     */
    protected @NonNull
    ErrorLayoutEntity getErrorLayoutEntity() {
        if (this.mErrorLayoutEntity == null) {
            this.mErrorLayoutEntity = new ErrorLayoutEntity();
        }
        return mErrorLayoutEntity;
    }

    /**
     * 设置地址
     *
     * @return
     */
    protected
    @Nullable
    String getUrl() {
        return null;
    }

    /**
     * 设置AgentWeb布局界面
     *
     * @return
     */
    protected abstract @NonNull
    ViewGroup getAgentWebParent();

    /**
     * 设置自定义进度条
     *
     * @return
     */
    protected abstract @NonNull
    BaseIndicatorView getIndicatorView();

    /**
     * 设置进度条颜色
     *
     * @return
     */
    protected @ColorInt
    int getIndicatorColor() {
        return -1;
    }

    /**
     * 设置进度条宽度
     *
     * @return
     */
    protected int getIndicatorHeight() {
        return -1;
    }

    /**
     * 设置下拉刷新
     *
     * @return
     */
    protected @Nullable
    IWebLayout getWebLayout() {
        return null;
    }

    protected @Nullable
    WebViewClient getWebViewClient() {
        return null;
    }

    protected @Nullable
    WebChromeClient getWebChromeClient() {
        return null;
    }

    protected @Nullable
    WebView getWebView() {
        return null;
    }

    protected @Nullable
    PermissionInterceptor getPermissionInterceptor() {
        return null;
    }

    public @Nullable
    AgentWebUIControllerImplBase getAgentWebUIController() {
        return null;
    }

    public @Nullable
    DefaultWebClient.OpenOtherPageWays getOpenOtherAppWay() {
        return null;
    }

    public boolean getIsDefaultIndicator() {
        return false;
    }

    /**
     * [ 简化  Toast  短提示 ]
     *
     * @param msg
     */
    protected void showToastShort(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_SHORT).show();
    }

    /**
     * [ 简化  Toast  长提示 ]
     *
     * @param msg
     */
    protected void showToastLong(String msg) {
        Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
    }

}
