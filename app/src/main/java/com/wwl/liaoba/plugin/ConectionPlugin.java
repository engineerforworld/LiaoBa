package com.wwl.liaoba.plugin;

import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;

import com.wwl.liaoba.R;

import io.rong.imkit.RongExtension;
import io.rong.imkit.plugin.IPluginModule;

public class ConectionPlugin implements IPluginModule {

    @Override
    public Drawable obtainDrawable(Context context) {
        return ContextCompat.getDrawable(context, R.mipmap.lb_contact_plugin_icon_hover);
    }

    @Override
    public String obtainTitle(Context context) {
        return "个人名片";
    }

    @Override
    public void onClick(Fragment fragment, RongExtension rongExtension) {

    }

    @Override
    public void onActivityResult(int i, int i1, Intent intent) {

    }
}
