package com.clanout.app.ui.screens.home.create_suggestion.mvp;

import com.clanout.app.model.EventCategory;

public interface CreateSuggestionPresenter
{
    void attachView(CreateSuggestionView view);

    void detachView();

    void select(EventCategory category);
}
