package com.clanout.app.ui.screens.edit;

import android.app.ProgressDialog;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Location;
import com.clanout.app.model.util.DateTimeUtil;
import com.clanout.app.service.EventService;
import com.clanout.app.ui._core.BaseFragment;
import com.clanout.app.ui.screens.edit.mvp.EditEventPresenter;
import com.clanout.app.ui.screens.edit.mvp.EditEventPresenterImpl;
import com.clanout.app.ui.screens.edit.mvp.EditEventView;
import com.clanout.app.ui.util.CategoryIconFactory;
import com.clanout.app.ui.util.SnackbarFactory;
import com.clanout.app.ui.util.SoftKeyboardHandler;
import com.wdullaer.materialdatetimepicker.date.DatePickerDialog;
import com.wdullaer.materialdatetimepicker.time.RadialPickerLayout;
import com.wdullaer.materialdatetimepicker.time.TimePickerDialog;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;

import java.util.Calendar;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class EditEventFragment extends BaseFragment implements
        EditEventView, LocationSelectionListener
{
    private static final String ARG_EVENT = "arg_event";

    public static EditEventFragment newInstance(Event event)
    {
        EditEventFragment fragment = new EditEventFragment();

        Bundle args = new Bundle();
        args.putSerializable(ARG_EVENT, event);
        fragment.setArguments(args);

        return fragment;
    }

    EditEventScreen screen;

    EditEventPresenter presenter;

    /* UI Elements */
    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.ivType)
    ImageView ivType;

    @Bind(R.id.tvType)
    TextView tvType;

    @Bind(R.id.tvTime)
    TextView tvTime;

    @Bind(R.id.tvDay)
    TextView tvDay;

    @Bind(R.id.tvLocation)
    TextView tvLocation;

    @Bind(R.id.etDescription)
    EditText etDescription;

    ProgressDialog progressDialog;

    /* Data */
    LocalTime startTime;
    LocalDate startDate;
    Location location;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* Presenter */
        EventService eventService = EventService.getInstance();
        Event event = (Event) getArguments().getSerializable(ARG_EVENT);
        presenter = new EditEventPresenterImpl(eventService, event);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_edit, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (EditEventScreen) getActivity();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
        screen.setLocationSelectionListener(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        presenter.detachView();
        screen.setLocationSelectionListener(null);
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.action_edit, menu);

        MenuItem edit = menu.findItem(R.id.action_edit);

        Drawable editDrawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CHECK)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();
        edit.setIcon(editDrawable);

        edit.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {

            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,
                        GoogleAnalyticsConstants.ACTION_DONE, GoogleAnalyticsConstants
                                .LABEL_PLAN_EDITED);
                /* Analytics */
                edit();
                return true;
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.llLocation)
    public void onLocationClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,GoogleAnalyticsConstants.ACTION_EDIT_LOCATION,GoogleAnalyticsConstants.LABEL_ATTEMPT);
        /* Analytics */

        screen.navigateToLocationSelectionScreen();
    }

    @Override
    public void onLocationSelected(Location location)
    {
        this.location = location;
        tvLocation.setText(location.getName());
    }

    @OnClick(R.id.llTime)
    public void onTimeClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,GoogleAnalyticsConstants.ACTION_EDIT_TIME,GoogleAnalyticsConstants.LABEL_ATTEMPT);
        /* Analytics */

        displayTimePicker();
    }

    @OnClick(R.id.llDay)
    public void onDayClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_EDIT,GoogleAnalyticsConstants.ACTION_EDIT_DAY,GoogleAnalyticsConstants.LABEL_ATTEMPT);
        /* Analytics */

        displayDayPicker();
    }

    /* View Methods */
    @Override
    public void init(Event event)
    {
        // Title
        tvTitle.setText(event.getTitle());

        // Type
        if (event.getType() == Event.Type.OPEN)
        {
            Drawable drawable = MaterialDrawableBuilder
                    .with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                    .setColor(ContextCompat.getColor(getActivity(), R.color.dark_grey))
                    .setSizeDp(10)
                    .build();
            ivType.setImageDrawable(drawable);
            tvType.setText(R.string.event_type_open);
        }
        else
        {
            Drawable drawable = MaterialDrawableBuilder
                    .with(getActivity())
                    .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                    .setColor(ContextCompat.getColor(getActivity(), R.color.dark_grey))
                    .setSizeDp(10)
                    .build();
            ivType.setImageDrawable(drawable);
            tvType.setText(R.string.event_type_secret);
        }

        //Description
        String description = event.getDescription();
        if (description != null)
        {
            etDescription.setText(description);
        }

        // Start Time
        startTime = event.getStartTime().toLocalTime();
        tvTime.setText(DateTimeUtil.formatTime(startTime));

        // Start Day
        startDate = event.getStartTime().toLocalDate();
        tvDay.setText(DateTimeUtil.formatDate(startDate));

        // Location
        Location location = event.getLocation();
        if (location != null)
        {
            String locationName = location.getName();
            if (locationName != null)
            {
                tvLocation.setText(locationName);
            }
        }

        // Category
        EventCategory category = EventCategory.valueOf(event.getCategory());
        ivCategoryIcon.setImageDrawable(CategoryIconFactory
                .get(category, Dimensions.CATEGORY_ICON_DEFAULT));
        llCategoryIconContainer
                .setBackground(CategoryIconFactory.getIconBackground(category));
    }

    @Override
    public void showLoading()
    {
        progressDialog = ProgressDialog.show(getActivity(), "Updating Plan", "Please Wait..");
    }

    @Override
    public void displayError()
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        SnackbarFactory.create(getActivity(), R.string.error_default);
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        if (progressDialog != null)
        {
            progressDialog.dismiss();
        }

        screen.navigateToDetailsScreen(eventId);
    }

    private void edit()
    {
        SoftKeyboardHandler.hideKeyboard(getActivity(), getView());

        String description = etDescription.getText().toString();
        DateTime start = DateTimeUtil.getDateTime(startDate, startTime);

        if (presenter != null)
        {
            presenter.edit(start, location, description);
        }
    }

    /* Helper Methods */
    private void displayTimePicker()
    {
        TimePickerDialog dialog = TimePickerDialog
                .newInstance(
                        new TimePickerDialog.OnTimeSetListener()
                        {
                            @Override
                            public void onTimeSet(RadialPickerLayout view, int hourOfDay, int
                                    minute)
                            {
                                startTime = new LocalTime(hourOfDay, minute);
                                tvTime.setText(DateTimeUtil.formatTime(startTime));
                            }
                        },
                        startTime.getHourOfDay(),
                        startTime.getMinuteOfHour(),
                        false);

        dialog.dismissOnPause(true);
        dialog.vibrate(false);
        dialog.setTitle("Start Time");
        dialog.show(getFragmentManager(), "TimePicker");
    }

    private void displayDayPicker()
    {
        DatePickerDialog dialog = DatePickerDialog
                .newInstance(
                        new DatePickerDialog.OnDateSetListener()
                        {
                            @Override
                            public void onDateSet(DatePickerDialog view, int year, int
                                    monthOfYear, int dayOfMonth)
                            {
                                startDate = new LocalDate(year, (monthOfYear + 1), dayOfMonth);
                                tvDay.setText(DateTimeUtil.formatDate(startDate));
                            }
                        },
                        startDate.getYear(),
                        (startDate.getMonthOfYear() - 1),
                        startDate.getDayOfMonth());

        dialog.dismissOnPause(true);
        dialog.vibrate(false);

        LocalDate today = LocalDate.now();

        int startYear = today.getYear();
        int endYear = today.getYear();
        dialog.setYearRange(startYear, endYear);

        Calendar minDate = Calendar.getInstance();
        minDate.set(Calendar.DAY_OF_MONTH, today.getDayOfMonth());
        minDate.set(Calendar.MONTH, (today.getMonthOfYear() - 1));
        minDate.set(Calendar.YEAR, today.getYear());
        dialog.setMinDate(minDate);

        dialog.show(getFragmentManager(), "DatePicker");
    }
}
