package com.clanout.app.ui.screens.details.mvp;

import com.clanout.app.model.Attendee;
import com.clanout.app.model.AttendeeWrapper;
import com.clanout.app.model.Event;
import com.clanout.app.model.User;

import java.util.List;

public interface EventDetailsView
{
    void init(User sessionUser, Event event, boolean isLastMinute);

    void displayAttendees(List<AttendeeWrapper> attendeeWrappers);

    void resetEvent(Event event);

    void showLoading();

    void hideLoading();

    void setEditVisibility(boolean isVisible);

    void setDeleteVisibility(boolean isVisible);

    void displayYayActions();

    void displayNayActions(boolean isInvited);

    void navigateToInvite(String eventId);

    void navigateToChat(String eventId);

    void navigateToEdit(Event event);

    void navigateToHome();

    void displayChatMarker();

    void hideChatMarker();

    void showPlanNotAvailableMessage();

    void showPlanExpiredMessage();
}
