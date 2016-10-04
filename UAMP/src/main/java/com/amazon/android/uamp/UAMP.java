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
package com.amazon.android.uamp;

import android.content.Context;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.amazon.mediaplayer.AMZNMediaPlayer;

/**
 * Universal Android Media Player Interface.
 */
public interface UAMP extends AMZNMediaPlayer {

    /**
     * Init implementation.
     *
     * @param context     Context
     * @param frameLayout FrameLayout where video will endup.
     * @param extras      Extra bundle to pass implementation specific data.
     */
    void init(Context context, FrameLayout frameLayout, Bundle extras);

    /**
     * Get Extra bundle.
     *
     * @return Extra Bundle.
     */
    Bundle getExtra();

    /**
     * Can render CC.
     *
     * @return result
     */
    boolean canRenderCC();

    /**
     * Can render Ads.
     *
     * @return result
     */
    boolean canRenderAds();

    /**
     * Attach SurfaceView to provided FrameLayout.
     */
    void attachSurfaceView();

    /**
     * Detach SurfaceView from provided FrameLayout.
     */
    void detachSurfaceView();
}
