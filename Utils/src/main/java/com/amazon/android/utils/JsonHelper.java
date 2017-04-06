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

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.util.Log;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This is a helper class that parses a JSON-encoded string into a Map. This class uses
 * JSONObject and JSONArray from org.json to handle the JSON manipulation.
 */
public class JsonHelper {

    private static final String TAG = JsonHelper.class.getSimpleName();

    /**
     * Comment start sequence.
     */
    private static final String COMMENT_START_SEQUENCE_ESCAPED = "\\/\\*";

    /**
     * Comment stop sequence.
     */
    private static final String COMMENT_STOP_SEQUENCE_ESCAPED = "\\*\\/";

    /**
     * Turns a Map object into a JSON-encoded string. Uses the JSONObject from org.json to create
     * the string.
     *
     * @param map The Map to convert.
     * @return A JSON-encoded String that represents the map parameter.
     */
    public static String mapToString(Map<String, Object> map) {

        if (map == null) {
            return null;
        }

        JSONObject object = new JSONObject(map);
        return object.toString();
    }

    /**
     * Parses a JSON-encoded string into a Map. If the data string is malformed
     * in any way, an exception is thrown.
     *
     * @param data The JSON-encoded string.
     * @return A Map containing the parsed JSON pairs of the string. If the data string is null or
     * empty, this method returns an empty map.
     * @throws Exception if the JSON-encoded string is malformed.
     */
    public static Map stringToMap(String data) throws Exception {

        JSONObject jsonObject;

        // Return an empty map for null or empty data string.
        if (data == null || data.isEmpty()) {
            return parseObject(null);
        }

        try {
            // Create a JSONObject from the data string.
            jsonObject = new JSONObject(data);

        }
        catch (JSONException e) {

            Log.e(TAG, "Error creating JSON Object from string.", e);
            throw new MalformedJSONException("Malformed JSON-encoded string", e);
        }
        // Parse the object and return its map representation.
        return parseObject(jsonObject);
    }

    /**
     * Helper method to parse a JSONObject.
     *
     * @param object The JSONObject to parse.
     * @return A Map containing the key/value pairs of the JSONObject. If the object passed in is
     * null, an empty map is returned.
     * @throws Exception if the JSON-encoded object is malformed.
     */
    private static Map parseObject(JSONObject object) throws Exception {

        LinkedHashMap<String, Object> map = new LinkedHashMap<>();

        if (object != null) {

            Iterator<String> keys = object.keys();

            // Loop through all keys
            while (keys.hasNext()) {

                String key = keys.next();

                try {
                    // Parse the value associated with the key and add it to the map.
                    map.put(key, parseValue(object.get(key)));

                }
                catch (JSONException e) {

                    Log.e(TAG, "Invalid key-value mapping for key: " + key, e);
                    throw new MalformedJSONException("Malformed JSON. Contains a bad key: " +
                                                             key, e);
                }
            }
        }
        return map;
    }

    /**
     * Helper method to parse a JSONArray.
     *
     * @param array The JSONArray to parse.
     * @return A List of the Objects from the JSONArray. If the array passed in is null, an empty
     * ArrayList is returned.
     * @throws Exception if the JSON-encoded array is malformed.
     */
    private static List parseArray(JSONArray array) throws Exception {

        ArrayList<Object> list = new ArrayList<>();

        if (array != null) {

            // Parse each value in the array and add it to the list.
            for (int i = 0; i < array.length(); i++) {

                try {

                    list.add(parseValue(array.get(i)));

                }
                catch (JSONException e) {
                    Log.e(TAG, "Array contains no value at index: " + i, e);
                    throw new MalformedJSONException("Malformed JSON. Array contains no value at " +
                                                             "index: " + i, e);
                }

            }
        }
        return list;
    }

    /**
     * Helper method to parse a value.
     *
     * @param value The value to parse.
     * @return An Object that represents the parsed object.
     * @throws Exception if the JSON-encoded value is malformed.
     */
    private static Object parseValue(Object value) throws Exception {

        // If value is a JSONObject, parse the JSONObject
        if (value instanceof JSONObject) {
            return parseObject((JSONObject) value);
        }
        // If value is a JSONArray, parse the JSONArray
        else if (value instanceof JSONArray) {
            return parseArray((JSONArray) value);
        }
        // Otherwise the value is an Object, String, int, etc, so just return its value.
        return value;
    }

    /**
     * This method removes comments from JSON string.
     *
     * @param jsonString JSON string with comments.
     * @return JSON string without comments.
     */
    public static String escapeComments(String jsonString) {

        if (jsonString == null) {
            return null;
        }

        String regex = COMMENT_START_SEQUENCE_ESCAPED +
                "(.*?|\n)" + // Match all type of cases including new line.
                COMMENT_STOP_SEQUENCE_ESCAPED;

        StringBuffer stringBuffer = new StringBuffer(jsonString.length());
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(jsonString);
        while (matcher.find()) {
            matcher.appendReplacement(stringBuffer, "");
        }
        matcher.appendTail(stringBuffer);

        return stringBuffer.toString();
    }

    /**
     * An exception class to for malformed strings.
     */
    public static class MalformedJSONException extends Exception {

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         * @param e       The throwable exception that took place.
         */
        public MalformedJSONException(String message, Throwable e) {

            super(message, e);
        }
    }
}