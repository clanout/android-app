package com.clanout.app.common.analytics;

import com.clanout.app.root.ClanOut;
import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * Created by Aditya on 15-09-2015.
 */
public class AnalyticsHelper
{
    private static Tracker googleAnalyticsTracker = ClanOut.getAnalyticsTracker();

    public AnalyticsHelper()
    {
    }

    public static void sendScreenNames(String screenName)
    {
        googleAnalyticsTracker.setScreenName(screenName);
        googleAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder().build());
    }

    public static void sendEvents(String category, String action, String label)
    {
        googleAnalyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .build());
    }

    public static void sendEvents(String category, String action, String label, long value)
    {
        googleAnalyticsTracker.send(new HitBuilders.EventBuilder()
                .setCategory(category)
                .setAction(action)
                .setLabel(label)
                .setValue(value)
                .build());
    }

    public static void sendCaughtExceptions(String exceptionMethod, String exceptionLocation, boolean isFatal)
    {
        googleAnalyticsTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(exceptionMethod + ":" + exceptionLocation)
                .setFatal(isFatal)
                .build());
    }

    public static void sendCaughtExceptions(String exceptionMethod, boolean isFatal)
    {
        googleAnalyticsTracker.send(new HitBuilders.ExceptionBuilder()
                .setDescription(exceptionMethod)
                .setFatal(isFatal)
                .build());
    }

    public static void sendCustomDimension(int index, String dimension)
    {
        googleAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder()
                        .setCustomDimension(index, dimension)
                        .build()
        );
    }

    public static void sendCustomMetric(int index, String metric)
    {
        googleAnalyticsTracker.send(new HitBuilders.ScreenViewBuilder()
                        .setCustomDimension(index, metric)
                        .build()
        );
    }
}