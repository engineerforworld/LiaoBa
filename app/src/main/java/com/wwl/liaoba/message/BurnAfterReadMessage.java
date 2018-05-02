package com.wwl.liaoba.message;

import android.net.Uri;
import android.os.Parcel;
import android.text.TextUtils;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;

import io.rong.common.ParcelUtils;
import io.rong.imlib.MessageTag;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ImageMessage;
import io.rong.message.ImageMessageHandler;

/**
 * <p>Description:
 * <p>Company：
 * <p>Email:bjxm2013@163.com
 * <p>Created by Devin Sun on 2018/3/21.
 */
@MessageTag(value = "app:BurnAfterReadMessage", flag = MessageTag.ISCOUNTED | MessageTag.ISPERSISTED,
        messageHandler = ImageMessageHandler.class)
public class BurnAfterReadMessage extends ImageMessage {
    private Uri mThumUri;
    private boolean mUpLoadExp = false;
    private String mBase64;
    private boolean mIsFull;
    public static final Creator<BurnAfterReadMessage> CREATOR = new Creator<BurnAfterReadMessage>() {
        public BurnAfterReadMessage createFromParcel(Parcel source) {
            return new BurnAfterReadMessage(source);
        }

        public BurnAfterReadMessage[] newArray(int size) {
            return new BurnAfterReadMessage[size];
        }
    };

    public BurnAfterReadMessage(byte[] data) {
        String jsonStr = new String(data);

        try {
            JSONObject jsonObj = new JSONObject(jsonStr);
            if (jsonObj.has("imageUri")) {
                String uri = jsonObj.optString("imageUri");
                if (!TextUtils.isEmpty(uri)) {
                    this.setRemoteUri(Uri.parse(uri));
                }
            }

            if (jsonObj.has("localPath")) {
                this.setLocalPath(Uri.parse(jsonObj.optString("localPath")));
            }

            if (jsonObj.has("content")) {
                this.setBase64(jsonObj.optString("content"));
            }

            if (jsonObj.has("extra")) {
                this.setExtra(jsonObj.optString("extra"));
            }

            if (jsonObj.has("exp")) {
                this.setUpLoadExp(true);
            }

            if (jsonObj.has("isFull")) {
                this.setIsFull(jsonObj.optBoolean("isFull"));
            }

            if (jsonObj.has("user")) {
                this.setUserInfo(this.parseJsonToUserInfo(jsonObj.getJSONObject("user")));
            }
        } catch (JSONException var5) {
            Log.e("JSONException", var5.getMessage());
        }

    }

    public BurnAfterReadMessage() {
    }

    private BurnAfterReadMessage(Uri thumbUri, Uri localUri) {
        this.mThumUri = thumbUri;
        this.setLocalPath(localUri);
    }

    private BurnAfterReadMessage(Uri thumbUri, Uri localUri, boolean original) {
        this.mThumUri = thumbUri;
        this.setLocalPath(localUri);
        this.mIsFull = original;
    }

    public static BurnAfterReadMessage obtain(Uri thumUri, Uri localUri) {
        return new BurnAfterReadMessage(thumUri, localUri);
    }

    public static BurnAfterReadMessage obtain(Uri thumUri, Uri localUri, boolean isFull) {
        return new BurnAfterReadMessage(thumUri, localUri, isFull);
    }

    public static BurnAfterReadMessage obtain() {
        return new BurnAfterReadMessage();
    }

    public Uri getThumUri() {
        return this.mThumUri;
    }

    public boolean isFull() {
        return this.mIsFull;
    }

    public void setIsFull(boolean isFull) {
        this.mIsFull = isFull;
    }


    public void setThumUri(Uri thumUri) {
        this.mThumUri = thumUri;
    }

    public Uri getLocalUri() {
        return this.getLocalPath();
    }

    public void setLocalUri(Uri localUri) {
        this.setLocalPath(localUri);
    }

    public Uri getRemoteUri() {
        return this.getMediaUrl();
    }

    public void setRemoteUri(Uri remoteUri) {
        this.setMediaUrl(remoteUri);
    }

    public void setBase64(String base64) {
        this.mBase64 = base64;
    }

    public String getBase64() {
        return this.mBase64;
    }

    public boolean isUpLoadExp() {
        return this.mUpLoadExp;
    }

    public void setUpLoadExp(boolean upLoadExp) {
        this.mUpLoadExp = upLoadExp;
    }

    public byte[] encode() {
        JSONObject jsonObj = new JSONObject();

        try {
            if (!TextUtils.isEmpty(this.mBase64)) {
                jsonObj.put("content", this.mBase64);
            } else {
                Log.d("BurnAfterReadMessage", "base64 is null");
            }

            if (this.getMediaUrl() != null) {
                jsonObj.put("imageUri", this.getMediaUrl().toString());
            }

            if (this.getLocalUri() != null) {
                jsonObj.put("localPath", this.getLocalUri().toString());
            }

            if (this.mUpLoadExp) {
                jsonObj.put("exp", true);
            }

            jsonObj.put("isFull", this.mIsFull);


            if (!TextUtils.isEmpty(this.getExtra())) {
                jsonObj.put("extra", this.getExtra());
            }

            if (this.getJSONUserInfo() != null) {
                jsonObj.putOpt("user", this.getJSONUserInfo());
            }
        } catch (JSONException var3) {
            Log.e("JSONException", var3.getMessage());
        }

        this.mBase64 = null;
        return jsonObj.toString().getBytes();
    }

    public int describeContents() {
        return 0;
    }

    public BurnAfterReadMessage(Parcel in) {
        this.setExtra(ParcelUtils.readFromParcel(in));
        this.setLocalPath((Uri) ParcelUtils.readFromParcel(in, Uri.class));
        this.setMediaUrl((Uri) ParcelUtils.readFromParcel(in, Uri.class));
        this.mThumUri = (Uri) ParcelUtils.readFromParcel(in, Uri.class);
        this.setUserInfo((UserInfo) ParcelUtils.readFromParcel(in, UserInfo.class));
        this.mIsFull = ParcelUtils.readIntFromParcel(in).intValue() == 1;
    }

    public void writeToParcel(Parcel dest, int flags) {
        ParcelUtils.writeToParcel(dest, this.getExtra());
        ParcelUtils.writeToParcel(dest, this.getLocalPath());
        ParcelUtils.writeToParcel(dest, this.getMediaUrl());
        ParcelUtils.writeToParcel(dest, this.mThumUri);
        ParcelUtils.writeToParcel(dest, this.getUserInfo());
        ParcelUtils.writeToParcel(dest, Integer.valueOf(this.mIsFull ? 1 : 0));
    }
}
