package com.bin.mylibrary.faceReg;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

/**
 * 构建人脸比较json参数
 */
public class MatchModel {

    public static String getJson(String hp, String data) {
        JSONObject obj1 = getMatchObj(hp);
        JSONObject obj2 = getMatchObj(data);
        return getJson(obj1, obj2);
    }

    public static String getJson(JSONObject obj1, JSONObject obj2) {
        JSONArray array = new JSONArray();
        array.put(obj1);
        array.put(obj2);
        return array.toString();
    }

    public static JSONObject getMatchObj(String str) {
        JSONObject obj = new JSONObject();
        try {
            obj.put("image", str);
            obj.put("image_type", "BASE64");
            obj.put("face_type", "LIVE");
            // 活体及质量分数可根据自己实际情况设置
            obj.put("quality_control", "NORMAL");
            // obj.put("liveness_control", "NORMAL");
        } catch (JSONException e) {
            e.printStackTrace();
        }
        return obj;
    }


}