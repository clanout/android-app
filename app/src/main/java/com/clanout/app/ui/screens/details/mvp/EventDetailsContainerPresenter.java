package com.clanout.app.ui.screens.details.mvp;

public interface EventDetailsContainerPresenter
{
    void attachView(EventDetailsContainerView view);

    void detachView();

    void setActivePosition(int activePosition);
}
