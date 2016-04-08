package com.clanout.app.ui.screens.details;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.ViewPager;
import android.view.MenuItem;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Event;
import com.clanout.app.service.EventService;
import com.clanout.app.ui._core.BaseActivity;
import com.clanout.app.ui.screens.chat.ChatActivity;
import com.clanout.app.ui.screens.details.mvp.EventDetailsContainerPresenter;
import com.clanout.app.ui.screens.details.mvp.EventDetailsContainerPresenterImpl;
import com.clanout.app.ui.screens.details.mvp.EventDetailsContainerView;
import com.clanout.app.ui.screens.home.HomeActivity;
import com.clanout.app.ui.screens.invite.InviteActivity;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventDetailsActivity extends BaseActivity implements
        EventDetailsContainerView,
        EventDetailsScreen
{
    private static final String ARG_EVENT_ID = "arg_event_id";
    private static final String ARG_IS_FROM_HOME = "arg_is_from_home";

    public static Intent callingIntent(Context context, String eventId, boolean isFromHome)
    {
        Intent intent = new Intent(context, EventDetailsActivity.class);
        intent.putExtra(ARG_EVENT_ID, eventId);
        intent.putExtra(ARG_IS_FROM_HOME, isFromHome);
        return intent;
    }

    EventDetailsContainerPresenter presenter;

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    @Bind(R.id.vpEventDetails)
    ViewPager vpEventDetails;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EVENT_DETAILS_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_event_details);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_details);
        setActionBarBackVisibility(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        String eventId = getIntent().getStringExtra(ARG_EVENT_ID);
        presenter = new EventDetailsContainerPresenterImpl(eventService, eventId);
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        presenter.detachView();
    }

    @Override
    public void onBackPressed()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_BACK,null);
        /* Analytics */

        navigateBack();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_UP,null);
            /* Analytics */

            navigateToHomeScreen();
        }
        return super.onOptionsItemSelected(item);
    }

    /* View Methods */
    @Override
    public void initView(List<Event> events, int activePosition)
    {
        vpEventDetails.setAdapter(new EventDetailsPagerAdapter(getFragmentManager(), events));
        vpEventDetails.setCurrentItem(activePosition);
        vpEventDetails.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {
            }

            @Override
            public void onPageSelected(int position)
            {
                if (presenter != null)
                {
                    presenter.setActivePosition(position);
                }
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {
            }
        });
    }

    @Override
    public void handleError()
    {
        /* Analytics */
        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z12, false);
        /* Analytics */

        navigateToHomeScreen();
    }

    /* Screen Methods */
    @Override
    public void navigateToChatScreen(String eventId)
    {
        startActivity(ChatActivity.callingIntent(this, eventId));
    }

    @Override
    public void navigateToInviteScreen(String eventId)
    {
        startActivity(InviteActivity.callingIntent(this, false, eventId));
    }

    @Override
    public void navigateToEditScreen(Event event)
    {
        // TODO
//        startActivity(EditEventActivity.callingIntent(this, event));
//        finish();
    }

    @Override
    public void navigateToHomeScreen()
    {
        boolean isFromHome = getIntent().getBooleanExtra(ARG_IS_FROM_HOME, false);
        if (!isFromHome)
        {
            startActivity(HomeActivity.callingIntent(this));
        }
        finish();
    }

    @Override
    public void setTitle(String title)
    {
        setScreenTitle(title);
    }

    /* Helper Method */
    private void navigateBack()
    {
        if (isTaskRoot())
        {
            startActivity(HomeActivity.callingIntent(this));
        }
        finish();
    }
}
