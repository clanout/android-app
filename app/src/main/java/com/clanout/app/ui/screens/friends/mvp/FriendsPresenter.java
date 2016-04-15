package com.clanout.app.ui.screens.friends.mvp;

import com.clanout.app.model.Friend;

public interface FriendsPresenter
{
    void attachView(FriendsView view);

    void detachView();

    void onBlockToggled(Friend friend, FriendsView.FriendListItem friendListItem);
}
