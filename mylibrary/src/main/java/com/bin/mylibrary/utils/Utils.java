package com.bin.mylibrary.utils;


import android.content.Context;
import android.os.Build;
import android.provider.Settings;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;


/**
 * 共通方法用文件
 * Created by zhaolei on 2017/10/9.
 */

public class Utils {

    // okhttp post
//    public static final MediaType JSON = MediaType.get("application/json; charset=utf-8");

//    /**
//     * okhttp3实现post取得数据
//     **/
//    public static String post(String url, String json) throws IOException {
//        RequestBody body = RequestBody.create(JSON, json);
//        Request request = new Request.Builder()
//                // 添加header
//                .addHeader("username","zhaolei")
//                .addHeader("password","leizhao")
//                .url(url)
//                .post(body)
//                .build();
//        try (Response response = client.newCall(request).execute()) {
//            return response.body().string();
//        }
//    }

    /**
     * 取得当前android的唯一标识(android_id+Serial Number)然后md5化
     **/
    public static String getUniqueId(Context context){
        String androidID = Settings.Secure.getString(context.getContentResolver(), Settings.Secure.ANDROID_ID);
        String id = androidID + Build.SERIAL;
        try {
            return toMD5(id);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return id;
        }
    }

    /**
     * 字符串md5化方法
     **/
    public static String toMD5(String text) throws NoSuchAlgorithmException {
        //获取摘要器 MessageDigest
        MessageDigest messageDigest = MessageDigest.getInstance("MD5");
        //通过摘要器对字符串的二进制字节数组进行hash计算
        byte[] digest = messageDigest.digest(text.getBytes());

        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < digest.length; i++) {
            //循环每个字符 将计算结果转化为正整数;
            int digestInt = digest[i] & 0xff;
            //将10进制转化为较短的16进制
            String hexString = Integer.toHexString(digestInt);
            //转化结果如果是个位数会省略0,因此判断并补0
            if (hexString.length() < 2) {
                sb.append(0);
            }
            //将循环结果添加到缓冲区
            sb.append(hexString);
        }
        //返回整个结果
        return sb.toString();
    }

    public static File getSaveFile(Context context) {
        File file = new File(context.getFilesDir(), "pic.jpg");
        return file;
    }

    /** 删除单个文件
     * @param filePath$Name 要删除的文件的文件名
     * @return 单个文件删除成功返回true，否则返回false
     */
    public static boolean deleteSingleFile(String filePath$Name) {
        File file = new File(filePath$Name);
        // 如果文件路径所对应的文件存在，并且是一个文件，则直接删除
        if (file.exists() && file.isFile()) {
            if (file.delete()) {
                Log.e("--Method--", "deleteSingleFile: 删除单个文件" + filePath$Name + "成功！");
                return true;
            } else {
                Log.e("--Method--", "删除单个文件" + filePath$Name + "失败！");
                return false;
            }
        } else {
            Log.e("--Method--", "删除单个文件失败：" + filePath$Name + "不存在！");
            return false;
        }
    }

    /**
     * 根据数据包路径返回文件字符串数据
     *
     * @param dataFilePath 文件所在路径
     * @return 数据
     */

    public static String fileRead(String dataFilePath) throws Exception {
        File file = new File(dataFilePath);//定义一个file对象，用来初始化FileReader
        FileReader reader = new FileReader(file);//定义一个fileReader对象，用来初始化BufferedReader
        BufferedReader bReader = new BufferedReader(reader);//new一个BufferedReader对象，将文件内容读取到缓存
        StringBuilder sb = new StringBuilder();//定义一个字符串缓存，将字符串存放缓存中
        String s = "";
        while ((s =bReader.readLine()) != null) {//逐行读取文件内容，不读取换行符和末尾的空格
            sb.append(s + "\n");//将读取的字符串添加换行符后累加存放在缓存中
        }
        bReader.close();
        return sb.toString();
    }

}
