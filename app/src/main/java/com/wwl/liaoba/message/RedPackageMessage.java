package com.wwl.liaoba.message;

import android.os.Parcel;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.MessageContent;

/**
 * Created by wangwenliang on 2017/11/9.
 */
@MessageTag(value = "app:RedPkgMsg", flag = MessageTag.ISCOUNTED)
public class RedPackageMessage extends MessageContent {
    private String title;
    private String status;
    private String type;

    @Override
    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();
        try {
            jsonObj.put("title", this.getTitle());
            jsonObj.put("status", this.getStatus());
            jsonObj.put("type", this.getType());
        } catch (JSONException e) {
            Log.e("JSONException", e.getMessage());
        }
        try {
            return jsonObj.toString().getBytes("UTF-8");
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return null;
    }

    protected RedPackageMessage() {

    }

    public static RedPackageMessage obtain(String title, String status, String type) {
        RedPackageMessage model = new RedPackageMessage();
        model.setTitle(title);
        model.setStatus(status);
        model.setType(type);
        return model;
    }

    public RedPackageMessage(byte[] data) {
        String jsonStr = null;
        try {
            jsonStr = new String(data, "UTF-8");
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }
        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("title"))
                setTitle(jsonObj.optString("title"));
            if (jsonObj.has("status"))
                setStatus(jsonObj.optString("status"));
            if (jsonObj.has("type"))
                setType(jsonObj.optString("type"));
        } catch (JSONException e) {
            Log.d("JSONException", e.getMessage());
        }
    }

    public RedPackageMessage(Parcel in) {
        setTitle(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setStatus(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
        setType(ParcelUtils.readFromParcel(in));//该类为工具类，消息属性
    }

    public static final Creator<RedPackageMessage> CREATOR = new Creator<RedPackageMessage>() {

        @Override
        public RedPackageMessage createFromParcel(Parcel source) {
            return new RedPackageMessage(source);
        }

        @Override
        public RedPackageMessage[] newArray(int size) {
            return new RedPackageMessage[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, getTitle());
        ParcelUtils.writeToParcel(dest, getStatus());
        ParcelUtils.writeToParcel(dest, getType());
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }


}
