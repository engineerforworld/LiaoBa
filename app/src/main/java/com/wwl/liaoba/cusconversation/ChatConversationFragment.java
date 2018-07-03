package com.wwl.liaoba.cusconversation;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.Toast;

import io.rong.imkit.RongExtension;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.widget.AutoRefreshListView;
import io.rong.imkit.widget.adapter.MessageListAdapter;

/**
 * Created by wangwenliang on 2018/2/8.
 */

public class ChatConversationFragment extends ConversationFragment {
    private RelativeLayout containerView;
    private RongExtension extension;
    private EditText inputEditText;
    private View mMsgListView;
    private AutoRefreshListView mList;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        containerView = (RelativeLayout) super.onCreateView(inflater, container, savedInstanceState);
        mMsgListView = findViewById(containerView, io.rong.imkit.R.id.rc_layout_msg_list);
        extension = (RongExtension) containerView.findViewById(io.rong.imkit.R.id.rc_extension);
        mList = findViewById(mMsgListView, io.rong.imkit.R.id.rc_list);
        inputEditText = extension.getInputEditText();
        inputEditText.setHint("xxx");
        //extension.setExtensionClickListener
        return containerView;
    }

    @Override
    public void onResume() {
        super.onResume();
        //extension.setExtensionClickListener(null);
    }

    @Override
    public void onSendToggleClick(View v, String text) {
        super.onSendToggleClick(v, text);
        Toast.makeText(getActivity(), "发送", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPluginClicked(IPluginModule pluginModule, int position) {
        super.onPluginClicked(pluginModule, position);
        Toast.makeText(getActivity(), "plugin被点击", Toast.LENGTH_LONG).show();
    }

    @Override
    public void onPluginToggleClick(View v, ViewGroup extensionBoard) {
        super.onPluginToggleClick(v, extensionBoard);
        Toast.makeText(getActivity(), " + 被点击", Toast.LENGTH_LONG).show();
    }

    @Override
    public MessageListAdapter onResolveAdapter(Context context) {
        return new ChatConversationAdapter(context);
    }
}
