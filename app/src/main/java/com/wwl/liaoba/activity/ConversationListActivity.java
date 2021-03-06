package com.wwl.liaoba.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.wwl.liaoba.R;
import com.wwl.liaoba.Utils.Constants;
import com.wwl.liaoba.message.RedPackageMessage;

import java.util.List;

import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.manager.IUnReadMessageObserver;
import io.rong.imkit.userInfoCache.RongUserInfoManager;
import io.rong.imlib.IRongCallback;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.UserInfo;
import io.rong.message.InformationNotificationMessage;
import io.rong.message.TextMessage;

public class ConversationListActivity extends FragmentActivity implements View.OnClickListener {
    private int insertMessageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_conversation_list);
        enterFragment();
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_list_insertmsg:
                insertMessage();
                break;
            case R.id.tv_list_sendmsg:
                sendMessage();
                break;
            case R.id.tv_unread_msg:
                getUnreadMessageCount();
                break;
            case R.id.tv_unread_listener:
                addUnreaMessageListener();
                break;
            case R.id.tv_conversation_list:
                getConversationList();
                break;
            case R.id.tv_conversation:
                getConversation();
                break;
            case R.id.btn_list_back:
                finish();
                break;
            case R.id.btn_list_sleect:
                Intent intent = new Intent(this, SelectConversationActivity.class);
                startActivity(intent);
                break;
            case R.id.tv_message_list:
                getMessageList();
                break;
            case R.id.tv_rmeote_message_list:
                getRemoteMessageList();
                break;
            default:
                break;
        }

    }

    private void getMessageList() {
        Log.i("www", "getMessageList()");
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(RongIMClient.getInstance().getCurrentUserId());
        String targetId = null;
//        if (Constants.userid_6.equals(userInfo.getUserId())) {
//            targetId = Constants.userid_7;
//        } else {
//            targetId = Constants.userid_6;
//        }
        targetId = Constants.userid_6;
        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.PRIVATE, targetId, -1, 30, new RongIMClient.ResultCallback<List<io.rong.imlib.model.Message>>() {
            @Override
            public void onSuccess(List<io.rong.imlib.model.Message> messages) {
                if (messages != null) {
                    for (int i = 0; i < messages.size(); i++) {
                        Log.i("www", "SenderUserI:" + messages.get(i).getSenderUserId() + "----" + "TargetId" + messages.get(i).getTargetId());
                    }
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
            }
        });
    }

    private void getRemoteMessageList() {
        Log.i("www", "getRemoteMessageList()");
        UserInfo userInfo = RongUserInfoManager.getInstance().getUserInfo(RongIMClient.getInstance().getCurrentUserId());
        String targetId = null;
//        if (Constants.userid_6.equals(userInfo.getUserId())) {
//            targetId = Constants.userid_7;
//        } else {
//            targetId = Constants.userid_6;
//        }
        targetId = Constants.userid_6;
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE, targetId, 0, 10, new RongIMClient.ResultCallback<List<io.rong.imlib.model.Message>>() {
            @Override
            public void onSuccess(List<io.rong.imlib.model.Message> messages) {
                if (messages != null) {
                    for (int i = 0; i < messages.size(); i++) {
                        Log.i("www", "SenderUserI:" + messages.get(i).getSenderUserId() + "----" + "TargetId" + messages.get(i).getTargetId());
                    }
                }

            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
            }
        });
    }


    public void getConversation() {
        RongIMClient.getInstance().getConversationListByPage(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                Log.i("ss", conversations.toString());
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        }, 0, 20, Conversation.ConversationType.PRIVATE);

    }

    public void insertMessage() {
        RedPackageMessage redPackageMessage = RedPackageMessage.obtain("biaoti", "sender", "微信红包sss");
        //TextMessage textMsg = TextMessage.obtain("这是插入的消息" + insertMessageCount);
        //insertMessageCount++;
        long time = System.currentTimeMillis();
//        RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, "001", Message.SentStatus.SENT, redPackageMessage, time, new RongIMClient.ResultCallback<Message>() {
//            @Override
//            public void onSuccess(Message message) {
//
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//
//            }
//        });

        Message message = Message.obtain("001", Conversation.ConversationType.PRIVATE, redPackageMessage);
        RongIM.getInstance().sendMessage(message, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

            }
        });

//
//        RongIM.getInstance().setConversationNotificationStatus(Conversation.ConversationType.PRIVATE, "003", Conversation.ConversationNotificationStatus.DO_NOT_DISTURB, new RongIMClient.ResultCallback<Conversation.ConversationNotificationStatus>() {
//            @Override
//            public void onSuccess(Conversation.ConversationNotificationStatus conversationNotificationStatus) {
//                if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.DO_NOT_DISTURB) {
////                    NToast.shortToast(context, "设置免打扰成功");
//                } else if (conversationNotificationStatus == Conversation.ConversationNotificationStatus.NOTIFY) {
////                    NToast.shortToast(context, "取消免打扰成功");
//                }
//
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//
//            }
//        });
    }

    public void sendMessage() {
        TextMessage textMessage = TextMessage.obtain("我是消息内容");
        final InformationNotificationMessage informationNotificationMessage = InformationNotificationMessage.obtain("小灰条");
        informationNotificationMessage.setExtra("拓展字段");
        //RedPackageMessage redPackageMessage = RedPackageMessage.obtain("红包","status","title");
        RongIMClient.getInstance().sendMessage(Conversation.ConversationType.PRIVATE, "002", informationNotificationMessage, null, null, new IRongCallback.ISendMessageCallback() {
            @Override
            public void onAttached(Message message) {
                // 消息成功存到本地数据库的回调
                Toast.makeText(ConversationListActivity.this, "消息成功存到本地数据库的回调", Toast.LENGTH_LONG).show();
            }

            @Override
            public void onSuccess(Message message) {
                // 消息发送成功的回调
                InformationNotificationMessage informationNotificationMessage1 = (InformationNotificationMessage) (message.getContent());
                informationNotificationMessage.getExtra();
                Toast.makeText(ConversationListActivity.this, "消息发送成功的回调", Toast.LENGTH_LONG).show();
                enterFragment();
            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {
                // 消息发送失败的回调
                Toast.makeText(ConversationListActivity.this, "消息发送失败的回调", Toast.LENGTH_LONG).show();

            }
        });
    }


    private void getUnreadMessageCount() {
        Conversation.ConversationType[] conversationTypes = new Conversation.ConversationType[1];
        conversationTypes[0] = Conversation.ConversationType.PRIVATE;
        RongIM.getInstance().getUnreadCount(conversationTypes, new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {
                Toast.makeText(ConversationListActivity.this, String.valueOf(integer), Toast.LENGTH_LONG).show();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

        RongIM.getInstance().getTotalUnreadCount(new RongIMClient.ResultCallback<Integer>() {
            @Override
            public void onSuccess(Integer integer) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

//        RongIM.getInstance().getUnreadCount(Conversation.ConversationType.SYSTEM, "001", new RongIMClient.ResultCallback<Integer>() {
//            @Override
//            public void onSuccess(Integer integer) {
//                Toast.makeText(ConversationListActivity.this, String.valueOf(integer), Toast.LENGTH_LONG).show();
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//                Log.i("ddd", errorCode.getMessage());
//
//            }
//        });
    }

    public void addUnreaMessageListener() {
        Conversation.ConversationType[] conversationTypes = new Conversation.ConversationType[5];
        conversationTypes[0] = Conversation.ConversationType.SYSTEM;
        conversationTypes[1] = Conversation.ConversationType.PRIVATE;
        conversationTypes[2] = Conversation.ConversationType.APP_PUBLIC_SERVICE;
        conversationTypes[3] = Conversation.ConversationType.CUSTOMER_SERVICE;
        conversationTypes[4] = Conversation.ConversationType.GROUP;
        RongIM.getInstance().addUnReadMessageCountChangedObserver(new IUnReadMessageObserver() {
            @Override
            public void onCountChanged(int i) {
                Toast.makeText(ConversationListActivity.this, String.valueOf(i), Toast.LENGTH_LONG).show();
            }
        }, conversationTypes);
    }

    private void getConversationList() {
        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                for (int j = 0; j < conversations.size(); j++) {
                    String conversationTitle = conversations.get(j).getConversationTitle();
                }
                //讨论组id
                String si = conversations.get(0).getTargetId();
                Conversation.ConversationType type = conversations.get(0).getConversationType();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    /**
     * 静态加载 会话列表 ConversationListFragment
     */
    private void enterFragment() {
        ConversationListFragment fragment = (ConversationListFragment) getSupportFragmentManager()
                .findFragmentById(R.id.conversationlist);
        Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon().appendPath("conversationlist")
                .appendQueryParameter(Conversation.ConversationType.PRIVATE.getName(), "false") // 设置私聊会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.GROUP.getName(), "false")// 设置群组会话聚合显示
                .appendQueryParameter(Conversation.ConversationType.DISCUSSION.getName(), "false")// 设置讨论组会话非聚合显示
                .appendQueryParameter(Conversation.ConversationType.SYSTEM.getName(), "false")// 设置系统会话非聚合显示
                .build();
        fragment.setUri(uri);
    }
}
