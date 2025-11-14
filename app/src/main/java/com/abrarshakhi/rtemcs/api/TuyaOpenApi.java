package com.abrarshakhi.rtemcs.api;

import android.util.Log;

import androidx.annotation.NonNull;

import com.abrarshakhi.rtemcs.model.DeviceInfo;
import com.abrarshakhi.rtemcs.model.TuyaCommand;
import com.abrarshakhi.rtemcs.model.TuyaCommandResponse;
import com.abrarshakhi.rtemcs.model.TuyaTokenInfo;
import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;
import com.abrarshakhi.rtemcs.utils.TuyaSign;

import org.jetbrains.annotations.NotNull;
import org.json.JSONArray;
import org.json.JSONObject;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import retrofit2.Call;
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
    private static Map<String, String> makeHeaders(@NonNull DeviceInfo device, TuyaSign.SignResult signResult, String tokenInfo) {
        Map<String, String> headers = new HashMap<>();
        headers.put("client_id", device.getAccessId());
        headers.put("sign", signResult.sign);
        headers.put("t", String.valueOf(signResult.timestamp));
        headers.put("sign_method", "HMAC-SHA256");
        headers.put("access_token", tokenInfo);
        headers.put("lang", "en");
        headers.put("dev_lang", "java");
        headers.put("dev_version", "1.0.0");
        return headers;
    }

    public void requestAccessToken(@NotNull DeviceInfo device, @NotNull Callback<TuyaTokenResponse> callback) {
        try {
            String method = "GET";
            String path = "/v1.0/token";

            Map<String, String> params = new HashMap<>();
            params.put("grant_type", "1");

            TuyaSign.SignResult signResult = new TuyaSign(device.getAccessId(), device.getAccessSecret())
                .calculateSign(method, path, params, null, null);

            final Map<String, String> headers = makeHeaders(device, signResult, "");

            TuyaApiService apiService = retrofit.create(TuyaApiService.class);
            Call<TuyaTokenResponse> call = apiService.getToken(headers, 1);
            call.enqueue(callback);
        } catch (Exception e) {
            Log.e("TUYA TOKEN", "EXCEPTION: " + e.getMessage());
        }
    }

    public void togglePower(@NotNull DeviceInfo device, @NotNull TuyaTokenInfo tokenInfo, boolean power, @NotNull Callback<TuyaCommandResponse> callback) {
        try {
            String method = "POST";
            String path = String.format("/v1.0/iot-03/devices/%s/commands", device.getDeviceId());

            TuyaCommand.Command command = new TuyaCommand.Command("switch", power);
            TuyaCommand body = new TuyaCommand(Collections.singletonList(command));

            JSONObject bodyJson = new JSONObject();
            JSONArray commandsArray = new JSONArray();
            for (TuyaCommand.Command cmd : body.getCommands()) {
                JSONObject cmdObj = new JSONObject();
                cmdObj.put("code", cmd.getCode());
                cmdObj.put("value", cmd.getValue());
                commandsArray.put(cmdObj);
            }
            bodyJson.put("commands", commandsArray);

            TuyaSign.SignResult signResult = new TuyaSign(device.getAccessId(), device.getAccessSecret())
                .calculateSign(method, path, null, bodyJson, tokenInfo.getAccessToken());

            final Map<String, String> headers = makeHeaders(device, signResult, tokenInfo.getAccessToken());

            TuyaApiService apiService = retrofit.create(TuyaApiService.class);
            Call<TuyaCommandResponse> call = apiService.sendCommand(headers, device.getDeviceId(), body);
            call.enqueue(callback);
        } catch (Exception e) {
            Log.e("TUYA TOKEN", "EXCEPTION: " + e.getMessage());
        }
    }
}
