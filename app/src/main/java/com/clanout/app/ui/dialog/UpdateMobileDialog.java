package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.content.DialogInterface;
import android.support.v7.app.AlertDialog;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.config.AppConstants;
import com.clanout.app.model.util.PhoneUtils;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.util.SoftKeyboardHandler;

public class UpdateMobileDialog
{
    public interface Listener
    {
        void onSuccess(String mobileNumber);
    }

    public static void show(final Activity activity, final Listener listener)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater inflater = activity.getLayoutInflater();
        final View dialogView = inflater.inflate(R.layout.dialog_update_mobile, null);
        builder.setView(dialogView);

        final EditText phoneNumber = (EditText) dialogView
                .findViewById(R.id.etMobileNumber);

        final TextView tvInvalidPhoneError = (TextView) dialogView
                .findViewById(R.id.tvInvalidPhoneError);

        String mobileNumber = "";
        try
        {
            mobileNumber = UserService.getInstance().getSessionUser().getMobileNumber()
                                      .replace("+91", "");
        }
        catch (Exception e)
        {
        }
        phoneNumber.setText(mobileNumber);
        phoneNumber.setSelection(phoneNumber.length());

        phoneNumber.addTextChangedListener(new TextWatcher()
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
                tvInvalidPhoneError.setVisibility(View.INVISIBLE);
            }
        });

        builder.setPositiveButton(R.string.add_phone_positive_button, new DialogInterface.OnClickListener()
        {
            @Override
            public void onClick(DialogInterface dialog, int which)
            {
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
                        String rawInput = phoneNumber.getText().toString();
                        String parsedPhone = PhoneUtils
                                .parsePhone(rawInput, AppConstants.DEFAULT_COUNTRY_CODE);

                        if (parsedPhone == null)
                        {
                            tvInvalidPhoneError.setVisibility(View.VISIBLE);
                        }
                        else
                        {
                            if (listener != null)
                            {
                                listener.onSuccess(parsedPhone);
                            }

                            UserService.getInstance().updatePhoneNumber(parsedPhone);
                            SoftKeyboardHandler.hideKeyboard(activity, dialogView);
                            alertDialog.dismiss();
                        }
                    }
                });
    }
}
