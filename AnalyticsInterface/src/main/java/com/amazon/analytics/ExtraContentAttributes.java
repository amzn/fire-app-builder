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

import com.amazon.android.utils.MapHelper;

import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

/**
 * Class to manage mapping between analytics attributes and Content extras. If a Content object has
 * an extra field, this class and the related config file can be used to map the extra field to an
 * analytics attribute.
 */
public class ExtraContentAttributes {

    private static final String TAG = ExtraContentAttributes.class.getSimpleName();

    /**
     * Map from analytics attribute to Content extra
     */
    private static HashMap<String, String> mAttributeToExtraMap = new HashMap<>();

    /**
     * Initialize the attribute to extra map.
     *
     * @param context Context.
     */
    public static void init(Context context) {

        mAttributeToExtraMap = MapHelper.loadStringMappingFromJsonFile(context,
                R.string.analytics_attribute_to_content_extra_map_file);
        Log.d(TAG, "Extra attribute mapping: " + mAttributeToExtraMap.toString());
    }

    /**
     * Transform the Content extras into a map of analytics attributes.
     *
     * @param extras Content extras map.
     * @return Analytics attributes.
     */
    public static Map<String, Object> getExtraAttributes(Map<String, Object> extras) {

        HashMap<String, Object> attributes = new HashMap<>();

        if (extras == null) {
            return attributes;
        }

        for (Entry<String, String> entry : mAttributeToExtraMap.entrySet()) {
            if (extras.containsKey(entry.getValue())) {
                attributes.put(entry.getKey(), extras.get(entry.getValue()));
            }
        }
        return attributes;
    }
}
