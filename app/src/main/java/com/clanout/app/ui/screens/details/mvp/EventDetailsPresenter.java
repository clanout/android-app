package com.clanout.app.ui.screens.details.mvp;

public interface EventDetailsPresenter
{
    void attachView(EventDetailsView view);

    void detachView();

    String getTitle();

    void toggleRsvp();

    void setStatus(String status);

    void sendInvitationResponse(String invitationResponse);

    void invite();

    void chat();

    void edit();

    void delete();
}
