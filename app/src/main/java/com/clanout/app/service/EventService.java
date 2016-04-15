package com.clanout.app.service;

import android.util.Log;
import android.util.Pair;

import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.api.event.request.CreateEventApiRequest;
import com.clanout.app.api.event.request.DeleteEventApiRequest;
import com.clanout.app.api.event.request.EditEventApiRequest;
import com.clanout.app.api.event.request.EventsApiRequest;
import com.clanout.app.api.event.request.FetchEventApiRequest;
import com.clanout.app.api.event.request.FetchPendingInvitesApiRequest;
import com.clanout.app.api.event.request.GetCreateEventSuggestionsApiRequest;
import com.clanout.app.api.event.request.InviteUsersApiRequest;
import com.clanout.app.api.event.request.RsvpUpdateApiRequest;
import com.clanout.app.api.event.request.SendChatNotificationApiRequest;
import com.clanout.app.api.event.request.SendInvitaionResponseApiRequest;
import com.clanout.app.api.event.request.UpdateStatusApiRequest;
import com.clanout.app.api.event.response.CreateEventApiResponse;
import com.clanout.app.api.event.response.EventsApiResponse;
import com.clanout.app.api.event.response.FetchEventApiResponse;
import com.clanout.app.api.event.response.FetchPendingInvitesApiResponse;
import com.clanout.app.api.event.response.GetCreateEventSuggestionsApiResponse;
import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.event.EventCache;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.CreateEventSuggestion;
import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Location;
import com.clanout.app.model.User;
import com.clanout.app.model.util.EventComparator;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import retrofit.RetrofitError;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventService
{
    private static EventService instance;

    public static void init(GcmService gcmService, LocationService locationService,
                            UserService userService, NotificationService notificationService)
    {
        instance = new EventService(gcmService, locationService, userService, notificationService);
    }

    public static EventService getInstance()
    {
        if (instance == null) {
            throw new IllegalStateException("[EventService Not Initialized]");
        }

        return instance;
    }

    private LocationService locationService;
    private GcmService gcmService;
    private UserService userService;
    private NotificationService notificationService;
    private EventCache eventCache;

    private GenericCache genericCache;

    private EventService(GcmService gcmService, LocationService locationService,
                         UserService userService, NotificationService notificationService)
    {
        eventCache = CacheManager.getEventCache();
        genericCache = CacheManager.getGenericCache();

        this.gcmService = gcmService;
        this.locationService = locationService;
        this.userService = userService;
        this.notificationService = notificationService;
    }

    /* Events */
    public Observable<Event> _fetchEvent(final String eventId)
    {
        return _fetchEventCache(eventId)
                .flatMap(new Func1<Event, Observable<Event>>()
                {
                    @Override
                    public Observable<Event> call(Event event)
                    {
                        if (event != null) {
                            return Observable.just(event);
                        }
                        else {
                            return _fetchEventNetwork(eventId);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _fetchEventCache(final String eventId)
    {
        return eventCache
                .getEvent(eventId)
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Event> _fetchEventNetwork(final String eventId)
    {
        return ApiManager.getEventApi().fetchEvent(new FetchEventApiRequest(eventId))
                .map(new Func1<FetchEventApiResponse, Event>()
                {
                    @Override
                    public Event call(FetchEventApiResponse response)
                    {
                        return response.getEvent();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        eventCache.save(event);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEvents()
    {
        return _fetchEventsCache()
                .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                {
                    @Override
                    public Observable<List<Event>> call(List<Event> events)
                    {
                        if (events.isEmpty()) {
                            return _fetchEventsNetwork();
                        }
                        else {
                            return Observable.just(events);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEventsCache()
    {
        return eventCache
                .getEvents()
                .map(new Func1<List<Event>, List<Event>>()
                {
                    @Override
                    public List<Event> call(List<Event> events)
                    {
                        List<Event> filtered = filterExpiredEvents(events);
                        Collections.sort(filtered, new EventComparator.Relevance(userService
                                .getSessionUserId()));
                        return filtered;
                    }
                })
                .doOnNext(new Action1<List<Event>>()
                {
                    @Override
                    public void call(List<Event> events)
                    {
                        eventCache.reset(events);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Event>> _fetchEventsNetwork()
    {
        EventsApiRequest request = new EventsApiRequest(null);
        return ApiManager.getEventApi()
                .getEvents(request)
                .map(new Func1<EventsApiResponse, List<Event>>()
                {
                    @Override
                    public List<Event> call(EventsApiResponse response)
                    {
                        List<Event> filtered = filterExpiredEvents(response.getEvents());
                        Collections.sort(filtered, new EventComparator.Relevance(userService
                                .getSessionUserId()));
                        return filtered;
                    }
                })
                .doOnNext(new Action1<List<Event>>()
                {
                    @Override
                    public void call(List<Event> events)
                    {
                        eventCache.reset(events);

                        for (Event event : events) {
                            handleTopicSubscription(event);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());

    }

    /* Invite */
    public void _inviteFriends(String eventId, List<String> friendIds, List<String> mobileNumbers)
    {
        InviteUsersApiRequest request = new InviteUsersApiRequest(eventId, friendIds,
                mobileNumbers);

        ApiManager.getEventApi().inviteFriends(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z9,
                                true);
                        /* Analytics */
                    }

                    @Override
                    public void onNext(Response response)
                    {
                    }
                });
    }


    /* Edit */

    public Observable<Boolean> _editEvent(final Event originalEvent, final DateTime startTime,
                                          final DateTime
                                                  endTime, Location placeLocation, final String
                                                  description)
    {
        if (placeLocation == null) {
            placeLocation = new Location();
        }
        final Location finalPlaceLocation = placeLocation;

        final EditEventApiRequest request = new EditEventApiRequest(placeLocation.getLongitude(),
                description, endTime, originalEvent.getId(), placeLocation.getLatitude(),
                placeLocation.getName
                        (), startTime);

        return ApiManager.getEventApi().editEvent(request)
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        /* Analytics */
                        AnalyticsHelper
                                .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Q, false);
                                   /* Analytics */
                        return false;
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSuccess)
                    {
                        if (isSuccess) {

                            if (startTime != null) {
                                originalEvent.setStartTime(startTime);
                                originalEvent.setEndTime(endTime);
                            }

                            if (finalPlaceLocation.getName() != null) {
                                originalEvent.setLocation(finalPlaceLocation);
                            }

                            if (description != null) {
                                originalEvent.setDescription(description);
                            }

                            eventCache.save(originalEvent);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Boolean> _deleteEvent(final String eventId)
    {
        DeleteEventApiRequest request = new DeleteEventApiRequest(eventId);
        return ApiManager.getEventApi()
                .deleteEvent(request)
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSuccessful)
                    {
                        if (isSuccessful) {
                            eventCache.delete(eventId);

                            if (genericCache.get(GenericCacheKeys.GCM_TOKEN) != null) {
                                gcmService.unsubscribeTopic(genericCache
                                        .get(GenericCacheKeys.GCM_TOKEN), eventId);
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Chat Notification */
    public Observable<Boolean> _sendChatNotification(String eventId, ChatMessage chatMessage)
    {
        return ApiManager.getEventApi()
                .sendChatNotification(new SendChatNotificationApiRequest(eventId, chatMessage))
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void updateChatLastSeen(String eventId, DateTime timestamp)
    {
        eventCache.updateChatSeenTimestamp(eventId, timestamp);
    }

    /* Create Suggestions */
    public Observable<Boolean> _fetchCreateSuggestions()
    {
        return Observable
                .create(new Observable.OnSubscribe<Boolean>()
                {
                    @Override
                    public void call(Subscriber<? super Boolean> subscriber)
                    {
                        boolean isSuggestionsAvailable = genericCache
                                .get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS) != null;
                        boolean isExpired = true;

                        try {
                            DateTime lastUpdated = genericCache
                                    .get(GenericCacheKeys
                                            .CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime
                                            .class);
                            isExpired = lastUpdated
                                    .plusDays(AppConstants.EXPIRY_DAYS_EVENT_SUGGESTIONS)
                                    .isBefore(DateTime.now());
                        }
                        catch (Exception e) {

                        }

                        if (isSuggestionsAvailable && !isExpired) {
                            subscriber.onNext(true);
                        }
                        else {
                            subscriber.onNext(false);
                        }
                        subscriber.onCompleted();
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isAvailable)
                    {
                        if (isAvailable) {
                            return Observable.just(true);
                        }
                        else {
                            return ApiManager.getEventApi()
                                    .getCreateEventSuggestion(new
                                            GetCreateEventSuggestionsApiRequest())
                                    .onErrorReturn(new Func1<Throwable,
                                            GetCreateEventSuggestionsApiResponse>()
                                    {
                                        @Override
                                        public GetCreateEventSuggestionsApiResponse call
                                                (Throwable throwable)
                                        {
                                            return null;
                                        }
                                    })
                                    .map(new Func1<GetCreateEventSuggestionsApiResponse, Boolean>()
                                    {
                                        @Override
                                        public Boolean call(GetCreateEventSuggestionsApiResponse
                                                                    response)
                                        {
                                            if (response == null) {
                                                return false;
                                            }
                                            else {
                                                genericCache
                                                        .put(GenericCacheKeys
                                                                .CREATE_EVENT_SUGGESTIONS, response
                                                                .getEventSuggestions());
                                                genericCache
                                                        .put(GenericCacheKeys
                                                                .CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP, DateTime
                                                                .now());
                                                return true;
                                            }
                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<CreateEventSuggestion>> _getCreateSuggestions()
    {
        return Observable
                .create(new Observable.OnSubscribe<List<CreateEventSuggestion>>()
                {
                    @Override
                    public void call(Subscriber<? super List<CreateEventSuggestion>> subscriber)
                    {
                        String createEventSuggestionStr = genericCache
                                .get(GenericCacheKeys.CREATE_EVENT_SUGGESTIONS);
                        Type type = new TypeToken<List<CreateEventSuggestion>>()
                        {
                        }.getType();

                        List<CreateEventSuggestion> createEventSuggestions = GsonProvider.getGson()
                                .fromJson(createEventSuggestionStr, type);
                        subscriber.onNext(createEventSuggestions);
                        subscriber.onCompleted();
                    }
                })
                .map(new Func1<List<CreateEventSuggestion>, List<CreateEventSuggestion>>()
                {
                    @Override
                    public List<CreateEventSuggestion> call(List<CreateEventSuggestion> suggestions)
                    {
                        Collections.shuffle(suggestions);
                        return suggestions;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Rsvp */
    public Observable<Boolean> _updateRsvp(final Event updatedEvent)
    {
        RsvpUpdateApiRequest request = new RsvpUpdateApiRequest(updatedEvent.getId(), updatedEvent
                .getRsvp());

        return ApiManager.getEventApi().updateRsvp(request)
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return (response.getStatus() == 200);
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSuccess)
                    {
                        Log.d("APP", "updateRsvp ----- " + isSuccess);
                        if (isSuccess) {
                            eventCache.delete(updatedEvent.getId());
                            eventCache.save(updatedEvent);
                            handleTopicSubscription(updatedEvent);

                            if (updatedEvent.getRsvp() == Event.RSVP.YES) {
                                notificationService
                                        .deleteInvitationNotification(updatedEvent.getId());
                            }
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Status */
    public void updateStatus(final Event updatedEvent, boolean shouldNotifyOthers)
    {

        UpdateStatusApiRequest request = new UpdateStatusApiRequest(updatedEvent.getId(),
                updatedEvent.getStatus(),
                shouldNotifyOthers);

        ApiManager.getEventApi().updateStatus(request).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Subscriber<Response>()
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
            public void onNext(Response response)
            {
                eventCache.delete(updatedEvent.getId());
                eventCache.save(updatedEvent);
            }
        });
    }

    /* Create */
    public Observable<Event> _create(String title, Event.Type eventType, EventCategory
            eventCategory,
                                     String description, Location placeLocation, DateTime
                                             startTime, DateTime endTime)
    {

        String zone = locationService.getCurrentLocation().getZone();

        if (placeLocation == null) {
            placeLocation = new Location();
        }

        CreateEventApiRequest request = new CreateEventApiRequest(title, eventType,
                eventCategory, description, placeLocation
                .getName(), zone, placeLocation.getLatitude(), placeLocation
                .getLongitude(), startTime, endTime);

        return ApiManager.getEventApi()
                .createEvent(request)
                .map(new Func1<CreateEventApiResponse, Event>()
                {
                    @Override
                    public Event call(CreateEventApiResponse createEventApiResponse)
                    {
                        return createEventApiResponse.getEvent();
                    }
                })
                .doOnNext(new Action1<Event>()
                {
                    @Override
                    public void call(Event event)
                    {
                        eventCache.save(event);
                        handleTopicSubscription(event);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Pending Invites */
    public Observable<Pair<Integer, List<Event>>> _fetchPendingInvites(final String mobileNumber)
    {
        FetchPendingInvitesApiRequest request = new FetchPendingInvitesApiRequest(mobileNumber);
        return ApiManager.getEventApi()
                .fetchPendingInvites(request)
                .doOnNext(new Action1<FetchPendingInvitesApiResponse>()
                {
                    @Override
                    public void call(FetchPendingInvitesApiResponse response)
                    {
                        User sessionUser = userService.getSessionUser();
                        sessionUser.setMobileNumber(mobileNumber);
                        userService.setSessionUser(sessionUser);
                    }
                })
                .flatMap(new Func1<FetchPendingInvitesApiResponse, Observable<Pair<Integer,
                        List<Event>>>>()
                {
                    @Override
                    public Observable<Pair<Integer, List<Event>>> call(final
                                                                       FetchPendingInvitesApiResponse response)
                    {
                        return _fetchEventsCache()
                                .flatMap(new Func1<List<Event>, Observable<Pair<Integer,
                                        List<Event>>>>()
                                {
                                    @Override
                                    public Observable<Pair<Integer, List<Event>>> call
                                            (List<Event> cachedEvents)
                                    {
                                        List<Event> pendingInvites = response.getActiveEvents();
                                        int expiredInvites = response.getExpiredCount();

                                        for (Event pendingInvite : pendingInvites) {
                                            if (!cachedEvents.contains(pendingInvite)) {
                                                cachedEvents.add(pendingInvite);
                                            }
                                        }

                                        eventCache.reset(cachedEvents);

                                        return Observable
                                                .just(new Pair<>(expiredInvites, pendingInvites));
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Invitation Response */
    public void sendInvitationResponse(String eventId, String message)
    {
        SendInvitaionResponseApiRequest request = new SendInvitaionResponseApiRequest(eventId,
                message);
        ApiManager.getEventApi()
                .sendInvitationResponse(request)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {
                    }
                });
    }

    /* Helper Methods */
    private boolean isExpired(Event event)
    {
        return event.getEndTime().isBeforeNow();
    }

    private List<Event> filterExpiredEvents(List<Event> events)
    {
        List<Event> filteredEvents = new ArrayList<Event>();
        for (Event event : events) {
            if (!isExpired(event)) {
                filteredEvents.add(event);
            }
        }

        return filteredEvents;
    }

    private void handleTopicSubscription(Event event)
    {
        if (event.getRsvp() == Event.RSVP.YES) {
            gcmService.subscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), event
                    .getId());
        }
        else {
            gcmService
                    .unsubscribeTopic(genericCache.get(GenericCacheKeys.GCM_TOKEN), event
                            .getId());
        }
    }

    public Observable<Boolean> markEventAsSeen(String eventId)
    {
        RsvpUpdateApiRequest rsvpUpdateApiRequest = new RsvpUpdateApiRequest(eventId, Event.RSVP
                .SEEN);

        return ApiManager.getEventApi().updateRsvp(rsvpUpdateApiRequest)
               .map(new Func1<Response, Boolean>()
               {
                   @Override
                   public Boolean call(Response response)
                   {
                       if(response.getStatus() == 200)
                       {
                           return true;
                       }else{

                           return false;
                       }
                   }
               })
                .subscribeOn(Schedulers.newThread());

    }
}
