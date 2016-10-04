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
package com.amazon.dynamicparser;

import com.amazon.dynamicparser.impl.JsonParser;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertTrue;

/**
 * This class tests the JsonParser class; an implementation of the IParser interface. The
 * following conditions are tested: null data string, empty data string, null query string, empty
 * query string, good data string, malformed data string, and malformed query string.
 */
@SuppressWarnings("unchecked")
public class JsonParserTest {

    // Create an instance of JsonParser.
    private JsonParser parser;

    private String json1;
    private String json2;
    private String badJson;

    @Before
    public void setUp() throws Exception {
        // Instantiate a new instance of the JSON parser.
        parser = new JsonParser();

        json1 = "{ 'name':'Bob', 'stuff': [{'foo':'bar'}] }";
        json2 = "{ 'name':'Sam', 'city':'Sunnyvale', 'numbers':[1,2,3]}";
        badJson = "{ 'broken', }";
    }

    @After
    public void tearDown() throws Exception {

        parser = null;
    }

    /**
     * Test the good data case for {@link JsonParser#parse(String)}
     */
    @Test
    public void testParse() throws Exception {

        // Test good JSON string
        Map<String, Object> result = (Map<String, Object>) parser.parse(json1);
        assertTrue("The result should be a Map with 2 items", result.size() == 2);
        List stuff = (List) result.get("stuff");
        assertTrue("The result Map should contain a List with 1 Object", stuff.size() == 1);

        // Test another good JSON string
        result = (Map<String, Object>) parser.parse(json2);
        assertTrue("Map should contain 3 Objects", result.size() == 3);

    }

    /**
     * Test the null case for {@link JsonParser#parse(String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithNull() throws Exception {

        parser.parse(null);
    }

    /**
     * Test the empty input case for {@link JsonParser#parse(String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseWithEmptyInput() throws Exception {

        parser.parse("");
    }

    /**
     * Test the bad input case for {@link JsonParser#parse(String)}
     */
    @Test(expected = IParser.InvalidDataException.class)
    public void testParseWithBadData() throws Exception {

        parser.parse(badJson);
    }

    /**
     * This method tests the {@link JsonParser#parseWithQuery(String, String)} method.
     */
    @Test
    public void testParseWithQuery() throws Exception {

        // Test good JSON string, good query
        List<Map<String, Object>> result = (List<Map<String, Object>>) parser.parseWithQuery
                (json1, "$.stuff");
        assertTrue("List should contain 1 Map", result.size() == 1);
        assertTrue("Map should contain 1 object", result.get(0).size() == 1);

        // Test another good JSON string
        result = (List<Map<String, Object>>) parser.parseWithQuery(json2, "$.numbers");
        assertTrue("List should contain 1 objects", result.size() == 3);
    }

    /**
     * Test the null case for both inputs for {@link JsonParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithNullInputs() throws Exception {

        parser.parseWithQuery(null, null);
    }

    /**
     * Test the null case for the query input for {@link JsonParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithNullQuery() throws Exception {

        parser.parseWithQuery(json1, null);
    }

    /**
     * Test the empty query case for {@link JsonParser#parseWithQuery(String, String)}
     */
    @Test(expected = IllegalArgumentException.class)
    public void testParseQueryWithEmptyInput() throws Exception {

        parser.parseWithQuery(json1, "");
    }

    /**
     * Test the bad query case for {@link JsonParser#parseWithQuery(String, String)}
     */
    @Test(expected = IParser.InvalidQueryException.class)
    public void testParseQueryWithBadQuery() throws Exception {

        parser.parseWithQuery(json1, "/ds");
    }
}
