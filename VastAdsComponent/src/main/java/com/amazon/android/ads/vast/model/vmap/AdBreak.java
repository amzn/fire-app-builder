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

import com.amazon.android.ads.vast.model.vast.MediaFile;
import com.amazon.dynamicparser.impl.XmlParser;
import com.amazon.utils.DateAndTimeHelper;
import com.amazon.utils.ListUtils;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single ad break. Each ad break may have multiple ads.
 */
public class AdBreak {

    private static final String TAG = AdBreak.class.getSimpleName();

    /**
     * Key to get the extensions element.
     */
    public static final String EXTENSIONS_KEY = "vmap:Extensions";

    /**
     * Key to get the extension element.
     */
    public static final String EXTENSION_KEY = "vmap:Extension";

    /**
     * Key to get the time offset attribute.
     */
    private static final String TIME_OFFSET_ATTR = "timeOffset";

    /**
     * Key to get the break type attribute.
     */
    private static final String BREAK_TYPE_ATTR = "breakType";

    /**
     * Key to get the break id attribute.
     */
    private static final String BREAK_ID_ATTR = "breakId";

    /**
     * Key to get the repeat after attribute.
     */
    private static final String REPEAT_AFTER_ATTR = "repeatAfter";

    /**
     * Key to get the ad source element.
     */
    private static final String AD_SOURCE_KEY = "vmap:AdSource";

    /**
     * Key to get the tracking events element.
     */
    private static final String TRACKING_EVENTS_KEY = "vmap:TrackingEvents";

    /**
     * Key to get the tracking event element.
     */
    private static final String TRACKING_EVENT_KEY = "vmap:Tracking";

    /**
     * Value to represent that the ad should be played at the start of the content.
     */
    public static final String TIME_OFFSET_START = "start";

    /**
     * Value to represent that the ad should be played at the end of the content.
     */
    public static final String TIME_OFFSET_END = "end";

    /**
     * Value to represent a linear break type.
     */
    public static final String BREAK_TYPE_LINEAR = "linear";

    /**
     * hh:mm:ss.mmm, "start", "end", n% (n is an integer from 0-100), #m (m represents sequence
     * and is an integer > 0)
     */
    private String mTimeOffset; // required

    /**
     * Suggested hint to the player.
     */
    private String mBreakType; // required

    /**
     * ID of the ad break.
     */
    private String mBreakId; // optional

    /**
     * Optional indicator that instructs the video player to repeat the same AdBreak and AdSource
     * at time offsets equal to the duration value of this attribute. Expressed in time format
     * HH.MM.SS[.mmm]
     */
    private String mRepeatAfter; // optional

    /**
     * Represents the ad data that will be used to fill the ad break.
     */
    private AdSource mAdSource; // optional

    /**
     * The tracking events.
     */
    private List<Tracking> mTrackingEvents; // optional

    /**
     * Container for Extensions that provide ability to express information not supported by VMAP.
     */
    private List<Extension> mExtensions; // optional

    /**
     * The selected media file. This will be used to display the ad video.
     */
    private String mSelectedMediaFileUrl;

    /**
     * Constructor.
     */
    public AdBreak() {

    }

    /**
     * Constructor.
     *
     * @param adBreakMap A map containing the data required to create an ad break.
     */
    public AdBreak(Map adBreakMap) {

        if (adBreakMap == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributeMap =
                (Map<String, String>) adBreakMap.get(XmlParser.ATTRIBUTES_TAG);

        setTimeOffset(attributeMap.get(TIME_OFFSET_ATTR));
        setBreakType(attributeMap.get(BREAK_TYPE_ATTR));
        setBreakId(attributeMap.get(BREAK_ID_ATTR));
        setRepeatAfter(attributeMap.get(REPEAT_AFTER_ATTR));

        Map<String, Map> adSourceMap = (Map<String, Map>) adBreakMap.get(AD_SOURCE_KEY);
        setAdSource(new AdSource(adSourceMap));

        mExtensions = new ArrayList<>();
        Map<String, Map> extensionsMap = (Map<String, Map>) adBreakMap.get(VmapHelper
                                                                                   .EXTENSIONS_KEY);
        if (extensionsMap != null) {
            mExtensions.addAll(VmapHelper.createExtensions(extensionsMap));
        }

        mTrackingEvents = new ArrayList<>();
        Map<String, Map> trackingEventsMap = (Map<String, Map>) adBreakMap.get(TRACKING_EVENTS_KEY);
        if (trackingEventsMap != null) {
            createTrackingEvents(trackingEventsMap);
        }
    }

    /**
     * Get the time offset.
     *
     * @return The time offset.
     */

    public String getTimeOffset() {

        return mTimeOffset;
    }

    /**
     * Set the time offset.
     *
     * @param timeOffset The time offset.
     */
    public void setTimeOffset(String timeOffset) {

        mTimeOffset = timeOffset;
    }

    /**
     * Get the break type.
     *
     * @return The break type.
     */
    public String getBreakType() {

        return mBreakType;
    }

    /**
     * Set the break type.
     *
     * @param breakType The break type.
     */
    public void setBreakType(String breakType) {

        mBreakType = breakType;
    }

    /**
     * Get the break id.
     *
     * @return The break id.
     */
    public String getBreakId() {

        return mBreakId;
    }

    /**
     * Set the break id.
     *
     * @param breakId The break id.
     */
    public void setBreakId(String breakId) {

        mBreakId = breakId;
    }

    /**
     * Get the repeat after option.
     *
     * @return The repeat after option.
     */
    public String getRepeatAfter() {

        return mRepeatAfter;
    }

    /**
     * Set the repeat after option.
     *
     * @param repeatAfter The repeat after option.
     */
    public void setRepeatAfter(String repeatAfter) {

        mRepeatAfter = repeatAfter;
    }

    /**
     * Get the ad source.
     *
     * @return The ad source.
     */
    public AdSource getAdSource() {

        return mAdSource;
    }

    /**
     * Set the ad source.
     *
     * @param adSource The ad source.
     */
    public void setAdSource(AdSource adSource) {

        mAdSource = adSource;
    }

    /**
     * Get the tracking events.
     *
     * @return The tracking events.
     */
    public List<Tracking> getTrackingEvents() {

        return mTrackingEvents;
    }

    /**
     * Set the tracking events.
     *
     * @param trackingEvents The tracking events.
     */
    public void setTrackingEvents(List<Tracking> trackingEvents) {

        mTrackingEvents = trackingEvents;
    }

    /**
     * Get the extensions.
     *
     * @return The extensions.
     */
    public List<Extension> getExtensions() {

        return mExtensions;
    }

    /**
     * Set the extensions.
     *
     * @param extensions The extensions.
     */
    public void setExtensions(List<Extension> extensions) {

        mExtensions = extensions;
    }

    /**
     * Get the time offset converted to seconds.
     *
     * @return Time offset in seconds.
     */
    public double getConvertedTimeOffset() {

        // TODO: handle other time offsets (DEVTECH-4139)
        return DateAndTimeHelper.convertDateFormatToSeconds(mTimeOffset);
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
     * Get the media files from the ad source.
     *
     * @return List of media files.
     */
    public List<MediaFile> getMediaFiles() {

        List<MediaFile> mediaFiles = new ArrayList<>();
        if (mAdSource != null && mAdSource.getVastResponse() != null) {
            mediaFiles.addAll(mAdSource.getVastResponse().getMediaFiles());
        }

        return mediaFiles;
    }

    /**
     * Get the impressions from the ad source.
     *
     * @return List of the impression strings.
     */
    public List<String> getImpressions() {

        List<String> impressions = new ArrayList<>();
        if (mAdSource != null && mAdSource.getVastResponse() != null) {
            impressions.addAll(mAdSource.getVastResponse().getImpressions());
        }

        return impressions;
    }

    /**
     * Get the tracking URLs from the ad source.
     *
     * @return List of tracking URLs.
     */
    public HashMap<String, List<String>> getTrackingUrls() {

        HashMap<String, List<String>> trackingUrls = new HashMap<>();

        if (getTrackingEvents() != null && getTrackingEvents().size() > 0) {
            Tracking.addTrackingEventsToMap(trackingUrls, getTrackingEvents());
        }
        else if (getAdSource().getVastResponse() != null) {
            trackingUrls.putAll(getAdSource().getVastResponse().getTrackingUrls());
        }

        return trackingUrls;
    }

    /**
     * Get the error URLs from the ad source.
     *
     * @return List of error URLs.
     */
    public List<String> getErrorUrls() {

        return null;
    }

    /**
     * Creates the tracking events for the ad break.
     *
     * @param trackingEventsMap Data map containing the info needed to create the tracking events.
     */
    private void createTrackingEvents(Map<String, Map> trackingEventsMap) {

        List<Map> trackingEventsList = ListUtils.getValueAsMapList
                (trackingEventsMap, TRACKING_EVENT_KEY);

        for (Map trackingMap : trackingEventsList) {
            mTrackingEvents.add(new Tracking(trackingMap));
        }

    }

    @Override
    public String toString() {

        return "AdBreak{" +
                "mTimeOffset='" + mTimeOffset + '\'' +
                ", mBreakType='" + mBreakType + '\'' +
                ", mBreakId='" + mBreakId + '\'' +
                ", mRepeatAfter='" + mRepeatAfter + '\'' +
                ", mAdSource=" + mAdSource +
                ", mTrackingEvents=" + mTrackingEvents +
                ", mExtensions=" + mExtensions +
                ", mSelectedMediaFileUrl='" + mSelectedMediaFileUrl + '\'' +
                '}';
    }
}
