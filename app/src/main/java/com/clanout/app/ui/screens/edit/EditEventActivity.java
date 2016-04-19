package com.clanout.app.ui.screens.edit;

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
import com.clanout.app.model.Event;
import com.clanout.app.model.Location;
import com.clanout.app.service.LocationService;
import com.clanout.app.ui.core.BaseActivity;
import com.clanout.app.ui.screens.details.EventDetailsActivity;
import com.clanout.app.ui.screens.home.HomeActivity;
import com.google.android.gms.common.api.Status;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;

import butterknife.Bind;
import butterknife.ButterKnife;
import timber.log.Timber;

public class EditEventActivity extends BaseActivity implements EditEventScreen
{
    private static final String ARG_EVENT = "arg_event";

    public static Intent callingIntent(Context context, Event event)
    {
        if (event == null)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z13, false);
            /* Analytics */

            throw new IllegalStateException("eventis null");
        }

        Intent intent = new Intent(context, EditEventActivity.class);
        intent.putExtra(ARG_EVENT, event);
        return intent;
    }

    private static final int PLACE_AUTOCOMPLETE_REQUEST_CODE = 44;

    /* UI Elements */
    @Bind(R.id.appBarLayout)
    AppBarLayout appBarLayout;

    LocationSelectionListener listener;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_EDIT_EVENT_ACTIVITY);

        /* Setup UI */
        setContentView(R.layout.activity_edit);
        ButterKnife.bind(this);

        /* Toolbar Setup */
        setActionBar(appBarLayout);
        showActionBar();
        setScreenTitle(R.string.title_edit);

        /* Close Action in toolbar */
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_close);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        /* Edit View */
        Event event = (Event) getIntent().getSerializableExtra(ARG_EVENT);

        FragmentManager fragmentManager = getFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.content, EditEventFragment.newInstance(event));
        fragmentTransaction.commit();
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item)
    {
        if (item.getItemId() == android.R.id.home)
        {
            /* Analytics */
            AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,GoogleAnalyticsConstants.ACTION_CANCEL,null);
            /* Analytics */

            String eventId = ((Event) getIntent().getSerializableExtra(ARG_EVENT)).getId();
            navigateToDetailsScreen(eventId);
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    public void onBackPressed()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,GoogleAnalyticsConstants.ACTION_BACK,null);
        /* Analytics */

        String eventId = ((Event) getIntent().getSerializableExtra(ARG_EVENT)).getId();
        navigateToDetailsScreen(eventId);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        if (requestCode == PLACE_AUTOCOMPLETE_REQUEST_CODE)
        {
            if (resultCode == RESULT_OK)
            {
                Place place = PlaceAutocomplete.getPlace(this, data);
                Location location = new Location();
                location.setName(place.getName().toString());
                location.setLatitude(place.getLatLng().latitude);
                location.setLongitude(place.getLatLng().longitude);
                location.setZone(LocationService.getInstance().getCurrentLocation().getZone());

                if (listener != null)
                {
                    listener.onLocationSelected(location);
                }
            }
            else if (resultCode == PlaceAutocomplete.RESULT_ERROR)
            {
                Status status = PlaceAutocomplete.getStatus(this, data);
                Timber.e("Result Error : " + status.getStatusMessage());

            }
            else if (resultCode == RESULT_CANCELED)
            {
            }
        }
    }

    /* Screen Methods */
    @Override
    public void setLocationSelectionListener(LocationSelectionListener listener)
    {
        this.listener = listener;
    }

    @Override
    public void navigateToLocationSelectionScreen()
    {
        try
        {
            Intent intent = new PlaceAutocomplete
                    .IntentBuilder(PlaceAutocomplete.MODE_OVERLAY)
                    .build(this);
            startActivityForResult(intent, PLACE_AUTOCOMPLETE_REQUEST_CODE);
        }
        catch (Exception e)
        {
           /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z14, false);
            /* Analytics */

            Timber.d("Exception while sending intent to PlaceAutocomplete " + e.getMessage());
        }
    }

    @Override
    public void navigateToHomeScreen()
    {
        startActivity(HomeActivity.callingIntent(this));
        finish();
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        Intent intent = EventDetailsActivity.callingIntent(this, eventId, false);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
        finish();
    }
}
