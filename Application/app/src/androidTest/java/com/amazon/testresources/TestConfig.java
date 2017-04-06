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
package com.amazon.testresources;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

/**
 * This class is used to determine test parameters given the feed type.
 */
public class TestConfig {

    // error tolerance
    public final static double LIMIT_MULTIPLIER = 1.1;

    // accepted feed sizes
    private final static int SMALL = 10;
    private final static int MEDIUM = 100;
    private final static int LARGE = 500;
    private final static int EXTRA_LARGE = 5000;

    // custom timeout for Robotium
    public final static int LONG_TIMEOUT = 500000;

    /**
     * Determine maximum loading time given number of total items.
     * Uses hard-coded values as default.
     *
     * @param itemCount Feed size.
     * @return The maximum time (in ms) allowed for splash screen.
     */
    public static int loadTimeLimit(int itemCount) {

        return loadTimeLimit(itemCount, false);
    }

    /**
     * Determine maximum loading time given .
     * Values obtained from build as of 11/14/2016 used as baseline.
     *
     * Sample feed 1        "size 10"       31 items        ~3 s
     * Sample feed 2        "size 100"      393 items       ~4.5s
     * Sample feed 3        "size 500"      1,516 items     ~11 s
     * Sample feed 4        "size 5,000"    11,798 items    ~340 s
     *
     * @param itemCount Feed size.
     * @param interpolate Set to true to use interpolation and extrapolation
     *                    instead of hard-coded values. Experimental and not supported.
     * @return The maximum time (in ms) allowed for splash screen.
     */
    public static int loadTimeLimit(int itemCount, boolean interpolate) {

        double loadTime;

        // TODO: investigate if the value for small feeds needs adjustment
        int SMALL_LOAD_TIME = 3000;
        int MEDIUM_LOAD_TIME = 4500;
        int LARGE_LOAD_TIME = 11000;
        int EXTRA_LARGE_LOAD_TIME = 340000;
        int DEFAULT_LOAD_TIME = 0; // 4000

        HashMap<Integer, Integer> loadTimeTable = new HashMap<>();
        loadTimeTable.put(SMALL, SMALL_LOAD_TIME);
        loadTimeTable.put(MEDIUM, MEDIUM_LOAD_TIME);
        loadTimeTable.put(LARGE, LARGE_LOAD_TIME);
        loadTimeTable.put(EXTRA_LARGE, EXTRA_LARGE_LOAD_TIME);

        if (interpolate) {
            // use extrapolation and interpolation to estimate values (unsupported)
            if (itemCount <= MEDIUM) {
                loadTime = MEDIUM_LOAD_TIME;
            }
            else if (itemCount > MEDIUM & itemCount <= LARGE) {
                loadTime = MEDIUM_LOAD_TIME + ((LARGE_LOAD_TIME - MEDIUM_LOAD_TIME) / (LARGE -
                        MEDIUM)) * (itemCount - MEDIUM);
            }
            else {
                loadTime = LARGE_LOAD_TIME + ((EXTRA_LARGE_LOAD_TIME - LARGE_LOAD_TIME) /
                        (EXTRA_LARGE - LARGE)) * (itemCount - LARGE);
            }
        }
        else {
            // use hard-coded values
            if (loadTimeTable.containsKey(itemCount)) {
                loadTime = loadTimeTable.get(itemCount);
            }
            else {
                warn(itemCount);
                loadTime = DEFAULT_LOAD_TIME;
            }
        }
        return (int) (loadTime * LIMIT_MULTIPLIER);
    }

    /**
     * Determine base memory usage given size of feed.
     * Uses hard-coded values as default.
     *
     * @param itemCount Feed size.
     * @return Base memory usage in kB.
     */
    public static int getBaseMemory(int itemCount) {

        return getBaseMemory(itemCount, false);
    }

    /**
     * Determine base memory usage.
     * Values obtained from build as of 11/14/2016 used as baseline.
     *
     * Sample feed 1        "size 10"       31 items        ~9 Mb
     * Sample feed 2        "size 100"      393 items       ~14 Mb
     * Sample feed 3        "size 500"      1,516 items     ~27 Mb
     * Sample feed 4        "size 5,000"    11,798 items    ~95 Mb
     *
     * @param itemCount Feed size.
     * @param interpolate Set to true to use interpolation and extrapolation
     *                    instead of hard-coded values. Experimental and not supported.
     * @return Base memory usage in kB.
     */
    public static int getBaseMemory(int itemCount, boolean interpolate) {

        int SMALL_MEM_LIMIT = 9000;
        int MEDIUM_MEM_LIMIT = 14300;
        int LARGE_MEM_LIMIT = 27000;
        int EXTRA_LARGE_MEM_LIMIT = 95000;
        int DEFAULT_MEM_LIMIT = 0; // 9800

        HashMap<Integer, Integer> baseMemoryTable = new HashMap<>();
        baseMemoryTable.put(SMALL, SMALL_MEM_LIMIT);
        baseMemoryTable.put(MEDIUM, MEDIUM_MEM_LIMIT);
        baseMemoryTable.put(LARGE, LARGE_MEM_LIMIT);
        baseMemoryTable.put(EXTRA_LARGE, EXTRA_LARGE_MEM_LIMIT);


        double memLimit;

        if (interpolate) {
            // use extrapolation and interpolation to estimate values (unsupported)
            if (itemCount <= SMALL) {
                memLimit = SMALL;
            }
            else if (itemCount > SMALL && itemCount <= MEDIUM) {
                memLimit = SMALL_MEM_LIMIT + ((MEDIUM_MEM_LIMIT - SMALL_MEM_LIMIT) / (MEDIUM -
                        SMALL)) * (itemCount - SMALL);

            }
            else if (itemCount > MEDIUM && itemCount <= LARGE) {
                memLimit = MEDIUM_MEM_LIMIT + ((LARGE_MEM_LIMIT - MEDIUM_MEM_LIMIT) / (LARGE -
                        MEDIUM)) * (itemCount - MEDIUM);
            }
            else {
                memLimit = LARGE_MEM_LIMIT * ((EXTRA_LARGE_MEM_LIMIT - LARGE_MEM_LIMIT) /
                        (EXTRA_LARGE - LARGE)) * (itemCount - LARGE);
            }
        }
        else {
            // use hard-coded values
            if (baseMemoryTable.containsKey(itemCount)) {
                memLimit = baseMemoryTable.get(itemCount);
            }
            else {
                warn(itemCount);
                memLimit = DEFAULT_MEM_LIMIT;
            }
        }

        return (int) (memLimit * LIMIT_MULTIPLIER);

    }

    /**
     * Determine base memory usage given size of feed.
     * Uses hard-coded values as default.
     *
     * @param itemCount Feed size.
     * @return Base memory usage in kB after starting video player.
     */
    public static int getBaseVideoMemory(int itemCount) {

        return getBaseVideoMemory(itemCount, false);
    }

    /**
     * Determine base memory usage given size of feed.
     * Values obtained from build as of 11/14/2016 used as baseline.
     *
     * Sample feed 1        "size 10"       31 items        ~44 Mb
     * Sample feed 2        "size 100"      393 items       ~47 Mb
     * Sample feed 3        "size 500"      1,516 items     ~56 Mb
     * Sample feed 4        "size 5,000"    11,798 items    ~119 Mb
     *
     * @param itemCount Feed size.
     * @param interpolate Set to true to use interpolation and extrapolation
     *                    instead of hard-coded values. Experimental and not supported.
     * @return Base memory usage in kB after starting video player.
     */
    public static int getBaseVideoMemory(int itemCount, boolean interpolate) {

        double memLimit;

        int SMALL_MEM_LIMIT = 44000;
        int MEDIUM_MEM_LIMIT = 47000;
        int LARGE_MEM_LIMIT = 56000;
        int EXTRA_LARGE_MEM_LIMIT = 119000;
        int DEFAULT_MEM_LIMIT = 0; // 49000

        HashMap<Integer, Integer> baseVideoMemoryTable = new HashMap<>();
        baseVideoMemoryTable.put(SMALL, SMALL_MEM_LIMIT);
        baseVideoMemoryTable.put(MEDIUM, MEDIUM_MEM_LIMIT);
        baseVideoMemoryTable.put(LARGE, LARGE_MEM_LIMIT);
        baseVideoMemoryTable.put(EXTRA_LARGE, EXTRA_LARGE_MEM_LIMIT);

        if (interpolate) {
            // use extrapolation and interpolation to estimate values (unsupported)
            if (itemCount <= SMALL) {
                memLimit = SMALL;
            }
            else if (itemCount > SMALL && itemCount <= MEDIUM) {
                memLimit = SMALL_MEM_LIMIT + ((MEDIUM_MEM_LIMIT - SMALL_MEM_LIMIT) / (MEDIUM -
                        SMALL)) * (itemCount - SMALL);

            }
            else if (itemCount > MEDIUM && itemCount <= LARGE) {
                memLimit = MEDIUM_MEM_LIMIT + ((LARGE_MEM_LIMIT - MEDIUM_MEM_LIMIT) / (LARGE -
                        MEDIUM)) * (itemCount - MEDIUM);
            }
            else {
                warn(itemCount);
                memLimit = LARGE_MEM_LIMIT * ((EXTRA_LARGE_MEM_LIMIT - LARGE_MEM_LIMIT) /
                        (EXTRA_LARGE - LARGE)) * (itemCount - LARGE);
            }
        }
        else {
            // use hard-coded values
            if (baseVideoMemoryTable.containsKey(itemCount)) {
                memLimit = baseVideoMemoryTable.get(itemCount);
            }
            else {
                warn(itemCount);
                memLimit = DEFAULT_MEM_LIMIT;
            }
        }
        return (int) (memLimit * LIMIT_MULTIPLIER);
    }

    /**
     * Get a title known to be unique to the given feed.
     * @param itemCount Feed size.
     * @return Item name.
     */
    public static String getUniqueTitle(int itemCount) {

        List<Integer> sizes = new ArrayList<>();
        sizes.add(SMALL);
        sizes.add(MEDIUM);
        sizes.add(LARGE);
        sizes.add(EXTRA_LARGE);

        if (sizes.contains(itemCount)) {
            return "Sample item " + itemCount;
        }
        else {
            warn(itemCount);
            return "Bamboo Rafting";
        }
    }

    // search string to return no results
    final public static String SEARCH_STRING_INVALID = "incorrect";

    // search string for sample feed
    final public static String SEARCH_STRING_CUSTOM = "Sample";

    static private void warn(int itemCount) {
        Log.w(TestConfig.class.getSimpleName(), "Unsupported feed size: " + itemCount +
                ". Returned value should not be used in a test.");
    }
}