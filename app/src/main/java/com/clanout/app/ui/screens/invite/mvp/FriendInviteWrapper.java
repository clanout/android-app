package com.clanout.app.ui.screens.invite.mvp;

import com.clanout.app.model.Friend;

public class FriendInviteWrapper
{
    private Friend friend;
    private boolean isGoing;
    private boolean isAlreadyInvited;
    private boolean isSelected;

    public Friend getFriend()
    {
        return friend;
    }

    public void setFriend(Friend friend)
    {
        this.friend = friend;
    }

    public boolean isGoing()
    {
        return isGoing;
    }

    public void setGoing(boolean going)
    {
        isGoing = going;
    }

    public boolean isAlreadyInvited()
    {
        return isAlreadyInvited;
    }

    public void setAlreadyInvited(boolean alreadyInvited)
    {
        isAlreadyInvited = alreadyInvited;
    }

    public boolean isSelected()
    {
        return isSelected;
    }

    public void setSelected(boolean selected)
    {
        isSelected = selected;
    }
}
