package com.wwl.liaoba.Extension;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;

import com.wwl.liaoba.R;

import io.rong.imkit.emoticon.IEmoticonTab;

public class LiaoBaEmoticonTab implements IEmoticonTab {

    @Override
    public Drawable obtainTabDrawable(Context context) {
        //展示的图标
        return context.getResources().getDrawable(R.drawable.rc_tab_emoji);
    }

    @Override
    public View obtainTabPager(Context context) {
        //初始化 Tab 中的内容， 返回 View 或 View 的子类皆可。还可参考 EmojiTab 中 obtainTabPager
        TextView textView = new TextView(context);
        textView.setText("Sample EmoticonTabs ");
        return new View(context);
    }

    @Override
    public void onTableSelected(int position) {

    }
}
