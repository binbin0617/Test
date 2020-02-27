package com.bin.mylibrary.aty;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.support.annotation.NonNull;
import android.support.design.widget.TabLayout;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.bin.mylibrary.R;
import com.bin.mylibrary.adapter.PublicTabViewPagerAdapter;
import com.bin.mylibrary.base.BaseApplication;
import com.bin.mylibrary.base.BaseAty;
import com.bin.mylibrary.entity.FileInfo;
import com.bin.mylibrary.fragment.FolderDataFragment;
import com.bin.mylibrary.utils.FileUtil;
import com.bin.mylibrary.utils.FileUtils;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.File;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.PROGRESS_TYPE;


/**
 * 使用遍历文件夹的方式
 */
public class FolderAty extends BaseAty {
    private TabLayout tlFile;
    private ViewPager vpFile;
    //负责界面之间跳转的loading
    private SweetAlertDialog loadDialog;
    private List<String> mTabTitle = new ArrayList<>();
    private List<Fragment> mFragment = new ArrayList<>();

    private ArrayList<FileInfo> wordData = new ArrayList<>();
    private ArrayList<FileInfo> xlsData = new ArrayList<>();
    private ArrayList<FileInfo> pptData = new ArrayList<>();
    private ArrayList<FileInfo> pdfData = new ArrayList<>();


    private Handler handler = new Handler() {
        @Override
        public void handleMessage(Message msg) {
            super.handleMessage(msg);
            if (msg.what == 1) {
                initData();
            }else if(msg.what == 2){
                loadDialog.dismiss();
                setResult(RESULT_OK,new Intent().putExtra("success","success"));
                finish();
            }
        }
    };

    @Override
    protected void onDestroy() {
        super.onDestroy();
        handler.removeMessages(1);
        handler.removeMessages(2);
    }


    @NonNull
    @Override
    protected int getView() {
        return R.layout.activity_folder;
    }

    @Override
    public void initView(Bundle parms) {

    }

    @Override
    public void doBusiness(Context mContext) {
        tlFile = findViewById(R.id.tl_file);
        vpFile = findViewById(R.id.vp_file);
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED
                || ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            // 取得相机权限
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                this.requestPermissions(new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE}, 100);
            }
        }
        loadDialog = new SweetAlertDialog(FolderAty.this, PROGRESS_TYPE);
//        loadDialog.setTitleVisibility(View.GONE);
        loadDialog.setContentText("请稍候...").show();

        new Thread() {
            @Override
            public void run() {
                super.run();
                getFolderData();
            }
        }.start();
    }

    /**
     * 遍历文件夹中资源
     */
    public void getFolderData() {
        scanDirNoRecursion(Environment.getExternalStorageDirectory().toString());
        handler.sendEmptyMessage(1);
    }


    private void initData() {

        mTabTitle = new ArrayList<>();
        mFragment = new ArrayList<>();

        mTabTitle.add("word");
        mTabTitle.add("xls");
        mTabTitle.add("ppt");
        mTabTitle.add("pdf");


        FolderDataFragment wordFragment = new FolderDataFragment();
        Bundle wordBundle = new Bundle();
        wordBundle.putParcelableArrayList("file_data", wordData);
        wordBundle.putBoolean("is_image", false);
        wordFragment.setArguments(wordBundle);
        mFragment.add(wordFragment);

        FolderDataFragment xlsFragment = new FolderDataFragment();
        Bundle xlsBundle = new Bundle();
        xlsBundle.putParcelableArrayList("file_data", xlsData);
        xlsBundle.putBoolean("is_image", false);
        xlsFragment.setArguments(xlsBundle);
        mFragment.add(xlsFragment);

        FolderDataFragment pptFragment = new FolderDataFragment();
        Bundle pptBundle = new Bundle();
        pptBundle.putParcelableArrayList("file_data", pptData);
        pptBundle.putBoolean("is_image", false);
        pptFragment.setArguments(pptBundle);
        mFragment.add(pptFragment);

        FolderDataFragment pdfFragment = new FolderDataFragment();
        Bundle pdfBundle = new Bundle();
        pdfBundle.putParcelableArrayList("file_data", pdfData);
        pdfBundle.putBoolean("is_image", false);
        pdfFragment.setArguments(pdfBundle);
        mFragment.add(pdfFragment);

        FragmentManager fragmentManager = getSupportFragmentManager();

        PublicTabViewPagerAdapter tabViewPagerAdapter = new PublicTabViewPagerAdapter(fragmentManager, mFragment, mTabTitle);
        vpFile.setAdapter(tabViewPagerAdapter);

        tlFile.setupWithViewPager(vpFile);

        tlFile.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
            @Override
            public void onTabSelected(TabLayout.Tab tab) {
                vpFile.setCurrentItem(tab.getPosition());
            }

            @Override
            public void onTabUnselected(TabLayout.Tab tab) {

            }

            @Override
            public void onTabReselected(TabLayout.Tab tab) {

            }
        });

        loadDialog.dismiss();
    }

    /**
     * 非递归
     *
     * @param path
     */
    public void scanDirNoRecursion(String path) {
        LinkedList list = new LinkedList();
        File dir = new File(path);
        File file[] = dir.listFiles();
        if (file == null) return;
        for (int i = 0; i < file.length; i++) {
            if (file[i].isDirectory())
                list.add(file[i]);
            else {
                System.out.println(file[i].getAbsolutePath());
            }
        }
        File tmp;
        while (!list.isEmpty()) {
            tmp = (File) list.removeFirst();//首个目录
            if (tmp.isDirectory()) {
                file = tmp.listFiles();
                if (file == null)
                    continue;
                for (int i = 0; i < file.length; i++) {
                    if (file[i].isDirectory())
                        list.add(file[i]);//目录则加入目录列表，关键
                    else {
//                        System.out.println(file[i]);
//                        if (file[i].getName().endsWith(".png") || file[i].getName().endsWith(".jpg") || file[i].getName().endsWith(".gif")) {
//                            //往图片集合中 添加图片的路径
//                            FileInfo document = FileUtil.getFileInfoFromFile(new File(file[i].getAbsolutePath()));
//                            imageData.add(document);
//                        } else
                            if (file[i].getName().endsWith(".doc") || file[i].getName().endsWith(".docx")) {
                            FileInfo document = FileUtils.getFileInfoFromFile(new File(file[i].getAbsolutePath()));
                            wordData.add(document);
                        } else if (file[i].getName().endsWith(".xls") || file[i].getName().endsWith(".xlsx")) {
                            //往图片集合中 添加图片的路径
                            FileInfo document = FileUtils.getFileInfoFromFile(new File(file[i].getAbsolutePath()));
                            xlsData.add(document);
                        } else if (file[i].getName().endsWith(".ppt") || file[i].getName().endsWith(".pptx")) {
                            //往图片集合中 添加图片的路径
                            FileInfo document = FileUtils.getFileInfoFromFile(new File(file[i].getAbsolutePath()));
                            pptData.add(document);
                        } else if (file[i].getName().endsWith(".pdf")) {
                            //往图片集合中 添加图片的路径
                            FileInfo document = FileUtils.getFileInfoFromFile(new File(file[i].getAbsolutePath()));
                            pdfData.add(document);
                        }
                    }
                }
            } else {
                System.out.println(tmp);
            }
        }
    }

    /**
     * 遍历手机所有文件 并将路径名存入集合中 参数需要 路径和集合 - 递归
     */
    public void recursionFile(File dir) {
        //得到某个文件夹下所有的文件
        File[] files = dir.listFiles();
        //文件为空
        if (files == null) {
            return;
        }
        //遍历当前文件下的所有文件
        for (File file : files) {
            //如果是文件夹
            if (file.isDirectory()) {
                //则递归(方法自己调用自己)继续遍历该文件夹
                recursionFile(file);
            } else { //如果不是文件夹 则是文件
                //如果文件名以 .mp3结尾则是mp3文件
                if (file.getName().endsWith(".doc") || file.getName().endsWith(".docx")) {
                    //往图片集合中 添加图片的路径
                    FileInfo document = FileUtils.getFileInfoFromFile(new File(file.getAbsolutePath()));
                    wordData.add(document);
                } else if (file.getName().endsWith(".xls") || file.getName().endsWith(".xlsx")) {
                    //往图片集合中 添加图片的路径
                    FileInfo document = FileUtils.getFileInfoFromFile(new File(file.getAbsolutePath()));
                    xlsData.add(document);
                } else if (file.getName().endsWith(".ppt") || file.getName().endsWith(".pptx")) {
                    //往图片集合中 添加图片的路径
                    FileInfo document = FileUtils.getFileInfoFromFile(new File(file.getAbsolutePath()));
                    pptData.add(document);
                } else if (file.getName().endsWith(".pdf")) {
                    Log.i("qqq", "pdf=======");
                    //往图片集合中 添加图片的路径
                    FileInfo document = FileUtils.getFileInfoFromFile(new File(file.getAbsolutePath()));
                    pdfData.add(document);
                }
            }
        }
    }

    public void onclick(View view) {
        if(BaseApplication.fileMap.size()==0){
            Toast.makeText(this,"请选择文件",Toast.LENGTH_SHORT);
            return;
        }
        loadDialog = new SweetAlertDialog(FolderAty.this, PROGRESS_TYPE);
//        loadDialog.setTitleVisibility(View.GONE);
        loadDialog.setContentText("请稍候...").show();
        new Thread() {
            @Override
            public void run() {
                super.run();
                fileToBase64();
            }
        }.start();

    }

    public void fileToBase64(){
        JSONArray array1 =new JSONArray();
        for(String key:BaseApplication.fileMap.keySet()){
            JSONObject object =new JSONObject();
            try {
                object.put(key, FileUtil.encodeBase64File(new File(BaseApplication.fileMap.get(key))));
                array1.put(object);
            } catch (Exception e) {
                e.printStackTrace();
                handler.sendEmptyMessage(2);
            }
        }
        BaseApplication.documentBase64 = array1.toString().trim();
        handler.sendEmptyMessage(2);
    }
}
