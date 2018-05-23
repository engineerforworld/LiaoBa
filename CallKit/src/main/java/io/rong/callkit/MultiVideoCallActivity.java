package io.rong.callkit;

import android.Manifest;
import android.annotation.SuppressLint;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.net.http.SslError;
import android.os.Build;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.SslErrorHandler;
import android.webkit.WebSettings;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.bailingcloud.bailingvideo.engine.view.BlinkVideoView;
import com.blink.RendererCommon;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import io.rong.calllib.CallUserProfile;
import io.rong.calllib.RongCallClient;
import io.rong.calllib.RongCallCommon;
import io.rong.calllib.RongCallSession;
import io.rong.calllib.message.MultiCallEndMessage;
import io.rong.common.RLog;
import io.rong.imkit.RongContext;
import io.rong.imkit.RongIM;
import io.rong.imkit.utilities.PermissionCheckUtil;
import io.rong.imkit.widget.AsyncImageView;
import io.rong.imlib.RongIMClient;
import io.rong.imlib.model.Conversation;
import io.rong.imlib.model.Discussion;
import io.rong.imlib.model.UserInfo;

/**
 * <a href="http://support.rongcloud.cn/kb/Njcy">如何实现不基于于群组的voip</a>
 */
public class MultiVideoCallActivity extends BaseCallActivity {
    private static final String TAG = "VoIPMultiVideoCallActivity";
    RongCallSession callSession;
    SurfaceView localView;
    ContainerLayout localViewContainer;
    LinearLayout remoteViewContainer;
    LinearLayout remoteViewContainer1;
    LinearLayout remoteViewContainer2;
    LinearLayout topContainer;
    LinearLayout waitingContainer;
    LinearLayout bottomButtonContainer;
    LinearLayout participantPortraitContainer;
    LinearLayout portraitContainer1;
    LinearLayout portraitContainer2;
    LayoutInflater inflater;
    ImageView minimizeButton;
    ImageView moreButton;
    ImageView switchCameraButton;
    AsyncImageView userPortrait;
    LinearLayout infoLayout;
    ImageView signalView;
    TextView userNameView;
    private WebView whiteboardView;
    private RelativeLayout mRelativeWebView;
    int remoteUserViewWidth;

    boolean isFullScreen = false;
    boolean isMuteMIC = false;
    boolean isMuteCamera = false;
    boolean startForCheckPermissions = false;

    String localViewUserId;
    private CallOptionMenu optionMenu;
    ImageView muteButtion;
    ImageView disableCameraButtion;
    CallPromptDialog dialog = null;
    RelativeLayout observerLayout;

    @Override
    @TargetApi(23)
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (savedInstanceState != null && RongCallClient.getInstance() == null) {
            // 音视频请求权限时，用户在设置页面取消权限，导致应用重启，退出当前activity.
            finish();
            return;
        }
        setContentView(R.layout.rc_voip_multi_video_call);

        Intent intent = getIntent();
        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        if (requestCallPermissions(RongCallCommon.CallMediaType.VIDEO, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
            initViews();
            setupIntent();
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {
        startForCheckPermissions = intent.getBooleanExtra("checkPermissions", false);
        super.onNewIntent(intent);
        if (requestCallPermissions(RongCallCommon.CallMediaType.VIDEO, REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS)) {
            initViews();
            setupIntent();
        }
    }

    @TargetApi(23)
    @Override
    public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
        switch (requestCode) {
            case REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS:
                Map<String, Integer> mapPermissions = new HashMap<>();
                mapPermissions.put(Manifest.permission.RECORD_AUDIO, PackageManager.PERMISSION_GRANTED);
                mapPermissions.put(Manifest.permission.CAMERA, PackageManager.PERMISSION_GRANTED);

                for (int i = 0; i < permissions.length; i++) {
                    mapPermissions.put(permissions[i], grantResults[i]);
                }
                if (mapPermissions.get(Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED
                        && mapPermissions.get(Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
                        RongCallClient.getInstance().onPermissionGranted();
                    } else {
                        initViews();
                        setupIntent();
                    }
                } else {
                    if (startForCheckPermissions) {
                        startForCheckPermissions = false;
                        Toast.makeText(this, "打设置相关权限", Toast.LENGTH_SHORT).show();
                        RongCallClient.getInstance().onPermissionDenied();
                    } else {
                        finish();
                    }
                }
                break;

            default:
                super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == REQUEST_CODE_ASK_MULTIPLE_PERMISSIONS) {
            if (PermissionCheckUtil.checkPermissions(this, VIDEO_CALL_PERMISSIONS)) {
                if (startForCheckPermissions) {
                    startForCheckPermissions = false;
                    RongCallClient.getInstance().onPermissionGranted();
                } else {
                    initViews();
                    setupIntent();
                }
            } else {
                if (startForCheckPermissions) {
                    startForCheckPermissions = false;
                    RongCallClient.getInstance().onPermissionDenied();
                } else {
                    finish();
                }
            }

        } else if (requestCode == REQUEST_CODE_ADD_MEMBER) {
            if (callSession.getEndTime() != 0) {
                finish();
                return;
            }
            setShouldShowFloat(true);
            if (resultCode == RESULT_OK) {
                ArrayList<String> invited = data.getStringArrayListExtra("invited");
                RongCallClient.getInstance().addParticipants(callSession.getCallId(), invited);
            }
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (localViewContainer != null) {
            localViewContainer.setIsNeedFillScrren(true);
        }
    }

    @Override
    public String onSaveFloatBoxState(Bundle bundle) {
        super.onSaveFloatBoxState(bundle);
        String intentAction = getIntent().getAction();
        bundle.putBoolean("muteCamera", isMuteCamera);
        bundle.putBoolean("muteMIC", isMuteMIC);
        bundle.putString("localViewUserId", localViewUserId);
        bundle.putString("callAction", RongCallAction.ACTION_RESUME_CALL.getName());
        bundle.putInt("mediaType", RongCallCommon.CallMediaType.VIDEO.getValue());
        return intentAction;
    }

    @Override
    public void onRestoreFloatBox(Bundle bundle) {
        super.onRestoreFloatBox(bundle);

        callSession = RongCallClient.getInstance().getCallSession();
        if (bundle != null) {
            RongCallAction callAction = RongCallAction.valueOf(bundle.getString("callAction"));
            if (!callAction.equals(RongCallAction.ACTION_RESUME_CALL))
                return;
            localViewUserId = bundle.getString("localViewUserId");
            isMuteCamera = bundle.getBoolean("muteCamera");
            isMuteMIC = bundle.getBoolean("muteMIC");
            if (callSession == null) {
                setShouldShowFloat(false);
                finish();
                return;
            }

            boolean isLocalViewExist = false;
            for (CallUserProfile profile : callSession.getParticipantProfileList()) {
                if (profile.getUserId().equals(localViewUserId)) {
                    isLocalViewExist = true;
                    break;
                }
            }
            remoteViewContainer1.removeAllViews();
            remoteViewContainer2.removeAllViews();
            for (CallUserProfile profile : callSession.getParticipantProfileList()) {
                String currentUserId = RongIMClient.getInstance().getCurrentUserId();
                if (profile.getUserId().equals(localViewUserId)
                        || (!isLocalViewExist && profile.getUserId().equals(currentUserId))) {
                    localView = profile.getVideoView();
                    if (localView.getParent() != null) {
                        ((ViewGroup) localView.getParent()).removeAllViews();
                    }
                    localViewUserId = profile.getUserId();
                    localView.setZOrderOnTop(false);
                    localView.setZOrderMediaOverlay(false);
                    localViewContainer.addView(localView);
                    observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
                    RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
                    observerLayout.setGravity(Gravity.CENTER);
                    observerLayout.setLayoutParams(param);
                    localViewContainer.addView(observerLayout);
                    localView.setTag(localViewUserId);
                    TextView userNameView = (TextView) topContainer.findViewById(R.id.rc_voip_user_name);
                    userNameView.setTag(localViewUserId + "name");
                    UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(localViewUserId);
                    if (userInfo != null) {
                        userNameView.setText(userInfo.getName());
                    } else {
                        userNameView.setText(localViewUserId);
                    }
                }
            }
            onCallConnected(callSession, null);
        }
    }

    @Override
    public void onCallOutgoing(final RongCallSession callSession, SurfaceView localVideo) {
        super.onCallOutgoing(callSession, localVideo);
        this.callSession = callSession;
        RongCallClient.getInstance().setEnableLocalAudio(true);
        RongCallClient.getInstance().setEnableLocalVideo(true);
        localView = localVideo;
        onOutgoingCallRinging();
        ((BlinkVideoView) localView).setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_BALANCED);
        localViewContainer.addView(localView);

        observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        observerLayout.setGravity(Gravity.CENTER);
        observerLayout.setLayoutParams(param);
        localViewContainer.addView(observerLayout);

        localViewUserId = RongIMClient.getInstance().getCurrentUserId();
        localView.setTag(localViewUserId);
    }

    @Override
    public void onRemoteUserJoined(String userId, RongCallCommon.CallMediaType mediaType, int userType, SurfaceView remoteVideo) {
        stopRing();
        if (localViewUserId != null && localViewUserId.equals(userId)) {
            return;
        }
        View singleRemoteView = remoteViewContainer.findViewWithTag(userId + "view");

        if (singleRemoteView == null) {
            singleRemoteView = addSingleRemoteView(userId);
        }
        addRemoteVideo(singleRemoteView, remoteVideo, userId);
        singleRemoteView.findViewById(R.id.user_status).setVisibility(View.GONE);
    }

    @Override
    public void onRemoteUserLeft(String userId, RongCallCommon.CallDisconnectedReason reason) {
        // incomming state
        if (participantPortraitContainer != null && participantPortraitContainer.getVisibility() == View.VISIBLE) {
            View participantView = participantPortraitContainer.findViewWithTag(userId + "participantPortraitView");
            if (participantView == null) return;
            LinearLayout portraitContainer = (LinearLayout) participantView.getParent();
            portraitContainer.removeView(participantView);
            /*
            如果第一排删除了一个头像(第一排不足4个头像了)，且第二排还有头像
            则把第二排最左边的头像移动到第一排最右边
            */
            if (portraitContainer.equals(portraitContainer1)) {
                if (portraitContainer2.getChildCount() > 0) {
                    View childPortraitView = portraitContainer2.getChildAt(0);
                    portraitContainer2.removeView(childPortraitView);
                    portraitContainer1.addView(childPortraitView);
                }
            }
        }
        //incoming状态，localViewUserId为空
        if (localViewUserId == null)
            return;
        if (localViewUserId.equals(userId)) {
            localViewContainer.removeAllViews();
            String currentUserId = RongIMClient.getInstance().getCurrentUserId();
            FrameLayout remoteVideoView = (FrameLayout) remoteViewContainer.findViewWithTag(currentUserId);
            localView = (SurfaceView) remoteVideoView.getChildAt(0);
            remoteVideoView.removeAllViews();
            localViewContainer.addView(localView);

            observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            observerLayout.setGravity(Gravity.CENTER);
            observerLayout.setLayoutParams(param);
            localViewContainer.addView(observerLayout);

            TextView topUserNameView = (TextView) topContainer.findViewById(R.id.rc_voip_user_name);
            topUserNameView.setTag(currentUserId + "name");
            UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(currentUserId);
            if (userInfo != null) {
                topUserNameView.setText(userInfo.getName());
            } else {
                topUserNameView.setText(currentUserId);
            }
            localViewUserId = currentUserId;
        }

        View singleRemoteView = remoteViewContainer.findViewWithTag(userId + "view");

        if (singleRemoteView == null) return;

        LinearLayout container = (LinearLayout) singleRemoteView.getParent();
        container.removeView(singleRemoteView);
        if (container.equals(remoteViewContainer2)) {
            if (remoteViewContainer1.getChildCount() > 0) {
                View childView = remoteViewContainer1.getChildAt(0);
                remoteViewContainer1.removeView(childView);
                remoteViewContainer2.addView(childView);
            }
        }
    }

    @Override
    public void onRemoteUserInvited(String userId, RongCallCommon.CallMediaType mediaType) {
        super.onRemoteUserInvited(userId, mediaType);

        if (callSession != null) {
            for (CallUserProfile profile : callSession.getParticipantProfileList()) {
                if (profile.getUserId().equals(RongIMClient.getInstance().getCurrentUserId())) {
                    if (profile.getCallStatus().equals(RongCallCommon.CallStatus.CONNECTED)) {
                        addSingleRemoteView(userId);
                    }
                }
            }
        }
    }

    @SuppressLint("NewApi")
    @Override
    public void onCallConnected(RongCallSession callSession, SurfaceView localVideo) {
        super.onCallConnected(callSession, localVideo);
        this.callSession = callSession;
        AudioManager audioManager = (AudioManager) getSystemService(AUDIO_SERVICE);
        if (audioManager.isWiredHeadsetOn()) {
            RongCallClient.getInstance().setEnableSpeakerphone(false);
        } else {
            RongCallClient.getInstance().setEnableSpeakerphone(true);
        }
        if (localView == null) {
            localView = localVideo;
            localViewContainer.addView(localView);
            observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            observerLayout.setGravity(Gravity.CENTER);
            observerLayout.setLayoutParams(param);
            localViewContainer.addView(observerLayout);
            if (callSession.getUserType() == RongCallCommon.CallUserType.OBSERVER) {
                observerLayout.setVisibility(View.VISIBLE);
            }
            localViewUserId = RongIMClient.getInstance().getCurrentUserId();
            localView.setTag(localViewUserId);
        }

        localViewContainer.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (!isFullScreen) {
                    isFullScreen = true;
                    topContainer.setVisibility(View.GONE);
                    bottomButtonContainer.setVisibility(View.GONE);
                } else {
                    isFullScreen = false;
                    topContainer.setVisibility(View.VISIBLE);
                    bottomButtonContainer.setVisibility(View.VISIBLE);
                }
            }
        });
        bottomButtonContainer.removeAllViews();
        FrameLayout bottomButtonLayout = (FrameLayout) inflater.inflate(R.layout.rc_voip_multi_video_calling_bottom_view, null);
        bottomButtonContainer.addView(bottomButtonLayout);
        muteButtion = bottomButtonContainer.findViewById(R.id.rc_voip_call_mute_btn);
        muteButtion.setSelected(isMuteMIC);
        disableCameraButtion = bottomButtonContainer.findViewById(R.id.rc_voip_disable_camera_btn);
        disableCameraButtion.setSelected(isMuteCamera);
        topContainer.setVisibility(View.VISIBLE);
        minimizeButton.setVisibility(View.VISIBLE);
        userPortrait.setVisibility(View.GONE);
        moreButton.setVisibility(View.VISIBLE);
        switchCameraButton.setVisibility(View.VISIBLE);
        waitingContainer.setVisibility(View.GONE);
        remoteViewContainer.setVisibility(View.VISIBLE);
        participantPortraitContainer.setVisibility(View.GONE);

        userNameView = (TextView) topContainer.findViewById(R.id.rc_voip_user_name);
        userNameView.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        String currentUserId = RongIMClient.getInstance().getCurrentUserId();
        userNameView.setTag(currentUserId + "name");
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(currentUserId);
        if (userInfo != null) {
            userNameView.setText(userInfo.getName());
        } else {
            userNameView.setText(currentUserId);
        }
        RelativeLayout.LayoutParams parm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        parm.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        parm.setMarginEnd(10);
        parm.setMarginStart(20);
        parm.setMargins(20, 40, 0, 0);
        userNameView.setLayoutParams(parm);

        TextView remindInfo = (TextView) topContainer.findViewById(R.id.rc_voip_call_remind_info);
        remindInfo.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        setupTime(remindInfo);

        infoLayout = (LinearLayout) topContainer.findViewById(R.id.rc_voip_call_info_layout);
        parm = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.WRAP_CONTENT, RelativeLayout.LayoutParams.WRAP_CONTENT);
        parm.addRule(RelativeLayout.ALIGN_PARENT_LEFT);
        parm.addRule(RelativeLayout.BELOW, userNameView.getId());
        parm.setMargins(0, 8, 0, 0);
        infoLayout.setLayoutParams(parm);

        signalView = (ImageView) topContainer.findViewById(R.id.rc_voip_signal);
        signalView.setVisibility(View.VISIBLE);

        updateRemoteVideoViews(callSession);

    }

    void updateRemoteVideoViews(RongCallSession callSession) {
        for (CallUserProfile profile : callSession.getParticipantProfileList()) {
            String userId = profile.getUserId();

            if (userId.equals(localViewUserId))
                continue;
            View singleRemoteView = remoteViewContainer.findViewWithTag(userId + "view");
            if (singleRemoteView == null) {
                singleRemoteView = addSingleRemoteView(userId);
            }
            SurfaceView video = profile.getVideoView();
            if (video != null) {
                FrameLayout remoteVideoView = (FrameLayout) remoteViewContainer.findViewWithTag(userId);
                if (remoteVideoView == null) {
                    addRemoteVideo(singleRemoteView, video, userId);
                }
            }
        }
    }

    void addRemoteVideo(View singleRemoteView, SurfaceView video, String userId) {
        if (singleRemoteView == null)
            return;
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(userId);
        FrameLayout remoteVideoView = (FrameLayout) singleRemoteView.findViewById(R.id.viewlet_remote_video_user);

        remoteVideoView.removeAllViews();
        AsyncImageView userPortraitView = (AsyncImageView) singleRemoteView.findViewById(R.id.user_portrait);
        if (userInfo != null) {
            if (userInfo.getPortraitUri() != null) {
//                userPortraitView.setBackgroundColor(getResources().getColor(R.color.rc_voip_transparent));
                userPortraitView.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }
        }
        if (video.getParent() != null) {
            ((ViewGroup) video.getParent()).removeView(video);
        }
        video.setTag(userId + "video");
        ((BlinkVideoView) video).setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        remoteVideoView.addView(video, new FrameLayout.LayoutParams(remoteUserViewWidth, remoteUserViewWidth));
        TextView remoteNameTextView = new TextView(this);
        TextView tv = (TextView) singleRemoteView.findViewById(R.id.user_name);
        remoteNameTextView.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        ViewGroup.LayoutParams params = tv.getLayoutParams();
        remoteNameTextView.setLayoutParams(params);
        remoteNameTextView.setTextAppearance(this, R.style.rc_voip_text_style_style);
        if (userInfo != null) {
            remoteNameTextView.setText(userInfo.getName());
        } else {
            remoteNameTextView.setText(userId);
        }
        remoteVideoView.addView(remoteNameTextView);
        remoteVideoView.setVisibility(View.VISIBLE);
        remoteVideoView.setTag(userId);
    }

    View addSingleRemoteView(String userId) {
        View singleRemoteView = inflater.inflate(R.layout.rc_voip_viewlet_remote_user, null);
        UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(userId);
        singleRemoteView.setTag(userId + "view");
        TextView userStatus = (TextView) singleRemoteView.findViewById(R.id.user_status);
        userStatus.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        AsyncImageView userPortraitView = (AsyncImageView) singleRemoteView.findViewById(R.id.user_portrait);
        if (userInfo != null) {
            if (userInfo.getPortraitUri() != null) {
//                userPortraitView.setBackgroundColor(getResources().getColor(R.color.rc_voip_transparent));
                userPortraitView.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }
        }
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(remoteUserViewWidth, remoteUserViewWidth);
        params.setMargins(4, 4, 4, 4);
        if (remoteViewContainer2.getChildCount() < 4) {
            remoteViewContainer2.addView(singleRemoteView, params);
        } else if (remoteViewContainer1.getChildCount() < 4) {
            remoteViewContainer1.addView(singleRemoteView, params);
        }
        return singleRemoteView;
    }

    @Override
    public void onCallDisconnected(RongCallSession callSession, RongCallCommon.CallDisconnectedReason reason) {
        isFinishing = true;
        if (reason == null || callSession == null) {
            RLog.e(TAG, "onCallDisconnected. callSession is null!");
            postRunnableDelay(new Runnable() {
                @Override
                public void run() {
                    finish();
                }
            });
            return;
        }

        MultiCallEndMessage multiCallEndMessage = new MultiCallEndMessage();
        multiCallEndMessage.setMediaType(RongIMClient.MediaType.VIDEO);
        multiCallEndMessage.setReason(reason);

        RongIM.getInstance().insertMessage(callSession.getConversationType(), callSession.getTargetId(), callSession.getCallerUserId(), multiCallEndMessage, null);
        stopRing();
        postRunnableDelay(new Runnable() {
            @Override
            public void run() {
                finish();
            }
        });

        super.onCallDisconnected(callSession, reason);
    }

    @Override
    public void onRemoteCameraDisabled(String userId, boolean muted) {
        if (!muted) {
            if (localViewUserId.equals(userId)) {
                localView.setVisibility(View.INVISIBLE);
            } else {
                remoteViewContainer.findViewWithTag(userId).findViewWithTag(userId + "video").setVisibility(View.INVISIBLE);
            }
        } else {
            if (localViewUserId.equals(userId)) {
                localView.setVisibility(View.VISIBLE);
            } else {
                remoteViewContainer.findViewWithTag(userId).findViewWithTag(userId + "video").setVisibility(View.VISIBLE);
            }
        }
    }

    @Override
    public void onWhiteBoardURL(String url) {
        loadWhiteBoard(url, false);
    }

    @Override
    public void onNetWorkLossRate(int lossRate) {
        int resId = R.drawable.rc_voip_signal_1;
        if (signalView != null) {
            if (lossRate < 5) {
                resId = R.drawable.rc_voip_signal_1;
            } else if (lossRate < 15) {
                resId = R.drawable.rc_voip_signal_2;
            } else if (lossRate < 30) {
                resId = R.drawable.rc_voip_signal_3;
            } else if (lossRate < 45) {
                resId = R.drawable.rc_voip_signal_4;
            } else {
                resId = R.drawable.rc_voip_signal_5;
            }
        }
        signalView.setImageResource(resId);
    }

    @Override
    public void onNotifyDegradeNormalUserToObserver(String userId) {
        callSession.setUserType(RongCallCommon.CallUserType.OBSERVER);
        if (localViewUserId.equals(callSession.getSelfUserId())) {
            observerLayout.setVisibility(View.VISIBLE);
        }
    }

    @Override
    public void onNotifyAnswerObserverRequestBecomeNormalUser(String userId, long status) {
        if (status == 1) {
            callSession.setUserType(RongCallCommon.CallUserType.NORMAL);
            observerLayout.setVisibility(View.GONE);
        }
    }

    @Override
    public void onNotifyUpgradeObserverToNormalUser() {
        dialog = CallPromptDialog.newInstance(MultiVideoCallActivity.this, getString(R.string.rc_voip_invite_to_normal));
        dialog.setPromptButtonClickedListener(new CallPromptDialog.OnPromptButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked() {
                RongCallClient.getInstance().answerUpgradeObserverToNormalUser(callSession.getSelfUserId(), true);
                callSession.setUserType(RongCallCommon.CallUserType.NORMAL);
                observerLayout.setVisibility(View.GONE);
            }

            @Override
            public void onNegativeButtonClicked() {
                RongCallClient.getInstance().answerUpgradeObserverToNormalUser(callSession.getSelfUserId(), false);
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    @Override
    public void onNotifyHostControlUserDevice(final String hostId, final int dType, final int isOpen) {

        String note;
        if (isOpen == 1) {
            note = getString(R.string.rc_voip_open);
        } else {
            if (dType == 1) {
                onDisableCameraBtnClick(disableCameraButtion);
            } else if (dType == 2) {
                onMuteButtonClick(muteButtion);
            } else if (dType == 3) {
                onMuteButtonClick(muteButtion);
                onDisableCameraBtnClick(disableCameraButtion);
            }
            RongCallClient.getInstance().answerHostControlUserDevice(hostId, dType, isOpen == 1, true);
            return;
        }
        String note2 = getString(R.string.rc_voip_camera);
        if (dType == 1) {
            note2 = getString(R.string.rc_voip_camera);
            ;
        } else if (dType == 2) {
            note2 = getString(R.string.rc_voip_mic);
            ;
        } else if (dType == 3) {
            note2 = getString(R.string.rc_voip_camera_mic);
        }


        String message = String.format(getString(R.string.rc_voip_ask_to_do), note, note2);
        dialog = CallPromptDialog.newInstance(MultiVideoCallActivity.this, message);
        dialog.setPromptButtonClickedListener(new CallPromptDialog.OnPromptButtonClickedListener() {
            @Override
            public void onPositiveButtonClicked() {
                if (dType == 1) {
                    onDisableCameraBtnClick(disableCameraButtion);
                } else if (dType == 2) {
                    onMuteButtonClick(muteButtion);
                } else if (dType == 3) {
                    onMuteButtonClick(muteButtion);
                    onDisableCameraBtnClick(disableCameraButtion);
                }
                RongCallClient.getInstance().answerHostControlUserDevice(hostId, dType, isOpen == 1, true);
            }

            @Override
            public void onNegativeButtonClicked() {
                RongCallClient.getInstance().answerHostControlUserDevice(hostId, dType, isOpen == 1, false);
            }
        });

        dialog.setCancelable(false);
        dialog.show();
    }

    protected void initViews() {
        inflater = LayoutInflater.from(this);
        localViewContainer = (ContainerLayout) findViewById(R.id.rc_local_user_view);
        remoteViewContainer = (LinearLayout) findViewById(R.id.rc_remote_user_container);
        remoteViewContainer1 = (LinearLayout) findViewById(R.id.rc_remote_user_container_1);
        remoteViewContainer2 = (LinearLayout) findViewById(R.id.rc_remote_user_container_2);
        topContainer = (LinearLayout) findViewById(R.id.rc_top_container);
        waitingContainer = (LinearLayout) findViewById(R.id.rc_waiting_container);
        bottomButtonContainer = (LinearLayout) findViewById(R.id.rc_bottom_button_container);
        participantPortraitContainer = (LinearLayout) findViewById(R.id.rc_participant_portait_container);
        minimizeButton = (ImageView) findViewById(R.id.rc_voip_call_minimize);
        userPortrait = (AsyncImageView) findViewById(R.id.rc_voip_user_portrait);
        moreButton = (ImageView) findViewById(R.id.rc_voip_call_more);
        switchCameraButton = (ImageView) findViewById(R.id.rc_voip_switch_camera);
        progressDialog = new ProgressDialog(this);
        progressDialog.setCancelable(false);
        progressDialog.setMessage("白板加载中...");
        mRelativeWebView = (RelativeLayout) findViewById(R.id.rc_whiteboard);
        whiteboardView = new WebView(getApplicationContext());
        ViewGroup.LayoutParams params = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT);
        whiteboardView.setLayoutParams(params);
        mRelativeWebView.addView(whiteboardView);

//         btnWhiteBoard.setVisibility(View.GONE);
        WebSettings settings = whiteboardView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setUseWideViewPort(true);
        settings.setLayoutAlgorithm(WebSettings.LayoutAlgorithm.SINGLE_COLUMN);
        settings.setLoadWithOverviewMode(true);
        settings.setBlockNetworkImage(false);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            settings.setMixedContentMode(WebSettings.MIXED_CONTENT_ALWAYS_ALLOW);
        }

        DisplayMetrics metrics = new DisplayMetrics();
        getWindowManager().getDefaultDisplay().getMetrics(metrics);
        remoteUserViewWidth = (metrics.widthPixels - 50) / 4;
        localView = null;
        localViewContainer.removeAllViews();
        remoteViewContainer1.removeAllViews();
        remoteViewContainer2.removeAllViews();

        minimizeButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                MultiVideoCallActivity.super.onMinimizeClick(v);
            }
        });
    }

    protected void setupIntent() {
        Intent intent = getIntent();
        RongCallAction callAction = RongCallAction.valueOf(intent.getStringExtra("callAction"));
        if (callAction == null || callAction.equals(RongCallAction.ACTION_RESUME_CALL)) {
            return;
        }
        ArrayList<String> invitedList = new ArrayList<>();
        ArrayList<String> observerList = new ArrayList<>();
        if (callAction.equals(RongCallAction.ACTION_INCOMING_CALL)) {
            callSession = intent.getParcelableExtra("callSession");

            onIncomingCallRinging();
            TextView callRemindInfoView = (TextView) topContainer.findViewById(R.id.rc_voip_call_remind_info);
            TextView userNameView = (TextView) topContainer.findViewById(R.id.rc_voip_user_name);
            callRemindInfoView.setText(R.string.rc_voip_video_call_inviting);
            if (callSession != null) {
                UserInfo userInfo = RongContext.getInstance().getUserInfoFromCache(callSession.getInviterUserId());
                userNameView.setTag(callSession.getInviterUserId() + "name");
                if (userInfo != null) {
                    userNameView.setText(userInfo.getName());
                    if (userInfo.getPortraitUri() != null) {
                        userPortrait.setAvatar(userInfo.getPortraitUri());
                    }
                } else {
                    userNameView.setText(callSession.getInviterUserId());
                }
                List<CallUserProfile> list = callSession.getParticipantProfileList();
                for (CallUserProfile profile : list) {
                    if (!profile.getUserId().equals(callSession.getCallerUserId())) {
                        invitedList.add(profile.getUserId());
                    }
                }

                FrameLayout bottomButtonLayout = (FrameLayout) inflater.inflate(R.layout.rc_voip_call_bottom_incoming_button_layout, null);
                ImageView answerV = (ImageView) bottomButtonLayout.findViewById(R.id.rc_voip_call_answer_btn);
                answerV.setImageResource(R.drawable.rc_voip_vedio_answer_selector);
                bottomButtonContainer.addView(bottomButtonLayout);
                for (int i = 0; i < invitedList.size(); i++) {
                    if (invitedList.get(i).equals(callSession.getCallerUserId()))
                        continue;
                    View userPortraitView = inflater.inflate(R.layout.rc_voip_user_portrait, null);
                    AsyncImageView portraitView = (AsyncImageView) userPortraitView.findViewById(R.id.rc_user_portrait);
                    userInfo = RongContext.getInstance().getUserInfoFromCache(invitedList.get(i));
                    if (userInfo != null && userInfo.getPortraitUri() != null) {
                        portraitView.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
                    }

                    portraitContainer1 = (LinearLayout) participantPortraitContainer.findViewById(R.id.rc_participant_portait_container_1);
                    portraitContainer2 = (LinearLayout) participantPortraitContainer.findViewById(R.id.rc_participant_portait_container_2);
                    LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
                    params.setMargins(10, 0, 0, 0);
                    if (i < 4) {
                        portraitContainer1.addView(userPortraitView, params);
                        userPortraitView.setTag(invitedList.get(i) + "participantPortraitView");
                    } else {
                        portraitContainer2.addView(userPortraitView, params);
                        userPortraitView.setTag(invitedList.get(i) + "participantPortraitView");
                    }
                }
            }

            topContainer.setVisibility(View.VISIBLE);
            minimizeButton.setVisibility(View.GONE);
            moreButton.setVisibility(View.GONE);
            switchCameraButton.setVisibility(View.GONE);
            waitingContainer.setVisibility(View.GONE);
            remoteViewContainer.setVisibility(View.GONE);
            participantPortraitContainer.setVisibility(View.VISIBLE);
            bottomButtonContainer.setVisibility(View.VISIBLE);
        } else if (callAction.equals(RongCallAction.ACTION_OUTGOING_CALL)) {
            Conversation.ConversationType conversationType = Conversation.ConversationType.valueOf(intent.getStringExtra("conversationType").toUpperCase(Locale.US));
            String targetId = intent.getStringExtra("targetId");
            ArrayList<String> userIds = intent.getStringArrayListExtra("invitedUsers");
            ArrayList<String> observerIds = intent.getStringArrayListExtra("observerUsers");
            if (observerIds != null && observerIds.size() > 0) {
                observerList.addAll(observerIds);
            }

            for (int i = 0; i < userIds.size(); i++) {
                if (!userIds.get(i).equals(RongIMClient.getInstance().getCurrentUserId())) {
                    invitedList.add(userIds.get(i));
                    String userId = userIds.get(i);
                    if (observerList.size() == 0 || !observerList.contains(userId)) {
                        addSingleRemoteView(userId);
                    }
                }
            }
            RongCallClient.getInstance().startCall(conversationType, targetId, invitedList, observerList, RongCallCommon.CallMediaType.VIDEO, "multi");
            FrameLayout bottomButtonLayout = (FrameLayout) inflater.inflate(R.layout.rc_voip_multi_video_calling_bottom_view, null);
            bottomButtonLayout.findViewById(R.id.rc_voip_call_mute).setVisibility(View.GONE);
            bottomButtonLayout.findViewById(R.id.rc_voip_disable_camera).setVisibility(View.GONE);
            bottomButtonContainer.addView(bottomButtonLayout);
            topContainer.setVisibility(View.GONE);
            waitingContainer.setVisibility(View.VISIBLE);
            remoteViewContainer.setVisibility(View.VISIBLE);
            participantPortraitContainer.setVisibility(View.GONE);
            bottomButtonContainer.setVisibility(View.VISIBLE);
        }
    }

    public void onHangupBtnClick(View view) {
        if (callSession == null || isFinishing) {
            return;
        }
        stopRing();
        RongCallClient.getInstance().hangUpCall(callSession.getCallId());
    }

    public void onReceiveBtnClick(View view) {
        if (callSession == null || isFinishing) {
            return;
        }
        RongCallClient.getInstance().acceptCall(callSession.getCallId());
        RongCallClient.getInstance().setEnableLocalAudio(true);
        RongCallClient.getInstance().setEnableLocalVideo(true);
        stopRing();
    }

    private void addButtionClickEvent() {
        setShouldShowFloat(false);

        if (callSession.getConversationType().equals(Conversation.ConversationType.DISCUSSION)) {
            RongIMClient.getInstance().getDiscussion(callSession.getTargetId(), new RongIMClient.ResultCallback<Discussion>() {
                @Override
                public void onSuccess(Discussion discussion) {
                    Intent intent = new Intent(MultiVideoCallActivity.this, CallSelectMemberActivity.class);
                    ArrayList<String> added = new ArrayList<>();
                    List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
                    for (CallUserProfile profile : list) {
                        added.add(profile.getUserId());
                    }
                    intent.putStringArrayListExtra("allMembers", (ArrayList<String>) discussion.getMemberIdList());
                    intent.putStringArrayListExtra("invitedMembers", added);
                    intent.putExtra("conversationType", callSession.getConversationType().getValue());
                    intent.putExtra("mediaType", RongCallCommon.CallMediaType.VIDEO.getValue());
                    startActivityForResult(intent, REQUEST_CODE_ADD_MEMBER);
                }

                @Override
                public void onError(RongIMClient.ErrorCode e) {

                }
            });
        } else if (callSession.getConversationType().equals(Conversation.ConversationType.GROUP)) {
            Intent intent = new Intent(MultiVideoCallActivity.this, CallSelectMemberActivity.class);
            ArrayList<String> added = new ArrayList<>();
            List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
            for (CallUserProfile profile : list) {
                added.add(profile.getUserId());
            }
            intent.putStringArrayListExtra("invitedMembers", added);
            intent.putExtra("groupId", callSession.getTargetId());
            intent.putExtra("conversationType", callSession.getConversationType().getValue());
            intent.putExtra("mediaType", RongCallCommon.CallMediaType.VIDEO.getValue());
            startActivityForResult(intent, REQUEST_CODE_ADD_MEMBER);
        } else {
            ArrayList<String> added = new ArrayList<>();
            List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
            for (CallUserProfile profile : list) {
                added.add(profile.getUserId());
            }
            addMember(added);
        }
    }

    public void onMoreButtonClick(View view) {
        optionMenu = new CallOptionMenu(MultiVideoCallActivity.this);
        optionMenu.setHandUpvisibility(callSession.getUserType() == RongCallCommon.CallUserType.OBSERVER);
        optionMenu.setOnItemClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int i = v.getId();
                if (i == R.id.voipItemAdd) {
                    addButtionClickEvent();
                } else if (i == R.id.voipItemWhiteboard) {
                    onWhiteBoardClick();
                } else if (i == R.id.voipItemHandup) {
                    onRequestNormalUserClick();
                }
                optionMenu.dismiss();
            }
        });
        optionMenu.showAsDropDown(moreButton, (int) moreButton.getX(), 0);
    }

    @Override
    protected void onAddMember(List<String> newMemberIds) {
        if (newMemberIds == null || newMemberIds.isEmpty()) {
            return;
        }
        List<String> added = new ArrayList<>();
        List<String> participants = new ArrayList<>();
        List<CallUserProfile> list = RongCallClient.getInstance().getCallSession().getParticipantProfileList();
        for (CallUserProfile profile : list) {
            participants.add(profile.getUserId());
        }
        for (String id : newMemberIds) {
            if (participants.contains(id)) {
                continue;
            } else {
                added.add(id);
            }
        }
        if (added.isEmpty()) {
            return;
        }

        RongCallClient.getInstance().addParticipants(callSession.getCallId(), added);
    }

    public void onSwitchCameraClick(View view) {
        RongCallClient.getInstance().switchCamera();
    }

    public void onRequestNormalUserClick() {
        RongCallClient.getInstance().requestNormalUser();
    }

    public void onWhiteBoardClick() {
        if (mRelativeWebView.isShown())
            mRelativeWebView.setVisibility(View.GONE);
        else {
            mRelativeWebView.setVisibility(View.VISIBLE);
            RongCallClient.getInstance().requestWhiteBoard();
        }
    }

    public void onMuteButtonClick(View view) {
        RongCallClient.getInstance().setEnableLocalAudio(view.isSelected());
        view.setSelected(!view.isSelected());
        isMuteMIC = view.isSelected();
    }

    public void onDisableCameraBtnClick(View view) {
        TextView text = (TextView) bottomButtonContainer.findViewById(R.id.rc_voip_disable_camera_text);
        String currentUserId = RongIMClient.getInstance().getCurrentUserId();

        RongCallClient.getInstance().setEnableLocalVideo(view.isSelected());
        if (view.isSelected()) {
            text.setText(R.string.rc_voip_disable_camera);
            if (localViewUserId.equals(currentUserId)) {
                localView.setVisibility(View.VISIBLE);
            } else {
                remoteViewContainer.findViewWithTag(currentUserId).findViewWithTag(currentUserId + "video").setVisibility(View.VISIBLE);
            }
        } else {
            text.setText(R.string.rc_voip_enable_camera);
            if (localViewUserId.equals(currentUserId)) {
                localView.setVisibility(View.INVISIBLE);
            } else {
                remoteViewContainer.findViewWithTag(currentUserId).findViewWithTag(currentUserId + "video").setVisibility(View.INVISIBLE);
            }
        }
        view.setSelected(!view.isSelected());
        isMuteCamera = view.isSelected();
    }

    public void onSwitchRemoteUsers(View view) {
        String from = (String) view.getTag();
        if (from == null)
            return;
        String to = (String) localView.getTag();
        FrameLayout layout = (FrameLayout) view;
        SurfaceView fromView = (SurfaceView) layout.getChildAt(0);
        SurfaceView toView = localView;
        if (fromView == null || toView == null) {
            return;
        }
        localViewContainer.removeAllViews();
        layout.removeAllViews();

        View singleRemoteView = remoteViewContainer.findViewWithTag(from + "view");
        UserInfo toUserInfo = RongContext.getInstance().getUserInfoFromCache(to);
        UserInfo fromUserInfo = RongContext.getInstance().getUserInfoFromCache(from);

        AsyncImageView userPortraitView = (AsyncImageView) singleRemoteView.findViewById(R.id.user_portrait);
//        TextView backUserNameView = (TextView) singleRemoteView.findViewById(R.id.user_name);
//        backUserNameView.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        if (toUserInfo != null) {
            if (toUserInfo.getPortraitUri() != null) {
                userPortraitView.setAvatar(toUserInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }
//            backUserNameView.setText(toUserInfo.getName());
        }
//        else {
//            backUserNameView.setText(to);
//        }
        singleRemoteView.setTag(to + "view");
        fromView.setZOrderOnTop(false);
        fromView.setZOrderMediaOverlay(false);
        localViewContainer.addView(fromView);
        observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
        RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
        observerLayout.setGravity(Gravity.CENTER);
        observerLayout.setLayoutParams(param);
        localViewContainer.addView(observerLayout);
        observerLayout.setVisibility(View.GONE);
        if (callSession.getSelfUserId().equals(fromUserInfo.getUserId())) {
            if (callSession.getUserType() == RongCallCommon.CallUserType.OBSERVER) {
                observerLayout.setVisibility(View.VISIBLE);
            }
        }
        toView.setZOrderOnTop(true);
        toView.setZOrderMediaOverlay(true);
        toView.setTag(to + "video");
        ((BlinkVideoView) toView).setScalingType(RendererCommon.ScalingType.SCALE_ASPECT_FILL);
        layout.addView(toView, new FrameLayout.LayoutParams(remoteUserViewWidth, remoteUserViewWidth));
        TextView remoteNameTextView = new TextView(this);
        TextView tv = (TextView) singleRemoteView.findViewById(R.id.user_name);
        ViewGroup.LayoutParams params = tv.getLayoutParams();
        remoteNameTextView.setLayoutParams(params);
        remoteNameTextView.setTextAppearance(this, R.style.rc_voip_text_style_style);
        if (toUserInfo != null) {
            remoteNameTextView.setText(toUserInfo.getName());
        } else {
            remoteNameTextView.setText(toUserInfo.getUserId());
        }
        remoteNameTextView.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        layout.addView(remoteNameTextView);

        TextView topUserNameView = (TextView) topContainer.findViewById(R.id.rc_voip_user_name);
        topUserNameView.setShadowLayer(16F, 0F, 2F, getResources().getColor(R.color.rc_voip_reminder_shadow));
        topUserNameView.setTag(from + "name");
        if (fromUserInfo != null) {
            topUserNameView.setText(fromUserInfo.getName());
        } else {
            topUserNameView.setText(from);
        }
        layout.setTag(to);
        localView = fromView;
        localView.setTag(from);
        localViewUserId = from;
    }

    @Override
    public void onBackPressed() {
        if (callSession == null || isFinishing) {
            return;
        }

        List<CallUserProfile> participantProfiles = callSession.getParticipantProfileList();
        RongCallCommon.CallStatus callStatus = null;
        for (CallUserProfile item : participantProfiles) {
            if (item.getUserId().equals(callSession.getSelfUserId())) {
                callStatus = item.getCallStatus();
                break;
            }
        }
        if (callStatus != null && callStatus.equals(RongCallCommon.CallStatus.CONNECTED)) {
            super.onBackPressed();
        } else {
            RongCallClient.getInstance().hangUpCall(callSession.getCallId());
        }
    }

    public void onEventMainThread(UserInfo userInfo) {
        if (isFinishing()) {
            return;
        }
        if (participantPortraitContainer.getVisibility() == View.VISIBLE) {
            View participantView = participantPortraitContainer.findViewWithTag(userInfo.getUserId() + "participantPortraitView");
            if (participantView != null && userInfo.getPortraitUri() != null) {
                AsyncImageView portraitView = (AsyncImageView) participantView.findViewById(R.id.rc_user_portrait);
                portraitView.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }
        }
        if (remoteViewContainer.getVisibility() == View.VISIBLE) {
            View remoteView = remoteViewContainer.findViewWithTag(userInfo.getUserId() + "view");
            if (remoteView != null && userInfo.getPortraitUri() != null) {
                AsyncImageView portraitView = (AsyncImageView) remoteView.findViewById(R.id.user_portrait);
                portraitView.setAvatar(userInfo.getPortraitUri().toString(), R.drawable.rc_default_portrait);
            }
        }
        if (topContainer.getVisibility() == View.VISIBLE) {
            TextView nameView = (TextView) topContainer.findViewWithTag(userInfo.getUserId() + "name");
            if (nameView != null && userInfo.getName() != null)
                nameView.setText(userInfo.getName());
        }
    }

    private ProgressDialog progressDialog;

    private void loadWhiteBoard(String url, boolean isReload) {
        if (isReload) {
            whiteboardView.reload();
            progressDialog.show();
            return;
        }

        if (TextUtils.isEmpty(url)) {
            return;
        }

        progressDialog.show();
        mRelativeWebView.setVisibility(View.VISIBLE);
        whiteboardView.setLayerType(View.LAYER_TYPE_SOFTWARE, null);
        // https://web.blinktalk.online/ewbweb/blink-wb.html?roomKey=1234567890abcdefg1dg%40blinktest&token=eyJhbGciOiJIUzUxMiJ9.eyJyb29tS2V5IjoiMTIzNDU2Nzg5MGFiY2RlZmcxZGdAYmxpbmt0ZXN0IiwiZXhwIjoxNTE2MzQ0MTc1fQ.6izAdEW6yfYns7ACmKBVL6ymASq_28crvseMCIsjv-ITjfCXB2S8O7gcKv1CUclkSSfCGOvgfo4Pycl_Z0yM0Q&type=android

        // https://web.blinkcloud.cn/ewbweb/blink-wb.html?roomKey=1234567890abcdefg1dg%40blink&token=eyJhbGciOiJIUzUxMiJ9.eyJyb29tS2V5IjoiMTIzNDU2Nzg5MGFiY2RlZmcxZGdAYmxpbmsiLCJleHAiOjE1MTYzNDM3NjJ9.DJCa1mt67xW_5sfzxHUWi5O143UjgFl-LDNLfc8GlWp-khWACXIzYipA_L-9SIU7h8_16N2Pu-fLmePOeRX6pA&type=android
        whiteboardView.loadUrl(url);
        whiteboardView.setWebViewClient(new WebViewClient() {
            @Override
            public boolean shouldOverrideUrlLoading(WebView view, String url) {
                // TODO Auto-generated method stub
                //返回值是true的时候控制去WebView打开，为false调用系统浏览器或第三方浏览器
                view.loadUrl(url);
                return true;
            }

            @Override
            public void onPageFinished(WebView view, String url) {
                super.onPageFinished(view, url);
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
                progressDialog.dismiss();
            }

            @Override
            public void onReceivedSslError(WebView view, SslErrorHandler handler, SslError error) {
                try {
                    handler.proceed();//接受证书
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        });
    }

    @Override
    public void onNotifySharingScreen(String userId, boolean isSharing) {
        localViewContainer.setIsNeedFillScrren(!isSharing);
        if (localViewUserId.equals(userId)) {
            localViewContainer.removeAllViews();
            localViewContainer.addView(localView);
            observerLayout = (RelativeLayout) inflater.inflate(R.layout.rc_voip_observer_hint, null);
            RelativeLayout.LayoutParams param = new RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT, RelativeLayout.LayoutParams.MATCH_PARENT);
            observerLayout.setGravity(Gravity.CENTER);
            observerLayout.setLayoutParams(param);
            localViewContainer.addView(observerLayout);
            if (callSession.getUserType() == RongCallCommon.CallUserType.OBSERVER) {
                observerLayout.setVisibility(View.VISIBLE);
            }
        }
    }
}
