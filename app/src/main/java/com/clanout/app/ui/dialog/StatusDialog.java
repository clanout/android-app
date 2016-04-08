package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.clanout.R;

public class StatusDialog
{
    public interface Listener
    {
        void onStatusEntered(String status);

        void onStatusCancelled();
    }

    public static void show(Activity activity, String oldStatus, final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_status, null);
        builder.setView(dialogView);

        TextView message = (TextView) dialogView.findViewById(R.id.tvMessage);
        final EditText status = (EditText) dialogView.findViewById(R.id.etStatus);

        message.setText(R.string.status_dialog_message);

        if (oldStatus == null || oldStatus.isEmpty())
        {
            status.setHint(R.string.status_default);
        }
        else
        {
            status.setText(oldStatus);
            status.setSelection(oldStatus.length());
        }

        builder.setPositiveButton(R.string.status_dialog_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                listener.onStatusEntered(status.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                listener.onStatusCancelled();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
