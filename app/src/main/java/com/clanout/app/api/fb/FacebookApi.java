package com.clanout.app.api.fb;

import com.clanout.app.api.fb.response.FacebookCoverPicResponse;

import retrofit.http.GET;
import rx.Observable;

public interface FacebookApi
{
    @GET("/me?fields=cover")
    Observable<FacebookCoverPicResponse> getCoverPic();
}
