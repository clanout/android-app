package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.clanout.R;

import java.util.Arrays;
import java.util.List;

public class LastMinuteStatusDialog
{
    public interface Listener
    {
        void onLastMinuteStatusSuggestionSelected(String suggestion);

        void onLastMinuteStatusEntered(String status);

        void onLastMinuteStatusCancelled();
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
        ListView list = (ListView) dialogView.findViewById(R.id.lvStatus);

        final List<String> statusList = Arrays.asList(activity.getResources().getStringArray(R.array.last_moment_status));

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(activity, R.layout.item_text, statusList);
        list.setAdapter(statusAdapter);
        list.setVisibility(View.VISIBLE);

        message.setText(R.string.status_dialog_message_last_minute);

        if (oldStatus == null || oldStatus.isEmpty())
        {
            status.setHint(R.string.status_default);
        }
        else
        {
            status.setText(oldStatus);
            status.setSelection(oldStatus.length());
        }

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                status.setText(statusList.get(position));
                listener.onLastMinuteStatusSuggestionSelected(statusList.get(position));
            }
        });

        builder.setPositiveButton(R.string.status_dialog_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                listener.onLastMinuteStatusEntered(status.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                listener.onLastMinuteStatusCancelled();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();
    }
}
