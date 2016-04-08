package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.clanout.R;

public class DescriptionDialog
{
    public static void show(Activity activity, String description)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_description, null);
        builder.setView(dialogView);

        TextView tvDescription = (TextView) dialogView.findViewById(R.id.tvDescription);
        tvDescription.setText(description);

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
