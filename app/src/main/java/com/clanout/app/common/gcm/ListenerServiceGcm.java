package com.clanout.app.common.gcm;

import android.os.Bundle;

import com.clanout.app.communication.Communicator;
import com.clanout.app.model.Notification;
import com.clanout.app.model.util.NotificationFactory;
import com.clanout.app.service.NotificationService;
import com.google.android.gms.gcm.GcmListenerService;
import com.squareup.otto.Bus;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ListenerServiceGcm extends GcmListenerService
{
    private NotificationService notificationService;
    private Bus bus;

    public ListenerServiceGcm()
    {
        bus = Communicator.getInstance().getBus();
        notificationService = NotificationService.getInstance();
    }

    @Override
    public void onMessageReceived(String from, final Bundle data)
    {
        Timber.d("GCM message received : " + data.toString());

        NotificationFactory.create(data)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Notification>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        Timber.d("onError ---- " + e.getMessage());
                    }

                    @Override
                    public void onNext(Notification notification)
                    {
                        if(notification != null) {
                            notificationService.handleNotification(notification);
                        }
                    }
                });
    }
}
