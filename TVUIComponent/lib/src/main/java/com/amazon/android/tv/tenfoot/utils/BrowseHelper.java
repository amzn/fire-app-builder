/**
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
package com.amazon.android.tv.tenfoot.utils;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.Action;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.presenter.CardPresenter;
import com.amazon.android.tv.tenfoot.presenter.SettingsCardPresenter;
import com.amazon.android.ui.constants.PreferencesConstants;
import com.amazon.android.utils.Preferences;
import com.amazon.utils.DateAndTimeHelper;

import android.app.Activity;
import android.content.Context;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.util.Log;

import java.util.List;

/**
 * This class contains common functionality between different browse fragment layouts.
 */
public class BrowseHelper {

    private static String TAG = BrowseHelper.class.getSimpleName();

    /**
     * Saves the browse activity state to be reloaded later, if necessary.
     *
     * @param activity The browse activity.
     */
    public static void saveBrowseActivityState(Activity activity) {

        // Update the last activity preference value if an activity restore is not currently in
        // progress.
        if (activity.getIntent() == null ||
                !activity.getIntent().getBooleanExtra(ContentBrowser.RESTORE_ACTIVITY, false)) {

            Preferences.setString(PreferencesConstants.LAST_ACTIVITY,
                                  ContentBrowser.CONTENT_HOME_SCREEN);
            Preferences.setLong(PreferencesConstants.TIME_LAST_SAVED,
                                DateAndTimeHelper.getCurrentDate().getTime());
        }
    }

    /**
     * Adds the settings actions to an adapter and adds the adapter to the row adapter.
     *
     * @param activity   The activity.
     * @param rowAdapter The row adapter.
     */
    public static ArrayObjectAdapter addSettingsActionsToRowAdapter(Activity activity,
                                                                    ArrayObjectAdapter rowAdapter) {

        List<Action> settings = ContentBrowser.getInstance(activity).getSettingsActions();

        if (settings == null || settings.isEmpty()) {
            Log.d(TAG, "No settings were found");
            return null;
        }

        SettingsCardPresenter cardPresenter = new SettingsCardPresenter();
        ArrayObjectAdapter settingsAdapter = new ArrayObjectAdapter(cardPresenter);

        for (Action item : settings) {
            settingsAdapter.add(item);
        }
        // Create settings header and row.
        HeaderItem header = new HeaderItem(0, activity.getResources()
                                                      .getString(R.string.settings_title));
        rowAdapter.add(new ListRow(header, settingsAdapter));

        return settingsAdapter;
    }

    /**
     * Get the index of the login/logout button from the settings row adapter.
     *
     * @param settingsAdapter The settings row adapter.
     * @return The index of the login/logout button or -1 if it was not found.
     */
    public static int getLoginButtonIndex(ArrayObjectAdapter settingsAdapter) {

        for (int i = 0; i < settingsAdapter.size(); i++) {

            Action action = (Action) settingsAdapter.get(i);
            if (action.getAction().equals(ContentBrowser.LOGIN_LOGOUT)) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Loads the content from the root content container into the rows adapter.
     *
     * @param activity    The activity.
     * @param rowsAdapter The rows adapter.
     */
    public static void loadRootContentContainer(Activity activity, ArrayObjectAdapter rowsAdapter) {

        ContentContainer rootContentContainer = ContentBrowser.getInstance(activity)
                                                              .getRootContentContainer();

        CardPresenter cardPresenter = new CardPresenter();

        for (ContentContainer contentContainer : rootContentContainer.getContentContainers()) {

            HeaderItem header = new HeaderItem(0, contentContainer.getName());
            ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(cardPresenter);

            for (ContentContainer innerContentContainer : contentContainer.getContentContainers()) {
                listRowAdapter.add(innerContentContainer);
            }

            for (Content content : contentContainer.getContents()) {
                listRowAdapter.add(content);
            }

            rowsAdapter.add(new ListRow(header, listRowAdapter));
        }
    }

    /**
     * Updates the recent row in the rows adapter. To be displayed before the settings row.
     *
     * @param activity      The activity.
     * @param recentListRow The recent list row.
     * @param rowsAdapter   The browse fragment rows adapter.
     * @return The updated list row.
     */
    public static ListRow updateContinueWatchingRow(Activity activity, ListRow recentListRow,
                                                    ArrayObjectAdapter rowsAdapter) {

        if (ContentBrowser.getInstance(activity).getContentLoader().isContentLoaded()) {

            List<Content> recentContent = ContentBrowser.getInstance(activity)
                                                        .getRecentContent();

            int maxItems = ContentBrowser.getInstance(activity).getMaxNumberOfRecentItems();

            return updateListRow(activity.getApplicationContext(), rowsAdapter, recentListRow,
                                 recentContent, rowsAdapter.size() - 1, R.string.recent_row,
                                 maxItems);

        }
        return null;
    }

    /**
     * Updates the watchlist row in the rows adapter. To be displayed before the settings row and
     * the recent row (if present).
     *
     * @param activity         The activity.
     * @param watchlistListRow The watchlist list row.
     * @param recentListRow    The recent list row.
     * @param rowsAdapter      The rows adapter of the browse fragment.
     * @return The updated watchlist list row.
     */
    public static ListRow updateWatchlistRow(Activity activity, ListRow watchlistListRow,
                                             ListRow recentListRow, ArrayObjectAdapter
                                                     rowsAdapter) {

        if (ContentBrowser.getInstance(activity).getContentLoader().isContentLoaded()) {

            List<Content> watchlistItems = ContentBrowser.getInstance(activity)
                                                         .getWatchlistContent();

            // This row should be inserted before the settings row and the recent row if present.
            int rowIndex = recentListRow == null ? rowsAdapter.size() - 1 : rowsAdapter.size() - 2;
            Log.d(TAG, "Inserting watchlist row at index " + rowIndex);
            return updateListRow(activity.getApplicationContext(), rowsAdapter, watchlistListRow,
                                 watchlistItems, rowIndex, R.string.watchlist_row,
                                 watchlistItems.size());
        }
        return null;
    }

    /**
     * Updates the given row of the rows adapter.
     *
     * @param context        The context.
     * @param rowsAdapter    The rows adapter of the browse fragment.
     * @param listRow        The list row to update.
     * @param content        The content for the row.
     * @param rowIndex       The index in which the list row should be added into the rows adapter.
     * @param headerStringId The string resource id for the row's header.
     * @param maxItems       The maximum number of content to put in the row.
     * @return The updated list row.
     */
    private static ListRow updateListRow(Context context, ArrayObjectAdapter rowsAdapter,
                                         ListRow listRow, List<Content> content, int rowIndex,
                                         int headerStringId, int maxItems) {

        if (listRow != null) {
            rowsAdapter.remove(listRow);
            rowIndex--;
        }
        // Create the new row.
        listRow = createListRow(context, content, headerStringId, maxItems);
        if (listRow != null) {
            rowsAdapter.add(rowIndex, listRow);
        }
        return listRow;
    }

    /**
     * Creates a list row of content with the given header string.
     *
     * @param context        The context.
     * @param contents       The contents for the list row.
     * @param headerStringId The string resource id for the row's header.
     * @param maxItems       The maximum number of content to put in the row.
     * @return A list row.
     */
    private static ListRow createListRow(Context context, List<Content> contents,
                                         int headerStringId, int maxItems) {

        // Only create the row if the content list is not empty.
        if (!contents.isEmpty()) {
            CardPresenter cardPresenter = new CardPresenter();
            HeaderItem header = new HeaderItem(0, context.getResources().getString(headerStringId));

            ArrayObjectAdapter recentRowAdapter = new ArrayObjectAdapter(cardPresenter);

            if (contents.size() == maxItems) {
                recentRowAdapter.addAll(0, contents);
            }
            else {
                for (int i = 0; i < contents.size() && i < maxItems; i++) {
                    recentRowAdapter.add(contents.get(i));
                }
            }
            return new ListRow(header, recentRowAdapter);
        }
        return null;
    }
}
