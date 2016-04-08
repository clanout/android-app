package com.clanout.app.model.util;

import com.clanout.app.model.AttendeeWrapper;

import java.util.Comparator;

public class EventAttendeeComparator implements Comparator<AttendeeWrapper>
{
    @Override
    public int compare(AttendeeWrapper first, AttendeeWrapper second)
    {
        if (first.isInviter() && second.isInviter())
        {
            if (first.isFriend() && !second.isFriend())
            {
                return -1;
            }
            else if (!first.isFriend() && second.isFriend())
            {
                return 1;
            }
            else
            {
                return first.getAttendee().getName().compareTo(second.getAttendee().getName());
            }
        }
        else if (first.isInviter() && !second.isInviter())
        {
            return -1;
        }
        else if (!first.isInviter() && second.isInviter())
        {
            return 1;
        }
        else
        {
            return first.getAttendee().getName().compareTo(second.getAttendee().getName());
        }
    }
}
