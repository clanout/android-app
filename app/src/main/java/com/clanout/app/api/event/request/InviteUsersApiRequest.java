package com.clanout.app.api.event.request;

import com.clanout.app.api.core.GsonProvider;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class InviteUsersApiRequest
{
    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("invitee")
    private List<String> inviteeList;

    @SerializedName("invitee_mobile")
    private List<String> mobileNumbers;

    public InviteUsersApiRequest(String eventId, List<String> inviteeList, List<String> mobileNumbers)
    {
        this.eventId = eventId;
        this.inviteeList = inviteeList;
        this.mobileNumbers = mobileNumbers;
    }
}
