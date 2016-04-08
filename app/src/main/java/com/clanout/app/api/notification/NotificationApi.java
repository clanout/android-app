package com.clanout.app.api.notification;

import com.clanout.app.api.notification.request.GCmRegisterUserApiRequest;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface NotificationApi
{
    @POST("/notification/register-gcm")
    Observable<Response> registerUser(@Body GCmRegisterUserApiRequest request);
}
