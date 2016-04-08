package com.clanout.app.api.auth;

import com.clanout.app.api.auth.response.CreateSessionApiResponse;
import com.clanout.app.api.auth.response.RefreshSessionApiResponse;

import retrofit.http.GET;
import retrofit.http.Header;
import rx.Observable;

public interface AuthApi
{
    @GET("/auth/token")
    Observable<CreateSessionApiResponse> createSession(@Header("X-Authentication-Method") String authMethod, @Header("X-Authentication-Token") String authToken);

    @GET("/auth/refresh")
    Observable<RefreshSessionApiResponse> refreshSession(@Header("Authorization") String refreshToken);
}
