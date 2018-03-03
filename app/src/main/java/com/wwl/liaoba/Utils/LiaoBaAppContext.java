package com.wwl.liaoba.Utils;

import android.content.Context;
import android.util.Log;
import android.view.View;

import com.wwl.liaoba.Extension.LiaoBaExtensionModule;
import com.wwl.liaoba.application.LiaoBaApp;
import com.wwl.liaoba.message.RedPackageItemProvider;
import com.wwl.liaoba.message.RedPackageMessage;

import java.util.List;

import io.rong.imkit.DefaultExtensionModule;
import io.rong.imkit.IExtensionModule;
import io.rong.imkit.RongExtensionManager;
import io.rong.imkit.RongIM;
import io.rong.imkit.model.GroupUserInfo;
import io.rong.imkit.model.UIConversation;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Group;
import io.rong.imlib.model.Message;
import io.rong.imlib.model.MessageContent;
import io.rong.imlib.model.UserInfo;
import io.rong.message.ContactNotificationMessage;
import io.rong.message.GroupNotificationMessage;
import io.rong.message.ImageMessage;
import io.rong.message.TextMessage;

/**
 * 融云相关监听 事件集合类
 */
public class LiaoBaAppContext implements
        RongIM.ConversationListBehaviorListener,
        RongIMClient.OnReceiveMessageListener,
        RongIM.UserInfoProvider,
        RongIM.GroupInfoProvider,
        RongIM.GroupUserInfoProvider,
        RongIM.LocationProvider,
        RongIMClient.ConnectionStatusListener,
        RongIM.ConversationClickListener,
        RongIM.IGroupMembersProvider {

    private Context mContext;
    private static LiaoBaAppContext mRongCloudInstance;
    private RongIM.LocationProvider.LocationCallback mLastLocationCallback;

    public LiaoBaAppContext(Context mContext) {
        this.mContext = mContext;
        initListener();
        UserInfoManager.init(mContext);
    }

    public static void init(Context context) {
        if (mRongCloudInstance == null) {
            synchronized (LiaoBaApp.class) {
                if (mRongCloudInstance == null) {
                    mRongCloudInstance = new LiaoBaAppContext(context);
                }
            }
        }
    }

    public static LiaoBaAppContext getInstance() {
        return mRongCloudInstance;
    }

    public Context getContext() {
        return mContext;
    }

    /**
     * init 后就能设置的监听
     */
    private void initListener() {
        registerMessageType();//注册消息模板
        registerExtensionPlugin();//注册plugin
        setReadReceiptConversationType();//设置已读消息回执的会话类型
        RongIM.setConversationListBehaviorListener(this);//设置会话列表界面操作的监听器
        RongIM.setConversationClickListener(this);//设置会话页面操作监听
        RongIM.setConnectionStatusListener(this);// 设置连接状态监听器
        RongIM.setUserInfoProvider(this, true);//设置用户信息提供者
        RongIM.setGroupInfoProvider(this, true);//设置群组信息提供者
        RongIM.setLocationProvider(this);//设置地理位置提供者,不用位置的同学可以注掉此行代码
        RongIM.setOnReceiveMessageListener(this);//设置消息到达监听
        RongIM.getInstance().enableNewComingMessageIcon(true);
        RongIM.getInstance().enableUnreadMessageIcon(true);
        RongIM.getInstance().setGroupMembersProvider(this);
    }

    private void registerMessageType() {
        RongIM.registerMessageType(RedPackageMessage.class);
        RongIM.registerMessageTemplate(new RedPackageItemProvider());
    }

    private void registerExtensionPlugin() {
        List<IExtensionModule> moduleList = RongExtensionManager.getInstance().getExtensionModules();
        IExtensionModule defaultModule = null;
        if (moduleList != null) {
            for (IExtensionModule module : moduleList) {
                if (module instanceof DefaultExtensionModule) {
                    defaultModule = module;
                    break;
                }
            }
            if (defaultModule != null) {
                RongExtensionManager.getInstance().unregisterExtensionModule(defaultModule);
                RongExtensionManager.getInstance().registerExtensionModule(new LiaoBaExtensionModule());
            }
        }
    }

    private void setReadReceiptConversationType() {
        Conversation.ConversationType[] types = new Conversation.ConversationType[]{
                Conversation.ConversationType.PRIVATE,
                Conversation.ConversationType.GROUP,
                Conversation.ConversationType.DISCUSSION
        };
        RongIM.getInstance().setReadReceiptConversationTypeList(types);
    }


    @Override
    public boolean onConversationPortraitClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationPortraitLongClick(Context context, Conversation.ConversationType conversationType, String s) {
        return false;
    }

    @Override
    public boolean onConversationLongClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    @Override
    public boolean onConversationClick(Context context, View view, UIConversation uiConversation) {
        return false;
    }

    /**
     * @param message 收到的消息实体。
     * @param i
     * @return
     */
    @Override
    public boolean onReceived(Message message, int i) {
        MessageContent messageContent = message.getContent();
        if (messageContent instanceof ContactNotificationMessage) {

        } else if (messageContent instanceof GroupNotificationMessage) {

        } else if (messageContent instanceof ImageMessage) {

        } else if (messageContent instanceof TextMessage) {
            TextMessage textMessage = (TextMessage) messageContent;
            Log.i("Rong", "onReceived收到的消息----" + message.getSenderUserId() + "---" + textMessage.getContent());
        }
        return false;
    }

    @Override
    public UserInfo getUserInfo(String s) {
        return UserInfoManager.getInstance().getUserInfo(s);
    }

    @Override
    public Group getGroupInfo(String s) {
        UserInfoManager.getInstance().getGroupInfo(s);
        return null;
    }

    @Override
    public GroupUserInfo getGroupUserInfo(String groupId, String userId) {
        //return GroupUserInfoEngine.getInstance(mContext).startEngine(groupId, userId);
        return null;
    }


    @Override
    public void onStartLocation(Context context, LocationCallback locationCallback) {
        //demo 代码  开发者需替换成自己的代码
    }

    @Override
    public boolean onUserPortraitClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        if (conversationType == Conversation.ConversationType.CUSTOMER_SERVICE || conversationType == Conversation.ConversationType.PUBLIC_SERVICE || conversationType == Conversation.ConversationType.APP_PUBLIC_SERVICE) {
            return false;
        }
        return false;
    }

    @Override
    public boolean onUserPortraitLongClick(Context context, Conversation.ConversationType conversationType, UserInfo userInfo, String s) {
        return false;
    }

    @Override
    public boolean onMessageClick(final Context context, final View view, final Message message) {
        //demo 代码  开发者需替换成自己的代码
//        if (message.getContent() instanceof ImageMessage) {
//            Intent intent = new Intent(context, SelectConversationActivity.class);
//            intent.putExtra("message", message);
//            context.startActivity(intent);
//        }
        return false;
    }


    private void startRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {

    }

    private void joinRealTimeLocation(Context context, Conversation.ConversationType conversationType, String targetId) {

    }

    @Override
    public boolean onMessageLinkClick(Context context, String s, Message message) {
        return false;
    }

    @Override
    public boolean onMessageLongClick(Context context, View view, Message message) {
        return false;
    }


    public RongIM.LocationProvider.LocationCallback getLastLocationCallback() {
        return mLastLocationCallback;
    }

    public void setLastLocationCallback(RongIM.LocationProvider.LocationCallback lastLocationCallback) {
        this.mLastLocationCallback = lastLocationCallback;
    }

    @Override
    public void onChanged(ConnectionStatus connectionStatus) {
        if (connectionStatus.equals(ConnectionStatus.KICKED_OFFLINE_BY_OTHER_CLIENT)) {
            //quit(true);
        }
    }

    @Override
    public void getGroupMembers(String groupId, final RongIM.IGroupMemberCallback callback) {

    }
}
