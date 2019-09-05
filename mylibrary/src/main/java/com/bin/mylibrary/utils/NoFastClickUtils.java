package com.bin.mylibrary.utils;



/**
 * Android开发中，防止控件短时间内被多次点击，从而重复触发事件。
 */
public class NoFastClickUtils {
    private static long lastClickTime = 0;//上次点击的时间   
    private static int spaceTime = 1000;//时间间隔   

    public static boolean isFastClick() {
        long currentTime = System.currentTimeMillis();//当前系统时间       
        boolean isAllowClick;//是否允许点击       
        if (currentTime - lastClickTime > spaceTime) {
            isAllowClick = false;
        } else {
            isAllowClick = true;
        }
        lastClickTime = currentTime;
        return isAllowClick;
    }


    /**
     * 调用的方法
     */
//    imageView.setOnClickListener(new View.OnClickListener() {
//        @Override   
//        public void onClick(View v) {
//            if (NoFastClickUtils.isFastClick()) {
//                ToastUtils.ShortToast(mContext, "点击间隔太短！");
//            } else {
//                listener.onImageAdClick(type, aimsId);
//            }
//        }
//    });

}
