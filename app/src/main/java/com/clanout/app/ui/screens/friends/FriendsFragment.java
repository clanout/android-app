package com.clanout.app.ui.screens.friends;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Friend;
import com.clanout.app.service.LocationService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui._core.BaseFragment;
import com.clanout.app.ui.screens.friends.mvp.FriendsPresenter;
import com.clanout.app.ui.screens.friends.mvp.FriendsPresenterImpl;
import com.clanout.app.ui.screens.friends.mvp.FriendsView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendsFragment extends BaseFragment implements
        FriendsView,
        FriendAdapter.BlockListener, SearchView.OnQueryTextListener
{
    private static final String ARG_NEW_FRIENDS = "arg_new_friends";

    public static FriendsFragment newInstance(HashSet<String> newFriendsSet)
    {
        FriendsFragment friendsFragment = new FriendsFragment();
        Bundle bundle = new Bundle();
        bundle.putSerializable(ARG_NEW_FRIENDS, newFriendsSet);
        friendsFragment.setArguments(bundle);
        return friendsFragment;
    }

    FriendsScreen screen;

    FriendsPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvFriends)
    RecyclerView rvFriends;

    @Bind(R.id.tvMessage)
    TextView tvMessage;

    @Bind(R.id.loading)
    ProgressBar loading;

    List<Friend> localFriends;
    List<Friend> otherFriends;
    String locationZone;

    HashSet<String> newFriends;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);

        /* User Service */
        UserService userService = UserService.getInstance();

        /* Location Service */
        LocationService locationService = LocationService.getInstance();

        newFriends = (HashSet<String>) getArguments().get(ARG_NEW_FRIENDS);

        /* Presenter */
        presenter = new FriendsPresenterImpl(userService, locationService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_friends, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        screen = (FriendsScreen) getActivity();
        initRecyclerView();
    }

    @Override
    public void onStart()
    {
        super.onStart();
        presenter.attachView(this);
    }

    @Override
    public void onStop()
    {
        super.onStop();

        presenter.detachView();
    }

    @Override
    public void onCreateOptionsMenu(Menu menu, MenuInflater inflater)
    {
        super.onCreateOptionsMenu(menu, inflater);

        menu.clear();
        inflater.inflate(R.menu.menu_friends, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id
                .action_search));
        searchView.setOnQueryTextListener(this);

    }

    /* Listeners */
    @Override
    public void onBlockToggled(Friend friend, FriendListItem friendListItem)
    {
        presenter.onBlockToggled(friend, friendListItem);
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        rvFriends.setVisibility(View.GONE);
        tvMessage.setVisibility(View.GONE);
    }

    @Override
    public void displayNoFriendsMessage()
    {
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_MANAGE_FRIENDS,
                        GoogleAnalyticsConstants.ACTION_NO_FRIENDS, null);
        /* Analytics */

        tvMessage.setText(R.string.no_facebook_friends);

        tvMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
    }

    @Override
    public void displayError()
    {
        /* Analytics */
        AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z15, false);
        /* Analytics */

        tvMessage.setText(R.string.error_facebook_friends);

        tvMessage.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvFriends.setVisibility(View.GONE);
    }

    @Override
    public void displayFriends(List<Friend> localFriends, List<Friend> otherFriends, String locationZone)
    {
        this.localFriends = localFriends;
        this.otherFriends = otherFriends;
        this.locationZone = locationZone;

        refreshRecyclerView(localFriends, otherFriends, locationZone);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvFriends.setLayoutManager(new LinearLayoutManager(getActivity()));
        refreshRecyclerView(new ArrayList<Friend>(), new ArrayList<Friend>(), null);
    }

    private void refreshRecyclerView(List<Friend> visibleLocalFriends, List<Friend> visibleOtherFriends, String locationZone)
    {
        rvFriends
                .setAdapter(new FriendAdapter(getActivity(), visibleLocalFriends, visibleOtherFriends, locationZone, this, newFriends));

        rvFriends.setVisibility(View.VISIBLE);
        tvMessage.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);
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

        List<Friend> visibleLocalFriends = new ArrayList<>();
        List<Friend> visibleOtherFriends = new ArrayList<>();

        if (newText.length() >= 1)
        {
            visibleLocalFriends = new ArrayList<>();
            for (Friend friend : localFriends)
            {
                String[] nameTokens = friend.getName().toLowerCase().split(" ");
                for (String nameToken : nameTokens)
                {
                    if (nameToken.startsWith(query))
                    {
                        visibleLocalFriends.add(friend);
                        break;
                    }
                }
            }

            visibleOtherFriends = new ArrayList<>();
            for (Friend friend : otherFriends)
            {
                String[] nameTokens = friend.getName().toLowerCase().split(" ");
                for (String nameToken : nameTokens)
                {
                    if (nameToken.startsWith(query))
                    {
                        visibleOtherFriends.add(friend);
                        break;
                    }
                }
            }

            if (visibleLocalFriends.isEmpty() && visibleOtherFriends.isEmpty())
            {
                displayNoFriendsMessage();
            }
            else
            {
                refreshRecyclerView(visibleLocalFriends, visibleOtherFriends, locationZone);
            }
        }
        else if (newText.length() == 0)
        {
            refreshRecyclerView(localFriends, otherFriends, locationZone);
        }
        return false;
    }
}
