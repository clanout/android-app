package com.clanout.app.cache.user;

import com.clanout.app.model.Friend;

import java.util.List;

import rx.Observable;

public interface UserCache
{
    Observable<List<Friend>> getFriends();

    Observable<List<Friend>> getContacts();

    void saveFriends(List<Friend> friends);

    void saveContacts(List<Friend> contacts);

    void deleteFriends();

    void deleteContacts();
}
