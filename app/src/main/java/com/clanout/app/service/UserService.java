package com.clanout.app.service;

import android.util.Pair;

import com.clanout.app.api.core.ApiManager;
import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.api.user.request.BlockFriendsApiRequest;
import com.clanout.app.api.user.request.GetFacebookFriendsApiRequest;
import com.clanout.app.api.user.request.GetRegisteredContactsApiRequest;
import com.clanout.app.api.user.request.UpdateMobileAPiRequest;
import com.clanout.app.api.user.response.GetFacebookFriendsApiResponse;
import com.clanout.app.api.user.response.GetRegisteredContactsApiResponse;
import com.clanout.app.cache.core.CacheManager;
import com.clanout.app.cache.generic.GenericCache;
import com.clanout.app.cache.user.UserCache;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GenericCacheKeys;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Friend;
import com.clanout.app.model.User;
import com.clanout.app.model.util.FriendsComparator;
import com.google.gson.reflect.TypeToken;

import java.lang.reflect.Type;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import retrofit.client.Response;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.functions.Func2;
import rx.schedulers.Schedulers;

public class UserService
{
    private static UserService instance;

    public static void init(LocationService locationService, PhonebookService phonebookService, FacebookService facebookService)
    {
        instance = new UserService(locationService, phonebookService, facebookService);
    }

    public static UserService getInstance()
    {
        if (instance == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z11, false);
            /* Analytics */

            throw new IllegalStateException("[UserService Not Initialized]");
        }

        return instance;
    }

    private UserCache userCache;

    private GenericCache genericCache;
    private LocationService locationService;
    private PhonebookService phonebookService;
    private FacebookService facebookService;

    private User activeUser;

    private UserService(LocationService locationService, PhonebookService phonebookService, FacebookService facebookService)
    {
        this.locationService = locationService;
        this.phonebookService = phonebookService;
        this.facebookService = facebookService;

        userCache = CacheManager.getUserCache();
        genericCache = CacheManager.getGenericCache();
    }

    /* Session User */
    public void setSessionUser(User user)
    {
        if (user == null) {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z11, false);
            /* Analytics */

            throw new IllegalStateException("[Session User null]");
        }

        activeUser = user;
        genericCache.put(GenericCacheKeys.SESSION_USER, user);
    }

    public User getSessionUser()
    {
        if (activeUser == null) {
            activeUser = genericCache.get(GenericCacheKeys.SESSION_USER, User.class);
        }

        return activeUser;
    }

    public String getSessionUserId()
    {
        if (getSessionUser() == null) {
            return null;
        }

        return getSessionUser().getId();
    }

    public String getSessionUserName()
    {
        if (getSessionUser() == null) {
            return null;
        }

        return getSessionUser().getName();
    }

    /* Update Mobile Number */
    public void updatePhoneNumber(final String phoneNumber)
    {
        UpdateMobileAPiRequest request = new UpdateMobileAPiRequest(phoneNumber);

        ApiManager.getUserApi().updateMobile(request)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {
                        activeUser.setMobileNumber(phoneNumber);
                        setSessionUser(activeUser);
                    }
                });
    }

    /* Block/Unblock Facebook Friends */
    public void sendBlockRequests(List<String> blockList, List<String> unblockList)
    {
        BlockFriendsApiRequest request = new BlockFriendsApiRequest(blockList, unblockList);
        ApiManager.getUserApi().blockFriends(request)
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Response>()
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
                    public void onNext(Response response)
                    {
                        CacheManager.clearFriendsCache();
                    }
                });
    }

    /* Facebook Friends */
    public Observable<List<Friend>> _fetchLocalFacebookFriends()
    {
        return _fetchLocalFacebookFriendsCache()
                .flatMap(new Func1<List<Friend>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<Friend> cachedFriends)
                    {
                        if (!cachedFriends.isEmpty()) {
                            return Observable.just(cachedFriends);
                        }
                        else {
                            return _fetchFacebookFriendsNetwork(false)
                                    .map(new Func1<Pair<List<Friend>, List<Friend>>, List<Friend>>()
                                    {
                                        @Override
                                        public List<Friend> call(Pair<List<Friend>, List<Friend>>
                                                                         allFriends)
                                        {
                                            return allFriends.first;
                                        }
                                    });
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<Pair<List<Friend>, List<Friend>>> _fetchFacebookFriendsNetwork(final
                                                                                     boolean fetchAll)
    {
        final String zone = locationService.getCurrentLocation().getZone();
        GetFacebookFriendsApiRequest request = new GetFacebookFriendsApiRequest(null);
        if (!fetchAll) {
            request = new GetFacebookFriendsApiRequest(zone);
        }

        return ApiManager.getUserApi().getFacebookFriends(request)
                .map(new Func1<GetFacebookFriendsApiResponse, Pair<List<Friend>, List<Friend>>>()
                {
                    @Override
                    public Pair<List<Friend>, List<Friend>> call
                            (GetFacebookFriendsApiResponse response)
                    {
                        List<Friend> allFriends = response.getFriends();

                        identifyNewFriends(allFriends);

                        List<Friend> localFriends = new ArrayList<Friend>();
                        List<Friend> otherFriends = new ArrayList<Friend>();

                        for (Friend friend : allFriends) {
                            if (friend.getLocationZone().equals(zone)) {
                                localFriends.add(friend);
                            }
                            else {
                                otherFriends.add(friend);
                            }
                        }

                        Collections.sort(localFriends, new FriendsComparator());
                        Collections.sort(otherFriends, new FriendsComparator());

                        return new Pair<>(localFriends, otherFriends);
                    }
                })
                .doOnNext(new Action1<Pair<List<Friend>, List<Friend>>>()
                {
                    @Override
                    public void call(Pair<List<Friend>, List<Friend>> friends)
                    {
                        List<Friend> localFriends = friends.first;
                        // Cache Local Friends
                        userCache.saveFriends(localFriends);
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    private void identifyNewFriends(List<Friend> allFriends)
    {
        Type type = new TypeToken<Set<String>>()
        {
        }.getType();
        Set<String> newFriends = GsonProvider.getGson().fromJson(CacheManager.getGenericCache
                ().get
                (GenericCacheKeys.NEW_FRIENDS_LIST), type);

        if (newFriends != null) {
            for (Friend friend : allFriends) {
                if (newFriends.contains(friend.getId())) {
                    friend.setIsNew(true);
                }
                else {
                    friend.setIsNew(false);
                }
            }
        }
        else {

            for (Friend friend : allFriends) {
                friend.setIsNew(false);
            }
        }
    }

    public Observable<List<Friend>> _fetchLocalFacebookFriendsCache()
    {
        return userCache
                .getFriends()
                .subscribeOn(Schedulers.newThread());
    }

    /* Registered Phonebook Contacts */
    public Observable<List<Friend>> _fetchLocalRegisteredContacts()
    {
        return _fetchLocalRegisteredContactsCache()
                .flatMap(new Func1<List<Friend>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<Friend> cachedContacts)
                    {
                        if (!cachedContacts.isEmpty()) {
                            return Observable.just(cachedContacts);
                        }
                        else {
                            return _fetchRegisteredContactsNetwork(false);
                        }
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _fetchRegisteredContactsNetwork(final boolean fetchAll)
    {
        return phonebookService
                .fetchAllNumbers()
                .flatMap(new Func1<List<String>, Observable<List<Friend>>>()
                {
                    @Override
                    public Observable<List<Friend>> call(List<String> allContacts)
                    {
                        List<String> hashedMobileNumbers = new ArrayList<>();

                        try {

                            MessageDigest messageDigest = MessageDigest.getInstance("MD5");

                            for(String mobileNumber : allContacts)
                            {
                                messageDigest.update(mobileNumber.getBytes());
                                byte messageDigestBytes[] = messageDigest.digest();

                                StringBuffer hashedMobileNumber = new StringBuffer();
                                for (int i=0; i<messageDigestBytes.length; i++)
                                    hashedMobileNumber.append(Integer.toHexString(0xFF & messageDigestBytes[i]));

                                hashedMobileNumbers.add(hashedMobileNumber.toString());
                            }

                        }
                        catch (NoSuchAlgorithmException e) {
                            e.printStackTrace();
                        }


                        GetRegisteredContactsApiRequest request = new
                                GetRegisteredContactsApiRequest(hashedMobileNumbers, null);
                        if (!fetchAll) {
                            String zone = locationService.getCurrentLocation().getZone();
                            request = new GetRegisteredContactsApiRequest(hashedMobileNumbers, zone);
                        }

                        return ApiManager.getUserApi()
                                .getRegisteredContacts(request)
                                .map(new Func1<GetRegisteredContactsApiResponse, List<Friend>>()
                                {
                                    @Override
                                    public List<Friend> call(GetRegisteredContactsApiResponse
                                                                     response)
                                    {
                                        List<Friend> registeredContacts = response
                                                .getRegisteredContacts();
                                        Collections
                                                .sort(registeredContacts, new FriendsComparator());
                                        return registeredContacts;
                                    }
                                })
                                .map(new Func1<List<Friend>, List<Friend>>()
                                {
                                    @Override
                                    public List<Friend> call(List<Friend> friends)
                                    {
                                        String sessionUserId = getSessionUserId();
                                        Friend me = new Friend();
                                        me.setUserId(sessionUserId);
                                        friends.remove(me);
                                        return friends;
                                    }
                                })
                                .doOnNext(new Action1<List<Friend>>()
                                {
                                    @Override
                                    public void call(List<Friend> contacts)
                                    {
                                        if (!fetchAll) {
                                            // Cache local registered contacts
                                            userCache.saveContacts(contacts);
                                        }
                                    }
                                });
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _fetchLocalRegisteredContactsCache()
    {
        return userCache
                .getContacts()
                .subscribeOn(Schedulers.newThread());
    }

    /* App Friends */
    public Observable<List<Friend>> _fetchLocalAppFriends()
    {
        return Observable
                .zip(_fetchLocalFacebookFriends(), _fetchLocalRegisteredContacts(),
                        new Func2<List<Friend>, List<Friend>, List<Friend>>()
                        {
                            @Override
                            public List<Friend> call(List<Friend> facebookFriends, List<Friend>
                                    registeredContacts)
                            {
                                Set<Friend> allFriends = new HashSet<Friend>();
                                allFriends.addAll(facebookFriends);
                                allFriends.addAll(registeredContacts);
                                return new ArrayList<>(allFriends);
                            }
                        }
                )
                .map(new Func1<List<Friend>, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(List<Friend> friends)
                    {
                        Collections.sort(friends, new FriendsComparator());
                        return friends;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<List<Friend>> _refreshLocalAppFriends()
    {
        return Observable
                .zip(_fetchFacebookFriendsNetwork(false), _fetchRegisteredContactsNetwork(false),
                        new Func2<Pair<List<Friend>, List<Friend>>, List<Friend>, List<Friend>>()
                        {
                            @Override
                            public List<Friend> call(Pair<List<Friend>, List<Friend>>
                                                             allFacebookFriends, List<Friend>
                                                             registeredContacts)
                            {
                                List<Friend> localFacebookFriends = allFacebookFriends.first;

                                Set<Friend> allFriends = new HashSet<Friend>();
                                allFriends.addAll(localFacebookFriends);
                                allFriends.addAll(registeredContacts);
                                return new ArrayList<>(allFriends);
                            }
                        })
                .map(new Func1<List<Friend>, List<Friend>>()
                {
                    @Override
                    public List<Friend> call(List<Friend> friends)
                    {
                        Collections.sort(friends, new FriendsComparator());
                        return friends;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    /* Feedback */
    public void shareFeedback(int type, String comment)
    {
        switch (type) {
            case 0:
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_FEEDBACK,
                        GoogleAnalyticsConstants.ACTION_BUG, comment);
                break;
            case 1:
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_FEEDBACK,
                        GoogleAnalyticsConstants.ACTION_NEW_FEATURE, comment);
                break;
            case 2:
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_FEEDBACK,
                        GoogleAnalyticsConstants.ACTION_OTHERS, comment);
                break;
        }

    }

    /* New User */
    public void markUserAsOld()
    {
        genericCache.put(GenericCacheKeys.IS_NEW_USER, false);
    }

    public void setIsNew(boolean isNew)
    {
        genericCache.put(GenericCacheKeys.IS_NEW_USER, isNew);
    }

    public Boolean isNewUser()
    {
        return Boolean.valueOf(genericCache.get(GenericCacheKeys.IS_NEW_USER));
    }

    public static String getProfilePicUrl(String id)
    {
        return AppConstants.BASE_URL_SERVER + "images/profile-pic/" + id + "?width=" + Dimensions
                .PROFILE_PIC_DEFAULT + "&height=" + Dimensions.PROFILE_PIC_DEFAULT;
    }

    public Observable<String> getCoverPicUrl()
    {
        return facebookService.getCoverPicUrl();
    }

    public void refreshFriendsCache()
    {
        _fetchLocalFacebookFriends()
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

                    }
                });
    }

    public void clearNewFriendsList()
    {
        CacheManager.getGenericCache().put(GenericCacheKeys.NEW_FRIENDS_LIST, new HashSet<>());
    }
}
