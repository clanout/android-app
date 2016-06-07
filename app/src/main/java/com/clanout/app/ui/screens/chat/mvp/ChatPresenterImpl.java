package com.clanout.app.ui.screens.chat.mvp;

import com.clanout.app.model.ChatMessage;
import com.clanout.app.model.Event;
import com.clanout.app.service.ChatService;
import com.clanout.app.service.EventService;
import com.clanout.app.service.NotificationService;
import com.clanout.app.service.UserService;

import org.joda.time.DateTime;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;

import retrofit.RetrofitError;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

public class ChatPresenterImpl implements ChatPresenter
{
    private ChatView view;
    private ChatService chatService;
    private UserService userService;
    private EventService eventService;
    private NotificationService notificationService;
    private String eventId;

    private Subscription chatSubscription;

    private List<ChatMessage> visibleChats;
    private int historyCount;
    private boolean isLoadHistoryInProgress;

    private boolean isChatSent;
    private ChatMessage chatMessage;
    private String eventTitle;

    private CompositeSubscription subscriptions;

    public ChatPresenterImpl(ChatService chatService, UserService userService, EventService eventService, NotificationService notificationService, String eventId)
    {
        this.chatService = chatService;
        this.userService = userService;
        this.eventService = eventService;
        this.notificationService = notificationService;
        this.eventId = eventId;

        visibleChats = new ArrayList<>();
        historyCount = 0;
        isLoadHistoryInProgress = false;
        isChatSent = false;

        subscriptions = new CompositeSubscription();
    }

    @Override
    public void attachView(final ChatView view)
    {
        this.view = view;
        chatService.updateLastSeen(eventId);
        initChat();

        eventService
                ._fetchEventCache(eventId)
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<Event>()
                {
                    @Override
                    public void onCompleted()
                    {
                    }

                    @Override
                    public void onError(Throwable e)
                    {
                    }

                    @Override
                    public void onNext(Event event)
                    {
                        if (view != null)
                        {
                            ChatPresenterImpl.this.eventTitle = event.getTitle();
                            view.displayTitle(event.getTitle());
                        }
                    }
                });

        fetchEventDetailsFromNetwork();
    }

    @Override
    public void detachView()
    {
        if (chatSubscription != null && !chatSubscription.isUnsubscribed())
        {
            chatSubscription.unsubscribe();
            chatSubscription = null;
        }

        chatService.leaveChat();
        view = null;

        if (isChatSent)
        {
            chatService.sendNotification(eventId, chatMessage);
        }

        chatService.updateLastSeen(eventId);
    }

    private void fetchEventDetailsFromNetwork()
    {
        Subscription subscription =
                eventService
                        ._fetchEventNetwork(eventId)
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Event>()
                        {
                            @Override
                            public void onCompleted()
                            {
                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                try
                                {
                                    if (((RetrofitError) e).getResponse().getStatus() == 404)
                                    {
                                        view.showPlanNotAvailableMessage();
                                        eventService.clearEventFromCache(eventId);

                                        notificationService.clearNotificationsForEvent(eventId);
                                    }
                                }
                                catch (Exception exception)
                                {

                                }
                            }

                            @Override
                            public void onNext(Event event)
                            {
                                if (event.isExpired())
                                {

                                    view.showPlanExpiredMessage();
                                    eventService.clearEventFromCache(event.getId());

                                    notificationService.clearNotificationsForEvent(eventId);
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void retry()
    {
        initChat();
    }

    @Override
    public void send(final String message)
    {
        chatMessage = buildChatMessage(message);

        if (view != null)
        {
            view.displayMessage(chatMessage);
            visibleChats.add(chatMessage);
        }

        chatService.post(chatMessage);

//        chatService
//                .post(chatMessage)
//                .subscribeOn(Schedulers.newThread())
//                .observeOn(AndroidSchedulers.mainThread())
//                .subscribe(new Subscriber<Object>()
//                {
//                    @Override
//                    public void onCompleted()
//                    {
//                        isChatSent = true;
//                    }
//
//                    @Override
//                    public void onError(Throwable e)
//                    {
//                        if (view != null)
//                        {
//                            view.displaySendMessageFailureError();
//                        }
//                    }
//
//                    @Override
//                    public void onNext(Object o)
//                    {
//                    }
//                });
    }

    @Override
    public void loadMore()
    {
//        view.displayNoMoreHistory();

        isLoadHistoryInProgress = true;
        historyCount++;

        if (chatSubscription != null && !chatSubscription.isUnsubscribed())
        {
            chatSubscription.unsubscribe();
            chatSubscription = null;
        }

        Observable.timer(2, TimeUnit.SECONDS)
                  .first()
                  .subscribeOn(Schedulers.newThread())
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Subscriber<Long>()
                  {
                      @Override
                      public void onCompleted()
                      {
                          if (view != null)
                          {
                              isLoadHistoryInProgress = false;
                              view.onHistoryLoaded();
                              view.displayNoMoreHistory();
                          }
                      }

                      @Override
                      public void onError(Throwable e)
                      {

                      }

                      @Override
                      public void onNext(Long aLong)
                      {

                      }
                  });

        chatSubscription =
                chatService
                        .fetchHistory(historyCount, visibleChats)
                        .subscribeOn(Schedulers.newThread())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<ChatMessage>()
                        {
                            @Override
                            public void onCompleted()
                            {

                            }

                            @Override
                            public void onError(Throwable e)
                            {
                                e.printStackTrace();
                                if (view != null)
                                {
                                    view.displayError();
                                }
                            }

                            @Override
                            public void onNext(ChatMessage chatMessage)
                            {
                                if (!visibleChats.contains(chatMessage))
                                {
                                    if (view != null)
                                    {
                                        visibleChats.add(chatMessage);
                                        view.displayMessage(chatMessage);
                                        chatService.updateLastSeen(eventId);

                                        if (isLoadHistoryInProgress)
                                        {
                                            isLoadHistoryInProgress = false;
                                            view.onHistoryLoaded();
                                        }
                                    }
                                }
                            }
                        });
    }

    /* Helper Methods */
    private void initChat()
    {
        if (chatSubscription != null && !chatSubscription.isUnsubscribed())
        {
            chatSubscription.unsubscribe();
            chatSubscription = null;
        }

        chatSubscription =
//                chatService
//                        .connect()
//                        .flatMap(new Func1<Boolean, Observable<ChatMessage>>()
//                        {
//                            @Override
//                            public Observable<ChatMessage> call(Boolean isConnected)
//                            {
//                                if (isConnected)
//                                {
//                                    return chatService.joinChat(eventId);
//                                }
//                                else
//                                {
//                                    throw new IllegalStateException();
//                                }
//                            }
//                        })
                chatService.joinChat(eventId)
                           .subscribeOn(Schedulers.newThread())
                           .observeOn(AndroidSchedulers.mainThread())
                           .subscribe(new Subscriber<ChatMessage>()
                           {
                               @Override
                               public void onCompleted()
                               {

                               }

                               @Override
                               public void onError(Throwable e)
                               {
                                   e.printStackTrace();
                                   if (view != null)
                                   {
                                       view.displayError();
                                   }
                               }

                               @Override
                               public void onNext(ChatMessage chatMessage)
                               {
                                   if (!visibleChats.contains(chatMessage))
                                   {
                                       if (view != null)
                                       {
                                           visibleChats.add(chatMessage);
                                           view.displayMessage(chatMessage);
                                           chatService.updateLastSeen(eventId);
                                       }
                                   }
                               }
                           });
    }

    public String generateMessageId()
    {
        return eventId + "_" + System.nanoTime();
    }

    public ChatMessage buildChatMessage(String message)
    {
        ChatMessage chatMessage = new ChatMessage();
        chatMessage.setId(generateMessageId());
        chatMessage.setMessage(message);
        chatMessage.setSenderId(userService.getSessionUserId());
        chatMessage.setSenderName(userService.getSessionUserName());
        chatMessage.setTimestamp(DateTime.now());
        chatMessage.setPlanId(eventId);
        chatMessage.setPlanTitle(eventTitle);
        return chatMessage;
    }
}
