package com.qiscus.rtc.data.model;

/**
 * Created by fitra on 2/10/17.
 */

public class QiscusRTCAccount {
    private String username;
    private String displayName;
    private String avatarUrl;

    public QiscusRTCAccount(String username, String avatarUrl) {
        this.username = username;
        this.avatarUrl = avatarUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getDisplayName() {
        return displayName;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}

