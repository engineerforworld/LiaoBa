package com.wwl.liaoba.cusconversationlist;

import android.content.Context;

import java.util.Collection;

import io.rong.imkit.model.UIConversation;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

public class ChatConversationListAdapter extends ConversationListAdapter {
    public ChatConversationListAdapter(Context context) {
        super(context);
    }

    @Override
    public void add(UIConversation uiConversation) {
        super.add(uiConversation);
    }

    @Override
    public void add(UIConversation uiConversation, int position) {
        super.add(uiConversation, position);
    }

    @Override
    public void addCollection(UIConversation... collection) {
        super.addCollection(collection);
    }

    @Override
    public void addCollection(Collection<UIConversation> collection) {
        super.addCollection(collection);
    }
}
