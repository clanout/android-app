package com.clanout.app.ui.screens.pending_invites;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.ui.core.BaseActivity;
import com.clanout.app.ui.screens.details.EventDetailsActivity;
import com.clanout.app.ui.screens.home.HomeActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class PendingInvitesActivity extends BaseActivity implements PendingInvitesScreen
{
    public static Intent callingIntent(Context context)
    {
        Intent intent = new Intent(context, PendingInvitesActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);
        return intent;
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_PENDING_INVITES_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_pending_invites);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_pending_invites);
        setActionBarBackVisibility(false);

        /* Pending Invitation View */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, PendingInvitesFragment.newInstance());
        fragmentTransaction.commit();
    }

    /* Screen Methods */
    @Override
    public void navigateToHomeScreen()
    {
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId, false));
    }
}
