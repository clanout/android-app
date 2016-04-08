package com.clanout.app.api.user.request;

import com.clanout.app.api.core.GsonProvider;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetRegisteredContactsApiRequest
{
    @SerializedName("mobile_numbers")
    private List<String> allContacts;

    @SerializedName("location_zone")
    private String zone;

    public GetRegisteredContactsApiRequest(List<String> allContacts, String zone)
    {
        this.allContacts = allContacts;
        this.zone = zone;
    }
}
