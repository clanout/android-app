package com.clanout.app.ui.screens.unknown_zone;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.ui.core.BaseActivity;

/**
 * Created by harsh on 07/04/16.
 */
public class UnKnownZoneActivity extends BaseActivity
{
    public static Intent getCallingIntent(Context context)
    {
        return new Intent(context, UnKnownZoneActivity.class);
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_UNKNOWN_ZONE);

        setContentView(R.layout.activity_unknown_zone);
    }
}
