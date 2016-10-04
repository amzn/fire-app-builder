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
package com.amazon.android.search.TestResources;


import com.amazon.android.search.ISearchAlgo;

/**
 * The TestableSearchAlgo class searches the contents of TestContent objects.
 * The TestableSearchAlgo class is the concrete implementation of the {@link ISearchAlgo}
 * interface.
 * The TestableSearchAlgo class has one method "onCompare".
 */
public class TestableSearchAlgo implements ISearchAlgo<TestContent> {

    /**
     * This method compares a single string query with one testContent object. The string is passed
     * into the testContent object along with what fields in the testContent object to search in.
     * If the TestContent Object has text matching the query in the given fields it will return
     * true.
     *
     * @param query       The query string
     * @param testContent a generic data object
     * @return will return true if the data object matches the query string
     */
    @Override
    public boolean onCompare(String query, TestContent testContent) {

        // If the testContent or query is null, return false right away.
        if (testContent == null || query == null) {
            return false;
        }

        // Search in the testContent via the method call searchInFields
        return testContent.searchInFields(query, new String[]{TestContent.TITLE_FIELD_NAME,
                TestContent.DESCRIPTION_FIELD_NAME});
    }
}
