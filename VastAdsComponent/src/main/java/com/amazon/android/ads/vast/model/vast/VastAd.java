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

import java.util.ArrayList;
import java.util.List;

/**
 * This class contains common properties of an ad from within a creative element of a vast response.
 */
public class VastAd {

    /**
     * A list of media files.
     */
    private List<MediaFile> mMediaFiles;

    /**
     * A list of tracking events.
     */
    private List<Tracking> mTrackingEvents;

    /**
     * Constructor.
     */
    public VastAd() {

        mMediaFiles = new ArrayList<>();
        mTrackingEvents = new ArrayList<>();
    }

    /**
     * Add a media file.
     *
     * @param mediaFile The media file.
     */
    public void addMediaFile(MediaFile mediaFile) {

        mMediaFiles.add(mediaFile);
    }

    /**
     * Get the media files.
     *
     * @return List of media files.
     */
    public List<MediaFile> getMediaFiles() {

        return mMediaFiles;
    }

    /**
     * Set the media files list.
     *
     * @param mediaFiles List of media files.
     */
    public void setMediaFiles(List<MediaFile> mediaFiles) {

        mMediaFiles = mediaFiles;
    }

    /**
     * Add a tracking event.
     *
     * @param tracking The tracking event.
     */
    public void addTrackingEvent(Tracking tracking) {

        mTrackingEvents.add(tracking);
    }

    /**
     * Get tracking events.
     *
     * @return List of tracking events.
     */
    public List<Tracking> getTrackingEvents() {

        return mTrackingEvents;
    }

    /**
     * Set the tracking events.
     *
     * @param trackingEvents List of tracking events.
     */
    public void setTrackingEvents(List<Tracking> trackingEvents) {

        mTrackingEvents = trackingEvents;
    }

    @Override
    public String toString() {

        return "VastAd{" +
                "mMediaFiles=" + mMediaFiles +
                ", mTrackingEvents=" + mTrackingEvents +
                '}';
    }
}
