package com.wwl.liaoba.bean;

import java.io.Serializable;

public class User implements Serializable {

    private String userName;
    private String userId;
    private String token;

    public User(String userName, String userId, String token) {
        this.userName = userName;
        this.userId = userId;
        this.token = token;
    }

    public String getUserName() {
        return userName;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getToken() {
        return token;
    }

    public void setToken(String token) {
        this.token = token;
    }
}
