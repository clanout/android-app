package com.clanout.app.ui.screens.details.mvp;

import com.clanout.app.model.Event;

import java.util.List;

public interface EventDetailsContainerView
{
    void initView(List<Event> events, int activePosition);

    void handleError();
}
