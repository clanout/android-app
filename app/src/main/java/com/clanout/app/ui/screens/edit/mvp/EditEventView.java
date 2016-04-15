package com.clanout.app.ui.screens.edit.mvp;

import com.clanout.app.model.Event;

public interface EditEventView
{
    void init(Event event);

    void showLoading();

    void displayError();

    void navigateToDetailsScreen(String eventId);
}
