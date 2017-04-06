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
package com.amazon.android.search;


import com.amazon.android.search.TestResources.TestContent;
import com.amazon.android.search.TestResources.TestableSearchAlgo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static junit.framework.Assert.assertEquals;

/**
 * The TestableSearchAlgoTest class tests the TestableSearchAlgo class.
 * This test class will hold an instance of the TestableSearchAlgo object and call onCompare with
 * different inputs.
 */
public class TestableSearchAlgoTest {

    // Create an instance of the TestableSearchAlgo
    TestableSearchAlgo testTestableSearchAlgo;

    @Before
    public void setUp() throws Exception {

        // Instantiate a new instance of TestableSearchAlgo
        testTestableSearchAlgo = new TestableSearchAlgo();
    }

    @After
    public void tearDown() throws Exception {

    }

    @Test
    public void testOnCompare() throws Exception {

        // Create the test strings
        String foundString = "TestContent";
        String notFoundString = "incorrect";
        String nullString = null;

        // Create the test contents
        TestContent testTestContent = new TestContent("TestContent Title");
        TestContent nullTestTestContent = null;

        // Call onCompare with a string that will not be found.
        assertEquals(testTestableSearchAlgo.onCompare(notFoundString, testTestContent), false);

        // Call onCompare with a string that will be found.
        assertEquals(testTestableSearchAlgo.onCompare(foundString, testTestContent), true);

        // Call onCompare with a null string that should return false
        assertEquals(testTestableSearchAlgo.onCompare(nullString, testTestContent), false);

        // Call onCompare with a null content object that should return false
        assertEquals(testTestableSearchAlgo.onCompare(foundString, nullTestTestContent), false);

    }
}