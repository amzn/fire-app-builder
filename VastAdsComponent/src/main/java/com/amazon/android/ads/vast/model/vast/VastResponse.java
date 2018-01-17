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
     * Key to get the ad element.
     */
    public static final String AD_KEY = "Ad";

    /**
     * Error URL at vast root element.
     */
    private String mErrorUrl;

    /**
     * The official version with which the response is complaint.
     */
    private String mVersion;

    /**
     * List of Ad elements.
     */
    private List<AdElement> mAdElements;

    /**
     * Constructor.
     */
    public VastResponse() {

        mAdElements = new ArrayList<>();
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
            vastResponse.setVersion(attributes.get(VmapHelper.VERSION_KEY));
            //Check for Vast root error element
            if (xmlMap.containsKey(VmapHelper.ERROR_ELEMENT_KEY)) {
                Map errorMap = xmlMap.get(VmapHelper.ERROR_ELEMENT_KEY);
                if (errorMap != null) {
                    vastResponse.setErrorUrl(VmapHelper.getTextValueFromMap(errorMap));
                }
                Log.d(TAG, "Root Error element found in vast response, Server does not / can not " +
                        "return an ad");
                return vastResponse;
            }
            Object adElementObject = xmlMap.get(AD_KEY);
            if (adElementObject == null) {
                Log.d(TAG, "No ad found in vast response");
                return vastResponse;
            }

            if (adElementObject instanceof List) {
                for (Map<String, Map> map : (List<Map<String, Map>>) adElementObject) {
                    vastResponse.getAdElements().add(AdElement.createInstance(map));
                }
            }
            else {
                vastResponse.getAdElements().add(AdElement.createInstance((Map<String, Map>)
                                                                                  adElementObject));
            }
        }
        return vastResponse;
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
     * Get the Vast Ad element list.
     *
     * @return The vast Ad element list.
     */
    public List<AdElement> getAdElements() {

        return mAdElements;
    }

    /**
     * Set the Vast ad elements.
     *
     * @param adElements The Vast ad elements.
     */
    public void setAdElements(List<AdElement> adElements) {

        mAdElements = adElements;
    }

    /**
     * Get the error URL.
     *
     * @return error URL.
     */
    public String getErrorUrl() {

        return mErrorUrl;
    }

    /**
     * Set the error URL.
     *
     * @param errorUrl error URL.
     */
    public void setErrorUrl(String errorUrl) {

        mErrorUrl = errorUrl;
    }

    @Override
    public String toString() {

        return "VastResponse{" +
                "mErrorUrl='" + mErrorUrl + '\'' +
                ", mVersion='" + mVersion + '\'' +
                ", mAdElements=" + mAdElements +
                '}';
    }
}