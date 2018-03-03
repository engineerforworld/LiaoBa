package com.wwl.liaoba.cusconversation;

import android.content.Context;

import java.util.Collection;

import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.adapter.MessageListAdapter;
import io.rong.imlib.model.Conversation;


public class ChatConversationAdapter extends MessageListAdapter {
    public ChatConversationAdapter(Context context) {
        super(context);
    }

    @Override
    public void add(UIMessage uiMessage) {
        if (uiMessage.getConversationType().equals(Conversation.ConversationType.PRIVATE)) {
            return;
        }
        super.add(uiMessage);
    }

    @Override
    public void addCollection(Collection<UIMessage> collection) {
        super.addCollection(collection);
    }

    @Override
    public void add(UIMessage uiMessage, int position) {
        if (uiMessage.getConversationType().equals(Conversation.ConversationType.PRIVATE)) {
            return;
        }
        super.add(uiMessage, position);
    }

    @Override
    public void addCollection(UIMessage... collection) {
        super.addCollection(collection);
    }
}
