package com.clanout.app.ui.screens.pending_invites;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Event;
import com.clanout.app.service.EventService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui._core.BaseFragment;
import com.clanout.app.ui.screens.pending_invites.mvp.PendingInvitesPresenter;
import com.clanout.app.ui.screens.pending_invites.mvp.PendingInvitesPresenterImpl;
import com.clanout.app.ui.screens.pending_invites.mvp.PendingInvitesView;
import com.clanout.app.ui.util.VisibilityAnimationUtil;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;

public class PendingInvitesFragment extends BaseFragment implements
        PendingInvitesView,
        PendingInviteAdapter.PendingInviteClickListener
{
    public static PendingInvitesFragment newInstance()
    {
        return new PendingInvitesFragment();
    }

    PendingInvitesScreen screen;

    PendingInvitesPresenter presenter;

    /* UI Elements */
    @Bind(R.id.etMobileNumber)
    EditText etMobileNumber;

    @Bind(R.id.tvInvalidPhoneError)
    TextView tvInvalidPhoneError;

    @Bind(R.id.llLoading)
    View llLoading;

    @Bind(R.id.llNoPendingInvites)
    View llNoPendingInvites;

    @Bind(R.id.rvInvites)
    RecyclerView rvInvites;

    @Bind(R.id.llExpiredInvites)
    View llExpiredInvites;

    @Bind(R.id.tvExpiredInvites)
    TextView tvExpiredInvites;

    @Bind(R.id.llBottomBar)
    View llBottomBar;

    @Bind(R.id.btnFetch)
    Button btnFetch;

    @Bind(R.id.btnSkip)
    Button btnSkip;

    @Bind(R.id.btnHome)
    Button btnHome;

    TextWatcher mobileErrorMessage;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        UserService userService = UserService.getInstance();
        EventService eventService = EventService.getInstance();
        presenter = new PendingInvitesPresenterImpl(userService, eventService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_pending_invites, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (PendingInvitesScreen) getActivity();
        initRecyclerView();

        mobileErrorMessage = new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                if (s.length() > 0)
                {
                    tvInvalidPhoneError.setVisibility(View.INVISIBLE);
                }
            }
        };
    }

    @Override
    public void onStart()
    {
        super.onStart();
        etMobileNumber.addTextChangedListener(mobileErrorMessage);
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();
        etMobileNumber.addTextChangedListener(null);
        presenter.detachView();
    }

    /* Listeners */
    @OnClick(R.id.btnHome)
    public void onHomeClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,
                GoogleAnalyticsConstants.ACTION_GO_TO_HOME, GoogleAnalyticsConstants
                        .LABEL_AFTER_FETCH);
        /* Analytics */

        if (presenter != null)
        {
            presenter.gotoHome();
        }
    }

    @OnClick(R.id.btnSkip)
    public void onSkipClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_GO_TO_HOME, GoogleAnalyticsConstants.LABEL_ON_SKIP);
        /* Analytics */

        if (presenter != null)
        {
            presenter.skip();
        }
    }

    @OnClick(R.id.btnFetch)
    public void onFetchClicked()
    {
        if (presenter != null)
        {
            presenter.fetchPendingInvites(etMobileNumber.getText().toString());
        }

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_FETCH_INVITES,GoogleAnalyticsConstants.LABEL_ATTEMPT);
        /* Analytics */
    }

    @Override
    public void onPendingInviteClicked(Event event)
    {
        if(presenter != null)
        {
            presenter.selectInvite(event);
        }
    }

    /* View Methods */
    @Override
    public void displayInvalidMobileNumberMessage()
    {
        tvInvalidPhoneError.setVisibility(View.VISIBLE);

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_FETCH_INVITES, GoogleAnalyticsConstants.LABEL_PHONE_ERROR);
        /* Analytics */
    }

    @Override
    public void showLoading()
    {
        etMobileNumber.setEnabled(false);
        llLoading.setVisibility(View.VISIBLE);
        VisibilityAnimationUtil.collapse(llBottomBar, 200);
    }

    @Override
    public void hideLoading()
    {
        llLoading.setVisibility(View.GONE);

        btnFetch.setVisibility(View.GONE);
        btnSkip.setVisibility(View.GONE);
        btnHome.setVisibility(View.VISIBLE);
        VisibilityAnimationUtil.expand(llBottomBar, 200);
    }

    @Override
    public void displayNoActivePendingInvitationsMessage()
    {
       /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_FETCH_INVITES, GoogleAnalyticsConstants.LABEL_NO_ACTIVE_PENDING_INVITES);
        /* Analytics */

        rvInvites.setVisibility(View.GONE);
        llNoPendingInvites.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayActivePendingInvitation(List<Event> events)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_FETCH_INVITES, GoogleAnalyticsConstants.LABEL_PENDING_INVITES_FOUND,events.size());
        /* Analytics */

        rvInvites.setAdapter(new PendingInviteAdapter(getActivity(), events, this));

        llNoPendingInvites.setVisibility(View.GONE);
        rvInvites.setVisibility(View.VISIBLE);
    }

    @Override
    public void displayExpiredEventsMessage(int expiredEventsCount)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_PENDING_INVITES,GoogleAnalyticsConstants.ACTION_FETCH_INVITES, GoogleAnalyticsConstants.LABEL_EXPIRED_INVITES_FOUND,expiredEventsCount);
        /* Analytics */

        llExpiredInvites.setVisibility(View.VISIBLE);
        tvExpiredInvites
                .setText("You have " + expiredEventsCount + " invitations for expired plans");
    }

    @Override
    public void navigateToHome()
    {
        screen.navigateToHomeScreen();
    }

    @Override
    public void navigateToDetails(String eventId)
    {
        screen.navigateToDetailsScreen(eventId);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvInvites.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvInvites.setAdapter(new PendingInviteAdapter(getActivity(), new ArrayList<Event>(), this));
    }
}
