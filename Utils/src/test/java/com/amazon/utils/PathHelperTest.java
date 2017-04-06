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
package com.amazon.utils;

import com.amazon.android.utils.PathHelper;

import junit.framework.TestCase;

import org.junit.Test;

import java.util.HashMap;

/**
 * PathHelperTest class is a test for PathHelper class.
 * Code coverage is used to make sure test touches all lines and methods.
 */
public class PathHelperTest extends TestCase {

    /**
     * Test get key from path method.
     */
    @Test
    public void testGetKeyFromPath() throws Exception {
        String keyOnly = "key";
        String keyWithPath = "path/key";

        // Key only test case.
        assertEquals(keyOnly, PathHelper.getKeyFromPath(keyOnly));

        // Key with path test case.
        assertEquals(keyOnly, PathHelper.getKeyFromPath(keyWithPath));
    }

    /**
     * Test set has a path method.
     */
    @Test
    public void testHasAPath() throws Exception {
        String noPath = "testNoPath";
        String path = "hasPath/key";

        // Negative test case.
        assertFalse(PathHelper.hasAPath(noPath));

        // Positive test case.
        assertTrue(PathHelper.hasAPath(path));
    }

    /**
     * Test get map by path method.
     */
    @Test
    public void testGetMapByPath() throws Exception {

        // Create a mock hash map and populate items.
        HashMap<String, Object> root = new HashMap<>();
        HashMap<String, String> path1 = new HashMap<>();
        root.put("path1", path1);
        path1.put("key", "test");
        root.put("path1", path1);

        // Negative test case, it should return root map if not found.
        assertNotNull(PathHelper.getMapByPath(root, "random/path/key"));

        // Positive test case.
        assertNotNull(PathHelper.getMapByPath(root, "path1"));

        // Positive test case to make sure we got the right map.
        assertEquals("test", PathHelper.getMapByPath(root, "path1").get("key"));
    }
}