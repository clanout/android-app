package com.clanout.app.ui.screens.details.mvp;

import com.clanout.app.model.Event;
import com.clanout.app.service.EventService;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class EventDetailsContainerPresenterImpl implements EventDetailsContainerPresenter
{
    private EventService eventService;
    private EventDetailsContainerView view;
    private String eventId;

    private List<Event> events;
    private int activePosition;

    public EventDetailsContainerPresenterImpl(EventService eventService, String eventId)
    {
        this.eventService = eventService;
        this.eventId = eventId;

        activePosition = -1;
        events = new ArrayList<>();
    }

    @Override
    public void attachView(final EventDetailsContainerView view)
    {
        this.view = view;

        getEventsObservable()
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
                        view.handleError();
                    }

                    @Override
                    public void onNext(List<Event> events)
                    {
                        if (activePosition == -1)
                        {
                            Event activeEvent = new Event();
                            activeEvent.setId(eventId);

                            activePosition = events.indexOf(activeEvent);
                            if (activePosition == -1)
                            {
                                activePosition = 0;
                            }
                        }

                        view.initView(events, activePosition);

                        EventDetailsContainerPresenterImpl.this.events = events;
                    }
                });
    }

    @Override
    public void detachView()
    {
        view = null;
    }

    @Override
    public void setActivePosition(int activePosition)
    {
        this.activePosition = activePosition;

        eventService.markEventAsSeen(events.get(activePosition).getId())
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
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

    private Observable<List<Event>> getEventsObservable()
    {
        return Observable
                .just(events)
                .flatMap(new Func1<List<Event>, Observable<List<Event>>>()
                {
                    @Override
                    public Observable<List<Event>> call(List<Event> events)
                    {
                        if (events == null || events.isEmpty())
                        {
                            return eventService._fetchEvents();
                        }
                        else
                        {
                            return Observable.just(events);
                        }
                    }
                });
    }
}
