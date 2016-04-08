package com.clanout.app.ui.screens.launch.mvp.bootstrap;

public interface BootstrapView
{
    void showLoading();

    void handleLocationPermissions();

    void displayLocationServiceUnavailableMessage();

    void displayError();

    void proceed();

    void navigateToPendingInvitesScreen();

    void displayAppNotAvailableScreen();
}
