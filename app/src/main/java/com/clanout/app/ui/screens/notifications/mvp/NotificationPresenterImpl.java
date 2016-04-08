package com.clanout.app.ui.screens.notifications.mvp;

import com.clanout.app.model.NotificationWrapper;
import com.clanout.app.service.NotificationService;

import java.util.ArrayList;
import java.util.List;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class NotificationPresenterImpl implements NotificationPresenter
{
    private NotificationView view;
    private NotificationService notificationService;

    private List<NotificationWrapper> notifications;

    private CompositeSubscription subscriptions;

    public NotificationPresenterImpl(NotificationService notificationService)
    {
        this.notificationService = notificationService;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(NotificationView view)
    {
        this.view = view;

        notificationService.markAllNotificationsAsRead();
        fetchNotifications();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        this.view = null;
    }

    @Override
    public void onNotificationSelected(final NotificationWrapper notification)
    {
        String eventId = notification.getEventId();
        int type = notification.getType();
        Timber.v(">>>> type = " + type);
        Timber.v(">>>> event_id = " + eventId);

        switch (type)
        {
            case NotificationWrapper.Type.EVENT_INVITATION:
                view.navigateToDetailsScreen(eventId);
                break;

            case NotificationWrapper.Type.NEW_FRIEND_JOINED_APP:
                view.navigateToFriendsScreen();
                break;

            case NotificationWrapper.Type.EVENT_REMOVED:
                view.displayEventRemovedMessage(notification);
                break;

            case NotificationWrapper.Type.EVENT_ACTIVITY:
                if (notification.getNotificationItems().size() > 1)
                {
                    view.navigateToDetailsScreen(eventId);
                }
                else
                {
                    NotificationWrapper.NotificationItem item = notification.getNotificationItems()
                                                                            .get(0);
                    if (item.getType() == NotificationWrapper.NotificationItem.Type.NEW_CHAT)
                    {
                        view.navigateToChatScreen(eventId);
                    }
                    else
                    {
                        view.navigateToDetailsScreen(eventId);
                    }
                }
                break;
        }

        notificationService.deleteNotificationFromCache(notification.getNotificationIds());
    }

    @Override
    public void onNotificationDeleted(int position)
    {
        NotificationWrapper deletedNotification = notifications.remove(position);

        if (notifications.isEmpty())
        {
            view.displayNoNotificationsMessage();
        }

        notificationService.deleteNotificationFromCache(deletedNotification.getNotificationIds());
    }

    @Override
    public void deleteAll()
    {
        view.displayNoNotificationsMessage();
        notificationService.deleteAllNotificationsFromCache();
    }

    private void fetchNotifications()
    {
        view.showLoading();

        Subscription subscription =
                notificationService
                        ._fetchNotifications()
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<List<NotificationWrapper>>()
                        {
                            @Override
                            public void onCompleted()
                            {
                                if (notifications == null || notifications.isEmpty())
                                {
                                    notifications = new ArrayList<>();
                                }
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                view.displayNoNotificationsMessage();
                            }

                            @Override
                            public void onNext(List<NotificationWrapper> notifications)
                            {
                                if (notifications.isEmpty())
                                {
                                    view.displayNoNotificationsMessage();
                                }
                                else
                                {
                                    NotificationPresenterImpl.this.notifications = notifications;
                                    view.displayNotifications(notifications);
                                }
                            }
                        });

        subscriptions.add(subscription);
    }
}
