package com.bin.mylibrary.view;


/**
 * AgentWeb 空布局页面
 */
public class ErrorLayoutEntity {
    public int layoutRes = com.just.agentweb.R.layout.agentweb_error_page;
    public int reloadId;

    public void setLayoutRes(int layoutRes) {
        this.layoutRes = layoutRes;
        if (layoutRes <= 0) {
            layoutRes = -1;
        }
    }

    public void setReloadId(int reloadId) {
        this.reloadId = reloadId;
        if (reloadId <= 0) {
            reloadId = -1;
        }
    }
}
