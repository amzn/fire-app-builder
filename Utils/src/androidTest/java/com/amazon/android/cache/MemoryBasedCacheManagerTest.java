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
package com.amazon.android.cache;

import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import java.util.HashSet;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Tests for {@link MemoryBasedCacheManager}
 */
public class MemoryBasedCacheManagerTest {

    private MemoryBasedCacheManager<String, Data> mMemoryBasedCacheManager;
    private String testKey = "testKey";
    private Data testValue;

    @Before
    public void setUp() {

        mMemoryBasedCacheManager = new MemoryBasedCacheManager<String, Data>();
        testValue = Data.createDataForPayload("testValue");
    }

    /**
     * Tests constructor with params and {@link MemoryBasedCacheManager#size()}
     */
    @Test
    public void testConstructorWithSizeParamAndMaxSize() {

        mMemoryBasedCacheManager = new MemoryBasedCacheManager<>(100);
        assertEquals(100, mMemoryBasedCacheManager.mLruCache.maxSize());
    }

    /**
     * Tests {@link MemoryBasedCacheManager#put(Object, Object)} and {@link
     * MemoryBasedCacheManager#get(Object)} and {@link MemoryBasedCacheManager#containsKey(Object)}
     * and {@link MemoryBasedCacheManager#containsValue(Object)} and {@link
     * MemoryBasedCacheManager#removeKey(Object)}
     */
    @Test
    public void testPutAndGetAndContainsKeyAndContainsValue() {

        mMemoryBasedCacheManager.put(testKey, testValue);
        assertEquals(testValue, mMemoryBasedCacheManager.get(testKey));
        assertTrue(mMemoryBasedCacheManager.containsKey(testKey));
        assertFalse(mMemoryBasedCacheManager.containsKey("randomKey"));
        Set set = new HashSet<>();
        set.add(testKey);
        assertEquals(set, mMemoryBasedCacheManager.containsValue(testValue));
        mMemoryBasedCacheManager.removeKey(testKey);
        assertFalse(mMemoryBasedCacheManager.containsKey(testKey));
        assertNull(mMemoryBasedCacheManager.get(testKey));
    }

    /**
     * Test {@link MemoryBasedCacheManager#put(Object, Object, long)}
     */
    @Test(expected = UnsupportedOperationException.class)
    public void testPutWithExpiration() {

        mMemoryBasedCacheManager.put(testKey, testValue, 1);
    }

    /**
     * Test for {@link MemoryBasedCacheManager#size()} and {@link MemoryBasedCacheManager#resize
     * (int)}
     */
    @Test
    public void testSizeAndResize() {
        // Assert current size is 0
        assertEquals(0, mMemoryBasedCacheManager.size());
        // Add a key
        mMemoryBasedCacheManager.put("testKey1", testValue);
        // Assert current size is 1
        assertEquals(1, mMemoryBasedCacheManager.size());
        // Resize the cache and validate the size is updated
        mMemoryBasedCacheManager.resize(200);
        assertEquals(200, mMemoryBasedCacheManager.maxSize());
    }

    /**
     * Tests for {@link MemoryBasedCacheManager#clear()}
     */
    @Test
    public void testClear() {
        // Add random data in memory and validate the size of cache
        mMemoryBasedCacheManager.put("testKey1", testValue);
        mMemoryBasedCacheManager.put("testKey2", testValue);
        mMemoryBasedCacheManager.put("testKey3", testValue);
        mMemoryBasedCacheManager.put("testKey4", testValue);
        assertEquals(4, mMemoryBasedCacheManager.size());

        // Clear the cache and validate that the keys are gone and size is 0
        mMemoryBasedCacheManager.clear();
        assertEquals(0, mMemoryBasedCacheManager.size());
        assertNull(mMemoryBasedCacheManager.get("testKey1"));
        assertNull(mMemoryBasedCacheManager.get("testKey2"));
        assertNull(mMemoryBasedCacheManager.get("testKey3"));
        assertNull(mMemoryBasedCacheManager.get("testKey4"));
    }
}
