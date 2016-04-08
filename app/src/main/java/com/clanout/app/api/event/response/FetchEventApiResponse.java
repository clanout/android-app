package com.clanout.app.api.event.response;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;

public class FetchEventApiResponse
{
    @SerializedName("plan")
    private Event event;

    public FetchEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
