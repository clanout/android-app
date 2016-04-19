package com.clanout.app.ui.screens.accounts;

import android.content.Intent;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.Dimensions;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.User;
import com.clanout.app.service.UserService;
import com.clanout.app.service.WhatsappService;
import com.clanout.app.ui.core.BaseFragment;
import com.clanout.app.ui.dialog.FeedbackDialog;
import com.clanout.app.ui.dialog.UpdateMobileDialog;
import com.clanout.app.ui.util.CircleTransform;
import com.clanout.app.ui.util.SnackbarFactory;
import com.squareup.picasso.Picasso;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;
import rx.Subscriber;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AccountFragment extends BaseFragment
{
    public static AccountFragment newInstance()
    {
        return new AccountFragment();
    }

    AccountScreen screen;

    UserService userService;

    /* UI Elements */
    @Bind(R.id.ivCoverPic)
    ImageView ivCoverPic;

    @Bind(R.id.ivProfilePic)
    ImageView ivProfilePic;

    @Bind(R.id.tvName)
    TextView tvName;

    @Bind(R.id.llBlockFriends)
    View llBlockFriends;

    /* Lifecycle Methods */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_accounts, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);

        screen = (AccountScreen) getActivity();

        userService = UserService.getInstance();

        /* User Service */
        UserService userService = UserService.getInstance();
        User sessionUser = userService.getSessionUser();

        /* Init User Details */
        tvName.setText(sessionUser.getName());

        final Drawable personDrawable = MaterialDrawableBuilder
                .with(getActivity())
                .setIcon(MaterialDrawableBuilder.IconValue.ACCOUNT_CIRCLE)
                .setColor(ContextCompat.getColor(getActivity(), R.color.light_grey))
                .setSizeDp(Dimensions.PROFILE_PIC_LARGE)
                .build();

        userService.getCoverPicUrl()
                .subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Subscriber<String>()
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
                    public void onNext(String coverPicUrl)
                    {
                        Picasso.with(getActivity())
                                .load(coverPicUrl)
                                .placeholder(personDrawable)
                                .fit()
                                .centerCrop()
                                .noFade()
                                .into(ivCoverPic);

                    }
                });

        Picasso.with(getActivity())
               .load(UserService.getProfilePicUrl(sessionUser.getId()))
               .placeholder(personDrawable)
               .fit()
               .centerCrop()
               .noFade()
               .transform(new CircleTransform())
               .into(ivProfilePic);

        llBlockFriends.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                        /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT,
                                GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants
                                        .LABEL_FRIENDS);
                /* Analytics */

                screen.navigateToFriendsScreen();
            }
        });
    }

    /* Listeners */
    @OnClick(R.id.llUpdateMobileNumber)
    public void onUpdateMobileClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendScreenNames(GoogleAnalyticsConstants.SCREEN_UPDATE_PHONE_DIALOG_FROM_ACCOUNTS);
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_UPDATE_MOBILE);
        /* Analytics */

        UpdateMobileDialog.show(getActivity(), new UpdateMobileDialog.Listener()
        {
            @Override
            public void onSuccess(String mobileNumber)
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT,
                                GoogleAnalyticsConstants.ACTION_UPDATE_PHONE,
                                GoogleAnalyticsConstants.LABEL_SUCCESS);
                /* Analytics */
            }
        });

    }

    @OnClick(R.id.llWhatsAppInvite)
    public void onWhatsAppInviteClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_WHATSAPP_INVITE);
        /* Analytics */


        WhatsappService accountsService = WhatsappService.getInstance();
        if (accountsService.isWhatsAppInstalled(getActivity()))
        {
            /* Analytics */
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_WHATSAPP_INVITE, GoogleAnalyticsConstants.LABEL_SUCCESS);
            /* Analytics */

            startActivity(accountsService.getWhatsAppIntent());
        }
        else
        {
            /* Analytics */
            AnalyticsHelper
                    .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_WHATSAPP_INVITE, GoogleAnalyticsConstants.LABEL_FAILURE);
            /* Analytics */

            SnackbarFactory.create(getActivity(), R.string.error_no_whatsapp);
        }
    }

    @OnClick(R.id.llFeedback)
    public void onFeedbackClicked()
    {
        /* Analytics */
        AnalyticsHelper.sendScreenNames(GoogleAnalyticsConstants.SCREEN_FEEDBACK_DIALOG);
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_FEEDBACK);
        /* Analytics */

        FeedbackDialog.show(getActivity(), new FeedbackDialog.Listener()
        {
            @Override
            public void onSuccess(int feedbackType, String comment)
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT,
                                GoogleAnalyticsConstants.ACTION_FEEDBACK,
                                GoogleAnalyticsConstants.LABEL_SUCCESS + "-" + String
                                .valueOf(feedbackType));
                /* Analytics */

            }

            @Override
            public void onCancel()
            {
                /* Analytics */
                AnalyticsHelper
                        .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT,
                                GoogleAnalyticsConstants.ACTION_FEEDBACK,
                                GoogleAnalyticsConstants.LABEL_CANCEL);
                /* Analytics */
            }
        });
    }

    @OnClick(R.id.llFaq)
    public void onFaqClicked()
    {
        /* Analytics */
        AnalyticsHelper
                .sendEvents(GoogleAnalyticsConstants.CATEGORY_ACCOUNT, GoogleAnalyticsConstants.ACTION_GO_TO, GoogleAnalyticsConstants.LABEL_FAQ);
        /* Analytics */

        String url = "http://www.clanout.com/faq.html";
        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(Uri.parse(url));
        startActivity(intent);
    }
}
