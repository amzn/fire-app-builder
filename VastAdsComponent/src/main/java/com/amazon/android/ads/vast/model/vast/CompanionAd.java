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

import com.amazon.utils.ListUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A representation of the Companion Ad element of a VAST response.
 */
public class CompanionAd extends VastAd {

    private static final String COMPANION_KEY = "Companion";

    /**
     * List of Companion objects.
     */
    private List<Companion> mCompanions;

    /**
     * Constructor.
     *
     * @param map A map containing the data needed to create the Companion Ad.
     */
    CompanionAd(Map<String, Map> map) {

        super();
        mCompanions = new ArrayList<>();
        List<Map> companionsList = ListUtils.getValueAsMapList(map, COMPANION_KEY);
        // Create all the companions.
        for (Map<String, Map> companionMap : companionsList) {
            Companion companion = new Companion(companionMap);
            super.getTrackingEvents().addAll(companion.getTrackings());
            mCompanions.add(companion);
        }
    }

    /**
     * Get the companions.
     *
     * @return List of companions.
     */
    public List<Companion> getCompanions() {

        return mCompanions;
    }

    /**
     * Set the companions.
     *
     * @param companions List of companions.
     */
    public void setCompanions(List<Companion> companions) {

        mCompanions = companions;
    }


    @Override
    public String toString() {

        return "CompanionAd{" +
                "mCompanions=" + mCompanions +
                "mMediaFiles=" + super.getMediaFiles() +
                ", mTrackingEvents=" + super.getTrackingEvents() +
                '}';
    }
}
