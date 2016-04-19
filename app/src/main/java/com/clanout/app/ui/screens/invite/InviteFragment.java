package com.clanout.app.ui.screens.invite;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.service.EventService;
import com.clanout.app.service.LocationService;
import com.clanout.app.service.PhonebookService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.core.BaseFragment;
import com.clanout.app.ui.core.PermissionHandler;
import com.clanout.app.ui.dialog.UpdateMobileDialog;
import com.clanout.app.ui.screens.invite.mvp.FriendInviteWrapper;
import com.clanout.app.ui.screens.invite.mvp.InvitePresenter;
import com.clanout.app.ui.screens.invite.mvp.InvitePresenterImpl;
import com.clanout.app.ui.screens.invite.mvp.InviteView;
import com.clanout.app.ui.screens.invite.mvp.PhonebookContactInviteWrapper;
import com.clanout.app.ui.util.VisibilityAnimationUtil;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteFragment extends BaseFragment implements
        InviteView,
        InviteAdapter.InviteListener,
        PermissionHandler.Listener, SearchView.OnQueryTextListener
{
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static InviteFragment newInstance(String eventId)
    {
        InviteFragment fragment = new InviteFragment();

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);

        return fragment;
    }

    InviteScreen screen;

    InvitePresenter presenter;

    /* UI Elements */
    @Bind(R.id.llError)
    View llError;

    @Bind(R.id.tvRetry)
    View tvRetry;

    @Bind(R.id.llPermission)
    View llPermission;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.rvFriends)
    RecyclerView rvFriends;

    @Bind(R.id.tvMessage)
    TextView tvMessage;

    @Bind(R.id.rlInvite)
    View rlInvite;

    @Bind(R.id.tvInviteCount)
    TextView tvInviteCount;

    ProgressBar pbRefreshing;

    MenuItem refresh;
    MenuItem addPhone;
    boolean isAddPhoneVisible;

//    TextWatcher search;
    String locationName;
    List<FriendInviteWrapper> friends;
    List<PhonebookContactInviteWrapper> contacts;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        isAddPhoneVisible = true;

        /* Presenter */
        UserService userService = UserService.getInstance();
        EventService eventService = EventService.getInstance();
        PhonebookService phonebookService = PhonebookService.getInstance();
        LocationService locationService = LocationService.getInstance();
        String eventId = getArguments().getString(ARG_EVENT_ID);
        presenter = new InvitePresenterImpl(userService, eventService, phonebookService, locationService, eventId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_invite, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (InviteScreen) getActivity();

        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();

        rlInvite.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.sendInvitations();
                }
            }
        });

        llPermission.setVisibility(View.GONE);
        screen.setReadContactsPermissionListener(this);
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        rlInvite.setOnClickListener(null);
        llPermission.setOnClickListener(null);
        screen.setReadContactsPermissionListener(null);
        tvRetry.setOnClickListener(null);
        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_invite, menu);

        refresh = menu.findItem(R.id.action_refresh);
        addPhone = menu.findItem(R.id.action_add_phone);

        Drawable addPhoneIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.CELLPHONE_ANDROID)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();
        addPhone.setIcon(addPhoneIcon);

        Drawable refreshIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();
        refresh.setIcon(refreshIcon);

        refresh.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                if (presenter != null)
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,
                            GoogleAnalyticsConstants.ACTION_REFRESH, null);
                    /* Analytics */

                    presenter.refresh();
                }
                return true;
            }
        });

        addPhone.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener()
        {
            @Override
            public boolean onMenuItemClick(MenuItem item)
            {
                UpdateMobileDialog.show(getActivity(), new UpdateMobileDialog.Listener()
                {
                    @Override
                    public void onSuccess(String mobileNumber)
                    {
                        addPhone.setVisible(false);
                    }
                });

                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_UPDATE_MOBILE);
                AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_UPDATE_PHONE_DIALOG_FROM_INVITE);
                /* Analytics */

                return true;
            }
        });

        addPhone.setVisible(isAddPhoneVisible);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id
                .action_search));
        searchView.setOnQueryTextListener(this);
    }

    /* Permission Handling */
    @Override
    public void onPermissionGranted(@PermissionHandler.Permissions int permission)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION,GoogleAnalyticsConstants.LABEL_GRANTED);
        /* Analytics */

        llPermission.setOnClickListener(null);
        presenter.attachView(this);

        if (llPermission.getVisibility() != View.GONE)
        {
            VisibilityAnimationUtil.collapse(llPermission, 200);
        }
    }

    @Override
    public void onPermissionDenied(@PermissionHandler.Permissions int permission)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION,GoogleAnalyticsConstants.LABEL_DENIED);
        /* Analytics */

        llPermission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                PermissionHandler
                        .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
            }
        });

        if (llPermission.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llPermission, 200);
        }
    }

    @Override
    public void onPermissionPermanentlyDenied(@PermissionHandler.Permissions int permission)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_INVITE,GoogleAnalyticsConstants.ACTION_CONTACTS_PERMISSION,GoogleAnalyticsConstants.LABEL_PERMANENTLY_DENIED);
        /* Analytics */

        llPermission.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                screen.navigateToAppSettings();
            }
        });

        if (llPermission.getVisibility() != View.VISIBLE)
        {
            VisibilityAnimationUtil.expand(llPermission, 200);
        }
    }

    /* Listeners */
    @Override
    public void onFriendSelected(FriendInviteWrapper friend, boolean isInvited)
    {
        if (presenter != null)
        {
            presenter.select(friend, isInvited);
        }
    }

    @Override
    public void onContactSelected(PhonebookContactInviteWrapper contact, boolean isInvited)
    {
        if (friends != null)
        {
            presenter.select(contact, isInvited);
        }
    }

    /* View Methods */
    @Override
    public void showAddPhoneOption()
    {
        isAddPhoneVisible = true;
        if (addPhone != null)
        {
            addPhone.setVisible(true);
        }
    }

    @Override
    public void hideAddPhoneOption()
    {
        isAddPhoneVisible = false;
        if (addPhone != null)
        {
            addPhone.setVisible(false);
        }
    }

    @Override
    public void handleReadContactsPermission()
    {
        if (PermissionHandler
                .isRationalRequired(getActivity(), PermissionHandler.Permissions.READ_CONTACTS))
        {
            llPermission.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PermissionHandler
                            .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
                }
            });

            VisibilityAnimationUtil.expand(llPermission, 200);
        }
        else
        {
            // Read contacts permission has not been granted yet. Request it directly.
            PermissionHandler
                    .requestPermission(getActivity(), PermissionHandler.Permissions.READ_CONTACTS);
        }
    }

    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public void displayError()
    {
        llError.setVisibility(View.VISIBLE);
        tvRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    presenter.retry();
                }
            }
        });
    }

    @Override
    public void displayNoFriendOrContactMessage()
    {
        tvMessage.setVisibility(View.VISIBLE);
        rvFriends.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void displayInviteList(String locationName, List<FriendInviteWrapper> friends, List<PhonebookContactInviteWrapper> phonebookContacts)
    {
        this.locationName = locationName;
        this.friends = friends;
        this.contacts = phonebookContacts;

        refreshRecyclerView(locationName, friends, phonebookContacts);
    }

    @Override
    public void showInviteButton(int inviteCount)
    {
        tvInviteCount.setText(String.valueOf(inviteCount));
        rlInvite.setVisibility(View.VISIBLE);
    }

    @Override
    public void hideInviteButton()
    {
        rlInvite.setVisibility(View.GONE);
    }

    @Override
    public void navigateToDetailsScreen()
    {
        screen.navigateToDetailsScreen();
    }

    @Override
    public void showRefreshing()
    {
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_ALWAYS);

        refresh.setActionView(R.layout.view_action_refreshing);
        pbRefreshing = (ProgressBar) refresh.getActionView().findViewById(R.id.pbRefreshing);
        pbRefreshing.getIndeterminateDrawable()
                    .setColorFilter(ContextCompat.getColor(getActivity(), R.color.white),
                            android.graphics.PorterDuff.Mode.SRC_IN);
    }

    @Override
    public void hideRefreshing()
    {
        refresh.setShowAsAction(MenuItem.SHOW_AS_ACTION_NEVER);

        refresh.setActionView(null);
        Drawable refreshIcon = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.REFRESH)
                .setColor(ContextCompat
                        .getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();
        refresh.setIcon(refreshIcon);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvFriends
                .setAdapter(new InviteAdapter(getActivity(), this, null, new ArrayList<FriendInviteWrapper>(), new ArrayList<PhonebookContactInviteWrapper>()));
    }

    private void refreshRecyclerView(String locationName, List<FriendInviteWrapper> friends, List<PhonebookContactInviteWrapper> contacts)
    {
        rvFriends
                .setAdapter(new InviteAdapter(getActivity(), this, locationName, friends, contacts));

        rvFriends.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        llError.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public boolean onQueryTextSubmit(String query)
    {
        return false;
    }

    @Override
    public boolean onQueryTextChange(String newText)
    {
        String query = newText.toLowerCase();

        List<FriendInviteWrapper> visibleFriends = new ArrayList<>();
        List<PhonebookContactInviteWrapper> visibleContacts = new ArrayList<>();

        if (newText.length() >= 1)
        {
            visibleFriends = new ArrayList<>();
            for (FriendInviteWrapper friend : friends)
            {
                String[] nameTokens = friend.getFriend().getName().toLowerCase().split(" ");
                for (String nameToken : nameTokens)
                {
                    if (nameToken.startsWith(query))
                    {
                        visibleFriends.add(friend);
                        break;
                    }
                }
            }

            visibleContacts = new ArrayList<>();
            for (PhonebookContactInviteWrapper contact : contacts)
            {
                String[] nameTokens = contact.getPhonebookContact().getName().toLowerCase().split(" ");
                for (String nameToken : nameTokens)
                {
                    if (nameToken.startsWith(query))
                    {
                        visibleContacts.add(contact);
                        break;
                    }
                }
            }

            if (visibleFriends.isEmpty() && visibleContacts.isEmpty())
            {
                displayNoFriendOrContactMessage();
            }
            else
            {
                refreshRecyclerView(locationName, visibleFriends, visibleContacts);
            }
        }
        else if (newText.length() == 0)
        {
            refreshRecyclerView(locationName, friends, contacts);
        }
        return false;
    }
}
