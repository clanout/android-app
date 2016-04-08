package com.clanout.app.api.user.request;

import com.google.gson.annotations.SerializedName;

public class GetFacebookFriendsApiRequest
{
    @SerializedName("location_zone")
    private String zone;

    public GetFacebookFriendsApiRequest(String zone)
    {
        this.zone = zone;
    }
}
