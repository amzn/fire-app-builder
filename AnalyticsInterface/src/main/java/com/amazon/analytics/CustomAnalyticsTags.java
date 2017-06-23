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
package com.amazon.analytics;

import android.content.Context;
import android.util.Log;

import com.amazon.android.utils.FileHelper;
import com.amazon.android.utils.MapHelper;

import java.util.HashMap;

/**
 * Class to manage mapping analytics tags to custom values.
 */
public class CustomAnalyticsTags {

    private static final String TAG = CustomAnalyticsTags.class.getSimpleName();

    private HashMap<String, String> mCustomTags;

    /**
     * Initialize the custom tag map. Override this method to manually add custom tags.
     */
    public void init() {

        mCustomTags = new HashMap<>();
    }

    /**
     * Initialize the custom tag map from the given file.
     *
     * @param context    Context.
     * @param fileNameId File name ID of the file that contains the custom tag mapping.
     */
    public void init(Context context, int fileNameId) {

        init();

        // Read the file only if it exists.
        if (FileHelper.doesFileExist(context, context.getResources().getString(fileNameId))) {
            mCustomTags = MapHelper.loadStringMappingFromJsonFile(context, fileNameId);
            Log.d(TAG, "Custom analytics tags initialized");
        }
    }

    /**
     * Get the custom value for the given analytics tag. If the tag is not customized, return the
     * original, given tag.
     *
     * @param tag Analytics tag.
     * @return Custom value for the analytics tag, or the given tag if it isn't customized.
     */
    public String getCustomTag(String tag) {

        if (mCustomTags == null || mCustomTags.isEmpty()) {
            return tag;
        }
        String customTag = mCustomTags.get(tag);
        return customTag != null ? customTag : tag;
    }

    /**
     * Is the given tag customized.
     *
     * @param tag Tag to check if it's customized.
     * @return True if the tag is customized, false otherwise.
     */
    public boolean tagCustomized(String tag) {

        return mCustomTags != null && mCustomTags.containsKey(tag);
    }

    /**
     * Transform the given map of tags into a map with custom tags in place of the original tags.
     * E.g., Map<Tag, Value> -> Map<CustomTag, Value>
     *
     * @param tagsToCustomize Map of tags to transform into custom tags map.
     * @return Map with custom tags.
     */
    public <T> HashMap<String, T> getCustomTags(HashMap<String, T> tagsToCustomize) {

        return getCustomTags(tagsToCustomize, true);
    }

    /**
     * Transform the given map of tags into a map with custom tags in place of the default tags.
     * E.g., Map<Tag, Value> -> Map<CustomTag, Value>
     *
     * @param tagsToCustomize Map of tags to transform into custom tags map.
     * @param keepDefaultTags True if the original tag should be used if a customized tag was not
     *                        provided, false otherwise.
     * @return Map with custom tags.
     */
    public <T> HashMap<String, T> getCustomTags(HashMap<String, T> tagsToCustomize,
                                                boolean keepDefaultTags) {

        if (mCustomTags == null || mCustomTags.isEmpty()) {
            return keepDefaultTags ? tagsToCustomize : new HashMap<String, T>();
        }
        HashMap<String, T> customTags = new HashMap<>();
        for (String key : tagsToCustomize.keySet()) {
            if (!keepDefaultTags && !tagCustomized(key)) {
                continue;
            }
            customTags.put(getCustomTag(key), tagsToCustomize.get(key));
        }
        return customTags;
    }
}