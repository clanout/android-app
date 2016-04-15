package com.clanout.app.ui.screens.create.mvp;

public interface CreateEventView
{
    void showLoading();

    void displayEmptyTitleError();

    void displayInvalidTimeError();

    void displayError();

    void navigateToInviteScreen(String eventId);
}
