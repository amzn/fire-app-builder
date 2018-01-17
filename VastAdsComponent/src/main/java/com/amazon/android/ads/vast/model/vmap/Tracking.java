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

import com.amazon.dynamicparser.impl.XmlParser;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Part of the <TrackingEvents> element. Provides a tracking URI that is used to provide tracking
 * URIs for the three tracking events available in VMAP.
 */
public class Tracking {

    private static final String TAG = Tracking.class.getSimpleName();

    /**
     * This event indicates that an individual creative portion of the ad was viewed.
     */
    public static final String CREATE_VIEW_TYPE = "createView";

    /**
     * This event is used to indicate that an individual creative within the ad was loaded and
     * playback began.
     */
    public static final String START_TYPE = "start";

    /**
     * The creative played for at least 50% of the total duration.
     */
    public static final String MIDPOINT_TYPE = "midpoint";

    /**
     * The creative played for at least 25% of the total duration.
     */
    public static final String FIRST_QUARTILE_TYPE = "firstQuartile";

    /**
     * The creative played for at least 75% of the total duration
     */
    public static final String THIRD_QUARTILE_TYPE = "thirdQuartile";

    /**
     * The creative was played to the end at a normal speed.
     */
    public static final String COMPLETE_TYPE = "complete";

    /**
     * The user activated the mute control and muted the creative.
     */
    public static final String MUTE_TYPE = "mute";

    /**
     * The user activated the mute control and unmuted the creative.
     */
    public static final String UNMUTE_TYPE = "unmute";

    /**
     * The user clicked teh pause control and stopped the creative.
     */
    public static final String PAUSE_TYPE = "pause";

    /**
     * The user activated the rewind control to access a previous point in the creative timeline.
     */
    public static final String REWIND_TYPE = "rewind";

    /**
     * The user activated a control to extend the video player to the edges of the viewer's screen.
     */
    public static final String RESUME_TYPE = "resume";

    /**
     * The user activated the control to extend the video player to the edges of the viewer's
     * screen.
     */
    public static final String FULL_SCREEN_TYPE = "fullscreen";

    /**
     * The user activated the control to reduce video player size to original dimensions.
     */
    public static final String EXIT_FULL_SCREEN_TYPE = "exitFullScreen";

    /**
     * The user activated a control to expand the creative.
     */
    public static final String EXPAND_TYPE = "expand";

    /**
     * The user activated a control to reduce the creative to its original dimensions.
     */
    public static final String COLLAPSE_TYPE = "collapse";

    /**
     * The user activated a control that launched an additional portion of the creative.
     */
    public static final String ACCEPT_INVITATION_TYPE = "acceptInvitationLinear";

    /**
     * The user clicked the close button on the creative.
     */
    public static final String CLOSE_TYPE = "closeLinear";

    /**
     * The user activated a skip control to skip the creative.
     */
    public static final String SKIP_TYPE = "skip";

    /**
     * The creative played for a duration at normal speed that is equal or greater than the value
     * provided in an additional attribute for offset.
     */
    public static final String PROGRESS_TYPE = "progress";

    /**
     * VMAP tracking event to notify the ad break start.
     */
    public static final String BREAK_START_TYPE = "breakStart";

    /**
     * VMAP tracking event to notify the ad break end.
     */
    public static final String BREAK_END_TYPE = "breakEnd";

    /**
     * VMAP tracking event to notify the error occurred.
     */
    public static final String ERROR_TYPE = "error";

    /**
     * Key to find the event attribute from the map.
     */
    private static final String EVENT_KEY = "event";

    /**
     * URI to track for specific event type.
     */
    private String mUri;

    /**
     * The name of the VMAP ad break level event to track.
     */
    private String mEvent;

    /**
     * Constructor.
     *
     * @param map A data map that contains the URI data and an attributes map.
     */
    public Tracking(Map<String, Map> map) {

        if (map == null) {
            Log.e(TAG, "Data map for constructing ad source cannot be null");
            throw new IllegalArgumentException("Data map parameter cannot be null");
        }

        Map<String, String> attributes = map.get(XmlParser.ATTRIBUTES_TAG);
        if (attributes != null) {
            setEvent(attributes.get(EVENT_KEY));
        }

        setUri(VmapHelper.getTextValueFromMap(map));
    }

    /**
     * Get the URI.
     *
     * @return The URI.
     */
    public String getUri() {

        return mUri;
    }

    /**
     * Set the URI.
     *
     * @param uri The URI.
     */
    public void setUri(String uri) {

        mUri = uri;
    }

    /**
     * Get the event type.
     *
     * @return The event type.
     */
    public String getEvent() {

        return mEvent;
    }

    /**
     * Set the event type.
     *
     * @param event The event type.
     */
    public void setEvent(String event) {

        mEvent = event;
    }

    /**
     * Helper method that sorts tracking events into a map by event type.
     *
     * @param trackingEventMap The map to put the sorted tracking events into.
     * @param trackingList     The list of tracking events to sort.
     * @return The sorted map.
     */
    public static HashMap<String, List<String>> addTrackingEventsToMap(
            HashMap<String, List<String>> trackingEventMap, List<Tracking> trackingList) {

        for (Tracking event : trackingList) {
            if (trackingEventMap.containsKey(event.getEvent())) {
                trackingEventMap.get(event.getEvent()).add(event.getUri());
            }
            else {
                List<String> uris = new ArrayList<>();
                uris.add(event.getUri());
                trackingEventMap.put(event.getEvent(), uris);
            }
        }
        return trackingEventMap;
    }


    @Override
    public String toString() {

        return "Tracking{" +
                "mUri='" + mUri + '\'' +
                ", mEvent='" + mEvent + '\'' +
                '}';
    }
}
