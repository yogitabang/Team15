package com.avaya.sdksampleapp.commpackage.Pojos;

/**
 * Created by yogita on 7/5/17.
 */

public class BasicUserInfo {
    private String username;
    private String password;

    public void setPassword(String password) {
        this.password = password;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public String getUsername() {
        return username;
    }
}
