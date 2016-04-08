package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;


public class FetchPendingInvitesApiRequest
{
    @SerializedName("mobile_number")
    private String phone;

    public FetchPendingInvitesApiRequest(String phone) {
        this.phone = phone;
    }
}
