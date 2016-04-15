package com.clanout.app.service;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.clanout.R;
import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.event.request.FetchEventApiRequest;
import com.clanout.app.api.event.response.FetchEventApiResponse;
import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.event.EventCache;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.cache.notification.NotificationCache;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.communication.Communicator;
import com.clanout.app.communication.NewNotificationReceivedTrigger;
import com.clanout.app.communication.NewNotificationsAvailableTrigger;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.config.MemoryCacheKeys;
import com.clanout.app.model.Event;
import com.clanout.app.model.Notification;
import com.clanout.app.model.NotificationWrapper;
import com.clanout.app.model.User;
import com.clanout.app.model.util.NotificationComparator;
import com.clanout.app.root.ClanOut;
import com.clanout.app.ui._core.FlowEntry;
import com.clanout.app.ui.screens.launch.LauncherActivity;
import com.facebook.stetho.common.ArrayListAccumulator;
import com.squareup.otto.Bus;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.functions.Func5;
import rx.schedulers.Schedulers;

/**
 * Created by Aditya on 08-09-2015.
 */
public class NotificationService
{
    private static NotificationService instance;

    public static NotificationService getInstance()
    {
        if (instance == null) {
            instance = new NotificationService();
        }

        return instance;
    }

    private NotificationCache notificationCache;
    private Bus bus;
    private EventCache eventCache;
    private GenericCache genericCache;

    private NotificationService()
    {
        notificationCache = CacheManager.getNotificationCache();
        bus = Communicator.getInstance().getBus();
        eventCache = CacheManager.getEventCache();
        genericCache = CacheManager.getGenericCache();
    }

    public Observable<List<NotificationWrapper>> _fetchNotifications()
    {
        Observable<List<NotificationWrapper>> deleteNotifications =
                notificationCache
                        .getAllForType(Notification.EVENT_REMOVED)
                        .map(new Func1<List<Notification>, List<NotificationWrapper>>()
                        {
                            @Override
                            public List<NotificationWrapper> call(List<Notification> notifications)
                            {
                                List<NotificationWrapper> notificationWrappers = new ArrayList<>();
                                for (Notification notification : notifications) {
                                    NotificationWrapper.NotificationItem notificationItem =
                                            new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .EVENT_REMOVED,
                                                            notification.getMessage());

                                    NotificationWrapper notificationWrapper =
                                            new NotificationWrapper(NotificationWrapper.Type
                                                    .EVENT_REMOVED,
                                                    null,
                                                    notification.getEventName(),
                                                    Collections.singletonList(notification.getId()),
                                                    Collections.singletonList(notificationItem),
                                                    notification.getTimestamp());

                                    notificationWrappers.add(notificationWrapper);
                                }
                                return notificationWrappers;
                            }
                        });

        Observable<List<NotificationWrapper>> invitationNotifications =
                notificationCache
                        .getAllForType(Notification.EVENT_INVITATION)
                        .map(new Func1<List<Notification>, List<NotificationWrapper>>()
                        {
                            @Override
                            public List<NotificationWrapper> call(List<Notification> notifications)
                            {
                                List<NotificationWrapper> notificationWrappers = new ArrayList<>();
                                for (Notification notification : notifications) {
                                    NotificationWrapper.NotificationItem notificationItem =
                                            new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .INVITATION,
                                                            notification.getMessage());

                                    NotificationWrapper notificationWrapper =
                                            new NotificationWrapper(NotificationWrapper.Type
                                                    .EVENT_INVITATION,
                                                    notification.getEventId(),
                                                    notification.getEventName(),
                                                    Collections.singletonList(notification.getId()),
                                                    Collections.singletonList(notificationItem),
                                                    notification.getTimestamp());

                                    notificationWrappers.add(notificationWrapper);
                                }
                                return notificationWrappers;
                            }
                        });

        Observable<List<NotificationWrapper>> newFriendsNotifications =
                notificationCache
                        .getAllForType(Notification.NEW_FRIEND_ADDED)
                        .map(new Func1<List<Notification>, List<NotificationWrapper>>()
                        {
                            @Override
                            public List<NotificationWrapper> call(List<Notification> notifications)
                            {
                                List<NotificationWrapper> notificationWrappers = new ArrayList<>();
                                for (Notification notification : notifications) {
                                    NotificationWrapper.NotificationItem notificationItem =
                                            new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .NEW_FRIEND_JOINED_APP,
                                                            notification.getMessage());

                                    NotificationWrapper notificationWrapper =
                                            new NotificationWrapper(NotificationWrapper.Type
                                                    .NEW_FRIEND_JOINED_APP,
                                                    null,
                                                    notification.getEventName(),
                                                    Collections.singletonList(notification.getId()),
                                                    Collections.singletonList(notificationItem),
                                                    notification.getTimestamp());

                                    notificationWrappers.add(notificationWrapper);
                                }
                                return notificationWrappers;
                            }
                        });

        Observable<List<NotificationWrapper>> activityNotifications =
                notificationCache
                        .getAll()
                        .map(new Func1<List<Notification>, List<NotificationWrapper>>()
                        {
                            @Override
                            public List<NotificationWrapper> call(List<Notification> notifications)
                            {
                                Map<String, NotificationWrapper> notifs = new HashMap<>();

                                for (Notification notification : notifications) {
                                    int type = notification.getType();
                                    if (type == Notification.EVENT_UPDATED || type ==
                                            Notification.CHAT || type == Notification.RSVP) {
                                        NotificationWrapper.NotificationItem item = null;
                                        if (type == Notification.EVENT_UPDATED) {
                                            item = new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .EVENT_UPDATED, notification
                                                            .getMessage());
                                        }
                                        else if (type == Notification.CHAT) {
                                            item = new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .NEW_CHAT, notification
                                                            .getMessage());
                                        }
                                        else {
                                            item = new NotificationWrapper.NotificationItem
                                                    (NotificationWrapper.NotificationItem.Type
                                                            .FRIEND_JOINED_EVENT, notification
                                                            .getMessage());
                                        }

                                        String eventId = notification.getEventId();
                                        NotificationWrapper notif = notifs.get(eventId);
                                        if (notif == null) {
                                            notif = new NotificationWrapper(
                                                    NotificationWrapper.Type.EVENT_ACTIVITY,
                                                    notification.getEventId(),
                                                    notification.getEventName(),
                                                    new ArrayList<>(Collections
                                                            .singletonList(notification.getId())),
                                                    new ArrayList<>(Collections
                                                            .singletonList(item)),
                                                    notification.getTimestamp()
                                            );
                                        }
                                        else {
                                            notif.getNotificationIds().add(notification.getId());
                                            notif.getNotificationItems().add(item);
                                            if (notif.getTimestamp()
                                                    .isBefore(notification.getTimestamp())) {
                                                notif.setTimestamp(notification.getTimestamp());
                                            }
                                        }

                                        notifs.put(eventId, notif);
                                    }
                                }

                                return new ArrayList<>(notifs.values());
                            }
                        });

        Observable<List<Event>> getAllEventsObservable = eventCache.getEvents()
                .map(new Func1<List<Event>, List<Event>>()
                {
                    List<Event> filteredEvents = new ArrayList<Event>();

                    @Override
                    public List<Event> call(List<Event> events)
                    {
                        for (Event event : events) {
                            if (!event.isExpired()) {
                                filteredEvents.add(event);
                            }
                        }

                        return filteredEvents;
                    }
                });

        return Observable
                .zip(deleteNotifications,
                        invitationNotifications,
                        newFriendsNotifications,
                        activityNotifications,
                        getAllEventsObservable,
                        new Func5<List<NotificationWrapper>, List<NotificationWrapper>,
                                List<NotificationWrapper>,
                                List<NotificationWrapper>, List<Event>, List<NotificationWrapper>>()
                        {
                            @Override
                            public List<NotificationWrapper> call(List<NotificationWrapper>
                                                                          deleteNotifications,
                                                                  List<NotificationWrapper>
                                                                          invitationNotifications,
                                                                  List<NotificationWrapper>
                                                                          newFriendsNotifications,
                                                                  List<NotificationWrapper>
                                                                          activityNotifications,
                                                                  List<Event> events)
                            {
                                Set<NotificationWrapper> notificationSet = new HashSet<>();
                                notificationSet.addAll(deleteNotifications);
                                notificationSet.addAll(invitationNotifications);
                                notificationSet.addAll(newFriendsNotifications);
                                notificationSet.addAll(activityNotifications);

                                List<NotificationWrapper> notifications = new ArrayList<>
                                        (notificationSet);

                                List<NotificationWrapper> filteredNotifications = new
                                        ArrayListAccumulator<NotificationWrapper>();

                                for (NotificationWrapper notificationWrapper : notifications) {
                                    if (!isNotificationExpired(notificationWrapper, events)) {
                                        filteredNotifications.add(notificationWrapper);
                                    }
                                }

                                Collections.sort(filteredNotifications, new
                                        NotificationComparator());
                                return filteredNotifications;
                            }
                        })
                .subscribeOn(Schedulers.newThread());
    }

    private boolean isNotificationExpired(NotificationWrapper notificationWrapper, List<Event>
            events)

    {
        Event event;
        boolean isExpired = false;

        if (notificationWrapper.getEventId() == null) {
            return false;
        }
        else {

            event = new Event();
            event.setId(notificationWrapper.getEventId());

            if (events.contains(event)) {
                return false;
            }
            else {

                return true;
            }
        }
    }

    public void handleNotification(Notification notification)
    {
        int notificationType = notification.getType();

        switch (notificationType) {
            case Notification.EVENT_CREATED:
                showCreateEventNotification(notification);
                break;
            case Notification.EVENT_INVITATION:
                showEventInvitedNotification(notification);
                break;
            case Notification.RSVP:
                showRSVPChangedNotification(notification);
                break;
            case Notification.EVENT_REMOVED:
                showEventRemovedNotification(notification);
                break;
            case Notification.EVENT_UPDATED:
                showEventUpdatedNotification(notification);
                break;
            case Notification.BLOCKED:
                handleBlockedNotification(notification);
                break;
            case Notification.UNBLOCKED:
                handleUnblockedNotification(notification);
                break;
            case Notification.FRIEND_RELOCATED:
                handleFriendNotification(notification);
                break;
            case Notification.NEW_FRIEND_ADDED:
                handleNewFriendJoinedAppNotification(notification);
                break;
            case Notification.CHAT:
                handleNewChatMessageNotification(notification);
                break;
            case Notification.STATUS:
                handleNewStatusUpdateNotification(notification);
                break;
            case Notification.PLAN_REMOVE_FROM_FEED:
                handlePlanRemoveFromFeedNotification(notification);
                break;
        }
    }

    private void handlePlanRemoveFromFeedNotification(Notification notification)
    {
        eventCache.delete(notification.getEventId());
    }

    private void handleNewStatusUpdateNotification(final Notification notification)
    {

        if (!(notification.getArgs().get("user_id").equals(genericCache.get(GenericCacheKeys
                .SESSION_USER, User.class).getId()))) {

            if (!ifAppRunningInForeground()) {
                int requestCode = ("someString" + Math.random() + System.currentTimeMillis())
                        .hashCode();
                String eventId = notification.getEventId();
                Intent launcherIntent = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(ClanOut
                                .getClanOutContext(), requestCode, launcherIntent, PendingIntent
                                .FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder
                        (ClanOut.getClanOutContext())
                        .setSmallIcon(R.drawable.notification_icon_small)
                        .setColor(ContextCompat.getColor(ClanOut.getClanOutContext(), R
                                .color.primary))
                        .setContentText(ClanOut.getClanOutContext().getResources()
                                .getString(R.string.reminder_notification_message))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) ClanOut.getClanOutContext()
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(eventId.hashCode(), notificationBuilder.build());

            }
        }
    }

    private void handleNewChatMessageNotification(final Notification notification)
    {

        if (!(notification.getArgs().get("user_id").equals(genericCache.get(GenericCacheKeys
                .SESSION_USER, User.class).getId()))) {

            final DateTime notificationTimestamp = DateTime.parse(notification.getArgs().get
                    ("timestamp"));

            eventCache.getChatSeenTimestamp(notification.getEventId())
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(new Subscriber<DateTime>()
                    {
                        @Override
                        public void onCompleted()
                        {

                        }

                        @Override
                        public void onError(Throwable e)
                        {
                        }

                        @Override
                        public void onNext(DateTime lastSeenTimestamp)
                        {
                            if (lastSeenTimestamp == null) {
                                notificationCache.put(notification).observeOn(Schedulers
                                        .newThread())
                                        .subscribe(new Subscriber<Object>()
                                        {
                                            @Override
                                            public void onCompleted()
                                            {

                                                if (ifAppRunningInForeground()) {
                                                    bus.post(new NewNotificationReceivedTrigger());

                                                }
                                                else {
                                                    buildNotification(notification);
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable e)
                                            {

                                            }

                                            @Override
                                            public void onNext(Object o)
                                            {

                                            }
                                        });
                            }
                            else if (notificationTimestamp.isAfter(lastSeenTimestamp)) {

                                notificationCache.put(notification).observeOn(Schedulers
                                        .newThread())
                                        .subscribe(new Subscriber<Object>()
                                        {
                                            @Override
                                            public void onCompleted()
                                            {

                                                if (ifAppRunningInForeground()) {

                                                    bus.post(new NewNotificationReceivedTrigger());

                                                }
                                                else {
                                                    buildNotification(notification);
                                                }
                                            }

                                            @Override
                                            public void onError(Throwable e)
                                            {

                                            }

                                            @Override
                                            public void onNext(Object o)
                                            {

                                            }
                                        });

                            }
                            else {
                            }
                        }
                    });
        }
    }

    private void handleNewFriendJoinedAppNotification(final Notification notification)
    {
        notificationCache.put(notification).observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {
                        if (ifAppRunningInForeground()) {
                            bus.post(new NewNotificationReceivedTrigger());

                        }
                        else {
                            buildNotification(notification);
                        }
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    private void handleFriendNotification(Notification notification)
    {
        CacheManager.clearFriendsCache();
    }

    private void handleUnblockedNotification(Notification notification)
    {
        CacheManager.clearFriendsCache();
    }

    private void handleBlockedNotification(Notification notification)
    {
        CacheManager.clearFriendsCache();
    }

    private void showEventUpdatedNotification(final Notification notification)
    {
        fetchEvent(notification.getEventId()).observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        if (!(notification.getArgs().get("user_id")
                                .equals(genericCache
                                        .get(GenericCacheKeys.SESSION_USER, User
                                                .class)
                                        .getId()))) {

                            notificationCache.put(notification)
                                    .observeOn(Schedulers
                                            .newThread())
                                    .subscribe(new Subscriber<Object>()
                                    {
                                        @Override
                                        public void onCompleted()
                                        {
                                            if (ifAppRunningInForeground()) {
                                                bus.post(new
                                                        NewNotificationReceivedTrigger());

                                            }
                                            else {
                                                buildNotification(notification);
                                            }
                                        }

                                        @Override
                                        public void onError
                                                (Throwable e)
                                        {
                                        }

                                        @Override
                                        public void onNext(Object o)
                                        {
                                        }
                                    });
                        }
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                    }
                });
    }

    private void showEventRemovedNotification(final Notification notification)
    {
        eventCache.delete(notification.getEventId());

        if (!(notification.getArgs().get("user_id").equals(genericCache.get(GenericCacheKeys
                .SESSION_USER, User.class).getId()))) {
            notificationCache.put(notification).observeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<Object>()
                    {
                        @Override
                        public void onCompleted()
                        {
                            if (ifAppRunningInForeground()) {
                                bus.post(new NewNotificationReceivedTrigger());

                            }
                            else {
                                buildNotification(notification);
                            }
                        }

                        @Override
                        public void onError(Throwable e)
                        {

                        }

                        @Override
                        public void onNext(Object o)
                        {

                        }
                    });
        }
    }

    private void showRSVPChangedNotification(final Notification notification)
    {

        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        notification
                                .setMessage("New friends joined " +
                                        notification
                                                .getArgs().get
                                                ("plan_title"));
                        notificationCache.put(notification)
                                .observeOn(Schedulers
                                        .newThread())
                                .subscribe(new Subscriber<Object>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new
                                                    NewNotificationReceivedTrigger());

                                        }
                                        else {
                                            buildNotification(notification);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e)
                                    {

                                    }

                                    @Override
                                    public void onNext(Object o)
                                    {

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                    }
                });

    }

    private void showEventInvitedNotification(final Notification notification)
    {
        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        notificationCache.put(notification)
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new NewNotificationReceivedTrigger());

                                        }
                                        else {
                                            buildNotification(notification);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e)
                                    {

                                    }

                                    @Override
                                    public void onNext(Object o)
                                    {

                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                    }
                });
    }

    private void showCreateEventNotification(final Notification notification)
    {

        fetchEvent(notification.getEventId())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                        notificationCache.put(notification)
                                .observeOn(Schedulers.newThread())
                                .subscribe(new Subscriber<Object>()
                                {
                                    @Override
                                    public void onCompleted()
                                    {
                                        if (ifAppRunningInForeground()) {
                                            bus.post(new NewNotificationReceivedTrigger());
                                        }
                                        else {
                                            buildNotification(notification);
                                        }
                                    }

                                    @Override
                                    public void onError(Throwable e)
                                    {
                                    }

                                    @Override
                                    public void onNext(Object o)
                                    {
                                    }
                                });
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        eventCache.save(event);
                    }
                });
    }

    private boolean ifAppRunningInForeground()
    {
        try {
            Boolean isAppInForeground = CacheManager.getMemoryCache()
                    .get(MemoryCacheKeys.IS_APP_IN_FOREGROUND, Boolean.class);
            if (isAppInForeground != null) {
                return isAppInForeground;
            }
            else {
                return false;
            }
        }
        catch (Exception e) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_S, false);
            /* Analytics */

            return false;
        }
    }

    private Observable<Event> fetchEvent(String eventId)
    {
        return ApiManager.getEventApi().fetchEvent(new FetchEventApiRequest(eventId))
                .map(new Func1<FetchEventApiResponse, Event>()
                {
                    @Override
                    public Event call(FetchEventApiResponse fetchEventApiResponse)
                    {
                        return fetchEventApiResponse.getEvent();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void deleteAllNotificationsFromCache()
    {
        notificationCache.clear().observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void markAllNotificationsAsRead()
    {
        notificationCache.markRead().observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void deleteNotificationFromCache(int notificationId)
    {
        notificationCache.clear(notificationId).observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void deleteNotificationFromCache(List<Integer> notificationIds)
    {
        notificationCache
                .clear(notificationIds)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Object o)
                    {

                    }
                });
    }

    public void areNewNotificationsAvailable()
    {
        notificationCache.isAvaliable().observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Boolean isAvailable)
                    {
                        if (isAvailable) {
                            bus.post(new NewNotificationsAvailableTrigger());
                        }
                    }
                });
    }

    private void buildNotification(final Notification notification)
    {

        final Intent[] intent = new Intent[1];

        final int requestCode = ("someString" + System.currentTimeMillis()).hashCode();

        notificationCache
                .getAll()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<List<Notification>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                        Log.d("APP", "onError build noti --- notificationCache.getAll()");
                    }

                    @Override
                    public void onNext(List<Notification> notifications)
                    {
                        if (notifications.size() == 0) {

                        }
                        else if (notifications.size() == 1) {
                            // if only one notification

                            handleSingleNotificationIntent(notification, intent);

                        }
                        else if (notifications.size() > 1) {
                            intent[0] = LauncherActivity
                                    .callingIntent(ClanOut.getClanOutContext(), FlowEntry
                                            .NOTIFICATIONS, null);
                        }

                        PendingIntent pendingIntent = PendingIntent
                                .getActivity(ClanOut
                                                .getClanOutContext(), requestCode, intent[0],
                                        PendingIntent.FLAG_ONE_SHOT);

                        Uri defaultSoundUri = RingtoneManager
                                .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                        NotificationCompat.Builder notificationBuilder = new NotificationCompat
                                .Builder(ClanOut
                                .getClanOutContext())
                                .setSmallIcon(R.drawable.notification_icon_small)
                                .setColor(ContextCompat.getColor(ClanOut.getClanOutContext(), R
                                        .color.primary))
                                .setAutoCancel(true)
                                .setSound(defaultSoundUri)
                                .setContentIntent(pendingIntent);

                        // Set Title and message for merged view
                        if (notifications.size() == 1) {
                            notificationBuilder.setContentTitle(notification.getTitle());
                            notificationBuilder.setContentText(notification.getMessage());

                        }
                        else if (notifications.size() > 1) {

                            notificationBuilder.setContentTitle("Clanout");

                            notificationBuilder.setContentText(buildCompressedMessage
                                    (notifications));

                            // Set Title and message for expanded view
                            StringBuilder bigTextMessage = new StringBuilder();

                            for (Notification noti : notifications) {
                                if (noti.getType() == Notification.CHAT || noti
                                        .getType() == Notification.EVENT_INVITATION || noti
                                        .getType() == Notification.EVENT_UPDATED || noti
                                        .getType() == Notification.EVENT_REMOVED) {
                                    bigTextMessage
                                            .append(noti.getMessage().substring(0, Math.min(noti
                                                    .getMessage().length(), 40)));
                                    bigTextMessage.append("\n");
                                }
                            }

                            if (bigTextMessage.toString().isEmpty()) {
                                for (Notification noti : notifications) {

                                    bigTextMessage
                                            .append(noti.getMessage().substring(0, Math.min(noti
                                                    .getMessage().length(), 40)));
                                    bigTextMessage.append("\n");

                                }
                            }


                            notificationBuilder
                                    .setStyle(new NotificationCompat.BigTextStyle()
                                            .bigText(bigTextMessage.toString()));

                        }

                        NotificationManager notificationManager =
                                (NotificationManager) ClanOut.getClanOutContext()
                                        .getSystemService(Context.NOTIFICATION_SERVICE);

                        notificationManager.notify(1, notificationBuilder.build());
                    }
                });
    }

    private String buildCompressedMessage(List<Notification> notifications)
    {
        int chatCount = 0;
        int updateCount = 0;
        int invitationCount = 0;

        for (Notification noti : notifications) {

            if (noti.getType() == Notification.CHAT) {
                chatCount++;
            }
            else if (noti.getType() == Notification.EVENT_INVITATION) {
                invitationCount++;
            }
            else if (noti
                    .getType() == Notification.EVENT_UPDATED || noti
                    .getType() == Notification.EVENT_REMOVED) {
                updateCount++;
            }
        }

        String message = "";
        if (invitationCount != 0) {
            if (invitationCount == 1) {
                message = message + invitationCount + " invitation";
            }
            else {
                message = message + invitationCount + " invitations";
            }

            int sumOfChatsAndUpdates = chatCount + updateCount;
            if (sumOfChatsAndUpdates != 0) {
                message = message + " and " + sumOfChatsAndUpdates + " other notifications.";
            }
            else {

                message = message + ".";
            }
        }
        else if (updateCount != 0) {

            if (updateCount == 1) {
                message = message + updateCount + " update";
            }
            else {
                message = message + updateCount + " updates";
            }


            if (chatCount != 0) {
                message = message + " and " + chatCount + " other notifications.";
            }
            else {
                message = message + ".";
            }
        }
        else if (chatCount != 0) {
            if (chatCount == 1) {
                message = message + chatCount + " chat";
            }
            else {
                message = message + chatCount + " chats";
            }

        }

        if (chatCount == 0 && updateCount == 0 && invitationCount == 0) {

            if (notifications.size() == 1) {
                message = notifications
                        .size() + " new notification";

            }
            else {

                message = notifications
                        .size() + " new notifications";
            }
        }

        return message;
    }

    private void handleSingleNotificationIntent(Notification notification, Intent[] intent)
    {

        String eventId = notification.getEventId();

        switch (notification.getType()) {
            case Notification.EVENT_INVITATION:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);
                break;

            case Notification.STATUS:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);
                break;

            case Notification.CHAT:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.CHAT, eventId);
                break;

            case Notification.EVENT_CREATED:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);
                break;

            case Notification.EVENT_REMOVED:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.HOME, null);
                break;

            case Notification.EVENT_UPDATED:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);
                break;

            case Notification.NEW_FRIEND_ADDED:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.HOME, null);
                break;

            case Notification.RSVP:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.DETAILS, eventId);
                break;

            default:
                intent[0] = LauncherActivity
                        .callingIntent(ClanOut.getClanOutContext(), FlowEntry.HOME, null);
                break;
        }
    }

    public void deletePlanCreateNotification(String eventId)
    {
        notificationCache.getAll(Notification.EVENT_CREATED, eventId)
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();

                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Boolean aBoolean)
                    {

                    }
                });
    }

    public void deleteInvitationNotification(String eventId)
    {
        notificationCache.getAll(Notification.EVENT_INVITATION, eventId)
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {
                        List<Integer> notificationIds = new ArrayList<Integer>();

                        for (Notification notification : notifications) {
                            notificationIds.add(notification.getId());
                        }

                        return notificationCache.clear(notificationIds);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {

                    }

                    @Override
                    public void onNext(Boolean aBoolean)
                    {

                    }
                });
    }

    public Observable<List<Notification>> getNotifications(int type, String eventId)
    {
        return notificationCache.getAll(type, eventId)
                .subscribeOn(Schedulers.newThread());
    }

    public void clearNotificationsFromBar()
    {
        NotificationManager notificationManager =
                (NotificationManager) ClanOut.getClanOutContext()
                        .getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancelAll();
    }
}
