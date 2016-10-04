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

/**
 * This interface holds the contract that defines the onCompare method used to search objects.
 * The ISearchAlgo interface uses java generics to handle an type of input object.
 */
public interface ISearchAlgo<T> {

    /**
     * This method will compare a single string query with one data object.
     *
     * @param query The query string.
     * @param t     A generic data object.
     * @return True if the data object matches the query string; false otherwise.
     */
    boolean onCompare(String query, T t);

}
