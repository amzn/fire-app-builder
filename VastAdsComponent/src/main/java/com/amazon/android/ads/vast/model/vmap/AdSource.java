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

import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.dynamicparser.impl.XmlParser;

import android.util.Log;

import java.util.Map;

/**
 * Provides the player with either an inline ad response or reference to an ad response. The ad
 * response should be in the form of one of the following: VAST ad response, custom ad response, or
 * ad tag URI.
 */
public class AdSource {

    private static final String TAG = AdSource.class.getSimpleName();

    /**
     * Key to get the allow multiple ads attribute.
     */
    private static final String ALLOW_MULTIPLE_ADS_KEY = "allowMultipleAds";

    /**
     * Key to get the follow redirects attribute.
     */
    private static final String FOLLOW_REDIRECTS_KEY = "followRedirects";

    /**
     * Key to get the ad tag uri element.
     */
    private static final String AD_TAG_URI_KEY = "vmap:AdTagURI";

    /**
     * Key to get the custom data element.
     */
    private static final String CUSTOM_AD_DATA_KEY = "vmap:CustomAdData";

    /**
     * Key to get the VAST ad data element.
     */
    private static final String VAST_AD_DATA_KEY = "vmap:VASTAdData";

    /**
     * The ad source id.
     */
    private String mId; // optional

    /**
     * Indicates whether the player should select and play only a single ad from the ad response
     * document, or play multiple ads. If not specified, left to the video player's discretion.
     * Non-VAST ad pods may be ignored.
     */
    private boolean mAllowMultipleAds = true; // optional

    /**
     * Whether the player should follow wrappers/redirects in the ad response document. If not
     * specified, left to the video player’s discretion
     */
    private boolean mFollowRedirects; // optional

    /**
     * A VAST 3.0 document that comprises the ad response document. Not contained within a CDATA.
     */
    private VastResponse mVastResponse;

    /**
     * An ad response document that is not VAST 3.0.
     */
    private CustomAdData mCustomAdData;

    /**
     * A URL to a secondary ad server that will provide the ad response document.
     */
    private AdTagURI mAdTagURI;

    /**
     * Constructor.
     */
    public AdSource() {

    }

    /**
     * Constructor.
     *
     * @param adSourceMap A map containing the data needed to create the ad source object.
     */
    public AdSource(Map<String, Map> adSourceMap) {

        if (adSourceMap == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributeMap =
                (Map<String, String>) adSourceMap.get(XmlParser.ATTRIBUTES_TAG);

        setId(attributeMap.get(VmapHelper.ID_KEY));
        setAllowMultipleAds(Boolean.valueOf(attributeMap.get(ALLOW_MULTIPLE_ADS_KEY)));
        setFollowRedirects(Boolean.valueOf(attributeMap.get(FOLLOW_REDIRECTS_KEY)));

        if (adSourceMap.get(AD_TAG_URI_KEY) != null) {
            Map<String, Map> adTagUriMap = (Map<String, Map>) adSourceMap.get(AD_TAG_URI_KEY);
            // Check that the ad tag URI is for a VAST template. If so, parse the VAST ad tag URI.
            if (isAdTagUriVast(adTagUriMap)) {
                String adTag = VmapHelper.getTextValueFromMap(adSourceMap.get(AD_TAG_URI_KEY));
                setVastResponse(VastResponse.createInstance(adTag));
            }
            else {
                setAdTagURI(new AdTagURI(adSourceMap.get(AD_TAG_URI_KEY)));
            }
        }
        else if (adSourceMap.get(CUSTOM_AD_DATA_KEY) != null) {
            setCustomAdData(new CustomAdData(adSourceMap.get(CUSTOM_AD_DATA_KEY)));
        }
        else if (adSourceMap.get(VAST_AD_DATA_KEY) != null) {
            Map<String, Map> vastAdDataMap = adSourceMap.get(VAST_AD_DATA_KEY);
            if (vastAdDataMap.containsKey(VmapHelper.VAST_KEY)) {
                setVastResponse(VastResponse.createInstance(vastAdDataMap.get(VmapHelper
                                                                                      .VAST_KEY)));
            }
        }
    }

    /**
     * Checks if the ad tag URI is for a VAST response.
     *
     * @param adTagUriMap Map containing data for ad tag URI.
     * @return True if the ad tag URI is for VAST, false otherwise.
     */
    private boolean isAdTagUriVast(Map<String, Map> adTagUriMap) {

        Map<String, String> attributes = adTagUriMap.get(XmlParser.ATTRIBUTES_TAG);
        return !(attributes == null || attributes.get(VmapHelper.TEMPLATE_TYPE_KEY) == null) &&
                isVastTemplate(attributes.get(VmapHelper.TEMPLATE_TYPE_KEY));
    }

    /**
     * Test whether or not the template is for an accepted VAST response.
     *
     * @param template The template to test.
     * @return True if the template matches an accepted VAST response; false otherwise.
     */
    private boolean isVastTemplate(String template) {

        return template.equals(VastResponse.VAST_3) || template.equals(VastResponse.VAST_2)
                || template.equals(VastResponse.VAST_1) || template.equals(VastResponse.VAST);
    }

    /**
     * Get the ad source id.
     *
     * @return The ad source id.
     */
    public String getId() {

        return mId;
    }

    /**
     * Set the add source id.
     *
     * @param id The ad source id.
     */
    public void setId(String id) {

        mId = id;
    }

    /**
     * Get the allow multiple ads value.
     *
     * @return True if allowing multiple ads has been specified.
     */
    public boolean isAllowMultipleAds() {

        return mAllowMultipleAds;
    }

    /**
     * Set the allow multiple ads value.
     *
     * @param allowMultipleAds The allow multiple ads value.
     */
    public void setAllowMultipleAds(boolean allowMultipleAds) {

        mAllowMultipleAds = allowMultipleAds;
    }

    /**
     * Get the follow redirects value.
     *
     * @return True if the follow redirects value has been specified.
     */
    public boolean isFollowRedirects() {

        return mFollowRedirects;
    }

    /**
     * Set the follow redirects value.
     *
     * @param followRedirects The follow redirects value.
     */
    public void setFollowRedirects(boolean followRedirects) {

        mFollowRedirects = followRedirects;
    }

    /**
     * Get the VAST response that contains the ad response document.
     *
     * @return The VAST response.
     */
    public VastResponse getVastResponse() {

        return mVastResponse;
    }

    /**
     * Set the VAST response.
     *
     * @param vastResponse The VAST response.
     */
    public void setVastResponse(VastResponse vastResponse) {

        mVastResponse = vastResponse;
    }

    /**
     * Get the custom ad data.
     *
     * @return The custom ad data.
     */
    public CustomAdData getCustomAdData() {

        return mCustomAdData;
    }

    /**
     * Set the custom ad data.
     *
     * @param customAdData The custom ad data.
     */
    public void setCustomAdData(CustomAdData customAdData) {

        mCustomAdData = customAdData;
    }

    /**
     * Get the ad tag URI.
     *
     * @return The ad tag URI.
     */
    public AdTagURI getAdTagURI() {

        return mAdTagURI;
    }

    /**
     * Set the ad tag URI.
     *
     * @param adTagURI The ad tag URI.
     */
    public void setAdTagURI(AdTagURI adTagURI) {

        mAdTagURI = adTagURI;
    }

    @Override
    public String toString() {

        return "AdSource{" +
                "mId='" + mId + '\'' +
                ", mAllowMultipleAds=" + mAllowMultipleAds +
                ", mFollowRedirects=" + mFollowRedirects +
                ", mVastResponse=" + mVastResponse +
                ", mCustomAdData=" + mCustomAdData +
                ", mAdTagURI=" + mAdTagURI +
                '}';
    }
}
