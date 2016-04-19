package com.clanout.app.ui.screens.invite;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.ui.core.BaseActivity;
import com.clanout.app.ui.core.PermissionHandler;
import com.clanout.app.ui.screens.details.EventDetailsActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteActivity extends BaseActivity implements InviteScreen
{
    private static final String ARG_IS_CREATE_FLOW = "arg_is_create_flow";
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static Intent callingIntent(Context context, boolean isCreateFlow, String eventId)
    {
        if (eventId == null) {
            throw new IllegalArgumentException("event_id cannot be null");
        }

        Intent intent = new Intent(context, InviteActivity.class);
        intent.putExtra(ARG_IS_CREATE_FLOW, isCreateFlow);
        intent.putExtra(ARG_EVENT_ID, eventId);
        return intent;
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    /* Fields */
    PermissionHandler.Listener readContactsPermissionListener;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_INVITE_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_invite);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_invite);
        setActionBarBackVisibility(true);

        /* Notification View */
        String eventId = getIntent().getStringExtra(ARG_EVENT_ID);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, InviteFragment.newInstance(eventId));
        fragmentTransaction.commit();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHandler.Permissions.READ_CONTACTS) {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED) {

                if (PermissionHandler
                        .isRationalRequired(this, PermissionHandler.Permissions.READ_CONTACTS)) {
                    if (readContactsPermissionListener != null) {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                                GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION_STATE,
                                GoogleAnalyticsConstants.LABEL_DENIED);
                        /* Analytics */

                        readContactsPermissionListener
                                .onPermissionDenied(PermissionHandler.Permissions.READ_CONTACTS);
                    }
                }
                else {
                    if (readContactsPermissionListener != null) {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                                GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION_STATE,
                                GoogleAnalyticsConstants.LABEL_PERMANENTLY_DENIED);
                        /* Analytics */

                        readContactsPermissionListener
                                .onPermissionPermanentlyDenied(PermissionHandler.Permissions
                                        .READ_CONTACTS);
                    }
                }
            }
            else {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                        GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION_STATE,
                        GoogleAnalyticsConstants.LABEL_GRANTED);
                /* Analytics */

                if (readContactsPermissionListener != null) {
                    readContactsPermissionListener
                            .onPermissionGranted(PermissionHandler.Permissions.READ_CONTACTS);
                }
            }
        }
    }

    @Override
    public void onBackPressed()

    {
        if (getIntent().getBooleanExtra(ARG_IS_CREATE_FLOW, false)) {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                    GoogleAnalyticsConstants.ACTION_BACK, GoogleAnalyticsConstants
                            .LABEL_CREATE_FLOW);
            /* Analytics */
        }
        else {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                    GoogleAnalyticsConstants.ACTION_BACK, GoogleAnalyticsConstants.LABEL_OTHERS);
            /* Analytics */
        }
        navigateToDetailsScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home) {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                    GoogleAnalyticsConstants.ACTION_UP, null);
            /* Analytics */

            navigateToDetailsScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */
    @Override
    public void setReadContactsPermissionListener(PermissionHandler.Listener listener)
    {
        readContactsPermissionListener = listener;
    }

    @Override
    public void navigateToAppSettings()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_APP_SETTINGS);
        /* Analytics */

        gotoAppSettings();
    }

    @Override
    public void navigateToDetailsScreen()
    {
//        boolean isCreateFlow = getIntent().getBooleanExtra(ARG_IS_CREATE_FLOW, false);

        String eventId = getIntent().getStringExtra(ARG_EVENT_ID);

        Intent intent = EventDetailsActivity.callingIntent(this, eventId, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);

        finish();
    }
}
