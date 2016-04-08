package com.clanout.app.ui.screens.home.create_suggestion.mvp;

import com.clanout.app.model.CreateEventSuggestion;
import com.clanout.app.model.EventCategory;

import java.util.List;

public interface CreateSuggestionView
{
    void init(List<CreateEventSuggestion> suggestions);

    void navigateToCreate(EventCategory category);
}
