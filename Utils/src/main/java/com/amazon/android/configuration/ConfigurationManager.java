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
package com.amazon.android.configuration;

import android.content.Context;

import java.util.HashMap;
import java.util.Map;

/**
 * Configuration manager class which provides in memory config storage.
 */
public class ConfigurationManager {

    /**
     * Debug TAG.
     */
    private static final String TAG = ConfigurationManager.class.getSimpleName();

    /**
     * Application context.
     */
    private final Context mAppContext;

    /**
     * Singleton instance.
     */
    private static ConfigurationManager sInstance;

    /**
     * Lock object for singleton pattern.
     */
    private static final Object sLock = new Object();

    /**
     * Typeface config hash map.
     */
    private Map<String, String> mTypefaceConfigValueMap = new HashMap<>();

    /**
     * Integer config hash map.
     */
    private Map<String, Integer> mIntegerConfigValueMap = new HashMap<>();

    /**
     * Boolean config hash map.
     */
    private Map<String, Boolean> mBooleanConfigValueMap = new HashMap<>();

    /**
     * Singleton private constructor.
     *
     * @param context Context.
     */
    private ConfigurationManager(Context context) {

        mAppContext = context.getApplicationContext();
    }

    /**
     * Get instance of configuration manager.
     *
     * @param context Context.
     * @return Configuration singleton instance.
     */
    public static ConfigurationManager getInstance(Context context) {

        synchronized (sLock) {
            if (sInstance == null) {
                sInstance = new ConfigurationManager(context.getApplicationContext());
            }
            return sInstance;
        }
    }

    /**
     * Get typeface value by key.
     *
     * @param key Key value.
     * @return The typeface.
     */
    public String getTypefacePath(String key) {

        return mTypefaceConfigValueMap.get(key);
    }

    /**
     * Get integer value by key.
     *
     * @param key Key value.
     * @return The integer value.
     */
    public int getIntegerValue(String key) {

        return mIntegerConfigValueMap.get(key);
    }

    /**
     * Get boolean value by key.
     *
     * @param key Key value.
     * @return The boolean value.
     */
    public boolean getBooleanValue(String key) {

        return mBooleanConfigValueMap.get(key);
    }

    /**
     * Set typeface value by key.
     *
     * @param key          Key value.
     * @param typefacePath Typeface value.
     * @return Configuration manager reference.
     */
    public ConfigurationManager setTypefacePathValue(String key, String typefacePath) {

        mTypefaceConfigValueMap.put(key, typefacePath);
        return this;
    }

    /**
     * Set integer value by key.
     *
     * @param key   Key value.
     * @param value Integer value.
     * @return Configuration manager reference.
     */
    public ConfigurationManager setIntegerValue(String key, int value) {

        mIntegerConfigValueMap.put(key, value);
        return this;
    }

    /**
     * Set boolean value by key.
     *
     * @param key   Key value.
     * @param value Boolean value.
     * @return Configuration manager reference.
     */
    public ConfigurationManager setBooleanValue(String key, boolean value) {

        mBooleanConfigValueMap.put(key, value);
        return this;
    }
}
