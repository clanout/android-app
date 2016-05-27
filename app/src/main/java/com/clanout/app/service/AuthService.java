package com.clanout.app.service;

import com.clanout.app.api.auth.response.CreateSessionApiResponse;
import com.clanout.app.api.auth.response.RefreshSessionApiResponse;
import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.user.request.GetUserDetailsApiRequest;
import com.clanout.app.api.user.response.GetUserDetailsApiResponse;
import com.clanout.app.cache.core.CacheManager;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.User;
import com.clanout.app.root.ClanOut;

import rx.Observable;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class AuthService
{
    private static final String FACEBOOK_AUTH_TYPE = "FACEBOOK";
    private static AuthService instance;

    public static void init(FacebookService facebookService, UserService userService)
    {
        instance = new AuthService(facebookService, userService);
    }

    public static AuthService getInstance()
    {
        if (instance == null) {
            throw new IllegalStateException("[AuthService Not Initialized]");
        }

        return instance;
    }

    private FacebookService facebookService;
    private UserService userService;
    private GenericCache genericCache;

    private AuthService(FacebookService facebookService, UserService userService)
    {
        this.facebookService = facebookService;
        this.userService = userService;
        genericCache = CacheManager.getGenericCache();

    }

    public Observable<Boolean> initSession()
    {
        return validateSession()
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isSessionValid)
                    {
                        if (isSessionValid) {
                            return Observable.just(true);
                        }
                        else {
                            return refreshSession();
                        }
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isSessionRefreshed)
                    {
                        if (isSessionRefreshed) {
                            return Observable.just(true);
                        }
                        else {
                            return createSession();
                        }
                    }
                })
                .doOnNext(new Action1<Boolean>()
                {
                    @Override
                    public void call(Boolean isSessionCreated)
                    {
                        if (isSessionCreated) {
                            ClanOut.getAnalyticsTracker().set("&uid", userService.getSessionUserId());
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public void logout()
    {
        facebookService.logout();
        CacheManager.clearAllCaches();
    }

    /* Helper Methods */
    private Observable<Boolean> refreshSession()
    {
        String refreshToken = genericCache.get(GenericCacheKeys.REFRESH_TOKEN);
        if (refreshToken == null) {
            return Observable.just(false);
        }
        else {

            return ApiManager.getAuthApi().refreshSession(refreshToken)
                    .map(new Func1<RefreshSessionApiResponse, Boolean>()
                    {
                        @Override
                        public Boolean call(RefreshSessionApiResponse refreshSessionApiResponse)
                        {
                            String refreshToken = refreshSessionApiResponse.getRefreshToken();
                            String accessToken = refreshSessionApiResponse.getAccessToken();
                            genericCache.put(GenericCacheKeys.REFRESH_TOKEN, refreshToken);
                            genericCache.put(GenericCacheKeys.ACCESS_TOKEN, accessToken);

                            resetAdapter();

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
                    .flatMap(new Func1<Boolean, Observable<Boolean>>()
                    {
                        @Override
                        public Observable<Boolean> call(Boolean isSessionRefreshed)
                        {
                            if (isSessionRefreshed) {
                                return getUser()
                                        .map(new Func1<User, Boolean>()
                                        {
                                            @Override
                                            public Boolean call(User user)
                                            {
                                                return true;
                                            }
                                        });
                            }
                            else {
                                return Observable.just(false);
                            }
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    private void resetAdapter()
    {
        ApiManager.getInstance().resetAdapter();
    }

    private Observable<Boolean> createSession()
    {
        return ApiManager.getAuthApi().createSession(FACEBOOK_AUTH_TYPE, facebookService
                .getAccessToken())
                .map(new Func1<CreateSessionApiResponse, Boolean>()
                {
                    @Override
                    public Boolean call(CreateSessionApiResponse createSessionApiResponse)
                    {
                        String refreshToken = createSessionApiResponse.getRefreshToken();
                        String accessToken = createSessionApiResponse.getAccessToken();
                        genericCache.put(GenericCacheKeys.REFRESH_TOKEN, refreshToken);
                        genericCache.put(GenericCacheKeys.ACCESS_TOKEN, accessToken);

                        userService.setIsNew(createSessionApiResponse.isNew());
                        resetAdapter();
                        return true;
                    }
                })
                .onErrorReturn(new Func1<Throwable, Boolean>()
                {
                    @Override
                    public Boolean call(Throwable throwable)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants
                                .METHOD_UNABLE_TO_CREATE_SESSION, false);
                        /* Analytics */

                        return false;
                    }
                })
                .flatMap(new Func1<Boolean, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Boolean isSessionCreated)
                    {
                        if (isSessionCreated) {
                            return getUser()
                                    .map(new Func1<User, Boolean>()
                                    {
                                        @Override
                                        public Boolean call(User user)
                                        {
                                            return true;
                                        }
                                    });
                        }
                        else {
                            return Observable.just(false);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private Observable<Boolean> validateSession()
    {
        return getUser()
                .map(new Func1<User, Boolean>()
                {
                    @Override
                    public Boolean call(User user)
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
                });
    }

    private Observable<User> getUser()
    {
        return ApiManager.getUserApi().getDetails(new GetUserDetailsApiRequest())
                .map(new Func1<GetUserDetailsApiResponse, User>()
                {
                    @Override
                    public User call(GetUserDetailsApiResponse getUserDetailsApiResponse)
                    {
                        return getUserDetailsApiResponse.getUser();
                    }
                })
                .doOnNext(new Action1<User>()
                {
                    @Override
                    public void call(User user)
                    {
                        userService.setSessionUser(user);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

}
