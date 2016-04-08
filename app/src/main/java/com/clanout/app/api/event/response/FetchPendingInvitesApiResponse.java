package com.clanout.app.api.event.response;

import com.clanout.app.model.Event;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harsh on 25/09/15.
 */
public class FetchPendingInvitesApiResponse
{
    @SerializedName("expired_plan_count")
    private int expiredPlanCount;

    @SerializedName("active_plans")
    private List<Event> activeEvents;

    public List<Event> getActiveEvents()
    {
        return activeEvents;
    }

    public int getExpiredCount()
    {
        return expiredPlanCount;
    }
}
