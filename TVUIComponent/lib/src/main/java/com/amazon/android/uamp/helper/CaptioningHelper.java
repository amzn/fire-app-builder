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
package com.amazon.android.uamp.helper;

import com.google.android.exoplayer.text.CaptionStyleCompat;
import com.google.android.exoplayer.text.SubtitleLayout;

import com.amazon.android.uamp.constants.PreferencesConstants;
import com.amazon.android.utils.Preferences;

import android.content.Context;
import android.view.accessibility.CaptioningManager;

/**
 * Captioning helper class.
 */
public class CaptioningHelper {

    private CaptioningManager mCaptioningManager;
    private SubtitleLayout mSubtitleLayout;
    private boolean mUseGlobalSetting = true;

    /**
     * Constructor.
     *
     * @param context        The context.
     * @param subtitleLayout The subtitle layout.
     */
    public CaptioningHelper(Context context, SubtitleLayout subtitleLayout) {

        mCaptioningManager =
                (CaptioningManager) context.getSystemService(Context.CAPTIONING_SERVICE);
        mSubtitleLayout = subtitleLayout;

        if (mCaptioningManager != null) {

            float fontScale = mCaptioningManager.getFontScale();

            mSubtitleLayout.setApplyEmbeddedStyles(false);
            mSubtitleLayout.setFractionalTextSize(
                    fontScale * SubtitleLayout.DEFAULT_TEXT_SIZE_FRACTION);

            mSubtitleLayout.setStyle(
                    CaptionStyleCompat.createFromCaptionStyle(mCaptioningManager.getUserStyle()));
        }

    }

    /**
     * Sets the captioning change listener.
     *
     * @param captioningChangeListener The captioning change listener.
     */
    public void setCaptioningManagerListener(CaptioningManager.CaptioningChangeListener
                                                     captioningChangeListener) {

        if (mCaptioningManager != null) {
            mCaptioningManager.addCaptioningChangeListener(captioningChangeListener);
        }
    }

    /**
     * Removes the captioning change listener.
     *
     * @param captioningChangeListener The captioning change listener.
     */
    public void removeCaptioningManagerListener(CaptioningManager.CaptioningChangeListener
                                                        captioningChangeListener) {

        if (mCaptioningManager != null) {
            mCaptioningManager.removeCaptioningChangeListener(captioningChangeListener);
        }
    }

    /**
     * True if captioning is enabled; false otherwise.
     *
     * @return True if captioning is enabled; false otherwise.
     */
    public boolean isEnabled() {

        return mCaptioningManager != null && mCaptioningManager.isEnabled();
    }

    /**
     * Set the state for using the global settings value of closed captioning.
     *
     * @param state True if global settings should be used; false if local setting should be used.
     */
    public void setUseGlobalSetting(boolean state) {

        mUseGlobalSetting = state;
    }

    /**
     * Get the value for using the global setting.
     *
     * @return True if global setting should be used; false if local setting should be used.
     */
    public boolean useGlobalSetting() {

        return !Preferences.containsPreference(PreferencesConstants.IS_CLOSE_CAPTION_FLAG_PERSISTED)
            && mUseGlobalSetting;
    }
}