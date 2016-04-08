package com.clanout.app.api.event.request;

import com.clanout.app.api.core.GsonProvider;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harsh on 27/09/15.
 */
public class InviteThroughSMSApiRequest
{

    @SerializedName("event_id")
    private String eventId;

    @SerializedName("phone_numbers")
    private String phoneNumbers;

    public InviteThroughSMSApiRequest(String eventId, List<String> phoneNumbers) {
        this.eventId = eventId;
        this.phoneNumbers = GsonProvider.getGson().toJson(phoneNumbers);
    }
}
