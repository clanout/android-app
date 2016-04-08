package com.clanout.app.ui.screens.chat;

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
import com.clanout.app.ui.screens.details.EventDetailsActivity;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatActivity extends BaseActivity implements ChatScreen
{
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static Intent callingIntent(Context context, String eventId)
    {
        if (eventId == null)
        {
            throw new IllegalArgumentException("event_id cannot be null");
        }

        Intent intent = new Intent(context, ChatActivity.class);
        intent.putExtra(ARG_EVENT_ID, eventId);
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
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_CHAT_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_chat);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_chat);
        setActionBarBackVisibility(true);

        /* Chat View */
        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, ChatFragment
                .newInstance(getIntent().getStringExtra(ARG_EVENT_ID)));
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed()
    {
        close();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            close();
        }
        return super.onOptionsItemSelected(item);
    }

    /* Screen Methods */
    @Override
    public void close()
    {
        if (isTaskRoot())
        {
            String eventId = getIntent().getStringExtra(ARG_EVENT_ID);
            startActivity(EventDetailsActivity.callingIntent(this, eventId, false));
        }
        finish();
    }
}