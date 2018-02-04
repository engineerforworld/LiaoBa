package com.wwl.liaoba.activity;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.TextView;

import com.wwl.liaoba.R;
import com.wwl.liaoba.Utils.UserInfoManager;
import com.wwl.liaoba.adapter.ConversationSelectAdapter;
import com.wwl.liaoba.bean.User;

import java.util.ArrayList;

import io.rong.imkit.RongIM;


public class SelectConversationActivity extends AppCompatActivity {
    private ListView mListView;
    private TextView chatRoom1;
    private TextView chatRoom2;
    private TextView groupChat;
    private ConversationSelectAdapter mAdapter;
    private ArrayList<User> mList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        setContentView(R.layout.activity_conversation_select);
        initView();
        setListener();
        setUserList();
    }

    private void initView() {
        mListView = (ListView) findViewById(R.id.lv_userlist);
        chatRoom1 = (TextView) findViewById(R.id.chat_room1);
        chatRoom2 = (TextView) findViewById(R.id.chat_room2);
        groupChat = (TextView) findViewById(R.id.chat_group);
    }

    private void setUserList() {
        mList = UserInfoManager.getInstance().getUserList();
        mAdapter = new ConversationSelectAdapter(mList, this);
        mListView.setAdapter(mAdapter);
    }

    private void setListener() {
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                RongIM.getInstance().startPrivateChat(SelectConversationActivity.this, mList.get(i).getUserId(), mList.get(i).getUserName());
            }
        });

        chatRoom1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIM.getInstance().startChatRoomChat(SelectConversationActivity.this, "001", true);
            }
        });
        chatRoom2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIM.getInstance().startChatRoomChat(SelectConversationActivity.this, "002", true);
            }
        });
        groupChat.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RongIM.getInstance().startGroupChat(SelectConversationActivity.this, "001", "一号群聊");
            }
        });
    }
}
