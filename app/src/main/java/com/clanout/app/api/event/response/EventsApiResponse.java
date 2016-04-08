package com.clanout.app.api.event.response;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

import java.util.List;

public class EventsApiResponse
{
    @SerializedName("plans")
    private List<Event> events;

    @SerializedName("updated_at")
    private DateTime updatedAt;

    public List<Event> getEvents()
    {
        return events;
    }

    public DateTime getUpdatedAt()
    {
        return updatedAt;
    }
}
