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

import android.util.Log;

import java.security.InvalidParameterException;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * This class helps with String and Map manipulations. This class contains one
 * method to inject strings into another string, and one method to follow an
 * XPATH string through a Map.
 */
public class PathHelper {

    private static final String TAG = PathHelper.class.getSimpleName();
    /*
     * The regex prefix with escape values.
     */
    private static final String REGEX_PREFIX_ESCAPE = "\\$\\$";
    /*
     * The regex prefix value.
     */
    private static final String REGEX_PREFIX = "$$";
    /*
     * The regex for matching a digit with escape value.
     */
    private static final String REGEX_INT_ESCAPE = "\\d";
    /*
     * The word to search for in the regex.
     */
    private static final String REGEX_WORD = "par";
    /**
     * The maximum number of values that the pars array of {@link PathHelper#injectParameters
     * (String, String[])} will support.
     */
    public static final int MAX_PARS = 10;

    /**
     * Path separator.
     */
    public static final String PATH_SEPARATOR = "/";

    /**
     * This method injects strings into another string. The entry points
     * into where the strings will be injected are designated by the
     * following pattern: $$par#$$
     * <p>
     * The number found in the pattern corresponds to the string found at
     * that index in the pars array, so par0 equals the first item
     * in the array. With this pattern, only 10 parameters are supported at a time: par0 - par9.
     * <p>
     * If there are more parameters in the pars array than supported an InvalidParameterException
     * is throw.
     * <p>
     * Example:
     * data: "Hello$$par0$$GoodBye$$par1$$"
     * pars: ["Hello", "GoodBye"]
     * return: "HelloHelloGoodByeGoodBye"
     *
     * @param data The string to inject into.
     * @param pars The strings to be injected into the data string.
     * @return A String that is composed of the data string with the
     * pars strings injected into it.
     */
    public static String injectParameters(String data, String[] pars) throws
            MalformedInjectionStringException {

        if (data == null || pars == null) {
            return null;
        }

        // Only support max parameters.
        if (pars.length > MAX_PARS) {
            Log.e(TAG, "Too many strings in pars. Method supports only 10.");
            throw new InvalidParameterException("too many strings in pars. Method ony supports 10" +
                                                        " but " + pars.length + " were passed in.");
        }

        // Build the regex that will support max parameters
        String regex = buildParameterMatchingRegex();

        // Indexing into the pattern to reach start of parameter number. This will be the prefix
        // without the escape characters and the word that the regex includes.
        int parIndexStart = REGEX_PREFIX.length() + REGEX_WORD.length();

        // Indexing into the pattern to reach end of the parameter number which will be single
        // digit.
        int parIndexEnd = parIndexStart + 1;

        StringBuffer stringBuffer = new StringBuffer(data.length());
        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(data);
        int i = 0;
        while (matcher.find()) {
            String par = matcher.group(i);

            int parIndex = Integer.valueOf(par.substring(parIndexStart, parIndexEnd));

            try {
                par = pars[parIndex];
                matcher.appendReplacement(stringBuffer, Matcher.quoteReplacement(par));
            }
            catch (ArrayIndexOutOfBoundsException e) {
                Log.e(TAG, "Couldn't inject parameter at index: " + parIndex, e);
                throw new MalformedInjectionStringException(MalformedInjectionStringException
                                                                    .message + "Couldn't inject " +
                                                                    "parameter at index: " +
                                                                    parIndex, e);
            }
        }
        matcher.appendTail(stringBuffer);
        return stringBuffer.toString();
    }

    /**
     * Get key value from the given path. Ex: /path1/path2/key {@code ->} key.
     *
     * @param path String input.
     * @return Key string.
     */
    public static String getKeyFromPath(String path) {

        if (path == null) {
            return null;
        }

        // Find the last separator.
        int lastPathSeparatorPos = path.lastIndexOf(PATH_SEPARATOR);
        // Zero if not found as string start at zero.
        if (lastPathSeparatorPos < 0) {
            lastPathSeparatorPos = 0;
        }
        else {
            // Skip separator itself.
            lastPathSeparatorPos++;
        }
        // Return only the key.
        return path.substring(lastPathSeparatorPos);
    }

    /**
     * Check if given string is a path.
     *
     * @param path A string that represents the path to follow in the map. Each step
     *             in the path represents a key in the map and should be separated
     *             by a '/' character. Example: 'root/level1/level2'
     * @return True if given string is a path.
     */
    public static boolean hasAPath(String path) {

        if (path == null) {
            return false;
        }

        return (path.indexOf(PATH_SEPARATOR) >= 0);
    }

    /**
     * This method follows a path through a map and returns the last map for the given path.
     *
     * @param map  The map to traverse through.
     * @param path A string that represents the path to follow in the map. Each step
     *             in the path represents a key in the map and should be separated
     *             by a '/' character. Example: 'root/level1/level2'
     * @return Last map for the given path.
     */
    public static Map getMapByPath(Map<String, Object> map, String path) {

        if (map == null || path == null) {
            return null;
        }

        String[] keys = path.split(PATH_SEPARATOR);

        // Traverse into the map using each key that was a part of the path.
        for (String key : keys) {
            Map<String, Object> next;
            try {
                // Try to get the next map using the current key
                //noinspection unchecked
                next = (Map<String, Object>) map.get(key);
            }
            // If using the current key did not retrieve a map object, we are done traversing the
            // map.
            catch (ClassCastException e) {
                // Signifies we are done traversing the map.
                next = null;
            }

            // If we are done traversing the map leave the loop because we have found the value
            // or can't traverse any further down the path.
            if (next == null) {
                break;
            }

            // Set the map to the current map we just found using the key so we can traverse
            // deeper down the path.
            map = next;
        }

        // Using the last map found and the last key provided in the path, return the value.
        return map;
    }

    /**
     * This method follows a path through a map and returns the value at that location.
     *
     * @param map  The map to traverse through.
     * @param path A string that represents the path to follow in the map. Each step
     *             in the path represents a key in the map and should be separated
     *             by a '/' character. Example: 'root/level1/level2'
     * @return The value that is associated with the key that the path leads to, or null.
     */
    public static Object getValueByPath(Map<String, Object> map, String path) {
        // Get the right map by using the path.
        map = getMapByPath(map, path);

        // Return null if map is null.
        if (map == null) {
            return null;
        }
        // Return key value.
        return map.get(getKeyFromPath(path));
    }

    /**
     * Checks a given string for the parameter injection pattern: $$par#$$
     *
     * @param string The string to check if the pattern is exists in.
     * @return True if the pattern is found, false otherwise.
     */
    public static boolean containsParameterMatchingRegex(String string) {

        if (string == null) {
            return false;
        }
        Pattern pattern = Pattern.compile(buildParameterMatchingRegex());
        Matcher matcher = pattern.matcher(string);
        return matcher.find();
    }

    /*
     * Builds the regex pattern to use for injecting parameter strings into other strings.
     * $$par#$$
     */
    private static String buildParameterMatchingRegex() {

        return REGEX_PREFIX_ESCAPE + REGEX_WORD + REGEX_INT_ESCAPE + REGEX_PREFIX_ESCAPE;
    }

    /**
     * An exception class for a string that does not fit the expectations of the injectParameters
     * method. The string is expected to have enough parameter patterns to match the number of
     * strings passed in the pars array.
     */
    public static class MalformedInjectionStringException extends Exception {

        /**
         * The informative message for this specific exception.
         */
        public static final String message = "Data string does not contain proper amount of " +
                "injection " +
                "points. ";

        /**
         * Constructor for the exception.
         *
         * @param message The custom message to display.
         * @param e       The throwable exception that took place.
         */
        public MalformedInjectionStringException(String message, Throwable e) {

            super(message, e);
        }
    }
}

