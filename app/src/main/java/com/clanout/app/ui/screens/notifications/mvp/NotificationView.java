package com.clanout.app.ui.screens.notifications.mvp;

import com.clanout.app.model.NotificationWrapper;

import java.util.List;

public interface NotificationView
{
    void showLoading();

    void displayNotifications(List<NotificationWrapper> notifications);

    void displayNoNotificationsMessage();

    void displayEventRemovedMessage(NotificationWrapper notification);

    void navigateToDetailsScreen(String eventId);

    void navigateToChatScreen(String eventId);

    void navigateToFriendsScreen();
}
