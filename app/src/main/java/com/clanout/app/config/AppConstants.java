package com.clanout.app.config;

public final class AppConstants
{
    /* Server URLs */
    public static final String BASE_URL_SERVER = "http://api.clanout.com/v0.9/";
    public static final String BASE_URL_GOOGLE_PLACES_API = "https://maps.googleapis.com/maps/api/place/";
    public static final String BASE_URL_FACEBOOK_API = "https://graph.facebook.com/v2.4/";

    /* API Keys */
    public static final String GOOGLE_API_KEY = "AIzaSyCoBgOqvl0FoFiAboM2fzYPtIWxUN5D2Po";
    public static final String GOOGLE_ANALYTICS_TRACKING_KEY = "UA-69780078-2";
//    public static final String GCM_SENDER_ID = "1014674558116";
    public static final String GCM_SENDER_ID = "657261759195";

    /* App Link */
    public static final String APP_LINK = "https://goo.gl/Jc33PQ";

    /* Chat */
    public static final String CHAT_SERVICE_NAME = "chat.clanout.com";
    public static final String CHAT_SERVICE_HOST = "chat.clanout.com";
    public static final int CHAT_SERVICE_PORT = 5222;
    public static final String CHAT_POSTFIX = "@conference.chat.clanout.com";
    public static final String CHAT_ADMIN_ID = "clanout";

    /* Others */
    public static final String DEFAULT_COUNTRY_CODE = "91";
    public static final int TITLE_LENGTH_LIMIT = 30;
    public static final int EXPIRY_DAYS_EVENT_SUGGESTIONS = 7;

    private AppConstants()
    {
    }
}
