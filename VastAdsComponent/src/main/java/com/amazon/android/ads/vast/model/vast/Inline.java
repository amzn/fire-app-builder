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

import com.amazon.android.ads.vast.model.vmap.Tracking;
import com.amazon.android.ads.vast.model.vmap.VmapHelper;
import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.utils.ListUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * A representation of the Inline element of a VAST response. Contains all the files and URIs
 * necessary to display the ad.
 */
public class Inline {

    /**
     * Key to get the creatives element.
     */
    private static final String CREATIVES_KEY = "Creatives";

    /**
     * Key to get the creative element.
     */
    private static final String CREATIVE_KEY = "Creative";

    /**
     * A list of creatives.
     */
    private List<Creative> mCreatives;

    /**
     * A list of impressions.
     */
    private List<String> mImpressions;

    /**
     * A list of error URLs.
     */
    private List<String> mErrorUrls;

    /**
     * Constructor.
     *
     * @param adMap A map containing the data necessary for creating the inline ad.
     */
    public Inline(Map<String, Map> adMap) {

        mCreatives = new ArrayList<>();
        mImpressions = new ArrayList<>();
        mErrorUrls = new ArrayList<>();

        if (adMap.containsKey((VmapHelper.INLINE_KEY))) {
            Map<String, Map> inLineMap = adMap.get(VmapHelper.INLINE_KEY);

            Map<String, Map> creativesMap = inLineMap.get(CREATIVES_KEY);
            if (creativesMap != null) {
                List<Map> creativeList = ListUtils.getValueAsMapList(creativesMap, CREATIVE_KEY);

                for (Map<String, Map> creativeMap : creativeList) {
                    mCreatives.add(new Creative(creativeMap));
                }
            }
            if (inLineMap.containsKey(VmapHelper.IMPRESSION_KEY)) {
                mImpressions.addAll(VmapHelper.getStringListFromMap(inLineMap,
                                                                    VmapHelper.IMPRESSION_KEY));
            }

            if (inLineMap.containsKey(VmapHelper.ERROR_ELEMENT_KEY)) {
                mErrorUrls.addAll(VmapHelper.getStringListFromMap(inLineMap,
                                                                  VmapHelper.ERROR_ELEMENT_KEY));
            }
        }
    }

    /**
     * Get the list of creatives.
     *
     * @return List of creatives.
     */
    public List<Creative> getCreatives() {

        return mCreatives;
    }

    /**
     * Set the list of creatives.
     *
     * @param creatives List of creatives.
     */
    public void setCreatives(List<Creative> creatives) {

        mCreatives = creatives;
    }

    /**
     * Get the list of impressions.
     *
     * @return List of impressions.
     */
    public List<String> getImpressions() {

        return mImpressions;
    }

    /**
     * Set the list of impressions.
     *
     * @param impressions List of impressions.
     */
    public void setImpressions(List<String> impressions) {

        mImpressions = impressions;
    }

    /**
     * Get a list of media files from all the creatives of this ad.
     *
     * @return List of media files.
     */
    public List<MediaFile> getMediaFiles() {

        List<MediaFile> mediaFiles = new ArrayList<>();
        for (Creative creative : mCreatives) {
            mediaFiles.addAll(creative.getVastAd().getMediaFiles());
        }
        return mediaFiles;
    }

    /**
     * Get a map of tracking events from all the creatives of this ad.
     *
     * @return Map of tracking events.
     */
    public HashMap<String, List<String>> getTrackingEvents() {

        HashMap<String, List<String>> trackingEventMap = new HashMap<>();
        for (Creative creative : mCreatives) {
            List<Tracking> trackingList = creative.getVastAd().getTrackingEvents();
            Tracking.addTrackingEventsToMap(trackingEventMap, trackingList);
        }
        return trackingEventMap;
    }

    /**
     * Get the list of error URLs.
     *
     * @return List of error URLs.
     */
    public List<String> getErrorUrls() {

        return mErrorUrls;
    }

    @Override
    public String toString() {

        return "Inline{" +
                ", mCreatives=" + mCreatives +
                ", mImpressions=" + mImpressions +
                ", mErrorUrls=" + mErrorUrls +
                '}';
    }
}
