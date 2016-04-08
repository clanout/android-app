package com.clanout.app.model;

import android.graphics.drawable.Drawable;

import com.clanout.app.config.Dimensions;
import com.clanout.app.ui.util.CategoryIconFactory;

public class CreateEventSuggestion implements Model
{
    private final EventCategory category;
    private final String title;

    public CreateEventSuggestion(EventCategory category, String title)
    {
        this.category = category;
        this.title = title;
    }

    public EventCategory getCategory()
    {
        return category;
    }

    public String getTitle()
    {
        return title;
    }

    public Drawable getIcon()
    {
        return CategoryIconFactory.get(category, Dimensions.CATEGORY_ICON_DEFAULT);
    }

    public Drawable getIconBackground()
    {
        return CategoryIconFactory.getIconBackground(category);
    }
}
