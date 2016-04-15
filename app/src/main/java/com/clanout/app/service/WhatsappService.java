package com.clanout.app.service;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;

import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.GoogleAnalyticsConstants;


public class WhatsappService
{
    private static WhatsappService instance;

    public static void init(UserService userService)
    {
        instance = new WhatsappService(userService);
    }

    public static WhatsappService getInstance()
    {
        if (instance == null)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z8, true);
            /* Analytics */

            throw new IllegalStateException("[AccountService Not Initialized]");
        }

        return instance;
    }

    private static final String WHATSAPP_MESSAGE = "Let\'s plan our hangouts on ClanOut. Join me here @ %s";
    private static final String WHATSAPP_PACKAGE_NAME = "com.whatsapp";

    private UserService userService;

    private WhatsappService(UserService userService)
    {
        this.userService = userService;
    }

    public boolean isWhatsAppInstalled(Activity activity)
    {
        try
        {
            activity.getPackageManager()
                    .getPackageInfo(WHATSAPP_PACKAGE_NAME, PackageManager.GET_ACTIVITIES);
            return true;
        }
        catch (PackageManager.NameNotFoundException e)
        {
            return false;
        }
    }

    public Intent getWhatsAppIntent()
    {
        String message = String
                .format(WHATSAPP_MESSAGE, AppConstants.APP_LINK);

        Intent sendIntent = new Intent();
        sendIntent.setAction(Intent.ACTION_SEND);
        sendIntent.putExtra(Intent.EXTRA_TEXT, message);
        sendIntent.setType("text/plain");
        sendIntent.setPackage(WHATSAPP_PACKAGE_NAME);

        return sendIntent;
    }
}
