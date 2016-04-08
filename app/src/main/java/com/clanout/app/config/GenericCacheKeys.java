package com.clanout.app.config;

public final class GenericCacheKeys
{
    /* Session User */
    public static final String SESSION_USER = "session_user";
    public static final String REFRESH_TOKEN = "refresh_token";
    public static final String ACCESS_TOKEN = "access_token";
    public static final String IS_NEW_USER = "is_new_user";

    /* GCM Token */
    public static final String GCM_TOKEN = "gcm_token";

    /* Create Event Suggestion */
    public static final String CREATE_EVENT_SUGGESTIONS = "create_event_suggestions";
    public static final String CREATE_EVENT_SUGGESTIONS_UPDATE_TIMESTAMP = "create_event_suggestions_update_timestamp";

    /* Phonebook Contacts */
    public static final String PHONEBOOK_CONTACTS = "phonebook_contacts";

    /* Friends */
    public static final String FRIENDS_CACHE_CLEARED_TIMESTAMP = "friends_cache_cleared_timestamp";

    /* Not Going Events */
    public static final String NOT_GOING_EVENT_LIST = "not_going_event_list";

    /* New Friends */
    public static final String NEW_FRIENDS_LIST = "new_friends_list";

    private GenericCacheKeys()
    {
    }
}
