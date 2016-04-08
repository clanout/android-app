package com.clanout.app.api.event.request;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;

public class RsvpUpdateApiRequest
{
    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("rsvp")
    private Event.RSVP rsvp;

    public RsvpUpdateApiRequest(String eventId, Event.RSVP rsvp)
    {
        this.eventId = eventId;
        this.rsvp = rsvp;
    }
}
