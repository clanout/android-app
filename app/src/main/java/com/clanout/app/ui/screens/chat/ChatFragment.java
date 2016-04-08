package com.clanout.app.ui.screens.chat;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.LinearLayoutManager;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.service.ChatService;
import com.clanout.app.service.EventService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui._core.BaseFragment;
import com.clanout.app.ui.screens.chat.mvp.ChatPresenter;
import com.clanout.app.ui.screens.chat.mvp.ChatPresenterImpl;
import com.clanout.app.ui.screens.chat.mvp.ChatView;
import com.clanout.app.ui.util.SnackbarFactory;
import com.clanout.app.ui.util.VisibilityAnimationUtil;
import com.jcodecraeer.xrecyclerview.ProgressStyle;
import com.jcodecraeer.xrecyclerview.XRecyclerView;

import net.steamcrafted.materialiconlib.MaterialIconView;

import butterknife.Bind;
import butterknife.ButterKnife;

public class ChatFragment extends BaseFragment implements ChatView
{
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static ChatFragment newInstance(String eventId)
    {
        ChatFragment fragment = new ChatFragment();

        Bundle args = new Bundle();
        args.putString(ARG_EVENT_ID, eventId);
        fragment.setArguments(args);

        return fragment;
    }

    ChatScreen screen;

    ChatPresenter presenter;

    /* UI Elements */
    @Bind(R.id.llTitleContainer)
    View llTitleContainer;

    @Bind(R.id.tvTitle)
    TextView tvTitle;

    @Bind(R.id.llError)
    View llError;

    @Bind(R.id.llChat)
    View llChat;

    @Bind(R.id.rvChat)
    XRecyclerView rvChat;

    @Bind(R.id.tvRetry)
    TextView tvRetry;

    @Bind(R.id.loading)
    ProgressBar loading;

    @Bind(R.id.etChatMessage)
    EditText etChatMessage;

    @Bind(R.id.mivSend)
    MaterialIconView mivSend;

    LinearLayoutManager linearLayoutManager;
    ChatAdapter chatAdapter;
    TextWatcher chatWatcher;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Presenter */
        ChatService chatService = ChatService.getInstance();
        UserService userService = UserService.getInstance();
        EventService eventService = EventService.getInstance();
        String eventId = getArguments().getString(ARG_EVENT_ID);
        presenter = new ChatPresenterImpl(chatService, userService, eventService, eventId);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (ChatScreen) getActivity();

        initRecyclerView();
        initChatBox();
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

        etChatMessage.removeTextChangedListener(chatWatcher);
        chatWatcher = null;

        tvRetry.setOnClickListener(null);

        presenter.detachView();
    }

    /* View Methods */

    @Override
    public void displayTitle(String title)
    {
        tvTitle.setText(title);
        VisibilityAnimationUtil.expand(llTitleContainer, 200);
    }

    @Override
    public void displayMessage(ChatMessage chatMessage)
    {
        boolean shouldScroll = chatAdapter.addMessage(chatMessage);
        if (shouldScroll)
        {
            linearLayoutManager.scrollToPositionWithOffset(linearLayoutManager.findFirstCompletelyVisibleItemPosition() - 1, 4);
        }

        llChat.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
    }

    @Override
    public void displaySendMessageFailureError()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_CHAT,
                GoogleAnalyticsConstants.ACTION_SEND, GoogleAnalyticsConstants.LABEL_FAILURE);
        /* Analytics */

        SnackbarFactory.create(getActivity(), R.string.error_chat_not_sent);
    }

    @Override
    public void displayError()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_CHAT, GoogleAnalyticsConstants.ACTION_DISPLAY_ERROR, null);
        /* Analytics */

        tvRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    tvRetry.setVisibility(View.GONE);
                    loading.setVisibility(View.VISIBLE);

                    presenter.retry();
                }
            }
        });

        tvRetry.setVisibility(View.VISIBLE);
        loading.setVisibility(View.GONE);
        llError.setVisibility(View.VISIBLE);
        llChat.setVisibility(View.GONE);
    }

    @Override
    public void onHistoryLoaded()
    {
        llChat.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        rvChat.loadMoreComplete();
    }

    @Override
    public void displayNoMoreHistory()
    {
        llChat.setVisibility(View.VISIBLE);
        llError.setVisibility(View.GONE);
        rvChat.noMoreLoading();
    }

    /* Helper Methods */
    private void initRecyclerView()
    {
        linearLayoutManager = new LinearLayoutManager(getActivity());
        linearLayoutManager.setReverseLayout(true);
        rvChat.setLayoutManager(linearLayoutManager);

        chatAdapter = new ChatAdapter(getActivity(), UserService.getInstance().getSessionUserId());
        rvChat.setAdapter(chatAdapter);

        rvChat.setPullRefreshEnabled(false);
        rvChat.setLaodingMoreProgressStyle(ProgressStyle.BallPulse);
        rvChat.setLoadingListener(new XRecyclerView.LoadingListener()
        {
            @Override
            public void onRefresh()
            {
            }

            @Override
            public void onLoadMore()
            {
                if (presenter != null)
                {
                    presenter.loadMore();
                }
            }
        });
    }

    private void initChatBox()
    {
        chatWatcher = new TextWatcher()
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
                    mivSend.setColor(ContextCompat.getColor(getActivity(), R.color.accent));
                }
                else
                {
                    mivSend.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));
                }
            }
        };
        etChatMessage.addTextChangedListener(chatWatcher);

        mivSend.setColor(ContextCompat.getColor(getActivity(), R.color.light_grey));
        mivSend.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_CHAT, GoogleAnalyticsConstants.ACTION_SEND, GoogleAnalyticsConstants.LABEL_ATTEMPT);
                /* Analytics */

                String message = etChatMessage.getText().toString();
                if (!TextUtils.isEmpty(message))
                {
                    etChatMessage.setText("");
                    if (presenter != null)
                    {
                        presenter.send(message);
                    }
                }
            }
        });
    }
}
