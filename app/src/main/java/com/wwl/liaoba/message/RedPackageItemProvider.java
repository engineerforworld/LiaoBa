package com.wwl.liaoba.message;

import android.content.ClipboardManager;
import android.content.Context;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.wwl.liaoba.R;

import io.rong.imkit.RongIM;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.utilities.OptionsPopupDialog;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Message;

/**
 * Created by wangwenliang on 2017/11/9.
 */

@ProviderTag(messageContent = RedPackageMessage.class, showReadState = true)
public class RedPackageItemProvider extends IContainerItemProvider.MessageProvider<RedPackageMessage> {

    public RedPackageItemProvider() {
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.item_redpackage_message, null);
        ViewHolder holder = new ViewHolder();
        holder.mTitle = (TextView) view.findViewById(R.id.tv_redpkg_title);
        holder.mStatus = (TextView) view.findViewById(R.id.tv_redpkg_status);
        holder.mType = (TextView) view.findViewById(R.id.tv_redpkg_type);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, RedPackageMessage redPackageMessage, UIMessage message) {
        ViewHolder holder = (ViewHolder) view.getTag();
        if (message.getMessageDirection() == Message.MessageDirection.SEND) {
            //消息方向，自己发送的
            //holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_right);
        } else {
            //holder.message.setBackgroundResource(io.rong.imkit.R.drawable.rc_ic_bubble_left);
        }
        //AndroidEmoji.ensure((Spannable) holder.message.getText());//显示消息中的 Emoji 表情。
        holder.mTitle.setText(redPackageMessage.getTitle());
        holder.mStatus.setText(redPackageMessage.getStatus());
        holder.mType.setText(redPackageMessage.getType());
    }

    @Override
    public Spannable getContentSummary(RedPackageMessage redPackageMessage) {
        return new SpannableString(redPackageMessage.getStatus());
    }

    @Override
    public void onItemClick(View view, int i, RedPackageMessage redPackageMessage, UIMessage uiMessage) {

    }

    @Override
    public void onItemLongClick(final View view, int i, final RedPackageMessage redPackageMessage, final UIMessage uiMessage) {
        String[] items1;//复制，删除
        items1 = new String[]{view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_copy), view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_delete), view.getContext().getResources().getString(io.rong.imkit.R.string.rc_dialog_item_message_recall)};
        OptionsPopupDialog.newInstance(view.getContext(), items1).setOptionsPopupDialogListener(new OptionsPopupDialog.OnOptionsItemClickedListener() {
            public void onOptionsItemClicked(int which) {
                if (which == 0) {
                    ClipboardManager clipboard = (ClipboardManager) view.getContext().getSystemService(Context.CLIPBOARD_SERVICE);
                    clipboard.setText(redPackageMessage.getStatus());
                } else if (which == 1) {
                    RongIM.getInstance().deleteMessages(new int[]{uiMessage.getMessageId()}, (RongIMClient.ResultCallback) null);
                } else if (which == 2) {
                    RongIM.getInstance().recallMessage(uiMessage.getMessage(), "pushcongent");
                }
            }
        }).show();
    }

    private static class ViewHolder {
        TextView mTitle, mStatus, mType;
    }
}
