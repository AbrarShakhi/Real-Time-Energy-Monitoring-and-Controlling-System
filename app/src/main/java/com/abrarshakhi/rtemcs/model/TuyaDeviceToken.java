package com.abrarshakhi.rtemcs.model;

import androidx.annotation.NonNull;
import com.abrarshakhi.rtemcs.api.TuyaOpenApi;

import org.jetbrains.annotations.NotNull;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class TuyaDeviceToken {

    public interface Listener {
        void onSuccess(TuyaDeviceToken token);
        void onError(Throwable t);
    }

    private static final TuyaOpenApi api = TuyaOpenApi.getInstance();

    private final DeviceInfo device;
    public final TuyaTokenInfo token;

    private TuyaDeviceToken(DeviceInfo device, TuyaTokenInfo token) {
        this.device = device;
        this.token = token;
    }

    public static void fetch(@NotNull DeviceInfo device, @NotNull Listener listener) {
        api.requestAccessToken(device, new Callback<>() {

            @Override
            public void onResponse(@NonNull Call<TuyaTokenResponse> call,
                                   @NonNull Response<TuyaTokenResponse> response) {

                TuyaTokenResponse body = response.body();

                if (response.isSuccessful() && body != null && body.isSuccess() && body.getResult() != null) {
                    listener.onSuccess(new TuyaDeviceToken(device, body.getResult()));
                } else {
                    listener.onError(new Exception("Failed to fetch Tuya token"));
                }
            }

            @Override
            public void onFailure(@NonNull Call<TuyaTokenResponse> call,
                                  @NonNull Throwable t) {
                listener.onError(t);
            }
        });
    }

    public DeviceInfo getDevice() {
        return device;
    }
}
