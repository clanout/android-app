package com.clanout.app.root;

import android.app.Application;
import android.content.Context;
import android.location.LocationManager;
import android.support.multidex.MultiDex;

import com.clanout.BuildConfig;
import com.clanout.app.cache._core.DatabaseManager;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.communication.Communicator;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.service.ChatService;
import com.clanout.app.service.EventService;
import com.clanout.app.service.GcmService;
import com.clanout.app.service.GoogleService;
import com.clanout.app.service.LocationService;
import com.clanout.app.service.NotificationService;
import com.clanout.app.service.PhonebookService;
import com.clanout.app.service.UserService;
import com.facebook.FacebookSdk;
import com.facebook.stetho.Stetho;
import com.google.android.gms.analytics.GoogleAnalytics;
import com.google.android.gms.analytics.Tracker;
import com.squareup.otto.Bus;
import com.squareup.otto.ThreadEnforcer;

import timber.log.Timber;

public class ClanOut extends Application
{
    private static ClanOut instance;
    private static Tracker tracker;

    public static ClanOut getClanOutContext()
    {
        return instance;
    }

    synchronized public static Tracker getAnalyticsTracker()
    {
        if (tracker == null)
        {
            tracker = GoogleAnalytics.getInstance(instance)
                                     .newTracker(AppConstants.GOOGLE_ANALYTICS_TRACKING_KEY);
            tracker.enableExceptionReporting(true);

        }
        return tracker;
    }

    @Override
    protected void attachBaseContext(Context base)
    {
        super.attachBaseContext(base);
        MultiDex.install(this);
    }

    @Override
    public void onCreate()
    {
        super.onCreate();

        /* Static reference */
        instance = this;

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,
                GoogleAnalyticsConstants.ACTION_APP_LAUNCH, null);
        /* Analytics */

        /* Facebook SDK */
        FacebookSdk.sdkInitialize(this);

        /* Stetho (debugging) */
        Stetho.initializeWithDefaults(this);

        /* Logging */
        initLogging();

        /* Communicator (Event Bus) */
        initCommunicator();

        /* SQLite */
        initDb();

        /* Services */
        initServices();
    }

    private void initLogging()
    {
        if (BuildConfig.DEBUG)
        {
            Timber.plant(new Timber.DebugTree());
        }
    }

    private void initCommunicator()
    {
        Bus bus = new Bus(ThreadEnforcer.ANY);
        bus.register(this);
        Communicator.init(bus);
    }

    private void initDb()
    {
        DatabaseManager.init(this);
    }

    private void initServices()
    {
        /* Gcm Service */
        GcmService gcmService = GcmService.getInstance();

        /* Location Service */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationService
                .init(getApplicationContext(), locationManager, GoogleService.getInstance());
        LocationService locationService = LocationService.getInstance();

        /* Phonebook Service */
        PhonebookService.init(getApplicationContext());
        PhonebookService phonebookService = PhonebookService.getInstance();

        /* User Service */
        UserService.init(locationService, phonebookService);
        UserService userService = UserService.getInstance();

        /* Notification Service */
        NotificationService notificationService = NotificationService.getInstance();

        /* Event Service */
        EventService.init(gcmService, locationService, userService, notificationService);
        EventService eventService = EventService.getInstance();

        /* Chat Service */
        if (userService.getSessionUser() != null)
        {
            ChatService.init(userService, eventService);
        }

        // TODO
        /* WhatsApp Service */
//        WhatsappService_.init(userService);
    }
}
