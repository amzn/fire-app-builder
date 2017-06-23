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
 * A URL to a secondary ad server that will provide the ad response document.
 */
public class AdTagURI {

    private static final String TAG = AdTagURI.class.getSimpleName();

    /**
     * The URL to a secondary ad server that will provide the ad response document.
     */
    private String mUri;

    /**
     * The ad response template employed by thee ad response document.
     */
    private String mTemplateType;

    /**
     * Constructor.
     *
     * @param map A map containing the data needed to create the ad tag URI.
     */
    public AdTagURI(Map map) {

        if (map == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributeMap = (Map<String, String>) map.get(XmlParser.ATTRIBUTES_TAG);
        if (attributeMap != null) {
            setTemplateType(attributeMap.get(VmapHelper.TEMPLATE_TYPE_KEY));
        }
        setUri(VmapHelper.getTextValueFromMap(map));
    }

    /**
     * Get the URI.
     *
     * @return The URI.
     */
    public String getUri() {

        return mUri;
    }

    /**
     * Set the URI.
     *
     * @param uri The URI.
     */
    public void setUri(String uri) {

        mUri = uri;
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

        return "AdTagURI{" +
                "mUri='" + mUri + '\'' +
                ", mTemplateType='" + mTemplateType + '\'' +
                '}';
    }
}