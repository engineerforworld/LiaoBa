package com.wwl.liaoba.activity;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.view.View;
import android.widget.TextView;

import com.jrmf360.rylib.JrmfClient;
import com.wwl.liaoba.Extension.LiaoBaExtensionModule;
import com.wwl.liaoba.R;
import com.wwl.liaoba.Utils.AppSharePreferenceMgr;
import com.wwl.liaoba.message.BurnAfterReadMessage;

import java.util.List;
import java.util.Locale;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.fragment.ConversationFragment;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.Message;
import io.rong.message.TextMessage;

public class ConversationActivity extends FragmentActivity {
    private TextView mTextView;
    private boolean flag;
    private String mTargetId;
    private String title;
    private Conversation.ConversationType mConversationType;
    private int insertMessageCount = 0;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //动态设置plugin，需要在setContentView方法之前调用
        //setExtensionPlugin();
        setContentView(R.layout.activity_conversation_fragment);
        Intent intent = getIntent();
        if (intent == null || intent.getData() == null) {
            return;
        }
        flag = isPushMessage(intent);
        mTextView = (TextView) findViewById(R.id.tv_conversation_user);
        mTargetId = intent.getData().getQueryParameter("targetId");
        mConversationType = Conversation.ConversationType
                .valueOf(intent.getData().getLastPathSegment().toUpperCase(Locale.getDefault()));
        title = intent.getData().getQueryParameter("title");
        mTextView.setText(title);
    }

    public void setExtensionPlugin() {
        Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(getIntent().getData().getLastPathSegment().toUpperCase(Locale.US));
        if (conversationType.getName().equals(Conversation.ConversationType.PRIVATE.getName())) {
            List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
            RongExtensionManager.getInstance().unregisterExtensionModule(moduleList.get(0));
            RongExtensionManager.getInstance().registerExtensionModule(new DefaultExtensionModule());
        } else if (conversationType.getName().equals(Conversation.ConversationType.GROUP.getName())) {
            List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
            RongExtensionManager.getInstance().unregisterExtensionModule(moduleList.get(0));
            RongExtensionManager.getInstance().registerExtensionModule(new LiaoBaExtensionModule());
        }
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.tv_conversation_insert:
                insertMessage();
                break;
            case R.id.tv_conversation_sendpackage:
                JrmfClient.intentWallet(ConversationActivity.this);
                break;
            case R.id.tv_chatroom_info:
                getDiscussInfo();
                break;
            case R.id.tv_history_msg:
                getHistotyMessage();
                break;
            case R.id.tv_yuehoujif:
                sendBurnDownMessage();
                break;
            case R.id.tv_base64:
                convertBase64ToImage();
                break;
            default:
                break;
        }

    }

    private void convertBase64ToImage() {

    }

    public void sendBurnDownMessage() {
        Uri mTakePictureUri = Uri.parse("file:///storage/emulated/0/Download/53da008c6bc8b3c78fe2901cd1fa0d2a.png");
        BurnAfterReadMessage burnAfterReadMessage = BurnAfterReadMessage.obtain(mTakePictureUri, mTakePictureUri, true);
        RongIM.getInstance().sendImageMessage(Conversation.ConversationType.PRIVATE, "003", burnAfterReadMessage, "阅后即焚", "阅后即焚", new RongIMClient.SendImageMessageCallback() {
            @Override
            public void onAttached(Message message) {

            }

            @Override
            public void onError(Message message, RongIMClient.ErrorCode errorCode) {

            }

            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onProgress(Message message, int i) {

            }
        });
    }

    public void insertMessage() {
        TextMessage textMsg = TextMessage.obtain("这是插入的消息" + insertMessageCount);
        insertMessageCount++;
        long time = System.currentTimeMillis();
        RongIM.getInstance().insertOutgoingMessage(Conversation.ConversationType.PRIVATE, "001", Message.SentStatus.SENT, textMsg, time, new RongIMClient.ResultCallback<Message>() {
            @Override
            public void onSuccess(Message message) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    private void getDiscussInfo() {
        RongIM.getInstance().getDiscussion("discussId", new RongIMClient.ResultCallback<Discussion>() {
            @Override
            public void onSuccess(Discussion discussion) {
                //群成员
                discussion.getMemberIdList();
                //创建者
                discussion.getCreatorId();
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });

    }

    public void getHistotyMessage() {
//        RongIMClient.getInstance().getHistoryMessages(Conversation.ConversationType.CUSTOMER_SERVICE, "001", "www", 1, 20, new RongIMClient.ResultCallback<List<Message>>() {
//            @Override
//            public void onSuccess(List<Message> messages) {
//                for (int j = 0; j < messages.size(); j++) {
//                    messages.get(j).getMessageDirection();
//                }
//
//            }
//
//            @Override
//            public void onError(RongIMClient.ErrorCode errorCode) {
//
//            }
//        });
        RongIMClient.getInstance().getRemoteHistoryMessages(Conversation.ConversationType.PRIVATE, "003", 0, 40, new RongIMClient.ResultCallback<List<Message>>() {
            @Override
            public void onSuccess(List<Message> messages) {

            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }

    /**
     * 判断是否是 Push 消息，判断是否需要做 connect 操作
     *
     * @param intent
     */
    private boolean isPushMessage(Intent intent) {
        // 通过intent.getData().getQueryParameter("push") 为true，判断是否是push消息
        if ("true".equals(intent.getData().getQueryParameter("isFromPush"))) {
            // 只有收到系统消息和不落地 push 消息的时候，pushId 不为 null。而且这两种消息只能通过 server来发送，客户端发送不了。
            String id = intent.getData().getQueryParameter("pushId");
            RongIMClient.getInstance().recordNotificationEvent(id);
            enterActivity();
            return true;
        } else {
            //通知过来程序切到后台，收到消息后点击进入,会执行这里
            if (RongIM.getInstance() == null || RongIM.getInstance().getRongIMClient() == null) {
                enterActivity();
                return true;
            }
        }
        return false;
    }

    private void enterActivity() {
        String isLogin = (String) AppSharePreferenceMgr.get(this, "isLogin", "n");
        if ("n".equals(isLogin)) {
            Intent intent1 = new Intent(ConversationActivity.this, LoginActivity.class);
            intent1.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent1);
        } else {
            ConversationFragment fragment = (ConversationFragment) getSupportFragmentManager()
                    .findFragmentById(R.id.conversation);
            Uri uri = Uri.parse("rong://" + getApplicationInfo().packageName).buildUpon()
                    .appendPath("conversation").appendPath(mConversationType.getName().toLowerCase())
                    .appendQueryParameter("targetId", mTargetId).build();
            fragment.setUri(uri);
        }
    }
}
