package com.abrarshakhi.rtemcs.api;

import com.abrarshakhi.rtemcs.model.TuyaTokenResponse;

import retrofit2.Call;
import retrofit2.http.GET;
import retrofit2.http.Query;

public interface TuyaApiService {

    @GET("/v1.0/token")
    Call<TuyaTokenResponse> getToken(@Query("grant_type") int grantType);
}
