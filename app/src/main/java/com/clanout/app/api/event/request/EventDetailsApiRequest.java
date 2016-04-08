package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;

public class EventDetailsApiRequest
{
    @SerializedName("event_id")
    private String eventId;

    public EventDetailsApiRequest(String eventId)
    {
        this.eventId = eventId;
    }
}
