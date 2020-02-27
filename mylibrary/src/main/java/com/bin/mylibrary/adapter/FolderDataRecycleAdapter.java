package com.bin.mylibrary.adapter;

import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.ImageView;

import com.bin.mylibrary.R;
import com.bin.mylibrary.entity.FileInfo;
import com.bin.mylibrary.utils.FileUtils;
import com.bumptech.glide.Glide;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;

import java.util.List;

import static com.bin.mylibrary.utils.FileUtils.getFileSzie;


/**
 * 使用遍历文件夹的方式
 * Created by yis on 2018/4/17.
 */

public class FolderDataRecycleAdapter extends BaseQuickAdapter<FileInfo, BaseViewHolder> {
    public boolean isPhoto = false;

    public FolderDataRecycleAdapter(int layoutResId, @Nullable List<FileInfo> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(@NonNull BaseViewHolder helper, FileInfo item) {
        helper.setText(R.id.tv_content,item.getFileName());
        helper.setText(R.id.tv_size,getFileSzie(item.getFileSize()));
        helper.setText(R.id.tv_time,item.getTime());
        //封面图
        if (isPhoto) {
            Glide.with(mContext).load(item.getFilePath()).into((ImageView) helper.getView(R.id.iv_cover));
        } else {
            Glide.with(mContext).load(FileUtils.getFileTypeImageId(mContext, item.getFilePath())).fitCenter().into((ImageView) helper.getView(R.id.iv_cover));
        }
        if(item.isCheck()){
            helper.getView(R.id.iv_check).setVisibility(View.VISIBLE);
        }else{
            helper.getView(R.id.iv_check).setVisibility(View.GONE);
        }
    }
}
