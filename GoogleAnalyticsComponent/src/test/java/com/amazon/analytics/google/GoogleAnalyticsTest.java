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

package com.amazon.analytics.google;

import com.google.android.gms.analytics.Tracker;

import com.amazon.analytics.AnalyticsTags;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

import static junit.framework.Assert.assertTrue;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyInt;
import static org.mockito.Mockito.verify;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.doReturn;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test suite for the {@link GoogleAnalytics} class
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest(com.google.android.gms.analytics.GoogleAnalytics.class)
public class GoogleAnalyticsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Mock
    Context mContext;

    @Mock
    Resources mResources;

    @Mock
    AssetManager mAssetManager;

    @Mock
    Tracker mTracker;

    @Mock
    com.google.android.gms.analytics.GoogleAnalytics mAnalytics;

    private GoogleAnalytics mGoogleAnalytics;

    @Before
    public void setUp() throws Exception {

        mGoogleAnalytics = new GoogleAnalytics();
        initMocks(this);
        doReturn(mContext).when(mContext).getApplicationContext();
        doReturn(mResources).when(mContext).getResources();
        PowerMockito.mockStatic(com.google.android.gms.analytics.GoogleAnalytics.class);
        when(com.google.android.gms.analytics.GoogleAnalytics.getInstance(mContext)).thenReturn
                (mAnalytics);
        when(mAnalytics.newTracker(anyInt())).thenReturn(mTracker);
        when(mContext.getAssets()).thenReturn(mAssetManager);
    }

    @After
    public void tearDown() throws Exception {

        mGoogleAnalytics = null;
    }

    /**
     * Tests the {@link GoogleAnalytics#configure(Context)} method.
     * Checks that we are getting an instance of Google's own GoogleAnalytics object.
     */
    @Test
    public void configure() throws Exception {

        mGoogleAnalytics.configure(mContext);
        com.google.android.gms.analytics.GoogleAnalytics analytics =
                PowerMockito.mock(com.google.android.gms.analytics.GoogleAnalytics.class);
        PowerMockito.verifyStatic();
        analytics.getInstance(mContext);
        verify(mAnalytics).setLocalDispatchPeriod(anyInt());

        // check against invalid input
        try {
            mGoogleAnalytics.configure(null);
        }
        catch (Exception e) {
            // hack: ExpectedException doesn't seem to be compatible with the Robolectric runner
            assertTrue("configure() should not accept a null Context.", e instanceof
                    NullPointerException);
        }
    }


    /**
     * Tests the {@link GoogleAnalytics#trackAction(HashMap)} method
     * Checks that action tracking works and that data object is processed correctly.
     *
     * Note: Configuration files cannot be accessed in a pure Java unit test. To check that the
     * correct data is being passed to GoogleAnalytics, run the Android test in the androidTest
     * folder.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void trackAction() throws Exception {

        mGoogleAnalytics.configure(mContext);
        final String TEST_ACTION = "newAction";
        HashMap<String, Object> dummyHashMap = new HashMap<>();
        dummyHashMap.put(AnalyticsTags.ACTION_NAME, TEST_ACTION);
        dummyHashMap.put(AnalyticsTags.ATTRIBUTES, new HashMap<String, Object>());
        mGoogleAnalytics.trackAction(dummyHashMap);
        verify(mTracker).send(any(Map.class));
    }

    /**
     * Tests the {@link GoogleAnalytics#trackState(String)} method.
     * Checks that screen name and impression data are passed to the tracker.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void trackState() throws Exception {

        mGoogleAnalytics.configure(mContext);
        mGoogleAnalytics.trackState("testScreen");
        verify(mTracker).setScreenName("testScreen");
        verify(mTracker).send(any(Map.class));
    }

    /**
     * Tests the {@link GoogleAnalytics#trackCaughtError(String, Throwable)} method.
     * Checks that the error message and Exception are caught by the tracker.
     */
    @SuppressWarnings("unchecked")
    @Test
    public void trackCaughtError() throws Exception {

        mGoogleAnalytics.configure(mContext);
        final String ERROR_MESSAGE = "sample error message";
        mGoogleAnalytics.trackCaughtError(ERROR_MESSAGE, new Exception());
        verify(mTracker).send(any(Map.class));
    }
}