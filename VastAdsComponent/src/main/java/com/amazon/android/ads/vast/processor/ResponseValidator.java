/**
 * This file was modified by Amazon:
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
/*
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast.processor;

import com.amazon.android.ads.vast.model.vast.AdElement;
import com.amazon.android.ads.vast.model.vast.MediaFile;
import com.amazon.android.ads.vast.model.vmap.AdBreak;
import com.amazon.android.ads.vast.model.vmap.VmapResponse;

import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * Validates a VMAP response.
 */
public class ResponseValidator {

    private static final String TAG = ResponseValidator.class.getSimpleName();

    /**
     * Validate if the VMAP response follows the specification.
     *
     * @param response The VMAP ad response.
     * @return True if the response is valid, false otherwise.
     */
    public static boolean validateVMAPResponse(VmapResponse response) {

        Log.d(TAG, "Validating vmap response.");

        if (response != null && response.getVmapVersion() == null) {
            Log.e(TAG, "Validator error: vmap response need the required attributes");
            return false;
        }

        return true;
    }

    /**
     * Validate the ad break.
     *
     * @param adBreak The ad to validate.
     * @return True if the ad break is valid, false otherwise.
     */
    public static boolean validateAdBreak(AdBreak adBreak) {

        Log.d(TAG, "Validating ad break.");

        //time offset and breakType are required field for an ad break
        if (adBreak.getTimeOffset() == null || adBreak.getBreakType() == null) {
            Log.e(TAG, "Validator error: ad break need the required attributes");
            return false;
        }

        //If ad source element is provided, it must define the source for ads with one of the
        //following elements <VastAdData> / <AdTagURI> / <CustomAdData>
        if (adBreak.getAdSource() != null) {
            if (adBreak.getAdSource().getAdTagURI() == null &&
                    adBreak.getAdSource().getCustomAdData() == null &&
                    adBreak.getAdSource().getVastResponse() == null) {
                Log.e(TAG, "Validator error: ad break needs a valid ad source");
                return false;
            }
        }

        return true;
    }

    /**
     * Validate the ad element. To be valid, the ad element must have at least one impression and at
     * least one media file supported by the media player.
     *
     * @param adElement   The ad element to validate.
     * @param mediaPicker The media file picker.
     * @return True if the ad break is valid, false otherwise.
     */
    public static boolean validateAdElement(AdElement adElement, MediaPicker mediaPicker) {

        Log.d(TAG, "Validating ad element.");

        if (adElement == null || adElement.getInlineAd() == null) {
            Log.e(TAG, "Validator error: no inline Ad found");
            return false;
        }

        // There should be at least one impression.
        List<String> impressions = adElement.getInlineAd().getImpressions();
        if (impressions == null || impressions.size() == 0) {
            Log.e(TAG, "Validator error: impressions list invalid");
            return false;
        }

        // There must be at least one media file.
        List<MediaFile> mediaFiles = adElement.getInlineAd().getMediaFiles();
        if (mediaFiles == null || mediaFiles.size() == 0) {
            Log.e(TAG, "Validator error: mediaFile list invalid");
            return false;
        }

        boolean isValid = false;

        // Must have a MediaPicker to choose one of the MediaFile element from XML
        if (mediaPicker != null) {
            MediaFile mediaFile = mediaPicker.pickVideo(adElement.getInlineAd().getMediaFiles());

            if (mediaFile != null) {
                String url = mediaFile.getValue();
                if (!TextUtils.isEmpty(url)) {
                    // Set the best media file to use when playing the ad.
                    adElement.setSelectedMediaFileUrl(url);
                    Log.d(TAG, "mediaPicker selected mediaFile with URL " + url);
                    isValid = true;
                }
            }
        }
        else {
            Log.e(TAG, "A MediaPicker is necessary to validate ad response.");
            return false;
        }

        return isValid;
    }
}
