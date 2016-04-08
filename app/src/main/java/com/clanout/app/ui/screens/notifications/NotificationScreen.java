package com.clanout.app.ui.screens.notifications;

public interface NotificationScreen
{
    void navigateToHomeScreen();

    void navigateToDetailsScreen(String eventId);

    void navigateToChatScreen(String eventId);

    void navigateToFriendsScreen();

    void navigateToCreateScreen();
}
