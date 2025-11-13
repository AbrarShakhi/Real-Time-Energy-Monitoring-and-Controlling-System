package com.abrarshakhi.rtemcs.api;

import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;

import java.util.Map;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.HeaderMap;
import retrofit2.http.Query;

public interface TuyaApiService {

    @GET("/v1.0/token")
    Call<TuyaTokenResponse> getToken(
        @HeaderMap Map<String, String> headers,
        @Query("grant_type") int grantType
    );
}
