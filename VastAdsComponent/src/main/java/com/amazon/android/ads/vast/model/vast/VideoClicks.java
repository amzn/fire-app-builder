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

import java.util.HashMap;
import java.util.Map;

/**
 * A representation of Video Click elements available in linear ad.
 */
public class VideoClicks {

    private static final String TAG = ClickElement.class.getSimpleName();

    /**
     * This element contains a uri which should be displayed in a web browser window.
     */
    public static final String CLICK_THROUGH_KEY = "ClickThrough";

    /**
     * This element contains a uri which should be requested if user click while the Linear ad is played.
     */
    public static final String CLICK_TRACKING_KEY = "ClickTracking";

    /**
     * This element contains a uri which should be requested when user clicks on a particular
     * button, link or another call to action associated within the linear ad.
     * This does not open a new web browser page and should not be requested along with ClickThrough
     * for the same click.
     */
    public static final String CUSTOM_CLICK_KEY = "CustomClick";

    /**
     * The video click event map.
     */
    private Map<String, ClickElement> mVideoClickElements = new HashMap<>();

    /**
     * Constructor.
     *
     * @param videoClicksMap A map containing the info needed to create the VideoClicks object.
     */
    public VideoClicks(Map<String, Map> videoClicksMap) {

        if (videoClicksMap != null) {

            Map<String, Map> clickThroughMap = videoClicksMap.get(CLICK_THROUGH_KEY);
            if (clickThroughMap != null) {
                getVideoClickElements().put(CLICK_THROUGH_KEY, new ClickElement(clickThroughMap));
            }
            Map<String, Map> clickTrackingMap = videoClicksMap.get(CLICK_TRACKING_KEY);
            if (clickTrackingMap != null) {
                getVideoClickElements().put(CLICK_TRACKING_KEY, new ClickElement(clickTrackingMap));
            }
            Map<String, Map> customClickMap = videoClicksMap.get(CUSTOM_CLICK_KEY);
            if (customClickMap != null) {
                getVideoClickElements().put(CUSTOM_CLICK_KEY, new ClickElement(customClickMap));
            }
        }
    }

    /**
     * Get map of video click elements.
     *
     * @return The videoClickElements map.
     */
    public Map<String, ClickElement> getVideoClickElements() {

        return mVideoClickElements;
    }

    /**
     * Set the videoClickElements Map.
     *
     * @param videoClickElements The clickElements map need to set.
     */
    public void setVideoClickElements(Map<String, ClickElement> videoClickElements) {

        mVideoClickElements = videoClickElements;
    }

    @Override
    public String toString() {

        return "VideoClicks{" +
                "mVideoClickElements=" + mVideoClickElements +
                '}';
    }
}
