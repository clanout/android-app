package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;

public class FetchEventApiRequest
{
    @SerializedName("plan_id")
    private String eventId;

    public FetchEventApiRequest(String eventId)
    {
        this.eventId = eventId;
    }
}
