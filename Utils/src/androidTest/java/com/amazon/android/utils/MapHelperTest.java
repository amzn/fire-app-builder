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

import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.assertNotNull;

/**
 * Tests the {@link MapHelper} class.
 */
@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)

public class MapHelperTest {

    private static final String TAG = MapHelperTest.class.getSimpleName();

    /**
     * Tests the {@link MapHelper#loadStringMappingFromJsonFile(Context, int)} method for the
     * positive case.
     */
    @Test
    public void testLoadStringMappingFromJsonFilePositive() {

        HashMap<String, String> result =
                MapHelper.loadStringMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                        com.amazon.utils.test.R.string
                                                                .load_string_mapping_test1_file);

        assertEquals("There should be 7 key-value pairs in the map", 7, result.size());

        assertEquals("value", result.get("string"));
        assertEquals("4", result.get("integer"));
        assertEquals("true", result.get("boolean"));
        assertEquals("{}", result.get("emptyObject"));
        assertEquals("[]", result.get("emptyList"));
        assertEquals("{innerKey=innerValue}", result.get("object"));
        assertEquals("[1, 2, 3]", result.get("list"));
    }

    /**
     * Tests the {@link MapHelper#loadStringMappingFromJsonFile(Context, int)} method for the case
     * if a non-existent file is provided.
     */
    @Test
    public void testLoadStringMappingFromJsonFileNegative() {

        Map result = MapHelper.loadStringMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                             com.amazon.utils.test.R.string
                                                                     .non_existent_file);
        assertNotNull(result);
        assertEquals("Expecting an empty map if using a non-existent file", 0, result.size());
    }

    /**
     * create requested HashMap
     *
     * @param key   of map
     * @param value of map
     * @return return map with given key and value
     */
    Map<String, String> getHashMap(String key, String value) {

        Map<String, String> map = new HashMap<>();
        map.put(key, value);
        return map;
    }

    /**
     * Inner class to provide generic implementation of HashSet
     */
    private class TestHashSet<T> {

        private HashSet<T> testSet = new HashSet<>();

        public void add(T... values) {

            testSet.addAll(Arrays.asList(values));
        }

        public HashSet<T> get() {

            return testSet;
        }
    }

    /**
     * create requested test HashSet
     *
     * @param type Type of object
     * @return return HashSet for test
     */
    private TestHashSet getTestHashSetObject(String type) {

        switch (type) {
            case "strings":
                TestHashSet<String> t1 = new TestHashSet<>();
                t1.add("value1", "value2");
                return t1;
            case "integers":
                TestHashSet<Integer> t2 = new TestHashSet<>();
                t2.add(4, 7, 13);
                return t2;
            case "booleans":
                TestHashSet<Boolean> t3 = new TestHashSet<>();
                t3.add(true, false);
                return t3;
            case "emptyObjects":
                TestHashSet<Map> t4 = new TestHashSet<>();
                t4.add(new HashMap());
                return t4;
            case "emptyLists":
                TestHashSet<List> t5 = new TestHashSet<>();
                t5.add(new ArrayList());
                return t5;
            case "objects":
                TestHashSet<Map<String, String>> t6 = new TestHashSet<>();
                t6.add(getHashMap("innerKey1", "innerValue1"),
                       getHashMap("innerKey2", "innerValue2"),
                       getHashMap("innerKey3", "innerValue3"));
                return t6;
            case "lists":
                TestHashSet<List<Integer>> t7 = new TestHashSet<>();
                List l1 = Arrays.asList(1, 2, 3);
                List l2 = Arrays.asList(4, 5, 6);
                List l3 = Arrays.asList(13, 16, 20);
                t7.add(l1, l2, l3);
                return t7;
        }
        return new TestHashSet<>();
    }

    /**
     * Tests the {@link MapHelper#loadArrayMappingFromJsonFile(Context, int)} method for the
     * positive case.
     */
    @Test
    public void testLoadArrayMappingFromJsonFilePositive() {

        HashMap<String, HashSet<Object>> result =
                MapHelper.loadArrayMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                       com.amazon.utils.test.R.string
                                                               .load_array_mapping_test1_file);

        assertEquals("There should be 7 key-value pairs in the map", 7, result.size());

        assertTrue(getTestHashSetObject("strings").get().equals(result.get("strings")));
        assertTrue(getTestHashSetObject("integers").get().equals(result.get("integers")));
        assertTrue(getTestHashSetObject("booleans").get().equals(result.get("booleans")));
        assertTrue(getTestHashSetObject("emptyObjects").get().equals(result.get("emptyObjects")));
        assertTrue(getTestHashSetObject("emptyLists").get().equals(result.get("emptyLists")));
        assertTrue(getTestHashSetObject("objects").get().equals(result.get("objects")));
        assertTrue(getTestHashSetObject("lists").get().equals(result.get("lists")));
    }

    /**
     * Tests the {@link MapHelper#loadArrayMappingFromJsonFile(Context, int)} method for the case
     * if a non-existent file is provided.
     */
    @Test
    public void testLoadArrayMappingFromJsonFileNegative() {

        Map result = MapHelper.loadArrayMappingFromJsonFile(InstrumentationRegistry.getContext(),
                                                            com.amazon.utils.test.R.string
                                                                    .non_existent_file);
        assertNotNull(result);
        assertEquals("Expecting an empty map if using a non-existent file", 0, result.size());
    }
}