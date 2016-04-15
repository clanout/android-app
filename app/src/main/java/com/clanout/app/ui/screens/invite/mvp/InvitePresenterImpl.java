package com.clanout.app.ui.screens.invite.mvp;

import android.util.Log;
import android.util.Pair;

import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Attendee;
import com.clanout.app.model.Event;
import com.clanout.app.model.Friend;
import com.clanout.app.model.PhonebookContact;
import com.clanout.app.service.EventService;
import com.clanout.app.service.LocationService;
import com.clanout.app.service.PhonebookService;
import com.clanout.app.service.UserService;

import java.util.ArrayList;
import java.util.List;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func2;
import rx.functions.Func3;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class InvitePresenterImpl implements InvitePresenter
{
    private InviteView view;
    private UserService userService;
    private EventService eventService;
    private PhonebookService phonebookService;
    private LocationService locationService;

    private String eventId;
    private boolean isReadContactsPermissionGranted;

    private List<String> invitedFriends;
    private List<String> invitedContacts;

    private CompositeSubscription subscriptions;

    public InvitePresenterImpl(UserService userService, EventService eventService,
                               PhonebookService phonebookService, LocationService locationService, String eventId)
    {
        this.userService = userService;
        this.eventService = eventService;
        this.phonebookService = phonebookService;
        this.locationService = locationService;
        this.eventId = eventId;

        invitedFriends = new ArrayList<>();
        invitedContacts = new ArrayList<>();

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(InviteView view)
    {
        this.view = view;

        isReadContactsPermissionGranted = phonebookService.isReadContactsPermissionGranted();
        if (!isReadContactsPermissionGranted)
        {
            this.view.handleReadContactsPermission();
        }

        if (userService.getSessionUser().getMobileNumber() == null)
        {
            this.view.showAddPhoneOption();
        }
        else
        {
            this.view.hideAddPhoneOption();
        }

        init();
    }

    @Override
    public void detachView()
    {
        subscriptions.clear();
        view = null;
    }

    @Override
    public void retry()
    {
        init();
    }

    @Override
    public void select(FriendInviteWrapper friend, boolean isInvited)
    {
        friend.setSelected(isInvited);

        String friendId = friend.getFriend().getId();
        if (isInvited && !invitedFriends.contains(friendId))
        {
            invitedFriends.add(friendId);
        }
        else if (!isInvited && invitedFriends.contains(friendId))
        {
            invitedFriends.remove(friendId);
        }

        int inviteCount = invitedFriends.size() + invitedContacts.size();
        if (inviteCount == 0)
        {
            view.hideInviteButton();
        }
        else
        {
            view.showInviteButton(inviteCount);
        }
    }

    @Override
    public void select(PhonebookContactInviteWrapper contact, boolean isInvited)
    {
        contact.setSelected(isInvited);

        String mobileNumber = contact.getPhonebookContact().getPhone();
        if (isInvited && !invitedContacts.contains(mobileNumber))
        {
            invitedContacts.add(mobileNumber);
        }
        else if (!isInvited && invitedContacts.contains(mobileNumber))
        {
            invitedContacts.remove(mobileNumber);
        }

        int inviteCount = invitedFriends.size() + invitedContacts.size();
        if (inviteCount == 0)
        {
            view.hideInviteButton();
        }
        else
        {
            view.showInviteButton(inviteCount);
        }
    }

    @Override
    public void sendInvitations()
    {
        Log.d("APP", "send invitaions");

        if (view != null)
        {
            view.navigateToDetailsScreen();
        }

        if (!invitedFriends.isEmpty() || !invitedContacts.isEmpty())
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                    GoogleAnalyticsConstants.ACTION_PEOPLE_INVITED, GoogleAnalyticsConstants
                            .LABEL_FB_FRIENDS, invitedFriends.size());

            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                    GoogleAnalyticsConstants.ACTION_PEOPLE_INVITED, GoogleAnalyticsConstants
                            .LABEL_PB_CONTACTS, invitedContacts.size());
            /* Analytics */

            eventService._inviteFriends(eventId, invitedFriends, invitedContacts);
        }
    }

    @Override
    public void refresh()
    {
        view.showRefreshing();

        Subscription subscription
                = getRefreshObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        view.hideRefreshing();
                    }

                    @Override
                    public void onNext(Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> inviteList)
                    {
                        List<FriendInviteWrapper> friends = inviteList.first;
                        List<PhonebookContactInviteWrapper> phonebookContacts = inviteList.second;
                        view.displayInviteList(locationService.getCurrentLocation().getName(),
                                friends, phonebookContacts);
                        view.hideRefreshing();

                        invitedFriends = new ArrayList<String>();
                        invitedContacts = new ArrayList<String>();
                        view.hideInviteButton();
                    }
                });

        subscriptions.add(subscription);
    }

    /* Helper Methods */
    private void init()
    {
        view.hideInviteButton();
        view.showLoading();

        Subscription subscription
                = getObservable()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                {
                    @Override
                    public void onCompleted()
                    {

                    }

                    @Override
                    public void onError(Throwable e)
                    {
                        e.printStackTrace();
                        view.displayError();
                    }

                    @Override
                    public void onNext(Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> inviteList)
                    {
                        List<FriendInviteWrapper> friends = inviteList.first;
                        List<PhonebookContactInviteWrapper> phonebookContacts = inviteList.second;
                        view.displayInviteList(locationService.getCurrentLocation().getName(),
                                friends, phonebookContacts);
                    }
                });

        subscriptions.add(subscription);
    }

    private Observable<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>> getObservable()
    {
        if (isReadContactsPermissionGranted)
        {
            return Observable
                    .zip(userService._fetchLocalAppFriends(),
                            eventService._fetchEvent(eventId),
                            phonebookService.fetchAllContacts(),
                            new Func3<List<Friend>, Event, List<PhonebookContact>, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> appFriends, Event event, List<PhonebookContact> phonebookContacts)
                                {
                                    List<FriendInviteWrapper> friendInviteWrappers = prepareFriendInviteWrappers(event, appFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = preparePhonebookContactInviteWrappers(phonebookContacts);
                                    return new Pair<>(friendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
        else
        {
            return Observable
                    .zip(userService._fetchLocalFacebookFriends(),
                            eventService._fetchEvent(eventId),
                            new Func2<List<Friend>, Event, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> facebookFriends, Event event)
                                {
                                    List<FriendInviteWrapper> facebookFriendInviteWrappers = prepareFriendInviteWrappers(event, facebookFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();
                                    return new Pair<>(facebookFriendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
    }

    private Observable<Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>> getRefreshObservable()
    {
        if (isReadContactsPermissionGranted)
        {
            return Observable
                    .zip(userService._refreshLocalAppFriends(),
                            eventService._fetchEvent(eventId),
                            phonebookService.refreshAllContacts(),
                            new Func3<List<Friend>, Event, List<PhonebookContact>, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(List<Friend> appFriends, Event event, List<PhonebookContact> phonebookContacts)
                                {
                                    List<FriendInviteWrapper> friendInviteWrappers = prepareFriendInviteWrappers(event, appFriends);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = preparePhonebookContactInviteWrappers(phonebookContacts);
                                    return new Pair<>(friendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
        else
        {
            return Observable
                    .zip(userService._fetchFacebookFriendsNetwork(false),
                            eventService._fetchEvent(eventId),
                            new Func2<Pair<List<Friend>, List<Friend>>, Event, Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>>>()
                            {
                                @Override
                                public Pair<List<FriendInviteWrapper>, List<PhonebookContactInviteWrapper>> call(Pair<List<Friend>, List<Friend>> allFacebookFriends, Event event)
                                {
                                    List<FriendInviteWrapper> facebookFriendInviteWrappers = prepareFriendInviteWrappers(event, allFacebookFriends.first);
                                    List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();
                                    return new Pair<>(facebookFriendInviteWrappers, phonebookContactInviteWrappers);
                                }
                            });
        }
    }

    private List<FriendInviteWrapper> prepareFriendInviteWrappers(Event event, List<Friend> friends)
    {
        List<FriendInviteWrapper> friendInviteWrappers = new ArrayList<>();

        List<Attendee> attendees = event.getAttendees();
        List<String> invitees = event.getInvitee();

        for (Friend friend : friends)
        {
            FriendInviteWrapper friendInviteWrapper = new FriendInviteWrapper();
            friendInviteWrapper.setFriend(friend);

            Attendee friendAttendee = new Attendee();
            friendAttendee.setId(friend.getId());
            if (attendees.contains(friendAttendee))
            {
                friendInviteWrapper.setGoing(true);
            }

            if (invitees.contains(friend.getId()))
            {
                friendInviteWrapper.setAlreadyInvited(true);
            }

            friendInviteWrappers.add(friendInviteWrapper);
        }

        return friendInviteWrappers;
    }

    private List<PhonebookContactInviteWrapper> preparePhonebookContactInviteWrappers(List<PhonebookContact> contacts)
    {
        List<PhonebookContactInviteWrapper> phonebookContactInviteWrappers = new ArrayList<>();

        for (PhonebookContact contact : contacts)
        {
            PhonebookContactInviteWrapper wrapper = new PhonebookContactInviteWrapper();
            wrapper.setPhonebookContact(contact);

            phonebookContactInviteWrappers.add(wrapper);
        }

        return phonebookContactInviteWrappers;
    }
}
