package com.clanout.app.model.util;

import android.os.Bundle;
import android.util.Log;
import android.util.Pair;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.notification.NotificationCache;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.NotificationMessages;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.Notification;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class NotificationFactory
{
    private static final String TITLE = "clanOut";
    private static NotificationCache notificationCache = CacheManager.getNotificationCache();

    private static Type TYPE = new TypeToken<Map<String, String>>()
    {
    }.getType();

    public static Observable<Notification> create(Bundle data)
    {
        try {
            String type = data.getString("type");
            Map<String, String> args = GsonProvider.getGson()
                    .fromJson(data.getString("parameters"), TYPE);

            int typeCode = NotificationHelper.getType(type);

            Log.d("NOTIFICATION", "typeCode" + typeCode);

            if (shouldBuildNotification(typeCode, args)) {
                return buildNotification(typeCode, args);
            }
            else {

                return Observable.just(null);
            }

        }
        catch (Exception e) {
            Timber.e("Unable to create notification [" + e.getMessage() + "]");
            throw new IllegalStateException("Exception in NotificationFactory.create()");
        }
    }

    private static boolean shouldBuildNotification(int typeCode, Map<String, String> args)
    {
        Set<String> notGoingEvents = getNotGoingEvents();

        if (args.get("plan_id") == null) {

            return true;
        }
        else {

            if (notGoingEvents == null) {

                return true;
            }
            else {

                if (typeCode == Notification.EVENT_INVITATION) {

                    return true;
                }
                else {

                    return !notGoingEvents.contains(args.get("plan_id"));
                }
            }
        }
    }


    private static Observable<Notification> buildNotification(int typeCode, Map<String, String>
            args)
    {
        switch (typeCode) {
            case Notification.CHAT:
                return buildChatNotification(args);

            case Notification.BLOCKED:
                return buildBlockedNotification(args);

            case Notification.UNBLOCKED:
                return buildUnblockedNotification(args);

            case Notification.EVENT_CREATED:
                return buildEventCreatedNotification(args);

            case Notification.NEW_FRIEND_ADDED:
                return buildNewFriendJoinedAppNotification(args);

            case Notification.EVENT_UPDATED:
                return buildNewEventUpdatedNotification(args);

            case Notification.RSVP:
                return buildNewRsvpUpdatedNotification(args);

            case Notification.FRIEND_RELOCATED:
                return buildFriendRelocatedNotification(args);

            case Notification.STATUS:
                return buildStatusNotification(args);

            case Notification.EVENT_REMOVED:
                return buildEventRemovedNotification(args);

            case Notification.EVENT_INVITATION:
                return buildEventInvitationNotification(args);

            case Notification.PLAN_REMOVE_FROM_FEED:
                return buildPlanRemoveFromFeed(args);

            default:
                throw new IllegalArgumentException("Notification Type Invalid");
        }
    }

    private static Observable<Notification> buildPlanRemoveFromFeed(final Map<String, String> args)
    {
        return notificationCache.getAllForEvent(args.get("plan_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(final List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .PLAN_REMOVE_FROM_FEED, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.PLAN_REMOVE_FROM_FEED, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildEventInvitationNotification(final Map<String,
            String>
                                                                                     args)
    {
        Observable<List<Notification>> getAllInvitationsForEvent = notificationCache.getAll
                (Notification.EVENT_INVITATION, args.get("plan_id"));
        Observable<List<Notification>> getCreateNotificationsForEvent = notificationCache.getAll
                (Notification.EVENT_CREATED, args.get("plan_id"));

        return Observable
                .zip(getAllInvitationsForEvent,
                        getCreateNotificationsForEvent,
                        new Func2<List<Notification>, List<Notification>,
                                Pair<List<Notification>, List<Notification>>>()
                        {
                            @Override
                            public Pair<List<Notification>, List<Notification>> call
                                    (List<Notification> inviteNotifs,
                                     List<Notification> createNotifs)
                            {
                                return new Pair<>(inviteNotifs, createNotifs);
                            }
                        })
                .flatMap(new Func1<Pair<List<Notification>, List<Notification>>,
                        Observable<List<Notification>>>()
                {
                    @Override
                    public Observable<List<Notification>> call(final Pair<List<Notification>,
                            List<Notification>> notifs)
                    {
                        List<Integer> createNotificationIds = getNotificationIdsList(notifs.second);
                        return notificationCache
                                .clear(createNotificationIds)
                                .flatMap(new Func1<Boolean, Observable<List<Notification>>>()
                                {
                                    @Override
                                    public Observable<List<Notification>> call(Boolean aBoolean)
                                    {
                                        return Observable.just(notifs.first);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<List<Notification>, Observable<Integer>>()
                         {
                             @Override
                             public Observable<Integer> call(final List<Notification> notifications)
                             {
                                 List<Integer> notificationIds = getNotificationIdsList
                                         (notifications);
                                 return notificationCache
                                         .clear(notificationIds)
                                         .flatMap(new Func1<Boolean, Observable<Integer>>()
                                         {
                                             @Override
                                             public Observable<Integer> call(Boolean aBoolean)
                                             {
                                                 return calculatePreviousInviteeCount
                                                         (notifications);
                                             }
                                         });
                             }
                         }
                )
                .flatMap(new Func1<Integer, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Integer previousInviteeCount)
                    {

                        String message = getInviteNotificationMessage(previousInviteeCount, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_INVITATION, args, message);

                        return Observable.just(notification);
                    }
                });
    }

    private static Observable<Notification> buildEventRemovedNotification(final Map<String,
            String> args)
    {
        return notificationCache.getAllForEvent(args.get("plan_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(final List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .EVENT_REMOVED, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_REMOVED, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildStatusNotification(final Map<String, String> args)
    {
        return notificationCache.getAll(Notification.STATUS, args.get("plan_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .STATUS, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.STATUS, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildFriendRelocatedNotification(Map<String, String>
                                                                                     args)

    {
        String message = NotificationHelper.getMessage(Notification.FRIEND_RELOCATED, args);

        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.FRIEND_RELOCATED,
                        args, message)
        );
    }

    private static Observable<Notification> buildNewRsvpUpdatedNotification(final Map<String,
            String> args)

    {
        return notificationCache.getAll(Notification.RSVP, args.get("plan_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .RSVP, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.RSVP, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildNewEventUpdatedNotification(final Map<String,
            String> args)

    {
        return notificationCache.getAll(Notification.EVENT_UPDATED, args.get("plan_id"))
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = getNotificationIdsList(notifications);

                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {
                        String message = NotificationHelper.getMessage(Notification
                                .EVENT_UPDATED, args);

                        Notification notification = getNotificationObjectHavingEventInformation
                                (Notification.EVENT_UPDATED, args, message);
                        return Observable.just(notification);
                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Observable<Notification> buildNewFriendJoinedAppNotification(final Map<String,
            String> args)
    {
        Set<String> newFriends = getNewFriends();

        if (newFriends == null) {
            Set<String> newFriendsSet = new HashSet<>();
            newFriendsSet.add(args.get("user_id"));
            CacheManager.getGenericCache().put(GenericCacheKeys.NEW_FRIENDS_LIST, newFriendsSet);
        }
        else {

            newFriends.add(args.get("user_id"));
            CacheManager.getGenericCache().put(GenericCacheKeys.NEW_FRIENDS_LIST, newFriends);
        }

        return notificationCache.getAllForType(Notification.NEW_FRIEND_ADDED)
                .flatMap(new Func1<List<Notification>, Observable<Integer>>()
                {
                    @Override
                    public Observable<Integer> call(final List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();

                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds)
                                .flatMap(new Func1<Boolean, Observable<Integer>>()
                                {
                                    @Override
                                    public Observable<Integer> call(Boolean aBoolean)
                                    {
                                        return calculateNumberOfNewFriendsInApp
                                                (notifications);
                                    }
                                });
                    }
                })
                .flatMap(new Func1<Integer, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Integer newFriendsAlreadyOnApp)
                    {
                        String message = getNewFriendJoinedAppMessage(newFriendsAlreadyOnApp, args);

                        return Observable.just(
                                getNotificationObjectNotHavingEventInformation(Notification
                                                .NEW_FRIEND_ADDED,
                                        args, message)
                        );
                    }
                });


    }

    private static Observable<Notification> buildEventCreatedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.EVENT_CREATED, args);

        return Observable.just(
                getNotificationObjectHavingEventInformation(Notification.EVENT_CREATED, args,
                        message)
        );
    }

    private static Observable<Notification> buildUnblockedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.UNBLOCKED, args);

        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.UNBLOCKED, args,
                        message)
        );
    }

    private static Observable<Notification> buildBlockedNotification(Map<String, String> args)
    {
        String message = NotificationHelper.getMessage(Notification.BLOCKED, args);

        return Observable.just(
                getNotificationObjectNotHavingEventInformation(Notification.BLOCKED, args, message)
        );

    }

    private static Observable<Notification> buildChatNotification(final Map<String, String> args)
    {
        String chatMessageJson = args.get("message");
        final ChatMessage chatMessage = GsonProvider.getGson().fromJson(chatMessageJson, ChatMessage.class);

        return notificationCache
                .getAll(Notification.CHAT, chatMessage.getPlanId())
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {

                        Log.d("NOTIFICATION", "size ---- " + notifications.size());

                        List<Integer> notificationIds = getNotificationIdsList(notifications);
                        return notificationCache.clear(notificationIds);
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Notification>>()
                {
                    @Override
                    public Observable<Notification> call(Boolean isDeleteSuccessful)
                    {

                        Log.d("NOTIFICATION", "isDeleteSuccessful ---- " + isDeleteSuccessful);

                        String message = NotificationHelper.getMessage(Notification.CHAT, args);

                        Notification notification = new Notification.Builder(Integer.parseInt
                                (args.get("notification_id")))
                                .type(Notification.CHAT)
                                .title(chatMessage.getPlanTitle())
                                .eventId(chatMessage.getPlanId())
                                .eventName(chatMessage.getPlanTitle())
                                .userId(chatMessage.getSenderId())
                                .userName("")
                                .timestamp(DateTime.now())
                                .message(message)
                                .isNew(true)
                                .args(args)
                                .build();
                        return Observable.just(notification);

                    }
                })
                .onErrorReturn(new Func1<Throwable, Notification>()
                {
                    @Override
                    public Notification call(Throwable throwable)
                    {
                        return null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private static Notification getNotificationObjectHavingEventInformation(int typeCode,
                                                                            Map<String, String>
                                                                                    args,
                                                                            String message)
    {
        return new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                .type(typeCode)
                .title(args.get("plan_title"))
                .eventId(args.get("plan_id"))
                .eventName(args.get("plan_title"))
                .userId(args.get("user_id"))
                .userName("user_name")
                .timestamp(DateTime.now())
                .message(message)
                .isNew(true)
                .args(args)
                .build();
    }

    private static Notification getNotificationObjectNotHavingEventInformation(int typeCode,
                                                                               Map<String, String>
                                                                                       args,
                                                                               String message)
    {
        return new Notification.Builder(Integer.parseInt(args.get("notification_id")))
                .type(typeCode)
                .title(TITLE)
                .eventId("")
                .eventName("")
                .userId("")
                .userName("")
                .timestamp(DateTime.now())
                .message(message)
                .isNew(true)
                .args(args)
                .build();
    }

    private static String getInviteNotificationMessage(Integer previousInviteeCount, Map<String,
            String> args)
    {
        String message = "";

        if (previousInviteeCount == 0) {
            message = NotificationHelper.getMessage(Notification
                    .EVENT_INVITATION, args);

        }
        else {

            message = String.format(NotificationMessages.EVENT_INVITATION,
                    args.get
                            ("user_name") + " & " + previousInviteeCount + " others",
                    args.get("plan_name"));

        }

        return message;
    }

    private static Observable<Integer> calculatePreviousInviteeCount(List<Notification>
                                                                             notifications)
    {
        if (notifications.size() != 0) {

            String message = notifications.get(0).getMessage();

            return Observable.just(getInviteeCountFromMessage(message));

        }
        else {
            return Observable.just(0);
        }
    }

    private static List<Integer> getNotificationIdsList(List<Notification> notifications)
    {
        List<Integer> notificationIds = new ArrayList<Integer>();
        for (Notification notification : notifications) {
            notificationIds.add(notification.getId());
        }

        return notificationIds;
    }

    private static Integer getInviteeCountFromMessage(String message)
    {
        try {
            if (message.contains("others")) {

                String[] wordArray = message.split(" ");

                try {

                    return Integer.valueOf(wordArray[6]) + 1;
                }
                catch (Exception e) {
                    try {
                        return Integer.valueOf(wordArray[5]) + 1;
                    }
                    catch (Exception exception) {
                        return 0;
                    }
                }

            }
            else if (message.contains("Join")) {

                return 1;

            }
            else {

                return 0;
            }
        }
        catch (Exception e) {
            return 0;
        }

    }

    private static Observable<Integer> calculateNumberOfNewFriendsInApp(List<Notification>
                                                                                notifications)
    {
        if (notifications.size() != 0) {

            String message = notifications.get(0).getMessage();

            return Observable.just(getNewFriendsOnAppCountFromMessage(message));

        }
        else {
            return Observable.just(0);
        }
    }

    private static Integer getNewFriendsOnAppCountFromMessage(String message)
    {
        try {
            if (message.contains("others are now on")) {

                String[] wordArray = message.split(" ");

                try {

                    return Integer.valueOf(wordArray[3]) + 1;
                }
                catch (Exception e) {
                    try {
                        return Integer.valueOf(wordArray[2]) + 1;
                    }
                    catch (Exception exception) {
                        return 0;
                    }
                }

            }
            else if (message.contains("is now on")) {

                return 1;

            }
            else {

                return 0;
            }
        }
        catch (Exception e) {
            return 0;
        }
    }

    private static String getNewFriendJoinedAppMessage(Integer newFriendsAlreadyOnApp,
                                                       Map<String, String> args)

    {
        String message = "";

        if (newFriendsAlreadyOnApp == 0) {
            message = NotificationHelper.getMessage(Notification
                    .NEW_FRIEND_ADDED, args);

        }
        else {

            message = String.format(NotificationMessages.NEW_FRIEND_JOINED_APP,
                    args.get
                            ("user_name") + " & " + newFriendsAlreadyOnApp + " others");

        }

        return message;
    }

    private static Set<String> getNotGoingEvents()
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> notGoingEvents = GsonProvider.getGson().fromJson(CacheManager.getGenericCache
                ().get
                (GenericCacheKeys.NOT_GOING_EVENT_LIST), type);

        return notGoingEvents;
    }

    private static Set<String> getNewFriends()
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> newFriends = GsonProvider.getGson().fromJson(CacheManager.getGenericCache
                ().get
                (GenericCacheKeys.NEW_FRIENDS_LIST), type);

        return newFriends;
    }

}
