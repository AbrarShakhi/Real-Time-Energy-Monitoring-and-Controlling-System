package com.abrarshakhi.rtemcs.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.model.TuyaCommand;
import com.abrarshakhi.rtemcs.model.TuyaCommandResponse;
import com.abrarshakhi.rtemcs.model.TuyaShadowPropertiesResponse;
import com.abrarshakhi.rtemcs.model.TuyaTokenInfo;
import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;
import com.abrarshakhi.rtemcs.utils.TuyaSign;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Callback;
import retrofit2.Retrofit;

public class TuyaOpenApi {
    private static TuyaOpenApi instance;
    private final Retrofit retrofit;

    private TuyaOpenApi() {
        this.retrofit = RetrofitInstance.getRetrofitInstance();
    }

    public static synchronized TuyaOpenApi getInstance() {
        if (instance == null) {
            instance = new TuyaOpenApi();
        }
        return instance;
    }

    @NonNull
    private TuyaApiService api() {
        return retrofit.create(TuyaApiService.class);
    }

    @NonNull
    private Map<String, String> buildHeaders(@NonNull DeviceInfo device, @NonNull TuyaSign.SignResult result, @NonNull String token) {
        Map<String, String> headers = new HashMap<>();
        headers.put("client_id", device.getAccessId());
        headers.put("sign", result.sign);
        headers.put("t", String.valueOf(result.timestamp));
        headers.put("sign_method", "HMAC-SHA256");
        headers.put("access_token", token);
        headers.put("lang", "en");
        headers.put("dev_lang", "java");
        headers.put("dev_version", "1.0.0");
        headers.put("dev_channel", "cloud_");
        return headers;
    }

    @NonNull
    private TuyaSign.SignResult sign(DeviceInfo device, String method, String path, Map<String, String> params, JSONObject body, String token) {

        return new TuyaSign(device.getAccessId(), device.getAccessSecret()).calculateSign(method, path, params, body, token);
    }

    @NonNull
    private JSONObject buildCommandBody(@NonNull TuyaCommand body) throws Exception {
        JSONObject obj = new JSONObject();
        JSONArray arr = new JSONArray();
        for (TuyaCommand.Command cmd : body.getCommands()) {
            JSONObject c = new JSONObject();
            c.put("code", cmd.getCode());
            c.put("value", cmd.getValue());
            arr.put(c);
        }
        obj.put("commands", arr);
        return obj;
    }

    private void requestToken(@NonNull String path, @NonNull DeviceInfo device, Map<String, String> params, @NonNull Callback<TuyaTokenResponse> callback) {

        try {
            TuyaSign.SignResult sign = sign(device, "GET", path, params, null, "");

            Map<String, String> headers = buildHeaders(device, sign, "");

            api().getToken(headers, 1).enqueue(callback);

        } catch (Exception e) {
            Log.e("TUYA TOKEN", "EXCEPTION: " + e.getMessage());
        }
    }

    public void requestAccessToken(@NonNull DeviceInfo device, @NonNull Callback<TuyaTokenResponse> callback) {

        Map<String, String> params = new HashMap<>();
        params.put("grant_type", "1");

        requestToken("/v1.0/token", device, params, callback);
    }

    public boolean refreshTokenIfNeeded(@NonNull DeviceInfo device, @NonNull TuyaTokenInfo token, @NonNull Callback<TuyaTokenResponse> callback) {

        long now = System.currentTimeMillis();

        if (token.getExpireTime() - 60_000 > now) {
            return false; // still valid
        }

        String path = "/v1.0/token/" + token.getRefreshToken();

        requestToken(path, device, null, callback);
        return true;
    }

    public void togglePower(@NonNull DeviceInfo device, @NonNull TuyaTokenInfo tokenInfo, boolean state, @NonNull Callback<TuyaCommandResponse> callback) {
        String method = "POST";
        String path = "/v1.0/iot-03/devices/" + device.getDeviceId() + "/commands";

        try {
            TuyaCommand.Command cmd = new TuyaCommand.Command("switch", state);
            TuyaCommand body = new TuyaCommand(Collections.singletonList(cmd));

            JSONObject bodyJson = buildCommandBody(body);

            TuyaSign.SignResult sig = sign(device, method, path, null, bodyJson, tokenInfo.getAccessToken());
            Map<String, String> headers = buildHeaders(device, sig, tokenInfo.getAccessToken());

            api().sendCommand(headers, device.getDeviceId(), body).enqueue(callback);

        } catch (Exception e) {
            Log.e("TUYA CMD", "EXCEPTION: " + e.getMessage());
        }
    }

    public void shadowProperties(@NonNull DeviceInfo device, @NonNull TuyaTokenInfo tokenInfo, @NonNull Callback<TuyaShadowPropertiesResponse> callback) {
        String method = "GET";
        String path = String.format("/v2.0/cloud/thing/%s/shadow/properties", device.getDeviceId());
        try {
            TuyaSign.SignResult sig = sign(device, method, path, null, null, tokenInfo.getAccessToken());
            Map<String, String> headers = buildHeaders(device, sig, tokenInfo.getAccessToken());

            api().getShadowProperties(headers, device.getDeviceId())
                .enqueue(callback);
        } catch (Exception e) {
            Log.e("TUYA CMD", "EXCEPTION: " + e.getMessage());
        }
    }
}
