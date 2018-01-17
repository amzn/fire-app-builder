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
package com.amazon.android.ads.vast.processor;

import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.android.ads.vast.model.vmap.AdBreak;
import com.amazon.android.ads.vast.model.vmap.VmapResponse;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.dynamicparser.IParser;
import com.amazon.dynamicparser.impl.XmlParser;

import android.util.Log;

import java.io.IOException;
import java.util.List;
import java.util.Map;

/**
 * This class contains the logic to process a VMAP ad tag. It will read the data from the URL and
 * attempt to parse it into the {@link VmapResponse} class. If the tag does not follow the proper
 * VMAP schema, an error is returned. See the following for documentation:
 * https://www.iab.com/wp-content/uploads/2015/06/VMAP.pdf
 */
public class AdTagProcessor {

    private static final String TAG = AdTagProcessor.class.getSimpleName();

    /**
     * Constant used to test for VMAP response.
     */
    private static final String XMLNS_VMAP_KEY = "xmlns:vmap";

    /**
     * The type of the ad. Currently only VMAP, or errors.
     */
    public enum AdTagType {
        vmap,
        vast,
        error,
        error_model_creation,
        validation_error,
        no_ad_break_found
    }

    /**
     * The VMAP response instance.
     */
    private VmapResponse mVmapResponse;

    /**
     * The media file picker.
     */
    private MediaPicker mMediaPicker;

    /**
     * Constructor.
     *
     * @param mediaPicker The media file picker. Used to validate the VMAP response after
     *                    processing the ad tag URL.
     */
    public AdTagProcessor(MediaPicker mediaPicker) {

        mMediaPicker = mediaPicker;
    }

    /**
     * Processes the data from a VMAP ad URL into a {@link VmapResponse} object.
     *
     * @param urlString The URL to process.
     * @return The {@link AdTagType#vmap} value if a model was successfully created, otherwise an
     * error value.
     */
    public AdTagType process(String urlString) {

        Log.d(TAG, "Processing ad url string");
        String xmlData;
        AdTagType type;
        try {
            xmlData = NetworkUtils.getDataLocatedAtUrl(urlString);
        }
        catch (IOException e) {
            Log.e(TAG, "Could not get data from url " + urlString, e);
            return AdTagType.error;
        }

        if (xmlData != null) {
            XmlParser parser = new XmlParser();
            try {
                Map<String, Map> xmlMap = (Map<String, Map>) parser.parse(xmlData);

                if (xmlMap != null) {
                    Map attributes = xmlMap.get(XmlParser.ATTRIBUTES_TAG);

                    try {
                        if (attributes != null && attributes.containsKey(XMLNS_VMAP_KEY)) {
                            mVmapResponse = VmapResponse.createInstance(xmlMap);
                            //Validate the vmap response
                            if (!ResponseValidator.validateVMAPResponse(mVmapResponse)) {
                                return AdTagType.validation_error;
                            }
                            //VMAP specification supports no Ad break so we will not fire error
                            // urls if no ad break found
                            if (mVmapResponse == null) {
                                return AdTagType.no_ad_break_found;
                            }
                            type = AdTagType.vmap;
                        }
                        else {
                            Log.d(TAG, "Converting VAST response into VMAP response");
                            VastResponse vastResponse = VastResponse.createInstance(xmlMap);
                            mVmapResponse = VmapResponse.createInstanceWithVast(vastResponse);
                            type = AdTagType.vast;
                        }
                        return type;
                    }
                    catch (IllegalArgumentException e) {
                        Log.e(TAG, "Caught IllegalArgumentException.", e);
                        return AdTagType.error_model_creation;
                    }
                }
            }
            catch (IParser.InvalidDataException e) {
                Log.e(TAG, "Data could not be parsed. ", e);
            }
        }
        return AdTagType.error;
    }

    /**
     * Get the ad response.
     *
     * @return The VMAP ad response.
     */
    public VmapResponse getAdResponse() {

        return mVmapResponse;
    }

}
