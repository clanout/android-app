package com.clanout.app.service;

import android.content.Context;
import android.content.Intent;

import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.common.gcm.RegistrationIntentService;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.root.ClanOut;
import com.google.android.gms.gcm.GcmPubSub;

import java.io.IOException;

import rx.Observable;
import rx.Subscriber;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class GcmService
{
    private static GcmService instance;

    public static GcmService getInstance()
    {
        if (instance == null)
        {
            instance = new GcmService();
        }

        return instance;
    }

    private static final String TOPIC_BASE_URL = "/topics/";

    private GcmService()
    {
    }

    public void register()
    {
        Context context = ClanOut.getClanOutContext();
        Intent intent = new Intent(context, RegistrationIntentService.class);
        context.startService(intent);
    }

    public void subscribeTopic(final String token, final String topic)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        try
                        {
                            GcmPubSub.getInstance(ClanOut.getClanOutContext())
                                     .subscribe(token, TOPIC_BASE_URL + topic, null);
                            subscriber.onCompleted();
                        }
                        catch (IOException e)
                        {

                            /* Analytics */
                            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants
                                    .METHOD_H, false);
                            /* Analytics */

                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {
                        Timber.e("[Event Subscription Failed] " + e.getMessage());
                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }

    public void unsubscribeTopic(final String token, final String topic)
    {
        Observable
                .create(new Observable.OnSubscribe<Object>()
                {
                    @Override
                    public void call(Subscriber<? super Object> subscriber)
                    {
                        try
                        {
                            GcmPubSub.getInstance(ClanOut.getClanOutContext())
                                     .unsubscribe(token, TOPIC_BASE_URL + topic);
                            subscriber.onCompleted();
                        }
                        catch (IOException e)
                        {
                            /* Analytics */
                            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_E, false);
                            /* Analytics */

                            subscriber.onError(e);
                        }

                    }
                })
                .subscribeOn(Schedulers.newThread())
                .observeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Object>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Object o) {

                    }
                });
    }
}
