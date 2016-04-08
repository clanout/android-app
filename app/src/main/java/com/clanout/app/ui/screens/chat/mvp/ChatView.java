package com.clanout.app.ui.screens.chat.mvp;

import com.clanout.app.model.ChatMessage;

public interface ChatView
{
    void displayTitle(String title);

    void displayMessage(ChatMessage chatMessage);

    void displaySendMessageFailureError();

    void displayError();

    void onHistoryLoaded();

    void displayNoMoreHistory();
}
