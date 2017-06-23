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

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * A representation of the Companion element of a VAST response. Note: currently only supports
 * tracking events.
 */
public class Companion {

    /**
     * Key to get the tracking events element.
     */
    public static final String TRACKING_EVENTS_KEY = "TrackingEvents";

    /**
     * Key to get the tracking element.
     */
    public static final String TRACKING_KEY = "Tracking";

    /**
     * Key to get the width attribute.
     */
    private static final String WIDTH_KEY = "width";

    /**
     * Key to get the height attribute.
     */
    private static final String HEIGHT_KEY = "height";

    /**
     * List of tracking events.
     */
    private List<Tracking> mTrackingEvents;

    /**
     * The companion id.
     */
    private String mId;

    /**
     * The width.
     */
    private String mWidth;

    /**
     * The height.
     */
    private String mHeight;

    /**
     * Constructor.
     *
     * @param companionMap A map containing the data needed to create a companion.
     */
    public Companion(Map<String, Map> companionMap) {

        mTrackingEvents = new ArrayList<>();
        Map<String, String> attributes = companionMap.get(XmlParser.ATTRIBUTES_TAG);
        if (attributes != null) {
            setId(attributes.get(VmapHelper.ID_KEY));
            setWidth(attributes.get(WIDTH_KEY));
            setHeight(attributes.get(HEIGHT_KEY));
        }

        Map<String, Map> trackingEventsMap = companionMap.get(TRACKING_EVENTS_KEY);
        List<Map> trackingEventsList = ListUtils.getValueAsMapList(trackingEventsMap,
                                                                         TRACKING_KEY);
        for (Map trackingEventMap : trackingEventsList) {
            mTrackingEvents.add(new Tracking(trackingEventMap));
        }
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public String getId() {

        return mId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(String id) {

        mId = id;
    }

    /**
     * Get the width.
     *
     * @return The width.
     */
    public String getWidth() {

        return mWidth;
    }

    /**
     * Set the width.
     *
     * @param width The width.
     */
    public void setWidth(String width) {

        mWidth = width;
    }

    /**
     * Get the height.
     *
     * @return The height.
     */
    public String getHeight() {

        return mHeight;
    }

    /**
     * Set the height.
     *
     * @param height The height.
     */
    public void setHeight(String height) {

        mHeight = height;
    }

    /**
     * Get the list of trackings.
     *
     * @return List of trackings.
     */
    public List<Tracking> getTrackings() {

        return mTrackingEvents;
    }

    /**
     * Set the trackings.
     *
     * @param trackings The list of trackings.
     */
    public void setTrackings(List<Tracking> trackings) {

        mTrackingEvents = trackings;
    }

    @Override
    public String toString() {

        return "Companion{" +
                "mTrackingEvents=" + mTrackingEvents +
                ", mId='" + mId + '\'' +
                ", mWidth='" + mWidth + '\'' +
                ", mHeight='" + mHeight + '\'' +
                '}';
    }
}
