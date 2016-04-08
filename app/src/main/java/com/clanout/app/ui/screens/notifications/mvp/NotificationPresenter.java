package com.clanout.app.ui.screens.notifications.mvp;

import com.clanout.app.model.NotificationWrapper;

public interface NotificationPresenter
{
    void attachView(NotificationView view);

    void detachView();

    void onNotificationSelected(NotificationWrapper notification);

    void onNotificationDeleted(int position);

    void deleteAll();
}
