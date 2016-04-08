package com.clanout.app.api.user.request;

import com.clanout.app.api.core.GsonProvider;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by Aditya on 27-08-2015.
 */
public class UpdateFacebookFriendsApiRequest
{
    @SerializedName("friend_list")
    private String friendIdList;

    public UpdateFacebookFriendsApiRequest(List<String> friendIdList)
    {
        this.friendIdList = GsonProvider.getGson().toJson(friendIdList);
    }
}
