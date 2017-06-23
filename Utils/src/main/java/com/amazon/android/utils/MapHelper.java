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

import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

/**
 * A collection of static utility methods to create maps from different sources like Json file.
 */
public class MapHelper {

    private static final String TAG = MapHelper.class.getSimpleName();
    public static final boolean DEBUG = false;

    /**
     * Making sure public utility methods remain static.
     */
    private MapHelper() {

    }

    /**
     * Load a map of strings for the given JSON file. The file should be formatted as a flat
     * object with string key, value pairs, e.g.:
     *
     * {
     * "Key1", "Value1",
     * "Key2", "Value2"
     * }
     *
     * @param context    Context.
     * @param fileNameId File name ID of the file to read from.
     * @return The JSON file parsed as a map of strings. If there was an error while reading the
     * file such as the file not existing, an empty map is returned and the error is logged.
     */
    public static HashMap<String, String> loadStringMappingFromJsonFile(Context context,
                                                                        int fileNameId) {

        HashMap<String, String> result = new HashMap<>();
        String fileName = context.getString(fileNameId);
        try {
            if (FileHelper.doesFileExist(context, fileName)) {
                String fileData = FileHelper.readFile(context, fileName);
                Map map = JsonHelper.stringToMap(fileData);

                for (Object key : map.keySet()) {
                    result.put((String) key, String.valueOf(map.get(key)));
                }
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Unable to read file " + fileName, e);
        }

        return result;
    }

    /**
     * Load a map of Arrays for the given JSON file. The file should be formatted as a flat
     * object with string key, array values, e.g.:
     *
     * {
     * "Key1", ["Value1", "Value2", ..]
     * "Key2", ["Value3", "Value4", ..]
     * }
     *
     * @param context    Context.
     * @param fileNameId File name ID of the file to read from.
     * @return The JSON file parsed as a map of Hash set. If there was an error while reading the
     * file such as the file not existing, an empty map is returned and the error is logged.
     */
    public static <T> HashMap<String, HashSet<T>> loadArrayMappingFromJsonFile(Context context,
                                                                                int fileNameId) {

        HashMap<String, HashSet<T>> result = new HashMap<>();
        String fileName = context.getString(fileNameId);
        try {
            if (FileHelper.doesFileExist(context, fileName)) {
                String fileData = FileHelper.readFile(context, fileName);
                Map map = JsonHelper.stringToMap(fileData);

                for (Object key : map.keySet()) {
                    Object arrayObject = map.get(key);
                    if (arrayObject instanceof List) {
                        result.put((String) key, new HashSet<T>((List) arrayObject));
                    }
                }
            }
        }
        catch (Exception e) {
            Log.w(TAG, "Unable to read file " + fileName, e);
        }

        return result;
    }
}
