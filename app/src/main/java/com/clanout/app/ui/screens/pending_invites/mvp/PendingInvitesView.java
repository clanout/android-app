package com.clanout.app.ui.screens.pending_invites.mvp;

import com.clanout.app.model.Event;

import java.util.List;

public interface PendingInvitesView
{
    void displayInvalidMobileNumberMessage();

    void showLoading();

    void hideLoading();

    void displayNoActivePendingInvitationsMessage();

    void displayActivePendingInvitation(List<Event> events);

    void displayExpiredEventsMessage(int expiredEventsCount);

    void navigateToHome();

    void navigateToDetails(String eventId);
}
