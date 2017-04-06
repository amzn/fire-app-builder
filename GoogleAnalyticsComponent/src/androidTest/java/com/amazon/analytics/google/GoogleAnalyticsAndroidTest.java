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

import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.google.resources.SampleActivity;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This class contains the Android portion of the GoogleAnalytics tests.
 */
@RunWith(AndroidJUnit4.class)
public class GoogleAnalyticsAndroidTest {

    final String TAG = this.getClass().getSimpleName();

    @Rule
    public ActivityTestRule<SampleActivity> mActivityRule = new ActivityTestRule<>(SampleActivity
                                                                                           .class);
    private GoogleAnalytics mGoogleAnalytics;

    @Before
    public void setUp() throws Exception {

        mGoogleAnalytics = new GoogleAnalytics();
    }

    @After
    public void tearDown() throws Exception {

        mGoogleAnalytics = null;
    }

    /**
     * Tests the {@link GoogleAnalytics#trackAction(HashMap)} method.
     * This portion of the test checks that the correct data is passed to the GoogleAnalytics
     * library.
     */
    @Test
    public void trackAction() throws Exception {

        final String EVENT_KEY = "&ea";
        final String DIMENSION_KEY1 = "&cd1";
        final String METRIC_KEY1 = "&cm1";

        mGoogleAnalytics.configure(mActivityRule.getActivity().getApplicationContext());
        final String TEST_ACTION = "newAction";
        HashMap<String, Object> dummyHashMap = new HashMap<>();
        // add test action
        dummyHashMap.put(AnalyticsTags.ACTION_NAME, TEST_ACTION);
        // HashMap to store attributes
        HashMap<String, Object> attributes = new HashMap<>();
        // test dimension attribute parsing
        attributes.put(AnalyticsTags.ATTRIBUTE_AD_SECONDS_WATCHED, 1);
        // test metric attribute parsing
        attributes.put(AnalyticsTags.ATTRIBUTE_PLATFORM, "test attribute");
        dummyHashMap.put(AnalyticsTags.ATTRIBUTES, attributes);
        mGoogleAnalytics.trackAction(dummyHashMap);
        Map<String, String> events = mGoogleAnalytics.getEvents();
        assertEquals("Custom Action name failed to match.", TEST_ACTION, events.get(EVENT_KEY));
        assertEquals("Dimension data failed to match.", "test attribute", events.get
                (DIMENSION_KEY1));
        assertEquals("Metric data failed to match.", "1.0", events.get(METRIC_KEY1));
    }
}
