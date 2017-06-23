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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class represents a VAST response.
 */
public class VastResponse {

    private static final String TAG = VastResponse.class.getSimpleName();

    /**
     * Value for VAST version 3.
     */
    public static final String VAST_3 = "vast3";

    /**
     * Value for VAST version 2.
     */
    public static final String VAST_2 = "vast2";

    /**
     * Value for VAST version 1.
     */
    public static final String VAST_1 = "vast1";

    /**
     * Another value for VAST version 1.
     */
    public static final String VAST = "vast";

    /**
     * Key to get the version attribute.
     */
    private static final String VERSION_KEY = "version";

    /**
     * Key to get the ad element.
     */
    private static final String AD_KEY = "Ad";

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
     * The official version with which the response is complaint.
     */
    private String mVersion;

    /**
     * The Inline element.
     */
    private List<Inline> mInlineAds;

    /**
     * Constructor.
     */
    public VastResponse() {

        mInlineAds = new ArrayList<>();
    }

    /**
     * Creates a instance of a VAST response given the parsed xml data.
     *
     * @param xmlMap A map representing the parsed xml response.
     * @return An VAST response instance.
     */
    public static VastResponse createInstance(Map<String, Map> xmlMap) {

        VastResponse vastResponse = new VastResponse();
        Log.d(TAG, "Creating VAST response from xml map");

        Map<String, String> attributes = xmlMap.get(XmlParser.ATTRIBUTES_TAG);

        if (attributes != null) {
            vastResponse.setVersion(attributes.get(VERSION_KEY));
            Object adElementObject = xmlMap.get(AD_KEY);
            if (adElementObject == null) {
                Log.d(TAG, "No ad found in vast response");
                return vastResponse;
            }

            if (adElementObject instanceof List) {
                for (Map<String, Map> map : (List<Map<String, Map>>) adElementObject) {
                    processAdElement(map, vastResponse);
                }
            }
            else {
                processAdElement((Map<String, Map>) adElementObject, vastResponse);
            }
        }
        return vastResponse;
    }

    /**
     * Helper method to process ad element. It will either process the wrapper withing the ad
     * element or create inlines (whichever is available in the map).
     *
     * @param map          The map containing the ad element data to process.
     * @param vastResponse The original VAST response object.
     */
    private static void processAdElement(Map<String, Map> map, VastResponse vastResponse) {

        if (map.containsKey(WRAPPER_ELEMENT_KEY)) {
            processWrapper(map.get(WRAPPER_ELEMENT_KEY), vastResponse);
        }
        else {
            vastResponse.getInlineAds().add(new Inline(map));
        }
    }

    /**
     * Process the wrapper element. Creates a VastResponse from the VASTAdTagURI element and then
     * ads the wrapper's impression URL, error URL, and linear tracking events to the Inline ads of
     * the VastResponse.
     *
     * @param wrapperMap   The map containing the wrapper data.
     * @param vastResponse The original VAST response object.
     */
    private static void processWrapper(Map<String, Map> wrapperMap, VastResponse
            vastResponse) {

        // Process the VastAdTagURI to get wrapped VAST response.
        String vastAdTagUri = VmapHelper.getTextValueFromMap(wrapperMap.get
                (VASTADTAGURI_ELEMENT_KEY));
        VastResponse wrappedVastResponse = createInstance(vastAdTagUri);

        // Add the creative's tracking events from the wrapper to the inline ad.
        Map<String, Map> trackingEventsMap =
                PathHelper.getMapByPath((Map) wrapperMap, TRACKING_EVENTS_PATH);

        // Add the impression, error url, and tracking events to each creative of each inline add.
        for (Inline inline : wrappedVastResponse.getInlineAds()) {
            for (Map trackingMap : (List<Map>) trackingEventsMap.get(VmapHelper.TRACKING_KEY)) {
                for (Creative creative : inline.getCreatives()) {
                    creative.getVastAd().addTrackingEvent(new Tracking(trackingMap));
                }
            }
            // Add the impression to the inline
            if (wrapperMap.containsKey(VmapHelper.IMPRESSION_KEY)) {
                inline.getImpressions().addAll(
                        VmapHelper.getStringListFromMap(wrapperMap, VmapHelper.IMPRESSION_KEY));
            }
            // Add the error url to the inline
            if (wrapperMap.containsKey(VmapHelper.ERROR_ELEMENT_KEY)) {
                inline.getErrorUrls().addAll(
                        VmapHelper.getStringListFromMap(wrapperMap, VmapHelper.ERROR_ELEMENT_KEY));
            }

            // Add the inline ad to the original vast response object.
            vastResponse.getInlineAds().add(inline);
        }
    }

    /**
     * Create an instance given an ad tag URL. Downloads the data from the URL, parses it, and
     * creates the VAST response object based on the parsed data.
     *
     * Note: This method should be called off the UI thread.
     *
     * @param adTag The URL to get the VAST response from.
     * @return A VAST response instance.
     */
    public static VastResponse createInstance(String adTag) {

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

            if (xmlMap != null) {
                Log.d(TAG, "Wrapping VAST response with VMAP");
                return VastResponse.createInstance(xmlMap);
            }
        }
        catch (IParser.InvalidDataException e) {
            Log.e(TAG, "Data could not be parsed. ", e);
        }
        Log.e(TAG, "Error creating vast model from ad tag.");
        return null;
    }

    /**
     * Get the version.
     *
     * @return The version.
     */
    public String getVersion() {

        return mVersion;
    }

    /**
     * Set the version.
     *
     * @param version The version.
     */
    public void setVersion(String version) {

        mVersion = version;
    }

    /**
     * Get the inline ads.
     *
     * @return The inline ads.
     */
    public List<Inline> getInlineAds() {

        return mInlineAds;
    }

    /**
     * Set the inline ads.
     *
     * @param inlineAds The inline ads.
     */
    public void setInlineAds(List<Inline> inlineAds) {

        mInlineAds = inlineAds;
    }

    /**
     * Gets all the media files from the inline.
     *
     * @return List of media files.
     */
    public List<MediaFile> getMediaFiles() {

        ArrayList<MediaFile> mediaFiles = new ArrayList<>();
        for (Inline inline : mInlineAds) {

            mediaFiles.addAll(inline.getMediaFiles());
        }
        return mediaFiles;
    }

    /**
     * Gets all the impressions from the inline.
     *
     * @return List of impression strings.
     */
    public List<String> getImpressions() {

        List<String> impressions = new ArrayList<>();
        for (Inline inline : mInlineAds) {

            impressions.addAll(inline.getImpressions());
        }
        return impressions;
    }

    /**
     * Gets all the tracking URLs from the inline.
     *
     * @return Map of tracking events and their corresponding URLs.
     */
    public Map<String, List<String>> getTrackingUrls() {

        Map<String, List<String>> sortedMap = new HashMap<>();
        for (Inline inline : mInlineAds) {
            HashMap<String, List<String>> map = inline.getTrackingEvents();
            for (String key : map.keySet()) {
                if (sortedMap.containsKey(key)) {
                    sortedMap.get(key).addAll(map.get(key));
                }
                else {
                    sortedMap.put(key, map.get(key));
                }
            }
        }
        return sortedMap;
    }

    /**
     * Get the error URLs from the inline.
     *
     * @return List of error URLs.
     */
    public List<String> getErrorUrls() {

        List<String> errorUrls = new ArrayList<>();
        for (Inline inline : mInlineAds) {
            errorUrls.addAll(inline.getErrorUrls());
        }
        return errorUrls;
    }

    @Override
    public String toString() {

        return "VastResponse{" +
                "mVersion='" + mVersion + '\'' +
                ", mInlineAds=" + mInlineAds +
                '}';
    }
}
