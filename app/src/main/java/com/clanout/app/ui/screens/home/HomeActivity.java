package com.clanout.app.ui.screens.home;

import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.communication.Communicator;
import com.clanout.app.communication.NewNotificationReceivedTrigger;
import com.clanout.app.communication.NewNotificationsAvailableTrigger;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.EventCategory;
import com.clanout.app.service.NotificationService;
import com.clanout.app.ui.core.BaseActivity;
import com.clanout.app.ui.screens.accounts.AccountActivity;
import com.clanout.app.ui.screens.create.CreateActivity;
import com.clanout.app.ui.screens.details.EventDetailsActivity;
import com.clanout.app.ui.screens.home.create_suggestion.CreateSuggestionFragment;
import com.clanout.app.ui.screens.home.feed.EventFeedFragment;
import com.clanout.app.ui.screens.notifications.NotificationActivity;
import com.squareup.otto.Bus;
import com.squareup.otto.Subscribe;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;

public class HomeActivity extends BaseActivity implements HomeScreen
{
    public static Intent callingIntent(Context context)
    {
        Intent intent = new Intent(context, HomeActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        return intent;
    }

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    MenuItem notification;
    Drawable notificationIcon;

    /* Notification Listener */
    Bus notificationCommunicator;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_HOME_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_home);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_home);
        setActionBarBackVisibility(false);

        /* Notification Communicator */
        notificationCommunicator = Communicator.getInstance().getBus();

        FragmentManager fragmentManager = getFragmentManager();
        /* Create Box */
        FragmentTransaction createFragmentTransaction = fragmentManager.beginTransaction();
        createFragmentTransaction.replace(R.id.createBox, CreateSuggestionFragment.newInstance());
        createFragmentTransaction.commit();

        /* Event Feed */
        FragmentTransaction feedFragmentTransaction = fragmentManager.beginTransaction();
        feedFragmentTransaction.replace(R.id.feed, EventFeedFragment.newInstance());
        feedFragmentTransaction.commit();
    }

    @Override
    protected void onResume()
    {
        super.onResume();

        notificationCommunicator.register(this);

        /* Notification */
        notificationIcon = ContextCompat.getDrawable(this, R.drawable.ab_notification_icon);

        if (notification != null)
        {
            notification.setIcon(notificationIcon);
        }

        /* Request Notifications */
        NotificationService notificationService = NotificationService.getInstance();
        notificationService.areNewNotificationsAvailable();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        notificationCommunicator.unregister(this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu)
    {
        menu.clear();
        getMenuInflater().inflate(R.menu.action_home, menu);

        notification = menu.findItem(R.id.action_notifications);

        notification.setIcon(notificationIcon);

        menu.findItem(R.id.action_account)
            .setIcon(MaterialDrawableBuilder
                    .with(this)
                    .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                    .setColor(ContextCompat.getColor(this, R.color.white))
                    .setSizeDp(Dimensions.ACTION_BAR_DP)
                    .build());

        notification
                .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
                {
                    @Override
                    public boolean onMenuItemClick(MenuItem item)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_NOTIFICATION);
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION, GoogleAnalyticsConstants.ACTION_OPEN, GoogleAnalyticsConstants.LABEL_FROM_HOME);
                        /* Analytics */

                        navigateToNotificationScreen();
                        return true;
                    }
                });

        menu.findItem(R.id.action_account)
            .setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
            {
                @Override
                public boolean onMenuItemClick(MenuItem menuItem)
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_ACCOUNT);
                    /* Analytics */

                    navigateToAccountsScreen();
                    return true;
                }
            });

        return super.onCreateOptionsMenu(menu);
    }

    /* Notification Listeners */
    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void newNotificationsAvailable(NewNotificationsAvailableTrigger trigger)
    {
        notificationIcon = ContextCompat.getDrawable(this, R.drawable.ab_notification_icon_active);
        if (notification != null)
        {
            notification.setIcon(notificationIcon);
        }
    }

    @SuppressWarnings("UnusedParameters")
    @Subscribe
    public void newNotificationReceived(NewNotificationReceivedTrigger trigger)
    {
        notificationIcon = ContextCompat.getDrawable(this, R.drawable.ab_notification_icon_active);
        if (notification != null)
        {
            notification.setIcon(notificationIcon);
        }
    }

    /* Screen Methods */
    @Override
    public void navigateToCreateDetailsScreen(EventCategory category)
    {
        startActivity(CreateActivity.callingIntent(this, category));
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        startActivity(EventDetailsActivity.callingIntent(this, eventId, true));
    }

    /* Helper Methods */
    private void navigateToAccountsScreen()
    {
        startActivity(AccountActivity.callingIntent(this));
    }

    private void navigateToNotificationScreen()
    {
        startActivity(NotificationActivity.callingIntent(this));
    }
}
