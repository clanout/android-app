package com.clanout.app.model.util;

import com.clanout.app.model.NotificationWrapper;

import java.util.Comparator;

public class NotificationComparator implements Comparator<NotificationWrapper>
{
    @Override
    public int compare(NotificationWrapper lhs, NotificationWrapper rhs)
    {
        if (lhs.getTimestamp().isAfter(rhs.getTimestamp()))
        {
            return -1;
        }
        else
        {
            return 1;
        }
    }
}
