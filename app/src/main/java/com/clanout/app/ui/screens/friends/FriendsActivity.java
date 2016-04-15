package com.clanout.app.ui.screens.friends;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.view.MenuItem;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.ui._core.BaseActivity;

import java.util.HashSet;
import java.util.Set;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsActivity extends BaseActivity implements FriendsScreen
{
    private static final String ARG_NEW_FRIENDS = "arg_new_friends";

    public static Intent callingIntent(Context context, Set<String> newFriends)
    {
        Intent intent = new Intent(context, FriendsActivity.class);
        HashSet<String> newFriendsHashSet = (HashSet<String>) newFriends;
        intent.putExtra(ARG_NEW_FRIENDS, newFriendsHashSet);
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
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_MANAGE_FRIENDS_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_friends);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_friends);
        setActionBarBackVisibility(true);

        /* Notification View */

        HashSet<String> newFriends = (HashSet<String>) getIntent().getExtras().get(ARG_NEW_FRIENDS);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, FriendsFragment.newInstance(newFriends));
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_MANAGE_FRIENDS, GoogleAnalyticsConstants.ACTION_BACK, GoogleAnalyticsConstants.LABEL_ACCOUNT);
        /* Analytics */

        navigateToAccountScreen();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_MANAGE_FRIENDS,GoogleAnalyticsConstants.ACTION_UP,null);
            /* Analytics */

            navigateToAccountScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */

    @Override
    public void navigateToAccountScreen()
    {
        finish();
    }
}
