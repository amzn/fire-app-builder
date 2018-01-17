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
import android.content.SharedPreferences;
import android.preference.PreferenceManager;


/**
 * Preferences helper class.
 */
public class Preferences {

    /**
     * Context.
     */
    private static Context sContext;

    /**
     * Set context.
     *
     * @param context Context.
     */
    public static void setContext(Context context) {

        if (sContext == null) {
            sContext = context;
        }
    }

    /**
     * Set string value to preferences.
     *
     * @param key   Key value.
     * @param value String value.
     */
    public static void setString(String key, String value) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putString(key, value);
        editor.apply();
    }

    /**
     * Set boolean value to preferences.
     *
     * @param key   Key value.
     * @param value Boolean value.
     */
    public static void setBoolean(String key, boolean value) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putBoolean(key, value);
        editor.apply();
    }

    /**
     * Set long value to preferences.
     *
     * @param key   Key value.
     * @param value Long value.
     */
    public static void setLong(String key, long value) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        SharedPreferences.Editor editor = prefs.edit();

        editor.putLong(key, value);
        editor.apply();
    }

    /**
     * Set string value to preferences.
     *
     * @param key Key value.
     * @return String value.
     */
    public static String getString(String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        return prefs.getString(key, "");
    }

    /**
     * Set boolean value to preferences.
     *
     * @param key Key value.
     * @return Boolean value.
     */
    public static boolean getBoolean(String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        return prefs.getBoolean(key, false);
    }

    /**
     * Set long value to preferences.
     *
     * @param key Key value.
     * @return Long value.
     */
    public static long getLong(String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        return prefs.getLong(key, 0);
    }

    /**
     * Checks if there is a preference stored for the given key.
     *
     * @param key The key to check.
     * @return True if there's a value stored for the key; false otherwise.
     */
    public static boolean containsPreference(String key) {

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(sContext);
        return prefs.contains(key);
    }
}
