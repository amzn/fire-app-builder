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

import java.util.ArrayList;
import java.util.Map;
import java.util.concurrent.CountDownLatch;

import static org.junit.Assert.*;

/**
 * This class will test the Search Manager.
 */
public class SearchManagerTest {

    public SearchManager<ArrayList<TestContent>, TestContent> mSearchManager;

    public TestContent mTestContent;

    public TestableSearchAlgo mTestSearchAlgo;

    public ArrayList<TestContent> mTestContainer;

    public String mSearchAlgoName;

    @Before
    public void setUp() throws Exception {

        mSearchManager = new SearchManager();

        mTestContent = new TestContent("Content Title");

        mTestSearchAlgo = new TestableSearchAlgo();

        mTestContainer = new ArrayList<>();

        mSearchAlgoName = "Basic Search";

    }

    @After
    public void tearDown() throws Exception {

        // Null out the created objects.
        mSearchManager = null;
        mTestContainer = null;
        mTestSearchAlgo = null;
        mTestContent = null;
        mSearchAlgoName = null;

    }

    /**
     * This method tests the {@link SearchManager#setSearchAlgoByName(String)}
     */
    @Test
    public void testSetSearchAlgoByName() throws Exception {

        // Create two search algos
        TestableSearchAlgo testSearchAlgoAlpha = new TestableSearchAlgo();
        TestableSearchAlgo testSearchAlgoBeta = new TestableSearchAlgo();

        // Create two different keys
        String alphaString = "Alpha Search";
        String betaString = "Beta Search";
        String falseString = "False String";
        String nullstring = null;

        // Add both to the search manager
        mSearchManager.addSearchAlgo(alphaString, testSearchAlgoAlpha);

        mSearchManager.addSearchAlgo(betaString, testSearchAlgoBeta);

        // Set the search algo, should return true
        assertTrue("Since we already added an algo this should return true",
                   mSearchManager.setSearchAlgoByName(alphaString));

        // Confirm that the get search algo is coming back correctly.
        assertTrue("Test to make sure the Alpha String is set for the search algo",
                   mSearchManager.getSearchAlgoMap().get(alphaString).equals(testSearchAlgoAlpha));

        // Set the other search algo
        assertTrue("Since we already added an algo this should return true",
                   mSearchManager.setSearchAlgoByName(betaString));

        // Confirm that the other search algo is coming back correctly.
        assertTrue("Test to make sure the Beta String is set for the search algo",
                   mSearchManager.getSearchAlgoMap().get(betaString).equals(testSearchAlgoBeta));

        assertFalse("Try to set the algo to one that is not in the algo hashmap",
                    mSearchManager.setSearchAlgoByName(falseString));

        assertFalse("Try to set the algo from a null string",
                    mSearchManager.setSearchAlgoByName(nullstring));
    }

    /**
     * This method tests the {@link SearchManager#addSearchAlgo(String, ISearchAlgo)}
     */
    @Test
    public void testAddSearchAlgo() throws Exception {

        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        Map testMap = mSearchManager.getSearchAlgoMap();

        assertTrue("Search Manager Contains the algo keyed 'Regular search'",
                   testMap.containsKey(mSearchAlgoName));

    }

    /**
     * This method tests the async search
     * {@link SearchManager#asyncSearch(String, String, ISearchResult, Iterable)}.
     * This method uses a {@link java.util.concurrent.CountDownLatch} to handle the callback.
     */
    @Test
    public void testASyncSearch() throws Exception {

        // Use a latch to allow testing of the callback
        final CountDownLatch signal = new CountDownLatch(2);

        // Create the call back
        ISearchResult testASyncSearchCallback = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {
                // If the signal is larger than one that means O has data and done is false.
                if (signal.getCount() > 1) {
                    assertTrue("We should get back the test content",
                               o.equals(mTestContent));

                    assertFalse("We should get false for done",
                                done);
                }

                // If the signal is equal to one that means O is null and done is true.
                if (signal.getCount() == 1) {
                    assertTrue("We should get back a null for o",
                               o == null);
                    assertTrue("We should get back true for done",
                               done);
                }

                signal.countDown();// notify the count down latch
            }
        };

        // Add the algo to the hash map
        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        // Set the algo
        mSearchManager.setSearchAlgoByName(mSearchAlgoName);

        // Create the search query
        String searchQuery = "Content Title";

        // Add the test content to the container.
        mTestContainer.add(mTestContent);


        // Make the call
        mSearchManager.asyncSearch(
                mSearchAlgoName,
                searchQuery,
                testASyncSearchCallback,
                mTestContainer);

        // Wait for callback
        signal.await();
    }

    /**
     * This method tests the async search when two are called at the same time.
     * {@link SearchManager#asyncSearch(String, String, ISearchResult, Iterable)}.
     */
    @Test
    public void testMultipleASyncSearches() throws Exception {

        // Create the call back, this is an empty callback since we only care about if an error
        // being thrown
        ISearchResult testASyncSearchCallback = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {

            }
        };

        // Add the algo to the hash map
        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        // Set the algo
        mSearchManager.setSearchAlgoByName(mSearchAlgoName);

        // Create the search query
        String searchQuery = "Content Title";

        // Add the test content to the container. Note: The largest known catalog is 50,000 items.
        for (int i = 0; i < 100000; i++) {
            mTestContainer.add(mTestContent);
        }

        // Make the first call to async search
        mSearchManager.asyncSearch(
                mSearchAlgoName,
                searchQuery,
                testASyncSearchCallback,
                mTestContainer);

        // Make a second call to async search
        mSearchManager.asyncSearch(
                mSearchAlgoName,
                searchQuery,
                testASyncSearchCallback,
                mTestContainer);
    }

    /**
     * This method tests the async search when two are called at the same time.
     * {@link SearchManager#asyncSearch(String, String, ISearchResult, Iterable)}.
     */
    @Test
    public void testCancelASyncSearch() throws Exception {

        // Use a latch to allow testing of the callback
        final CountDownLatch signal = new CountDownLatch(3);

        // Create the call back, this is an empty callback since we only care about if an error
        // being thrown
        ISearchResult testASyncSearchCallback = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {

                assertTrue("There should be no object returned.", o == null);
                assertTrue("There should be a done signal sent.", done);

                signal.countDown();// notify the count down latch
            }
        };

        // Create a second callback that is called twice.
        ISearchResult testASyncSearchCallback2 = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {

                signal.countDown();// notify the count down latch
            }
        };

        // Add the algo to the hash map
        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        // Set the algo
        mSearchManager.setSearchAlgoByName(mSearchAlgoName);

        // Create the search query
        String searchQuery = "Last Item";

        // Add the test content to the container. Note: The largest known catalog is 50,000 items.
        for (int i = 0; i < 100000; i++) {
            mTestContainer.add(mTestContent);
        }

        // Create a new content item at the end of the list
        TestContent lastElement = new TestContent("Last Item");
        mTestContainer.add(lastElement);

        // Make the first call to async search
        mSearchManager.asyncSearch(
                mSearchAlgoName,
                searchQuery,
                testASyncSearchCallback,
                mTestContainer);

        // Make the second call to async search
        mSearchManager.asyncSearch(
                mSearchAlgoName,
                searchQuery,
                testASyncSearchCallback2,
                mTestContainer);

        // Wait for callback
        signal.await();
    }

    /**
     * This method tests {@link SearchManager#syncSearch(String, String, ISearchResult, Iterable)}
     * This method uses a {@link java.util.concurrent.CountDownLatch} to handle the callback.
     */
    @Test
    public void testSyncSearch() throws Exception {
        // Use a latch to allow testing of the callback
        final CountDownLatch signal = new CountDownLatch(2);

        // Create the call back
        ISearchResult testSyncSearchCallback = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {
                // If the signal is larger than one that means O has data and done is false.
                if (signal.getCount() > 1) {
                    assertTrue("We should get back the test content",
                               o.equals(mTestContent));

                    assertFalse("We should get false for done",
                                done);
                }

                // If the signal is equal to one that means O is null and done is true.
                if (signal.getCount() == 1) {
                    assertTrue("We should get back a null for o",
                               o == null);
                    assertTrue("We should get back true for done",
                               done);
                }

                signal.countDown(); // Notify the count down latch
            }
        };

        // Add the algo to the hash map
        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        // Set the algo
        mSearchManager.setSearchAlgoByName(mSearchAlgoName);

        // Create the search query
        String searchQuery = "Content Title";

        // Add the test content to the container.
        mTestContainer.add(mTestContent);


        // Make the call
        mSearchManager.syncSearch(
                mSearchAlgoName,
                searchQuery,
                testSyncSearchCallback,
                mTestContainer);

        // Wait for callback
        signal.await();

    }

    /**
     * This method passes null objects into the async search to test checkSearchInputs
     */
    @Test
    public void testCheckSearchInputs() throws Exception {

        // Setup Test Variables
        String nullSearchAlgoName = null;
        String nullSearchQuery = null;
        ISearchResult nullISearchResult = null;
        ArrayList<TestContent> nullTestContent = null;


        // Add the algo to the hash map
        mSearchManager.addSearchAlgo(mSearchAlgoName, mTestSearchAlgo);

        // Set the algo
        mSearchManager.setSearchAlgoByName(mSearchAlgoName);

        // Create the search query
        String searchQuery = "Content Title";

        // Add the test content to the container.
        mTestContainer.add(mTestContent);

        // Create the call back, this is an empty callback since we only care about an error being
        // thrown
        ISearchResult testASyncSearchCallback = new ISearchResult() {
            @Override
            public void onSearchResult(Object o, boolean done) {

            }
        };

        // Test a null SearchAlgoName
        try {
            mSearchManager.syncSearch(
                    nullSearchAlgoName,
                    searchQuery,
                    testASyncSearchCallback,
                    mTestContainer);

        }
        catch (Throwable ex) {
            assertTrue(ex instanceof NullPointerException);
        }

        // Test a null searchQuery
        try {
            mSearchManager.syncSearch(
                    mSearchAlgoName,
                    nullSearchQuery,
                    testASyncSearchCallback,
                    mTestContainer);

        }
        catch (Throwable ex) {
            assertTrue(ex instanceof NullPointerException);
        }

        // Test a null ASyncSearchCallback
        try {
            mSearchManager.syncSearch(
                    mSearchAlgoName,
                    searchQuery,
                    nullISearchResult,
                    mTestContainer);

        }
        catch (Throwable ex) {
            assertTrue(ex instanceof NullPointerException);
        }

        // Test a null SearchAlgoName
        try {
            mSearchManager.syncSearch(
                    mSearchAlgoName,
                    searchQuery,
                    testASyncSearchCallback,
                    nullTestContent);

        }
        catch (Throwable ex) {
            assertTrue(ex instanceof NullPointerException);
        }

    }


}