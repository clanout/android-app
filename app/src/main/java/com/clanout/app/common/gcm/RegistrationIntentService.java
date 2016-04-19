package com.clanout.app.common.gcm;

import android.app.IntentService;
import android.content.Intent;

import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.notification.NotificationApi;
import com.clanout.app.api.notification.request.GCmRegisterUserApiRequest;
import com.clanout.app.cache.core.CacheManager;
import com.clanout.app.cache.event.EventCache;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.model.Event;
import com.clanout.app.service.GcmService;
import com.google.android.gms.gcm.GoogleCloudMessaging;
import com.google.android.gms.iid.InstanceID;

import java.util.List;
import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class RegistrationIntentService extends IntentService
{
    private static final String TAG = "RegIntentService";
    private GenericCache genericCache;

    public RegistrationIntentService()
    {
        super(TAG);
        genericCache = CacheManager.getGenericCache();
    }

    @Override
    protected void onHandleIntent(Intent intent)
    {
        try
        {
            synchronized (TAG)
            {
                InstanceID instanceID = InstanceID.getInstance(this);
                String token = instanceID
                        .getToken(AppConstants.GCM_SENDER_ID, GoogleCloudMessaging.INSTANCE_ID_SCOPE, null);

                sendTokenToServer(token)
                        .flatMap(new Func1<Boolean, Observable<Boolean>>()
                        {
                            @Override
                            public Observable<Boolean> call(Boolean isTokenSent)
                            {
                                if (isTokenSent)
                                {
                                    return processEvents();
                                }
                                else
                                {
                                    throw new IllegalStateException("[GCM] Failed to push token to server");
                                }
                            }
                        })
                        .doOnNext(new Action1<Boolean>()
                        {
                            @Override
                            public void call(Boolean isEventsProcessingSuccessful)
                            {
                                if (!isEventsProcessingSuccessful)
                                {
                                    throw new IllegalStateException("[GCM] Failed to process events");
                                }
                            }
                        })
                        .subscribeOn(Schedulers.newThread())
                        .subscribe(new Subscriber<Boolean>()
                        {
                            @Override
                            public void onCompleted()
                            {
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                Timber.v(e.getMessage());
                            }

                            @Override
                            public void onNext(Boolean isSuccess)
                            {
                            }
                        });
            }
        }
        catch (Exception e)
        {
            genericCache.delete(GenericCacheKeys.GCM_TOKEN);
        }
    }

    private Observable<Boolean> sendTokenToServer(final String token)
    {
        NotificationApi notificationApi = ApiManager.getNotificationApi();

        GCmRegisterUserApiRequest request = new GCmRegisterUserApiRequest(token);
        return notificationApi
                .registerUser(request)
                .doOnNext(new Action1<Response>()
                {
                    @Override
                    public void call(Response response)
                    {
                        genericCache.put(GenericCacheKeys.GCM_TOKEN, token);
                    }
                })
                .doOnError(new Action1<Throwable>()
                {
                    @Override
                    public void call(Throwable throwable)
                    {
                        genericCache.delete(GenericCacheKeys.GCM_TOKEN);
                    }
                })
                .map(new Func1<Response, Boolean>()
                {
                    @Override
                    public Boolean call(Response response)
                    {
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        return false;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<Boolean> processEvents()
    {
        final GcmService gcmService = GcmService.getInstance();
        EventCache eventCache = CacheManager.getEventCache();
        return eventCache
                .getEvents()
                .flatMap(new Func1<List<Event>, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(List<Event> events)
                    {
                        int subscribeCount = 0;
                        int unsubscribeCount = 0;

                        for (Event event : events)
                        {
                            if (event.getRsvp() == Event.RSVP.YES)
                            {
                                gcmService.subscribeTopic(genericCache
                                        .get(GenericCacheKeys.GCM_TOKEN), event
                                        .getId());

                                subscribeCount++;
                            }
                            else
                            {
                                gcmService.unsubscribeTopic(genericCache
                                        .get(GenericCacheKeys.GCM_TOKEN), event
                                        .getId());

                                unsubscribeCount++;
                            }
                        }

                        Timber.v("[GCM] Subscribe Count = " + subscribeCount);

                        return Observable.just(true);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }
}
