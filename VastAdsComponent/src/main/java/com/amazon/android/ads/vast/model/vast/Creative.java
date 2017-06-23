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
 * A representation of the Creative element of a VAST response. Note: does not support the
 * Nonlinear element.
 */
public class Creative {

    /**
     * Key to get the linear element.
     */
    private static final String LINEAR_KEY = "Linear";

    /**
     * Key to get the sequence attribute.
     */
    private static final String SEQUENCE_KEY = "sequence";

    /**
     * Key to get the companion ads element.
     */
    private static final String COMPANION_ADS_KEY = "CompanionAds";

    /**
     * Key to get the VAST ad id for VAST 2.0 response.
     */
    private static final String VAST_2_AD_ID_KEY = "AdID";

    /**
     * The creative id.
     */
    private String mId;

    /**
     * The sequence.
     */
    private String mSequence;

    /**
     * The Ad type. Should be either a LinearAd or CompanionAd.
     */
    private VastAd mVastAd;

    /**
     * Constructor.
     *
     * @param creativeMap The map that contains the data to create the creative.
     */
    public Creative(Map<String, Map> creativeMap) {

        Map<String, String> attributes = creativeMap.get(XmlParser.ATTRIBUTES_TAG);
        if (attributes != null) {
            if (attributes.containsKey(VmapHelper.ID_KEY)) {
                setId(attributes.get(VmapHelper.ID_KEY));
            }
            else {
                setId(attributes.get(VAST_2_AD_ID_KEY));
            }
            setSequence(attributes.get(SEQUENCE_KEY));
        }
        if (creativeMap.containsKey(LINEAR_KEY)) {
            mVastAd = new LinearAd(creativeMap.get(LINEAR_KEY));
        }
        else if (creativeMap.containsKey(COMPANION_ADS_KEY)) {
            mVastAd = new CompanionAd(creativeMap.get(COMPANION_ADS_KEY));
        }
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

    /**
     * Get the sequence.
     *
     * @return The sequence.
     */
    public String getSequence() {

        return mSequence;
    }

    /**
     * Set the sequence.
     *
     * @param sequence The sequence.
     */
    public void setSequence(String sequence) {

        mSequence = sequence;
    }

    /**
     * Get the ad.
     *
     * @return The ad.
     */
    public VastAd getVastAd() {

        return mVastAd;
    }

    /**
     * Set the ad.
     *
     * @param vastAd The ad.
     */
    public void setVastAd(VastAd vastAd) {

        mVastAd = vastAd;
    }

    @Override
    public String toString() {

        return "Creative{" +
                "mId='" + mId + '\'' +
                ", mSequence='" + mSequence + '\'' +
                ", mVastAd=" + mVastAd +
                '}';
    }
}
