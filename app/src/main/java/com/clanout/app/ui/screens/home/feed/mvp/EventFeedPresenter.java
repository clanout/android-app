package com.clanout.app.ui.screens.home.feed.mvp;

import com.clanout.app.model.Event;

public interface EventFeedPresenter
{
    void attachView(EventFeedView view);

    void detachView();

    void refreshEvents();

    void selectEvent(Event event);
}
