package com.clanout.app.ui.screens.launch;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewPager;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.service.AuthService;
import com.clanout.app.service.EventService;
import com.clanout.app.service.FacebookService;
import com.clanout.app.service.GcmService;
import com.clanout.app.service.GoogleService;
import com.clanout.app.service.LocationService;
import com.clanout.app.service.NotificationService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui._core.BaseActivity;
import com.clanout.app.ui._core.FlowEntry;
import com.clanout.app.ui._core.PermissionHandler;
import com.clanout.app.ui.dialog.DefaultDialog;
import com.clanout.app.ui.screens.chat.ChatActivity;
import com.clanout.app.ui.screens.details.EventDetailsActivity;
import com.clanout.app.ui.screens.home.HomeActivity;
import com.clanout.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenter;
import com.clanout.app.ui.screens.launch.mvp.bootstrap.BootstrapPresenterImpl;
import com.clanout.app.ui.screens.launch.mvp.bootstrap.BootstrapView;
import com.clanout.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenter;
import com.clanout.app.ui.screens.launch.mvp.fb_login.FacebookLoginPresenterImpl;
import com.clanout.app.ui.screens.launch.mvp.fb_login.FacebookLoginView;
import com.clanout.app.ui.screens.notifications.NotificationActivity;
import com.clanout.app.ui.screens.pending_invites.PendingInvitesActivity;
import com.clanout.app.ui.screens.unknown_zone.UnKnownZoneActivity;
import com.clanout.app.ui.util.SnackbarFactory;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginManager;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.LocationServices;

import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import timber.log.Timber;


public class LauncherActivity extends BaseActivity implements
        FacebookCallback<LoginResult>,
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener,
        PermissionHandler.Listener,
        FacebookLoginView,
        BootstrapView
{
    private static final String ARG_FLOW_ENTRY = "arg_flow_entry";
    private static final String ARG_EVENT_ID = "arg_event_id";

    public static Intent callingIntent(Context context, @FlowEntry int flowEntry, String eventId)
    {
        Intent intent = new Intent(context, LauncherActivity.class);

        intent.putExtra(ARG_FLOW_ENTRY, flowEntry);
        intent.putExtra(ARG_EVENT_ID, eventId);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TOP);

        return intent;
    }

    FacebookLoginPresenter facebookLoginPresenter;

    BootstrapPresenter bootstrapPresenter;

    /* UI Elements */
    @Bind(R.id.rlBootstrap)
    View rlBootstrap;

    @Bind(R.id.llFb)
    View llFb;

    @Bind(R.id.vpIntro)
    ViewPager vpIntro;

    @Bind(R.id.btnFbLogin)
    LoginButton btnFbLogin;

    @Bind(R.id.ivIntro1)
    ImageView ivIntro1;

    @Bind(R.id.ivIntro2)
    ImageView ivIntro2;

    @Bind(R.id.ivIntro3)
    ImageView ivIntro3;

    @Bind(R.id.ivIntro4)
    ImageView ivIntro4;

    @Bind(R.id.loading)
    LinearLayout loading;

    @Bind(R.id.llAction)
    View llAction;

    @Bind(R.id.tvActionMessage)
    TextView tvActionMessage;

    @Bind(R.id.tvAction)
    TextView tvAction;

    @Bind(R.id.tvuserAgreement)
    TextView userAgreement;

    /* Fields */
    CallbackManager facebookCallbackManager;

    GoogleService googleService;

    boolean introScrolled;

    /* Lifecycle Methods */
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_LAUNCHER_ACTIVITY);

        setContentView(R.layout.activity_launcher);
        ButterKnife.bind(this);

        if (llFb == null)
        {
            throw new IllegalStateException();
        }

        /* Init View */
        displayBlankView();

        facebookCallbackManager = CallbackManager.Factory.create();

        /* Google Service */
        googleService = GoogleService.getInstance();

        /* Facebook Service */
        FacebookService facebookService = FacebookService.getInstance();

        /* Location Service */
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        LocationService.init(getApplicationContext(), locationManager, googleService);
        LocationService locationService = LocationService.getInstance();

        /* User Service */
        UserService userService = UserService.getInstance();

        /* Auth Service */
        AuthService.init(facebookService, userService);
        AuthService authService = AuthService.getInstance();

        /* Gcm Service */
        GcmService gcmService = GcmService.getInstance();

        /* Event Service */
        EventService eventService = EventService.getInstance();

        /* Presenters */
        facebookLoginPresenter = new FacebookLoginPresenterImpl(authService, facebookService);
        bootstrapPresenter = new BootstrapPresenterImpl(locationService, authService, gcmService, userService, eventService);
    }

    @Override
    protected void onStart()
    {
        super.onStart();
        setupGooglePlayService();
    }

    @Override
    protected void onStop()
    {
        super.onStop();
        facebookLoginPresenter.detachView();
        bootstrapPresenter.detachView();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data)
    {
        super.onActivityResult(requestCode, resultCode, data);
        facebookCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults)
    {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (requestCode == PermissionHandler.Permissions.LOCATION)
        {
            if (grantResults[0] == PackageManager.PERMISSION_DENIED)
            {
                if (PermissionHandler
                        .isRationalRequired(this, PermissionHandler.Permissions.LOCATION))
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_LOCATION_PERMISSION,GoogleAnalyticsConstants.LABEL_DENIED);
                    /* Analytics */

                    onPermissionDenied(PermissionHandler.Permissions.LOCATION);
                }
                else
                {
                    /* Analytics */
                    AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_LOCATION_PERMISSION,GoogleAnalyticsConstants.LABEL_PERMANENTLY_DENIED);
                    /* Analytics */

                    onPermissionPermanentlyDenied(PermissionHandler.Permissions.LOCATION);
                }
            }
            else
            {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_LOCATION_PERMISSION,GoogleAnalyticsConstants.LABEL_GRANTED);
                /* Analytics */

                onPermissionGranted(PermissionHandler.Permissions.LOCATION);
            }
        }
    }

    /* Permission Handling */
    @Override
    public void onPermissionGranted(@PermissionHandler.Permissions int permission)
    {
        bootstrapPresenter.attachView(this);
    }

    @Override
    public void onPermissionDenied(@PermissionHandler.Permissions int permission)
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.permission_location_message);
        tvAction.setText(R.string.permission_request_again);
        tvAction.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,
                        GoogleAnalyticsConstants.ACTION_REQUEST_LOCATION_PERMISSION_AGAIN, null);
                /* Analytics */

                PermissionHandler
                        .requestPermission(LauncherActivity.this, PermissionHandler.Permissions
                                .LOCATION);
            }
        });
    }

    @Override
    public void onPermissionPermanentlyDenied(@PermissionHandler.Permissions int permission)
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.permission_location_message);
        tvAction.setText(R.string.permission_goto_settings);
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_GO_TO_SETTINGS,null);
                /* Analytics */


                LauncherActivity.this.gotoAppSettings();
            }
        });
    }

    /* View Methods (FacebookLogonView) */
    @Override
    public void displayFacebookLoginButton()
    {
        displayFbLoginView();

        btnFbLogin.setVisibility(View.VISIBLE);
        btnFbLogin.setReadPermissions(FacebookService.PERMISSIONS);
        btnFbLogin.registerCallback(facebookCallbackManager, this);
    }

    @Override
    public void displayFacebookLoginError()
    {
        SnackbarFactory.create(this, R.string.error_facebook_login);
    }

    @Override
    public void displayFacebookPermissionsMessage()
    {
        displayFacebookPermissionsDialog();
    }

    @Override
    public void proceedToSessionValidation()
    {
        displayBootstrapView();
        facebookLoginPresenter.detachView();
        bootstrapPresenter.attachView(this);

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_POST_LOGIN_SPLASH);
        /* Analytics */
    }

    /* View Methods (BootstrapView) */
    @Override
    public void showLoading()
    {
        loading.setVisibility(View.VISIBLE);
        llAction.setVisibility(View.GONE);
    }

    @Override
    public void handleLocationPermissions()
    {
        if (PermissionHandler.isRationalRequired(this, PermissionHandler.Permissions.LOCATION))
        {
            showBootstrapAction();

            tvActionMessage.setText(R.string.permission_location_message);
            tvAction.setText(R.string.permission_request_again);
            tvAction.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    PermissionHandler
                            .requestPermission(LauncherActivity.this, PermissionHandler.Permissions.LOCATION);
                }
            });
        }
        else
        {
            // Location permission has not been granted yet. Request it directly.
            PermissionHandler.requestPermission(this, PermissionHandler.Permissions.LOCATION);
        }
    }

    @Override
    public void displayLocationServiceUnavailableMessage()
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.location_off_message);
        tvAction.setText(R.string.location_off_action);
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(android.provider.Settings.ACTION_LOCATION_SOURCE_SETTINGS));
            }
        });
    }

    @Override
    public void displayError()
    {
        showBootstrapAction();

        tvActionMessage.setText(R.string.error_default);
        tvAction.setText(R.string.try_again);
        tvAction.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                bootstrapPresenter.attachView(LauncherActivity.this);
            }
        });
    }

    @Override
    public void proceed()
    {
        handleIntent();
    }

    @Override
    public void navigateToPendingInvitesScreen()
    {
        startActivity(PendingInvitesActivity.callingIntent(this));
        finish();
    }

    @Override
    public void displayAppNotAvailableScreen()
    {
        startActivity(UnKnownZoneActivity.getCallingIntent(this));
        finish();
    }

    /* Facebook Login Callbacks */
    @Override
    public void onSuccess(LoginResult loginResult)
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginSuccess();
        }

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,
                GoogleAnalyticsConstants.ACTION_LOGIN_ATTEMPT, GoogleAnalyticsConstants
                        .LABEL_SUCCESS);
        /* Analytics */
    }

    @Override
    public void onCancel()
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginCancel();
        }

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN, GoogleAnalyticsConstants.ACTION_LOGIN_ATTEMPT, GoogleAnalyticsConstants.LABEL_CANCEL);
        /* Analytics */
    }

    @Override
    public void onError(FacebookException error)
    {
        if (facebookLoginPresenter != null)
        {
            facebookLoginPresenter.onFacebookLoginError();
        }

        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_LOGIN_ATTEMPT, GoogleAnalyticsConstants.LABEL_ERROR);
        /* Analytics */
    }

    /* Google Play Services */
    private void setupGooglePlayService()
    {
        if (!googleService.isGoogleApiClientSet())
        {
            GoogleApiClient googleApiClient = new GoogleApiClient.Builder(this)
                    .addConnectionCallbacks(this)
                    .addOnConnectionFailedListener(this)
                    .addApi(LocationServices.API)
                    .build();

            googleService.setGoogleApiClient(googleApiClient);
        }

        if (!googleService.isConnected())
        {
            googleService.connect();
        }
        else
        {
            facebookLoginPresenter.attachView(this);
        }
    }

    @Override
    public void onConnected(@Nullable Bundle bundle)
    {
        facebookLoginPresenter.attachView(this);
    }

    @Override
    public void onConnectionSuspended(int i)
    {
    }

    @Override
    public void onConnectionFailed(@NonNull ConnectionResult connectionResult)
    {
        displayPlayServicesErrorDialog();
    }

    /* Helper Methods */
    private void setupIntroViewPager()
    {
        introScrolled = false;

        vpIntro.setAdapter(new IntroAdapter(getFragmentManager()));
        vpIntro.addOnPageChangeListener(new ViewPager.OnPageChangeListener()
        {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels)
            {

            }

            @Override
            public void onPageSelected(int position)
            {
                setIntroMarker(position);
            }

            @Override
            public void onPageScrollStateChanged(int state)
            {

            }
        });

        vpIntro.setOnTouchListener(new View.OnTouchListener()
        {
            @Override
            public boolean onTouch(View v, MotionEvent event)
            {
                introScrolled = true;
                return false;
            }
        });

        Observable.interval(4, TimeUnit.SECONDS)
                  .observeOn(AndroidSchedulers.mainThread())
                  .subscribe(new Subscriber<Long>()
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
                                 public void onNext(Long aLong)
                                 {
                                     if (!introScrolled) {
                                         int position = vpIntro.getCurrentItem();
                                         if (position == 3) {
                                             position = 0;
                                         }
                                         else {
                                             position++;
                                         }

                                         setIntroMarker(position);
                                         vpIntro.setCurrentItem(position);
                                     }
                                     else {
                                         introScrolled = false;
                                     }
                                 }
                             }
                  );
    }

    private void setIntroMarker(int position)
    {
        switch (position)
        {
            case 0:
                ivIntro1.setImageResource(R.drawable.intro_dot_selected);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 1:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot_selected);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 2:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot_selected);
                ivIntro4.setImageResource(R.drawable.intro_dot);
                break;

            case 3:
                ivIntro1.setImageResource(R.drawable.intro_dot);
                ivIntro2.setImageResource(R.drawable.intro_dot);
                ivIntro3.setImageResource(R.drawable.intro_dot);
                ivIntro4.setImageResource(R.drawable.intro_dot_selected);
                break;
        }
    }

    private void displayBootstrapView()
    {
        llFb.setVisibility(View.GONE);
        rlBootstrap.setVisibility(View.VISIBLE);
    }

    private void displayBlankView()
    {
        llFb.setVisibility(View.GONE);
        rlBootstrap.setVisibility(View.GONE);
    }

    private void displayFbLoginView()
    {
        llFb.setVisibility(View.VISIBLE);
        rlBootstrap.setVisibility(View.GONE);

        renderUserAgreementMessage();

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_LOGIN_FRAGMENT);
        /* Analytics */

        setupIntroViewPager();
    }

    private void renderUserAgreementMessage()
    {
        SpannableString ss = new SpannableString(getResources().getString(R.string.user_agreement));

        ClickableSpan span1 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                String url = "http://www.clanout.com/terms.html";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        };

        ClickableSpan span2 = new ClickableSpan() {
            @Override
            public void onClick(View textView) {

                String url = "http://www.clanout.com/privacy.html";
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setData(Uri.parse(url));
                startActivity(intent);
            }
        };

        ss.setSpan(span1, 32, 48, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        ss.setSpan(span2, 53, 67, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

        userAgreement.setText(ss);
        userAgreement.setMovementMethod(LinkMovementMethod.getInstance());
    }

    private void showBootstrapAction()
    {
        loading.setVisibility(View.GONE);
        llAction.setVisibility(View.VISIBLE);
    }

    private void displayPlayServicesErrorDialog()
    {
        /* Analytics */
        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_PLAY_SERVICES_ERROR,null);
        /* Analytics */

        DefaultDialog.show(this,
                R.string.play_services_error_dialog_title,
                R.string.play_services_error_dialog_message,
                DefaultDialog.BUTTON_DISABLED,
                R.string.play_services_error_dialog_negative_button,
                false,
                new DefaultDialog.Listener()
                {
                    @Override
                    public void onPositiveButtonClicked()
                    {
                    }

                    @Override
                    public void onNegativeButtonClicked()
                    {
                        closeApp();
                    }
                });

        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_GOOGLE_SERVICES_PERMISSION_DIALOG);
        /* Analytics */
    }

    private void displayFacebookPermissionsDialog()
    {
        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_FACEBOOK_PERMISSION_DIALOG);
        /* Analytics */

        DefaultDialog.show(this,
                R.string.facebook_permission_dialog_title,
                R.string.facebook_permission_dialog_message,
                R.string.facebook_permission_dialog_positive_button,
                R.string.facebook_permission_dialog_negative_button,
                false,
                new DefaultDialog.Listener() {
                    @Override
                    public void onPositiveButtonClicked() {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_FACEBOOK_PERMISSIONS,GoogleAnalyticsConstants.LABEL_GRANTED);
                        /* Analytics */

                        LoginManager
                                .getInstance()
                                .logInWithReadPermissions(LauncherActivity.this, FacebookService.PERMISSIONS);
                    }

                    @Override
                    public void onNegativeButtonClicked() {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_FACEBOOK_PERMISSIONS,GoogleAnalyticsConstants.LABEL_DENIED);
                        /* Analytics */

                        closeApp();
                    }
                });
    }

    private void handleIntent()
    {
        Intent sourceIntent = getIntent();

        @FlowEntry int flowEntry = sourceIntent.getIntExtra(ARG_FLOW_ENTRY, FlowEntry.HOME);
        String eventId = sourceIntent.getStringExtra(ARG_EVENT_ID);

        switch (flowEntry)
        {
            case FlowEntry.HOME:

                startActivity(HomeActivity.callingIntent(this));
                break;

            case FlowEntry.DETAILS:
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_DETAILS,GoogleAnalyticsConstants.ACTION_OPEN,GoogleAnalyticsConstants.LABEL_FROM_PUSH_NOTIFICATION);
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,
                        GoogleAnalyticsConstants.ACTION_LAUNCH_FROM_NOTIF,
                        GoogleAnalyticsConstants.LABEL_TO_DETAILS);
                /* Analytics */

                NotificationService.getInstance().deleteAllNotificationsFromCache();

                startActivity(EventDetailsActivity.callingIntent(this, eventId, false));
                break;

            case FlowEntry.CHAT:
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_CHAT,GoogleAnalyticsConstants.ACTION_OPEN,GoogleAnalyticsConstants.LABEL_FROM_PUSH_NOTIFICATION);
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,
                        GoogleAnalyticsConstants.ACTION_LAUNCH_FROM_NOTIF,
                        GoogleAnalyticsConstants.LABEL_TO_CHAT);
                /* Analytics */

                NotificationService.getInstance().deleteAllNotificationsFromCache();

                startActivity(ChatActivity.callingIntent(this, eventId));
                break;

            case FlowEntry.NOTIFICATIONS:
                /* Analytics */
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_NOTIFICATION,GoogleAnalyticsConstants.ACTION_OPEN,GoogleAnalyticsConstants.LABEL_FROM_PUSH_NOTIFICATION);
                AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_LOGIN,GoogleAnalyticsConstants.ACTION_LAUNCH_FROM_NOTIF,GoogleAnalyticsConstants.LABEL_TO_NOTIFICATION);
                /* Analytics */

                startActivity(NotificationActivity.callingIntent(this));
                break;
        }

        finish();
    }
}
