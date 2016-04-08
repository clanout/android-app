package com.clanout.app.api.event.response;

import com.clanout.app.model.CreateEventSuggestion;
import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by harsh on 19/11/15.
 */
public class GetCreateEventSuggestionsApiResponse
{

    @SerializedName("create_suggestions")
    private List<CreateEventSuggestion> eventSuggestions;

    public GetCreateEventSuggestionsApiResponse(List<CreateEventSuggestion> eventSuggestions) {
        this.eventSuggestions = eventSuggestions;
    }

    public List<CreateEventSuggestion> getEventSuggestions() {
        return eventSuggestions;
    }
}
