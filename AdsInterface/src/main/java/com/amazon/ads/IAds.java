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
package com.amazon.ads;

import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;

/**
 * Ads Interface.
 */
public interface IAds {

    /**
     * Constant for Ad Id.
     */
    String ID = "id";

    /**
     * Constant for duration received from ad metadata.
     */
    String DURATION_RECEIVED = "durationReceived";

    /**
     * Constant for duration calculated during ad play.
     */
    String DURATION_PLAYED = "durationPlayed";

    /**
     * Constant for getting the ad pod complete boolean out of the extras bundle.
     */
    String AD_POD_COMPLETE = "adPodComplete";

    /**
     * Constant for getting the ad type out of the extras bundle.
     */
    String AD_TYPE = "ad_type";

    /**
     * Constant for a pre-roll ad.
     */
    String PRE_ROLL_AD = "preroll";

    /**
     * Constant for a mid-roll ad.
     */
    String MID_ROLL_AD = "midroll";

    /**
     * Constant for a post-roll ad.
     */
    String POST_ROLL_AD = "postroll";

    /**
     * Parameter to add to an ad tag URL with a timestamp so the add will play consecutively if
     * called upon.
     */
    String CORRELATOR_PARAMETER = "correlator";

    /**
     * Constant for getting the videos extras bundle from the ad implementation's extras bundle.
     */
    String VIDEO_EXTRAS = "video";

    /**
     * Constant for getting the video duration long out of the video extras bundle.
     */
    String VIDEO_DURATION = "duration";

    /**
     * Major version number.
     */
    int major = 0;

    /**
     * Minor version number.
     */
    int minor = 1;

    /**
     * Init Ads instance.
     *
     * @param context     The context.
     * @param frameLayout Layout for the Ads player.
     * @param extras      Extra bundle to pass through data.
     */
    void init(Context context, FrameLayout frameLayout, Bundle extras);

    /**
     * Method to show Ads.
     */
    void showAds();

    /**
     * Interface for Ads events.
     */
    interface IAdsEvents {

        /**
         * Event triggered on starting ad slot.
         *
         * @param extras Extras bundle.
         */
        void onAdSlotStarted(Bundle extras);

        /**
         * Event triggered on ending ad slot.
         *
         * @param extras Extras bundle.
         */
        void onAdSlotEnded(Bundle extras);
    }

    /**
     * Set ad events interface.
     *
     * @param iAdsEvents AdsEvents interface
     */
    void setIAdsEvents(IAdsEvents iAdsEvents);

    /**
     * Set position of the current video.
     *
     * @param position Current video position.
     */
    void setCurrentVideoPosition(double position);

    /**
     * Return true if there are one or more post roll ads to play; false otherwise.
     *
     * @return True if there are one or more post roll ads to play; false otherwise.
     */
    boolean isPostRollAvailable();

    /**
     * Activity states for ads implementation consumption.
     */
    enum ActivityState {
        START,
        RESUME,
        PAUSE,
        STOP,
        DESTROY
    }

    /**
     * Set state of activity for ads implementation consumption.
     *
     * @param activityState Activity state.
     */
    void setActivityState(ActivityState activityState);

    /**
     * Player states for ads implementation consumption.
     */
    enum PlayerState {
        PLAYING,
        PAUSED,
        COMPLETED
    }

    /**
     * Set state of player for ads implementation consumption.
     *
     * @param playerState Player state.
     */
    void setPlayerState(PlayerState playerState);

    /**
     * Set the extra data bundle.
     *
     * @param extra Bundle for extra data.
     */
    void setExtra(Bundle extra);

    /**
     * Gets the extra data bundle.
     *
     * @return Bundle for extra data.
     */
    Bundle getExtra();

    /**
     * Gets the total number of segments in ad.
     * Indicates the total number of segments of the content media, which is equal to the number of
     * mid-roll ad pods + 1. Value is 1 if there are no mid-roll ad pods for the content media.
     *
     * @return int number of total segments.
     */
    int getNumberOfSegments();

    /**
     * Get the current segment number of the content based on the mid roll ads list.
     *
     * @param position playback location of current Content.
     * @param duration total duration of the current Content.
     * @return the current segment of the content media. Start with value 1 and calculated based on
     * mid roll ads.
     */
    int getCurrentContentSegmentNumber(long position, long duration);
}
