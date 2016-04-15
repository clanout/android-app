package com.clanout.app.ui.screens.edit.mvp;

import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Event;
import com.clanout.app.model.Location;
import com.clanout.app.model.util.DateTimeUtil;
import com.clanout.app.service.EventService;

import org.joda.time.DateTime;

import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;

public class EditEventPresenterImpl implements EditEventPresenter
{
    /* Services */
    private EventService eventService;

    /* Data */
    private Event originalEvent;

    private CompositeSubscription subscriptions;

    /* View */
    private EditEventView view;

    public EditEventPresenterImpl(EventService eventService, Event originalEvent)
    {
        this.eventService = eventService;
        this.originalEvent = originalEvent;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(EditEventView view)
    {
        this.view = view;
        this.view.init(originalEvent);
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void edit(DateTime startTime, Location location, String description)
    {
        DateTime endTime = DateTimeUtil.getEndTime(startTime);
        if (startTime.equals(originalEvent.getStartTime()))
        {
            startTime = null;
            endTime = null;
        }
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,
                    GoogleAnalyticsConstants.ACTION_EDIT_TIME, GoogleAnalyticsConstants
                            .LABEL_SUCCESS);
            /* Analytics */
        }

        if (originalEvent.getLocation().equals(location))
        {
            location = null;
        }
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT, GoogleAnalyticsConstants.ACTION_EDIT_LOCATION, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */
        }


        if (description.equals(originalEvent.getDescription()))
        {
            description = null;
        }
        else
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT, GoogleAnalyticsConstants.ACTION_EDIT_DESCRIPTION, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */
        }

        if(startTime == null && description == null && location == null)
        {
            view.navigateToDetailsScreen(originalEvent.getId());
            return;
        }

        Subscription subscription = eventService
                ._editEvent(originalEvent, startTime, endTime, location, description)
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
                        view.displayError();
                    }

                    @Override
                    public void onNext(Boolean isSuccess)
                    {
                        if (isSuccess)
                        {
                            view.navigateToDetailsScreen(originalEvent.getId());
                        }
                        else
                        {
                            view.displayError();
                        }
                    }
                });

        subscriptions.add(subscription);
    }
}
