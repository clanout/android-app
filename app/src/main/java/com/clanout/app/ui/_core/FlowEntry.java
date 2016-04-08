package com.clanout.app.ui._core;

import android.support.annotation.IntDef;

@IntDef({
        FlowEntry.HOME,
        FlowEntry.DETAILS,
        FlowEntry.NOTIFICATIONS,
        FlowEntry.CHAT
})
public @interface FlowEntry
{
    int HOME = 0;
    int DETAILS = 1;
    int NOTIFICATIONS = 2;
    int CHAT = 3;
}
