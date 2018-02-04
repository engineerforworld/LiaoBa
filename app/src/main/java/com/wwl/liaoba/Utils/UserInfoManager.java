package com.wwl.liaoba.Utils;

import android.content.Context;

import com.wwl.liaoba.bean.User;

import java.util.ArrayList;

import io.rong.imlib.model.Group;
import io.rong.imlib.model.UserInfo;

public class UserInfoManager {
    private final Context mContext;
    private static UserInfoManager sInstance;
    public static ArrayList<User> list;

    public UserInfoManager(Context context) {
        mContext = context;
    }

    public static void init(Context context) {
        sInstance = new UserInfoManager(context);
        list = new ArrayList<User>();
        User user1 = new User(Constants.userName_1, Constants.userid_1, Constants.token_user1);
        User user2 = new User(Constants.userName_2, Constants.userid_2, Constants.token_user2);
        User user3 = new User(Constants.userName_3, Constants.userid_3, Constants.token_user3);
        list.add(user1);
        list.add(user2);
        list.add(user3);
    }

    public static UserInfoManager getInstance() {
        return sInstance;
    }

    public ArrayList<User> getUserList() {
        return list;
    }

    public UserInfo getUserInfo(String userId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserId().equals(userId)) ;
            UserInfo userInfo = new UserInfo(list.get(i).getUserId(), list.get(i).getUserName(), null);
            return userInfo;
        }
        return null;
    }

    public Group getGroupInfo(String groupId) {
        Group group = new Group(Constants.groupid_1, Constants.groupName_1, null);
        return group;
    }
}
