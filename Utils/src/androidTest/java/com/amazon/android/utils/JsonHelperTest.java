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

import org.json.JSONObject;
import org.junit.Test;


import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * Test class for JsonParserHelper.
 */
public class JsonHelperTest {

    /*
     * Test JSON string.
     */
    private final String data1 = "{\"test\":\"bar\"," +
            "\"foo2\":1, " +
            "\"foo3\":[]," +
            "\"foo4\":{}}";

    /*
     * Test JSON string with nested objects.
     */
    private final String data2 = "{ \"foo1\": " +
            "{ \"foo2\" : " +
            "{ \"foo3\": {}" +
            "}" +
            "}" +
            "}";

    /*
     * Test JSON string with nested arrays.
     */
    private final String data3 = "{ \"foo\": [ {\"foo2\":1}," +
            "[\"foo3\", \"foo4\"]," +
            "\"foo5\", 5]," +
            "\"foo6\":[]}";

    /**
     * Tests a malformed JSON-encoded string as input to stringToMap method.
     */
    @Test
    public void testStringToMapBadData() throws Exception {

        boolean exceptionCaught = false;
        try {
            JsonHelper.stringToMap("{");
        }
        catch (JsonHelper.MalformedJSONException e) {
            exceptionCaught = true;
        }
        assertTrue(exceptionCaught);
    }

    /**
     * Tests the null case for the stringToMap method.
     */
    @Test
    public void testStringToMapNull() throws Exception {

        assertEquals(new HashMap<>(), JsonHelper.stringToMap(null));
    }

    /**
     * Tests passing an empty string to the stringToMap method.
     */
    @Test
    public void testStringToMapEmptyString() throws Exception {

        assertEquals(0, JsonHelper.stringToMap("").size());
    }

    /**
     * Tests passing an empty object to the stringToMap method.
     */
    @Test
    public void testStringToMapEmptyObject() throws Exception {

        String data = "{}";

        Map<String, Object> expected = new HashMap<>();

        Map map = JsonHelper.stringToMap(data);

        assertEquals(expected, map);

    }

    /**
     * Tests passing a JSON object with several key/value pairs to
     * the stringToMap method.
     */
    @Test
    public void testStringToMapObject() throws Exception {

        Map result = JsonHelper.stringToMap(data1);

        assertEquals(getData1Map(), result);
    }

    /**
     * Tests passing a JSON object that contains nested objects to the
     * stringToMap method.
     */
    @Test
    public void testStringToMapObjectNested() throws Exception {

        Map result = JsonHelper.stringToMap(data2);
        assertEquals(getData2Map(), result);

    }

    /**
     * Tests passing a JSON object that contains nested arrays to the
     * stringToMap method.
     */
    @Test
    public void testStringToMapObjectArray() throws Exception {

        Map result = JsonHelper.stringToMap(data3);
        assertEquals(getData3Map(), result);
    }

    /**
     * Tests the null case for the mapToString method.
     */
    @Test
    public void testMapToStringNull() throws Exception {

        assertNull(JsonHelper.mapToString(null));
    }

    /**
     * Tests passing an empty map to the mapToString method.
     */
    @Test
    public void testMapToStringEmptyMap() throws Exception {

        assertEquals("{}", JsonHelper.mapToString(new HashMap<String, Object>()));
    }

    /**
     * Tests passing a map that represents a JSON object
     * to the mapToString method.
     */
    @Test
    public void testMapToStringObject() throws Exception {

        String result = JsonHelper.mapToString(getData1Map());
        String expected = (new JSONObject(data1)).toString();

        assertEquals(expected, result);
    }

    /**
     * Tests passing a map that contains nested objects to
     * the mapToString method.
     */
    @Test
    public void testMapToStringObjectNested() throws Exception {

        String result = JsonHelper.mapToString(getData2Map());
        String expected = (new JSONObject(data2)).toString();

        assertEquals(expected, result);
    }

    /**
     * Tests passing a map that contains arrays to the mapToString method.
     */
    @Test
    public void testMapToStringObjectArray() throws Exception {

        String result = JsonHelper.mapToString(getData3Map());
        String expected = (new JSONObject(data3)).toString();

        assertEquals(expected, result);
    }

    /*
     * Helper method to return a map that represents the data1 string.
     */
    private Map<String, Object> getData1Map() {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("test", "bar");
        map.put("foo2", 1);
        map.put("foo3", new ArrayList<>());
        map.put("foo4", new HashMap<>());

        return map;
    }

    /*
     * Helper method to return a map that represents the data2 string.
     */
    private Map<String, Object> getData2Map() {

        LinkedHashMap<String, Object> foo2 = new LinkedHashMap<>();
        LinkedHashMap<String, Object> foo1 = new LinkedHashMap<>();
        foo2.put("foo3", new LinkedHashMap<>());
        foo1.put("foo2", foo2);

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        map.put("foo1", foo1);

        return map;
    }

    /*
     * Helper method to return a map that represents the data3 string.
     */
    private Map<String, Object> getData3Map() {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();
        ArrayList<Object> foo = new ArrayList<>();
        LinkedHashMap<String, Object> foo2 = new LinkedHashMap<>();
        foo2.put("foo2", 1);
        ArrayList<Object> foo3 = new ArrayList<>();
        foo3.add("foo3");
        foo3.add("foo4");

        foo.add(foo2);
        foo.add(foo3);
        foo.add("foo5");
        foo.add(5);
        map.put("foo", foo);
        map.put("foo6", new ArrayList<>());

        return map;
    }
}