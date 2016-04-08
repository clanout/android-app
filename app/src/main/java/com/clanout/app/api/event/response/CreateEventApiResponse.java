package com.clanout.app.api.event.response;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;

/**
 * Created by aditya on 04/07/15.
 */
public class CreateEventApiResponse
{
    @SerializedName("plan")
    private Event event;

    public CreateEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
