package com.clanout.app.ui.screens.home.feed.mvp;

import com.clanout.app.model.Event;
import com.clanout.app.model.Friend;

import java.util.List;

public interface EventFeedView
{
    void showLoading();

    void showEvents(List<Event> events, List<Friend> friends);

    void showNoEventsMessage();

    void showError();

    void gotoDetailsView(String eventId);
}
