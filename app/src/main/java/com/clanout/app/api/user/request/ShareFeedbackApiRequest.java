package com.clanout.app.api.user.request;

import com.google.gson.annotations.SerializedName;

public class ShareFeedbackApiRequest
{
    @SerializedName("type")
    private int type;

    @SerializedName("comment")
    private String comment;

    public ShareFeedbackApiRequest(String comment, int type)
    {
        this.comment = comment;
        this.type = type;
    }
}
