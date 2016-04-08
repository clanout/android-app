package com.clanout.app.ui.screens.notifications;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.helper.ItemTouchHelper;
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
import com.clanout.app.model.NotificationWrapper;
import com.clanout.app.service.NotificationService;
import com.clanout.app.ui._core.BaseFragment;
import com.clanout.app.ui.dialog.DefaultDialog;
import com.clanout.app.ui.screens.notifications.mvp.NotificationPresenter;
import com.clanout.app.ui.screens.notifications.mvp.NotificationPresenterImpl;
import com.clanout.app.ui.screens.notifications.mvp.NotificationView;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationFragment extends BaseFragment implements
        NotificationView,
        NotificationAdapter.NotificationClickListener
{
    public static NotificationFragment newInstance()
    {
        return new NotificationFragment();
    }

    NotificationScreen screen;

    NotificationPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rvNotifications)
    RecyclerView rvNotifications;

    @Bind(R.id.tvNoNotifications)
    TextView tvNoNotifications;

    @Bind(R.id.loading)
    ProgressBar loading;

    MenuItem clearAll;
    boolean isClearAllVisible;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        setHasOptionsMenu(true);
        isClearAllVisible = true;

        /* Services */
        NotificationService notificationService = NotificationService.getInstance();

        /* Presenter */
        presenter = new NotificationPresenterImpl(notificationService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_notification, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (NotificationScreen) getActivity();
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
        inflater.inflate(R.menu.menu_notification, menu);

        Drawable drawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.NOTIFICATION_CLEAR_ALL)
                .setColor(ContextCompat.getColor(getActivity(), R.color.white))
                .setSizeDp(Dimensions.ACTION_BAR_DP)
                .build();

        clearAll = menu.findItem(R.id.action_clear);
        clearAll.setIcon(drawable);
        clearAll.setVisible(isClearAllVisible);
        clearAll.setOnMenuItemClickListener(new MenuItem.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,
                        GoogleAnalyticsConstants.ACTION_CLEAR_ALL, null);
                /* Analytics */
                if (presenter != null) {
                    presenter.deleteAll();
                }
                return true;
            }
        });
    }

    /* View Methods */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
    }

    @Override
    public void displayNotifications(List<NotificationWrapper> notifications)
    {
        isClearAllVisible = true;
        if (clearAll != null)
        {
            clearAll.setVisible(isClearAllVisible);
        }

        rvNotifications.setAdapter(new NotificationAdapter(getActivity(), notifications, this));

        rvNotifications.setVisibility(View.VISIBLE);
        tvNoNotifications.setVisibility(View.GONE);
        loading.setVisibility(View.GONE);

        ItemTouchHelper.SimpleCallback callback =
                new ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT | ItemTouchHelper.RIGHT)
                {
                    @Override
                    public boolean onMove(RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, RecyclerView.ViewHolder target)
                    {
                        return false;
                    }

                    @Override
                    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,GoogleAnalyticsConstants.ACTION_CLEAR_NOTIF, null);
                        /* Analytics */

                        int position = viewHolder.getAdapterPosition();
                        presenter.onNotificationDeleted(position);
                        rvNotifications.getAdapter().notifyItemRemoved(position);
                    }
                };

        ItemTouchHelper touchHelper = new ItemTouchHelper(callback);
        touchHelper.attachToRecyclerView(rvNotifications);
    }

    @Override
    public void displayNoNotificationsMessage()
    {
        isClearAllVisible = false;
        if (clearAll != null)
        {
            clearAll.setVisible(isClearAllVisible);
        }

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION, GoogleAnalyticsConstants.ACTION_NO_NOTIFICATION, null);
        /* Analytics */

        tvNoNotifications.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        rvNotifications.setVisibility(View.GONE);
    }

    @Override
    public void displayEventRemovedMessage(final NotificationWrapper notification)
    {
        /* Analytics  */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_DELETED_PLAN);
        /* Analytics  */

        DefaultDialog.show(getActivity(),
                R.string.event_removed_title,
                R.string.event_removed_message,
                R.string.event_removed_positive_button,
                R.string.event_removed_negative_button,
                true,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_CREATE,
                                GoogleAnalyticsConstants.ACTION_OPEN, GoogleAnalyticsConstants
                                        .LABEL_PLAN_DELETED_NOTIFICATION);
                        /* Analytics */

                        screen.navigateToCreateScreen();
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {

                        ((NotificationAdapter) rvNotifications.getAdapter()).removeNotification
                                (notification);
                    }
                }
        );
    }

    @Override
    public void navigateToDetailsScreen(String eventId)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_DETAILS);
        /* Analytics */

        screen.navigateToDetailsScreen(eventId);
    }

    @Override
    public void navigateToChatScreen(String eventId)
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_CHAT);
        /* Analytics */

        screen.navigateToChatScreen(eventId);
    }

    @Override
    public void navigateToFriendsScreen()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,GoogleAnalyticsConstants.ACTION_GO_TO,GoogleAnalyticsConstants.LABEL_MANAGE_FRIENDS);
        /* Analytics */

        screen.navigateToFriendsScreen();
    }

    /* Listeners */
    @Override
    public void onNotificationClicked(NotificationWrapper notification)
    {
        presenter.onNotificationSelected(notification);
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        rvNotifications.setLayoutManager(new LinearLayoutManager(getActivity()));
        rvNotifications
                .setAdapter(new NotificationAdapter(getActivity(), new ArrayList<NotificationWrapper>(), this));
    }
}
