package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

import com.clanout.R;
import com.clanout.app.root.ClanOut;

public class NoInternetDialog
{
    public static void show(Activity activity)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(false);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_no_internet, null);
        builder.setView(dialogView);

        final AlertDialog dialog = builder.create();

        Button btnRetry = (Button) dialogView.findViewById(R.id.btnRetry);
        btnRetry.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                ConnectivityManager cm = (ConnectivityManager) ClanOut.getClanOutContext()
                                                                     .getSystemService(Context.CONNECTIVITY_SERVICE);
                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                if (netInfo != null && netInfo.isConnected())
                {
                    dialog.dismiss();
                }
            }
        });

        dialog.show();
    }
}
