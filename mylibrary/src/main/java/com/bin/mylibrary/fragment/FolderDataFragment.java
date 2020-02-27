package com.bin.mylibrary.fragment;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.bin.mylibrary.R;
import com.bin.mylibrary.adapter.FolderDataRecycleAdapter;
import com.bin.mylibrary.base.BaseApplication;
import com.bin.mylibrary.entity.FileInfo;
import com.chad.library.adapter.base.BaseQuickAdapter;

import java.util.List;


/**
 * Created by yis on 2018/4/17.
 */

public class FolderDataFragment extends Fragment {

    private RecyclerView rvDoc;
    private long doSize = 0;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View rootView = inflater.inflate(R.layout.fragment_doc, container, false);
        rvDoc = rootView.findViewById(R.id.rv_doc);
        return rootView;
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

        initData();
    }

    private void initData() {
        Bundle bundle = this.getArguments();

        final List<FileInfo> data = bundle.getParcelableArrayList("file_data");
        boolean isImage = bundle.getBoolean("is_image");

        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(getActivity());
        //设置RecyclerView 布局
        rvDoc.setLayoutManager(linearLayoutManager);
        final FolderDataRecycleAdapter pptListAdapter = new FolderDataRecycleAdapter(R.layout.adapter_folder_data_rv_item, data);
        pptListAdapter.isPhoto = isImage;
        rvDoc.setAdapter(pptListAdapter);
        pptListAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                FileInfo fa = data.get(position);
                if(BaseApplication.fileMap.get(fa.getFileName())==null){
                    if(BaseApplication.fileMap.size()>=9){
                        Toast.makeText(getContext(),"最多只能选择9个文件！",Toast.LENGTH_LONG).show();
                        return;
                    }
                    BaseApplication.fileMap.put(fa.getFileName(),fa.getFilePath());
                    doSize= doSize+fa.getFileSize();
                    if(doSize>5*1024*1024){
                        Toast.makeText(getContext(),"上传文件不能大于5M!",Toast.LENGTH_LONG).show();
                        return;
                    }
                    data.get(position).setCheck(true);
                }else{
                    BaseApplication.fileMap.remove(fa.getFileName());
                    data.get(position).setCheck(false);
                    doSize= doSize-fa.getFileSize();
                }
                pptListAdapter.notifyItemChanged(position);
            }
        });
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        BaseApplication.fileMap.clear();
    }
}
