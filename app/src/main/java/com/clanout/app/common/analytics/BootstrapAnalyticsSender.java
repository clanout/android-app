package com.clanout.app.common.analytics;

/**
 * Created by Zuko on 4/2/16.
 */
public class BootstrapAnalyticsSender
{
    private static BootstrapAnalyticsSender instance;

    public static BootstrapAnalyticsSender getInstance()
    {
        if (instance == null)
        {
            instance = new BootstrapAnalyticsSender();
        }
        return instance;
    }

    private boolean isAnalyticsSent;

    private BootstrapAnalyticsSender()
    {}

    public void send()
    {
        if(!isAnalyticsSent)
        {
            // TODO: add events

            //User Zone:
            //AnalyticsHelper.sendCustomDimension(1, userZone);

            //local Friends Count:
            //AnalyticsHelper.sendCustomDimension(2, localFriendCount);

            //No. of plans in feed:
            //AnalyticsHelper.sendCustomDimension(3, planCount);

            isAnalyticsSent = true;
        }
    }
}
