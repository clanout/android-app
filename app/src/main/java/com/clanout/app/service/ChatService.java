package com.clanout.app.service;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.Event;
import com.clanout.app.model.util.DateTimeUtil;
import com.google.firebase.database.ChildEventListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;

import org.joda.time.DateTime;
import org.joda.time.DateTimeZone;

import java.util.List;

import rx.Observable;
import rx.Subscriber;
import rx.functions.Action1;
import rx.functions.Func1;
import rx.schedulers.Schedulers;
import timber.log.Timber;

public class ChatService
{
    private static final String CHAT_URI_PREFIX = "chat/";

    private static ChatService instance;

    public static void init(UserService userService, EventService eventService)
    {
        instance = new ChatService(userService, eventService);
    }

    public static ChatService getInstance()
    {
        if (instance == null)
        {
            throw new IllegalStateException("[ChatService Not Initialized]");
        }

        return instance;
    }

    private static final int DEFAULT_HISTORY_SIZE = 20;

    private UserService userService;
    private EventService eventService;

    private FirebaseDatabase connection;

    private String activeChatId;
    private DatabaseReference activeChat;
    private Query activeChatQuery;
    private ChildEventListener messageListener;

    private ChatService(UserService userService, EventService eventService)
    {
        this.userService = userService;
        this.eventService = eventService;

        connection = FirebaseDatabase.getInstance();
    }

    public Observable<ChatMessage> joinChat(final String planId)
    {
        activeChatId = planId;
        activeChat = connection.getReference(CHAT_URI_PREFIX + activeChatId);
        activeChatQuery = activeChat.limitToLast(DEFAULT_HISTORY_SIZE);

        return Observable
                .create(new Observable.OnSubscribe<String>()
                {
                    @Override
                    public void call(final Subscriber<? super String> subscriber)
                    {
                        messageListener = new ChildEventListener()
                        {
                            @Override
                            public void onChildAdded(DataSnapshot dataSnapshot, String s)
                            {
                                subscriber.onNext((String) dataSnapshot.getValue());
                            }

                            @Override
                            public void onChildChanged(DataSnapshot dataSnapshot, String s)
                            {

                            }

                            @Override
                            public void onChildRemoved(DataSnapshot dataSnapshot)
                            {

                            }

                            @Override
                            public void onChildMoved(DataSnapshot dataSnapshot, String s)
                            {

                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError)
                            {

                            }
                        };

                        activeChatQuery.addChildEventListener(messageListener);
                    }
                })
                .onBackpressureBuffer()
                .doOnError(new Action1<Throwable>()
                {
                    @Override
                    public void call(Throwable throwable)
                    {
                        /* Analytics */
                        AnalyticsHelper
                                .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_Z1, false);

                        throwable.printStackTrace();
                    }
                })
                .map(new Func1<String, ChatMessage>()
                {
                    @Override
                    public ChatMessage call(String json)
                    {
                        return map(json);
                    }
                })
                .filter(new Func1<ChatMessage, Boolean>()
                {
                    @Override
                    public Boolean call(ChatMessage chatMessage)
                    {
                        return chatMessage != null;
                    }
                })
                .subscribeOn(Schedulers.newThread());
    }

    public Observable<ChatMessage> fetchHistory(final int historySize, final List<ChatMessage>
            availableMessages)
    {
        if (activeChat == null)
        {
            return Observable.error(new IllegalStateException("[Chat not joined]"));
        }
        else
        {
            activeChatQuery = activeChat.limitToLast(DEFAULT_HISTORY_SIZE * (historySize + 1));

            return Observable
                    .create(new Observable.OnSubscribe<String>()
                    {
                        @Override
                        public void call(final Subscriber<? super String> subscriber)
                        {
                            messageListener = new ChildEventListener()
                            {
                                @Override
                                public void onChildAdded(DataSnapshot dataSnapshot, String s)
                                {
                                    subscriber.onNext((String) dataSnapshot.getValue());
                                }

                                @Override
                                public void onChildChanged(DataSnapshot dataSnapshot, String s)
                                {

                                }

                                @Override
                                public void onChildRemoved(DataSnapshot dataSnapshot)
                                {

                                }

                                @Override
                                public void onChildMoved(DataSnapshot dataSnapshot, String s)
                                {

                                }

                                @Override
                                public void onCancelled(DatabaseError databaseError)
                                {

                                }
                            };

                            activeChatQuery.addChildEventListener(messageListener);
                        }
                    })
                    .onBackpressureBuffer()
                    .doOnError(new Action1<Throwable>()
                    {
                        @Override
                        public void call(Throwable throwable)
                        {
                        /* Analytics */
                            AnalyticsHelper
                                    .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_A,
                                            false);
                                /* Analytics */

                            throwable.printStackTrace();
                        }
                    })
                    .map(new Func1<String, ChatMessage>()
                    {
                        @Override
                        public ChatMessage call(String json)
                        {
                            return map(json);
                        }
                    })
                    .filter(new Func1<ChatMessage, Boolean>()
                    {
                        @Override
                        public Boolean call(ChatMessage chatMessage)
                        {
                            return chatMessage != null && !availableMessages.contains(chatMessage);
                        }
                    })
                    .subscribeOn(Schedulers.newThread());
        }
    }

    public void post(ChatMessage message)
    {
        try
        {
            if (activeChat == null || activeChatId == null)
            {
                Timber.e("[No active chat]");
                throw new IllegalStateException("No active chat");
            }

            activeChat.push().setValue(map(message));
        }
        catch (Exception e)
        {
            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_B, false);
            /* Analytics */
        }
    }

    public void leaveChat()
    {
        if (activeChat != null)
        {
            try
            {
                activeChatQuery.removeEventListener(messageListener);
                messageListener = null;
                activeChatQuery = null;
                activeChat = null;
                activeChatId = null;
            }
            catch (Exception e)
            {
                /* Analytics */
                AnalyticsHelper
                        .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_LEAVE_CHAT_FAILED,
                                false);
                /* Analytics */

                Timber.e("[Leave Chat Failed] " + e.getMessage());
            }
        }
    }

    public void sendNotification(final String eventId, final ChatMessage chatMessage)
    {
        eventService
                ._fetchEvent(eventId)
                .flatMap(new Func1<Event, Observable<Boolean>>()
                {
                    @Override
                    public Observable<Boolean> call(Event event)
                    {
                        return eventService
                                ._sendChatNotification(eventId, chatMessage);
                    }
                })
                .subscribeOn(Schedulers.newThread())
                .subscribe(new Subscriber<Boolean>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {

                        /* Analytics */
                        AnalyticsHelper
                                .sendCaughtExceptions(GoogleAnalyticsConstants
                                        .METHOD_CHAT_NOTIFICATION_FAILED, false);
                        /* Analytics */

                        Timber.e("[Chat Notification Failed] " + e.getMessage());
                    }

                    @Override
                    public void onNext(Boolean aBoolean)
                    {
                    }
                });
    }

    public void updateLastSeen(String eventId)
    {
        eventService.updateChatLastSeen(eventId, DateTime.now());
    }

    /* Helper Methods */
    private ChatMessage map(String json)
    {
        try
        {
            ChatMessage chatMessage = GsonProvider.getGson().fromJson(json, ChatMessage.class);

            if (chatMessage.getSenderId() == null || chatMessage.getSenderId().isEmpty() ||
                    chatMessage.getSenderName() == null || chatMessage.getSenderName().isEmpty() ||
                    chatMessage.getMessage() == null || chatMessage.getMessage().isEmpty() ||
                    chatMessage.getTimestamp() == null)
            {
                return null;
            }

            if (chatMessage.isAdmin())
            {
                return processAdminMessage(chatMessage);
            }
            else
            {
                return chatMessage;
            }
        }
        catch (Exception e)
        {

            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_C, false);
            /* Analytics */

            return null;
        }
    }

    private String map(ChatMessage message)
    {
        return message.toString();
    }

    private String getNickname()
    {
        return userService.getSessionUserId();
    }

    private ChatMessage processAdminMessage(ChatMessage chatMessage)
    {
        try
        {
            String message = chatMessage.getMessage();
            String[] messageTokens = message.split(":");

            String typeToken = messageTokens[0];
            if (typeToken.equalsIgnoreCase("start_time"))
            {
                String user = messageTokens[1];
                String startTime = messageTokens[2];
                String localStartTime = DateTime.parse(startTime)
                                                .toDateTime(DateTimeZone.getDefault())
                                                .toString(DateTimeUtil.DATE_TIME_FORMATTER);
                chatMessage.setMessage(user + " updated start time to " + localStartTime);
            }
            else if (typeToken.equalsIgnoreCase("location"))
            {
                String user = messageTokens[1];
                String location = messageTokens[2];
                if (location.equalsIgnoreCase("0"))
                {
                    chatMessage.setMessage(user + " set location as undecided");
                }
                else
                {
                    chatMessage.setMessage(user + " updated location to " + location);
                }
            }
            else if (typeToken.equalsIgnoreCase("invitation_response"))
            {
                String name = messageTokens[1];
                String invitationResponse = messageTokens[2];
                chatMessage.setMessage(name + " is not joining.\n'" + invitationResponse + "'");
            }
            else if (typeToken.equalsIgnoreCase("rsvp"))
            {
                String name = messageTokens[1];
                String rsvp = messageTokens[2];
                if (rsvp.equalsIgnoreCase("YES"))
                {
                    chatMessage.setMessage(name + " joined");
                }
                else
                {
                    chatMessage.setMessage(name + " left");
                }
            }
            else if (typeToken.equalsIgnoreCase("description"))
            {
                String user = messageTokens[1];
                String description = messageTokens[2];
                if (description.equalsIgnoreCase("0"))
                {
                    chatMessage.setMessage(user + " removed the plan description");
                }
                else
                {
                    chatMessage
                            .setMessage(user + " updated the description\n'" + description + "'");
                }
            }
            else
            {
                return null;
            }

            return chatMessage;
        }
        catch (Exception e)
        {

            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_D, false);
            /* Analytics */

            return null;
        }
    }

}
