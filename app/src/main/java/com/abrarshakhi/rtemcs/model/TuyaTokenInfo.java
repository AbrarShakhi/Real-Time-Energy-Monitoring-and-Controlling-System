package com.abrarshakhi.rtemcs.model;

import com.google.gson.annotations.SerializedName;

public class TuyaTokenInfo {

    @SerializedName("expire_time")
    private long expireTime;

    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("uid")
    private String uid;

    public long getExpireTime() {
        return expireTime;
    }

    public String getAccessToken() {
        return accessToken;
    }

    public String getRefreshToken() {
        return refreshToken;
    }

    public String getUid() {
        return uid;
    }
}

