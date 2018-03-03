package com.wwl.liaoba.cusconversation;

import android.content.Context;

import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.widget.adapter.MessageListAdapter;

/**
 * Created by wangwenliang on 2018/2/8.
 */

public class ChatConversationFragment extends ConversationFragment {

    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        return new ChatConversationAdapter(context);
    }
}
