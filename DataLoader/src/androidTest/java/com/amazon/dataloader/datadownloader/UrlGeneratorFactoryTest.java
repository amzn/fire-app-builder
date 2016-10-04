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

import com.amazon.dataloader.testResources.MockUrlGenerator;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link UrlGeneratorFactory} class by using a {@link MockUrlGenerator} class
 * that extends the {@link AUrlGenerator} abstract class.
 */
public class UrlGeneratorFactoryTest {

    private String urlGeneratorClassPath = MockUrlGenerator.class.getName();
    private Context context = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() throws AObjectCreator.ObjectCreatorException {

        MockUrlGenerator.mMockUrlGenerator = new MockUrlGenerator(context);
    }

    /**
     * Tests the {@link UrlGeneratorFactory#createUrlGenerator(Context, String)} method to validate
     * the successful creation of an {@link AUrlGenerator} instance.
     */
    @Test
    public void testCreateUrlGenerator() throws UrlGeneratorFactory
            .UrlGeneratorInitializationFailedException {

        UrlGeneratorFactory.createUrlGenerator(context, urlGeneratorClassPath);
        assertEquals(MockUrlGenerator.mMockUrlGenerator, UrlGeneratorFactory.createUrlGenerator
                (context, urlGeneratorClassPath));
    }

    /**
     * Tests the {@link UrlGeneratorFactory#createUrlGenerator(Context, String)} method using a
     * non-existent {@link AUrlGenerator} implementation.
     */
    @Test(expected = UrlGeneratorFactory.UrlGeneratorInitializationFailedException.class)
    public void testUrlGeneratorNotFoundException() throws UrlGeneratorFactory
            .UrlGeneratorInitializationFailedException {

        UrlGeneratorFactory.createUrlGenerator(context, "NonExistentUrlGenerator");
    }
}
