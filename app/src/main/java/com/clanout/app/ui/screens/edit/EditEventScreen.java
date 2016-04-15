package com.clanout.app.ui.screens.edit;

public interface EditEventScreen
{
    void setLocationSelectionListener(LocationSelectionListener listener);

    void navigateToLocationSelectionScreen();

    void navigateToHomeScreen();

    void navigateToDetailsScreen(String eventId);
}
