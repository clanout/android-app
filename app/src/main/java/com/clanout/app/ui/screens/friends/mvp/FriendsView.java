package com.clanout.app.ui.screens.friends.mvp;

import com.clanout.app.model.Friend;

import java.util.List;

public interface FriendsView
{
    void showLoading();

    void displayNoFriendsMessage();

    void displayError();

    void displayFriends(List<Friend> localFriends, List<Friend> otherFriends, String locationZone);

    interface FriendListItem
    {
        void render(Friend friend);
    }
}
