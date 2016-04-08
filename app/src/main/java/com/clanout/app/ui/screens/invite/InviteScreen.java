package com.clanout.app.ui.screens.invite;

import com.clanout.app.ui._core.PermissionHandler;

public interface InviteScreen
{
    void setReadContactsPermissionListener(PermissionHandler.Listener listener);

    void navigateToAppSettings();

    void navigateToDetailsScreen();
}
