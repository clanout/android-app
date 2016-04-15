package com.clanout.app.model;

import com.clanout.app.api.core.GsonProvider;

import org.joda.time.DateTime;

import java.util.HashMap;
import java.util.Map;

public class Notification
{
    public static final int EVENT_CREATED = 0;
    public static final int EVENT_INVITATION = 1;
    public static final int RSVP = 2;
    public static final int EVENT_REMOVED = 3;
    public static final int EVENT_UPDATED = 4;
    public static final int BLOCKED = 5;
    public static final int UNBLOCKED = 6;
    public static final int FRIEND_RELOCATED = 7;
    public static final int NEW_FRIEND_ADDED = 8;
    public static final int CHAT = 9;
    public static final int STATUS = 10;
    public static final int PLAN_REMOVE_FROM_FEED = 11;

    private int id;
    private int type;
    private String title;
    private String eventId;
    private String eventName;
    private String userId;
    private String userName;
    private String message;
    private DateTime timestamp;
    private DateTime timestampReceived;
    private boolean isNew;
    private Map<String, String> args;

    private Notification(int id)
    {
        this.id = id;
        timestampReceived = DateTime.now();
        args = new HashMap<>();
    }

    public int getId()
    {
        return id;
    }

    public int getType()
    {
        return type;
    }

    public String getEventId()
    {
        return eventId;
    }

    public String getEventName()
    {
        return eventName;
    }

    public String getUserId()
    {
        return userId;
    }

    public String getUserName()
    {
        return userName;
    }

    public String getMessage()
    {
        return message;
    }

    public void setMessage(String message)
    {
        this.message = message;
    }

    public DateTime getTimestamp()
    {
        return timestamp;
    }

    public DateTime getTimestampReceived()
    {
        return timestampReceived;
    }

    public String getTitle()
    {
        return title;
    }

    public boolean isNew()
    {
        return isNew;
    }

    public Map<String, String> getArgs()
    {
        return args;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof Notification))
        {
            return false;
        }

        Notification that = (Notification) o;

        return id == that.id;

    }

    @Override
    public int hashCode()
    {
        return id;
    }

    @Override
    public String toString()
    {
        return GsonProvider.getGson().toJson(this);
    }

    public static class Builder
    {
        private Notification notification;

        public Builder(int id)
        {
            notification = new Notification(id);
        }

        public Notification build()
        {
            return notification;
        }

        public Builder type(int type)
        {
            notification.type = type;
            return this;
        }

        public Builder eventId(String eventId)
        {
            notification.eventId = eventId;
            return this;
        }

        public Builder eventName(String eventName)
        {
            notification.eventName = eventName;
            return this;
        }

        public Builder userId(String userId)
        {
            notification.userId = userId;
            return this;
        }

        public Builder userName(String userName)
        {
            notification.userName = userName;
            return this;
        }

        public Builder timestamp(DateTime timestamp)
        {
            notification.timestamp = timestamp;
            return this;
        }

        public Builder message(String message)
        {
            notification.message = message;
            return this;
        }

        public Builder title(String title)
        {
            notification.title = title;
            return this;
        }

        public Builder isNew(boolean isNew)
        {
            notification.isNew = isNew;
            return this;
        }

        public Builder args(Map<String, String> args)
        {
            notification.args = args;
            return this;
        }
    }
}
