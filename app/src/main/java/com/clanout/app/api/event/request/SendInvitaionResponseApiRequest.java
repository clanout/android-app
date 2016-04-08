package com.clanout.app.api.event.request;

import com.google.gson.annotations.SerializedName;


/**
 * Created by harsh on 19/10/15.
 */
public class SendInvitaionResponseApiRequest
{

    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("invitation_response")
    private String message;

    public SendInvitaionResponseApiRequest(String eventId, String message) {
        this.eventId = eventId;
        this.message = message;
    }
}
