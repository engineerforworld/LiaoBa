package com.wwl.liaoba.message;

import android.content.Context;
import android.content.Intent;
import android.text.Spannable;
import android.text.SpannableString;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import com.wwl.liaoba.R;

import io.rong.imkit.RongKitIntent;
import io.rong.imkit.model.ProviderTag;
import io.rong.imkit.model.UIMessage;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imkit.widget.provider.IContainerItemProvider;
import io.rong.imlib.model.Message;

/**
 * <p>Description:
 * <p>Company：
 * <p>Email:bjxm2013@163.com
 * <p>Created by Devin Sun on 2018/3/21.
 */
@ProviderTag(
        messageContent = BurnAfterReadMessage.class,
        showProgress = false,
        showReadState = true
)
public class BurnAfterReadMessageProvider extends IContainerItemProvider.MessageProvider<BurnAfterReadMessage> {

    private static class ViewHolder {
        AsyncImageView img;
        TextView message;

        private ViewHolder() {
        }
    }

    @Override
    public View newView(Context context, ViewGroup viewGroup) {
        View view = LayoutInflater.from(context).inflate(R.layout.rc_item_image_message, (ViewGroup) null);
        ViewHolder holder = new ViewHolder();
        holder.message = (TextView) view.findViewById(R.id.rc_msg);
        holder.img = (AsyncImageView) view.findViewById(R.id.rc_img);
        view.setTag(holder);
        return view;
    }

    @Override
    public void bindView(View view, int i, BurnAfterReadMessage messageContent, UIMessage uiMessage) {
        ViewHolder holder = (ViewHolder) view.getTag();

        boolean isOpenBigImg = uiMessage.getReceivedStatus().isDownload();

        if (uiMessage.getMessageDirection() == Message.MessageDirection.SEND) {
//            view.setBackgroundResource(R.drawable.rc_ic_bubble_no_right);
            holder.img.setImageResource(isOpenBigImg ? R.drawable.ic_launcher : R.drawable.ic_launcher);
        } else {
//            view.setBackgroundResource(R.drawable.rc_ic_bubble_no_left);
            holder.img.setImageResource(isOpenBigImg ? R.drawable.ic_launcher : R.drawable.ic_launcher);
        }

//        holder.img.setResource(messageContent.getThumUri());
        int progress = uiMessage.getProgress();
        Message.SentStatus status = uiMessage.getSentStatus();
        if (status.equals(Message.SentStatus.SENDING) && progress < 100) {
            holder.message.setText(progress + "%");
            holder.message.setVisibility(View.VISIBLE);
        } else {
            holder.message.setVisibility(View.GONE);
        }

    }

    @Override
    public Spannable getContentSummary(BurnAfterReadMessage messageContent) {
        return new SpannableString("[阅后即焚]");
    }

    @Override
    public void onItemClick(View view, int i, BurnAfterReadMessage messageContent, UIMessage uiMessage) {

//        if (uiMessage.getReceivedStatus().isDownload()) {
//            return;
//        }
//
//        Message.SentStatus status = uiMessage.getSentStatus();
//        if (!status.equals(Message.SentStatus.SENDING) && !status.equals(Message.SentStatus.FAILED)) {
//            //ShowBurnImageActivity.startShowBurnImageActivity(view.getContext(), uiMessage.getMessage());
//        }
        if (uiMessage != null) {
            Intent intent = new Intent(RongKitIntent.RONG_INTENT_ACTION_PICTUREPAGERVIEW);
            intent.setPackage(view.getContext().getPackageName());
            intent.putExtra("message", uiMessage.getMessage());
            view.getContext().startActivity(intent);
        }
    }
}
