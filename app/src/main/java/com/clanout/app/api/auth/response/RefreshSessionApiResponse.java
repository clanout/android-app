package com.clanout.app.api.auth.response;

import com.google.gson.annotations.SerializedName;

import retrofit.http.GET;
import rx.Observable;

/**
 * Created by harsh on 07/04/16.
 */
public class RefreshSessionApiResponse
{
    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("access_token")
    private String accessToken;

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public String getAccessToken()
    {
        return accessToken;
    }
}
