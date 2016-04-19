package com.clanout.app.ui.core;

import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;

import com.clanout.R;
import com.clanout.app.cache.core.CacheManager;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.config.MemoryCacheKeys;
import com.clanout.app.ui.dialog.NoInternetDialog;
import com.clanout.app.ui.screens.launch.LauncherActivity;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.functions.Func1;
import rx.schedulers.Schedulers;

public class BaseActivity extends AppCompatActivity
{
    private boolean isConnected;
    private Subscription noInternetSubscription;

    private void initConnectivityHandler()
    {
        isConnected = true;

        noInternetSubscription =
                Observable
                        .interval(5, TimeUnit.SECONDS)
                        .map(new Func1<Long, Boolean>()
                        {
                            @Override
                            public Boolean call(Long aLong)
                            {
                                ConnectivityManager cm = (ConnectivityManager) getApplicationContext()
                                        .getSystemService(Context.CONNECTIVITY_SERVICE);
                                NetworkInfo netInfo = cm.getActiveNetworkInfo();

                                return (netInfo != null && netInfo.isConnected());
                            }
                        })
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new Subscriber<Boolean>()
                        {
                            @Override
                            public void onCompleted()
                            {

                            }

                            @Override
                            public void onError(Throwable e)
                            {
                            }

                            @Override
                            public void onNext(Boolean isConnectedNow)
                            {
                                if (isConnected && !isConnectedNow)
                                {
                                    if (!(BaseActivity.this instanceof LauncherActivity))
                                    {
                                        NoInternetDialog.show(BaseActivity.this);

                                        /* Analytics */
                                        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants
                                                .SCREEN_NO_INTERNET);
                                    }
                                }

                                isConnected = isConnectedNow;
                            }
                        });

    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // Clear Notifications
        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        notificationManager.cancelAll();
    }

    @Override
    protected void onResume()
    {
        super.onResume();
        CacheManager.getMemoryCache().put(MemoryCacheKeys.IS_APP_IN_FOREGROUND, true);
        initConnectivityHandler();
    }

    @Override
    protected void onPause()
    {
        super.onPause();
        CacheManager.getMemoryCache().put(MemoryCacheKeys.IS_APP_IN_FOREGROUND, false);
        noInternetSubscription.unsubscribe();
    }

    protected void gotoAppSettings()
    {
        Intent intent = new Intent();
        intent.setAction(Settings.ACTION_APPLICATION_DETAILS_SETTINGS);
        Uri uri = Uri.fromParts("package", getPackageName(), null);
        intent.setData(uri);
        startActivity(intent);
    }

    protected void closeApp()
    {
        finish();
        System.exit(0);
    }

    /* Action Bar */
    private AppBarLayout appBarLayout;
    private Toolbar toolbar;

    protected void setActionBar(AppBarLayout _appBarLayout)
    {
        appBarLayout = _appBarLayout;
        toolbar = (Toolbar) appBarLayout.findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
    }

    protected void showActionBar()
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().show();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //Hide Shadow for lower version
            View view = findViewById(R.id.ivShadow);
            if (view != null)
            {
                view.setVisibility(View.VISIBLE);
            }
        }
    }

    protected void hideActionBar()
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().hide();
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP)
        {
            //Hide Shadow for lower version
            View view = findViewById(R.id.ivShadow);
            if (view != null)
            {
                view.setVisibility(View.GONE);
            }
        }
    }

    protected void setScreenTitle(@StringRes int title)
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().setTitle(title);
    }

    protected void setScreenTitle(String title)
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().setTitle(title);
    }

    protected void setActionBarBackVisibility(boolean isVisible)
    {
        if (getSupportActionBar() == null)
        {
            return;
        }

        getSupportActionBar().setDisplayHomeAsUpEnabled(isVisible);
    }
}
