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

package com.amazon.analytics;

import com.amazon.analytics.testresources.SampleActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import static junit.framework.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the {@link AnalyticsManager} class
 */
@RunWith(AndroidJUnit4.class)
public class AnalyticsManagerTest {

    private static final String TAG = AnalyticsManagerTest.class.getSimpleName();

    @Rule
    public ActivityTestRule<SampleActivity> mActivityRule = new ActivityTestRule<>
            (SampleActivity.class);

    private AnalyticsManager mAnalyticsManager;

    private final int CLEAR_LOG_DELAY = 2000;

    /**
     * Check that console output contains the specified text.
     *
     * @param command    Console command
     * @param outputLine Text to check for
     */
    private boolean checkConsole(String command, String outputLine) throws Exception {

        boolean stringFound = false;
        Process process = Runtime.getRuntime().exec(command);
        String line;
        BufferedReader bufferedReader =
                new BufferedReader(new InputStreamReader(process.getInputStream()));

        while ((line = bufferedReader.readLine()) != null) {
            if (line.contains(outputLine)) {
                stringFound = true;
                break;
            }

        }
        return stringFound;
    }

    /**
     * Clears LogCat messages
     */
    private void clearLogs() throws Exception {

        Runtime.getRuntime().exec("logcat -c");
        // allow logs to finish clearing, this is not instantaneous;
        // removing the two-second wait will cause the test to fail
        Thread.sleep(CLEAR_LOG_DELAY);
    }

    @Before
    public void setUp() throws Exception {

        mAnalyticsManager = AnalyticsManager.getInstance(mActivityRule.getActivity()
                                                                      .getApplicationContext());
    }

    @After
    public void tearDown() throws Exception {

        mAnalyticsManager.reset();
        mAnalyticsManager = null;
    }

    /**
     * tests the {@link AnalyticsManager#getInstance(Context)} method.
     *
     * Checks that an AnalyticsManager singleton is created and accessible.
     */
    @Test
    public void testGetInstance() throws Exception {

        assertNotNull("AnalyticsManager singleton should not be null", mAnalyticsManager);
    }

    /**
     * Tests the {@link AnalyticsManager#setAnalyticsInterface(IAnalytics)} method.
     *
     * Checks that we can pass an IAnalytics interface to AnalyticsManager.
     */
    @Test
    public void testSetAnalyticsInterface() throws Exception {

        IAnalytics iAnalytics = new IAnalytics() {
            @Override
            public void configure(Context context) {

            }

            @Override
            public void collectLifeCycleData(Activity activity, boolean active) {

            }

            @Override
            public void trackAction(HashMap<String, Object> data) {

            }

            @Override
            public void trackState(String screen) {

            }

            @Override
            public void trackCaughtError(String errorMessage, Throwable t) {

            }
        };
        mAnalyticsManager.setAnalyticsInterface(iAnalytics);
        assertEquals("An IAnalytics interface should be been set.", mAnalyticsManager
                .getIAnalytics(), iAnalytics);
    }

    /**
     * Tests the {@link AnalyticsManager#getIAnalytics()} method.
     *
     * Should return null if setAnalyticsInterface() has never been called.
     */
    @Test
    public void testGetIAnalytics() throws Exception {

        assertNull("getIAnalytics() should be null before initialization", mAnalyticsManager
                .getIAnalytics());
    }

    /**
     * Tests the {@link AnalyticsManager#addAnalyticsConstantForActivity(String, String)} method.
     *
     * Should return AnalyticsManager singleton after taking in Activity name and constant,
     * and update the constant accordingly.
     */
    @Test
    public void testAddAnalyticsConstantForActivity() throws Exception {

        final String CONSTANT = "sample constant";
        assertEquals("addAnalyticsConstantForActivity() should return AnalyticsManager singleton",
                     mAnalyticsManager.addAnalyticsConstantForActivity(
                             mActivityRule.getClass().getSimpleName(),
                             CONSTANT),
                     mAnalyticsManager);
        assertEquals("Checking analytics constant", CONSTANT, mAnalyticsManager
                .getConstant(mActivityRule.getClass()
                                          .getSimpleName()));
    }

    /**
     * Tests the {@link AnalyticsManager#onActivityResumed(Activity)} method.
     *
     * Checks that method is recording tracking status.
     */
    @Test
    public void testOnActivityResumed() throws Exception {

        IAnalytics iAnalytics = new IAnalytics() {
            @Override
            public void configure(Context context) {

            }

            @Override
            public void collectLifeCycleData(Activity activity, boolean active) {

                if (active) {
                    Log.d(TAG, "Begin dummy data collection");
                }
                else {
                    Log.d(TAG, "End dummy data collection");
                }
            }

            @Override
            public void trackAction(HashMap<String, Object> data) {

            }

            @Override
            public void trackState(String screen) {

            }

            @Override
            public void trackCaughtError(String errorMessage, Throwable t) {

            }
        };

        clearLogs();
        mAnalyticsManager.setAnalyticsInterface(iAnalytics);
        mAnalyticsManager.onActivityResumed(mActivityRule.getActivity());
        assertTrue("onActivityResumed() should record tracking status in logs", checkConsole
                ("logcat -d AnalyticsManager:D *:S", "onActivityResumed, analytics tracking"));
        assertTrue("Checking for collectLifeCycleData() execution (active = true):", checkConsole
                ("logcat -d AnalyticsManagerTest:D *:S", "Begin dummy data collection"));

    }

    /**
     * Tests the {@link AnalyticsManager#onActivityPaused(Activity)} method.
     *
     * Checks that method is recording tracking status.
     */
    @Test
    public void testOnActivityPaused() throws Exception {

        IAnalytics iAnalytics = new IAnalytics() {
            @Override
            public void configure(Context context) {

            }

            @Override
            public void collectLifeCycleData(Activity activity, boolean active) {

                if (active) {
                    Log.d(TAG, "Begin dummy data collection");
                }
                else {
                    Log.d(TAG, "End dummy data collection");
                }
            }

            @Override
            public void trackAction(HashMap<String, Object> data) {

            }

            @Override
            public void trackState(String screen) {

            }

            @Override
            public void trackCaughtError(String errorMessage, Throwable t) {

            }
        };

        clearLogs();
        mAnalyticsManager.setAnalyticsInterface(iAnalytics);
        mAnalyticsManager.onActivityPaused(mActivityRule.getActivity());
        assertTrue("onActivityPause() should record tracking status in logs",
                   checkConsole("logcat -d AnalyticsManager:D *:S",
                                "onActivityPaused, analytics tracking"));

        assertTrue("Checking for collectLifeCycleData() execution (active = false):",
                   checkConsole("logcat -d AnalyticsManagerTest:D *:S",
                                "End dummy data collection"));
    }

}
