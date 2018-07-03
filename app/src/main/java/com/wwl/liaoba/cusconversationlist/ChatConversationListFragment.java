package com.wwl.liaoba.cusconversationlist;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

import io.rong.imkit.fragment.ConversationListFragment;
import io.rong.imkit.fragment.IHistoryDataResultCallback;
import io.rong.imkit.widget.adapter.ConversationListAdapter;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;

public class ChatConversationListFragment extends ConversationListFragment {

    @Override
    public ConversationListAdapter onResolveAdapter(Context context) {
        return new ChatConversationListAdapter(context);
    }

    @Override
    public void getConversationList(Conversation.ConversationType[] conversationTypes, final IHistoryDataResultCallback<List<Conversation>> callback) {
        //super.getConversationList(conversationTypes, callback);
        RongIMClient.getInstance().getConversationList(new RongIMClient.ResultCallback<List<Conversation>>() {
            @Override
            public void onSuccess(List<Conversation> conversations) {
                if (callback != null) {
                    List<Conversation> resultConversations = new ArrayList<>();
                    if (conversations != null) {
                        for (Conversation conversation : conversations) {
                            if (!shouldFilterConversation(conversation.getConversationType(), conversation.getTargetId())) {
                                resultConversations.add(conversation);
                            }

                        }
                    }
                    //根据条件删除指定的会话
                    if (resultConversations != null) {
                        for (int i = 0; i < resultConversations.size(); i++) {
                            if (conversations.get(i).getConversationType().equals(Conversation.ConversationType.GROUP)) {
                                resultConversations.remove(i);
                            }
                        }
                    }

                    callback.onResult(resultConversations);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode e) {
                if (callback != null) {
                    callback.onError();
                }
            }
        }, conversationTypes);
    }
}
