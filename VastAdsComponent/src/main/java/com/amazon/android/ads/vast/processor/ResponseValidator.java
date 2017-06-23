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
     * Validate if the VMAP response with the given media picker.
     *
     * @param response    The VMAP ad response.
     * @param mediaPicker The media file picker.
     * @return True if the response is valid, false otherwise.
     */
    public static boolean validate(VmapResponse response, MediaPicker mediaPicker) {

        boolean isValid = false;

        Log.d(TAG, "validating ad response.");

        // We need at least one media file to play for the VMAP response to be valid.
        for (AdBreak adBreak : response.getAdBreaks()) {

            // Must have a MediaPicker to choose one of the MediaFile element from XML
            if (mediaPicker != null) {
                MediaFile mediaFile = mediaPicker.pickVideo(adBreak.getMediaFiles());

                if (mediaFile != null) {
                    String url = mediaFile.getValue();
                    if (!TextUtils.isEmpty(url)) {
                        isValid = true;
                        // Set the best media file to use when playing the ad.
                        adBreak.setSelectedMediaFileUrl(url);
                        Log.d(TAG, "mediaPicker selected mediaFile with URL " + url);
                    }
                }
            }
            else {
                Log.e(TAG, "A MediaPicker is necessary to validate ad response.");
                return false;
            }
        }

        Log.d(TAG, "Validator returns: " + (isValid ? "valid" : "not valid (no media file)"));

        return isValid;
    }

    /**
     * Validate the ad break. To be valid, the ad break must have at least one impression and at
     * least one media file.
     *
     * @param adBreak The ad to validate.
     * @return True if the ad break is valid, false otherwise.
     */
    public static boolean validateAdBreak(AdBreak adBreak) {

        Log.d(TAG, "Validating ad break.");

        // Validate that there is an ad source
        if (adBreak.getAdSource().getAdTagURI() == null &&
                adBreak.getAdSource().getCustomAdData() == null &&
                adBreak.getAdSource().getVastResponse() == null) {
            Log.d(TAG, "Validator error: ad break needs a valid ad source");
            return false;
        }

        // There should be at least one impression.
        List<String> impressions = adBreak.getImpressions();
        if (impressions == null || impressions.size() == 0) {
            Log.d(TAG, "Validator error: impressions list invalid");
            return false;
        }

        // There must be at least one media file.
        List<MediaFile> mediaFiles = adBreak.getMediaFiles();
        if (mediaFiles == null || mediaFiles.size() == 0) {
            Log.d(TAG, "Validator error: mediaFile list invalid");
            return false;
        }

        return true;
    }

}
