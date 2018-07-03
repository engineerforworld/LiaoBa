package com.wwl.liaoba.Utils;

import android.content.Context;
import android.net.Uri;

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
        User user4 = new User(Constants.userName_4, Constants.userid_4, Constants.token_user4);
        User user5 = new User(Constants.userName_5, Constants.userid_5, Constants.token_user5);
        User user6 = new User(Constants.userName_6, Constants.userid_6, Constants.token_user6);
        User user7 = new User(Constants.userName_7, Constants.userid_7, Constants.token_user7);
        User user8 = new User(Constants.userName_8, Constants.userid_8, Constants.token_user8);
        User user9 = new User(Constants.userName_9, Constants.userid_9, Constants.token_user9);

        list.add(user1);
        list.add(user2);
        list.add(user3);
        list.add(user4);
        list.add(user5);
        list.add(user6);
        list.add(user7);
        list.add(user8);
        list.add(user9);
    }

    public static UserInfoManager getInstance() {
        return sInstance;
    }

    public ArrayList<User> getUserList() {
        return list;
    }

    public UserInfo getUserInfo(String userId) {
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getUserId().equals(userId)) {
                UserInfo userInfo = new UserInfo(list.get(i).getUserId(), list.get(i).getUserName(), null);
                return userInfo;
            }
        }
        return null;
    }

    public Group getGroupInfo(String groupId) {
        Group group = new Group(Constants.groupid_1, Constants.groupName_1, Uri.parse("https://www.baidu.com/img/bd_logo1.png?qua=high"));
        return group;
    }
}
