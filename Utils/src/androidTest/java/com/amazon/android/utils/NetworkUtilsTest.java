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
package com.amazon.android.utils;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.io.IOException;

import static org.junit.Assert.assertNotNull;

/**
 * Tests for {@link NetworkUtils}
 */
public class NetworkUtilsTest {

    private Context mContext;

    @Before
    public void setUp() {

        mContext = InstrumentationRegistry.getTargetContext();
    }

    /**
     * Tests fetching data from valid URL
     */
    @Test
    public void testGetDataLocatedAtUrlWithValidUrl() throws IOException {

        String data = NetworkUtils
                .getDataLocatedAtUrl("http://www.lightcast.com/api/firetv/channels" +
                                             ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                                             "=channels_videos");
        assertNotNull(data);
    }

    /**
     * Tests fetching data from invalid URL
     */
    @Test(expected = IOException.class)
    public void testGetDataLocatedAtUrlWithInvalidUrl() throws IOException {

        NetworkUtils.getDataLocatedAtUrl("");
    }
}
