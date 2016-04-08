package com.clanout.app.service;

import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.fb.FacebookApi;
import com.clanout.app.api.fb.response.FacebookCoverPicResponse;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.facebook.AccessToken;
import com.facebook.GraphRequest;
import com.facebook.GraphResponse;
import com.facebook.login.LoginManager;

import org.json.JSONArray;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class FacebookService
{
    public static FacebookService instance;

    public static FacebookService getInstance()
    {
        if (instance == null)
        {
            instance = new FacebookService();
        }

        return instance;
    }

    public static List<String> PERMISSIONS = Arrays.asList("email", "user_friends");

    private FacebookService()
    {
    }

    public boolean isAccessTokenValid()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        return accessToken != null && !accessToken.isExpired() && accessToken
                .getDeclinedPermissions().size() == 0;
    }

    public void logout()
    {
        LoginManager.getInstance().logOut();
    }

    public String getAccessToken()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
        {
            return null;
        }

        return accessToken.getToken();
    }

    public Set<String> getDeclinedPermissions()
    {
        AccessToken accessToken = AccessToken.getCurrentAccessToken();
        if (accessToken == null)
        {
            return null;
        }

        return accessToken.getDeclinedPermissions();
    }

    public Observable<String> getCoverPicUrl()
    {
        return ApiManager.getFacebookApi()
                .getCoverPic()
                .map(new Func1<FacebookCoverPicResponse, String>()
                {
                    @Override
                    public String call(FacebookCoverPicResponse response)
                    {
                        return response.getCover().getSource();
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<String>> getFriends()
    {
        final GraphResponse[] response = new GraphResponse[1];
        final int[] totalFriends = {0};
        final List<String> friendsIdList = new ArrayList<>();

        return Observable
                .create(new Observable.OnSubscribe<List<String>>()
                        {
                            @Override
                            public void call(final Subscriber<? super List<String>> subscriber)
                            {
                                GraphRequest graphRequest =
                                        GraphRequest.newMyFriendsRequest(AccessToken
                                                        .getCurrentAccessToken(), new
                                                        GraphRequest.GraphJSONArrayCallback()
                                                {
                                                    @Override
                                                    public void onCompleted(JSONArray jsonArray,
                                                                            GraphResponse
                                                                                    graphResponse)
                                                    {
                                                        response[0] = graphResponse;
                                                        friendsIdList.clear();
                                                        JSONObject jsonObject = response[0]
                                                                .getJSONObject();
                                                        totalFriends[0] = 0;

                                                        try {
                                                            JSONArray jsonArrayData = jsonObject
                                                                    .getJSONArray("data");
                                                            JSONObject dataObject;
                                                            for (int j = 0; j < jsonArrayData
                                                                    .length(); j++) {

                                                                dataObject = (JSONObject)
                                                                        jsonArrayData
                                                                        .get(j);
                                                                friendsIdList
                                                                        .add(dataObject.getString
                                                                                ("id"));
                                                            }

                                                            JSONObject jsonObjectSummary =
                                                                    jsonObject
                                                                    .getJSONObject("summary");
                                                            totalFriends[0] = Integer
                                                                    .parseInt(jsonObjectSummary
                                                                            .getString
                                                                                    ("total_count"));
                                                        }
                                                        catch (Exception e) {

                                                            /* Analytics */
                                                            AnalyticsHelper
                                                                    .sendCaughtExceptions
                                                                            (GoogleAnalyticsConstants.METHOD_F, false);
                                                            /* Analytics */

                                                            subscriber.onNext(null);
                                                            subscriber.onCompleted();
                                                            totalFriends[0] = -1;
                                                            return;
                                                        }

                                                        int count = (totalFriends[0] / 25) + 2;

                                                        for (int i = 0; i < count; i++) {
                                                            GraphRequest nextPageRequest =
                                                                    response[0]
                                                                    .getRequestForPagedResults
                                                                            (GraphResponse
                                                                                    .PagingDirection.NEXT);
                                                            if (nextPageRequest != null) {
                                                                nextPageRequest
                                                                        .setCallback(new GraphRequest.Callback()
                                                                        {
                                                                            @Override
                                                                            public void
                                                                            onCompleted
                                                                                    (GraphResponse graphResponse)
                                                                            {
                                                                                response[0] =
                                                                                        graphResponse;
                                                                                JSONObject
                                                                                        responseObject = response[0]
                                                                                        .getJSONObject();

                                                                                try {
                                                                                    JSONArray
                                                                                            jsonArrayData = responseObject
                                                                                            .getJSONArray("data");
                                                                                    JSONObject
                                                                                            dataObject;
                                                                                    for (int j =
                                                                                         0; j <
                                                                                            jsonArrayData
                                                                                            .length(); j++) {

                                                                                        dataObject = (JSONObject) jsonArrayData
                                                                                                .get(j);
                                                                                        friendsIdList
                                                                                                .add(dataObject
                                                                                                        .getString("id"));
                                                                                    }
                                                                                }
                                                                                catch (Exception
                                                                                        e) {

                                                                                    /* Analytics */
                                                                                    AnalyticsHelper
                                                                                            .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_G, false);
                                                                                    /* Analytics */

                                                                                    subscriber
                                                                                            .onNext(null);
                                                                                    subscriber
                                                                                            .onCompleted();
                                                                                    totalFriends[0] = -1;
                                                                                    return;
                                                                                }

                                                                            }
                                                                        });

                                                                nextPageRequest.executeAndWait();
                                                            }
                                                            else {
                                                                break;
                                                            }
                                                        }

                                                    }
                                                }
                                        );

                                graphRequest.executeAndWait();

                                if (totalFriends[0] != -1)
                                {
                                    subscriber.onNext(friendsIdList);
                                    subscriber.onCompleted();
                                }
                            }
                        }

                )
                .subscribeOn(Schedulers.newThread());
    }
}

