package com.clanout.app.ui.screens.launch.mvp.fb_login;


import com.clanout.app.service.AuthService;
import com.clanout.app.service.FacebookService;

import timber.log.Timber;

public class FacebookLoginPresenterImpl implements FacebookLoginPresenter
{
    private FacebookLoginView view;
    private AuthService authService;
    private FacebookService facebookService;

    public FacebookLoginPresenterImpl(AuthService authService, FacebookService facebookService)
    {
        this.authService = authService;
        this.facebookService = facebookService;
    }

    @Override
    public void attachView(FacebookLoginView view)
    {
        this.view = view;

        if (facebookService.isAccessTokenValid())
        {
            Timber.d("Before Bootstrap View " + System.currentTimeMillis());
            this.view.proceedToSessionValidation();
        }
        else
        {
            authService.logout();
            Timber.d("Before FB View " + System.currentTimeMillis());
            this.view.displayFacebookLoginButton();
        }
    }

    @Override
    public void detachView()
    {
        view = null;
    }

    @Override
    public void onFacebookLoginSuccess()
    {
        if (view == null)
        {
            return;
        }

        if (facebookService.getAccessToken() == null)
        {
            view.displayFacebookLoginError();
        }
        else
        {
            if (facebookService.getDeclinedPermissions().size() > 0)
            {
                view.displayFacebookPermissionsMessage();
            }
            else
            {
                view.proceedToSessionValidation();
            }
        }
    }

    @Override
    public void onFacebookLoginCancel()
    {
        if (view == null)
        {
            return;
        }

        view.displayFacebookLoginButton();
    }

    @Override
    public void onFacebookLoginError()
    {
        if (view == null)
        {
            return;
        }

        view.displayFacebookLoginError();
    }
}
