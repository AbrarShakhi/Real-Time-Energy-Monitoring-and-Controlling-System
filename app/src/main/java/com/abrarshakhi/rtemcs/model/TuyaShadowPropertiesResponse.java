package com.abrarshakhi.rtemcs.model;

import com.google.gson.annotations.SerializedName;
import java.util.List;

public class TuyaShadowPropertiesResponse {

    @SerializedName("success")
    private boolean success;

    @SerializedName("code")
    private int code;

    @SerializedName("msg")
    private String message;

    @SerializedName("t")
    private long timestamp;

    @SerializedName("tid")
    private String tid;

    @SerializedName("result")
    private Result result;

    public boolean isSuccess() {
        return success;
    }

    public int getCode() {
        return code;
    }

    public String getMessage() {
        return message;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public String getTid() {
        return tid;
    }

    public Result getResult() {
        return result;
    }

    // ---------------- INNER CLASSES ---------------- //

    public static class Result {

        @SerializedName("properties")
        private List<Property> properties;

        public List<Property> getProperties() {
            return properties;
        }
    }

    public static class Property {

        @SerializedName("code")
        private String code;

        @SerializedName("value")
        private Object value;

        @SerializedName("update_time")
        private long updateTime;

        public String getCode() {
            return code;
        }

        public Object getValue() {
            return value;
        }

        public long getUpdateTime() {
            return updateTime;
        }
    }
}
