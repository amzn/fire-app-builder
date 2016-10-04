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

import java.util.Collections;
import java.util.Map;

import static org.junit.Assert.assertEquals;

/**
 * This class tests the {@link AUrlGenerator} class.
 */
public class AUrlGeneratorTest {

    private Context context = InstrumentationRegistry.getTargetContext();
    private AUrlGenerator mAUrlGenerator;

    @Before
    public void setUp() throws AObjectCreator.ObjectCreatorException {

        mAUrlGenerator = new MockUrlGenerator(context);
    }

    /**
     * Test the {@link AUrlGenerator#getKey(Map, String)} method when key is not present in params.
     */
    @Test
    public void testGetKeyWithKeyNotInParams() {

        String actualValue = mAUrlGenerator.getKey(Collections.emptyMap(), "testKey");
        assertEquals("testValue", actualValue);
    }

    /**
     * Test the {@link AUrlGenerator#getKey(Map, String)} method when key is present in params.
     */
    @Test
    public void testGetKeyWithKeyInParams() {

        String actualValue = mAUrlGenerator.getKey(Collections.singletonMap("testKey",
                                                                            "testValue1"),
                                                   "testKey");
        assertEquals("testValue1", actualValue);
    }

    /**
     * Test the {@link AUrlGenerator#getKey(Map, String)} method when key is not present in params
     * or config.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testGetKeyWithAbsentKey() {

        mAUrlGenerator.getKey(Collections.emptyMap(), "absentKey");
    }
}
