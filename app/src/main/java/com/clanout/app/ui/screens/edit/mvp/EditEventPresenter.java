package com.clanout.app.ui.screens.edit.mvp;

import com.clanout.app.model.Location;

import org.joda.time.DateTime;

public interface EditEventPresenter
{
    void attachView(EditEventView view);

    void detachView();

    void edit(DateTime starTime, Location location, String description);
}
