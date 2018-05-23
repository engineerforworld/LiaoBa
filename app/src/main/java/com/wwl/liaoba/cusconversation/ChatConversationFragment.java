package com.wwl.liaoba.cusconversation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;

import io.rong.imkit.RongExtension;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.widget.adapter.MessageListAdapter;

/**
 * Created by wangwenliang on 2018/2/8.
 */

public class ChatConversationFragment extends ConversationFragment {
    private RelativeLayout containerView;
    private RongExtension extension;
    private EditText inputEditText;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = (RelativeLayout) super.onCreateView(inflater, container, savedInstanceState);
        extension = (RongExtension) containerView.findViewById(io.rong.imkit.R.id.rc_extension);
        inputEditText = extension.getInputEditText();
        return containerView;
    }

    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        return new ChatConversationAdapter(context);
    }
}
