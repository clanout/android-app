package com.clanout.app.model;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.config.AppConstants;

import org.joda.time.DateTime;

public class ChatMessage implements Model
{
    public static final ChatMessage FIRST_MESSAGE = new ChatMessage();

    static
    {
        FIRST_MESSAGE.setId("chat_created");
    }


    private String id;
    private String message;
    private String senderName;
    private String senderId;
    private DateTime timestamp;
    private String planId;
    private String planTitle;

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getSenderId()
    {
        return senderId;
    }

    public void setSenderId(String senderId)
    {
        this.senderId = senderId;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public String getSenderName()
    {
        return senderName;
    }

    public void setSenderName(String senderName)
    {
        this.senderName = senderName;
    }

    public DateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public boolean isAdmin()
    {
        return senderId.equalsIgnoreCase(AppConstants.CHAT_ADMIN_ID);
    }

    public String getPlanId()
    {
        return planId;
    }

    public void setPlanId(String planId)
    {
        this.planId = planId;
    }

    public String getPlanTitle()
    {
        return planTitle;
    }

    public void setPlanTitle(String planTitle)
    {
        this.planTitle = planTitle;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof ChatMessage))
        {
            return false;
        }

        ChatMessage that = (ChatMessage) o;

        return id.equals(that.id);

    }

    @Override
    public int hashCode()
    {
        return id.hashCode();
    }
}
