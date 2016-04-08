package com.clanout.app.ui.screens.home.feed.mvp;

import android.util.Log;
import android.util.Pair;

import com.clanout.app.cache._core.CacheManager;
import com.clanout.app.cache.user.UserCache;
import com.clanout.app.model.Event;
import com.clanout.app.model.Friend;
import com.clanout.app.service.EventService;
import com.clanout.app.service.UserService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class EventFeedPresenterImpl implements EventFeedPresenter
{
    /* Services */
    private EventService eventService;
    private UserService userService;

    /* View */
    private EventFeedView view;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    public EventFeedPresenterImpl(EventService eventService, UserService userService)
    {
        this.eventService = eventService;
        this.userService = userService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(final EventFeedView view)
    {
        this.view = view;

        this.view.showLoading();

        Subscription subscription = Observable
                .zip(eventService._fetchEvents(), userService
                        ._fetchLocalFacebookFriendsCache()
                        , new Func2<List<Event>, List<Friend>, Pair<List<Event>, List<Friend>>>()
                {
                    @Override
                    public Pair<List<Event>, List<Friend>> call(List<Event> events, List<Friend>
                            friends)
                    {
                        return new Pair<List<Event>, List<Friend>>(events, friends);
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<Event>, List<Friend>>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.showError();
                    }

                    @Override
                    public void onNext(Pair<List<Event>, List<Friend>> pair)
                    {
                        List<Event> events = pair.first;
                        List<Friend> friends = pair.second;

                        if(events == null)
                        {
                            events = new ArrayList<Event>();
                        }

                        if(friends == null)
                        {
                            friends = new ArrayList<Friend>();
                        }

                        if (events.isEmpty()) {
                            view.showNoEventsMessage();
                        }
                        else {

                            view.showEvents(events, friends);
                        }
                    }
                });


        subscriptions.add(subscription);


    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void refreshEvents()
    {
        Subscription subscription = Observable
                .zip(eventService._fetchEventsNetwork(), userService
                        ._fetchLocalFacebookFriendsCache()
                        , new Func2<List<Event>, List<Friend>, Pair<List<Event>, List<Friend>>>()
                {
                    @Override
                    public Pair<List<Event>, List<Friend>> call(List<Event> events, List<Friend>
                            friends)
                    {
                        return new Pair<List<Event>, List<Friend>>(events, friends);
                    }
                }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<Event>, List<Friend>>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.showError();
                    }

                    @Override
                    public void onNext(Pair<List<Event>, List<Friend>> pair)
                    {
                        List<Event> events = pair.first;
                        List<Friend> friends = pair.second;

                        if(events == null)
                        {
                            events = new ArrayList<Event>();
                        }

                        if(friends == null)
                        {
                            friends = new ArrayList<Friend>();
                        }

                        if (events.isEmpty()) {
                            view.showNoEventsMessage();
                        }
                        else {

                            view.showEvents(events, friends);
                        }
                    }
                });


        subscriptions.add(subscription);
    }

    @Override
    public void selectEvent(Event event)
    {
        view.gotoDetailsView(event.getId());
    }
}
