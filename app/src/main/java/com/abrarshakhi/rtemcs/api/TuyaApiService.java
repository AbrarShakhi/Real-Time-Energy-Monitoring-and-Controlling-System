package com.abrarshakhi.rtemcs.api;

import com.abrarshakhi.rtemcs.model.TuyaCommand;
import com.abrarshakhi.rtemcs.model.TuyaCommandResponse;
import com.abrarshakhi.rtemcs.model.TuyaShadowPropertiesResponse;
import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.POST;
import retrofit2.http.Path;
import retrofit2.http.Query;

public interface TuyaApiService {

    @GET("/v1.0/token")
    Call<TuyaTokenResponse> getToken(
        @HeaderMap Map<String, String> headers,
        @Query("grant_type") int grantType
    );

    @POST("/v1.0/iot-03/devices/{device_id}/commands")
    Call<TuyaCommandResponse> sendCommand(
        @HeaderMap Map<String, String> headers,
        @Path("device_id") String deviceId,
        @Body TuyaCommand body
    );

    @GET("/v2.0/cloud/thing/{device_id}/shadow/properties")
    Call<TuyaShadowPropertiesResponse> getShadowProperties(
        @HeaderMap Map<String, String> headers,
        @Path("device_id") String deviceId
    );

    @GET("/v1.0/token/{refresh_token}")
    Call<TuyaTokenResponse> getNewToken(
        @HeaderMap Map<String, String> headers,
        @Path("refresh_token") String refreshToken
    );
}
