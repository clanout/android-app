package com.clanout.app.api.user.response;

import com.clanout.app.model.User;
import com.google.gson.annotations.SerializedName;

import retrofit.http.POST;

/**
 * Created by harsh on 07/04/16.
 */
public class GetUserDetailsApiResponse
{
    @SerializedName("user")
    private User user;

    public User getUser()
    {
        return user;
    }
}
