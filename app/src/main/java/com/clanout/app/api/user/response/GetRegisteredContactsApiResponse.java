package com.clanout.app.api.user.response;

import com.clanout.app.model.Friend;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class GetRegisteredContactsApiResponse
{
    @SerializedName("registered_contacts")
    private List<Friend> registeredContacts;

    public List<Friend> getRegisteredContacts()
    {
        return registeredContacts;
    }
}
