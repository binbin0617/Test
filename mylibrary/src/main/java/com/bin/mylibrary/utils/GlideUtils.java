package com.bin.mylibrary.utils;

import android.content.Context;
import android.graphics.Bitmap;
import android.support.v4.graphics.drawable.RoundedBitmapDrawable;
import android.support.v4.graphics.drawable.RoundedBitmapDrawableFactory;
import android.widget.ImageView;


import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;
import com.bumptech.glide.request.target.BitmapImageViewTarget;

public class GlideUtils {
    public static void loadImgCircle(Context context, String url, ImageView iv) {
        RequestOptions requestOptions = RequestOptions.circleCropTransform();
        Glide.with(context).load(url).apply(requestOptions).into(iv);
    }

    public static void loadImgCircle(final Context context, int id, final ImageView iv) {
        Glide.with(context).asBitmap().load(id).centerCrop().into(new BitmapImageViewTarget(iv) {
            @Override
            protected void setResource(Bitmap resource) {
                RoundedBitmapDrawable circularBitmapDrawable =
                        RoundedBitmapDrawableFactory.create(context.getResources(), resource);
                circularBitmapDrawable.setCircular(true);
                iv.setImageDrawable(circularBitmapDrawable);
            }
        });
    }
}
