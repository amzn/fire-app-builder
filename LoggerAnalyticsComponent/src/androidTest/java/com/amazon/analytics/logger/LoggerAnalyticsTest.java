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

package com.amazon.analytics.logger;

import com.amazon.analytics.logger.testresources.SampleActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.test.UiThreadTest;
import android.test.mock.MockContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import static org.junit.Assert.assertTrue;

/**
 * Test the {@link LoggerAnalytics} class
 */
@RunWith(AndroidJUnit4.class)
public class LoggerAnalyticsTest {

    @Rule
    public ActivityTestRule<SampleActivity> mActivityRule = new ActivityTestRule<>
            (SampleActivity.class);

    private final int CLEAR_LOG_DELAY = 2000;

    private final String LOG_CONFIGURATION_DONE = "Configuration done.";
    private final String LOG_TRACKING = "Tracking screen";
    private final String LOG_TRACKING_ACTION = "Tracking Action called";
    private final String LOG_LIFE_CYCLE = "Collecting life cycle data for activity:";
    private final String TEST_STRING = "test string";
    private final String ERROR_MESSAGE = "sample error message";

    private LoggerAnalytics mLoggerAnalytics;

    @Before
    public void setUp() throws Exception {

        clearLogs();
        mLoggerAnalytics = new LoggerAnalytics();
    }

    @After
    public void tearDown() throws Exception {

        mLoggerAnalytics = null;
    }

    /**
     * Check that console output contains the specified text
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

    /**
     * tests the {@link LoggerAnalytics#configure(Context)} method
     */
    @Test
    public void testConfigure() throws Exception {

        mLoggerAnalytics.configure(new MockContext());
        assertTrue("Log message for configure() not found",
                   checkConsole("logcat -d LoggerAnalytics:D *:S", LOG_CONFIGURATION_DONE));
    }

    /**
     * tests the {@link LoggerAnalytics#collectLifeCycleData(Activity, boolean)} method
     */
    @UiThreadTest
    @Test
    public void testCollectLifeCycleData() throws Exception {

        mLoggerAnalytics.collectLifeCycleData(mActivityRule.getActivity(), true);
        assertTrue("Log message for collectLifeCycleData() not found",
                   checkConsole("logcat -d LoggerAnalytics:D *:S", LOG_LIFE_CYCLE));
    }

    /**
     * tests the {@link LoggerAnalytics#trackAction(HashMap)} method
     */
    @Test
    public void testTrackAction() throws Exception {

        HashMap<String, Object> dummyHashMap = new HashMap<>();
        mLoggerAnalytics.trackAction(dummyHashMap);
        assertTrue("Log message for trackAction() not found",
                   checkConsole("logcat -d LoggerAnalytics:D *:S", LOG_TRACKING_ACTION));
    }

    /**
     * tests the {@link LoggerAnalytics#trackState(String)} method
     */
    @Test
    public void testTrackState() throws Exception {

        mLoggerAnalytics.trackState(TEST_STRING);
        assertTrue("Log message for trackState() not found",
                   checkConsole("logcat -d LoggerAnalytics:D *:S", LOG_TRACKING + " " +
                           TEST_STRING));
    }

    /**
     * tests the {@link LoggerAnalytics#trackCaughtError(String, Throwable)} method
     */
    @Test
    public void testTrackCaughtError() throws Exception {

        mLoggerAnalytics.trackCaughtError(ERROR_MESSAGE, new Exception());
        assertTrue("Log message for trackCaughtError() not found",
                   checkConsole("logcat -d LoggerAnalytics:E *:S", ERROR_MESSAGE));
    }
}
