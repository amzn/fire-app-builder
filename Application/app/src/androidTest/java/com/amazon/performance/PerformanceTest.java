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

package com.amazon.performance;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.ui.activities.SplashActivity;
import com.amazon.testresources.TestConfig;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ErrorCollector;
import org.junit.runner.RunWith;

import android.app.ActivityManager;
import android.content.Context;
import android.os.Debug;
import android.support.test.InstrumentationRegistry;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;
import android.view.KeyEvent;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test suite containing performance tests.
 */
@RunWith(AndroidJUnit4.class)
public class PerformanceTest {

    @Rule
    public ActivityTestRule<SplashActivity> mActivityTestRule = new ActivityTestRule<>
            (SplashActivity.class);

    @Rule
    public ErrorCollector errors = new ErrorCollector();

    private SplashActivity mSplashActivity;

    private Solo solo;
    private ActivityManager mActivityManager;
    private int[] calypsoPID;
    private int count;
    private boolean isSampleFeed;
    private long loadTime;

    final private double LIMIT = TestConfig.LIMIT_MULTIPLIER;
    final private int[] SAMPLE_SIZES = {10, 100, 500, 5000};

    final private String TAG = this.getClass().getSimpleName();

    @Before
    public void setUp() throws Exception {

        mSplashActivity = mActivityTestRule.getActivity();
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), mSplashActivity);
        init();
    }

    @After
    public void tearDown() throws Exception {

        solo = null;
    }

    /**
     * Tests the following areas:
     *
     * 1. Tests how long it takes for content to load.
     * 2. Checks that memory usage stays within acceptable limits.
     *    Intended to detect code changes that result in increased memory consumption.
     *
     * Important: consider disabling the screen saver prior to this case, especially for large
     * feeds. Otherwise, the test may get stuck after the splash screen disappears.
     */
    @Test(timeout = 500000)
    public void testPerformance() throws Exception {

        doMemoryTest();
        doPerformanceTest();
    }

    /**
     * Set up environment for testing.
     */
    private void init() throws Exception {

        final String APP_NAME = "com.fireappbuilder.android.calypso";

        // TODO: handle situations in which clearing logs takes an arbitrary amount of time
        // not needed for later versions of Android as Logcat supports time-based filtering
        Runtime.getRuntime().exec("logcat -c");

        // use ActivityManager to measure memory usage
        mActivityManager = (ActivityManager) mSplashActivity.getApplicationContext()
                                                            .getSystemService(Context.ACTIVITY_SERVICE);
        ActivityManager.RunningAppProcessInfo calypsoProcess = null;
        // find the process for Calypso
        for (ActivityManager.RunningAppProcessInfo p : mActivityManager.getRunningAppProcesses()) {
            if (p.processName.equals(APP_NAME)) {
                calypsoProcess = p;
                break;
            }
        }
        assertNotNull("Calypso process not found.", calypsoProcess);
        calypsoPID = new int[]{calypsoProcess.pid};

        long startTime = System.currentTimeMillis();
        solo.waitForView(R.id.full_content_browse_fragment, 0, TestConfig.LONG_TIMEOUT);
        loadTime = (int) (System.currentTimeMillis() - startTime);

        // get item count
        count = 0;
        ContentContainer mContentContainer = ContentBrowser.getInstance(mSplashActivity)
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
    }

    /**
     * Test initialization time.
     */
    private void doPerformanceTest() throws Exception {

        Log.i(TAG, "Feed loaded, number of distinct items = " + count);
        if (!isSampleFeed) {
            Log.w(TAG, "You do not appear to be using a sample feed. Testing on live feed " +
                    "is unsupported as it is not static and subject to change.");
        }
        else {
            Log.i(TAG, "Using sample feed with " + count + " items");
        }
        Log.i(TAG, "Loading time: " + loadTime + " ms");

        int SPLASH_LOAD_TIME_LIMIT_MS = TestConfig.loadTimeLimit(count);

        if (isSampleFeed) {
            try {
                assertTrue("Splash screen took longer to load than " + SPLASH_LOAD_TIME_LIMIT_MS
                                   + " ms", loadTime < SPLASH_LOAD_TIME_LIMIT_MS * LIMIT);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
        }
    }

    /**
     * Test memory usage before and after starting video.
     */
    private void doMemoryTest() throws Exception {

        // time for memory usage to change after starting or starting video
        final int TRANSITION_TIME_MS = 10000;
        // continue playing video for pre-determined amount of time
        final int VIDEO_PLAYBACK_TIME_MS = 10000;

        // get expected memory values
        int baseMemoryUsage = TestConfig.getBaseMemory(count);
        int baseVideoMemoryUsage = TestConfig.getBaseVideoMemory(count);

        int currentUsage = getMemoryUsage();

        // check initial memory usage
        Log.i(TAG, "Initial memory usage = " + currentUsage + " kB");
        if (isSampleFeed) {
            assertTrue(checkMemoryUsage(baseMemoryUsage, currentUsage), currentUsage <
                    LIMIT * baseMemoryUsage);
        }
        solo.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
        solo.waitForView(R.id.content_details_fragment);
        solo.sendKey(KeyEvent.KEYCODE_DPAD_CENTER);
        solo.waitForView(R.id.playback_controls_fragment);
        Thread.sleep(TRANSITION_TIME_MS);
        // check memory usage after starting video
        currentUsage = getMemoryUsage();
        Log.i(TAG, "Current memory usage after starting video = " + currentUsage + " kB");
        if (isSampleFeed) {
            try {
                assertTrue(checkMemoryUsage(baseVideoMemoryUsage, currentUsage), currentUsage <
                        LIMIT * baseVideoMemoryUsage);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
        }
        Thread.sleep(VIDEO_PLAYBACK_TIME_MS);
        // check memory usage after playing video for some time
        currentUsage = getMemoryUsage();
        if (isSampleFeed) {
            try {
                assertTrue(checkMemoryUsage(baseVideoMemoryUsage, currentUsage), currentUsage <
                        LIMIT * baseVideoMemoryUsage);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
        }
        solo.goBack();
        // check memory usage after going back to details Fragment
        currentUsage = getMemoryUsage();
        Log.i(TAG, "Current memory usage after finishing video = " + currentUsage + " kB");
        if (isSampleFeed) {
            try {
                assertTrue(checkMemoryUsage(baseVideoMemoryUsage, currentUsage), currentUsage <
                        LIMIT * baseVideoMemoryUsage);
            }
            catch (Throwable t) {
                errors.addError(t);
            }
        }
    }

    /**
     * Get String containing memory usage information.
     *
     * @param expected Expected memory usage.
     * @param actual   Actual memory usage.
     * @return String containing memory usage information.
     */
    private String checkMemoryUsage(int expected, int actual) {

        String logInfo = "";
        if (actual > expected * LIMIT) {
            logInfo += "Memory usage exceeded expected value by 10% or more. ";
        }
        logInfo += "Expected maximum memory usage = " + expected + " kB, actual usage = " +
                actual + " kB";
        return logInfo;
    }

    /**
     * Get memory usage.
     *
     * @return Current memory usage in kB.
     */
    private int getMemoryUsage() {

        Debug.MemoryInfo[] memoryInfo = mActivityManager.getProcessMemoryInfo(calypsoPID);
        return memoryInfo[0].dalvikPss;
    }
}
