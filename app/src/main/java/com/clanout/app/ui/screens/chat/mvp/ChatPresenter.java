package com.clanout.app.ui.screens.chat.mvp;

public interface ChatPresenter
{
    void attachView(ChatView view);

    void detachView();

    void retry();

    void send(String message);

    void loadMore();
}
