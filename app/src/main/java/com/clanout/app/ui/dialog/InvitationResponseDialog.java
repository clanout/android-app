package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;

import com.clanout.R;

import java.util.Arrays;
import java.util.List;

public class InvitationResponseDialog
{
    public interface Listener
    {
        void onInvitationResponseSuggestionSelected(String suggestion);

        void onInvitationResponseEntered(String invitationResponse);

        void onInvitationResponseCancelled();
    }

    public static void show(Activity activity, final Listener listener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        View dialogView = layoutInflater.inflate(R.layout.dialog_invitation_response, null);
        builder.setView(dialogView);

        final EditText invitationResponse = (EditText) dialogView
                .findViewById(R.id.etInvitationResponse);
        ListView list = (ListView) dialogView.findViewById(R.id.lvInvitationResponse);

        final List<String> invitationResponses = Arrays.asList(activity.getResources().getStringArray(R.array.invitation_responses));

        ArrayAdapter<String> statusAdapter = new ArrayAdapter<>(activity, R.layout.item_text, invitationResponses);
        list.setAdapter(statusAdapter);
        list.setVisibility(View.VISIBLE);

        list.setOnItemClickListener(new AdapterView.OnItemClickListener()
        {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id)
            {
                invitationResponse.setText(invitationResponses.get(position));
                listener.onInvitationResponseSuggestionSelected(invitationResponses.get(position));
            }
        });

        builder.setPositiveButton(R.string.status_dialog_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                listener.onInvitationResponseEntered(invitationResponse.getText().toString());
                dialog.dismiss();
            }
        });

        builder.setOnCancelListener(new DialogInterface.OnCancelListener()
        {
            @Override
            public void onCancel(DialogInterface dialog)
            {
                listener.onInvitationResponseCancelled();
                dialog.dismiss();
            }
        });

        AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
