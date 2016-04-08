package com.clanout.app.ui.dialog;

import android.app.Activity;
import android.support.v7.app.AlertDialog;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.clanout.R;
import com.clanout.app.config.Dimensions;
import com.clanout.app.model.EventCategory;
import com.clanout.app.ui.util.CategoryIconFactory;


public class EventCategorySelectionDialog
{
    public interface Listener
    {
        void onCategorySelected(EventCategory category);
    }

    public static void show(Activity activity, final Listener listener)
    {
        final AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setCancelable(true);

        LayoutInflater layoutInflater = activity.getLayoutInflater();
        final View dialogView = layoutInflater.inflate(R.layout.dialog_event_category, null);
        builder.setView(dialogView);

        final AlertDialog alertDialog = builder.create();

        final LinearLayout cafe = (LinearLayout) dialogView.findViewById(R.id.llCafe);
        final LinearLayout movies = (LinearLayout) dialogView.findViewById(R.id.llMovie);
        final LinearLayout eatOut = (LinearLayout) dialogView.findViewById(R.id.llEatOut);
        final LinearLayout sports = (LinearLayout) dialogView.findViewById(R.id.llSports);
        final LinearLayout outdoors = (LinearLayout) dialogView.findViewById(R.id.llOutdoors);
        final LinearLayout indoors = (LinearLayout) dialogView.findViewById(R.id.llIndoors);
        final LinearLayout drinks = (LinearLayout) dialogView.findViewById(R.id.llDrinks);
        final LinearLayout shopping = (LinearLayout) dialogView.findViewById(R.id.llShopping);
        final LinearLayout general = (LinearLayout) dialogView.findViewById(R.id.llGeneral);

        cafe.setBackground(CategoryIconFactory.getIconBackground(EventCategory.CAFE));
        movies.setBackground(CategoryIconFactory.getIconBackground(EventCategory.MOVIES));
        eatOut.setBackground(CategoryIconFactory.getIconBackground(EventCategory.EAT_OUT));
        sports.setBackground(CategoryIconFactory.getIconBackground(EventCategory.SPORTS));
        outdoors.setBackground(CategoryIconFactory.getIconBackground(EventCategory.OUTDOORS));
        indoors.setBackground(CategoryIconFactory.getIconBackground(EventCategory.INDOORS));
        drinks.setBackground(CategoryIconFactory.getIconBackground(EventCategory.DRINKS));
        shopping.setBackground(CategoryIconFactory.getIconBackground(EventCategory.SHOPPING));
        general.setBackground(CategoryIconFactory.getIconBackground(EventCategory.GENERAL));

        ImageView cafeIcon = (ImageView) dialogView.findViewById(R.id.ivCafe);
        ImageView moviesIcon = (ImageView) dialogView.findViewById(R.id.ivMovie);
        ImageView eatOutIcon = (ImageView) dialogView.findViewById(R.id.ivEatOut);
        ImageView sportsIcon = (ImageView) dialogView.findViewById(R.id.ivSports);
        ImageView outdoorsIcon = (ImageView) dialogView.findViewById(R.id.ivOutdoors);
        ImageView indoorsIcon = (ImageView) dialogView.findViewById(R.id.ivIndoors);
        ImageView drinksIcon = (ImageView) dialogView.findViewById(R.id.ivDrinks);
        ImageView shoppingIcon = (ImageView) dialogView.findViewById(R.id.ivShopping);
        ImageView generalIcon = (ImageView) dialogView.findViewById(R.id.ivGeneral);

        cafeIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.CAFE, Dimensions.CATEGORY_ICON_DEFAULT));
        moviesIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.MOVIES, Dimensions.CATEGORY_ICON_DEFAULT));
        eatOutIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.EAT_OUT, Dimensions.CATEGORY_ICON_DEFAULT));
        sportsIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.SPORTS, Dimensions.CATEGORY_ICON_DEFAULT));
        outdoorsIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.OUTDOORS, Dimensions.CATEGORY_ICON_DEFAULT));
        indoorsIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.INDOORS, Dimensions.CATEGORY_ICON_DEFAULT));
        drinksIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.DRINKS, Dimensions.CATEGORY_ICON_DEFAULT));
        shoppingIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.SHOPPING, Dimensions.CATEGORY_ICON_DEFAULT));
        generalIcon.setImageDrawable(CategoryIconFactory
                .get(EventCategory.GENERAL, Dimensions.CATEGORY_ICON_DEFAULT));

        TextView cafeText = (TextView) dialogView.findViewById(R.id.tvCafe);
        TextView moviesText = (TextView) dialogView.findViewById(R.id.tvMovie);
        TextView eatOutText = (TextView) dialogView.findViewById(R.id.tvEatOut);
        TextView sportsText = (TextView) dialogView.findViewById(R.id.tvSports);
        TextView outdoorsText = (TextView) dialogView.findViewById(R.id.tvOutdoors);
        TextView indoorsText = (TextView) dialogView.findViewById(R.id.tvIndoors);
        TextView drinksText = (TextView) dialogView.findViewById(R.id.tvDrinks);
        TextView shoppingText = (TextView) dialogView.findViewById(R.id.tvShopping);
        TextView generalText = (TextView) dialogView.findViewById(R.id.tvGeneral);

        cafeText.setText("Cafe");
        moviesText.setText("Movies");
        eatOutText.setText("Eat Out");
        sportsText.setText("Sports");
        outdoorsText.setText("Outdoors");
        indoorsText.setText("Indoors");
        drinksText.setText("Drinks");
        shoppingText.setText("Shopping");
        generalText.setText("Others");

        cafe.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.CAFE);
                alertDialog.dismiss();
            }
        });

        movies.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.MOVIES);
                alertDialog.dismiss();
            }
        });


        eatOut.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.EAT_OUT);
                alertDialog.dismiss();
            }
        });


        sports.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.SPORTS);
                alertDialog.dismiss();
            }
        });


        outdoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.OUTDOORS);
                alertDialog.dismiss();
            }
        });


        indoors.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.INDOORS);
                alertDialog.dismiss();
            }
        });


        drinks.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.DRINKS);
                alertDialog.dismiss();
            }
        });


        shopping.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.SHOPPING);
                alertDialog.dismiss();
            }
        });


        general.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                listener.onCategorySelected(EventCategory.GENERAL);
                alertDialog.dismiss();

            }
        });

        alertDialog.show();
    }
}
