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
package com.amazon.android.ads.vast.model.vmap;

import com.amazon.dynamicparser.impl.XmlParser;

import android.util.Log;

import java.util.Map;

/**
 * An ad response document that is not VAST 3.0
 */
public class CustomAdData {

    private static final String TAG = CustomAdData.class.getSimpleName();

    /**
     * Arbitrary string data that represents non-VAST ad response.
     */
    private String mCustomAdResponse;

    /**
     * The ad response template employed by the ad response document.
     */
    private String mTemplateType;

    /**
     * Constructor.
     *
     * @param map A map containing the data needed to create the custom data object.
     */
    public CustomAdData(Map map) {

        if (map == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributeMap = (Map<String, String>) map.get(XmlParser.ATTRIBUTES_TAG);
        if (attributeMap != null) {
            setTemplateType(attributeMap.get(VmapHelper.TEMPLATE_TYPE_KEY));
        }

        setCustomAdResponse(VmapHelper.getTextValueFromMap(map));
    }

    /**
     * Get the custom ad response.
     *
     * @return The custom ad response.
     */
    public String getCustomAdResponse() {

        return mCustomAdResponse;
    }

    /**
     * Set the custom ad response.
     *
     * @param customAdResponse The custom ad response.
     */
    public void setCustomAdResponse(String customAdResponse) {

        mCustomAdResponse = customAdResponse;
    }

    /**
     * Get the template type.
     *
     * @return The template type.
     */
    public String getTemplateType() {

        return mTemplateType;
    }

    /**
     * Set the template type.
     *
     * @param templateType The template type.
     */
    public void setTemplateType(String templateType) {

        mTemplateType = templateType;
    }

    @Override
    public String toString() {

        return "CustomAdData{" +
                "mCustomAdResponse='" + mCustomAdResponse + '\'' +
                ", mTemplateType='" + mTemplateType + '\'' +
                '}';
    }
}