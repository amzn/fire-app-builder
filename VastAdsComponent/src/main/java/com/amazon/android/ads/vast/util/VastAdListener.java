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
package com.amazon.android.ads.vast.util;

import com.amazon.android.ads.vast.model.vmap.AdBreak;

import java.util.List;

/**
 * Listener to keep track of playing vast ad pods.
 */
public interface VastAdListener {

    /**
     * Indicates that the VMAP response has been parsed and the ads are ready to be played.
     */
    void adsReady();

    /**
     * To be called on the start of an ad pod, or chuck of ads to be played together.
     *
     * @param adIdx  The index of the ad to start at.
     * @param adList The list of ads to play.
     */
    void startAdPod(int adIdx, List<AdBreak> adList);

    /**
     * To be called when the ad should start playing.
     */
    void startAd();

    /**
     * To be called when the ad is done playing.
     */
    void adComplete();

    /**
     * To be called when all the ads of the ad pod have been played.
     */
    void adPodComplete();
}
