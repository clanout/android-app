package com.clanout.app.api.user.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aditya on 02/07/15.
 */
public class UpdateMobileAPiRequest
{
    @SerializedName("mobile_number")
    private String phoneNumber;

    public UpdateMobileAPiRequest(String phoneNumber){
        this.phoneNumber = phoneNumber;
    }
}
