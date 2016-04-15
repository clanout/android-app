package com.clanout.app.ui.screens.notifications;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.model.NotificationWrapper;
import com.clanout.app.root.ClanOut;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationsViewHolder>
{
    private Context context;
    private List<NotificationWrapper> notifications;
    private NotificationClickListener notificationClickListener;

    /* Notification Icons */
    private Drawable friendAddedDrawable;
    private Drawable eventRemovedDrawable;
    private Drawable eventInvitationDrawable;
    private Drawable chatDrawable;
    private Drawable updateDrawable;
    private Drawable friendJoinedEventDrawable;
    private Drawable alertDrawable;

    public NotificationAdapter(Context context, List<NotificationWrapper> notifications, NotificationClickListener notificationClickListener)
    {
        this.context = context;
        this.notifications = notifications;
        this.notificationClickListener = notificationClickListener;

        generateDrawables();
    }

    @Override
    public NotificationsViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context)
                                  .inflate(R.layout.item_notification, parent, false);
        return new NotificationsViewHolder(view);
    }

    @Override
    public void onBindViewHolder(NotificationsViewHolder holder, int position)
    {
        NotificationWrapper notification = notifications.get(position);
        holder.render(notification);
    }

    @Override
    public int getItemCount()
    {
        return notifications.size();
    }

    public void removeNotification(NotificationWrapper notification)
    {
        int index = notifications.indexOf(notification);
        notifications.remove(notification);
        notifyItemRemoved(index);
    }

    public interface NotificationClickListener
    {
        void onNotificationClicked(NotificationWrapper notification);
    }

    public class NotificationsViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.llNotificationIconContainer)
        LinearLayout notificationIconContainer;

        @Bind(R.id.ivNotificationIcon)
        ImageView notificationIcon;

        @Bind(R.id.tvTitle)
        TextView tvTitle;

        @Bind(R.id.tvMessage1)
        TextView tvMessage1;

        public NotificationsViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);

            itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    if (notificationClickListener != null)
                    {
                        notificationClickListener
                                .onNotificationClicked(notifications.get(getAdapterPosition()));
                    }
                }
            });
        }

        public void render(NotificationWrapper notification)
        {
            int type = notification.getType();

            ShapeDrawable circle = new ShapeDrawable(new OvalShape());
            circle.getPaint()
                    .setColor(ContextCompat.getColor(ClanOut.getClanOutContext(), R.color
                            .primary_light));
            notificationIconContainer.setBackground(circle);

            NotificationWrapper.NotificationItem item;
            switch (type)
            {
                case NotificationWrapper.Type.EVENT_ACTIVITY:
                    tvTitle.setText(notification.getTitle());
                    int size = notification.getNotificationItems().size();
                    if (size == 1)
                    {
                        item = notification.getNotificationItems().get(0);
                        renderIcon(item, notificationIcon);
                        renderItem(item, tvMessage1);
                    }
                    else if (size == 2)
                    {
                        notificationIcon.setImageDrawable(alertDrawable);
                        displayMessage(notification.getNotificationItems(), tvMessage1);
                    }
                    else if (size == 3)
                    {
                        notificationIcon.setImageDrawable(alertDrawable);
                        tvMessage1.setText("Details Updated | New Joinees | New Chat");
                    }
                    break;

                case NotificationWrapper.Type.EVENT_INVITATION:
                    notificationIcon.setImageDrawable(eventInvitationDrawable);
                    tvTitle.setText(notification.getTitle());
                    item = notification.getNotificationItems().get(0);
                    renderItem(item, tvMessage1);
                    break;

                case NotificationWrapper.Type.EVENT_REMOVED:
                    notificationIcon.setImageDrawable(eventRemovedDrawable);
                    tvTitle.setText(notification.getTitle());
                    item = notification.getNotificationItems().get(0) ;
                    renderItem(item, tvMessage1);
                    break;

                case NotificationWrapper.Type.NEW_FRIEND_JOINED_APP:
                    notificationIcon.setImageDrawable(friendAddedDrawable);
                    tvTitle.setText("New Friends");
                    item = notification.getNotificationItems().get(0);
                    renderItem(item, tvMessage1);
                    break;
            }
        }

        private void renderItem(NotificationWrapper.NotificationItem item, TextView tvMessage)
        {
            int type = item.getType();
            switch (type)
            {
                case NotificationWrapper.NotificationItem.Type.EVENT_REMOVED:
                    tvMessage.setText("This clan has been removed");
                    break;

                case NotificationWrapper.NotificationItem.Type.NEW_FRIEND_JOINED_APP:
                    tvMessage.setText(item.getMessage());
                    break;

                case NotificationWrapper.NotificationItem.Type.INVITATION:
                    tvMessage.setText(item.getMessage().substring(9, item.getMessage().length() - 1));
                    break;

                case NotificationWrapper.NotificationItem.Type.EVENT_UPDATED:
                    tvMessage.setText("Details updated");
                    break;

                case NotificationWrapper.NotificationItem.Type.NEW_CHAT:
                    tvMessage.setText("New chat");
                    break;

                case NotificationWrapper.NotificationItem.Type.FRIEND_JOINED_EVENT:
                    tvMessage.setText("New friends have joined");
                    break;
            }
        }
    }

    private void displayMessage(List<NotificationWrapper.NotificationItem> notificationItems,
                                TextView tvMessage)
    {
        if(notificationItems.size() == 2)
        {
            tvMessage.setText(getMessage(notificationItems.get(0)) + " | " + getMessage(notificationItems.get(1)));
        }else if(notificationItems.size() == 3)
        {
            if(notificationItems.size() == 2)
            {
                tvMessage.setText(getMessage(notificationItems.get(0)) + " | " + getMessage(notificationItems.get(1)) + " | " + getMessage(notificationItems.get(2)));
            }
        }
    }

    private String getMessage(NotificationWrapper.NotificationItem notificationItem)
    {
        int type = notificationItem.getType();
        switch (type)
        {
            case NotificationWrapper.NotificationItem.Type.EVENT_UPDATED:
                return "Details updated";

            case NotificationWrapper.NotificationItem.Type.NEW_CHAT:
                return "New Chat";

            case NotificationWrapper.NotificationItem.Type.FRIEND_JOINED_EVENT:
                return ("New joinees");

            default:
                return "New Notification";
        }
    }

    private void renderIcon(NotificationWrapper.NotificationItem notificationItem, ImageView
            notificationIcon)
    {
        switch (notificationItem.getType())
        {
            case NotificationWrapper.NotificationItem.Type.NEW_CHAT:
                notificationIcon.setImageDrawable(chatDrawable);
                break;

            case NotificationWrapper.NotificationItem.Type.EVENT_UPDATED:
                notificationIcon.setImageDrawable(updateDrawable);
                break;

            case NotificationWrapper.NotificationItem.Type.FRIEND_JOINED_EVENT:
                notificationIcon.setImageDrawable(friendJoinedEventDrawable);
                break;
        }
    }

    private void generateDrawables()
    {
        friendJoinedEventDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_MULTIPLE_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();

        updateDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.TABLE_EDIT)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();

        chatDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.MESSAGE_TEXT)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();


        friendAddedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_PLUS)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();

        eventRemovedDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_REMOVE)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();

        eventInvitationDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.EMAIL_OPEN)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();

        alertDrawable = MaterialDrawableBuilder
                .with(context)
                .setIcon(MaterialDrawableBuilder.IconValue.CALENDAR_BLANK)
                .setColor(ContextCompat
                        .getColor(context, R.color.white))
                .build();
    }
}
