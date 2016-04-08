package com.clanout.app.api.event.response;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;


/**
 * Created by aditya on 21/07/15.
 */
public class EditEventApiResponse
{
    @SerializedName("event")
    private Event event;

    public EditEventApiResponse(Event event)
    {
        this.event = event;
    }

    public Event getEvent()
    {
        return event;
    }
}
