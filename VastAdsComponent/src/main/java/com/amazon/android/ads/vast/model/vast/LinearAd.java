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

import java.util.List;
import java.util.Map;

/**
 * A representation of a Linear element of a VAST response. Note: Omits parsing the VideoClicks
 * element since it doesn't make sense for the TV environment.
 */
public class LinearAd extends VastAd {

    /**
     * Key to get MediaFiles element.
     */
    private static final String MEDIA_FILES_KEY = "MediaFiles";

    /**
     * Key to get MediaFile element.
     */
    private static final String MEDIA_FILE_KEY = "MediaFile";

    /**
     * Key to get TrackingEvents element.
     */
    private static final String TRACKING_EVENTS_KEY = "TrackingEvents";

    /**
     * Key to get Duration element.
     */
    private static final String DURATION_KEY = "Duration";

    /**
     * The ad duration of the linear creative. "HH:MM.SS.mmm" format.
     */
    private String mDuration;

    /**
     * Constructor.
     *
     * @param linearAdMap A map containing the info needed to create the linear ad.
     */
    public LinearAd(Map<String, Map> linearAdMap) {

        super();

        if (linearAdMap != null) {

            Map<String, Map> mediaFilesMap = linearAdMap.get(MEDIA_FILES_KEY);
            List<Map> mediaFilesList = ListUtils.getValueAsMapList(mediaFilesMap, MEDIA_FILE_KEY);
            for (Map<String, Map> mediaFileMap : mediaFilesList) {
                super.addMediaFile(new MediaFile(mediaFileMap));
            }

            Map<String, Map> trackingEventsMap = linearAdMap.get(TRACKING_EVENTS_KEY);
            List<Map> trackingList =
                    ListUtils.getValueAsMapList(trackingEventsMap, VmapHelper.TRACKING_KEY);

            for (Map<String, Map> trackingMap : trackingList) {
                super.addTrackingEvent(new Tracking(trackingMap));
            }

            Map<String, String> durationMap = linearAdMap.get(DURATION_KEY);
            setDuration(durationMap.get(XmlParser.TEXT_TAG));
        }
    }

    /**
     * Get the duration.
     *
     * @return The duration.
     */
    public String getDuration() {

        return mDuration;
    }

    /**
     * Set the duration.
     *
     * @param duration The duration.
     */
    public void setDuration(String duration) {

        mDuration = duration;
    }

    @Override
    public String toString() {

        return "LinearAd{" +
                "mDuration='" + mDuration + '\'' +
                "mMediaFiles=" + super.getMediaFiles() +
                ", mTrackingEvents=" + super.getTrackingEvents() +
                '}';
    }
}
