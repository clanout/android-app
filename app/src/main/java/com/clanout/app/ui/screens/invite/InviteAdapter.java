package com.clanout.app.ui.screens.invite;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.root.ClanOut;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.screens.invite.mvp.FriendInviteWrapper;
import com.clanout.app.ui.screens.invite.mvp.PhonebookContactInviteWrapper;
import com.clanout.app.ui.util.CircleTransform;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class InviteAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int TYPE_HEADER_FRIENDS = 0;
    private static final int TYPE_FRIENDS = 1;
    private static final int TYPE_HEADER_CONTACTS = 2;
    private static final int TYPE_CONTACTS = 3;

    private Context context;
    private InviteListener inviteListener;
    private String locationName;
    private List<FriendInviteWrapper> friends;
    private List<PhonebookContactInviteWrapper> contacts;
    private int size;

    private Drawable personDrawable;

    public InviteAdapter(Context context, InviteListener inviteListener, String locationName,
                         List<FriendInviteWrapper> friends, List<PhonebookContactInviteWrapper>
                                 contacts)
    {
        this.context = context;
        this.inviteListener = inviteListener;
        this.locationName = locationName;
        this.friends = friends;
        this.contacts = contacts;

        size = 0;
        if (!friends.isEmpty()) {
            size += (friends.size() + 1);
        }

        if (!contacts.isEmpty()) {
            size += (contacts.size() + 1);
        }

        generateDrawables();
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        switch (viewType) {
            case TYPE_HEADER_FRIENDS:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.item_invite_friends_header, parent, false);
                return new FriendsHeaderViewHolder(view);

            case TYPE_FRIENDS:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.item_invite_friend, parent, false);
                return new FriendViewHolder(view);

            case TYPE_HEADER_CONTACTS:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.item_invite_contacts_header, parent, false);
                return new ContactsHeaderViewHolder(view);

            case TYPE_CONTACTS:
                view = LayoutInflater.from(context)
                        .inflate(R.layout.item_invite_contact, parent, false);
                return new ContactViewHolder(view);

            default:
                /* Analytics */
                AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z10, false);
                /* Analytics */

                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        int type = getItemViewType(position);
        switch (type) {
            case TYPE_HEADER_FRIENDS:
                ((FriendsHeaderViewHolder) holder).render();
                break;

            case TYPE_FRIENDS:
                int friendIndex = position - 1;
                ((FriendViewHolder) holder).render(friends.get(friendIndex));
                break;

            case TYPE_HEADER_CONTACTS:
                ((ContactsHeaderViewHolder) holder).render();
                break;

            case TYPE_CONTACTS:
                if (friends.isEmpty()) {
                    int contactIndex = position - 1;
                    ((ContactViewHolder) holder).render(contacts.get(contactIndex));
                }
                else {
                    int contactIndex = position - (friends.size() + 2);
                    ((ContactViewHolder) holder).render(contacts.get(contactIndex));
                }
                break;
        }
    }

    @Override
    public int getItemViewType(int position)
    {
        if (!friends.isEmpty()) {
            if (position == 0) {
                return TYPE_HEADER_FRIENDS;
            }
            else if (position <= friends.size()) {
                return TYPE_FRIENDS;
            }
            else if (position == (friends.size() + 1)) {
                return TYPE_HEADER_CONTACTS;
            }
            else {
                return TYPE_CONTACTS;
            }
        }
        else {
            if (position == 0) {
                return TYPE_HEADER_CONTACTS;
            }
            else {
                return TYPE_CONTACTS;
            }
        }
    }

    @Override
    public int getItemCount()
    {
        return size;
    }

    public interface InviteListener
    {
        void onFriendSelected(FriendInviteWrapper friend, boolean isInvited);

        void onContactSelected(PhonebookContactInviteWrapper contact, boolean isInvited);
    }

    /* Friends Header */
    public class FriendsHeaderViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvTitle)
        TextView tvTitle;

        public FriendsHeaderViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render()
        {
            if (TextUtils.isEmpty(locationName)) {
                tvTitle.setText("Friends");
            }
            else {
                tvTitle.setText("Friends in " + locationName);
            }
        }
    }

    /* Friend */
    public class FriendViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.ivProfilePic)
        ImageView ivProfilePic;

        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.mivGoing)
        View going;

        @Bind(R.id.cbInvite)
        CheckBox cbInvite;

        @Bind(R.id.rlFriendContainer)
        RelativeLayout friendContainer;

        public FriendViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(final FriendInviteWrapper friend)
        {
            Picasso.with(context)
                    .load(UserService.getProfilePicUrl(friend.getFriend().getId()))
                            .placeholder(personDrawable)
                            .transform(new CircleTransform())
                            .into(ivProfilePic);

            tvName.setText(friend.getFriend().getName());

            if (friend.isGoing()) {
                going.setVisibility(View.VISIBLE);
                cbInvite.setVisibility(View.GONE);
            }
            else {
                cbInvite.setVisibility(View.VISIBLE);
                going.setVisibility(View.GONE);

                if (friend.isAlreadyInvited()) {
                    cbInvite.setEnabled(false);
                    cbInvite.setChecked(true);
                }
                else {
                    cbInvite.setEnabled(true);
                    cbInvite.setChecked(friend.isSelected());

                    cbInvite.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            cbInvite.setChecked(!friend.isSelected());
                            inviteListener.onFriendSelected(friend, cbInvite.isChecked());
                        }
                    });

                    friendContainer.setOnClickListener(new View.OnClickListener()
                    {
                        @Override
                        public void onClick(View v)
                        {
                            cbInvite.setChecked(!friend.isSelected());
                            inviteListener.onFriendSelected(friend, cbInvite.isChecked());
                        }
                    });
                }
            }
        }
    }

    /* Contacts Header */
    public class ContactsHeaderViewHolder extends RecyclerView.ViewHolder
    {
        public ContactsHeaderViewHolder(View itemView)
        {
            super(itemView);
        }

        public void render()
        {
        }
    }

    /* Contact */
    public class ContactViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.tvNumber)
        TextView tvNumber;

        @Bind(R.id.cbInvite)
        CheckBox cbInvite;

        @Bind(R.id.rlContactContainer)
        RelativeLayout contactContainer;

        public ContactViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(final PhonebookContactInviteWrapper contact)
        {
            tvName.setText(contact.getPhonebookContact().getName());
            tvNumber.setText(contact.getPhonebookContact().getPhone());
            cbInvite.setChecked(contact.isSelected());

            cbInvite.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    cbInvite.setChecked(!contact.isSelected());
                    inviteListener.onContactSelected(contact, cbInvite.isChecked());
                }
            });

            contactContainer.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    cbInvite.setChecked(!contact.isSelected());
                    inviteListener.onContactSelected(contact, cbInvite.isChecked());
                }
            });
        }
    }

    private void generateDrawables()
    {
        personDrawable = MaterialDrawableBuilder
                .with(ClanOut.getClanOutContext())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(context, R.color.light_grey))
                .setSizeDp(Dimensions.PROFILE_PIC_DEFAULT)
                .build();
    }
}
