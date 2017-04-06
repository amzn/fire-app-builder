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


import com.amazon.utils.test.R;

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

/**
 * Tests the {@link Helpers} class.
 */
@RunWith(AndroidJUnit4.class)

public class HelpersTest {

    private static final String TAG = HelpersTest.class.getSimpleName();

    /**
     * Tests the {@link Helpers#loadStringMappingFromJsonFile(Context, int)} method for the
     * positive case.
     */
    @Test
    public void testLoadStringMappingFromJsonFilePositive() {

        HashMap<String, String> result =
                Helpers.loadStringMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                      R.string.load_string_mapping_test1_file);

        assertEquals("There should be 4 key-value pairs in the map", 7, result.size());

        assertEquals("value", result.get("string"));
        assertEquals("4", result.get("integer"));
        assertEquals("true", result.get("boolean"));
        assertEquals("{}", result.get("emptyObject"));
        assertEquals("[]", result.get("emptyList"));
        assertEquals("{innerKey=innerValue}", result.get("object"));
        assertEquals("[1, 2, 3]", result.get("list"));
    }

    /**
     * Tests the {@link Helpers#loadStringMappingFromJsonFile(Context, int)} method for the case
     * if a non-existent file is provided.
     */
    @Test
    public void testLoadStringMappingFromJsonFileNegative() {

        Map result = Helpers.loadStringMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                           R.string.non_existent_file);
        assertNotNull(result);
        assertEquals("Expecting an empty map if using a non-existent file", 0, result.size());
    }
}