package com.clanout.app.api.user.response;

import com.google.gson.annotations.SerializedName;

public class UpdateUserLocationApiResponse
{
    @SerializedName("is_relocated")
    private boolean isRelocated;

    @SerializedName("location_zone")
    private String zone;

    @SerializedName("location_name")
    private String name;

    public boolean isRelocated()
    {
        return isRelocated;
    }

    public String getZone()
    {
        return zone;
    }

    public String getName()
    {
        return name;
    }
}
