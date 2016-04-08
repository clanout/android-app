package com.clanout.app.ui.util;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.ShapeDrawable;
import android.graphics.drawable.shapes.OvalShape;
import android.support.v4.content.ContextCompat;

import com.clanout.R;
import com.clanout.app.model.EventCategory;
import com.clanout.app.root.ClanOut;

import net.steamcrafted.materialiconlib.MaterialDrawableBuilder;

import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CategoryIconFactory
{
    private static Map<EventCategory, MaterialDrawableBuilder.IconValue> iconMapping;
    private static Map<EventCategory, Integer> colorMapping;
    private static List<Integer> colors = Arrays.asList(
            R.color.category_icon_one,
            R.color.category_icon_two,
            R.color.category_icon_three,
            R.color.category_icon_four,
            R.color.category_icon_five,
            R.color.category_icon_six,
            R.color.category_icon_seven,
            R.color.category_icon_eight,
            R.color.category_icon_nine);

    static
    {
        iconMapping = new HashMap<>();
        iconMapping.put(EventCategory.GENERAL, MaterialDrawableBuilder.IconValue.CALENDAR_CHECK);
        iconMapping.put(EventCategory.EAT_OUT, MaterialDrawableBuilder.IconValue.FOOD);
        iconMapping.put(EventCategory.DRINKS, MaterialDrawableBuilder.IconValue.MARTINI);
        iconMapping.put(EventCategory.CAFE, MaterialDrawableBuilder.IconValue.COFFEE);
        iconMapping.put(EventCategory.MOVIES, MaterialDrawableBuilder.IconValue.THEATER);
        iconMapping.put(EventCategory.OUTDOORS, MaterialDrawableBuilder.IconValue.BIKE);
        iconMapping.put(EventCategory.SPORTS, MaterialDrawableBuilder.IconValue.TENNIS);
        iconMapping
                .put(EventCategory.INDOORS, MaterialDrawableBuilder.IconValue.XBOX_CONTROLLER);
        iconMapping.put(EventCategory.SHOPPING, MaterialDrawableBuilder.IconValue.SHOPPING);
    }

    static
    {
        Collections.shuffle(colors);
        colorMapping = new HashMap<>();
        colorMapping.put(EventCategory.GENERAL, colors.get(0));
        colorMapping.put(EventCategory.EAT_OUT, colors.get(1));
        colorMapping.put(EventCategory.DRINKS, colors.get(2));
        colorMapping.put(EventCategory.CAFE, colors.get(3));
        colorMapping.put(EventCategory.MOVIES, colors.get(4));
        colorMapping.put(EventCategory.OUTDOORS, colors.get(5));
        colorMapping.put(EventCategory.INDOORS, colors.get(6));
        colorMapping.put(EventCategory.SPORTS, colors.get(7));
        colorMapping.put(EventCategory.SHOPPING, colors.get(8));
    }


    public static Drawable get(EventCategory eventCategory, int size)
    {
        return MaterialDrawableBuilder.with(ClanOut.getClanOutContext())
                                      .setIcon(iconMapping.get(eventCategory))
                                      .setColor(Color.WHITE)
                                      .setSizeDp(size)
                                      .build();
    }

    public static Drawable getIconBackground(EventCategory eventCategory)
    {
        int color = colorMapping.get(eventCategory);
        ShapeDrawable circle = new ShapeDrawable(new OvalShape());
        circle.getPaint()
              .setColor(ContextCompat.getColor(ClanOut.getClanOutContext(), color));
        return circle;
    }
}
