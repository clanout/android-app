package com.clanout.app.model.util;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.notification.NotificationCache;
import com.clanout.app.config.NotificationConstants;
import com.clanout.app.config.NotificationMessages;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.Notification;

import java.util.Map;

public class NotificationHelper
{
    private static NotificationCache notificationCache = CacheManager.getNotificationCache();

    public static int getType(String name)
    {
        if (name.equals(NotificationConstants.EVENT_CREATED))
        {
            return Notification.EVENT_CREATED;
        }else if(name.equals(NotificationConstants.EVENT_INVITATION))
        {
            return Notification.EVENT_INVITATION;
        }else if(name.equals(NotificationConstants.FRIEND_JOINED_EVENT))
        {
            return Notification.RSVP;
        }else if(name.equals(NotificationConstants.EVENT_REMOVED))
        {
            return Notification.EVENT_REMOVED;
        }else if(name.equals(NotificationConstants.EVENT_UPDATED))
        {
            return Notification.EVENT_UPDATED;
        }else if(name.equals(NotificationConstants.BLOCKED))
        {
            return Notification.BLOCKED;
        }else if(name.equals(NotificationConstants.UNBLOCKED))
        {
            return Notification.UNBLOCKED;
        }else if(name.equals(NotificationConstants.FRIEND_RELOCATED))
        {
            return Notification.FRIEND_RELOCATED;
        }else if(name.equals(NotificationConstants.NEW_FRIEND_JOINED))
        {
            return Notification.NEW_FRIEND_ADDED;
        }else if(name.equals(NotificationConstants.CHAT))
        {
            return Notification.CHAT;
        }else if(name.equals(NotificationConstants.STATUS))
        {
            return Notification.STATUS;
        }
        else if(name.equals(NotificationConstants.PLAN_REMOVE_FROM_FEED))
        {
            return Notification.PLAN_REMOVE_FROM_FEED;
        }
        else
        {
            throw new IllegalArgumentException("invalid event type [" + name + "]");
        }
    }

    public static String getMessage(int type, Map<String, String> args)
    {
        switch (type)
        {
            case Notification.EVENT_CREATED:
                return getEventCreatedMessage(args);
            case Notification.EVENT_INVITATION:
                return getEventInvitationMessage(args);
            case Notification.RSVP:
                return getRSVPChangeMessage(args);
            case Notification.EVENT_REMOVED:
                return getEventRemovedMessage(args);
            case Notification.EVENT_UPDATED:
                return getEventUpdatedMessage(args);
            case Notification.BLOCKED:
                return getBlockedMessage(args);
            case Notification.UNBLOCKED:
                return getUnblockedMessage(args);
            case Notification.FRIEND_RELOCATED:
                return getFriendRelocatedMessage(args);
            case Notification.NEW_FRIEND_ADDED:
                return newFriendJoinedAppMessage(args);
            case Notification.CHAT:
                return newChatMessageReceivedMessage(args);
            case Notification.STATUS:
                return newStatusUpdateReceived(args);
            case Notification.PLAN_REMOVE_FROM_FEED:
                return newPlanRemoveFromFeedMessage(args);
            default:
                return "";
        }
    }

    private static String newPlanRemoveFromFeedMessage(Map<String, String> args)
    {
        return NotificationMessages.PLAN_REMOVE_FROM_FEED;
    }

    private static String newStatusUpdateReceived(Map<String, String> args) {

        return String.format(NotificationMessages.NEW_STATUS_UPDATED, args.get("user_name"), args.get("plan_title"));
    }

    private static String newChatMessageReceivedMessage(Map<String, String> args) {

        String chatMessageJson = args.get("message");
        ChatMessage chatMessage = GsonProvider.getGson().fromJson(chatMessageJson, ChatMessage.class);

        return String.format(NotificationMessages.NEW_CHAT_MESSAGE, chatMessage.getPlanTitle(), chatMessage.getMessage());
    }

    private static String newFriendJoinedAppMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.NEW_FRIEND_JOINED_APP, args.get("user_name"));
    }

    private static String getFriendRelocatedMessage(Map<String, String> args)
    {
        return NotificationMessages.FRIEND_RELOCATED;
    }

    private static String getUnblockedMessage(Map<String, String> args)
    {
        return NotificationMessages.UNBLOCKED;
    }

    private static String getBlockedMessage(Map<String, String> args)
    {
        return NotificationMessages.BLOCKED;

    }

    private static String getEventUpdatedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_UPDATED, args.get("user_name"), args.get("plan_title"));
    }

    private static String getEventRemovedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_REMOVED, args.get("user_name"), args.get("plan_title"));
    }

    private static String getRSVPChangeMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.RSVP_CHANGED, args.get("plan_title"));
    }

    private static String getEventInvitationMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_INVITATION, args.get("user_name"), args.get("plan_title"));
    }

    private static String getEventCreatedMessage(Map<String, String> args)
    {
        return String.format(NotificationMessages.EVENT_CREATED, args.get("user_name"), args.get("plan_title"));
    }

}
