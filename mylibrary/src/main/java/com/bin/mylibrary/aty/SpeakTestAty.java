package com.bin.mylibrary.aty;

import android.annotation.SuppressLint;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.speech.RecognitionListener;
import android.speech.SpeechRecognizer;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.baidu.speech.VoiceRecognitionService;
import com.baidu.voicerecognition.android.ui.BaiduASRDigitalDialog;
import com.bin.mylibrary.R;
import com.bin.mylibrary.base.BaseAty;
import com.bin.mylibrary.entity.SpeakInfo;
import com.bin.mylibrary.utils.LogUtils;
import com.chad.library.adapter.base.BaseQuickAdapter;
import com.chad.library.adapter.base.BaseViewHolder;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.gson.Gson;

import org.json.JSONException;
import org.json.JSONObject;

import java.lang.reflect.Parameter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class SpeakTestAty extends BaseAty {
    protected final String TAG = this.getClass().getSimpleName();
    private EditText et;
    private RecyclerView rv;
    private SpeechRecognizer speechRecognizer;
    private SpeakAdapter speakAdapter;
    private List<SpeakInfo.HelpListBean> mList;
    private FloatingActionButton iv_speak;
    private FloatingActionButton iv_search;

    @NonNull
    @Override
    protected int getView() {
        return R.layout.speak_test_aty;
    }

    @Override
    public void initView(Bundle parms) {
        mList = new ArrayList<>();
    }

    @Override
    public void doBusiness(Context mContext) {
        et = findViewById(R.id.et);
        rv = findViewById(R.id.rv);
        iv_speak = findViewById(R.id.iv_speak);
        iv_search = findViewById(R.id.iv_search);
        iv_speak.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                initSpeak();
            }
        });
        iv_search.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String speakStr = et.getText().toString().trim();
                if (speakStr.length() <= 1) {
                    logShow(TAG, "当前输入的查询关键字字数太少，请重新输入。");
                } else {
                    SharedPreferences dataInfo = getSharedPreferences("userinfo", Context.MODE_PRIVATE);
                    org.json.JSONObject jsonObject = new org.json.JSONObject();
                    try {
                        jsonObject.put("userInfo", dataInfo.getString("data", ""));
                        jsonObject.put("keyWords", speakStr);
                        httpSpeak(jsonObject.toString());
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                }
            }
        });
    }

    private void httpSpeak(String jsonStr) {
//        HttpRequest.POST(SpeakTestAty.this, speakUrl, new Parameter().add("jsonStr", jsonStr), new ResponseListener() {
//            @Override
//            public void onResponse(String response, Exception error) {
//                LogUtils.e(TAG, response + "----------");
//                if (error == null) {
//                    LogUtils.e(TAG, response);
//                    try {
//                        Gson gson = new Gson();
//                        SpeakInfo speakInfo = gson.fromJson(response, SpeakInfo.class);
//                        mList.clear();
//                        mList.addAll(speakInfo.getHelpList());
//                        initAdapter();
//                    } catch (Exception e) {
//                        e.printStackTrace();
//                    }
//                } else {
//                    LogUtils.e(TAG, error.toString());
//                }
//            }
//        });
    }

    @SuppressLint("WrongConstant")
    private void initAdapter() {
        speakAdapter = new SpeakAdapter(R.layout.item_speak_test, mList);
        rv.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        rv.setAdapter(speakAdapter);
        speakAdapter.setOnItemClickListener(new BaseQuickAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(BaseQuickAdapter adapter, View view, int position) {
                Intent intent = new Intent(SpeakTestAty.this, TestWebViewAty.class);
                if ("pdf".equals(mList.get(position).getBy1())) {
                    intent.putExtra("url", "file:///android_asset/pdfjs/web/viewer.html?file=" + mList.get(position).getLjdz());
                } else {
                    intent.putExtra("url", mList.get(position).getLjdz());
                }
                startActivity(intent);
            }
        });
    }

    private void initSpeak() {
        speechRecognizer = SpeechRecognizer.createSpeechRecognizer(this, new ComponentName(this, VoiceRecognitionService.class));
        speechRecognizer.setRecognitionListener(recognitionListener);
        Intent intent = new Intent(SpeakTestAty.this, BaiduASRDigitalDialog.class);
        Bundle params = new Bundle();
        intent.putExtras(params);
        /*intent.putExtra(BaiduConstant.EXTRA_SOUND_START, R.raw.bdspeech_recognition_start);
        intent.putExtra(BaiduConstant.EXTRA_SOUND_END, R.raw.bdspeech_speech_end);
        intent.putExtra(BaiduConstant.EXTRA_SOUND_SUCCESS, R.raw.bdspeech_recognition_success);
        intent.putExtra(BaiduConstant.EXTRA_SOUND_ERROR, R.raw.bdspeech_recognition_error);
        intent.putExtra(BaiduConstant.EXTRA_SOUND_CANCEL, R.raw.bdspeech_recognition_cancel);
        intent.putExtra(BaiduConstant.EXTRA_PROP, 10060);*/
        startActivityForResult(intent, 2);
    }

    private RecognitionListener recognitionListener = new RecognitionListener() {
        @Override
        public void onReadyForSpeech(Bundle params) {

        }

        @Override
        public void onBeginningOfSpeech() {

        }

        @Override
        public void onRmsChanged(float rmsdB) {

        }

        @Override
        public void onBufferReceived(byte[] buffer) {

        }

        @Override
        public void onEndOfSpeech() {

        }

        @Override
        public void onError(int error) {
            StringBuilder sb = new StringBuilder();
            switch (error) {
                case SpeechRecognizer.ERROR_AUDIO:
                    sb.append("音频问题");
                    break;
                case SpeechRecognizer.ERROR_SPEECH_TIMEOUT:
                    sb.append("没有语音输入");
                    break;
                case SpeechRecognizer.ERROR_CLIENT:

                    sb.append("其它客户端错误");
                    break;
                case SpeechRecognizer.ERROR_INSUFFICIENT_PERMISSIONS:
                    sb.append("权限不足");
                    break;
                case SpeechRecognizer.ERROR_NETWORK:
                    sb.append("网络问题");
                    break;
                case SpeechRecognizer.ERROR_NO_MATCH:
                    sb.append("没有匹配的识别结果");
                    break;
                case SpeechRecognizer.ERROR_RECOGNIZER_BUSY:
                    sb.append("引擎忙");
                    break;
                case SpeechRecognizer.ERROR_SERVER:
                    sb.append("服务端错误");
                    break;
                case SpeechRecognizer.ERROR_NETWORK_TIMEOUT:
                    sb.append("连接超时");
                    break;
            }
            sb.append(":" + error);
            print("识别失败：" + sb.toString());
        }

        @Override
        public void onResults(Bundle results) {
            ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            String json_res = results.getString("origin_result");
            print(json_res);
            et.setText(nbest.get(0));
            et.setSelection(nbest.get(0).length());
        }

        @Override
        public void onPartialResults(Bundle partialResults) {
            ArrayList<String> nbest = partialResults.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
            if (nbest.size() > 0) {
                et.setText(nbest.get(0));
                et.setSelection(nbest.get(0).length());
            }
        }

        @Override
        public void onEvent(int eventType, Bundle params) {
            switch (eventType) {
                case 11:
                    String reason = params.get("reason") + "";
                    print("EVENT_ERROR, " + reason);
                    break;
                case VoiceRecognitionService.EVENT_ENGINE_SWITCH:
                    int type = params.getInt("engine_type");
                    print("*引擎切换至" + (type == 0 ? "在线" : "离线"));
                    break;
            }
        }
    };

    private void print(String msg) {
        LogUtils.e(TAG, msg);
    }

    private void onResults(Bundle results) {
        ArrayList<String> nbest = results.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION);
        print("识别成功：" + Arrays.toString(nbest.toArray(new String[nbest.size()])));
        String json_res = results.getString("origin_result");
        try {
            print("origin_result=\n" + new JSONObject(json_res).toString(4));
        } catch (Exception e) {
            print("origin_result=[warning: bad json]\n" + json_res);
        }
        et.setText(nbest.get(0));
        et.setSelection(nbest.get(0).length());
    }

    /**
     * 重写取得活动返回值的方法
     **/
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent intent) {
        switch (requestCode) {
            case 2:
                if (intent != null) {
                    onResults(intent.getExtras());
                }
                break;
            default:
        }
    }

    public void finish(View view) {
        finish();
    }
}

class SpeakAdapter extends BaseQuickAdapter<SpeakInfo.HelpListBean, BaseViewHolder> {

    public SpeakAdapter(int layoutResId, @Nullable List<SpeakInfo.HelpListBean> data) {
        super(layoutResId, data);
    }

    @Override
    protected void convert(BaseViewHolder helper, SpeakInfo.HelpListBean item) {
        helper.setText(R.id.tv, item.getWdmc());
    }
}
