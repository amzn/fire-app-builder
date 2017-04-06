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

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.powermock.core.classloader.annotations.PowerMockIgnore;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link GoogleAnalyticsImplCreator} class
 */
@RunWith(RobolectricTestRunner.class)
@Config(constants = BuildConfig.class, sdk = 21, manifest = Config.NONE)
@PowerMockIgnore({"org.mockito.*", "org.robolectric.*", "android.*", "org.json.*"})
public class GoogleAnalyticsImplCreatorTest {

    private GoogleAnalyticsImplCreator mGoogleAnalyticsImplCreator;

    @Before
    public void setUp() throws Exception {

        mGoogleAnalyticsImplCreator = new GoogleAnalyticsImplCreator();
    }

    @After
    public void tearDown() throws Exception {

        mGoogleAnalyticsImplCreator = null;
    }

    /**
     * Tests the {@link GoogleAnalyticsImplCreator#createImpl()} method.
     *
     * Checks that an GoogleAnalytics object can be created.
     */
    @Test
    public void createImpl() throws Exception {

        assertTrue("createImpl() should create an GoogleAnalytics object",
                   mGoogleAnalyticsImplCreator.createImpl() instanceof GoogleAnalytics);
    }
}
