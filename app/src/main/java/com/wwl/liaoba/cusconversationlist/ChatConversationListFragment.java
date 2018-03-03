package com.wwl.liaoba.cusconversationlist;

import android.content.Context;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.widget.adapter.ConversationListAdapter;

public class ChatConversationListFragment extends ConversationListFragment {

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new ChatConversationListAdapter(context);
    }
}
