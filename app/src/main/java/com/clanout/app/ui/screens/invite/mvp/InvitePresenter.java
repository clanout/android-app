package com.clanout.app.ui.screens.invite.mvp;

public interface InvitePresenter
{
    void attachView(InviteView view);

    void detachView();

    void retry();

    void select(FriendInviteWrapper friend, boolean isInvited);

    void select(PhonebookContactInviteWrapper contact, boolean isInvited);

    void sendInvitations();

    void refresh();
}
