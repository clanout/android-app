package com.clanout.app.model;

import android.support.annotation.IntDef;

import org.joda.time.DateTime;

import java.util.List;

public class NotificationWrapper
{
    @Type
    private int type;
    private String eventId;
    private String title;
    private List<Integer> notificationIds;
    private List<NotificationItem> notificationItems;
    private DateTime timestamp;

    public NotificationWrapper(@Type int type, String eventId, String title,
                               List<Integer> notificationIds, List<NotificationItem> notificationItems,
                               DateTime timestamp)
    {
        this.type = type;
        this.eventId = eventId;
        this.title = title;
        this.notificationIds = notificationIds;
        this.notificationItems = notificationItems;
        this.timestamp = timestamp;
    }

    public int getType()
    {
        return type;
    }

    public String getEventId()
    {
        return eventId;
    }

    public String getTitle()
    {
        return title;
    }

    public List<Integer> getNotificationIds()
    {
        return notificationIds;
    }

    public List<NotificationItem> getNotificationItems()
    {
        return notificationItems;
    }

    public DateTime getTimestamp()
    {
        return timestamp;
    }

    public void setTimestamp(DateTime timestamp)
    {
        this.timestamp = timestamp;
    }

    public static class NotificationItem
    {
        @Type
        private int type;
        private String message;

        public NotificationItem(@Type int type, String message)
        {
            this.type = type;
            this.message = message;
        }

        public int getType()
        {
            return type;
        }

        public String getMessage()
        {
            return message;
        }

        @IntDef({
                Type.INVITATION,
                Type.NEW_FRIEND_JOINED_APP,
                Type.EVENT_REMOVED,
                Type.EVENT_UPDATED,
                Type.NEW_CHAT,
                Type.FRIEND_JOINED_EVENT
        })
        public @interface Type
        {
            int INVITATION = 0;
            int NEW_FRIEND_JOINED_APP = 1;
            int EVENT_REMOVED = 2;
            int EVENT_UPDATED = 3;
            int NEW_CHAT = 4;
            int FRIEND_JOINED_EVENT = 5;
        }
    }

    @IntDef({
            Type.EVENT_ACTIVITY,
            Type.EVENT_INVITATION,
            Type.EVENT_REMOVED,
            Type.NEW_FRIEND_JOINED_APP
    })
    public @interface Type
    {
        int EVENT_ACTIVITY = 0;
        int EVENT_INVITATION = 1;
        int EVENT_REMOVED = 2;
        int NEW_FRIEND_JOINED_APP = 3;
    }
}
