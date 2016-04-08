package com.clanout.app.ui.screens.pending_invites.mvp;

import android.util.Pair;

import com.clanout.app.config.AppConstants;
import com.clanout.app.model.Event;
import com.clanout.app.model.util.PhoneUtils;
import com.clanout.app.service.EventService;
import com.clanout.app.service.UserService;

import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class PendingInvitesPresenterImpl implements PendingInvitesPresenter
{
    private PendingInvitesView view;
    private UserService userService;
    private EventService eventService;

    private CompositeSubscription subscriptions;

    public PendingInvitesPresenterImpl(UserService userService, EventService eventService)
    {
        this.userService = userService;
        this.eventService = eventService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(PendingInvitesView view)
    {
        this.view = view;
        userService.markUserAsOld();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void fetchPendingInvites(String mobileNumber)
    {
        String parsedPhone = PhoneUtils.parsePhone(mobileNumber, AppConstants.DEFAULT_COUNTRY_CODE);
        if (parsedPhone == null)
        {
            view.displayInvalidMobileNumberMessage();
            return;
        }

        view.showLoading();

        Subscription subscription =
                eventService
                        ._fetchPendingInvites(parsedPhone)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Pair<Integer, List<Event>>>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                view.hideLoading();
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                view.displayNoActivePendingInvitationsMessage();
                            }

                            @Override
                            public void onNext(Pair<Integer, List<Event>> data)
                            {
                                int expiredInvitesCount = data.first;
                                List<Event> pendingInvites = data.second;

                                if (expiredInvitesCount > 0)
                                {
                                    view.displayExpiredEventsMessage(expiredInvitesCount);
                                }

                                if (pendingInvites.isEmpty())
                                {
                                    view.displayNoActivePendingInvitationsMessage();
                                }
                                else
                                {
                                    view.displayActivePendingInvitation(pendingInvites);
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void skip()
    {
        view.navigateToHome();
    }

    @Override
    public void gotoHome()
    {
        view.navigateToHome();
    }

    @Override
    public void selectInvite(Event event)
    {
        view.navigateToDetails(event.getId());
    }
}
