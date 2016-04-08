package com.clanout.app.cache.event;

import com.clanout.app.model.Event;

import org.joda.time.DateTime;

import java.util.List;

import rx.Observable;

public interface EventCache
{
    Observable<List<Event>> getEvents();

    Observable<Event> getEvent(String eventId);

    void reset(List<Event> events);

    void save(Event event);

    void deleteAll();

    void delete(String eventId);

    void updateChatSeenTimestamp(String eventId, DateTime timestamp);

    Observable<DateTime> getChatSeenTimestamp(String eventId);
}
