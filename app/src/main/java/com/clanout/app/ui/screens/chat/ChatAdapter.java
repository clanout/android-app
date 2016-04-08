package com.clanout.app.ui.screens.chat;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.model.ChatMessage;

import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;

import java.util.ArrayList;
import java.util.List;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatAdapter extends RecyclerView.Adapter<RecyclerView.ViewHolder>
{
    private static final int TYPE_ME = 0;
    private static final int TYPE_OTHERS = 1;
    private static final int TYPE_ADMIN = 2;

    private static DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormat
            .forPattern("dd MMM, HH:mm");

    private static DateTimeFormatter TIMESTAMP_FORMATTER_TIME_ONLY = DateTimeFormat
            .forPattern("HH:mm");

    private Context context;
    private List<ChatMessage> chatMessages;
    private String sessionUserId;

    public ChatAdapter(Context context, String sessionUserId)
    {
        this.context = context;
        this.sessionUserId = sessionUserId;
        chatMessages = new ArrayList<>();
    }

    public boolean addMessage(ChatMessage chatMessage)
    {
        int index = getInsertIndex(chatMessage);
        boolean shouldScroll = index == 0;

        chatMessages.add(index, chatMessage);

        if (index != 0)
        {
            notifyDataSetChanged();
        }
        else
        {
            notifyItemInserted(1);
        }

        return shouldScroll;
    }

    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
    {
        View view;
        switch (viewType)
        {
            case TYPE_ME:
                view = LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_chat_me, parent, false);
                return new MyChatViewHolder(view);

            case TYPE_OTHERS:
                view = LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_chat_others, parent, false);
                return new OthersChatViewHolder(view);

            case TYPE_ADMIN:
                view = LayoutInflater
                        .from(context)
                        .inflate(R.layout.item_chat_admin, parent, false);
                return new AdminChatViewHolder(view);

            default:
                throw new IllegalStateException();
        }
    }

    @Override
    public void onBindViewHolder(RecyclerView.ViewHolder holder, int position)
    {
        switch (getItemViewType(position))
        {
            case TYPE_ME:
                ((MyChatViewHolder) holder).render(chatMessages.get(position));
                break;

            case TYPE_OTHERS:
                ((OthersChatViewHolder) holder).render(chatMessages.get(position));
                break;

            case TYPE_ADMIN:
                ((AdminChatViewHolder) holder).render(chatMessages.get(position));
        }
    }

    private int getInsertIndex(ChatMessage chatMessage)
    {
        int len = chatMessages.size();
        for (int i = 0; i < len; i++)
        {
            ChatMessage current = chatMessages.get(i);
            if (current.getTimestamp().isBefore(chatMessage.getTimestamp()))
            {
                return i;
            }
        }

        return len;
    }

    @Override
    public int getItemCount()
    {
        return chatMessages.size();
    }

    @Override
    public int getItemViewType(int position)
    {
        ChatMessage chatMessage = chatMessages.get(position);
        if (chatMessage.isAdmin())
        {
            return TYPE_ADMIN;
        }
        else
        {
            return chatMessage.getSenderId().equals(sessionUserId) ? TYPE_ME : TYPE_OTHERS;
        }
    }

    public class MyChatViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvChatMessage)
        TextView tvChatMessage;

        @Bind(R.id.tvTimestamp)
        TextView tvTimestamp;

        public MyChatViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(ChatMessage chatMessage)
        {
            int currentIndex = chatMessages.indexOf(chatMessage);
            int nextIndex = currentIndex + 1;
            if (nextIndex < chatMessages.size())
            {
                ChatMessage previousMessage = chatMessages.get(nextIndex);
                boolean isDateInvisible = previousMessage.getTimestamp().toLocalDate()
                                                         .equals(chatMessage.getTimestamp()
                                                                            .toLocalDate());
                if (isDateInvisible)
                {
                    tvTimestamp.setText(chatMessage.getTimestamp()
                                                   .toString(TIMESTAMP_FORMATTER_TIME_ONLY)
                                                   .toUpperCase());
                }
                else
                {
                    tvTimestamp.setText(chatMessage.getTimestamp().toString(TIMESTAMP_FORMATTER)
                                                   .toUpperCase());
                }
            }
            else
            {
                tvTimestamp.setText(chatMessage.getTimestamp().toString(TIMESTAMP_FORMATTER)
                                               .toUpperCase());
            }

            tvChatMessage.setText(chatMessage.getMessage());
        }
    }

    public class OthersChatViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvName)
        TextView tvName;

        @Bind(R.id.tvChatMessage)
        TextView tvChatMessage;

        @Bind(R.id.tvTimestamp)
        TextView tvTimestamp;

        public OthersChatViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(ChatMessage chatMessage)
        {
            int currentIndex = chatMessages.indexOf(chatMessage);
            int nextIndex = currentIndex + 1;

            if (nextIndex < chatMessages.size())
            {
                ChatMessage previousMessage = chatMessages.get(nextIndex);
                boolean isNameInvisible = previousMessage.getSenderId()
                                                         .equals(chatMessage.getSenderId());
                boolean isDateInvisible = previousMessage.getTimestamp().toLocalDate()
                                                         .equals(chatMessage.getTimestamp()
                                                                            .toLocalDate());

                if (isNameInvisible)
                {
                    tvName.setVisibility(View.GONE);
                }
                else
                {
                    tvName.setText(chatMessage.getSenderName());
                    tvName.setVisibility(View.VISIBLE);
                }

                if (isDateInvisible)
                {
                    tvTimestamp.setText(chatMessage.getTimestamp()
                                                   .toString(TIMESTAMP_FORMATTER_TIME_ONLY)
                                                   .toUpperCase());
                }
                else
                {
                    tvTimestamp.setText(chatMessage.getTimestamp().toString(TIMESTAMP_FORMATTER)
                                                   .toUpperCase());
                }
            }
            else
            {
                tvTimestamp.setText(chatMessage.getTimestamp().toString(TIMESTAMP_FORMATTER)
                                               .toUpperCase());
                tvName.setText(chatMessage.getSenderName());
                tvName.setVisibility(View.VISIBLE);
            }

            tvChatMessage.setText(chatMessage.getMessage());
        }
    }

    public class AdminChatViewHolder extends RecyclerView.ViewHolder
    {
        @Bind(R.id.tvChatMessage)
        TextView tvChatMessage;

        @Bind(R.id.tvTimestamp)
        TextView tvTimestamp;

        public AdminChatViewHolder(View itemView)
        {
            super(itemView);
            ButterKnife.bind(this, itemView);
        }

        public void render(ChatMessage chatMessage)
        {
            tvTimestamp.setText(chatMessage.getTimestamp().toString(TIMESTAMP_FORMATTER)
                                           .toUpperCase());
            tvChatMessage.setText(chatMessage.getMessage());
        }
    }
}
