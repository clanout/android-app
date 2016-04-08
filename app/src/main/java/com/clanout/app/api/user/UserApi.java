package com.clanout.app.api.user;

import com.clanout.app.api.user.request.BlockFriendsApiRequest;
import com.clanout.app.api.user.request.GetFacebookFriendsApiRequest;
import com.clanout.app.api.user.request.GetRegisteredContactsApiRequest;
import com.clanout.app.api.user.request.GetUserDetailsApiRequest;
import com.clanout.app.api.user.request.ShareFeedbackApiRequest;
import com.clanout.app.api.user.request.UpdateFacebookFriendsApiRequest;
import com.clanout.app.api.user.request.UpdateMobileAPiRequest;
import com.clanout.app.api.user.request.UpdateUserLocationApiRequest;
import com.clanout.app.api.user.response.GetFacebookFriendsApiResponse;
import com.clanout.app.api.user.response.GetRegisteredContactsApiResponse;
import com.clanout.app.api.user.response.GetUserDetailsApiResponse;
import com.clanout.app.api.user.response.UpdateUserLocationApiResponse;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface UserApi
{
    @POST("/me/location")
    Observable<UpdateUserLocationApiResponse> updateUserLocation(@Body
                                                                 UpdateUserLocationApiRequest
                                                                         request);

    @POST("/me/mobile")
    Observable<Response> updateMobile(@Body UpdateMobileAPiRequest request);

    @POST("/me/friends")
    Observable<GetFacebookFriendsApiResponse> getFacebookFriends(@Body
                                                                 GetFacebookFriendsApiRequest
                                                                         request);

    @POST("/me/registered-contacts")
    Observable<GetRegisteredContactsApiResponse> getRegisteredContacts(@Body GetRegisteredContactsApiRequest request);

    @POST("/me/block")
    Observable<Response> blockFriends(@Body BlockFriendsApiRequest request);

    @POST("/me")
    Observable<GetUserDetailsApiResponse> getDetails(@Body GetUserDetailsApiRequest getUserDetailsApiRequest);
}
