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
import com.amazon.android.tv.tenfoot.ui.activities.ContentBrowseActivity;
import com.amazon.android.tv.tenfoot.utils.BrowseHelper;
import com.amazon.android.ui.constants.PreferencesConstants;
import com.amazon.android.utils.Preferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v17.leanback.app.RowsFragment;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.OnItemViewSelectedListener;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.util.Log;


/**
 * This fragment displays content in horizontal rows for browsing. Each row has its title displayed
 * above it.
 */
public class ContentBrowseFragment extends RowsFragment {

    private static final String TAG = ContentBrowseFragment.class.getSimpleName();
    private static final int WAIT_BEFORE_FOCUS_REQUEST_MS = 500;
    private OnBrowseRowListener mCallback;
    private ArrayObjectAdapter mSettingsAdapter = null;
    private ListRow mRecentListRow = null;
    private ListRow mWatchlistListRow = null;
    private int mLoginButtonIndex;

    // Container Activity must implement this interface.
    public interface OnBrowseRowListener {

        void onItemSelected(Object item);
    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {

        super.onActivityCreated(savedInstanceState);
        EventBus.getDefault().register(this);
        // This makes sure that the container activity has implemented the callback interface.
        // If not, it throws an exception.
        try {
            mCallback = (OnBrowseRowListener) getActivity();
        }
        catch (ClassCastException e) {
            throw new ClassCastException(getActivity().toString() +
                                                 " must implement OnBrowseRowListener: " + e);
        }

        CustomListRowPresenter customListRowPresenter = new CustomListRowPresenter();
        customListRowPresenter.setHeaderPresenter(new RowHeaderPresenter());

        // Uncomment this code to remove shadow from the cards
        //customListRowPresenter.setShadowEnabled(false);

        ArrayObjectAdapter rowsAdapter = new ArrayObjectAdapter(customListRowPresenter);

        BrowseHelper.loadRootContentContainer(getActivity(), rowsAdapter);
        mSettingsAdapter = BrowseHelper.addSettingsActionsToRowAdapter(getActivity(), rowsAdapter);
        mLoginButtonIndex = BrowseHelper.getLoginButtonIndex(mSettingsAdapter);

        setAdapter(rowsAdapter);

        setOnItemViewClickedListener(new ItemViewClickedListener());
        setOnItemViewSelectedListener(new ItemViewSelectedListener());

        // Wait for WAIT_BEFORE_FOCUS_REQUEST_MS for the data to load before requesting focus.
        Handler handler = new Handler(Looper.getMainLooper());
        handler.postDelayed(() -> {
            if (getView() != null) {
                VerticalGridView verticalGridView = findGridViewFromRoot(getView());
                if (verticalGridView != null) {
                    verticalGridView.requestFocus();
                }
            }
        }, WAIT_BEFORE_FOCUS_REQUEST_MS);
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
            mWatchlistListRow = BrowseHelper.updateWatchlistRow(getActivity(), mWatchlistListRow,
                                                                mRecentListRow, rowsAdapter);
        }
    }

    /**
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

                // Update the details preview if the action occurred from the home screen.
                if (Preferences.getString(PreferencesConstants.LAST_ACTIVITY)
                               .equals(ContentBrowser.CONTENT_HOME_SCREEN)) {
                    if (authenticationStatusUpdateEvent.isUserAuthenticated()) {
                        ((ContentBrowseActivity) getActivity()).callImageLoadSubscription(
                                getString(R.string.logout_label),
                                getString(R.string.logout_description),
                                null);
                    }
                    else {
                        ((ContentBrowseActivity) getActivity()).callImageLoadSubscription(
                                getString(R.string.login_label),
                                getString(R.string.login_description),
                                null);
                    }
                }
            }
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Content) {
                Content content = (Content) item;
                Log.d(TAG, "Content with title " + content.getTitle() + " was clicked");

                ContentBrowser.getInstance(getActivity())
                              .setLastSelectedContent(content)
                              .switchToScreen(ContentBrowser.CONTENT_DETAILS_SCREEN, content);
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
                Action settingsAction = (Action) item;
                Log.d(TAG, "Settings with title " + settingsAction.getAction() + " was clicked");
                ContentBrowser.getInstance(getActivity())
                              .settingsActionTriggered(getActivity(),
                                                       settingsAction);
            }
        }
    }

    private final class ItemViewSelectedListener implements OnItemViewSelectedListener {

        @Override
        public void onItemSelected(Presenter.ViewHolder itemViewHolder, Object item,
                                   RowPresenter.ViewHolder rowViewHolder, Row row) {

            mCallback.onItemSelected(item);
        }
    }
}
