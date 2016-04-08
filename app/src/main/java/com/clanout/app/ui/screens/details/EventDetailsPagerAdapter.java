package com.clanout.app.ui.screens.details;

import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

import com.clanout.app.model.Event;

import java.util.List;

public class EventDetailsPagerAdapter extends FragmentStatePagerAdapter
{
    List<Event> events;

    public EventDetailsPagerAdapter(FragmentManager fm, List<Event> events)
    {
        super(fm);
        this.events = events;
    }

    @Override
    public Fragment getItem(int position)
    {
        return EventDetailsFragment.newInstance(events.get(position));
    }

    @Override
    public int getCount()
    {
        return events.size();
    }
}
