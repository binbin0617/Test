package com.bin.mylibrary.utils;

import android.util.Log;

public class LogUtils {
    public static boolean isOpen = true;

    public static void e(String Tag, String title) {
        if (isOpen) {
            Log.e("-->" + Tag, "-->" + title);
        }
    }
}
