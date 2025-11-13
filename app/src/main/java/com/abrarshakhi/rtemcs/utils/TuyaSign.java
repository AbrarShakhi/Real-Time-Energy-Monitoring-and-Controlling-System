package com.abrarshakhi.rtemcs.utils;

import android.util.Log;

import org.json.JSONObject;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.Map;
import java.util.TreeMap;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

public class TuyaSign {

    private static final String TAG = "TuyaSign";
    private final String ACCESS_ID;
    private final String ACCESS_SECRET;

    public TuyaSign(final String ACCESS_ID, final String ACCESS_SECRET) {
        this.ACCESS_ID = ACCESS_ID;
        this.ACCESS_SECRET = ACCESS_SECRET;
    }

    private static String sha256Hex(String input) throws Exception {
        MessageDigest digest = MessageDigest.getInstance("SHA-256");
        byte[] hash = digest.digest(input.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    private static String hmacSha256(String data, String key) throws Exception {
        Mac sha256_HMAC = Mac.getInstance("HmacSHA256");
        SecretKeySpec secret_key = new SecretKeySpec(key.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
        sha256_HMAC.init(secret_key);
        byte[] hash = sha256_HMAC.doFinal(data.getBytes(StandardCharsets.UTF_8));

        StringBuilder hexString = new StringBuilder();
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) hexString.append('0');
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public SignResult calculateSign(
        String method,
        String path,
        Map<String, String> params,
        JSONObject body,
        String accessToken
    ) {
        try {
            StringBuilder strToSign = new StringBuilder();

            strToSign.append(method.toUpperCase()).append("\n");

            String contentToSha256 = "";
            if (body != null && body.length() > 0) {
                contentToSha256 = body.toString();
            }
            String bodyHash = sha256Hex(contentToSha256);
            strToSign.append(bodyHash).append("\n");

            strToSign.append("\n");

            strToSign.append(path);

            if (params != null && !params.isEmpty()) {
                strToSign.append("?");
                Map<String, String> sortedParams = new TreeMap<>(params);
                for (Map.Entry<String, String> entry : sortedParams.entrySet()) {
                    strToSign.append(entry.getKey())
                        .append("=")
                        .append(entry.getValue())
                        .append("&");
                }
                strToSign.setLength(strToSign.length() - 1);
            }

            long timestamp = System.currentTimeMillis();
            String message = ACCESS_ID;
            if (accessToken != null && !accessToken.isEmpty()) {
                message += accessToken;
            }
            message += timestamp + strToSign.toString();

            String sign = hmacSha256(message, ACCESS_SECRET).toUpperCase();

            Log.d(TAG, "StringToSign:\n" + strToSign);
            Log.d(TAG, "Sign: " + sign);

            return new SignResult(sign, timestamp);

        } catch (Exception e) {
            Log.e(TAG, "Error in calculateSign: " + e.getMessage());
            return null;
        }
    }

    public static class SignResult {
        public final String sign;
        public final long timestamp;

        public SignResult(String sign, long timestamp) {
            this.sign = sign;
            this.timestamp = timestamp;
        }
    }
}
