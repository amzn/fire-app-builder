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

import com.amazon.dataloader.testResources.MockObjectCreator;
import com.amazon.dataloader.testResources.MockSingletonObjectCreator;

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import java.lang.reflect.InvocationTargetException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class tests the {@link AObjectCreator} class.
 */
public class AObjectCreatorTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    /**
     * Tests whether the constructor is initializing the required objects correctly.
     */
    @Test
    public void testConstructor() throws AUrlGenerator.UrlGeneratorException, AObjectCreator
            .ObjectCreatorException {

        AObjectCreator objectCreator = new MockObjectCreator(context);
        assertEquals(context.getApplicationContext(), objectCreator.mContext);
        assertNotNull(objectCreator.mConfiguration);
        assertEquals("testValue", objectCreator.mConfiguration.getItemAsString("testKey"));
    }

    /**
     * Tests {@link AObjectCreator#fetchInstanceViaReflection(Context, String)} for non singleton
     * class.
     */
    @Test
    public void testCreateInstanceViaReflectionForNonSingletonCase() throws
            ClassNotFoundException, NoSuchMethodException, InvocationTargetException,
            IllegalAccessException {

        AObjectCreator objectCreator =
                AObjectCreator.fetchInstanceViaReflection(context, MockObjectCreator.class
                        .getName());
        assertEquals("testValue", objectCreator.mConfiguration.getItemAsString("testKey"));
    }

    /**
     * Tests {@link AObjectCreator#fetchInstanceViaReflection(Context, String)} for singleton
     * class.
     */
    @Test
    public void testCreateInstanceViaReflectionForSingletonCase() throws ClassNotFoundException,
            NoSuchMethodException, InvocationTargetException, IllegalAccessException {

        AObjectCreator objectCreator =
                AObjectCreator.fetchInstanceViaReflection(context, MockSingletonObjectCreator
                        .class.getName());
        assertEquals("testValue", objectCreator.mConfiguration.getItemAsString("testKey"));
    }
}
