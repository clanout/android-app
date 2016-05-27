package com.clanout.app.ui.screens.details.mvp;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.model.Attendee;
import com.clanout.app.model.AttendeeWrapper;
import com.clanout.app.model.Event;
import com.clanout.app.model.Notification;
import com.clanout.app.model.util.DateTimeUtil;
import com.clanout.app.model.util.EventAttendeeComparator;
import com.clanout.app.service.EventService;
import com.clanout.app.service.NotificationService;
import com.clanout.app.service.UserService;
import com.google.gson.reflect.TypeToken;

import org.joda.time.DateTime;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventDetailsPresenterImpl implements EventDetailsPresenter
{
    /* View */
    private EventDetailsView view;

    /* Service */
    private EventService eventService;
    private UserService userService;
    private NotificationService notificationService;
    private GenericCache genericCache;

    /* Data */
    private Event event;
    private boolean isNetworkUpdated;
    private boolean isLastMinute;

    private boolean isRsvpUpdateInProgress;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventDetailsPresenterImpl(EventService eventService, UserService userService,
                                     NotificationService notificationService, GenericCache
                                             genericCache, Event event)
    {
        this.eventService = eventService;
        this.userService = userService;
        this.notificationService = notificationService;
        this.genericCache = genericCache;

        this.event = event;
        processIsLastMinute();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EventDetailsView view)
    {
        this.view = view;
        notificationService.deletePlanCreateNotification(event.getId());
        initView();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public String getTitle()
    {
        return DateTimeUtil.getDetailsScreenTitle(event.getStartTime());
    }

    @Override
    public void toggleRsvp()
    {
        if (view == null) {
            return;
        }


        final boolean oldRsvp = event.getRsvp() == Event.RSVP.YES;
        if (isRsvpUpdateInProgress) {
            view.resetEvent(event);
            return;
        }

        isRsvpUpdateInProgress = true;

        if (oldRsvp) {
            event.setRsvp(Event.RSVP.NO);
        }
        else {
            event.setRsvp(Event.RSVP.YES);
        }
        view.resetEvent(event);
        processEditState();
        processEventActions();

        Subscription subscription =
                eventService
                        ._updateRsvp(event)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isRsvpUpdateInProgress = false;
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                if (oldRsvp) {
                                    event.setRsvp(Event.RSVP.YES);
                                }
                                else {
                                    event.setRsvp(Event.RSVP.NO);
                                }

                                view.resetEvent(event);
                                processEditState();
                                processEventActions();
                                isRsvpUpdateInProgress = false;
                            }

                            @Override
                            public void onNext(Boolean isSuccess)
                            {
                                if (!isSuccess) {
                                    if (oldRsvp) {
                                        event.setRsvp(Event.RSVP.YES);
                                    }
                                    else {
                                        event.setRsvp(Event.RSVP.NO);
                                    }

                                    view.resetEvent(event);
                                    processEditState();
                                    processEventActions();
                                    isRsvpUpdateInProgress = false;
                                }
                                else {

                                    removeEventFromNotGoingList();
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void setStatus(String status)
    {
        if (status == null) {
            status = "";
        }

        if (!status.equals(event.getStatus())) {
            event.setStatus(status);
            view.resetEvent(event);

            if (isLastMinute && !status.isEmpty()) {
                eventService.updateStatus(event, true);
            }
            else {
                eventService.updateStatus(event, false);
            }
        }
    }

    @Override
    public void sendInvitationResponse(String invitationResponse)
    {
        addEventToNotGoingList();
        processEventActions();

        eventService.sendInvitationResponse(event.getId(), invitationResponse);
    }

    @Override
    public void invite()
    {
        view.navigateToInvite(event.getId());
    }

    @Override
    public void chat()
    {
        notificationService.getNotifications(Notification.CHAT, event.getId())
                .flatMap(new Func1<List<Notification>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Notification> notifications)
                    {

                        List<Integer> notificationIds = getNotificationIdsList(notifications);
                        notificationService.deleteNotificationFromCache(notificationIds);
                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
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

        view.navigateToChat(event.getId());
    }

    @Override
    public void edit()
    {
        view.navigateToEdit(event);
    }

    @Override
    public void delete()
    {
        view.showLoading();
        eventService
                ._deleteEvent(event.getId())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {
                        view.navigateToHome();
                        view.hideLoading();
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Boolean isSuccessful)
                    {
                        view.navigateToHome();
                        view.hideLoading();
                    }
                });
    }

    /* Helper Methods */
    private void initView()
    {
        view.init(userService.getSessionUser(), event, isLastMinute);
        processEventActions();

        displayDetails(event);
        fetchChatNotifications();

        if (!isNetworkUpdated) {
            view.showLoading();
            fetchEventDetailsFromNetwork();
        }
    }

    private void fetchChatNotifications()
    {
        Subscription subscription = notificationService.getNotifications(Notification.CHAT, event
                .getId())
                .observeOn(AndroidSchedulers.mainThread())
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
                        if (notifications.size() > 0) {
                            view.displayChatMarker();
                        }
                        else {

                            view.hideChatMarker();
                        }
                    }
                });

        subscriptions.add(subscription);

    }

    private void fetchEventDetailsFromNetwork()
    {
        Subscription subscription =
                eventService
                        ._fetchEventNetwork(event.getId())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Event>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                isNetworkUpdated = true;
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                if (((RetrofitError) e).getResponse().getStatus() == 404) {
                                    view.showPlanNotAvailableMessage();
                                    eventService.clearEventFromCache(event.getId());
                                }
                            }

                            @Override
                            public void onNext(Event event)
                            {
                                if (event.isExpired()) {

                                    view.showPlanExpiredMessage();
                                    eventService.clearEventFromCache(event.getId());
                                }
                                else {

                                    EventDetailsPresenterImpl.this.event = event;

                                    displayDetails(event);
                                    view.hideLoading();

                                    processEditState();
                                    processDeleteVisibility();
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    private void processEventActions()
    {
        if (event.getRsvp() == Event.RSVP.YES) {
            view.displayYayActions();
        }
        else {

            view.displayNayActions((event.getInviterCount() > 0) && !hasDeclinedInvitation());
        }
    }

    private void processDeleteVisibility()
    {
        view.setDeleteVisibility(event.getCreatorId().equals(userService.getSessionUserId()));
    }

    private void processEditState()
    {
        view.setEditVisibility(event.getRsvp() == Event.RSVP.YES);
    }

    private void displayDetails(Event event)
    {
        this.event = event;
        List<Attendee> attendees = event.getAttendees();

        if (event.getRsvp() == Event.RSVP.YES) {

            Attendee attendee = new Attendee();
            attendee.setId(userService.getSessionUserId());
            attendees.remove(attendee);
        }

        List<AttendeeWrapper> attendeeWrappers = getAttendeeWrappers(attendees);

        Collections.sort(attendeeWrappers, new EventAttendeeComparator());
        view.displayAttendees(attendeeWrappers);
    }

    private List<AttendeeWrapper> getAttendeeWrappers(List<Attendee> attendees)
    {
        List<AttendeeWrapper> attendeeWrappers = new ArrayList<>();

        for (Attendee attendee : attendees) {
            attendeeWrappers.add(new AttendeeWrapper(attendee, event.getInviter(), event
                    .getFriends()));
        }

        return attendeeWrappers;
    }

    private void processIsLastMinute()
    {
        isLastMinute = DateTime.now().plusHours(1).isAfter(event.getStartTime());
    }

    private void addEventToNotGoingList()
    {
        Set<String> notGoingEvents = getNotGoingEvents();

        if (notGoingEvents == null) {
            Set<String> notGoingEventsSet = new HashSet<>();
            notGoingEventsSet.add(event.getId());
            genericCache.put(GenericCacheKeys.NOT_GOING_EVENT_LIST, notGoingEventsSet);
        }
        else {

            notGoingEvents.add(event.getId());
            genericCache.put(GenericCacheKeys.NOT_GOING_EVENT_LIST, notGoingEvents);
        }
    }

    private void removeEventFromNotGoingList()
    {
        Set<String> notGoingEvents = getNotGoingEvents();

        if (notGoingEvents != null) {
            notGoingEvents.remove(event.getId());

            genericCache.put(GenericCacheKeys.NOT_GOING_EVENT_LIST, notGoingEvents);
        }
    }

    private Set<String> getNotGoingEvents()
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> notGoingEvents = GsonProvider.getGson().fromJson(genericCache.get
                (GenericCacheKeys.NOT_GOING_EVENT_LIST), type);

        return notGoingEvents;
    }

    private boolean hasDeclinedInvitation()
    {
        Set<String> notGoingEvents = getNotGoingEvents();

        if (notGoingEvents != null) {
            boolean hasDeclinedInvitation = notGoingEvents.contains(event.getId());
            return hasDeclinedInvitation;
        }
        else {

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
