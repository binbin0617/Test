/*
 * Copyright (C) 2017 Baidu, Inc. All Rights Reserved.
 */
package com.bin.mylibrary.faceReg;

import android.content.Context;

/**
 * Created by wangtianfei01 on 17/7/13.
 */

public class APIService {


    private static final String ACCESS_TOEKN_URL = "https://aip.baidubce.com/oauth/2.0/token?";

    private static final String FACE_COMPARE = "https://aip.baidubce.com/rest/2.0/face/v3/match";

    private String accessToken;

    private APIService() {

    }

    private static volatile APIService instance;

    public static APIService getInstance() {
        if (instance == null) {
            synchronized (APIService.class) {
                if (instance == null) {
                    instance = new APIService();
                }
            }
        }
        return instance;
    }

    public void init(Context context) {
        HttpUtil.getInstance().init();
    }

    /**
     * 设置accessToken 如何获取 accessToken 详情见:
     *
     * @param accessToken accessToken
     */
    public void setAccessToken(String accessToken) {
        this.accessToken = accessToken;
    }

    public String getAccessToken() {
        return accessToken;
    }

    /**
     * 明文aksk获取token
     *
     * @param listener
     * @param ak
     * @param sk
     */
    public void initAccessTokenWithAkSk(final OnResultListener<AccessToken> listener, String ak,
                                        String sk) {

        StringBuilder sb = new StringBuilder();
        sb.append("client_id=").append(ak);
        sb.append("&client_secret=").append(sk);
        sb.append("&grant_type=client_credentials");
        HttpUtil.getInstance().getAccessToken(listener, ACCESS_TOEKN_URL, sb.toString());

    }

     /**
     * URL append access token，sdkversion，aipdevid
     *
     * @param url
     * @return
     */
    private String urlAppendCommonParams(String url) {
        StringBuilder sb = new StringBuilder(url);
        sb.append("?access_token=").append(accessToken);

        return sb.toString();
    }

    /**
     * 人脸比较
     *
     * @param listener 回调
     * @param hp    人脸图片文件
     * @param data    人脸图片文件
     */
    public void faceCompare(OnResultListener listener, String hp, String data) {
        DynamicParams params = new DynamicParams();
        String jsonParams = MatchModel.getJson(hp, data);
        params.setJsonParams(jsonParams);
        //  WriteFile.writeString(jsonParams);
        RegResultParser parser = new RegResultParser();
        HttpUtil.getInstance().post(urlAppendCommonParams(FACE_COMPARE),
                "images", params, parser, listener);
    }

}
