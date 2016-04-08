package com.clanout.app.api.auth.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsh on 07/04/16.
 */
public class CreateSessionApiResponse
{
    @SerializedName("access_token")
    private String accessToken;

    @SerializedName("refresh_token")
    private String refreshToken;

    @SerializedName("is_new_user")
    private boolean isNew;

    public String getAccessToken()
    {
        return accessToken;
    }

    public String getRefreshToken()
    {
        return refreshToken;
    }

    public boolean isNew()
    {
        return isNew;
    }
}
