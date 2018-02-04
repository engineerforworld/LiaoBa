package com.wwl.liaoba.activity;

import android.app.Activity;
import android.os.Bundle;
import android.os.Handler;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.wwl.liaoba.R;
import com.wwl.liaoba.Utils.AppSharePreferenceMgr;
import com.wwl.liaoba.Utils.UserInfoManager;
import com.wwl.liaoba.bean.User;

import java.util.ArrayList;

import io.rong.imkit.RongIM;
import io.rong.imlib.RongIMClient;


public class LoginActivity extends Activity implements View.OnClickListener {
    private static final String TAG = LoginActivity.class.getName();
    private long exitTime = 0;
    private ArrayList<User> mList;
    private Button mButton;
    private EditText mEditText;
    private ImageView mImg_Background;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);
        mButton = (Button) findViewById(R.id.btn_main_login);
        mEditText = (EditText) findViewById(R.id.edt_main_username);
        mImg_Background = (ImageView) findViewById(R.id.de_img_backgroud);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Animation animation = AnimationUtils.loadAnimation(LoginActivity.this, R.anim.translate_anim);
                mImg_Background.startAnimation(animation);
            }
        }, 200);
        mList = UserInfoManager.getInstance().getUserList();
        mButton.setOnClickListener(this);
    }


    @Override
    public void onClick(View view) {
        String userName = mEditText.getText().toString().trim();
        if (!TextUtils.isEmpty(userName)) {
            for (int i = 0; i < mList.size(); i++) {
                if (userName.equals(mList.get(i).getUserName())) {
                    connect(mList.get(i));
                    return;
                }
            }
        }
    }

    private void connect(final User user) {
        RongIM.connect(user.getToken(), new RongIMClient.ConnectCallback() {
            @Override
            public void onTokenIncorrect() {
                Log.i(TAG, "onTokenIncorrect");
            }

            @Override
            public void onSuccess(String s) {
                if (RongIM.getInstance() != null) {
                    //Map<String, Boolean> map = new HashMap<>();
                    // 会话列表需要显示私聊会话, 第二个参数 true 代表私聊会话需要聚合显示
                    //map.put(Conversation.ConversationType.PRIVATE.getName(), false);
                    RongIM.getInstance().startConversationList(LoginActivity.this, null);
                }
            }

            @Override
            public void onError(RongIMClient.ErrorCode errorCode) {

            }
        });
    }


    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_BACK && event.getAction() == KeyEvent.ACTION_DOWN) {
            if ((System.currentTimeMillis() - exitTime) > 2000) {
                Toast.makeText(getApplicationContext(), "再按一次退出程序", Toast.LENGTH_SHORT).show();
                exitTime = System.currentTimeMillis();
            } else {
                RongIM.getInstance().disconnect();
                //RongIM.getInstance().logout();
                AppSharePreferenceMgr.put(this, "isLogin", "n");
                System.exit(0);
            }
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}
