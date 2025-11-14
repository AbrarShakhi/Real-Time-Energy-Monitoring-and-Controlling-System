package com.abrarshakhi.rtemcs.model;

import com.google.gson.annotations.SerializedName;

public class TuyaCommandResponse {

    @SerializedName("result")
    private boolean result;

    @SerializedName("success")
    private boolean success;

    @SerializedName("t")
    private long timestamp;

    @SerializedName("tid")
    private String tid;

    public boolean isResult() {
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
