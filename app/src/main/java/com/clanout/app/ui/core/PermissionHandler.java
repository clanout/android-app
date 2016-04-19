package com.clanout.app.ui.core;

import android.Manifest;
import android.app.Activity;
import android.support.annotation.IntDef;
import android.support.v4.app.ActivityCompat;

public class PermissionHandler
{
    @IntDef({Permissions.LOCATION, Permissions.READ_CONTACTS})
    public @interface Permissions
    {
        int LOCATION = 1;
        int READ_CONTACTS = 2;
    }

    public static boolean isRationalRequired(Activity activity, @Permissions int permission)
    {
        return ActivityCompat
                .shouldShowRequestPermissionRationale(activity, getPermissionString(permission));
    }

    public static void requestPermission(Activity activity, @Permissions int permission)
    {
        ActivityCompat
                .requestPermissions(activity, new String[]{getPermissionString(permission)},
                        permission);
    }

    private static String getPermissionString(@Permissions int permission)
    {
        switch (permission)
        {
            case Permissions.LOCATION:
                return Manifest.permission.ACCESS_COARSE_LOCATION;

            case Permissions.READ_CONTACTS:
                return Manifest.permission.READ_CONTACTS;

            default:
                throw new IllegalArgumentException("Invalid Permission");
        }
    }

    public interface Listener
    {
        void onPermissionGranted(@Permissions int permission);

        void onPermissionDenied(@Permissions int permission);

        void onPermissionPermanentlyDenied(@Permissions int permission);
    }
}
