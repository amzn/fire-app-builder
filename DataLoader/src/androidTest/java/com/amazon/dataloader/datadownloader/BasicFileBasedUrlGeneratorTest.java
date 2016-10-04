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
package com.amazon.dataloader.datadownloader;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link BasicFileBasedUrlGenerator} class.
 */
public class BasicFileBasedUrlGeneratorTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    /**
     * Tests for {@link BasicFileBasedUrlGenerator#BasicFileBasedUrlGenerator(Context)}
     * constructor.
     */
    @Test
    public void testConstructor() throws AObjectCreator.ObjectCreatorException {

        BasicFileBasedUrlGenerator urlGenerator = new BasicFileBasedUrlGenerator(context);
        assertEquals("testValue", urlGenerator.mConfiguration.getItemAsString("testKey"));
    }

    /**
     * Tests the {@link BasicFileBasedUrlGenerator#createInstance(Context)} method. Validates that
     * the instance created is valid.
     */
    @Test
    public void createInstanceTest() throws AObjectCreator.ObjectCreatorException {

        assertNotNull(BasicFileBasedUrlGenerator.createInstance(context));
        assertTrue(BasicFileBasedUrlGenerator.createInstance(context) instanceof
                           BasicFileBasedUrlGenerator);
    }

    /**
     * Tests the {@link BasicFileBasedUrlGenerator#getUrl(Map)} method for the positive case with
     * keys that are not in the params argument.
     */
    @Test
    public void testGetUrlHappyCaseWithKeysNotInParams() throws AObjectCreator
            .ObjectCreatorException, AUrlGenerator
            .UrlGeneratorException {

        BasicFileBasedUrlGenerator urlGenerator = new BasicFileBasedUrlGenerator(context);
        Map<String, String> params = new HashMap<>();
        params.put("url_index", "1");
        assertEquals("testUrl2", urlGenerator.getUrl(params));
    }

    /**
     * Tests the {@link BasicFileBasedUrlGenerator#getUrl(Map)} method for the positive case with
     * keys in the params argument.
     */
    @Test
    public void testGetUrlHappyCaseWithKeysInParams() throws AObjectCreator
            .ObjectCreatorException, AUrlGenerator
            .UrlGeneratorException {

        BasicFileBasedUrlGenerator urlGenerator = new BasicFileBasedUrlGenerator(context);
        Map<String, String> params = new HashMap<>();
        params.put("url_file", "SampleUrls.json");
        params.put("url_index", "1");
        assertEquals("testUrl2", urlGenerator.getUrl(params));
    }

    /**
     * Tests the {@link BasicFileBasedUrlGenerator#getUrl(Map)} method for an invalid file.
     */
    @Test(expected = AUrlGenerator.UrlGeneratorException.class)
    public void testGetUrlWithInvalidFile() throws AObjectCreator.ObjectCreatorException,
            AUrlGenerator.UrlGeneratorException {

        BasicFileBasedUrlGenerator urlGenerator = new BasicFileBasedUrlGenerator(context);
        Map<String, String> params = new HashMap<>();
        params.put("url_file", "dummyFile");
        params.put("url_index", "1");
        urlGenerator.getUrl(params);
    }

    /**
     * Tests the {@link BasicFileBasedUrlGenerator#getUrl(Map)} method for an invalid index.
     */
    @Test(expected = AUrlGenerator.UrlGeneratorException.class)
    public void testGetUrlWithInvalidIndex() throws AObjectCreator.ObjectCreatorException,
            AUrlGenerator.UrlGeneratorException {

        BasicFileBasedUrlGenerator urlGenerator = new BasicFileBasedUrlGenerator(context);
        Map<String, String> params = new HashMap<>();
        params.put("url_file", "SampleUrls.json");
        params.put("url_index", "-1");
        urlGenerator.getUrl(params);
    }

}
