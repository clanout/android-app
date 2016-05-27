package com.clanout.app.ui.screens.invite.mvp;

import java.util.List;

public interface InviteView
{
    void showAddPhoneOption();

    void hideAddPhoneOption();

    void handleReadContactsPermission();

    void showLoading();

    void displayError();

    void displayInviteList(String locationZone, List<FriendInviteWrapper> friends,
                           List<PhonebookContactInviteWrapper> phonebookContacts);

    void displayNoFriendOrContactMessage();

    void showInviteButton(int inviteCount);

    void hideInviteButton();

    void navigateToDetailsScreen();

    void showRefreshing();

    void hideRefreshing();

    void showPlanNotAvailableMessage();

    void showPlanExpiredMessage();
}
