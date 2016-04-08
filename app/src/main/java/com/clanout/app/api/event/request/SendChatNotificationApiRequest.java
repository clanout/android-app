package com.clanout.app.api.event.request;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.model.ChatMessage;
import com.google.gson.annotations.SerializedName;

import org.joda.time.DateTime;

/**
 * Created by harsh on 24/09/15.
 */
public class SendChatNotificationApiRequest
{

    @SerializedName("plan_id")
    private String eventId;

    @SerializedName("message")
    private String message;

    public SendChatNotificationApiRequest(String eventId, ChatMessage message)
    {
        this.eventId = eventId;
        this.message = GsonProvider.getGson().toJson(message);
    }
}
