package com.clanout.app.cache.notification;

import com.clanout.app.model.Notification;

import java.util.List;

import rx.Observable;

public interface NotificationCache
{
    Observable<Object> put(Notification notification);

    Observable<List<Notification>> getAll();

    Observable<Object> clear();

    Observable<Object> markRead();

    Observable<Object> clear(int notificationId);

    Observable<Boolean> isAvaliable();

    Observable<List<Notification>> getAllForType(int type);

    Observable<List<Notification>> getAllForEvent(String eventId);

    Observable<List<Notification>> getAll(int type, String eventId);

    Observable<Boolean> clear(List<Integer> notifificationIds);

    void clearAll();
}
