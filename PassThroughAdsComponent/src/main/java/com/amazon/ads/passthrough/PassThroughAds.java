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
package com.amazon.ads.passthrough;

import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.amazon.ads.IAds;

/**
 * Some of the media player might be handling Ads internally thus we need a pass through module.
 */
public class PassThroughAds implements IAds {

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    final static String IMPL_CREATOR_NAME = PassThroughAds.class.getSimpleName();

    /**
     * Store pass through data in here.
     */
    private Bundle extra;

    /**
     * Ad event interface.
     */
    private IAdsEvents mIAdsEvents;

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extra) {

        this.extra = extra;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void showAds() {

        showPreRollAds();
    }

    /**
     * Show pre roll Ads in the FrameLayout provided.
     */
    private void showPreRollAds() {

        if (mIAdsEvents != null) {
            mIAdsEvents.onAdSlotStarted(null);
            mIAdsEvents.onAdSlotEnded(null);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setIAdsEvents(IAdsEvents iAdsEvents) {

        mIAdsEvents = iAdsEvents;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setCurrentVideoPosition(double position) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isPostRollAvailable() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setActivityState(ActivityState activityState) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setPlayerState(PlayerState playerState) {

    }

    /**
     * Set pass through data.
     *
     * @param extra Pass through bundle.
     */
    public void setExtra(Bundle extra) {

        this.extra = extra;
    }

    /**
     * Get pass through data.
     *
     * @return Pass through bundle.
     */
    public Bundle getExtra() {

        return this.extra;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getNumberOfSegments(){

        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentContentSegmentNumber(long position, long duration){

        return 1;
    }
}
