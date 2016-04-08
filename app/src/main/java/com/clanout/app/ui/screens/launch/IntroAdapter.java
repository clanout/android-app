package com.clanout.app.ui.screens.launch;


import android.app.Fragment;
import android.app.FragmentManager;
import android.support.v13.app.FragmentStatePagerAdapter;

public class IntroAdapter extends FragmentStatePagerAdapter
{
    public IntroAdapter(FragmentManager fm)
    {
        super(fm);
    }

    @Override
    public Fragment getItem(int position)
    {
        return IntroFragment.newInstance(position);
    }

    @Override
    public int getCount()
    {
        return 4;
    }
}
