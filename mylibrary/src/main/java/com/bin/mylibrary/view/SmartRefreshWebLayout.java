package com.bin.mylibrary.view;

import android.app.Activity;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.bin.mylibrary.R;
import com.just.agentweb.IWebLayout;
import com.scwang.smartrefresh.layout.SmartRefreshLayout;

public class SmartRefreshWebLayout implements IWebLayout {

    private SmartRefreshLayout mSmartRefreshWebLayout;
    private WebView mWebView;

    public SmartRefreshWebLayout(Activity activity) {
        View mView = View.inflate(activity, R.layout.fragment_srl_web, null);
        View smartRefreshView = mView.findViewById(R.id.smarkLayout);
        mSmartRefreshWebLayout = (SmartRefreshLayout) smartRefreshView;
        mWebView = mSmartRefreshWebLayout.findViewById(R.id.webView);
    }


    @NonNull
    @Override
    public ViewGroup getLayout() {
        return mSmartRefreshWebLayout;
    }

    @Nullable
    @Override
    public WebView getWebView() {
        return mWebView;
    }


}
