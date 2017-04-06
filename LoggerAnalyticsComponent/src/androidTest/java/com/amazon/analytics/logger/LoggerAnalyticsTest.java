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

import com.amazon.analytics.logger.resources.SampleActivity;
import com.amazon.android.utils.Helpers;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.support.test.InstrumentationRegistry;
import android.test.UiThreadTest;
import android.test.mock.MockContext;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.HashMap;

import static com.amazon.android.utils.Helpers.checkConsole;
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

    private final String LOGCAT_COMMAND_DEBUG = "logcat -d LoggerAnalytics:D *:S";
    private final String LOGCAT_COMMAND_ERROR = "logcat -d LoggerAnalytics:E *:S";
    private final String LOG_CONFIGURATION_DONE = "Configuration done.";
    private final String LOG_TRACKING = "Tracking screen";
    private final String LOG_TRACKING_ACTION = "Tracking action";
    private final String LOG_LIFE_CYCLE = "Collecting life cycle data for activity:";
    private final String TEST_STRING = "test string";
    private final String ERROR_MESSAGE = "sample error message";

    private final Context mContext = InstrumentationRegistry.getContext();
    private LoggerAnalytics mLoggerAnalytics;

    @Before
    public void setUp() throws Exception {

        Helpers.clearLogs(CLEAR_LOG_DELAY);
        mLoggerAnalytics = new LoggerAnalytics();
    }

    @After
    public void tearDown() throws Exception {

        mLoggerAnalytics = null;
    }

    /**
     * tests the {@link LoggerAnalytics#configure(Context)} method
     */
    @Test
    public void testConfigure() throws Exception {

        mLoggerAnalytics.configure(new MockContext() {
            @Override
            public Resources getResources() {

                return mContext.getResources();

            }

            @Override
            public AssetManager getAssets() {

                return mContext.getAssets();
            }
        });

        assertTrue("Log message for configure() not found",
                   checkConsole(LOGCAT_COMMAND_DEBUG, LOG_CONFIGURATION_DONE));
    }

    /**
     * tests the {@link LoggerAnalytics#collectLifeCycleData(Activity, boolean)} method
     */
    @UiThreadTest
    @Test
    public void testCollectLifeCycleData() throws Exception {

        mLoggerAnalytics.collectLifeCycleData(mActivityRule.getActivity(), true);
        assertTrue("Log message for collectLifeCycleData() not found",
                   checkConsole(LOGCAT_COMMAND_DEBUG, LOG_LIFE_CYCLE));
    }

    /**
     * tests the {@link LoggerAnalytics#trackAction(HashMap)} method
     */
    @Test
    public void testTrackAction() throws Exception {

        HashMap<String, Object> dummyHashMap = new HashMap<>();
        mLoggerAnalytics.trackAction(dummyHashMap);
        assertTrue("Log message for trackAction() not found",
                   checkConsole(LOGCAT_COMMAND_DEBUG, LOG_TRACKING_ACTION));
    }

    /**
     * tests the {@link LoggerAnalytics#trackState(String)} method
     */
    @Test
    public void testTrackState() throws Exception {

        mLoggerAnalytics.trackState(TEST_STRING);
        assertTrue("Log message for trackState() not found",
                   checkConsole(LOGCAT_COMMAND_DEBUG, LOG_TRACKING + " " + TEST_STRING));
    }

    /**
     * tests the {@link LoggerAnalytics#trackCaughtError(String, Throwable)} method
     */
    @Test
    public void testTrackCaughtError() throws Exception {

        mLoggerAnalytics.trackCaughtError(ERROR_MESSAGE, new Exception());
        assertTrue("Log message for trackCaughtError() not found",
                   checkConsole(LOGCAT_COMMAND_ERROR, ERROR_MESSAGE));
    }
}