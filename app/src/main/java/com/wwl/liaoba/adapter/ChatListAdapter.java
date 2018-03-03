package com.wwl.liaoba.adapter;

import android.content.Context;

import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

/**
 * Created by wangwenliang on 2018/2/5.
 */

public class ChatListAdapter extends ConversationListAdapter {


    public ChatListAdapter(Context context) {
        super(context);
    }

    @Override
    public void add(UIConversation uiConversation) {
        super.add(uiConversation);
    }
}
