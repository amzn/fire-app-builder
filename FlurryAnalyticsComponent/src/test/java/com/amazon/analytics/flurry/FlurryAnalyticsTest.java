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
package com.amazon.analytics.flurry;

import com.flurry.android.FlurryAgent;

import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.powermock.modules.junit4.rule.PowerMockRule;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import android.app.Activity;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

import static org.mockito.Matchers.anyBoolean;
import static org.mockito.Matchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.MockitoAnnotations.initMocks;
import static org.powermock.api.mockito.PowerMockito.verifyStatic;

/**
 * Tests for {@link FlurryAnalytics}
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 18, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*"})
@PrepareForTest(FlurryAgent.class)
public class FlurryAnalyticsTest {

    @Rule
    public PowerMockRule rule = new PowerMockRule();
    private FlurryAnalytics mFlurryAnalytics;

    @Before
    public void setUp() {

        mFlurryAnalytics = new FlurryAnalytics();
        initMocks(this);
        PowerMockito.mockStatic(FlurryAgent.class);
    }

    /**
     * Tests for {@link FlurryAnalytics#collectLifeCycleData(Activity, boolean)}
     */
    @Test
    public void testCollectLifeCycleData() {

        Activity activity = mock(Activity.class);
        //Test start of activity
        mFlurryAnalytics.collectLifeCycleData(activity, true);
        verifyStatic();
        FlurryAgent.logEvent(anyString(), anyBoolean());
        //Test end of activity
        mFlurryAnalytics.collectLifeCycleData(activity, false);
        verifyStatic();
        FlurryAgent.endTimedEvent(anyString());
    }

    /**
     * Tests for {@link FlurryAnalytics#trackAction(HashMap)}
     */
    @Test
    public void testTrackAction() {

        HashMap<String, Object> map = new HashMap<>();
        map.put("action", "testAction");

        //Test for valid attribute Map
        HashMap<String, String> contextData = new HashMap<>();
        contextData.put("key", "value");
        map.put("attributes", contextData);
        mFlurryAnalytics.trackAction(map);
        verifyStatic();
        FlurryAgent.logEvent("testAction", contextData);

        //Test for invalid attribute Map
        map.put("attributes", "testValue");
        mFlurryAnalytics.trackAction(map);
        verifyStatic();
        FlurryAgent.logEvent("testAction");
    }

    /**
     * Tests for {@link FlurryAnalytics#trackState(String)}
     */
    @Test
    public void testTrackState() {

        mFlurryAnalytics.trackState("testScreen");
        verifyStatic();
        FlurryAgent.logEvent("testScreen");
    }
}