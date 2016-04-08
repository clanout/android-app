package com.clanout.app.api.user.response;

import com.clanout.app.model.Friend;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class GetFacebookFriendsApiResponse
{
    @SerializedName("friends")
    private List<Friend> friends;

    public List<Friend> getFriends()
    {
        if (friends == null)
        {
            return new ArrayList<>();
        }
        else
        {
            return friends;
        }
    }
}
