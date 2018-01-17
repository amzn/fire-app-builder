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

import com.amazon.ads.IAds;
import com.amazon.android.ads.vast.model.vast.AdElement;
import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.android.ads.vast.processor.ResponseValidator;
import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.utils.ListUtils;

import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * This class represents a parsed VMAP response and follows selected parts of the VMAP 1.0 Schema as
 * presented by IAB here: https://www.iab.com/wp-content/uploads/2015/06/VMAP.pdf
 */
public class VmapResponse {

    private static final String TAG = VmapResponse.class.getSimpleName();

    /**
     * Key to find the ad break element.
     */
    private static final String AD_BREAK_KEY = "vmap:AdBreak";

    /**
     * List of ad breaks.
     */
    private List<AdBreak> mAdBreaks;

    /**
     * List of ad elements.
     */
    private List<AdElement> mAdElements;

    /**
     * Current Ad to be played
     */
    private AdElement mCurrentAd;

    /**
     * Current Ad Break being played
     */
    private AdBreak mCurrentAdBreak;

    /**
     * Vmap version
     */
    private String mVmapVersion;

    /**
     * Constructor.
     */
    public VmapResponse() {

        mAdBreaks = new ArrayList<>();
    }

    /**
     * Creates an instance of the {@link VmapResponse} class with the given xml data. The xml should
     * follow the official VMAP schema as described here:
     * https://www.iab.com/wp-content/uploads/2015/06/VMAP.pdf
     *
     * @param xmlMap The xml data.
     * @return A model object or null if there were instantiation errors.
     */
    public static VmapResponse createInstance(Map<String, Map> xmlMap) {

        VmapResponse model = new VmapResponse();
        Log.d(TAG, "Creating VMAP model from xml data");

        if (xmlMap != null) {

            Map<String, String> attributes = xmlMap.get(XmlParser.ATTRIBUTES_TAG);

            //setting the VMAP version
            if (attributes != null) {
                model.setVmapVersion(attributes.get(VmapHelper.VERSION_KEY));

                // All VMAP models must have at least one ad break, return null if none are found.
                if (xmlMap.get(AD_BREAK_KEY) == null) {
                    Log.e(TAG, "VMAP model xml contains no ad break element");
                    return null;
                }
                List<Map> adBreakMapList = ListUtils.getValueAsMapList(xmlMap, AD_BREAK_KEY);
                for (Map adBreakMap : adBreakMapList) {
                    AdBreak adBreak = new AdBreak(adBreakMap);
                    //Validate if Ad break follows VMAP specification
                    if (ResponseValidator.validateAdBreak(adBreak)) {
                        model.getAdBreaks().add(adBreak);
                    }
                }
            }
        }

        return model;
    }

    /**
     * Create a VMAP response with the given VAST response. It will set the VAST response ad as a
     * linear pre-roll ad to be played at the start.
     *
     * @param vastResponse The VAST ad response.
     * @return Teh VMAP response.
     */
    public static VmapResponse createInstanceWithVast(VastResponse vastResponse) {

        VmapResponse vmapResponse = new VmapResponse();

        // Create an Ad Break to store the pre-roll ad.
        AdBreak adBreak = new AdBreak();
        adBreak.setBreakId(IAds.PRE_ROLL_AD);
        adBreak.setTimeOffset(AdBreak.TIME_OFFSET_START);
        adBreak.setBreakType(AdBreak.BREAK_TYPE_LINEAR);

        // Create an Ad Source to store the VAST ad.
        AdSource adSource = new AdSource();
        adSource.setVastResponse(vastResponse);

        adBreak.setAdSource(adSource);

        vmapResponse.addAdBreak(adBreak);

        return vmapResponse;
    }

    /**
     * Get the vmap version.
     *
     * @return The vmap version.
     */
    public String getVmapVersion() {

        return mVmapVersion;
    }

    /**
     * Set the ad breaks.
     *
     * @param vmapVersion The vmap version.
     */
    public void setVmapVersion(String vmapVersion) {

        mVmapVersion = vmapVersion;
    }

    /**
     * Get the ad breaks.
     *
     * @return The ad breaks.
     */
    public List<AdBreak> getAdBreaks() {

        return mAdBreaks;
    }

    /**
     * Set the ad breaks.
     *
     * @param adBreaks The ad breaks.
     */
    public void setAdBreaks(List<AdBreak> adBreaks) {

        mAdBreaks = adBreaks;
    }

    /**
     * Add an ad break.
     *
     * @param adBreak The ad break.
     */
    public void addAdBreak(AdBreak adBreak) {

        if (mAdBreaks == null) {
            mAdBreaks = new ArrayList<>();
        }
        mAdBreaks.add(adBreak);
    }

    /**
     * Get all the ads with "preroll" in their ids.
     *
     * @return List of ads.
     */
    public List<AdBreak> getPreRollAdBreaks(long duration) {

        return getAdBreaksInRange(duration, -1, 1);
    }

    /**
     * Get all the ads with "midroll" in their ids.
     *
     * @return List of ads.
     */
    public List<AdBreak> getMidRollAdBreaks(long duration) {

        return getAdBreaksInRange(duration, 0, duration);
    }

    /**
     * Get all the ads with "postroll" in their ids.
     *
     * @return List of ids.
     */
    public List<AdBreak> getPostRollAdBreaks(long duration) {

        if (duration == 0) {
            return new ArrayList<>();
        }
        return getAdBreaksInRange(duration, duration - 1, duration + 1);
    }

    /**
     * Get a list of ad breaks for the given playback position range and duration.
     *
     * @param duration The duration of the video.
     * @param low      The ad playback position should be higher than this.
     * @param high     The ad playback position should be lower than this.
     * @return List of ad breaks.
     */
    private List<AdBreak> getAdBreaksInRange(long duration, long low, long high) {

        List<AdBreak> ads = new ArrayList<>();
        for (AdBreak ad : mAdBreaks) {
            double timeOffset = ad.getConvertedTimeOffset(duration);
            if (timeOffset > low && timeOffset < high) {
                ads.add(ad);
            }
        }
        return ads;
    }

    /**
     * Get the current ad to play.
     *
     * @return The current ad.
     */
    public AdElement getCurrentAd() {

        return mCurrentAd;
    }

    /**
     * Set the current ad to play.
     *
     * @param currentAd The current ad.
     */
    public void setCurrentAd(AdElement currentAd) {

        mCurrentAd = currentAd;
    }

    /**
     * Get the current ad break being played.
     *
     * @return The current ad break.
     */
    public AdBreak getCurrentAdBreak() {

        return mCurrentAdBreak;
    }

    /**
     * Set the current ad being played.
     *
     * @param currentAdBreak The current ad break.
     */
    public void setCurrentAdBreak(AdBreak currentAdBreak) {

        mCurrentAdBreak = currentAdBreak;
    }

    @Override
    public String toString() {

        return "VmapResponse{" +
                "mAdBreaks=" + mAdBreaks +
                ", mAdElements=" + mAdElements +
                ", mCurrentAd=" + mCurrentAd +
                ", mVmapVersion='" + mVmapVersion + '\'' +
                '}';
    }
}