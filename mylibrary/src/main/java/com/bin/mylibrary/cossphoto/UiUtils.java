package com.bin.mylibrary.cossphoto;

import android.content.Context;
import android.support.annotation.NonNull;


public class UiUtils {
    private static Context mContext;
    public static Context getContext(){
        return mContext;
    }
    public static void  init(@NonNull Context context){
        UiUtils.mContext = context;
    }

}
