package com.clanout.app.ui.screens.create.mvp;

import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Location;
import com.clanout.app.model.util.DateTimeUtil;
import com.clanout.app.service.EventService;
import com.clanout.app.service.LocationService;

import org.joda.time.DateTime;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class CreateEventPresenterImpl implements CreateEventPresenter
{
    /* Services */
    private EventService eventService;
    private LocationService locationService;

    /* Subscriptions */
    private CompositeSubscription subscriptions;

    /* View */
    private CreateEventView view;

    public CreateEventPresenterImpl(EventService eventService, LocationService locationService)
    {
        this.eventService = eventService;
        this.locationService = locationService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(CreateEventView view)
    {
        this.view = view;
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void create(String title, Event.Type type, EventCategory category, String description,
                       DateTime startTime, Location location)
    {
        if (view == null)
        {
            return;
        }

        view.showLoading();
        if (title == null || title.isEmpty())
        {
            view.displayEmptyTitleError();
            return;
        }

        DateTime now = DateTime.now();
        if (startTime.isBefore(now))
        {
            view.displayInvalidTimeError();
            return;
        }

        DateTime endTime = DateTimeUtil.getEndTime(startTime);

        Subscription subscription = eventService
                ._create(title, type, category, description, location, startTime, endTime)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.displayError();
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        view.navigateToInviteScreen(event.getId());
                    }
                });

        subscriptions.add(subscription);
    }
}
