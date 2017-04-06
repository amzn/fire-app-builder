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

package com.amazon.analytics.omniture;

import com.amazon.analytics.AnalyticsActionBuilder;
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

import android.app.Activity;
import android.content.Context;
import android.content.res.AssetManager;
import android.content.res.Resources;

import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.times;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.mock;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;
import static org.powermock.api.mockito.PowerMockito.when;

/**
 * Test suite for the {@link OmnitureAnalytics} class
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
@PrepareForTest({com.adobe.mobile.Config.class, com.adobe.mobile.Analytics.class})
public class OmnitureAnalyticsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();

    @Mock
    AssetManager mAssetManager;

    @Mock
    Context mContext;

    @Mock
    Resources mResources;

    private OmnitureAnalytics mOmnitureAnalytics;

    @Before
    public void setUp() throws Exception {

        initMocks(this);
        doReturn(mAssetManager).when(mContext).getAssets();
        doReturn(mContext).when(mContext).getApplicationContext();
        doReturn(mResources).when(mContext).getResources();
        PowerMockito.mockStatic(com.adobe.mobile.Config.class);
        PowerMockito.mockStatic(com.adobe.mobile.Analytics.class);
        mOmnitureAnalytics = new OmnitureAnalytics();
    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * Tests the {@link OmnitureAnalytics#configure(Context)} method.
     *
     * Checks that the configuration is being set.
     */
    @Test
    public void configure() throws Exception {

        mOmnitureAnalytics.configure(mContext);
        verifyStatic(times(1));
        com.adobe.mobile.Config.setContext(mContext);
    }

    /**
     * Tests the {@link OmnitureAnalytics#collectLifeCycleData(Activity, boolean)} method.
     *
     * Checks that the code for collecting life cycle data is executed/
     */
    @Test
    public void collectLifeCycleData() throws Exception {

        Activity activity = mock(Activity.class);
        // check case: start collection of life cycle data
        mOmnitureAnalytics.collectLifeCycleData(activity, true);
        verifyStatic(times(1));
        com.adobe.mobile.Config.collectLifecycleData(activity);
        // check case: end collection of life cycle data
        mOmnitureAnalytics.collectLifeCycleData(activity, false);
        verifyStatic(times(1));
        com.adobe.mobile.Config.pauseCollectingLifecycleData();
    }

    /**
     * Tests the {@link OmnitureAnalytics#trackAction(HashMap)} method
     *
     * Checks that action tracking works and that data object is processed correctly.
     */
    @Test
    public void trackAction() throws Exception {

        final String TEST_ACTION = "newAction";
        HashMap<String, Object> dummyHashMap = new HashMap<>();
        dummyHashMap.put(AnalyticsTags.ACTION_NAME, TEST_ACTION);
        dummyHashMap.put(AnalyticsTags.ATTRIBUTES, new HashMap<String, Object>());
        mOmnitureAnalytics.trackAction(dummyHashMap);
        verifyStatic(times(1));
        com.adobe.mobile.Analytics.trackAction(TEST_ACTION, (HashMap<String, Object>)
                dummyHashMap.get(AnalyticsTags.ATTRIBUTES));
    }

    /**
     * Tests the {@link OmnitureAnalytics#trackState(String)} method
     *
     * Checks tracking of screen state.
     */
    @Test
    public void trackState() throws Exception {

        final String TEST_STRING = "test string";
        mOmnitureAnalytics.trackState(TEST_STRING);
        HashMap<String, Object> data = AnalyticsActionBuilder.buildTimeDateData();
        verifyStatic(times(1));
        com.adobe.mobile.Analytics.trackState(TEST_STRING, data);
    }

    /**
     * Tests the {@link OmnitureAnalytics#trackCaughtError(String, Throwable)} method
     *
     * Check that detected errors are being recorded.
     */
    @Test
    public void trackCaughtError() throws Exception {

        final String ERROR_MESSAGE = "sample error message";
        mOmnitureAnalytics.trackCaughtError(ERROR_MESSAGE, new Exception());
        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsTags.ACTION_NAME, AnalyticsTags.ACTION_ERROR);
        verifyStatic(times(1));
        com.adobe.mobile.Analytics.trackAction(any(String.class), any(Map.class));
    }
}