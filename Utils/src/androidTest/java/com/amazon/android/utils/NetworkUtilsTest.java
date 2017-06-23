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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

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

    /**
     * Tests the {@link NetworkUtils#urlContainsParameter(String, String)} method.
     */
    @Test
    public void testUrlContainsParameter() throws Exception {

        String par1 = "param1";
        String par2 = "app_id";
        String par3 = "action";
        String par4 = "empty";
        String url1 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos";
        String url2 = url1 + "&" + par4 + "=";
        String url3 = "www.badurls.comm";

        assertFalse("Parameter should not be found", NetworkUtils.urlContainsParameter(url1, par1));
        assertTrue("Parameter should be found", NetworkUtils.urlContainsParameter(url1, par2));
        assertTrue("Parameter should be found", NetworkUtils.urlContainsParameter(url1, par3));
        assertFalse("Parameter should not be found", NetworkUtils.urlContainsParameter(url2, par4));
        assertFalse("URL is bad, should be false", NetworkUtils.urlContainsParameter(url3, par1));
    }

    /**
     * Tests the {@link NetworkUtils#addParameterToUrl(String, String, String)} method.
     */
    @Test
    public void testAddParameterToUrl() throws Exception {

        String url1 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos";
        String url2 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos&newparam=";
        String url3 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&newparam=&action" +
                "=channels_videos";
        String url4 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos&newparam=differentvalue";

        String url5 = "http://www.someurlwithoutaquery.com";

        String param = "newparam";
        String value = "value1";

        // Contains new param at the end
        String expected1 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos&newparam=value1";
        // Contains new param in the middle
        String expected2 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&newparam=value1&action" +
                "=channels_videos";
        // Contains a different value for newparam.
        String expected3 = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action" +
                "=channels_videos&newparam=differentvalue";

        // Test adding a new parameter completely
        assertEquals("value1 should have been added.",
                     expected1, NetworkUtils.addParameterToUrl(url1, param, value));
        // Test adding a new parameter if it just has no value in the url
        assertEquals("value1 should have been added.",
                     expected1, NetworkUtils.addParameterToUrl(url2, param, value));
        // Test adding a new parameter into the middle of a url
        assertEquals("value1 should have been added.",
                     expected2, NetworkUtils.addParameterToUrl(url3, param, value));
        // Test adding a new parameter to url that already contains the parameter.
        assertEquals("value1 should not have been added.",
                     expected3, NetworkUtils.addParameterToUrl(url4, param, value));
        // Test adding a new parameter to a url that doesn't have a query.
        assertEquals("value1 should not have been added.",
                     url5, NetworkUtils.addParameterToUrl(url5, param, value));
    }
}
