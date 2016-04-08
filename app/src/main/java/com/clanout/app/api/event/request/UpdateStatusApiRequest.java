package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;


/**
 * Created by harsh on 19/10/15.
 */
public class UpdateStatusApiRequest
{

    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("status")
    private String status;

    @SerializedName("is_last_moment")
    private boolean shouldNotifyFriends;

    public UpdateStatusApiRequest(String eventId, String status, boolean shouldNotifyFriends) {
        this.eventId = eventId;
        this.status = status;
        this.shouldNotifyFriends = shouldNotifyFriends;
    }
}
