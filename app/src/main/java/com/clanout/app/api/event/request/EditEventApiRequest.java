package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

public class EditEventApiRequest
{
    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("start_time")
    private DateTime startTime;

    @SerializedName("end_time")
    private DateTime endTime;

    @SerializedName("latitude")
    private Double locationLatitude;

    @SerializedName("longitude")
    private Double locationLongitude;

    @SerializedName("location_name")
    private String locationName;

    @SerializedName("description")
    private String description;

    public EditEventApiRequest(Double locationLongitude, String description, DateTime endTime, String eventId, Double locationLatitude, String locationName, DateTime startTime)
    {
        this.locationLongitude = locationLongitude;
        this.description = description;
        this.endTime = endTime;
        this.eventId = eventId;
        this.locationLatitude = locationLatitude;
        this.locationName = locationName;
        this.startTime = startTime;
    }
}
