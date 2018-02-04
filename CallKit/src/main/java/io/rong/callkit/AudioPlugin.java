package io.rong.callkit;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v4.app.Fragment;
import android.widget.Toast;

import java.util.ArrayList;

import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.common.RLog;
import io.rong.imkit.RongExtension;
import io.rong.imkit.RongIM;
import io.rong.imkit.plugin.IPluginModule;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;

/**
 * Created by weiqinxiao on 16/8/16.
 */
public class AudioPlugin implements IPluginModule {
    private static final String TAG = "AudioPlugin";
    private ArrayList<String> allMembers;
    private Context context;

    private Conversation.ConversationType conversationType;
    private String targetId;

    @Override
    public Drawable obtainDrawable(Context context) {
        return context.getResources().getDrawable(R.drawable.rc_ic_phone_selector);
    }

    @Override
    public String obtainTitle(Context context) {
        return context.getString(R.string.rc_voip_audio);
    }

    @Override
    public void onClick(final Fragment currentFragment, final RongExtension extension) {
        String[] permissions = {Manifest.permission.RECORD_AUDIO};
        if (!PermissionCheckUtil.requestPermissions(currentFragment, permissions)) {
            return;
        }

        context = currentFragment.getActivity().getApplicationContext();
        conversationType = extension.getConversationType();
        targetId = extension.getTargetId();

        RongCallSession profile = RongCallClient.getInstance().getCallSession();
        if (profile != null && profile.getActiveTime() > 0) {
            Toast.makeText(context,
                    profile.getMediaType() == RongCallCommon.CallMediaType.AUDIO ?
                            currentFragment.getString(R.string.rc_voip_call_audio_start_fail) :
                            currentFragment.getString(R.string.rc_voip_call_video_start_fail),
                    Toast.LENGTH_SHORT)
                    .show();
            return;
        }
        ConnectivityManager cm = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = cm.getActiveNetworkInfo();
        if (networkInfo == null || !networkInfo.isConnected() || !networkInfo.isAvailable()) {
            Toast.makeText(context, currentFragment.getString(R.string.rc_voip_call_network_error), Toast.LENGTH_SHORT).show();
            return;
        }

        if (conversationType.equals(Conversation.ConversationType.PRIVATE)) {
            Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_SINGLEAUDIO);
            intent.putExtra("conversationType", conversationType.getName().toLowerCase());
            intent.putExtra("targetId", targetId);
            intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.setPackage(context.getPackageName());
            context.getApplicationContext().startActivity(intent);
        } else if (conversationType.equals(Conversation.ConversationType.DISCUSSION)) {
            RongIM.getInstance().getDiscussion(targetId, new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    Intent intent = new Intent(context, CallSelectMemberActivity.class);
                    allMembers = (ArrayList<String>) discussion.getMemberIdList();
                    intent.putStringArrayListExtra("allMembers", allMembers);
                    intent.putExtra("conversationType", conversationType.getValue());
                    String myId = RongIMClient.getInstance().getCurrentUserId();
                    ArrayList<String> invited = new ArrayList<>();
                    invited.add(myId);
                    intent.putStringArrayListExtra("invitedMembers", invited);
                    intent.putExtra("mediaType", RongCallCommon.CallMediaType.AUDIO.getValue());
                    extension.startActivityForPluginResult(intent, 110, AudioPlugin.this);
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {
                    RLog.d(TAG, "get discussion errorCode = " + e.getValue());
                }
            });
        } else if (conversationType.equals(Conversation.ConversationType.GROUP)) {
            Intent intent = new Intent(context, CallSelectMemberActivity.class);
            String myId = RongIMClient.getInstance().getCurrentUserId();
            ArrayList<String> invited = new ArrayList<>();
            invited.add(myId);
            intent.putStringArrayListExtra("invitedMembers", invited);
            intent.putExtra("conversationType", conversationType.getValue());
            intent.putExtra("groupId", targetId);
            intent.putExtra("mediaType", RongCallCommon.CallMediaType.AUDIO.getValue());
            extension.startActivityForPluginResult(intent, 110, this);
        }
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode != Activity.RESULT_OK) {
            return;
        }

        Intent intent = new Intent(RongVoIPIntent.RONG_INTENT_ACTION_VOIP_MULTIAUDIO);
        ArrayList<String> userIds = data.getStringArrayListExtra("invited");
        userIds.add(RongIMClient.getInstance().getCurrentUserId());
        intent.putExtra("conversationType", conversationType.getName().toLowerCase());
        intent.putExtra("targetId", targetId);
        intent.putExtra("callAction", RongCallAction.ACTION_OUTGOING_CALL.getName());
        intent.putStringArrayListExtra("invitedUsers", userIds);
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage(context.getPackageName());
        context.getApplicationContext().startActivity(intent);
    }
}
