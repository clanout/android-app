package com.clanout.app.api.notification.request;

import com.google.gson.annotations.SerializedName;

public class GCmRegisterUserApiRequest
{
    @SerializedName("gcm_token")
    private String token;

    public GCmRegisterUserApiRequest(String token)
    {
        this.token = token;
    }
}
