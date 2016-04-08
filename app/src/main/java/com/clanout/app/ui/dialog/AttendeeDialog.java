package com.clanout.app.ui.dialog;

import android.content.Context;
import android.graphics.drawable.Drawable;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.config.AppConstants;
import com.clanout.app.config.Dimensions;
import com.clanout.app.model.Attendee;
import com.clanout.app.model.AttendeeWrapper;
import com.clanout.app.service.FacebookService;
import com.clanout.app.service.UserService;
import com.clanout.app.ui.util.CircleTransform;
import com.squareup.picasso.Picasso;

/**
 * Created by harsh on 02/04/16.
 */
public class AttendeeDialog
{

    public static void show(Context context, AttendeeWrapper attendeeWrapper, Drawable personDrawable)
    {
        AlertDialog.Builder builder = new AlertDialog.Builder(context);
        builder.setCancelable(true);

        LayoutInflater inflater = LayoutInflater.from(context);
        final View dialogView = inflater.inflate(R.layout.dialog_attendee, null);
        builder.setView(dialogView);

        final ImageView userPic = (ImageView) dialogView
                .findViewById(R.id.ivPic);
        final ImageView frindIcon = (ImageView) dialogView
                .findViewById(R.id.ivFriendIcon);
        final TextView userName = (TextView) dialogView
                .findViewById(R.id.tvName);
        final TextView isFriend = (TextView) dialogView
                .findViewById(R.id.tvFriend);
        final TextView hasInvited = (TextView) dialogView
                .findViewById(R.id.tvInvite);
        final TextView status = (TextView) dialogView
                .findViewById(R.id.tvStatus);

        Picasso.with(context)
                .load(UserService.getProfilePicUrl(attendeeWrapper.getAttendee().getId()))
                .placeholder(personDrawable)
                .transform(new CircleTransform())
                .into(userPic);

        userName.setText(attendeeWrapper.getAttendee().getName());

        if(attendeeWrapper.isFriend())
        {
            isFriend.setVisibility(View.VISIBLE);
            frindIcon.setVisibility(View.VISIBLE);
        }else{

            isFriend.setVisibility(View.GONE);
            frindIcon.setVisibility(View.GONE);
        }

        if(attendeeWrapper.isInviter())
        {
            hasInvited.setVisibility(View.VISIBLE);
        }else{

            hasInvited.setVisibility(View.GONE);
        }


        if(attendeeWrapper.getAttendee().getStatus() == null)
        {
            status.setText(R.string.no_status);
        }else if(attendeeWrapper.getAttendee().getStatus().isEmpty())
        {
            status.setText(R.string.no_status);
        }else{

            status.setText(attendeeWrapper.getAttendee().getStatus());
        }

        final AlertDialog alertDialog = builder.create();
        alertDialog.show();

    }
}
