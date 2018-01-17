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

package com.amazon.functional;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.ui.activities.SplashActivity;
import com.amazon.testresources.TestConfig;
import com.robotium.solo.Solo;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.SearchEditText;
import android.util.Log;
import android.view.KeyEvent;
import android.widget.Button;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Functional tests for Calypso.
 */
@RunWith(AndroidJUnit4.class)
public class FunctionalTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>
            (SplashActivity.class);

    @Rule
    public ErrorCollector errors = new ErrorCollector();

    private SplashActivity mSplashActivity;
    private ContentContainer mContentContainer;

    private Solo solo;

    final private String TAG = this.getClass().getSimpleName();
    final private int[] SAMPLE_SIZES = {10, 100, 500, 5000};

    private int count;
    private boolean isSampleFeed;

    @Before
    public void setUp() throws Exception {

        solo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityTestRule
                .getActivity());
        mSplashActivity = mActivityTestRule.getActivity();
        init();
    }

    /**
     * Does functional tests on Calypso sample app. Disabled for live feed as it is not static
     * and subject to change.
     */
    @Test(timeout = 500000)
    public void testFunctional() throws Exception {

        checkVideoDetails();
        checkContainerNames();
        doStandardSearchTest();
        doUniqueSearchTest();
        doInvalidSearchTest();
    }

    /**
     * Set up environment for testing.
     */
    private void init() {
        // wait for browse screen to load
        solo.waitForView(R.id.full_content_browse_fragment, 0, TestConfig.LONG_TIMEOUT);
        // get item count
        count = 0;
        mContentContainer = ContentBrowser.getInstance(mSplashActivity)
                                          .getRootContentContainer();
        List<Content> contentFound = new ArrayList<>();
        for (Content c : mContentContainer) {
            if (!contentFound.contains(c)) {
                contentFound.add(c);
                count++;
            }
        }

        // check if we are using sample feed
        Content firstItem = contentFound.get(0);
        boolean hasSampleVideo = firstItem.getTitle().contains("Sample item");
        boolean isValidSize = false;
        for (int s : SAMPLE_SIZES) {
            if (count == s) {
                isValidSize = true;
                break;
            }
        }
        isSampleFeed = hasSampleVideo && isValidSize;

        Log.i(TAG, "Feed loaded, number of distinct items = " + count);
        if (!isSampleFeed) {
            Log.w(TAG, "You do not appear to be using a sample feed. Testing on live feed " +
                    "is disabled.");
        }
        else {
            Log.i(TAG, "Using sample feed with " + count + " items");
        }
    }

    /**
     * Check the names of the categories.
     */
    private void checkContainerNames() {

        if (isSampleFeed) {
            final int CONTAINERS_TO_CHECK = 6;
            int totalContainers = mContentContainer.getContentContainerCount();
            assertEquals("Sample feeds should have six containers.", CONTAINERS_TO_CHECK,
                         totalContainers);
            for (int i = 1; i <= totalContainers; i++) {
                try {
                    assertEquals("Name of container " + i + " does not match expected.", "Sample " +
                                         "container " + i,
                                 mContentContainer.getChildContentContainerAtIndex(i - 1).getName
                                         ());

                }
                catch (Throwable t) {
                    errors.addError(t);
                }
            }
        }
        else {
            Log.i(TAG, "checkContainerNames(): not using sample feed, skipping test");
        }
    }

    /**
     * Do functional tests related to the details screen.
     *
     * Important: make sure to clear the application data first using this command:
     *     adb shell pm clear com.fireappbuilder.android.calypso
     * or else this part of the test may fail.
     */
    private void checkVideoDetails() throws Exception {


        final int ITEMS_TO_TEST = 5;
        final int PLAYBACK_TIME_MS = 10000;
        HorizontalGridView horizontalGridView;
        Button button;
        for (int a = 0; a < ITEMS_TO_TEST; a++) {
            if (a > 0) {
                solo.sendKey(KeyEvent.KEYCODE_DPAD_RIGHT);
            }
            solo.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
            solo.waitForView(R.id.content_details_fragment);
            if (isSampleFeed) {
                String title = ((TextView) solo.getView(R.id.details_description_title)).getText
                        ().toString();
                try {
                    assertEquals("Item title does not match expected.", "Sample item " + (a + 1),
                                 title);
                }
                catch (Throwable t) {
                    errors.addError(t);
                }

                String subtitle = ((TextView) solo.getView(R.id.details_description_subtitle))
                        .getText().toString();
                // TODO: check for unique subtitle (requires update to sample feed generator)
                try {
                    assertTrue("Item subtitle does not match expected.", subtitle.contains
                            ("Sample subtitle"));
                }
                catch (Throwable t) {
                    errors.addError(t);
                }
                String description = ((TextView) solo.getView(R.id.ellipsized_description_text))
                        .getText().toString();
                // TODO: check for unique subtitle (requires update to sample feed generator)
                try {
                    assertTrue("Video description does not match expected.", description.contains
                            ("Lorem ipsum"));
                }
                catch (Throwable t) {
                    errors.addError(t);
                }
            }

            horizontalGridView = (HorizontalGridView) solo.getView(R.id.details_overview_actions);
            try {
                assertEquals("There should only be one button.", 1,
                             horizontalGridView.getChildCount());
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            button = (Button) horizontalGridView.getChildAt(0);
            try {
                assertEquals("Only the \"Watch Now\" button should be available.", "Watch\nNow",
                             button.getText()
                                   .toString());
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            // play video
            solo.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
            solo.waitForView(R.id.playback_controls_fragment);
            if (isSampleFeed) {
                String videoTitle = ((TextView) solo.getView(R.id.videoTitle)).getText().toString();
                try {
                    assertEquals("Item title in video does not match expected.", "Sample item " +
                            (a + 1), videoTitle);

                }
                catch (Throwable t) {
                    errors.addError(t);
                }
                String videoSubtitle = ((TextView) solo.getView(R.id.videoSubtitle)).getText()
                                                                                    .toString();
                try {
                    assertTrue("Item subtitle in video does not match expected.", videoSubtitle
                            .contains("Sample subtitle"));

                }
                catch (Throwable t) {
                    errors.addError(t);
                }
            }
            Thread.sleep(PLAYBACK_TIME_MS);
            solo.goBack();
            solo.waitForView(R.id.content_details_fragment);
            horizontalGridView = (HorizontalGridView) solo.getView(R.id.details_overview_actions);
            try {
                assertEquals("There should now be two buttons.", 2, horizontalGridView
                        .getChildCount());

            }
            catch (Throwable t) {
                errors.addError(t);
            }
            button = (Button) horizontalGridView.getChildAt(0);
            try {
                assertEquals("\"Resume Playback\" button should be available.",
                             "Resume\nPlayback", button

                                     .getText().toString());
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            button = (Button) horizontalGridView.getChildAt(1);
            try {
                assertEquals("\"Watch from Beginning\" button should be available.", "Watch\nfrom" +
                                     " Beginning",

                             button.getText().toString());
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            solo.goBack();
            solo.waitForView(R.id.full_content_browse_fragment);

        }
    }

    /**
     * Checks that we are getting search results.
     */
    private void doStandardSearchTest() throws Exception {

        if (isSampleFeed) {
            int itemsFound = searchResultCount(TestConfig.SEARCH_STRING_CUSTOM);
            try {
                assertTrue("Search string should return at least one result.", itemsFound > 0);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            solo.goBack();
            solo.waitForView(R.id.full_content_browse_fragment);
        }
        else {
            Log.i(TAG, "doStandardSearchTest(): not using sample feed, skipping test");
        }

    }

    /**
     * Checks that searching for an invalid string returns no results.
     */
    private void doInvalidSearchTest() throws Exception {

        int found = searchResultCount(TestConfig.SEARCH_STRING_INVALID, false);
        try {
            assertEquals("Invalid search string should not return any results.", 0, found);
        }
        catch (Throwable t) {
            errors.addError(t);
        }
        solo.goBack();
        solo.waitForView(R.id.full_content_browse_fragment);
    }

    /**
     * Checks that a unique search string should return one result.
     */
    private void doUniqueSearchTest() throws Exception {

        if (isSampleFeed) {
            int found = searchResultCount(TestConfig.getUniqueTitle(count));
            try {
                assertEquals("Unique search string should return exactly one result.", 1, found);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
            solo.goBack();
            solo.waitForView(R.id.full_content_browse_fragment);
        }
        else {
            Log.i(TAG, "doUniqueSearchTest(): not using sample feed, skipping test");
        }
    }

    /**
     * Helper method for going to search screen.
     */
    private void goToSearch() throws Exception {

        solo.sendKey(KeyEvent.KEYCODE_DPAD_UP);
        solo.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
        solo.waitForView(R.id.content_search_fragment);
        solo.goBack();

        solo.waitForView(R.id.lb_search_text_editor);
    }

    /**
     * Helper method for searching.
     */
    private void searchForText(String searchString) throws Exception {

        goToSearch();
        SearchEditText searchEditText = (SearchEditText) solo.getView(R.id.lb_search_text_editor);
        solo.enterText(searchEditText, searchString);
        solo.waitForView(R.id.row_content);
    }

    /**
     * Helper method for getting number of results.
     *
     * @param searchString String to search for.
     * @param resultsExpected true if we are expecting a non-zero number of results.
     * @return Number of search results.
     */
    private int searchResultCount(String searchString, boolean resultsExpected) throws Exception {

        int timeout = 5000;
        searchForText(searchString);
        HorizontalGridView items = (HorizontalGridView) solo.getView(R.id.row_content);
        if (resultsExpected) {
            long startTime = System.currentTimeMillis();
            while (items.getChildCount() == 0) {
                if (System.currentTimeMillis() - startTime > timeout) {
                    fail("Search results timed out.");
                }
            }
        }
        return items.getChildCount();
    }

    /**
     * Helper method for getting number of results.
     *
     * @param searchString String to search for.
     * @return Number of search results.
     */
    private int searchResultCount(String searchString) throws Exception {

        return searchResultCount(searchString, true);
    }
}
