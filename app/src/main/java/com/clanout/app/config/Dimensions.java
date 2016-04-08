package com.clanout.app.config;

import android.content.res.Resources;

/**
 * Created by Aditya on 15-09-2015.
 */
public final class Dimensions
{
    public static final int DP_TO_PX_MULTIPLIER = (int) (Resources.getSystem()
                                                                  .getDisplayMetrics().density);

    public static final int ACTION_BAR_DP = 24;

    /*
     ** CATEGORY ICON
     */

    // Used inside standard 48dp bubble
    public static final int CATEGORY_ICON_DEFAULT = 32;

    // Used in create screen
    public static final int CATEGORY_ICON_LARGE = 48;


    /*
     ** USER PROFILE PIC
     */

    // Used inside standard 48dp bubble
    public static final int PROFILE_PIC_DEFAULT = DP_TO_PX_MULTIPLIER * 48;

    // Used in account screen
    public static final int PROFILE_PIC_LARGE = DP_TO_PX_MULTIPLIER * 100;

    private Dimensions()
    {
    }
}
