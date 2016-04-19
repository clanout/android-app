package com.clanout.app.service;

import com.clanout.app.api.core.GsonProvider;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.Event;
import com.clanout.app.model.util.DateTimeUtil;

import org.jivesoftware.smack.AbstractConnectionClosedListener;
import org.jivesoftware.smack.AbstractXMPPConnection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.tcp.XMPPTCPConnection;
import org.jivesoftware.smack.tcp.XMPPTCPConnectionConfiguration;
import org.jivesoftware.smackx.muc.DiscussionHistory;
import org.jivesoftware.smackx.muc.MultiUserChat;
import org.jivesoftware.smackx.muc.MultiUserChatManager;
import org.jivesoftware.smackx.ping.PingFailedListener;
import org.jivesoftware.smackx.ping.PingManager;
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

    /* Xmpp connection */
    private AbstractXMPPConnection connection;
    private PingManager pingManager;
    private boolean isHealthy;

    /* Clan chat */
    private String activeChat;
    private MultiUserChat chat;

    /* Message Listener */
    private MessageListener messageListener;

    private ChatService(UserService userService, EventService eventService)
    {
        this.userService = userService;
        this.eventService = eventService;
        isHealthy = false;
        activeChat = null;
        chat = null;

        initConnection();
    }

    public Observable<Boolean> connect()
    {
        if (isHealthy)
        {
            Timber.v("[XmppConnection already established]");
            return Observable.just(true);
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<Boolean>()
                    {
                        @Override
                        public void call(Subscriber<? super Boolean> subscriber)
                        {
                            try
                            {
                                if (!connection.isConnected())
                                {
                                    connection.connect();
                                }

                                if (!connection.isAuthenticated())
                                {
                                    connection.login();
                                }

                                pingManager = PingManager.getInstanceFor(connection);
                                pingManager.setPingInterval(10);
                                pingManager.registerPingFailedListener(new PingFailedListener()
                                {
                                    @Override
                                    public void pingFailed()
                                    {
                                        Timber.v("[Xmpp Ping Failed]");
                                    }
                                });

                                Timber.v("[XmppConnection established]");
                                isHealthy = true;
                                subscriber.onNext(true);
                                subscriber.onCompleted();
                            }
                            catch (Exception e)
                            {

                                /* Analytics */
                                AnalyticsHelper
                                        .sendCaughtExceptions(GoogleAnalyticsConstants
                                                .METHOD_XMPP_CONNECTION_FAILED, false);
                                /* Analytics */

                                Timber.v("[XmppConnection Connection Failed] " + e.getMessage());
                                subscriber.onNext(false);
                                subscriber.onCompleted();
                            }
                        }
                    })
                    .subscribeOn(Schedulers.io());
        }
    }

    public Observable<ChatMessage> joinChat(final String eventId)
    {
        return Observable
                .create(new Observable.OnSubscribe<Message>()
                {
                    @Override
                    public void call(final Subscriber<? super Message> subscriber)
                    {
                        leaveChat();

                        MultiUserChatManager manager = MultiUserChatManager
                                .getInstanceFor(connection);
                        chat = manager
                                .getMultiUserChat(eventId + AppConstants.CHAT_POSTFIX);

                        DiscussionHistory history = new DiscussionHistory();
                        history.setMaxStanzas(DEFAULT_HISTORY_SIZE);

                        messageListener = new MessageListener()
                        {
                            @Override
                            public void processMessage(Message message)
                            {
                                subscriber.onNext(message);
                            }
                        };
                        chat.addMessageListener(messageListener);

                        try
                        {
                            chat.join(getNickname(), null, history, connection
                                    .getPacketReplyTimeout());
                            activeChat = eventId;
                        }
                        catch (Exception e)
                        {

                            /* Analytics */
                            AnalyticsHelper
                                    .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_UNABLE_TO_JOIN_CHAT, false);
                            /* Analytics */

                            subscriber
                                    .onError(new Exception("[Unable to join clan chat] " + e
                                            .getMessage()));
                        }
                    }
                })
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
                .map(new Func1<Message, ChatMessage>()
                {
                    @Override
                    public ChatMessage call(Message message)
                    {
                        return map(message);
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

    public Observable<ChatMessage> fetchHistory(final int historySize, final List<ChatMessage> availableMessages)
    {
        if (chat == null)
        {
            return Observable.error(new IllegalStateException("[Chat not joined]"));
        }
        else
        {
            return Observable
                    .create(new Observable.OnSubscribe<Message>()
                    {
                        @Override
                        public void call(final Subscriber<? super Message> subscriber)
                        {
                            try
                            {
                                chat.removeMessageListener(messageListener);
                                messageListener = null;
                                chat.leave();

                                DiscussionHistory history = new DiscussionHistory();
                                history.setMaxStanzas(DEFAULT_HISTORY_SIZE * (historySize + 1));

                                messageListener = new MessageListener()
                                {
                                    @Override
                                    public void processMessage(Message message)
                                    {
                                        subscriber.onNext(message);
                                    }
                                };
                                chat.addMessageListener(messageListener);

                                chat.join(getNickname(), null, history, connection
                                        .getPacketReplyTimeout());
                            }
                            catch (Exception e)
                            {
                                /* Analytics */
                                AnalyticsHelper
                                        .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_A, false);
                                /* Analytics */

                                subscriber.onError(e);
                            }
                        }
                    })
                    .map(new Func1<Message, ChatMessage>()
                    {
                        @Override
                        public ChatMessage call(Message message)
                        {
                            return map(message);
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

    public Observable<Object> post(ChatMessage message)
    {
        if (activeChat == null || chat == null)
        {
            Timber.e("[No active chat]");
            return Observable.error(new Exception("No active chat"));
        }

        try
        {
            chat.sendMessage(map(message));
            return Observable.empty();
        }
        catch (SmackException.NotConnectedException e)
        {

            /* Analytics */
            AnalyticsHelper.sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_B, false);
            /* Analytics */
            return Observable.error(e);
        }
    }

    public void leaveChat()
    {
        if (chat != null)
        {
            try
            {
                chat.removeMessageListener(messageListener);
                chat.leave();
                messageListener = null;
                activeChat = null;
            }
            catch (SmackException.NotConnectedException e)
            {

                /* Analytics */
                AnalyticsHelper
                        .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_LEAVE_CHAT_FAILED, false);
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
                                .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_CHAT_NOTIFICATION_FAILED, false);
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
    private void initConnection()
    {
        String userId = userService.getSessionUserId();

        XMPPTCPConnectionConfiguration configuration =
                XMPPTCPConnectionConfiguration
                        .builder()
                        .setUsernameAndPassword(userId, userId)
                        .setServiceName(AppConstants.CHAT_SERVICE_NAME)
                        .setSecurityMode(ConnectionConfiguration.SecurityMode.disabled)
                        .setHost(AppConstants.CHAT_SERVICE_HOST)
                        .setPort(AppConstants.CHAT_SERVICE_PORT)
                        .build();

        connection = new XMPPTCPConnection(configuration);

        connection.addConnectionListener(new AbstractConnectionClosedListener()
        {
            @Override
            public void connectionTerminated()
            {
                Timber.v("[XmppConnection Terminated]");
                isHealthy = false;
            }
        });

        connect()
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
                                .sendCaughtExceptions(GoogleAnalyticsConstants.METHOD_CHAT_CONNECT_FAILED, false);
                            /* Analytics */
                    }

                    @Override
                    public void onNext(Boolean isCnnected)
                    {
                    }
                });
    }

    private ChatMessage map(Message message)
    {
        try
        {
            ChatMessage chatMessage = GsonProvider.getGson()
                                                  .fromJson(message.getBody(), ChatMessage.class);

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

    private Message map(ChatMessage message)
    {
        Message msg = new Message();
        msg.setStanzaId(message.getId());
        msg.setBody(GsonProvider.getGson().toJson(message));
        return msg;
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
