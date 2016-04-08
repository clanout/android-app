package com.clanout.app.ui.screens.launch.mvp.fb_login;

public interface FacebookLoginView
{
    void displayFacebookLoginButton();

    void displayFacebookLoginError();

    void displayFacebookPermissionsMessage();

    void proceedToSessionValidation();
}
