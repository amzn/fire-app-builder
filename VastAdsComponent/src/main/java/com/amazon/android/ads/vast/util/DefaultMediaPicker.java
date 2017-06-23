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
package com.amazon.android.ads.vast.util;

import com.amazon.android.ads.vast.model.vast.MediaFile;
import com.amazon.android.ads.vast.processor.MediaPicker;

import android.content.Context;
import android.text.TextUtils;
import android.util.DisplayMetrics;
import android.util.Log;

import java.math.BigInteger;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

public class DefaultMediaPicker implements MediaPicker {

    private static final String TAG = "DefaultMediaPicker";
    private static final int maxPixels = 5000;

    // These are the Android supported MIME types, see http://developer.android
    // .com/guide/appendix/media-formats.html#core (as of API 18)
    String SUPPORTED_VIDEO_TYPE_REGEX = "video/.*(?i)(mp4|3gpp|mp2t|webm|matroska)";

    private int deviceWidth;
    private int deviceHeight;
    private int deviceArea;
    private Context context;

    public DefaultMediaPicker(Context context) {

        this.context = context;
        setDeviceWidthHeight();
    }

    public DefaultMediaPicker(int width, int height) {

        setDeviceWidthHeight(width, height);
    }


    @Override
    // given a list of MediaFiles, select the most appropriate one.
    public MediaFile pickVideo(List<MediaFile> mediaFiles) {
        //make sure that the list of media files contains the correct attributes
        if (mediaFiles == null || prefilterMediaFiles(mediaFiles) == 0) {
            return null;
        }
        Collections.sort(mediaFiles, new AreaComparator());
        return getBestMatch(mediaFiles);
    }

    /*
     * This method filters the list of mediafiles and return the count.
     * Validate that the media file objects contain the required attributes for the Default Media
     * Picker processing.
     *
     * 		Required attributes:
     * 			1. type
     * 			2. height
     * 			3. width
     * 			4. url
     */
    private int prefilterMediaFiles(List<MediaFile> mediaFiles) {

        Iterator<MediaFile> iter = mediaFiles.iterator();

        while (iter.hasNext()) {

            MediaFile mediaFile = iter.next();

            // type attribute
            String type = mediaFile.getType();
            if (TextUtils.isEmpty(type)) {
                Log.d(TAG, "Validator error: mediaFile type empty");
                iter.remove();
                continue;
            }

            // Height attribute
            BigInteger height = mediaFile.getHeight();

            if (null == height) {
                Log.d(TAG, "Validator error: mediaFile height null");
                iter.remove();
                continue;
            }
            int videoHeight = height.intValue();
            if (!(0 < videoHeight && videoHeight < maxPixels)) {
                Log.d(TAG, "Validator error: mediaFile height invalid: " + videoHeight);
                iter.remove();
                continue;
            }

            // width attribute
            BigInteger width = mediaFile.getWidth();
            if (null == width) {
                Log.d(TAG, "Validator error: mediaFile width null");
                iter.remove();
                continue;
            }
            int videoWidth = width.intValue();
            if (!(0 < videoWidth && videoWidth < maxPixels)) {
                Log.d(TAG, "Validator error: mediaFile width invalid: " + videoWidth);
                iter.remove();
                continue;
            }

            // mediaFile url
            String url = mediaFile.getValue();
            if (TextUtils.isEmpty(url)) {
                Log.d(TAG, "Validator error: mediaFile url empty");
                iter.remove();
            }
        }

        return mediaFiles.size();
    }


    private void setDeviceWidthHeight() {

        // get the device width and height of the device using the context
        DisplayMetrics metrics = context.getResources().getDisplayMetrics();
        deviceWidth = metrics.widthPixels;
        deviceHeight = metrics.heightPixels;
        deviceArea = deviceWidth * deviceHeight;
    }

    private void setDeviceWidthHeight(int width, int height) {

        this.deviceWidth = width;
        this.deviceHeight = height;
        deviceArea = deviceWidth * deviceHeight;

    }

    private class AreaComparator implements Comparator<MediaFile> {

        @Override
        public int compare(MediaFile obj1, MediaFile obj2) {
            // get area of the video of the two MediaFiles
            int obj1Area = obj1.getWidth().intValue() * obj1.getHeight().intValue();
            int obj2Area = obj2.getWidth().intValue() * obj2.getHeight().intValue();

            // get the difference between the area of the MediaFile and the area of the screen
            int obj1Diff = Math.abs(obj1Area - deviceArea);
            int obj2Diff = Math.abs(obj2Area - deviceArea);
            Log.v(TAG, "AreaComparator: obj1:" + obj1Diff + " obj2:" + obj2Diff);

            // choose the MediaFile which has the lower difference in area
            if (obj1Diff < obj2Diff) {
                return -1;
            }
            else if (obj1Diff > obj2Diff) {
                return 1;
            }
            else {
                return 0;
            }
        }

    }

    private boolean isMediaFileCompatible(MediaFile media) {

        // check if the MediaFile is compatible with the device.
        // further checks can be added here
        return media.getType().matches(SUPPORTED_VIDEO_TYPE_REGEX);
    }

    private MediaFile getBestMatch(List<MediaFile> list) {

        Log.d(TAG, "getBestMatch");

        for (MediaFile media : list) {
            if (isMediaFileCompatible(media)) {
                return media;
            }
        }
        return null;
    }
}
