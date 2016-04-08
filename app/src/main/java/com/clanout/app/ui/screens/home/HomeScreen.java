package com.clanout.app.ui.screens.home;

import com.clanout.app.model.EventCategory;

public interface HomeScreen
{
    void navigateToCreateDetailsScreen(EventCategory category);

    void navigateToDetailsScreen(String eventId);
}
