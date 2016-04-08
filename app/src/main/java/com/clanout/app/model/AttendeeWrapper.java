package com.clanout.app.model;

import java.util.List;

/**
 * Created by harsh on 08/04/16.
 */
public class AttendeeWrapper
{
    private Attendee attendee;
    private List<String> inviters;
    private List<String> friends;

    public AttendeeWrapper(Attendee attendee, List<String> inviters, List<String> friends)
    {
        this.attendee = attendee;
        this.inviters = inviters;
        this.friends = friends;
    }

    public Attendee getAttendee()
    {
        return attendee;
    }

    public List<String> getInviters()
    {
        return inviters;
    }

    public List<String> getFriends()
    {
        return friends;
    }

    public boolean isInviter()
    {
        if(inviters.contains(attendee.getId()))
        {
            return true;
        }else{

            return false;
        }
    }

    public boolean isFriend()
    {
        if(friends.contains(attendee.getId()))
        {
            return true;
        }else{

            return false;
        }
    }
}
