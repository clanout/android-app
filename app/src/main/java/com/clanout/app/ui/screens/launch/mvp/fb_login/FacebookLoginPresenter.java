package com.clanout.app.ui.screens.launch.mvp.fb_login;

public interface FacebookLoginPresenter
{
    void attachView(FacebookLoginView view);

    void detachView();

    void onFacebookLoginSuccess();

    void onFacebookLoginCancel();

    void onFacebookLoginError();
}
