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
 * Part of the <Extensions> element. Proprietary XML data, expressed in a unique namespace.
 */
public class Extension {

    private static final String TAG = Extension.class.getSimpleName();

    /**
     * Key for getting the suppress bumper attribute.
     */
    private static final String SUPPRESS_BUMPER_KEY = "suppress_bumper";

    /**
     * Key for getting the type attribute.
     */
    private static final String TYPE_KEY = "type";

    /**
     * The XML content of the Extension.
     */
    private boolean mSuppressBumper;

    /**
     * The type of the extension. The type value must be globally unique. A URI is recommended
     */
    private String mType;

    /**
     * Constructor.
     *
     * @param extensionMap A map containing the data needed to create the extension.
     */
    public Extension(Map<String, Map> extensionMap) {

        if (extensionMap == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributesMap =
                (Map<String, String>) extensionMap.get(XmlParser.ATTRIBUTES_TAG);

        setSuppressBumper(Boolean.valueOf(attributesMap.get(SUPPRESS_BUMPER_KEY)));
        setType(attributesMap.get(TYPE_KEY));
    }

    /**
     * Get the type of the extension.
     *
     * @return The extension type.
     */
    public String getType() {

        return mType;
    }

    /**
     * Set the type of the extension.
     *
     * @param type The extension type.
     */
    public void setType(String type) {

        mType = type;
    }

    /**
     * Get the XML content value.
     *
     * @return The XML content.
     */
    public boolean isSuppressBumper() {

        return mSuppressBumper;
    }

    /**
     * Set the XML content suppressBumper.
     *
     * @param suppressBumper The XML content.
     */

    public void setSuppressBumper(boolean suppressBumper) {

        mSuppressBumper = suppressBumper;
    }

    @Override
    public String toString() {

        return "Extension{" +
                "mSuppressBumper=" + mSuppressBumper +
                ", mType='" + mType + '\'' +
                '}';
    }
}
