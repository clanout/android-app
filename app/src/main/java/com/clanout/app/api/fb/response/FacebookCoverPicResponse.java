package com.clanout.app.api.fb.response;

import com.google.gson.annotations.SerializedName;

/**
 * Created by harsh on 02/10/15.
 */
public class FacebookCoverPicResponse
{

    @SerializedName("cover")
    private Cover cover;

    public Cover getCover() {
        return cover;
    }

    public class Cover {

        @SerializedName("source")
        private String source;

        public String getSource() {
            return source;
        }
    }
}
