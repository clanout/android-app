package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.design.widget.TextInputLayout;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.RadioGroup;

import com.clanout.R;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.util.SoftKeyboardHandler;

public class FeedbackDialog
{
    public interface Listener
    {
        void onSuccess(int feedbackType, String comment);

        void onCancel();
    }

    public static void show(final Activity activity, final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_feedback, null);
        builder.setView(dialogView);

        final EditText commentMessage = (EditText) dialogView
                .findViewById(R.id.etComment);
        final RadioGroup radioGroup = (RadioGroup) dialogView
                .findViewById(R.id.rgFeedbackType);
        final TextInputLayout tilFeedbackMessage = (TextInputLayout) dialogView
                .findViewById(R.id.tilFeedbackMessage);

        commentMessage.addTextChangedListener(new TextWatcher()
        {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after)
            {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count)
            {

            }

            @Override
            public void afterTextChanged(Editable s)
            {
                tilFeedbackMessage.setError("");
                tilFeedbackMessage.setErrorEnabled(false);
            }
        });

        builder.setPositiveButton(R.string.feedback_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialogInterface, int i)
            {
            }
        });

        builder.setNegativeButton(R.string.feedback_remind_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
                if (listener != null)
                {
                    listener.onCancel();
                }

                dialog.dismiss();
            }
        });

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

        alertDialog
                .getButton(AlertDialog.BUTTON_POSITIVE)
                .setOnClickListener(new View.OnClickListener()
                {
                    @Override
                    public void onClick(View v)
                    {
                        int type = 0;
                        switch (radioGroup.getCheckedRadioButtonId())
                        {
                            case R.id.rbBug:
                                type = 0;
                                break;
                            case R.id.rbFeature:
                                type = 1;
                                break;
                            case R.id.rbOthers:
                                type = 2;
                                break;
                        }

                        String comment = commentMessage.getText().toString();

                        if (TextUtils.isEmpty(comment))
                        {
                            tilFeedbackMessage
                                    .setError(activity
                                            .getString(R.string.feedback_empty_comment));
                            tilFeedbackMessage.setErrorEnabled(true);
                        }
                        else
                        {
                            if (listener != null)
                            {
                                listener.onSuccess(type, comment);
                            }

                            UserService.getInstance().shareFeedback(type, comment);
                            SoftKeyboardHandler.hideKeyboard(activity, dialogView);
                            alertDialog.dismiss();
                        }
                    }
                });
    }
}
