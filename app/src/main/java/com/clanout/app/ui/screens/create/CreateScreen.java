package com.clanout.app.ui.screens.create;

public interface CreateScreen
{
    void setLocationSelectionListener(LocationSelectionListener listener);

    void navigateToLocationSelectionScreen();

    void navigateToInviteScreen(String eventId);

    void navigateToDetailsScreen(String eventId);
}
