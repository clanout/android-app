package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;

/**
 * Created by aditya on 21/07/15.
 */
public class DeleteEventApiRequest
{
    @SerializedName("plan_id")
    private String eventId;

    public DeleteEventApiRequest(String eventId)
    {
        this.eventId = eventId;
    }
}
