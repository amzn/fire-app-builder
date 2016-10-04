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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.spy;

/**
 * This class tests the {@link BasicTokenBasedUrlGenerator} class.
 */
public class BasicTokenBasedUrlGeneratorTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    /**
     * Tests the {@link BasicTokenBasedUrlGenerator#createInstance(Context)} method. Validates
     * that the instance created is valid.
     */
    @Test
    public void createInstanceTest() throws AObjectCreator.ObjectCreatorException, AUrlGenerator
            .UrlGeneratorException {

        assertNotNull(BasicTokenBasedUrlGenerator.createInstance(context));
        assertTrue(BasicTokenBasedUrlGenerator.createInstance(context) instanceof
                           BasicTokenBasedUrlGenerator);
    }

    /**
     * Tests the {@link BasicTokenBasedUrlGenerator#getUrl(Map)} method for the positive case
     * with a base url and a token generation url present in params.
     */
    @Test
    public void testGetUrlWithKeysInParams() throws AObjectCreator
            .ObjectCreatorException, AUrlGenerator.UrlGeneratorException, IOException {

        BasicTokenBasedUrlGenerator urlGenerator = new BasicTokenBasedUrlGenerator(context);
        BasicTokenBasedUrlGenerator spyUrlGenerator = spy(urlGenerator);
        doReturn("token").when(spyUrlGenerator).requestToken("tokenGenerationUrl");

        Map<String, String> params = new HashMap<>();
        params.put(BasicTokenBasedUrlGenerator.BASE_URL, "baseUrlPrefix$$token$$baseUrlSuffix");
        params.put(BasicTokenBasedUrlGenerator.TOKEN_GENERATION_URL, "tokenGenerationUrl");
        String url = spyUrlGenerator.getUrl(params);
        assertEquals("baseUrlPrefixtokenbaseUrlSuffix", url);
    }

    /**
     * Tests the {@link BasicTokenBasedUrlGenerator#getUrl(Map)} method for the positive case with
     * a base url and a token generation url not present in params
     */
    @Test
    public void testGetUrlWithKeysNotInParams() throws AObjectCreator
            .ObjectCreatorException, AUrlGenerator.UrlGeneratorException, IOException {

        BasicTokenBasedUrlGenerator urlGenerator = new BasicTokenBasedUrlGenerator(context);
        BasicTokenBasedUrlGenerator spyUrlGenerator = spy(urlGenerator);
        doReturn("token").when(spyUrlGenerator).requestToken("config_token_generation_url");

        Map<String, String> params = new HashMap<>();
        String url = spyUrlGenerator.getUrl(params);
        assertEquals("config_base_urltokenconfig_base_url_postfix", url);
    }

    /**
     * Tests the {@link BasicTokenBasedUrlGenerator#getUrl(Map)} method for the negative case with
     * an incorrect token url passed in params.
     */
    @Test(expected = AUrlGenerator.UrlGeneratorException.class)
    public void testGetUrlWithIncorrectTokenUrl() throws AObjectCreator.ObjectCreatorException,
            AUrlGenerator.UrlGeneratorException {

        BasicTokenBasedUrlGenerator urlGenerator = new BasicTokenBasedUrlGenerator(context);
        Map<String, String> params = new HashMap<>();
        params.put(BasicTokenBasedUrlGenerator.BASE_URL, "baseUrl");
        params.put(BasicTokenBasedUrlGenerator.TOKEN_GENERATION_URL, "token");
        urlGenerator.getUrl(params);
    }
}
