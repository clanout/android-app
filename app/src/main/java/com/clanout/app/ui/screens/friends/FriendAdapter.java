package com.clanout.app.ui.screens.friends;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
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
import com.clanout.app.model.Friend;
import com.clanout.app.root.ClanOut;
import com.clanout.app.service.FacebookService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.screens.friends.mvp.FriendsView;
import com.clanout.app.ui.util.CircleTransform;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.HashSet;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class FriendAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int TYPE_HEADER_LOCAL_FRIENDS = 0;
    private static final int TYPE_LOCAL_FRIENDS = 1;
    private static final int TYPE_HEADER_OTHER_FRIENDS = 2;
    private static final int TYPE_OTHER_FRIENDS = 3;

    private Context context;
    private BlockListener blockListener;

    private List<Friend> localFriends;
    private List<Friend> otherFriends;
    private String locationZone;
    int size;

    private Drawable blockedDrawable;
    private Drawable unblockedDrawable;
    private Drawable personDrawable;

    private HashSet<String> newFriends;

    public FriendAdapter(Context context, List<Friend> localFriends, List<Friend> otherFriends,
                         String locationZone, BlockListener blockListener, HashSet<String>
                                 newFriends)
    {
        this.context = context;
        this.localFriends = localFriends;
        this.otherFriends = otherFriends;
        this.locationZone = locationZone;
        this.blockListener = blockListener;
        this.newFriends = newFriends;

        size = 0;
        if (!localFriends.isEmpty()) {
            size += (localFriends.size() + 1);
        }

        if (!otherFriends.isEmpty()) {
            size += (otherFriends.size() + 1);
        }

        generateDrawables();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        switch (viewType) {
            case TYPE_HEADER_LOCAL_FRIENDS:
            case TYPE_HEADER_OTHER_FRIENDS:
                View headerView = LayoutInflater.from(context)
                        .inflate(R.layout.item_friend_header, parent, false);
                return new FriendHeaderViewHolder(headerView);

            case TYPE_LOCAL_FRIENDS:
            case TYPE_OTHER_FRIENDS:
                View friendView = LayoutInflater.from(context)
                        .inflate(R.layout.item_friend, parent, false);
                return new FriendViewHolder(friendView);

            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_HEADER_LOCAL_FRIENDS:
                ((FriendHeaderViewHolder) holder).render(true);
                break;

            case TYPE_LOCAL_FRIENDS:
                int localFriendIndex = position - 1;
                ((FriendViewHolder) holder).render(localFriends.get(localFriendIndex));
                break;

            case TYPE_HEADER_OTHER_FRIENDS:
                ((FriendHeaderViewHolder) holder).render(false);
                break;

            case TYPE_OTHER_FRIENDS:
                if (localFriends.isEmpty()) {
                    int otherFriendIndex = position - 1;
                    ((FriendViewHolder) holder).render(otherFriends.get(otherFriendIndex));
                }
                else {
                    int otherFriendIndex = position - (localFriends.size() + 2);
                    ((FriendViewHolder) holder).render(otherFriends.get(otherFriendIndex));
                }
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
        if (!localFriends.isEmpty()) {
            if (position == 0) {
                return TYPE_HEADER_LOCAL_FRIENDS;
            }
            else if (position <= localFriends.size()) {
                return TYPE_LOCAL_FRIENDS;
            }
            else if (position == (localFriends.size() + 1)) {
                return TYPE_HEADER_OTHER_FRIENDS;
            }
            else {
                return TYPE_OTHER_FRIENDS;
            }
        }
        else {
            if (position == 0) {
                return TYPE_HEADER_OTHER_FRIENDS;
            }
            else {
                return TYPE_OTHER_FRIENDS;
            }
        }
    }

    public interface BlockListener
    {
        void onBlockToggled(Friend friend, FriendsView.FriendListItem friendListItem);
    }

    class FriendViewHolder extends RecyclerView.ViewHolder implements FriendsView.FriendListItem
    {
        ImageView userPic;
        TextView username;
        TextView isNew;
        ImageView blockIcon;

        public FriendViewHolder(View itemView)
        {
            super(itemView);

            username = (TextView) itemView.findViewById(R.id.tvName);
            userPic = (ImageView) itemView.findViewById(R.id.ivProfilePic);
            blockIcon = (ImageView) itemView.findViewById(R.id.ivBlock);
            isNew = (TextView) itemView.findViewById(R.id.tvNew);
        }

        @Override
        public void render(final Friend friend)
        {
            Picasso.with(context)
                    .load(UserService.getProfilePicUrl(friend.getId()))
                    .placeholder(personDrawable)
                    .transform(new CircleTransform())
                    .into(userPic);

            username.setText(friend.getName());

            if (friend.isBlocked()) {
                blockIcon.setImageDrawable(blockedDrawable);
            }
            else {
                blockIcon.setImageDrawable(unblockedDrawable);
            }

            if (newFriends != null) {
                if (newFriends.contains(friend.getId())) {

                    isNew.setVisibility(View.VISIBLE);
                }else{

                    isNew.setVisibility(View.INVISIBLE);
                }
            }

            blockIcon.setOnClickListener(new View.OnClickListener()
            {

                @Override
                public void onClick(View v)
                {

                    /* Analytics */
                    AnalyticsHelper
                            .sendEvents(GoogleAnalyticsConstants.CATEGORY_MANAGE_FRIENDS,
                                    GoogleAnalyticsConstants.ACTION_BLOCK_OR_UNBLOCK, null);
                    /* Analytics */


                    blockListener.onBlockToggled(friend, FriendViewHolder.this);
                }
            });
        }
    }

    class FriendHeaderViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvTitle)
        TextView tvTitle;

        public FriendHeaderViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(boolean isLocalFriendsHeader)
        {
            if (!isLocalFriendsHeader) {
                tvTitle.setText("Other Friends");
            }
            else {
                tvTitle.setText("Friends in " + locationZone);
            }
        }
    }

    private void generateDrawables()
    {
        blockedDrawable = MaterialDrawableBuilder
                .with(ClanOut.getClanOutContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.RED)
                .setSizeDp(24)
                .build();

        unblockedDrawable = MaterialDrawableBuilder
                .with(ClanOut.getClanOutContext())
                .setIcon(MaterialDrawableBuilder.IconValue.BLOCK_HELPER)
                .setColor(Color.LTGRAY)
                .setSizeDp(24)
                .build();

        personDrawable = MaterialDrawableBuilder
                .with(ClanOut.getClanOutContext())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(Dimensions.PROFILE_PIC_DEFAULT)
                .build();
    }
}
