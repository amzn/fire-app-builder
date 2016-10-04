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

import org.junit.Before;
import org.junit.Test;

import java.security.InvalidParameterException;
import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link PathHelper} class.
 */
public class PathHelperTest {

    private final String TAG = PathHelper.class.getSimpleName();

    // Create test input and expected output for injectParameter tests
    private String testString = "I went $$par0$$ the $$par1$$ yesterday $$par2$$.";
    private String[] params = {"to", "store", "morning"};
    private String[] missingParams = {"to", "store"};

    // Create test maps and paths for testing getValueByPath
    private String pathToItem = "level1/item";
    private HashMap<String, Object> root = new HashMap<>();

    @Before
    public void setup() {

        // Setup data for getValueByPath tests
        HashMap<String, Object> level1 = new HashMap<>();
        level1.put("item", "value");
        root.put("level1", level1);
    }

    /**
     * Tests the injectParameters method of the {@link PathHelper} class with a variety of
     * parameter combinations.
     */
    @Test
    public void testInjectParameters() throws Exception {

        // Test with correct input
        String expected = "I went to the store yesterday morning.";
        assertEquals(expected, PathHelper.injectParameters(testString, params));
    }

    /**
     * Test the case where the string has zero injection points and zero pars are passed into
     * {@link PathHelper#injectParameters(String, String[])}. The expected result is the same as
     * the string passed in.
     */
    @Test
    public void testInjectParametersWithZeroPars() throws Exception {
        // Test with an empty array and zero patterns in string
        assertEquals("Hello", PathHelper.injectParameters("Hello", new String[]{}));
    }

    /**
     * Test the case where extra pars are passed in that the string doesn't need for {@link
     * PathHelper#injectParameters(String, String[])}. The string should be returned with the
     * expected pars injected and the extra pars are ignored.
     */
    @Test
    public void testInjectParametersWithExtraPars() throws Exception {
        // Test with more params than patterns in the string.
        String testStringMissingParam = "I went the $$par1$$ yesterday $$par2$$.";
        String expectedWithMissingParam = "I went the store yesterday morning.";
        assertEquals(expectedWithMissingParam,
                     PathHelper.injectParameters(testStringMissingParam, params));
    }

    /**
     * Test the case where null inputs are passed into {@link PathHelper#injectParameters(String,
     * String[])}. Expected to return null.
     */
    @Test
    public void testInjectParametersWithNull() throws Exception {

        assertNull(PathHelper.injectParameters(null, null));
        assertNull(PathHelper.injectParameters(null, params));
        assertNull(PathHelper.injectParameters(testString, null));
    }

    /**
     * Test the case where the inputs are both empty values for {@link
     * PathHelper#injectParameters(String, String[])}. An empty string is expected to be returned.
     */
    @Test
    public void testInjectParametersWithEmptyInputs() throws Exception {
        // Test with an empty array and empty string
        assertEquals("", PathHelper.injectParameters("", new String[]{}));
    }

    /**
     * Tests the case where more patterns are in the string and parameters passed in pars for
     * {@link PathHelper#injectParameters(String, String[])}
     */
    @Test(expected = PathHelper.MalformedInjectionStringException.class)
    public void testInjectParametersWithMissingPars() throws Exception {

        PathHelper.injectParameters(testString, missingParams);
    }

    /**
     * Tests the {@link PathHelper#injectParameters(String, String[])} method with pars greater
     * than the max value.
     */
    @Test(expected = InvalidParameterException.class)
    public void testInjectParametersWithMoreThanMaxPars() throws Exception {

        String[] pars = {"0", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};
        PathHelper.injectParameters(testString, pars);
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with good input data.
     */
    @Test
    public void testGetValueByPath() throws Exception {

        // Test normal working case
        assertEquals("value", PathHelper.getValueByPath(root, pathToItem));
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with null input data.
     */
    @Test
    public void testGetValueByPathWithNull() throws Exception {
        // Null tests
        assertNull(PathHelper.getValueByPath(null, null));
        assertNull(PathHelper.getValueByPath(root, null));
        assertNull(PathHelper.getValueByPath(null, pathToItem));
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with empty input data.
     */
    @Test
    public void testGetValueByPathWithEmptyInputs() throws Exception {
        // Test an empty map and string
        assertNull(PathHelper.getValueByPath(new HashMap<String, Object>(), ""));
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with an empty path.
     */
    @Test
    public void testGetValueByPathWithEmptyPath() throws Exception {
        // Test an empty string
        assertNull(PathHelper.getValueByPath(root, ""));
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with an empty map.
     */
    @Test
    public void testGetValueByPathWithEmptyMap() throws Exception {
        // Test an empty map
        assertNull(PathHelper.getValueByPath(new HashMap<String, Object>(), pathToItem));
    }

    /**
     * Tests the {@link PathHelper#getValueByPath(Map, String)} method with a bad path.
     */
    @Test
    public void testGetValueByPathWithBadPath() throws Exception {

        assertNull(PathHelper.getValueByPath(root, "level1/badpath"));
    }

    /**
     * Tests the {@link PathHelper#containsParameterMatchingRegex(String)} method for the positive
     * case.
     */
    @Test
    public void testContainsPatternPositiveCase() throws Exception {

        assertTrue(PathHelper.containsParameterMatchingRegex("here's the pattern $$par1$$ "));
    }

    /**
     * Tests the {@link PathHelper#containsParameterMatchingRegex(String)} method for the negative
     * case.
     */
    @Test
    public void testContainsPatternNegativeCase() throws Exception {

        assertFalse(PathHelper.containsParameterMatchingRegex(null));
        assertFalse(PathHelper.containsParameterMatchingRegex("no pattern here"));
    }

}
