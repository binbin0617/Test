package com.bin.mylibrary.faceReg;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.widget.Toast;

import com.alibaba.fastjson.JSON;
import com.baidu.idl.face.platform.FaceStatusEnum;
import com.baidu.idl.face.platform.ui.FaceLivenessActivity;
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
import java.util.HashMap;

import cn.pedant.SweetAlert.SweetAlertDialog;

import static cn.pedant.SweetAlert.SweetAlertDialog.PROGRESS_TYPE;
import static com.bin.mylibrary.utils.MyCode.EncryptDES;

public class FaceLivenessExpActivity extends FaceLivenessActivity {
    private String WSDL_URI_FACE = "http://114.116.53.84:10001/Face/FaceCheck.ashx";
    private SweetAlertDialog loadDialog;
    SharedPreferences dataInfo;
    String userStr;
    com.alibaba.fastjson.JSONObject user = null;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dataInfo = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        userStr = dataInfo.getString("data", "");
    }

    @Override
    public void onLivenessCompletion(FaceStatusEnum status, String message, HashMap<String, String> base64ImageMap) {
        super.onLivenessCompletion(status, message, base64ImageMap);
        // 返回传参用
        Intent intent = new Intent();
        if (status == FaceStatusEnum.OK && mIsCompletion) {
            Log.d(TAG, "onLivenessCompletion: 活体检测成功");
            String data = base64ImageMap.get("bestImage0");
            faceCompare(data);
        } else if (status == FaceStatusEnum.Error_DetectTimeout ||
                status == FaceStatusEnum.Error_LivenessTimeout ||
                status == FaceStatusEnum.Error_Timeout) {
            // 采集超时
            if (JavascriptInterfaceImpl.isDelFace) {
                JavascriptInterfaceImpl.isDelFace = false;
            }
            Log.d(TAG, "onLivenessCompletion: 活体检测采集超时");
            // 活体检测采集超时
            intent.putExtra("result", "livetimeout");
            // 设置返回状态OK
            setResult(RESULT_OK, intent);
            finish();
        }
    }

    @Override
    public void finish() {
        if (JavascriptInterfaceImpl.isDelFace) {
            JavascriptInterfaceImpl.isDelFace = false;
        }
        super.finish();
    }

    // 调用百度人脸对比
    private void faceCompare(final String data) {
        loadDialog = new SweetAlertDialog(FaceLivenessExpActivity.this, PROGRESS_TYPE);
//        loadDialog.setTitleVisibility(View.GONE);
        loadDialog.setContentText("请稍候...").show();
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Command", "CHK");
            jsonObject.put("PIC", data);
        } catch (JSONException e) {
            e.printStackTrace();
        }
        Intent intent = new Intent();
        HttpRequest.POST(FaceLivenessExpActivity.this, WSDL_URI_FACE,
                new Parameter().add("data", jsonObject.toString()), new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        loadDialog.dismiss();
                        if (error == null) {
                            try {
                                Gson gson = new Gson();
                                FaceInfo faceInfo = gson.fromJson(response, FaceInfo.class);
                                LogUtils.e("人脸校验结果", response);
                                if (!"".equals(userStr.trim())) {
                                    // 用户信息不能为空
                                    user = JSON.parseObject(userStr);
                                }
                                if ("0".equals(faceInfo.getResCode()) &&
                                        user.getString("rybh").equals(faceInfo.getResMsg())) {
                                    if (JavascriptInterfaceImpl.isDelFace) {
                                        JavascriptInterfaceImpl.isDelFace = false;
                                        delHttp(intent);
                                    } else {
//                                        // 是同一个人
                                        intent.putExtra("result", "true");
                                        // 设置返回状态OK
                                        setResult(RESULT_OK, intent);
                                        finish();
                                    }
                                } else if ("-100".equals(faceInfo.getResCode())) {
                                    if (JavascriptInterfaceImpl.isDelFace) {
                                        JavascriptInterfaceImpl.isDelFace = false;
                                    }
                                    Toast.makeText(FaceLivenessExpActivity.this, "服务器超时!", Toast.LENGTH_SHORT).show();
                                    finish();

                                } else if ("-1000".equals(faceInfo.getResCode())) {
                                    if (JavascriptInterfaceImpl.isDelFace) {
                                        JavascriptInterfaceImpl.isDelFace = false;
                                    }
                                    Toast.makeText(FaceLivenessExpActivity.this, "传入图片无法获得人脸特征信息!", Toast.LENGTH_SHORT).show();
                                    finish();
                                } else {
                                    if (JavascriptInterfaceImpl.isDelFace) {
                                        JavascriptInterfaceImpl.isDelFace = false;
                                    }
                                    //不是同一个人
                                    intent.putExtra("result", "false");
                                    // 设置返回状态OK
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                                if (JavascriptInterfaceImpl.isDelFace) {
                                    JavascriptInterfaceImpl.isDelFace = false;
                                }
                                LogUtils.e("面部比对Json转换异常", e.toString());
                                intent.putExtra("result", "error");
                                // 设置返回状态OK
                                setResult(RESULT_OK, intent);
                                finish();

                            }
                        } else {
                            if (JavascriptInterfaceImpl.isDelFace) {
                                JavascriptInterfaceImpl.isDelFace = false;
                            }
                            LogUtils.e("WSDL_URI_FACE", "失败的原因" + error.toString());
                            // 返回结果为空 对比失败
                            intent.putExtra("result", "error");
                            // 设置返回状态OK
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
    }

    private void delHttp(Intent intent) {
        SharedPreferences dataInfo = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
        String userStr = dataInfo.getString("data", "");
        com.alibaba.fastjson.JSONObject user = null;
        if (!"".equals(userStr.trim())) {
            // 用户信息不能为空
            user = JSON.parseObject(userStr);
        }
        JSONObject jsonObject = new JSONObject();
        try {
            jsonObject.put("Command", "DEL");
            jsonObject.put("ID", EncryptDES(user.getString("rybh")));
            LogUtils.e(TAG, "data=" + user.getString("rybh"));
        } catch (JSONException e) {
            e.printStackTrace();
        }
        /**
         * 删除面部网络请求
         */
        LogUtils.e(TAG, "data=" + jsonObject.toString());
        HttpRequest.POST(FaceLivenessExpActivity.this, WSDL_URI_FACE,
                new Parameter().add("data", jsonObject.toString()), new ResponseListener() {
                    @Override
                    public void onResponse(String response, Exception error) {
                        if (error == null) {
                            try {
                                Gson gson = new Gson();
                                FaceInfo faceInfo = gson.fromJson(response, FaceInfo.class);
                                if ("0".equals(faceInfo.getResCode())) {
//                                    Toast.makeText(FaceLivenessExpActivity.this, "删除成功", Toast.LENGTH_SHORT).show();
                                    LogUtils.e(TAG, "resCode:" + faceInfo.getResCode() + "  ,  " + "resMsg:" + faceInfo.getResMsg());
                                    String hp_path = FaceLivenessExpActivity.this.getExternalFilesDir(Environment.DIRECTORY_DCIM).getPath() +
                                            File.separator + "image.dat";
                                    // 判断是否已经存在当前数据图片数据、存在的话需要先删除文件
                                    Utils.deleteSingleFile(hp_path);
                                    // 删除成功
                                    intent.putExtra("result", "deleteSuccess");
                                    // 设置返回状态OK
                                    setResult(RESULT_OK, intent);
                                    finish();
                                } else if ("-100".equals(faceInfo.getResCode())) {
                                    if (JavascriptInterfaceImpl.isDelFace) {
                                        JavascriptInterfaceImpl.isDelFace = false;
                                    }
                                    Toast.makeText(FaceLivenessExpActivity.this, "服务器超时!", Toast.LENGTH_SHORT).show();
                                    finish();

                                } else {
                                    LogUtils.e(TAG, "resCode:" + faceInfo.getResCode() + "  ,  " + "resMsg:" + faceInfo.getResMsg());
                                    // 删除成功
                                    intent.putExtra("result", "deleteError");
                                    // 设置返回状态OK
                                    setResult(RESULT_OK, intent);
                                    finish();
                                }
                            } catch (Exception e) {
                                Toast.makeText(FaceLivenessExpActivity.this, "Gosn转换异常", Toast.LENGTH_SHORT).show();
                                LogUtils.e(TAG, e.toString());
                                intent.putExtra("result", "error");
                                // 设置返回状态OK
                                setResult(RESULT_OK, intent);
                                finish();
                            }
                        } else {
                            LogUtils.e(TAG, "网络请求失败!" + error.toString());
                            intent.putExtra("result", "error");
                            // 设置返回状态OK
                            setResult(RESULT_OK, intent);
                            finish();
                        }
                    }
                });
    }

}
