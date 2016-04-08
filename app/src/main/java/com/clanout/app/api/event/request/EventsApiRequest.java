package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class EventsApiRequest
{
    @SerializedName("last_updated")
    private DateTime lastUpdated;

    public EventsApiRequest(DateTime lastUpdated)
    {
        this.lastUpdated = lastUpdated;
    }
}
