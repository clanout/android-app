package com.clanout.app.ui.util;

import android.app.Activity;
import android.graphics.Color;
import android.support.annotation.StringRes;
import android.support.design.widget.Snackbar;
import android.view.View;
import android.widget.TextView;

public class SnackbarFactory
{
    public static Snackbar create(Activity activity, @StringRes int resId)
    {
        Snackbar snackbar = Snackbar.make(activity.findViewById(android.R.id.content), activity
                .getString(resId), Snackbar.LENGTH_SHORT);

        View snackBarView = snackbar.getView();
        TextView tv = (TextView) snackBarView
                .findViewById(android.support.design.R.id.snackbar_text);
        tv.setTextColor(Color.WHITE);

        snackbar.show();
        return snackbar;
    }
}
