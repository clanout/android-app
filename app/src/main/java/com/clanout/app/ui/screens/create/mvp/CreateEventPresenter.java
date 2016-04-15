package com.clanout.app.ui.screens.create.mvp;

import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Location;

import org.joda.time.DateTime;

public interface CreateEventPresenter
{
    void attachView(CreateEventView view);

    void detachView();

    void create(String title, Event.Type type, EventCategory category,
                String description, DateTime startTime, Location location);
}
