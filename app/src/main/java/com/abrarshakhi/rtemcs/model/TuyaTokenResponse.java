package com.abrarshakhi.rtemcs.model;

import com.google.gson.annotations.SerializedName;

public class TuyaTokenResponse {

    @SerializedName("result")
    private TuyaTokenInfo result;

    @SerializedName("success")
    private boolean success;

    @SerializedName("t")
    private long timestamp;

    @SerializedName("tid")
    private String tid;

    public TuyaTokenInfo getResult() {
        return result;
    }

    public boolean isSuccess() {
        return success;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTid() {
        return tid;
    }
}
