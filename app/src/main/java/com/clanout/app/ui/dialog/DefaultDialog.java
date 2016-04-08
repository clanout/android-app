package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.annotation.StringRes;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.clanout.R;

public class DefaultDialog
{
    public static final int BUTTON_DISABLED = -1;

    public interface Listener
    {
        void onPositiveButtonClicked();

        void onNegativeButtonClicked();
    }

    public static void show(final Activity activity, @StringRes int titleRes, @StringRes int messageRes,
                            int positiveButton, int negativeButton, boolean isCancelable,
                            final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(isCancelable);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_default, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

        tvTitle.setText(titleRes);
        tvMessage.setText(messageRes);

        if (positiveButton != BUTTON_DISABLED)
        {
            builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    listener.onPositiveButtonClicked();
                }
            });
        }

        if (negativeButton != BUTTON_DISABLED)
        {
            builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    listener.onNegativeButtonClicked();
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }

    public static void show(final Activity activity, String title, String message,
                            int positiveButton, int negativeButton, boolean isCancelable,
                            final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(isCancelable);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_default, null);
        builder.setView(dialogView);

        TextView tvTitle = (TextView) dialogView.findViewById(R.id.tvTitle);
        TextView tvMessage = (TextView) dialogView.findViewById(R.id.tvMessage);

        tvTitle.setText(title);
        tvMessage.setText(message);

        if (positiveButton != BUTTON_DISABLED)
        {
            builder.setPositiveButton(positiveButton, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    listener.onPositiveButtonClicked();
                }
            });
        }

        if (negativeButton != BUTTON_DISABLED)
        {
            builder.setNegativeButton(negativeButton, new DialogInterface.OnClickListener()
            {
                @Override
                public void onClick(DialogInterface dialog, int which)
                {
                    listener.onNegativeButtonClicked();
                    dialog.dismiss();
                }
            });
        }

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
