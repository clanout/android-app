package com.clanout.app.ui.screens.home.create_suggestion;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextSwitcher;
import android.widget.TextView;
import android.widget.ViewSwitcher;

import com.clanout.R;
import com.clanout.app.common.analytics.AnalyticsHelper;
import com.clanout.app.config.GoogleAnalyticsConstants;
import com.clanout.app.model.CreateEventSuggestion;
import com.clanout.app.model.EventCategory;
import com.clanout.app.service.EventService;
import com.clanout.app.ui.core.BaseFragment;
import com.clanout.app.ui.screens.home.HomeScreen;
import com.clanout.app.ui.screens.home.create_suggestion.mvp.CreateSuggestionPresenter;
import com.clanout.app.ui.screens.home.create_suggestion.mvp.CreateSuggestionPresenterImpl;
import com.clanout.app.ui.screens.home.create_suggestion.mvp.CreateSuggestionView;
import com.clanout.app.ui.util.OnSwipeTouchListener;

import java.util.List;
import java.util.concurrent.TimeUnit;

import butterknife.Bind;
import butterknife.ButterKnife;
import rx.Observable;
import rx.Subscriber;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.subscriptions.CompositeSubscription;
import timber.log.Timber;

public class CreateSuggestionFragment extends BaseFragment implements CreateSuggestionView
{
    public static CreateSuggestionFragment newInstance()
    {
        return new CreateSuggestionFragment();
    }

    HomeScreen screen;

    CreateSuggestionPresenter presenter;

    /* UI Elements */
    @Bind(R.id.rlSuggestionContainer)
    View rlSuggestionContainer;

    @Bind(R.id.llCategoryIconContainer)
    View llCategoryIconContainer;

    @Bind(R.id.ivCategoryIcon)
    ImageView ivCategoryIcon;

    @Bind(R.id.tsTitle)
    TextSwitcher tsTitle;

    @Bind(R.id.tvMakePlan)
    TextView tvMakePlan;

    CompositeSubscription subscriptions;
    List<CreateEventSuggestion> suggestions;
    int activePosition;
    boolean isSwiped;

    /* Lifecycle Methods */
    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        subscriptions = new CompositeSubscription();

        /* Presenter */
        EventService eventService = EventService.getInstance();
        presenter = new CreateSuggestionPresenterImpl(eventService);
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_create_suggestion, container, false);
        ButterKnife.bind(this, view);
        return view;
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState)
    {
        super.onActivityCreated(savedInstanceState);
        screen = (HomeScreen) getActivity();

        tsTitle.setFactory(new ViewSwitcher.ViewFactory()
        {
            public View makeView()
            {
                TextView title = new TextView(getActivity());
                title.setTextColor(ContextCompat.getColor(getActivity(), R.color.text_title));
                title.setTextSize(TypedValue.COMPLEX_UNIT_SP, 18);
                return title;
            }
        });

        Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
        Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
        tsTitle.setInAnimation(in);
        tsTitle.setOutAnimation(out);

        tvMakePlan.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                if (presenter != null)
                {
                    if (suggestions != null && activePosition >= 0)
                    {
                        /* Analytics */
                        AnalyticsHelper.sendEvents(GoogleAnalyticsConstants.CATEGORY_HOME,
                                GoogleAnalyticsConstants.ACTION_CREATE, String.valueOf
                                        (suggestions.get(activePosition).getTitle()));
                        /* Analytics */

                        presenter.select(suggestions.get(activePosition).getCategory());
                    }
                    else
                    {
                        Timber.v(">>>> active position = " + activePosition);
                        Timber.v(">>>> suggestions size = " + suggestions.size());
                        presenter.select(EventCategory.GENERAL);
                    }
                }
            }
        });

        rlSuggestionContainer.setOnTouchListener(new OnSwipeTouchListener(getActivity())
        {
            @Override
            public void onSwipeLeft()
            {
                isSwiped = true;
                activePosition++;
                if (activePosition == suggestions.size())
                {
                    activePosition = 0;
                }

                Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_right);
                Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_left);
                tsTitle.setInAnimation(in);
                tsTitle.setOutAnimation(out);
                render(suggestions.get(activePosition));
            }

            @Override
            public void onSwipeRight()
            {
                isSwiped = true;
                activePosition--;
                if (activePosition < 0)
                {
                    activePosition = suggestions.size() - 1;
                }

                Animation in = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_in_left);
                Animation out = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_out_right);
                tsTitle.setInAnimation(in);
                tsTitle.setOutAnimation(out);
                render(suggestions.get(activePosition));
            }
        });
    }

    @Override
    public void onResume()
    {
        super.onResume();
        presenter.attachView(this);
    }

    @Override
    public void onPause()
    {
        super.onPause();
        subscriptions.clear();
        presenter.detachView();
    }

    /* View Methods */
    @Override
    public void init(final List<CreateEventSuggestion> suggestions)
    {
        this.suggestions = suggestions;
        activePosition = 0;
        render(suggestions.get(activePosition));

        Subscription subscription =
                Observable
                        .interval(3, TimeUnit.SECONDS)
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
                            public void onNext(Long counter)
                            {
                                if (!isSwiped)
                                {
                                    activePosition++;
                                    if (activePosition == suggestions.size())
                                    {
                                        activePosition = 0;
                                    }

                                    render(suggestions.get(activePosition));
                                }
                                else
                                {
                                    isSwiped = false;
                                    Animation in = AnimationUtils
                                            .loadAnimation(getActivity(), R.anim.slide_in_right);
                                    Animation out = AnimationUtils
                                            .loadAnimation(getActivity(), R.anim.slide_out_left);
                                    tsTitle.setInAnimation(in);
                                    tsTitle.setOutAnimation(out);
                                }
                            }
                        });

        subscriptions.add(subscription);
    }

    @Override
    public void navigateToCreate(EventCategory category)
    {
        screen.navigateToCreateDetailsScreen(category);
    }

    /* Helper Methods */
    private void render(CreateEventSuggestion suggestion)
    {
        tsTitle.setText(suggestion.getTitle());
        ivCategoryIcon.setImageDrawable(suggestion.getIcon());
        llCategoryIconContainer.setBackground(suggestion.getIconBackground());
    }
}
