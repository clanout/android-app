package com.clanout.app.ui.screens.home.feed;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Friend;
import com.clanout.app.ui.util.CategoryIconFactory;

import net.steamcrafted.materialiconlib.MaterialIconView;

import org.joda.time.DateTime;
import org.joda.time.LocalDate;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventsAdapter extends RecyclerView.Adapter<EventsAdapter.EventViewHolder>
{
    List<Friend> facebookFriends;
    Context context;
    List<Event> events;
    EventActionListener eventActionListener;

    public EventsAdapter(Context context, List<Event> events, List<Friend> facebookFriends,
                         EventActionListener eventActionListener)
    {
        this.context = context;
        this.events = events;
        this.facebookFriends = facebookFriends;
        this.eventActionListener = eventActionListener;
    }

    @Override
    public EventViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_event, parent, false);
        return new EventViewHolder(v);
    }

    @Override
    public void onBindViewHolder(EventViewHolder holder, int position)
    {
        Event event = events.get(position);
        holder.render(event);
    }

    @Override
    public int getItemCount()
    {
        return events.size();
    }

    public interface EventActionListener
    {
        void onEventClicked(Event event);
    }

    public class EventViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.llCategoryIconContainer)
        View llCategoryIconContainer;

        @Bind(R.id.ivCategoryIcon)
        ImageView ivCategoryIcon;

        @Bind(R.id.tvTitle)
        TextView tvTitle;

        @Bind(R.id.tvFriendsGoing)
        TextView tvFriendsGoing;

        @Bind(R.id.mivRsvp)
        MaterialIconView mivRsvp;

        @Bind(R.id.tvToday)
        TextView tvToday;

        public EventViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View view)
                {
                    eventActionListener.onEventClicked(events.get(getAdapterPosition()));
                }
            });

            /* Analytics */
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME, GoogleAnalyticsConstants
                            .ACTION_OPEN_FEED_ITEM, String
                            .valueOf(getAdapterPosition()));
            /* Analytics */
        }

        public void render(Event event)
        {
            // Title
            tvTitle.setText(event.getTitle());

            // Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(CategoryIconFactory
                    .get(category, Dimensions.CATEGORY_ICON_DEFAULT));
            llCategoryIconContainer.setBackground(CategoryIconFactory.getIconBackground(category));

            // Friends Attending
            List<String> friendIds = event.getFriends();

            List<String> friendNames = getFriendNamesFromIds(friendIds);

            if (friendNames == null) {
                friendNames = new ArrayList<>();
            }

            if (friendIds == null) {
                friendIds = new ArrayList<>();
            }

            int friendsSize = friendNames.size();
            if (event.getFriendCount() == 0) {
                tvFriendsGoing.setText(R.string.label_feed_no_friends);
            }
            else if (event.getFriendCount() == 1) {
                if (friendsSize == 1) {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_one_friend, friendNames.get(0)));
                }
                else {
                    tvFriendsGoing.setText(R.string.label_feed_one_friend_default);
                }
            }
            else {
                if (friendsSize > 0) {
                    int otherCount = event.getFriendCount() - 1;
                    if (otherCount == 1) {
                        if (friendsSize == 2) {
                            tvFriendsGoing.setText(context
                                    .getString(R.string.label_feed_two_friend,
                                            friendNames.get(0), friendNames.get(1)));
                        }
                        else {
                            tvFriendsGoing.setText(context
                                    .getString(R.string.label_feed_multiple_friend_one_other,
                                            friendNames.get(0)));
                        }
                    }
                    else {
                        tvFriendsGoing.setText(context
                                .getString(R.string.label_feed_multiple_friend_multiple_other,
                                        friendNames.get(0), otherCount));
                    }
                }
                else {
                    tvFriendsGoing.setText(context
                            .getString(R.string.label_feed_multiple_friend_default,
                                    event.getFriendCount()));
                }
            }

            // FRIEND_JOINED_EVENT
            if (event.getRsvp() == Event.RSVP.YES) {
                mivRsvp.setVisibility(View.VISIBLE);
            }
            else {
                mivRsvp.setVisibility(View.GONE);
            }

            // Time
            DateTime startTime = event.getStartTime();
            LocalDate today = LocalDate.now();
            LocalDate startDate = startTime.toLocalDate();
            if (startDate.equals(today)) {
                tvToday.setVisibility(View.VISIBLE);
            }
            else {
                tvToday.setVisibility(View.GONE);
            }
        }
    }

    private List<String> getFriendNamesFromIds(List<String> friendIds)
    {
        List<String> friendNames = new ArrayList<>();

        for (String id : friendIds) {
            int index = facebookFriends.indexOf(Friend.of(id));
            if (index != -1) {
                friendNames.add(facebookFriends.get(index).getName());
                if (friendNames.size() == 2) {
                    break;
                }
            }
        }

        return friendNames;
    }

    private String getFriendNameFromId(String friendId)
    {
        for (Friend friend : facebookFriends) {
            if (friend.getId().equals(friendId)) {
                return friend.getName();
            }
        }

        return null;
    }

}
