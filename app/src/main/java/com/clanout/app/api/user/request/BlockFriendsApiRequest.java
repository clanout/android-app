package com.clanout.app.api.user.request;

import com.clanout.app.api.core.GsonProvider;
import com.google.gson.annotations.SerializedName;

import java.util.List;

public class BlockFriendsApiRequest
{
    @SerializedName("to_block")
    private List<String> blockedUsers;

    @SerializedName("to_unblock")
    private List<String> unblockedUsers;

    public BlockFriendsApiRequest(List<String> blockedUsers, List<String> unblockedUsers)
    {
        this.blockedUsers = blockedUsers;
        this.unblockedUsers = unblockedUsers;
    }
}
