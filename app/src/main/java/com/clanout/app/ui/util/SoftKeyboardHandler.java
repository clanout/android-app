package com.clanout.app.ui.util;

import android.app.Activity;
import android.content.Context;
import android.view.View;
import android.view.inputmethod.InputMethodManager;

import timber.log.Timber;

/**
 * Created by harsh on 01/11/15.
 */
public class SoftKeyboardHandler {

    public static void hideKeyboard(Activity activity, View view) {
        if (activity != null) {

            try {
                InputMethodManager inputManager = (InputMethodManager) activity.getSystemService(Context.INPUT_METHOD_SERVICE);
                inputManager.hideSoftInputFromWindow(view.getWindowToken(), InputMethodManager.HIDE_NOT_ALWAYS);
            } catch (Exception e) {
                Timber.d("Exception while closing keyboard");
            }
        }
    }
}
