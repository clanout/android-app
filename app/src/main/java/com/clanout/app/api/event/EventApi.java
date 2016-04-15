package com.clanout.app.api.event;

import com.clanout.app.api.event.request.CreateEventApiRequest;
import com.clanout.app.api.event.request.DeleteEventApiRequest;
import com.clanout.app.api.event.request.EditEventApiRequest;
import com.clanout.app.api.event.request.EventsApiRequest;
import com.clanout.app.api.event.request.FetchEventApiRequest;
import com.clanout.app.api.event.request.FetchPendingInvitesApiRequest;
import com.clanout.app.api.event.request.GetCreateEventSuggestionsApiRequest;
import com.clanout.app.api.event.request.InviteUsersApiRequest;
import com.clanout.app.api.event.request.RsvpUpdateApiRequest;
import com.clanout.app.api.event.request.SendChatNotificationApiRequest;
import com.clanout.app.api.event.request.SendInvitaionResponseApiRequest;
import com.clanout.app.api.event.request.UpdateStatusApiRequest;
import com.clanout.app.api.event.response.CreateEventApiResponse;
import com.clanout.app.api.event.response.EventsApiResponse;
import com.clanout.app.api.event.response.FetchEventApiResponse;
import com.clanout.app.api.event.response.FetchPendingInvitesApiResponse;
import com.clanout.app.api.event.response.GetCreateEventSuggestionsApiResponse;

import retrofit.client.Response;
import retrofit.http.Body;
import retrofit.http.POST;
import rx.Observable;

public interface EventApi
{
    @POST("/plan")
    Observable<FetchEventApiResponse> fetchEvent(@Body FetchEventApiRequest request);

    @POST("/plan/feed")
    Observable<EventsApiResponse> getEvents(@Body EventsApiRequest request);

    @POST("/plan/rsvp")
    Observable<Response> updateRsvp(@Body RsvpUpdateApiRequest request);

    @POST("/plan/create")
    Observable<CreateEventApiResponse> createEvent(@Body CreateEventApiRequest request);

    @POST("/plan/edit")
    Observable<Response> editEvent(@Body EditEventApiRequest request);

    @POST("/plan/delete")
    Observable<Response> deleteEvent(@Body DeleteEventApiRequest request);

    @POST("/plan/invite")
    Observable<Response> inviteFriends(@Body InviteUsersApiRequest request);

    @POST("/plan/chat-update")
    Observable<Response> sendChatNotification(@Body SendChatNotificationApiRequest request);

    @POST("/event/invitation-response")
    Observable<Response> sendInvitationResponse(@Body SendInvitaionResponseApiRequest request);

    @POST("/plan/status")
    Observable<Response> updateStatus(@Body UpdateStatusApiRequest request);

    @POST("/plan/create-suggestions")
    Observable<GetCreateEventSuggestionsApiResponse> getCreateEventSuggestion(@Body GetCreateEventSuggestionsApiRequest request);

    @POST("/plan/pending-invitations")
    Observable<FetchPendingInvitesApiResponse> fetchPendingInvites(@Body FetchPendingInvitesApiRequest request);
}
