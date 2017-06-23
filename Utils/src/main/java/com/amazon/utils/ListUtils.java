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

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;

/**
 * Utilities for Lists.
 */
public class ListUtils {

    private static final String TAG = ListUtils.class.getSimpleName();

    /**
     * Add the item to the list if the item is not already in the list.
     *
     * @param list List.
     * @param item Item to add to the list.
     * @param <T>  Item type.
     * @return True if the item was added, false otherwise.
     */
    public static <T> boolean safeAdd(List<T> list, T item) {

        if (list.contains(item)) {
            return false;
        }
        return list.add(item);
    }

    /**
     * Remove duplicate items from the given list. Return the result as a new list and don't modify
     * the original list.
     *
     * @param list List to remove duplicates from.
     * @param <T>  Item type.
     * @return A new list that contains the items of the original list without any duplicates.
     */
    public static <T> List<T> removeDuplicates(List<T> list) {

        return new ArrayList<>(new LinkedHashSet<>(list));
    }

    /**
     * Turns a string to a list. The items within the string should be separated by commas.
     * Example,
     * the string "one, two, three" will return a list with three items ["one", "two", "three"].
     *
     * @param string The string to turn into a list.
     * @return The list, an empty array if there was an error, or null if the input is null.
     * @throws ExpectingJsonArrayException if the string is not in a JSONArray format.
     */
    public static List<String> stringToList(String string) throws ExpectingJsonArrayException {

        if (string == null) {
            return null;
        }

        List<String> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(string);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        }
        catch (JSONException e) {
            throw new ExpectingJsonArrayException(string);
        }
        return list;
    }

    /**
     * Get a map value from a map using a key and return it as a list.
     *
     * @param map The map to get the map value from.
     * @param key The key to get the map value.
     * @return A list of maps.
     */
    public static List<Map> getValueAsMapList(Map<String, Map> map, String key) {

        if (map == null || StringManipulation.isNullOrEmpty(key) || map.get(key) == null) {
            return new ArrayList<>();
        }
        if (map.get(key) instanceof List) {
            return (List<Map>) map.get(key);
        }
        List<Map> list = new ArrayList<>();
        list.add(map.get(key));
        return list;
    }

    /**
     * An error message for when a content's member variable is expected to be a string
     * representation of a JSON array but is something else.
     */
    public static class ExpectingJsonArrayException extends Exception {

        /**
         * Constructor.
         *
         * @param wrongValue The value used instead of a string representation of a JSON array.
         */
        public ExpectingJsonArrayException(String wrongValue) {

            super("Expecting a string representation of a JSON array but got: " + wrongValue);
        }
    }
}
