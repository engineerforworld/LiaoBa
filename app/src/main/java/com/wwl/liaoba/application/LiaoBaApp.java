package com.wwl.liaoba.application;

import android.app.Application;

import com.wwl.liaoba.Utils.Constants;
import com.wwl.liaoba.Utils.LiaoBaAppContext;

import io.rong.imkit.RongIM;
import io.rong.push.RongPushClient;

import static io.rong.imkit.utils.SystemUtils.getCurProcessName;

public class LiaoBaApp extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        if (getApplicationInfo().packageName.equals(getCurProcessName(getApplicationContext()))) {
            RongPushClient.registerHWPush(this);
            RongPushClient.registerMiPush(this, Constants.mipush_appid, Constants.mipush_appkey);
            RongIM.init(this);
            LiaoBaAppContext.init(this);
        }
    }
}
