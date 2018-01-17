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

import com.amazon.ads.IAds;
import com.amazon.android.ads.vast.model.vmap.Tracking;
import com.amazon.android.ads.vast.model.vmap.VmapHelper;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.android.utils.PathHelper;
import com.amazon.dynamicparser.IParser;
import com.amazon.dynamicparser.impl.XmlParser;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * A representation of a Single Ad element of a VAST Ad response. Contains all the files and URIs
 * necessary to display the ad.
 */
public class AdElement implements Comparable<AdElement> {

    private static final String TAG = VastResponse.class.getSimpleName();

    /**
     * Key to get the wrapper element.
     */
    private static final String WRAPPER_ELEMENT_KEY = "Wrapper";

    /**
     * Key to get the VAST ad tag URI element.
     */
    private static final String VASTADTAGURI_ELEMENT_KEY = "VASTAdTagURI";

    /**
     * Path for the TrackingEvents element.
     */
    private static final String TRACKING_EVENTS_PATH = "Creatives/Creative/Linear/TrackingEvents";

    /**
     * The ad id.
     */
    private String mId;

    /**
     * The ad sequence.
     */
    private int mAdSequence = 0;

    /**
     * The Inline element.
     */
    private Inline mInlineAd;

    /**
     * The selected media file. This will be used to display the ad video.
     */
    private String mSelectedMediaFileUrl;

    /**
     * Creates a instance of a Ad Element given the parsed xml data.
     *
     * @param xmlMap A map representing the parsed xml response.
     * @return An Ad Element instance.
     */
    public static AdElement createInstance(Map<String, Map> xmlMap) {

        AdElement adElement = new AdElement();
        Log.d(TAG, "Creating VAST response from xml map");

        Map<String, String> attributes = xmlMap.get(XmlParser.ATTRIBUTES_TAG);

        if (attributes != null) {
            adElement.setId(attributes.get(VmapHelper.ID_KEY));
            if (attributes.containsKey(VmapHelper.SEQUENCE_KEY)) {
                adElement.setAdSequence(Integer.valueOf(attributes.get(VmapHelper.SEQUENCE_KEY)));
            }

            if (xmlMap.containsKey(WRAPPER_ELEMENT_KEY)) {
                processWrapper(xmlMap.get(WRAPPER_ELEMENT_KEY), adElement);
            }
            else {
                adElement.setInlineAd(new Inline(xmlMap));
            }
        }

        return adElement;
    }

    /**
     * Process the wrapper element. Creates a VastResponse from the VASTAdTagURI element and then
     * ads the wrapper's impression URL, error URL, and linear tracking events to the Inline ads of
     * the VastResponse.
     *
     * @param wrapperMap The map containing the wrapper data.
     * @param adElement  The original Ad element object.
     */
    private static void processWrapper(Map<String, Map> wrapperMap, AdElement
            adElement) {

        // Process the VastAdTagURI to get wrapped VAST response.
        String vastAdTagUri = VmapHelper.getTextValueFromMap(wrapperMap.get
                (VASTADTAGURI_ELEMENT_KEY));
        AdElement wrappedAdElement = createInstance(vastAdTagUri);

        // Add the creative's tracking events from the wrapper to the inline ad.
        Map<String, Map> trackingEventsMap =
                PathHelper.getMapByPath((Map) wrapperMap, TRACKING_EVENTS_PATH);

        // Add the impression, error url, and tracking events to each creative of each inline add.
        for (Map trackingMap : (List<Map>) trackingEventsMap.get(VmapHelper.TRACKING_KEY)) {
            for (Creative creative : wrappedAdElement.getInlineAd().getCreatives()) {
                creative.getVastAd().addTrackingEvent(new Tracking(trackingMap));
            }
        }
        // Add the impression to the inline
        if (wrapperMap.containsKey(VmapHelper.IMPRESSION_KEY)) {
            wrappedAdElement.getInlineAd().getImpressions().addAll(
                    VmapHelper.getStringListFromMap(wrapperMap, VmapHelper.IMPRESSION_KEY));
        }
        // Add the error url to the inline
        if (wrapperMap.containsKey(VmapHelper.ERROR_ELEMENT_KEY)) {
            wrappedAdElement.getInlineAd().getErrorUrls().addAll(
                    VmapHelper.getStringListFromMap(wrapperMap, VmapHelper.ERROR_ELEMENT_KEY));
        }
        adElement.setInlineAd(wrappedAdElement.getInlineAd());
    }

    /**
     * Create an instance given an ad tag URL. Downloads the data from the URL, parses it, and
     * creates the Ad Element object based on the parsed data.
     *
     * Note: This method should be called off the UI thread.
     *
     * @param adTag The URL to get the VAST response from.
     * @return An Ad Element instance.
     */
    public static AdElement createInstance(String adTag) {

        Log.d(TAG, "Processing ad url string for vast model");
        String xmlData;
        try {
            adTag = NetworkUtils.addParameterToUrl(adTag, IAds.CORRELATOR_PARAMETER,
                                                   "" + System.currentTimeMillis());
            xmlData = NetworkUtils.getDataLocatedAtUrl(adTag);
        }
        catch (IOException e) {
            Log.e(TAG, "Could not get data from url " + adTag, e);
            return null;
        }

        XmlParser parser = new XmlParser();
        try {
            Map<String, Map> xmlMap = (Map<String, Map>) parser.parse(xmlData);

            if (xmlMap != null && xmlMap.containsKey(VastResponse.AD_KEY)) {
                Log.d(TAG, "Wrapping VAST response with VMAP");
                return AdElement.createInstance(xmlMap.get(VastResponse.AD_KEY));
            }
        }
        catch (IParser.InvalidDataException e) {
            Log.e(TAG, "Data could not be parsed. ", e);
        }
        Log.e(TAG, "Error creating vast model from ad tag.");
        return null;
    }

    /**
     * Get the selected media file URL.
     *
     * @return The selected media file URL.
     */
    public String getSelectedMediaFileUrl() {

        return mSelectedMediaFileUrl;
    }

    /**
     * Set the selected media file URL. This URL will be used to play the video.
     *
     * @param selectedMediaFileUrl The selected media file URL.
     */
    public void setSelectedMediaFileUrl(String selectedMediaFileUrl) {

        mSelectedMediaFileUrl = selectedMediaFileUrl;
    }

    /**
     * Get the id.
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
     * Get the ad sequence.
     *
     * @return ad sequence.
     */
    public int getAdSequence() {

        return mAdSequence;
    }

    /**
     * Set the sequence.
     *
     * @param adSequence ad sequence.
     */
    public void setAdSequence(int adSequence) {

        mAdSequence = adSequence;
    }

    /**
     * Get the inline ad.
     *
     * @return The inline ad.
     */
    public Inline getInlineAd() {

        return mInlineAd;
    }

    /**
     * Set the inline ad.
     *
     * @param inlineAd The inline ad.
     */
    public void setInlineAd(Inline inlineAd) {

        mInlineAd = inlineAd;
    }

    /**
     * Comparable interface implementation.
     *
     * @param adElement The ad element to compare with current element.
     * @return difference of two ad sequence.
     */
    public int compareTo(AdElement adElement) {

        int adSequence = adElement.getAdSequence();

        //ascending order
        return this.getAdSequence() - adSequence;
    }

    @Override
    public String toString() {

        return "AdElement{" +
                "mId='" + mId + '\'' +
                ", mAdSequence=" + mAdSequence +
                ", mInlineAd=" + mInlineAd +
                ", mSelectedMediaFileUrl='" + mSelectedMediaFileUrl + '\'' +
                '}';
    }
}