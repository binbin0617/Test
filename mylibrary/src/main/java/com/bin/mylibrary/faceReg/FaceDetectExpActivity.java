package com.bin.mylibrary.faceReg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceDetectActivity;
import com.bin.mylibrary.entity.FaceInfo;
import com.bin.mylibrary.interfaces.JavascriptInterfaceImpl;
import com.bin.mylibrary.utils.LogUtils;
import com.bin.mylibrary.utils.Utils;
import com.google.gson.Gson;
import com.kongzue.baseokhttp.HttpRequest;
import com.kongzue.baseokhttp.listener.ResponseListener;
import com.kongzue.baseokhttp.util.Parameter;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.PROGRESS_TYPE;
import static com.bin.mylibrary.utils.MyCode.EncryptDES;

public class FaceDetectExpActivity extends FaceDetectActivity {
    //负责界面之间跳转的loading
    private SweetAlertDialog loadDialog;
    /**
     * 面部识别所需要的参数
     */
    private String WSDL_URI_FACE = "http://114.116.53.84:10001/Face/FaceCheck.ashx";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onDetectCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onDetectCompletion(status, message, base64ImageMap);
        // 返回传参用
        Intent intent = new Intent();
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            // 采集成功
            LogUtils.e(TAG, "onLivenessCompletion: 人脸图像采集成功");
            loadDialog = new SweetAlertDialog(FaceDetectExpActivity.this, PROGRESS_TYPE);
//            loadDialog.setTitleVisibility(View.GONE);
            loadDialog.setContentText("请稍候...").show();
            SharedPreferences dataInfo = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
            String userStr = dataInfo.getString("data", "");
            com.alibaba.fastjson.JSONObject user = null;
            if (!"".equals(userStr.trim())) {
                // 用户信息不能为空
                user = JSON.parseObject(userStr);
            }
            JSONObject jsonObject = new JSONObject();
            try {
                jsonObject.put("Command", "REG");
                jsonObject.put("ID", EncryptDES(user.getString("rybh")));
                LogUtils.e(TAG, "data=" + user.getString("rybh"));
                jsonObject.put("PIC", base64ImageMap.get("bestImage0"));
            } catch (JSONException e) {
                e.printStackTrace();
            }
            /**
             * 面部注册网络请求
             */
            LogUtils.e(TAG, "data=" + jsonObject.toString());
            HttpRequest.POST(this, WSDL_URI_FACE, new Parameter().add("data", jsonObject.toString()), new ResponseListener() {
                @Override
                public void onResponse(String response, Exception error) {
                    loadDialog.dismiss();
                    if (error == null) {
                        // 保存图片到本地
                        try {
                            Gson gson = new Gson();
                            FaceInfo faceInfo = gson.fromJson(response, FaceInfo.class);
                            int code;
                            if (!TextUtils.isEmpty(faceInfo.getResCode())) {
                                code = Integer.parseInt(faceInfo.getResCode());
                                switch (code) {
                                    case 0://todo  在调用的时候  录入脸的照片时记得加密码校验
                                        String hp = base64ImageMap.get("bestImage0");
                                        String hp_path = FaceDetectExpActivity.this.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                                                File.separator + "image.dat";
                                        // 判断是否已经存在当前数据图片数据、存在的话需要先删除文件
                                        Utils.deleteSingleFile(hp_path);
                                        // 保存文件
                                        File dataFile = new File(hp_path);
                                        try {
                                            dataFile.createNewFile();
                                            FileOutputStream outStream = new FileOutputStream(dataFile);
                                            outStream.write(hp.getBytes());
                                            outStream.close();
                                            intent.putExtra("result", "success");
                                        } catch (IOException e) {
                                            Log.e(TAG, "创建image.dat文件失败");
                                            e.printStackTrace();
                                            intent.putExtra("result", "error");
                                        }
                                        // 设置返回状态OK
                                        setResult(RESULT_OK, intent);
                                        finish();
                                        break;
                                    case 1000:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "要校验得脸得特征码数据库中不存在!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                    case -1:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "没传id!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                    case -2:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "没传图片信息!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;

                                    case -100:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "服务器超时!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                    case -1000:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "传入图片无法获得人脸特征信息!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                    case -2000:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "要注册的脸已存在!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                    default:
                                        if (JavascriptInterfaceImpl.isDelFace) {
                                            JavascriptInterfaceImpl.isDelFace = false;
                                        }
                                        Toast.makeText(FaceDetectExpActivity.this, "服务器异常!", Toast.LENGTH_SHORT).show();
                                        finish();
                                        break;
                                }
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                            LogUtils.e("面部比对Json转换异常", e.toString());
                            if (JavascriptInterfaceImpl.isDelFace) {
                                JavascriptInterfaceImpl.isDelFace = false;
                            }
                            Toast.makeText(FaceDetectExpActivity.this, "服务器异常", Toast.LENGTH_SHORT).show();
                            finish();
                        }


                    } else {
                        if (JavascriptInterfaceImpl.isDelFace) {
                            JavascriptInterfaceImpl.isDelFace = false;
                        }
                        Toast.makeText(FaceDetectExpActivity.this, "请求服务器获得数据异常", Toast.LENGTH_SHORT).show();
                        finish();
                        LogUtils.e("WSDL_URI_FACE", "失败的原因" + error.toString());
                    }
                }
            });

        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            // 采集超时 TODO
            Log.d(TAG, "onLivenessCompletion: 人脸图像采集超时");
            intent.putExtra("result", "error");
            // 设置返回状态OK
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void finish() {
        super.finish();
    }

}
