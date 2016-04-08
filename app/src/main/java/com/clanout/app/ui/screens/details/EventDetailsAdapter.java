package com.clanout.app.ui.screens.details;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.TextUtils;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.Attendee;
import com.clanout.app.model.AttendeeWrapper;
import com.clanout.app.model.Event;
import com.clanout.app.model.EventCategory;
import com.clanout.app.model.Location;
import com.clanout.app.model.User;
import com.clanout.app.model.util.DateTimeUtil;
import com.clanout.app.service.FacebookService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.dialog.AttendeeDialog;
import com.clanout.app.ui.util.CategoryIconFactory;
import com.clanout.app.ui.util.CircleTransform;
import com.clanout.app.ui.util.FriendBubbles;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class EventDetailsAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static Drawable personDrawable;

    private static final int TYPE_DETAILS = 0;
    private static final int TYPE_ME = 1;
    private static final int TYPE_ATTENDEES = 2;
    private static final int TYPE_NO_ATTENDEES = 3;

    private Context context;
    private EventDetailsListener listener;
    private User sessionUser;
    private Event event;
    private boolean isLastMinute;
    private List<AttendeeWrapper> attendees;
    private int size;

    public EventDetailsAdapter(Context context, EventDetailsListener listener,
                               User sessionUser, Event event, boolean isLastMinute)
    {
        this.context = context;
        this.listener = listener;
        this.sessionUser = sessionUser;
        this.event = event;
        this.isLastMinute = isLastMinute;

        size = 2;
    }

    public void resetEvent(Event event)
    {
        this.event = event;
        notifyItemRangeChanged(0, 2);
    }

    public void setAttendees(List<AttendeeWrapper> attendees)
    {
        if (attendees == null || attendees.isEmpty())
        {
            size = 3;
        }
        else
        {
            this.attendees = attendees;
            size = 2 + attendees.size();
        }
        notifyDataSetChanged();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        switch (viewType)
        {
            case TYPE_DETAILS:
                view = LayoutInflater.from(context)
                                     .inflate(R.layout.item_event_details, parent, false);
                return new EventDetailsViewHolder(view);

            case TYPE_ME:
                view = LayoutInflater.from(context)
                                     .inflate(R.layout.item_event_details_me, parent, false);
                return new MeViewHolder(view);

            case TYPE_ATTENDEES:
                view = LayoutInflater.from(context)
                                     .inflate(R.layout.item_event_details_attendee, parent, false);
                return new AttendeeViewHolder(view);

            case TYPE_NO_ATTENDEES:
                view = LayoutInflater.from(context)
                                     .inflate(R.layout.item_event_details_no_attendee, parent, false);
                return new NoAttendeeViewHolder(view);
        }

        return null;
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        int type = getItemViewType(position);
        switch (type)
        {
            case TYPE_DETAILS:
                ((EventDetailsViewHolder) holder).render(event);
                break;

            case TYPE_ME:
                ((MeViewHolder) holder).render();
                break;

            case TYPE_ATTENDEES:
                int index = position - 2;
                if (index >= 0)
                {
                    ((AttendeeViewHolder) holder).render(attendees.get(index));
                }
                break;

            case TYPE_NO_ATTENDEES:
                ((NoAttendeeViewHolder) holder).render();
                break;
        }
    }

    @Override
    public int getItemCount()
    {
        return size;
    }

    @Override
    public int getItemViewType(int position)
    {
        if (position == 0)
        {
            return TYPE_DETAILS;
        }
        else if (position == 1)
        {
            return TYPE_ME;
        }
        else if (position == 2)
        {
            if (attendees == null || attendees.isEmpty())
            {
                return TYPE_NO_ATTENDEES;
            }
            else
            {
                return TYPE_ATTENDEES;
            }
        }
        else
        {
            return TYPE_ATTENDEES;
        }
    }

    public interface EventDetailsListener
    {
        void onEdit();

        void onDescriptionClicked(String description);

        void onNavigationClicked(Location location);

        void onRsvpToggled();

        void onStatusClicked(String oldStatus);

        void onLastMinuteStatusClicked(String oldStatus);

        void onFriendsBubbleClicked();
    }

    public class EventDetailsViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.llFinalizationInfo)
        View llFinalizationInfo;

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

        @Bind(R.id.llDescription)
        View llDescription;

        @Bind(R.id.tvDescription)
        TextView tvDescription;

        @Bind(R.id.tvTime)
        TextView tvTime;

        @Bind(R.id.llLocation)
        View llLocation;

        @Bind(R.id.tvLocation)
        TextView tvLocation;

        @Bind(R.id.mivNavigation)
        View mivNavigation;

        @Bind(R.id.tvEdit)
        TextView tvEdit;

        public EventDetailsViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(final Event event)
        {
            if (event == null)
            {
                return;
            }

            boolean isDescriptionNull = false;
            boolean isLocationNull = false;

            // Finalization Info
            llFinalizationInfo.setVisibility(View.GONE);

            // Category Icon
            EventCategory category = EventCategory.valueOf(event.getCategory());
            ivCategoryIcon.setImageDrawable(CategoryIconFactory
                    .get(category, Dimensions.CATEGORY_ICON_DEFAULT));
            llCategoryIconContainer
                    .setBackground(CategoryIconFactory.getIconBackground(category));

            // Title
            tvTitle.setText(event.getTitle());

            // Type
            if (event.getType() == Event.Type.OPEN)
            {
                Drawable drawable = MaterialDrawableBuilder
                        .with(context)
                        .setIcon(MaterialDrawableBuilder.IconValue.LOCK_OPEN)
                        .setColor(ContextCompat.getColor(context, R.color.dark_grey))
                        .setSizeDp(10)
                        .build();
                ivType.setImageDrawable(drawable);
                tvType.setText(R.string.event_type_open);
            }
            else
            {
                Drawable drawable = MaterialDrawableBuilder
                        .with(context)
                        .setIcon(MaterialDrawableBuilder.IconValue.LOCK)
                        .setColor(ContextCompat.getColor(context, R.color.dark_grey))
                        .setSizeDp(10)
                        .build();
                ivType.setImageDrawable(drawable);
                tvType.setText(R.string.event_type_secret);
            }

            // Description
            String description = event.getDescription();
            if (!TextUtils.isEmpty(description))
            {
                tvDescription.setText(description);

                Layout l = tvDescription.getLayout();
                if (l != null)
                {
                    int lines = l.getLineCount();
                    if (lines > 0)
                    {
                        if (l.getEllipsisCount(lines - 1) > 0)
                        {
                            int start = l.getLineStart(0);
                            int end = l.getLineEnd(1);

                            String descStr = description.substring(start, end - 1);
                            if (descStr.charAt(descStr.length() - 1) == '\n')
                            {
                                descStr = descStr.substring(0, descStr.length() - 1);
                            }

                            if (descStr.length() < 60)
                            {
                                descStr = descStr + "…" + "more";
                            }
                            else
                            {
                                descStr = descStr.substring(0, 56) + "more";
                            }

                            int spanStartIndex = descStr.length() - 4;
                            int spanEndIndex = descStr.length();
                            Spannable spannable = new SpannableString(descStr);
                            spannable.setSpan(new ForegroundColorSpan(ContextCompat
                                            .getColor(context, R.color.accent)), spanStartIndex, spanEndIndex,
                                    Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
                            tvDescription.setText(spannable);

                            llDescription.setOnClickListener(new View.OnClickListener()
                            {
                                @Override
                                public void onClick(View v)
                                {
                                    if (listener != null)
                                    {
                                        listener.onDescriptionClicked(event.getDescription());
                                    }
                                }
                            });
                        }
                    }
                }

                llDescription.setVisibility(View.VISIBLE);
            }
            else
            {
                llDescription.setVisibility(View.GONE);
                isDescriptionNull = true;
            }

            // Time
            tvTime.setText(event.getStartTime().toString(DateTimeUtil.DATE_TIME_FORMATTER));

            // Location
            final Location location = event.getLocation();
            if (TextUtils.isEmpty(location.getName()))
            {
                llLocation.setVisibility(View.GONE);
                isLocationNull = true;
            }
            else
            {
                llLocation.setVisibility(View.VISIBLE);
                tvLocation.setText(location.getName());

                if (location.getLatitude() != null && location.getLongitude() != null)
                {
                    mivNavigation.setVisibility(View.VISIBLE);

                    llLocation.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (listener != null)
                            {
                                listener.onNavigationClicked(event.getLocation());
                            }
                        }
                    });
                }
                else
                {
                    mivNavigation.setVisibility(View.GONE);
                }
            }

            // Post Refresh Rendering
            tvEdit.setVisibility(View.GONE);
            if (event.getRsvp() == Event.RSVP.YES)
            {
                tvEdit.setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        if (listener != null)
                        {
                            if (tvEdit.getText().toString() == "Add Location and Description")
                            {
                                /* Analytics */
                                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants
                                        .CATEGORY_DETAILS, GoogleAnalyticsConstants
                                        .ACTION_ADD_LOCATION_AND_DESCRIPTION, null);
                                /* Analytics */
                            }
                            else if (tvEdit.getText().toString() == "Add Location")
                            {
                                /* Analytics */
                                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_ADD_LOCATION,null);
                                /* Analytics */
                            }
                            else if (tvEdit.getText().toString() == "Add Description")
                            {
                                /* Analytics */
                                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_ADD_DESCRIPTION,null);
                                /* Analytics */
                            }

                            listener.onEdit();
                        }
                    }
                });

                if (isLocationNull && isDescriptionNull)
                {
                    tvEdit.setVisibility(View.VISIBLE);
                    tvEdit.setText("Add Location and Description");
                }
                else if (isLocationNull && !isDescriptionNull)
                {
                    tvEdit.setVisibility(View.VISIBLE);
                    tvEdit.setText("Add Location");
                }
                else if (!isLocationNull && isDescriptionNull)
                {
                    tvEdit.setVisibility(View.VISIBLE);
                    tvEdit.setText("Add Description");
                }
            }
        }
    }

    public class MeViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.rlMeContainer)
        View rlMeContainer;

        @Bind(R.id.ivProfilePic)
        ImageView ivProfilePic;

        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.ivStatus)
        ImageView ivStatus;

        @Bind(R.id.tvStatus)
        TextView tvStatus;

        @Bind(R.id.llRsvp)
        View llRsvp;

        @Bind(R.id.sRsvp)
        SwitchCompat sRsvp;

        @Bind(R.id.tvRsvp)
        TextView tvRsvp;

        public MeViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render()
        {
            if (sessionUser == null || event == null)
            {
                return;
            }

            // Name
            tvName.setText(sessionUser.getName());

            // Profile Pic
            Drawable placeHolder =
                    MaterialDrawableBuilder
                            .with(context)
                            .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                            .setColor(ContextCompat.getColor(context, R.color.light_grey))
                            .setSizeDp(24)
                            .build();

            Picasso.with(context)
                   .load(UserService.getProfilePicUrl(sessionUser.getId()))
                   .placeholder(placeHolder)
                   .transform(new CircleTransform())
                   .into(ivProfilePic);

            // Rsvp
            if (event.getCreatorId().equals(sessionUser.getId()))
            {
                llRsvp.setVisibility(View.GONE);
            }
            else
            {
                llRsvp.setVisibility(View.VISIBLE);
            }

            if (event.getRsvp() == Event.RSVP.YES)
            {
                sRsvp.setChecked(true);
                tvRsvp.setText(R.string.rsvp_yes);
                tvRsvp.setTextColor(ContextCompat.getColor(context, R.color.accent));
            }
            else
            {
                sRsvp.setChecked(false);
                tvRsvp.setText(R.string.rsvp_no);
                tvRsvp.setTextColor(ContextCompat.getColor(context, R.color.text_subtitle));
            }

            llRsvp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (listener != null)
                    {
                        listener.onRsvpToggled();
                    }
                }
            });

            sRsvp.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (listener != null)
                    {
                        listener.onRsvpToggled();
                    }
                }
            });

            // Status
            if (event.getRsvp() != Event.RSVP.YES)
            {
                ivStatus.setVisibility(View.GONE);
                tvStatus.setVisibility(View.GONE);
            }
            else
            {
                ivStatus.setVisibility(View.VISIBLE);
                tvStatus.setVisibility(View.VISIBLE);

                if (isLastMinute)
                {
                    Drawable drawable = MaterialDrawableBuilder
                            .with(context)
                            .setIcon(MaterialDrawableBuilder.IconValue.CLOCK_FAST)
                            .setColor(ContextCompat.getColor(context, R.color.accent))
                            .setSizeDp(18)
                            .build();

                    ivStatus.setImageDrawable(drawable);

                    if (TextUtils.isEmpty(event.getStatus()))
                    {
                        tvStatus.setText(R.string.label_status_last_moment);
                        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent));
                    }
                    else
                    {
                        tvStatus.setText(event.getStatus());
                        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent));
                    }

                    rlMeContainer.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (listener != null)
                            {
                                listener.onLastMinuteStatusClicked(event.getStatus());
                            }
                        }
                    });
                }
                else
                {
                    Drawable drawable = MaterialDrawableBuilder
                            .with(context)
                            .setIcon(MaterialDrawableBuilder.IconValue.TOOLTIP_EDIT)
                            .setColor(ContextCompat.getColor(context, R.color.accent))
                            .setSizeDp(18)
                            .build();

                    ivStatus.setImageDrawable(drawable);

                    if (TextUtils.isEmpty(event.getStatus()))
                    {
                        tvStatus.setText(R.string.label_status);
                        tvStatus.setTextColor(ContextCompat.getColor(context, R.color.accent));
                    }
                    else
                    {
                        tvStatus.setText(event.getStatus());
                        tvStatus.setTextColor(ContextCompat
                                .getColor(context, R.color.text_subtitle));
                    }

                    rlMeContainer.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            if (listener != null)
                            {
                                listener.onStatusClicked(event.getStatus());
                            }
                        }
                    });
                }
            }
        }
    }

    public class AttendeeViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.rlAttendeeContainer)
        RelativeLayout attendeeContainer;

        @Bind(R.id.ivPic)
        ImageView ivPic;

        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.tvStatus)
        TextView tvStatus;

        @Bind(R.id.tvInvite)
        TextView tvInvite;

        public AttendeeViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            if (personDrawable == null)
            {
                personDrawable = MaterialDrawableBuilder
                        .with(context)
                        .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                        .setColor(ContextCompat
                                .getColor(context, R.color.light_grey))
                        .setSizeDp(Dimensions.PROFILE_PIC_DEFAULT)
                        .build();
            }
        }

        public void render(final AttendeeWrapper attendeeWrapper)
        {
            // Profile Pic
            Picasso.with(context)
                   .load(UserService.getProfilePicUrl(attendeeWrapper.getAttendee().getId()))
                   .placeholder(personDrawable)
                   .transform(new CircleTransform())
                   .into(ivPic);

            // Name
            tvName.setText(attendeeWrapper.getAttendee().getName());

            // Status
            if (attendeeWrapper.getAttendee().getStatus() == null || attendeeWrapper.getAttendee().getStatus().isEmpty())
            {
                tvStatus.setVisibility(View.GONE);
            }
            else
            {
                tvStatus.setVisibility(View.VISIBLE);
                tvStatus.setText(attendeeWrapper.getAttendee().getStatus());
            }

            // Inviter
            if (attendeeWrapper.isInviter())
            {
                tvInvite.setVisibility(View.VISIBLE);
            }
            else
            {
                tvInvite.setVisibility(View.GONE);
            }

            attendeeContainer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    AttendeeDialog.show(context, attendeeWrapper, personDrawable);
                }
            });
        }
    }

    public class NoAttendeeViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.friendBubbles)
        View friendBubbles;

        public NoAttendeeViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            friendBubbles.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (listener != null)
                    {
                        listener.onFriendsBubbleClicked();
                    }
                }
            });
        }

        public void render()
        {
            FriendBubbles
                    .render(context, friendBubbles, "No Attendees yet. Invite your %s friends.");
        }
    }
}
