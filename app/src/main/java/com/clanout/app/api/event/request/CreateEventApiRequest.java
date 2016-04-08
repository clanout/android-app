package com.clanout.app.api.event.request;

import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;


/**
 * Created by aditya on 04/07/15.
 */
public class CreateEventApiRequest
{
    @SerializedName("title")
    private String eventTitle;

    @SerializedName("type")
    private Event.Type eventType;

    @SerializedName("category")
    private EventCategory eventCategory;

    @SerializedName("description")
    private String description;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("location_zone")
    private String locationZone;

    @SerializedName("latitude")
    private Double latitude;

    @SerializedName("longitude")
    private Double longitude;

    @SerializedName("start_time")
    private DateTime startTime;

    @SerializedName("end_time")
    private DateTime endTime;

    public CreateEventApiRequest(String eventTitle, Event.Type eventType, EventCategory eventCategory, String description, String locationName, String locationZone, Double latitude, Double longitude, DateTime startTime, DateTime endTime)
    {
        this.eventTitle = eventTitle;
        this.eventType = eventType;
        this.eventCategory = eventCategory;
        this.description = description;
        this.locationName = locationName;
        this.locationZone = locationZone;
        this.latitude = latitude;
        this.longitude = longitude;
        this.startTime = startTime;
        this.endTime = endTime;
    }
}
