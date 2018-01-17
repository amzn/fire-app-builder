/**
 * This file was modified by Amazon:
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.amazon.android.tv.tenfoot.ui.fragments;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.helper.AuthHelper;
import com.amazon.android.model.Action;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.presenter.CustomListRowPresenter;
import com.amazon.android.tv.tenfoot.ui.activities.ContentDetailsActivity;
import com.amazon.android.tv.tenfoot.utils.BrowseHelper;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v17.leanback.app.BackgroundManager;
import android.support.v17.leanback.app.BrowseFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.PresenterSelector;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * Main class to show ContentBrowseFragment with header and rows of content.
 */
public class FullContentBrowseFragment extends BrowseFragment {

    private static final String TAG = FullContentBrowseFragment.class.getSimpleName();

    private ArrayObjectAdapter mRowsAdapter;
    private Drawable mDefaultBackground;
    private BackgroundManager mBackgroundManager;
    private ArrayObjectAdapter mSettingsAdapter;
    private ListRow mRecentListRow = null;
    private ListRow mWatchlistListRow = null;
    private int mLoginButtonIndex;


    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        Log.d(TAG, "onCreate");
        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        mRowsAdapter = new ArrayObjectAdapter(new CustomListRowPresenter());
        BrowseHelper.loadRootContentContainer(getActivity(), mRowsAdapter);
        mSettingsAdapter = BrowseHelper.addSettingsActionsToRowAdapter(getActivity(), mRowsAdapter);
        mLoginButtonIndex = BrowseHelper.getLoginButtonIndex(mSettingsAdapter);

        setAdapter(mRowsAdapter);

        prepareBackgroundManager();
        setupUIElements();
        setupEventListeners();
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        final View view = super.onCreateView(inflater, container, savedInstanceState);

        if (view != null) {
            // Hiding these views because we want to use our own search widget instead
            // of Leanback's SearchOrb that's part of the BrowseFragment.
            View searchOrb = view.findViewById(R.id.search_orb);
            if (searchOrb != null) {
                searchOrb.setVisibility(View.GONE);
            }
            ImageView icon = (ImageView) view.findViewById(R.id.icon);
            if (icon != null) {
                icon.setVisibility(View.GONE);
            }
        }
        return view;
    }

    @Override
    public void onResume() {

        super.onResume();
        ArrayObjectAdapter rowsAdapter = (ArrayObjectAdapter) getAdapter();


        if (ContentBrowser.getInstance(getActivity()).isRecentRowEnabled()) {
            mRecentListRow = BrowseHelper.updateContinueWatchingRow(getActivity(),
                                                                    mRecentListRow, rowsAdapter);
        }

        if (ContentBrowser.getInstance(getActivity()).isWatchlistRowEnabled()) {
            mWatchlistListRow  = BrowseHelper.updateWatchlistRow(getActivity(), mWatchlistListRow,
                                                                 mRecentListRow, rowsAdapter);
        }
    }

    private void prepareBackgroundManager() {

        mBackgroundManager = BackgroundManager.getInstance(getActivity());
        mBackgroundManager.attach(getActivity().getWindow());
    }

    private void setupUIElements() {
        // Set custom badge drawable and title here but note that when badge is set title does
        // not show so we have setTitle commented out.
        setBadgeDrawable(ContextCompat.getDrawable(getActivity(), R.drawable.company_logo));
        //setTitle(getString(R.string.browse_title));

        setHeadersState(HEADERS_ENABLED);
        setHeadersTransitionOnBackEnabled(true);

        // Set headers and rows background color
        setBrandColor(ContextCompat.getColor(getActivity(), R.color.browse_headers_bar));
        mBackgroundManager.setColor(ContextCompat.getColor(getActivity(),
                                                           R.color.browse_background_color));

        // Disables the scaling of rows when Headers bar is in open state.
        enableRowScaling(false);

        // Here is where a header presenter can be set to customize the look
        // of the headers list.
        setHeaderPresenterSelector(new PresenterSelector() {
            @Override
            public Presenter getPresenter(Object o) {

                return new RowHeaderPresenter();
            }
        });
    }

    /**
     * /**
     * Event bus listener method to listen for authentication updates from AuthHelper and update
     * the login action status in settings.
     *
     * @param authenticationStatusUpdateEvent Broadcast event for update in authentication status.
     */
    @Subscribe
    public void onAuthenticationStatusUpdateEvent(AuthHelper.AuthenticationStatusUpdateEvent
                                                          authenticationStatusUpdateEvent) {

        if (mSettingsAdapter != null) {
            if (mLoginButtonIndex != -1) {
                mSettingsAdapter.notifyArrayItemRangeChanged(mLoginButtonIndex, 1);
            }
        }
    }

    private void setupEventListeners() {

        setOnSearchClickedListener(new View.OnClickListener() {

            @Override
            public void onClick(View view) {

                ContentBrowser.getInstance(getActivity())
                              .switchToScreen(ContentBrowser.CONTENT_SEARCH_SCREEN);
            }
        });

        setOnItemViewClickedListener(new ItemViewClickedListener());
    }

    protected void setDefaultBackground(Drawable background) {

        mDefaultBackground = background;
    }

    protected void setDefaultBackground(int resourceId) {

        mDefaultBackground = ContextCompat.getDrawable(getActivity(), resourceId);
    }

    protected void updateBackground(Drawable drawable) {

        BackgroundManager.getInstance(getActivity()).setDrawable(drawable);
    }

    protected void clearBackground() {

        BackgroundManager.getInstance(getActivity()).setDrawable(mDefaultBackground);
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Content) {
                Content content = (Content) item;
                Log.d(TAG, "Content clicked: " + item.toString());

                View imageView = ((ImageCardView) itemViewHolder.view).getMainImageView();

                Bundle bundle = ActivityOptionsCompat.makeSceneTransitionAnimation(
                        getActivity(),
                        imageView,
                        ContentDetailsActivity.SHARED_ELEMENT_NAME).toBundle();

                ContentBrowser.getInstance(getActivity())
                              .setLastSelectedContent(content)
                              .switchToScreen(ContentBrowser.CONTENT_DETAILS_SCREEN, content,
                                              bundle);
            }
            else if (item instanceof ContentContainer) {
                ContentContainer contentContainer = (ContentContainer) item;
                Log.d(TAG, "ContentContainer with name " + contentContainer.getName() + " was " +
                        "clicked");

                ContentBrowser.getInstance(getActivity())
                              .setLastSelectedContentContainer(contentContainer)
                              .switchToScreen(ContentBrowser.CONTENT_SUBMENU_SCREEN);
            }
            else if (item instanceof Action) {
                Action settingsItemModel = (Action) item;
                Log.d(TAG, "Settings with title " + settingsItemModel.getAction() + " was clicked");
                ContentBrowser.getInstance(getActivity())
                              .settingsActionTriggered(getActivity(), settingsItemModel);
            }
        }
    }
}
