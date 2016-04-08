package com.clanout.app.api.user.request;

import com.google.gson.annotations.SerializedName;

public class UpdateUserLocationApiRequest
{
    @SerializedName("latitude")
    private double latitude;

    @SerializedName("longitude")
    private double longitude;

    public UpdateUserLocationApiRequest(double latitude, double longitude)
    {
        this.longitude = longitude;
        this.latitude = latitude;
    }
}
