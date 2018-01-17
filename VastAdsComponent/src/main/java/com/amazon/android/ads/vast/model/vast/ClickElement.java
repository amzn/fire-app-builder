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
package com.amazon.android.ads.vast.model.vast;

import com.amazon.android.ads.vast.model.vmap.VmapHelper;
import com.amazon.dynamicparser.impl.XmlParser;

import java.util.Map;

/**
 * A representation of Click element which might be available in different parts of vast response.
 */
public class ClickElement {

    private static final String TAG = ClickElement.class.getSimpleName();

    /**
     * URI of click element need to be used for a specific action.
     */
    private String mUri;

    /**
     * Id of click element.
     */
    private String mId;

    /**
     * Constructor.
     *
     * @param clickElementMap A map containing the data needed to create the click element.
     */
    public ClickElement(Map<String, Map> clickElementMap) {

        if (clickElementMap != null) {

            setUri(VmapHelper.getTextValueFromMap(clickElementMap));
            Map<String, String> attributes = clickElementMap.get(XmlParser.ATTRIBUTES_TAG);
            if (attributes != null) {
                setId(attributes.get(VmapHelper.ID_KEY));
            }
        }
    }

    /**
     * Get uri.
     *
     * @return The uri.
     */
    public String getUri() {

        return mUri;
    }

    /**
     * Set the uri.
     *
     * @param uri The uri.
     */
    public void setUri(String uri) {

        mUri = uri;
    }

    /**
     * Get id.
     *
     * @return The id.
     */
    public String getId() {

        return mId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id) {

        mId = id;
    }

    @Override
    public String toString() {

        return "ClickElement{" +
                "mUri='" + mUri + '\'' +
                ", mId='" + mId + '\'' +
                '}';
    }
}
