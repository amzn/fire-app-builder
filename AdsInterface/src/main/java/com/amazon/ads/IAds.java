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
     * Constant for duration field.
     */
    String DURATION = "duration";

    /**
     * Constant for wasAMidRoll field.
     */
    String WAS_A_MID_ROLL = "wasAMidRoll";

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
     * @param context     Context which Ads consumed in.
     * @param frameLayout Layout for Ads.
     * @param extras      Extra bundle to pass through data.
     */
    void init(Context context, FrameLayout frameLayout, Bundle extras);

    /**
     * Method to show Pre Roll Ads.
     */
    void showPreRollAd();

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
     * Activity states for ads implementation consumption.
     */
    enum ActivityState {
        START,
        RESUME,
        PAUSE,
        STOP
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
}
