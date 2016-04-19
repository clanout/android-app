package com.clanout.app.common.alarm;

import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.media.RingtoneManager;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;

import com.clanout.R;
import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.event.EventCache;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.cache.notification.NotificationCache;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.MemoryCacheKeys;
import com.clanout.app.model.Event;
import com.clanout.app.model.Notification;
import com.clanout.app.root.ClanOut;
import com.clanout.app.ui._core.FlowEntry;
import com.clanout.app.ui.screens.launch.LauncherActivity;

import org.joda.time.DateTime;
import org.joda.time.Days;
import org.joda.time.Minutes;

import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

/**
 * Created by harsh on 31/10/15.
 */
public class AlarmReceiver extends BroadcastReceiver
{

    @Override
    public void onReceive(Context context, Intent intent)
    {
        if (!ifAppRunningInForeground()) {
            fetchEvents(context);
        }
    }

    private void fetchEvents(final Context context)
    {
        EventCache eventCache = CacheManager.getEventCache();

        eventCache
                .getEvents()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<List<Event>>()
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
                    public void onNext(List<Event> events)
                    {

                        List<Event> filteredEvents = filterEvents(events);

                        DateTime currentTimestamp = DateTime.now();
                        List<Event> eventsToStartShortly = new ArrayList<Event>();
                        for (Event event : filteredEvents) {
                            if ((event.getStartTime().isAfter(currentTimestamp)) && (Minutes
                                    .minutesBetween(currentTimestamp, event.getStartTime())
                                    .isLessThan(Minutes.minutes(60)))) {
                                eventsToStartShortly.add(event);
                            }
                        }
                        buildNotification(eventsToStartShortly, context);

                        cleanFriendsCache();

                        cleanSuggestions();

                    }
                });
    }

    private void cleanSuggestions()
    {
        GenericCache genericCache = CacheManager.getGenericCache();
        DateTime suggestionsClearedTimestamp = genericCache.get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime.class);

        if(suggestionsClearedTimestamp == null) {
            genericCache.delete(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS);
            genericCache.put(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime.now());
        }else{

            if(Days.daysBetween(suggestionsClearedTimestamp, DateTime.now()).isGreaterThan(Days.days(7)))
            {
                genericCache.delete(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS);
                genericCache.put(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime.now());
            }
        }
    }

    private void cleanFriendsCache()
    {
        GenericCache genericCache = CacheManager.getGenericCache();
        DateTime friendCacheClearedTimeStamp = genericCache.get(GenericCacheKeys
                .FRIENDS_CACHE_CLEARED_TIMESTAMP, DateTime.class);

        if(friendCacheClearedTimeStamp == null) {
            CacheManager.clearFriendsCache();
            genericCache.put(GenericCacheKeys.FRIENDS_CACHE_CLEARED_TIMESTAMP, DateTime.now());
        }else{

            if(Days.daysBetween(friendCacheClearedTimeStamp, DateTime.now()).isGreaterThan(Days.days(15)))
            {
                CacheManager.clearFriendsCache();
                genericCache.put(GenericCacheKeys.FRIENDS_CACHE_CLEARED_TIMESTAMP, DateTime.now());
            }
        }
    }

    private List<Event> filterEvents(List<Event> events)
    {
        List<Event> filteredEvents = new ArrayList<>();
        EventCache eventCache = CacheManager.getEventCache();
        final NotificationCache notificationCache = CacheManager.getNotificationCache();

        for(Event event : events)
        {
            if(!event.getEndTime().isBefore(DateTime.now()))
            {
                filteredEvents.add(event);
            }else {

                eventCache.delete(event.getId());
                notificationCache.getAllForEvent(event.getId())
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(Schedulers.newThread())
                        .subscribe(new Subscriber<List<Notification>>()
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
                            public void onNext(List<Notification> notifications)
                            {
                                if(notifications.size() > 0)
                                {

                                    notificationCache.clear(getNotificationIdsList(notifications));
                                }
                            }
                        });
            }
        }

        return filteredEvents;
    }

    private void buildNotification(List<Event> events, Context context)
    {
        if (!events.isEmpty()) {
            for (Event event : events) {
                int requestCode = ("someString" + Math.random() + System.currentTimeMillis()).hashCode();
                String eventId = event.getId();
                Intent launcherIntent = LauncherActivity
                        .callingIntent(context, FlowEntry.DETAILS, eventId);

                PendingIntent pendingIntent = PendingIntent
                        .getActivity(context, requestCode, launcherIntent, PendingIntent
                                .FLAG_ONE_SHOT);

                Uri defaultSoundUri = RingtoneManager
                        .getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
                NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder
                        (context)
                        .setSmallIcon(R.drawable.notification_icon_small)
                        .setColor(ContextCompat.getColor(ClanOut.getClanOutContext(), R
                                .color.primary))
                        .setContentTitle(event.getTitle())
                        .setContentText(context.getResources()
                                .getString(R.string.reminder_notification_message))
                        .setAutoCancel(true)
                        .setSound(defaultSoundUri)
                        .setContentIntent(pendingIntent);

                NotificationManager notificationManager =
                        (NotificationManager) context
                                .getSystemService(Context.NOTIFICATION_SERVICE);

                notificationManager.notify(eventId.hashCode(), notificationBuilder.build());
            }
        }
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
            return false;
        }
    }

    private List<Integer> getNotificationIdsList(List<Notification> notifications)
    {
        List<Integer> notificationIds = new ArrayList<Integer>();
        for (Notification notification : notifications) {
            notificationIds.add(notification.getId());
        }

        return notificationIds;
    }
}
