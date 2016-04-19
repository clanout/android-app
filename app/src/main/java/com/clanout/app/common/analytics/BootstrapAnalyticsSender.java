package com.clanout.app.common.analytics;

import com.clanout.app.model.Event;
import com.clanout.app.model.Friend;
import com.clanout.app.service.EventService;
import com.clanout.app.service.UserService;

import java.util.List;

import rx.Subscriber;
import rx.schedulers.Schedulers;

/**
 * Created by Zuko on 4/2/16.
 */
public class BootstrapAnalyticsSender
{
    private static BootstrapAnalyticsSender instance;

    public static BootstrapAnalyticsSender getInstance()
    {
        if (instance == null)
        {
            instance = new BootstrapAnalyticsSender();
        }
        return instance;
    }

    private boolean isAnalyticsSent;

    private BootstrapAnalyticsSender()
    {}

    public void send()
    {
        if(!isAnalyticsSent)
        {
            //User Zone:
            AnalyticsHelper.sendCustomDimension(1, UserService.getInstance().getSessionUser().getLocationZone());

            //local Friends Count:
            UserService userService = UserService.getInstance();
            userService._fetchLocalAppFriends()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
                    .subscribe(new Subscriber<List<Friend>>()
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
                        public void onNext(List<Friend> friends)
                        {
                            AnalyticsHelper.sendCustomDimension(2, String.valueOf(friends.size()));
                        }
                    });

            //No. of plans in feed:
            EventService eventService = EventService.getInstance();
            eventService._fetchEvents()
                    .subscribeOn(Schedulers.newThread())
                    .observeOn(Schedulers.newThread())
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
                            AnalyticsHelper.sendCustomDimension(3, String.valueOf(events.size()));
                        }
                    });

            isAnalyticsSent = true;
        }
    }
}
