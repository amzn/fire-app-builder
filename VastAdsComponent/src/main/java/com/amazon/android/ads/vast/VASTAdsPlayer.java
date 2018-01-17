/**
 * This file was modified by Amazon:
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
/*
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast;

import com.amazon.ads.IAds;
import com.amazon.android.ads.vast.model.vast.AdElement;
import com.amazon.android.ads.vast.model.vast.Creative;
import com.amazon.android.ads.vast.model.vast.Inline;
import com.amazon.android.ads.vast.model.vast.LinearAd;
import com.amazon.android.ads.vast.model.vast.VastAd;
import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.android.ads.vast.model.vmap.AdBreak;
import com.amazon.android.ads.vast.model.vmap.AdSource;
import com.amazon.android.ads.vast.model.vmap.Tracking;
import com.amazon.android.ads.vast.model.vmap.VmapResponse;
import com.amazon.android.ads.vast.processor.AdTagProcessor;
import com.amazon.android.ads.vast.processor.MediaPicker;
import com.amazon.android.ads.vast.processor.ResponseValidator;
import com.amazon.android.ads.vast.util.VastAdListener;
import com.amazon.android.ads.vast.util.DefaultMediaPicker;
import com.amazon.android.ads.vast.util.HttpTools;
import com.amazon.android.ads.vast.util.NetworkTools;
import com.amazon.android.utils.NetworkUtils;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.DisplayMetrics;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;
import java.util.TreeSet;
import java.util.concurrent.CountDownLatch;

import static com.amazon.utils.DateAndTimeHelper.convertDateFormatToSeconds;

/**
 * This implementation of VAST is experimental and under development. Very limited support and
 * documentation is provided. This can be used as a proof of concept for VAST but all code and
 * interfaces are subject to change.
 */
public class VASTAdsPlayer implements IAds,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback,
        AdVideoClickHandler.OnWebBrowserActivityListener {

    private static final String TAG = VASTAdsPlayer.class.getSimpleName();
    public static final String VERSION = "2.0";

    // errors that can be returned in the vastError callback method of the
    // VASTPlayerListener
    public static final int ERROR_NONE = 0;
    public static final int ERROR_NO_NETWORK = 1;
    public static final int ERROR_XML_OPEN_OR_READ = 2;
    public static final int ERROR_XML_PARSE = 3;
    public static final int ERROR_SCHEMA_VALIDATION = 4; // not used in SDK, only in sourcekit
    public static final int ERROR_POST_VALIDATION = 5;
    public static final int ERROR_EXCEEDED_WRAPPER_LIMIT = 6;
    public static final int ERROR_VIDEO_PLAYBACK = 7;

    /**
     * constant to be used to convert seconds to milliseconds
     */
    private static final int MILLISECONDS_IN_SECOND = 1000;

    private static final long QUARTILE_TIMER_INTERVAL = 250;

    private Context mContext;
    private FrameLayout mFrameLayout;
    private Bundle mExtras;
    private IAdsEvents mIAdsEvents;
    private Bundle mAdDetail;

    private HashMap<String, List<String>> mTrackingEventMap;

    private MediaPlayer mMediaPlayer;
    private SurfaceView mSurfaceView;
    private ImageView mImageView;
    private SurfaceHolder mSurfaceHolder;
    private AdVideoClickHandler mAdVideoClickHandler;

    private int mVideoHeight;
    private int mVideoWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsVideoPaused = false;
    private boolean mIsPlayBackError = false;
    private boolean mIsProcessedImpressions = false;
    private boolean mIsCompleted = false;
    private boolean mSurfaceCreated = false;

    private Timer mTrackingEventTimer;
    private int mQuartile = 0;

    private ActivityState mActivityState;

    private VmapResponse mAdResponse;
    private AdTagProcessor.AdTagType mAdType;
    private HashMap<Long, List<AdBreak>> mMidRollAds;
    private HashMap<Long, Boolean> mPlayedMidRollAds;
    private List<AdBreak> mPostRollAds;

    /**
     * Flag to make sure post roll ads are not getting started again.
     */
    private boolean mPlayedPostRollAds;
    private TreeSet<Long> mMidRollAdsSegment;
    private int mAdPlayedCount;
    private boolean mIsLastAdSkipped;
    private String mCurrentAdType;
    private boolean mAreMidAndPostRollsSet = false;
    private MediaPicker mMediaPicker;
    private boolean mMediaPlayerPaused = false;
    private boolean mKeyEventHandlingEnabled = false;
    private boolean mPlayStateOverlaySelected = false;
    private boolean mBrowserSupportEnabled = false;
    private CountDownLatch mSurfaceCreatedLatch = null;

    /**
     * Ad start time.
     */
    private long mAdSlotStartTime;

    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extras) {

        mContext = context;
        mFrameLayout = frameLayout;
        mExtras = extras;

        DisplayMetrics displayMetrics = mContext.getResources().getDisplayMetrics();

        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;
        mKeyEventHandlingEnabled = context.getResources().getBoolean(R.bool.enable_key_events_on_ad_view);
        mPlayStateOverlaySelected = context.getResources().getBoolean(R.bool.enable_play_state_overlay_on_ad_view);
        mBrowserSupportEnabled = mContext.getResources().getBoolean(R.bool.enable_browser_support_on_ad_view);
        mAdVideoClickHandler = new AdVideoClickHandler(mContext, this);
        Log.d(TAG, "Init called, version:" + VERSION);
    }

    @Override
    public void showAds() {

        Log.d(TAG, "showAds called");
        // Media player context is available, no need to cleanup and start with pre-roll ads.
        if (mMediaPlayer != null) {
            if (mIAdsEvents != null) {
                mIAdsEvents.onAdSlotStarted(getBasicAdDetailBundle());
            }
            return;
        }
        showPreRollAds();
    }

    /**
     * Show pre roll Ads in the FrameLayout provided.
     */
    private void showPreRollAds() {

        Log.d(TAG, "showPreRollAds called");
        cleanUpMediaPlayer();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        createSurface(params);
        mCurrentAdType = IAds.PRE_ROLL_AD;
        mAdPlayedCount = 0;
        mIsLastAdSkipped = false;
        loadAdFromUrl();
    }

    @Override
    public void setIAdsEvents(IAdsEvents iAdsEvents) {

        mIAdsEvents = iAdsEvents;
    }

    @Override
    public void setCurrentVideoPosition(double position) {

        long videoDuration = getVideoDurationFromExtras();

        if (!mAreMidAndPostRollsSet) {
            setMidAndPostRollAds(videoDuration);
        }

        // If there are any mid-roll ads left to play
        if (mMidRollAds != null && mMidRollAds.size() > 0
                && mPlayedMidRollAds.containsValue(false)) {

            long positionInSeconds = (long) position / 1000;
            // Play the list of mid-roll ads that matched the playback position if not already
            // played
            if (mMidRollAds.containsKey(positionInSeconds)
                    && !mPlayedMidRollAds.get(positionInSeconds)) {
                createMediaPlayer();

                Log.d(TAG, "Play mid-rolls at position " + positionInSeconds);
                mCurrentAdType = IAds.MID_ROLL_AD;
                mPlayedMidRollAds.put(positionInSeconds, true);
                mAdListener.startAdPod(0, mMidRollAds.get(positionInSeconds));
            }
        }
    }

    /**
     * Get the video duration value from the video extras from the extras bundle.
     *
     * @return the video duration.
     */
    private long getVideoDurationFromExtras() {

        if (getExtra() != null) {
            Bundle videoExtras = (Bundle) getExtra().get(IAds.VIDEO_EXTRAS);
            if (videoExtras != null) {
                return videoExtras.getLong(IAds.VIDEO_DURATION);
            }
        }
        return 0;
    }

    @Override
    public boolean isPostRollAvailable() {

        return mPostRollAds != null && mPostRollAds.size() > 0;
    }

    @Override
    public void setActivityState(ActivityState activityState) {

        Log.d(TAG, "Activity state changed from " + mActivityState + " to " + activityState);

        mActivityState = activityState;
        activityStateChanged(activityState);
    }

    /**
     * call media player resume operations based on whether user paused the ads before
     * activity stop or not. If paused just seek to show one frame else start media player.
     *
     * @param isUserPaused If user paused the ad before activity stop.
     */
    private void onResumeRunMediaOperation(boolean isUserPaused) {

        //check if user paused the ad before activity paused.
        if (isUserPaused) {
            try {
                //Seek media player to show one frame as surface view does not hold the last frame,
                //causing black screen.
                mMediaPlayer.seekTo(mMediaPlayer.getCurrentPosition());
            }
            catch (IllegalStateException e) {
                Log.e(TAG, "Error in ad seek during activity resume", e);
            }
        }
        else {
            //Start media player now as surface should have been created.
            mMediaPlayer.start();
        }
        mMediaPlayerPaused = false;
    }

    /**
     * Wait for the surface creation and then call media player resume operations.
     *
     * @param isUserPaused If user paused the ad before activity stop.
     */
    private void onResumeWaitForSurfaceCreation(final boolean isUserPaused) {

        //check if surface has been created already
        if (!mSurfaceCreated) {
            //create new thread as we need to wait for the surface creation.
            (new Thread(new Runnable() {
                @Override
                public void run() {

                    mSurfaceCreatedLatch = new CountDownLatch(1);
                    try {
                        //wait for call of surfaceCreated callback before media player operation.
                        mSurfaceCreatedLatch.await();
                    }
                    catch (InterruptedException e) {
                        Log.e(TAG, "Waiting for surface creation got exception", e);
                        mSurfaceCreatedLatch = null;
                        return;
                    }
                    //Start media player operation now as surface should have been created.
                    mSurfaceCreatedLatch = null;
                    onResumeRunMediaOperation(isUserPaused);
                }
            })).start();
        }
        else {
            onResumeRunMediaOperation(isUserPaused);
        }
    }

    /**
     * Tasks to perform on change of activity state.
     *
     * @param activityState new activity state
     */
    private void activityStateChanged(ActivityState activityState) {

        if (mMediaPlayer != null) {
            Log.d(TAG, "Media player state is: " + mMediaPlayer.isPlaying());
        }

        switch (activityState) {
            case PAUSE:
                if (mMediaPlayer != null && mMediaPlayer.isPlaying()) {
                    mMediaPlayer.pause();
                    mMediaPlayerPaused = true;
                }
                break;
            case RESUME:
                if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && mMediaPlayerPaused) {
                    //Wait for surface creation and then seek or start media player based on state.
                    onResumeWaitForSurfaceCreation(mIsVideoPaused);
                }
                break;
            case DESTROY:
                cleanUpMediaPlayer();
                break;
        }
    }

    @Override
    public void setPlayerState(PlayerState playerState) {

        if (playerState == PlayerState.COMPLETED) {
            if (mPostRollAds != null && mPostRollAds.size() > 0 && !mPlayedPostRollAds) {
                if (mAdListener != null) {
                    Log.d(TAG, "Start post roll ads");
                    mCurrentAdType = IAds.POST_ROLL_AD;
                    mPlayedPostRollAds = true;
                    mAdListener.startAdPod(0, mPostRollAds);
                }
            }
        }
    }

    @Override
    public void setExtra(Bundle extra) {

        mExtras = extra;
    }

    @Override
    public Bundle getExtra() {

        if (mExtras == null) {
            mExtras = new Bundle();
        }
        return mExtras;
    }

    @Override
    public int getNumberOfSegments() {

        if (mMidRollAds != null) {
            return mMidRollAds.size() + 1;
        }
        return 1;
    }

    /**
     * Get the current inline ad
     *
     * @return Inline ad element from current Ad.
     */
    private Inline getCurrentInlineAd() {

        Inline inline = null;
        if (mAdResponse != null) {

            AdElement adElement = mAdResponse.getCurrentAd();
            if (adElement != null) {
                inline = adElement.getInlineAd();
            }
        }
        return inline;
    }

    /**
     * Get linear ad element in current ad if available.
     *
     * @return Linear ad element in current ad or null.
     */
    private LinearAd getCurrentLinearAd() {

        LinearAd linearAd = null;
        Inline inlineAd = getCurrentInlineAd();
        // We may possibly get a wrapper ad instead of a inline ad attribute
        if (inlineAd != null) {
            List<Creative> mCreatives = inlineAd.getCreatives();
            // Creatives is a required field in linear ad
            VastAd mVastAd = mCreatives.get(0).getVastAd();
            if (mVastAd != null && mVastAd instanceof LinearAd) {
                linearAd = (LinearAd) mVastAd;
            }
        }
        return linearAd;
    }

    /**
     * Get vast response from provided ad break
     *
     * @param adBreak AdBreak
     * @return Vast response from server parsed in Java object.
     */
    private VastResponse getVastResponseFromAdBreak(AdBreak adBreak) {

        VastResponse vastResponse = null;
        if (adBreak != null) {

            AdSource adSource = adBreak.getAdSource();
            if (adSource != null) {
                vastResponse = adSource.getVastResponse();
            }
        }
        return vastResponse;
    }

    /**
     * Get ad id from a inline ad
     *
     * @param currentAd AdElement
     * @return String ad id.
     */
    private String getCurrentAdId(AdElement currentAd) {

        String adId = null;
        if (currentAd != null) {
            adId = currentAd.getId();
        }
        return adId;
    }

    /**
     * provide duration from a linear ad
     *
     * @return String ad duration.
     */
    private String getCurrentAdDuration() {

        String duration = null;

        LinearAd linearAd = getCurrentLinearAd();
        if (linearAd != null) {
            duration = linearAd.getDuration();
        }
        return duration;
    }

    /**
     * provide basic ad details
     *
     * @return bundle containing ad details.
     */
    private Bundle getBasicAdDetailBundle() {

        if (mAdDetail == null) {
            mAdDetail = new Bundle();

            if (mAdResponse != null) {
                AdElement currentAd = mAdResponse.getCurrentAd();
                if (currentAd != null) {
                    mAdDetail.putString(ID, getCurrentAdId(currentAd));
                    mAdDetail.putString(AD_TYPE, mCurrentAdType);
                    String duration = getCurrentAdDuration();

                    if (duration != null) {
                        mAdDetail.putLong(DURATION_RECEIVED, (long) (convertDateFormatToSeconds
                                (duration) * MILLISECONDS_IN_SECOND));
                    }
                }
            }
        }
        return mAdDetail;
    }

    /**
     * reset ad parameters to reflect data from new ad.
     */
    private void resetAdState() {

        mAdResponse = null;
        mAreMidAndPostRollsSet = false;
        mMidRollAdsSegment = new TreeSet<>();
    }

    /**
     * Load data from the ad tag string resource and process the data to get the ad response.
     */
    private void loadAdFromUrl() {

        Log.d(TAG, "Loading the ad model from url.");
        resetAdState();

        if (NetworkTools.connectedToInternet(mContext)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {

                    mMediaPicker = new DefaultMediaPicker(mContext);
                    AdTagProcessor adTagProcessor = new AdTagProcessor(mMediaPicker);

                    String adUrl = mContext.getResources().getString(R.string.ad_tag);

                    // Try to add a correlator value to the url if needed.
                    adUrl = NetworkUtils.addParameterToUrl(adUrl, IAds.CORRELATOR_PARAMETER,
                                                           "" + System.currentTimeMillis());

                    mAdType = adTagProcessor.process(adUrl);
                    if (mAdType == AdTagProcessor.AdTagType.no_ad_break_found) {
                        //No Ad break found just mark this ad as complete.
                        mVASTPlayerListener.vastComplete();
                    }
                    else if (mAdType == AdTagProcessor.AdTagType.vmap ||
                            mAdType == AdTagProcessor.AdTagType.vast) {
                        mAdResponse = adTagProcessor.getAdResponse();
                        mAdListener.adsReady();
                    }
                    else if (mAdType == AdTagProcessor.AdTagType.validation_error) {
                        mVASTPlayerListener.vastError(ERROR_SCHEMA_VALIDATION);
                    }
                    else {
                        mVASTPlayerListener.vastError(ERROR_XML_PARSE);
                    }
                }
            })).start();
        }
        else {
            mVASTPlayerListener.vastError(ERROR_NO_NETWORK);
        }
    }

    /**
     * An ad listener that deals with playing ad pods (multiple ads at a time).
     */
    private VastAdListener mAdListener = new VastAdListener() {

        List<AdElement> adList;
        List<AdBreak> adBreakList;
        int adIdx;
        int adBreakIdx;

        @Override
        public void adsReady() {

            Log.d(TAG, "Ad models are ready; Starting pre-roll ads");
            startAdPod(0, mAdResponse.getPreRollAdBreaks(0));
        }

        /**
         * Get an ad break and return a list of Ads to be played
         *
         * @param adBreak AdBreak
         * @return list of Ads to be played in this ad pod
         */
        private List<AdElement> getAdListFromAdBreak(AdBreak adBreak) {

            List<AdElement> adList = new ArrayList<>();
            VastResponse vastResponse = getVastResponseFromAdBreak(adBreak);
            if (vastResponse != null) {
                adList.addAll(vastResponse.getAdElements());
            }
            //ads need to be played in this ad pod based on the ad sequence
            Collections.sort(adList);
            return adList;
        }

        @Override
        public void startAdPod(int adBreakIdx, List<AdBreak> adBreakList) {

            this.adBreakIdx = adBreakIdx;
            this.adBreakList = adBreakList;

            if (adBreakList != null && adBreakIdx < adBreakList.size()) {

                mAdResponse.setCurrentAdBreak(adBreakList.get(adBreakIdx));
                //Get VMAP tracking events from current Ad Break
                mTrackingEventMap = mAdResponse.getCurrentAdBreak().getTrackingUrls();
                //set list of ads in current ad break
                this.adIdx = 0;
                this.adList = getAdListFromAdBreak(mAdResponse.getCurrentAdBreak());
                // Capture ad start time.
                mAdSlotStartTime = SystemClock.elapsedRealtime();
                startAd();
            }
            else {
                // There were no ads to play so inform the listening player and cleanup.
                adPodComplete();

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        mIAdsEvents.onAdSlotEnded(null);
                    }
                });
            }
        }

        /**
         * Check if any more ads in this ad pod to be played
         */
        private boolean isAdPodCompleted() {

            return (adBreakIdx == adBreakList.size() && adIdx == adList.size());
        }

        /**
         * Increment the ad counter ad break counter if needed
         */
        private void incrementAdCounter() {

            adIdx++;
            if ((adList.size() == 0 || adIdx == adList.size()) && adBreakIdx < adBreakList.size()) {
                adBreakIdx++;
                //Also increment the number of Ad breaks completed
                mAdPlayedCount++;
            }
        }

        /**
         * Move to next ad in the ad break if available else move to next Ad break
         */
        private void moveToNextAd() {

            if (adIdx < adList.size()) {
                startAd();
            }
            else {
                if (adBreakIdx < adBreakList.size()) {
                    adIdx = 0;
                    mAdResponse.setCurrentAdBreak(adBreakList.get(adBreakIdx));
                    adList = getAdListFromAdBreak(mAdResponse.getCurrentAdBreak());
                    //Get VMAP tracking events from current Ad Break
                    mTrackingEventMap = mAdResponse.getCurrentAdBreak().getTrackingUrls();
                    startAd();
                }
                else {
                    adPodComplete();
                }
            }
        }

        /**
         * Skip the invalid ad and fire the error event.
         */
        private void skipInvalidAd() {

            Log.e(TAG, "Skipping invalid ad");
            mIsLastAdSkipped = true;
            processErrorEvent();
            incrementAdCounter();
            moveToNextAd();
        }

        @Override
        public void startAd() {

            Log.d(TAG, "start ad with index " + adIdx);
            if (!isAdPodCompleted()) {
                //check if there is no ad element in current ad break and ad is valid
                if (adList.size() > 0 && ResponseValidator.validateAdElement(adList.get(adIdx),
                                                                             mMediaPicker)) {
                    mAdResponse.setCurrentAd(adList.get(adIdx));
                    if (mKeyEventHandlingEnabled) {
                        mAdVideoClickHandler.initVideoClickURIs(getCurrentLinearAd());
                    }
                    if (mMediaPlayer == null) {
                        createMediaPlayer();
                    }
                    mVASTPlayerListener.vastReady();
                    //check for ad break start and fire VMAP breakStart event
                    if (adIdx == 0 && mTrackingEventMap != null && mTrackingEventMap.containsKey
                            (Tracking.BREAK_START_TYPE)) {
                        processEvent(Tracking.BREAK_START_TYPE);
                    }
                }
                else {
                    skipInvalidAd();
                }
            }
        }

        @Override
        public void adComplete() {

            Log.d(TAG, "ad complete with index " + adIdx);
            mIsLastAdSkipped = false;
            incrementAdCounter();
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            cleanUpMediaPlayer();
            //check for ad break end and fire VMAP breakEnd
            if (adIdx == adList.size() && mTrackingEventMap != null && mTrackingEventMap
                    .containsKey(Tracking.BREAK_END_TYPE)) {
                processEvent(Tracking.BREAK_END_TYPE);
            }
            if (mIAdsEvents != null) {

                // Let listener know about Ad slot stop event.
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Calculate how long Ads played.
                        long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                        Bundle extras = getBasicAdDetailBundle();
                        extras.putLong(DURATION_PLAYED, adSlotTime);
                        extras.putBoolean(IAds.AD_POD_COMPLETE, isAdPodCompleted());
                        mIAdsEvents.onAdSlotEnded(extras);
                    }
                });
            }
            moveToNextAd();
        }

        @Override
        public void adPodComplete() {

            Log.d(TAG, "ad pod complete");
            Log.d(TAG, "Played " + mAdPlayedCount + " of " + mAdResponse.getAdBreaks().size()
                    + " ad breaks");

            //Last ad in the ad pod is invalid, need to notify the ui to start the content here
            if (mIsLastAdSkipped && mIAdsEvents != null) {

                // Let listener know about Ad slot stop event.
                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        // Calculate how long Ads played.
                        long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                        Bundle extras = new Bundle();
                        extras.putLong(DURATION_PLAYED, adSlotTime);
                        extras.putBoolean(IAds.AD_POD_COMPLETE, true);
                        mIAdsEvents.onAdSlotEnded(extras);
                    }
                });
            }

            if (mAdPlayedCount == mAdResponse.getAdBreaks().size()) {
                if (mVASTPlayerListener != null) {
                    mVASTPlayerListener.vastComplete();
                }
            }
        }
    };

    // NOT CALLED IN UI THREAD!!!
    private VASTPlayerListener mVASTPlayerListener = new VASTPlayerListener() {

        @Override
        public void vastReady() {

            Log.d(TAG, "Vast ready!");

            if (mAdResponse != null) {

                //Add tracking events from current Ad
                Inline inline = getCurrentInlineAd();
                if (inline != null) {

                    HashMap<String, List<String>> adTrackingEventMap = inline.getTrackingEvents();
                    if (mTrackingEventMap != null) {
                        mTrackingEventMap.putAll(adTrackingEventMap);
                    }
                    else {
                        mTrackingEventMap = adTrackingEventMap;
                    }
                }

                ((Activity) mContext).runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        String url = mAdResponse.getCurrentAd().getSelectedMediaFileUrl();

                        Log.d(TAG, "URL for media file:" + url);
                        try {
                            mMediaPlayer.setDataSource(url);
                        }
                        catch (IOException e) {
                            Log.e(TAG, "Could not set data source for VAST ad", e);
                        }
                        mMediaPlayer.prepareAsync();
                    }
                });
            }
        }

        @Override
        public void vastError(int error) {

            Log.e(TAG, "vast error:" + error);

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                    }
                    cleanUpMediaPlayer();

                    if (mIAdsEvents != null) {
                        Bundle extras = getBasicAdDetailBundle();
                        if (mAdSlotStartTime != 0) {
                            // Calculate how long Ads played.
                            long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                            extras.putLong(DURATION_PLAYED, adSlotTime);
                        }
                        mIAdsEvents.onAdSlotEnded(extras);
                    }
                    processErrorEvent();
                }
            });
        }

        @Override
        public void vastClick() {

        }

        @Override
        public void vastComplete() {

            Log.d(TAG, "vastComplete");
            cleanUpSurface();
        }

        @Override
        public void vastDismiss() {

            Log.d(TAG, "vastDismiss");
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        Log.d(TAG, "surfaceCreated -- (SurfaceHolder callback)");
        try {
            if (mMediaPlayer == null) {
                createMediaPlayer();
            }
            mSurfaceCreated = true;
            mMediaPlayer.setDisplay(mSurfaceHolder);
            //Before starting media player we want to make sure that surface has been created.
            if (mSurfaceCreatedLatch != null && mSurfaceCreatedLatch.getCount() > 0) {
                mSurfaceCreatedLatch.countDown();
            }
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

        Log.d(TAG, "surfaceChanged -- (SurfaceHolder callback)");
    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

        Log.d(TAG, "surfaceDestroyed -- (SurfaceHolder callback)");
        mSurfaceCreated = false;
    }

    @Override
    public void onCompletion(MediaPlayer mediaPlayer) {

        Log.d(TAG, "entered onCompletion-- (MediaPlayer callback)");

        if (!mIsPlayBackError && !mIsCompleted) {
            mIsCompleted = true;
            this.processEvent(Tracking.COMPLETE_TYPE);

            if (mAdListener != null) {
                mAdListener.adComplete();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

        Log.e(TAG, "entered onError -- (MediaPlayer callback)");
        mIsPlayBackError = true;
        Log.e(TAG, "Shutting down Activity due to Media Player errors: WHAT:" + what + ": " +
                "EXTRA:" + extra + ":");

        processErrorEvent();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        calculateAspectRatio();

        mAdDetail = null;
        mIAdsEvents.onAdSlotStarted(getBasicAdDetailBundle());

        // Capture ad start time.
        mAdSlotStartTime = SystemClock.elapsedRealtime();

        //If need to handle key events on ad view
        if (mKeyEventHandlingEnabled) {
            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    //Need surface view focus to get the key events in OnKeyListener of surface view
                    mSurfaceView.setFocusable(true);
                    mSurfaceView.requestFocus();
                    //If overlay has been requested for play state
                    if (mPlayStateOverlaySelected) {
                        mFrameLayout.addView(mImageView);
                    }
                }
            });
        }

        if (!mIsProcessedImpressions) {
            this.processImpressions();
        }

        startQuartileTimer();

        if (!mMediaPlayer.isPlaying() && !mIsVideoPaused) {
            mMediaPlayer.start();
        }
    }

    @Override
    public void onVideoSizeChanged(MediaPlayer mediaPlayer, int width, int height) {

        mVideoWidth = width;
        mVideoHeight = height;
    }

    /**
     * Get the imageView with given properties.
     *
     * @param resId      resId of the image resource need to be set in image view.
     * @param alphaValue Alpha value to be set for this resource.
     */
    private ImageView createMediaControlImageView(int resId, int alphaValue) {

        //create an image view with user parameters.
        ImageView imageView = new ImageView(mContext);
        imageView.setImageResource(resId);
        imageView.setImageAlpha(alphaValue);
        FrameLayout.LayoutParams paramsOverlay = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.WRAP_CONTENT,
                FrameLayout.LayoutParams.WRAP_CONTENT);
        imageView.setLayoutParams(paramsOverlay);
        return imageView;
    }

    private void createSurface(FrameLayout.LayoutParams params) {

        Log.d(TAG, "Creating surface");
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setLayoutParams(params);

        //If need to handle user key events
        if (mKeyEventHandlingEnabled) {
            mSurfaceView.setOnKeyListener(new ViewKeyListener());
            if (mPlayStateOverlaySelected) {
                //create Play/Pause overlay if required.
                mImageView = createMediaControlImageView(android.R.drawable.ic_media_pause,
                                                         mContext.getResources().getInteger(R.integer.play_state_overlay_alpha_value));
            }
        }

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
    }

    private void createMediaPlayer() {

        Log.d(TAG, "create media player");
        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);

        ((Activity) mContext).runOnUiThread(new Runnable() {
            @Override
            public void run() {

                mFrameLayout.addView(mSurfaceView);
            }
        });

    }

    private void cleanUpMediaPlayer() {

        Log.d(TAG, "entered cleanUpMediaPlayer ");

        if (mMediaPlayer != null) {

            if (mMediaPlayer.isPlaying()) {
                mMediaPlayer.stop();
            }

            mMediaPlayer.setOnCompletionListener(null);
            mMediaPlayer.setOnErrorListener(null);
            mMediaPlayer.setOnPreparedListener(null);
            mMediaPlayer.setOnVideoSizeChangedListener(null);

            mMediaPlayer.reset();
            mMediaPlayer.release();
            mMediaPlayer = null;

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mFrameLayout.removeView(mSurfaceView);
                    //cleaning up play state overlay if enabled
                    if (mKeyEventHandlingEnabled && mPlayStateOverlaySelected) {
                        mFrameLayout.removeView(mImageView);
                    }
                }
            });

            mIsVideoPaused = false;
            mIsPlayBackError = false;
            mIsProcessedImpressions = false;
            mIsCompleted = false;
            mQuartile = 0;
            mMediaPlayerPaused = false;
        }

    }

    /**
     * Clean up the surface view. Should only be done after we're done using the player.
     */
    private void cleanUpSurface() {

        if (mSurfaceHolder != null) {
            mSurfaceHolder.removeCallback(this);
            mSurfaceHolder.getSurface().release();
        }
        mSurfaceView = null;
        mSurfaceHolder = null;
        mSurfaceCreated = false;
    }

    private void processEvent(String eventName) {

        Log.i(TAG, "entered Processing Event: " + eventName);
        List<String> urls = mTrackingEventMap.get(eventName);

        HttpTools.fireUrls(urls);
    }

    private void processErrorEvent() {

        Log.i(TAG, "entered processErrorEvent");
        List<String> errorUrls = new ArrayList();
        if (mAdResponse != null) {

            //Check for VMAP tracking element errors
            if (mTrackingEventMap != null && mTrackingEventMap.containsKey(Tracking.ERROR_TYPE)) {
                processEvent(Tracking.ERROR_TYPE);
            }
            //Check for vast response root error element in case server does not / can not serve ad.
            VastResponse vastResponse = getVastResponseFromAdBreak(mAdResponse.getCurrentAdBreak());
            if (vastResponse != null) {
                errorUrls.add(vastResponse.getErrorUrl());
            }
            //Fire Error urls found in inline ad if root error element was not found.
            if (errorUrls.size() == 0) {
                Inline inline = getCurrentInlineAd();
                if (inline != null) {
                    errorUrls.addAll(inline.getErrorUrls());
                }
            }
            HttpTools.fireUrls(errorUrls);
        }
    }

    private void processImpressions() {

        Log.i(TAG, "entered processImpressions");

        mIsProcessedImpressions = true;
        Inline inline = getCurrentInlineAd();
        if (inline != null) {
            List<String> impressions = inline.getImpressions();
            HttpTools.fireUrls(impressions);
        }
    }

    private void startQuartileTimer() {

        Log.i(TAG, "entered startQuartileTimer");
        stopQuartileTimer();

        if (mIsCompleted) {
            Log.i(TAG, "ending quartileTimer because the video has been replayed");
            return;
        }

        final int videoDuration = mMediaPlayer.getDuration();

        mTrackingEventTimer = new Timer();
        mTrackingEventTimer.scheduleAtFixedRate(new TimerTask() {

            @Override
            public void run() {

                int percentage;
                try {
                    int curPos = mMediaPlayer.getCurrentPosition();
                    // wait for the video to really start
                    if (curPos == 0) {
                        return;
                    }
                    percentage = 100 * curPos / videoDuration;
                }
                catch (Exception e) {
                    Log.e(TAG, "mediaPlayer.getCurrentPosition exception: " + e.getMessage());
                    this.cancel();
                    return;
                }

                if (percentage >= 25 * mQuartile) {
                    if (mQuartile == 0) {
                        Log.i(TAG, "Video at start: (" + percentage + "%)");
                        processEvent(Tracking.START_TYPE);
                    }
                    else if (mQuartile == 1) {
                        Log.i(TAG, "Video at first quartile: (" + percentage + "%)");
                        processEvent(Tracking.FIRST_QUARTILE_TYPE);
                    }
                    else if (mQuartile == 2) {
                        Log.i(TAG, "Video at midpoint: (" + percentage + "%)");
                        processEvent(Tracking.MIDPOINT_TYPE);
                    }
                    else if (mQuartile == 3) {
                        Log.i(TAG, "Video at third quartile: (" + percentage + "%)");
                        processEvent(Tracking.THIRD_QUARTILE_TYPE);
                        stopQuartileTimer();
                    }
                    mQuartile++;
                }
            }
        }, 0, QUARTILE_TIMER_INTERVAL);
    }

    private void stopQuartileTimer() {

        if (mTrackingEventTimer != null) {
            mTrackingEventTimer.cancel();
            mTrackingEventTimer = null;
        }
    }

    private void calculateAspectRatio() {

        Log.d(TAG, "entered calculateAspectRatio");

        if (mVideoWidth == 0 || mVideoHeight == 0) {
            Log.w(TAG, "mVideoWidth or mVideoHeight is 0, skipping calculateAspectRatio");
            return;
        }

        Log.d(TAG, "calculating aspect ratio");
        double widthRatio = 1.0 * mScreenWidth / mVideoWidth;
        double heightRatio = 1.0 * mScreenHeight / mVideoHeight;

        double scale = Math.min(widthRatio, heightRatio);

        int surfaceWidth = (int) (scale * mVideoWidth);
        int surfaceHeight = (int) (scale * mVideoHeight);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                surfaceWidth, surfaceHeight);
        mSurfaceView.setLayoutParams(params);

        mSurfaceHolder.setFixedSize(surfaceWidth, surfaceHeight);

        Log.d(TAG, " screen size: " + mScreenWidth + "x" + mScreenHeight);
        Log.d(TAG, " video size:  " + mVideoWidth + "x" + mVideoHeight);
        Log.d(TAG, " widthRatio:   " + widthRatio);
        Log.d(TAG, " heightRatio:   " + heightRatio);
        Log.d(TAG, "surface size: " + surfaceWidth + "x" + surfaceHeight);
    }

    /**
     * Categorize the mid and post roll ads from the ad response and set them to be played by the
     * player.
     *
     * @param videoDuration The duration of the video. Used to calculate the ad playback location
     *                      to determine if its a mid or post roll ad.
     */
    private void setMidAndPostRollAds(long videoDuration) {

        if (videoDuration < 1 || mAdResponse == null) {
            // The video duration is not known yet so don't mark the boolean as true so ads can
            // be set later
            return;
        }
        mMidRollAds = new HashMap<>();
        mPlayedMidRollAds = new HashMap<>();
        mPlayedPostRollAds = false;

        List<AdBreak> midRollAdBreaks = mAdResponse.getMidRollAdBreaks(videoDuration);
        for (AdBreak midRoll : midRollAdBreaks) {
            long offset = (long) midRoll.getConvertedTimeOffset(videoDuration);
            if (!mMidRollAds.containsKey(offset)) {
                mMidRollAds.put(offset, new ArrayList<AdBreak>());
                mPlayedMidRollAds.put(offset, false);
            }
            mMidRollAds.get(offset).add(midRoll);
        }
        mPostRollAds = mAdResponse.getPostRollAdBreaks(videoDuration);
        mAreMidAndPostRollsSet = true;
    }

    /**
     * Get the current segment number of the content based on the mid roll ads list.
     *
     * @param position playback location of current Content.
     * @param duration total duration of the current Content.
     * @return the current segment of the content media. Start with value 1 and based on mid roll
     * ads.
     */
    public int getCurrentContentSegmentNumber(long position, long duration) {

        if (mMidRollAds == null) {
            setMidAndPostRollAds(duration);
        }
        if (mMidRollAdsSegment.isEmpty() && mMidRollAds != null && !mMidRollAds.isEmpty()) {
            mMidRollAdsSegment.addAll(mMidRollAds.keySet());
        }
        if (!mMidRollAdsSegment.isEmpty()) {
            Long lowerValue = mMidRollAdsSegment.lower(position);
            if (lowerValue != null) {
                return mMidRollAdsSegment.headSet(lowerValue, true).size() + 1;
            }
        }
        return 1;
    }

    /**
     * Implementation of OnWebBrowserActivityListener
     */
    @Override
    public void onWebBrowserActivityLaunch() {

        if (mMediaPlayer != null && mMediaPlayer.isPlaying() && !mIsVideoPaused) {

            mMediaPlayer.pause();
        }
        mMediaPlayerPaused = true;
    }

    /**
     * Handle click event on an ad screen.
     *
     * @param isBrowserSupportEnabled if customer configured web browser support.
     */
    private void handleAdClicked(boolean isBrowserSupportEnabled) {

        Log.d(TAG, "handleAdClicked, Browser support: " + isBrowserSupportEnabled);
        mAdVideoClickHandler.handleAdVideoClicks(isBrowserSupportEnabled);
    }

    /**
     * Create the new key listener class for Ad view.
     */
    private class ViewKeyListener implements View.OnKeyListener {

        /**
         * Toggle media player state based on current media player state and last user action.
         */
        private void togglePlayState() {

            if (mMediaPlayer != null && !mMediaPlayer.isPlaying() && mIsVideoPaused) {
                mIsVideoPaused = !mIsVideoPaused;
                if (mPlayStateOverlaySelected && mImageView != null) {
                    mImageView.setImageResource(android.R.drawable.ic_media_pause);
                }
                mMediaPlayer.start();
            }
            else if (mMediaPlayer != null && mMediaPlayer.isPlaying() && !mIsVideoPaused) {
                mIsVideoPaused = !mIsVideoPaused;
                if (mPlayStateOverlaySelected && mImageView != null) {
                    mImageView.setImageResource(android.R.drawable.ic_media_play);
                }
                mMediaPlayer.pause();
            }
            //Call handleAdClicked for tracking the ad click.
            handleAdClicked(false);
        }

        @Override
        public boolean onKey(View v, int key, KeyEvent e) {
            // More key events can be added as required. Also different functionality can be added
            // on different key events if required.
            Log.d(TAG, "User pressed: " + e.getKeyCode() + " current action: " + e.getAction());
            switch (e.getKeyCode()) {
                case KeyEvent.KEYCODE_DPAD_CENTER:
                    //If not enabling browser support on ad view, the default handling is toggle
                    // play state on dpad center key event as well.
                    if (e.getAction() == KeyEvent.ACTION_UP && mBrowserSupportEnabled) {
                        handleAdClicked(true);
                        return true;
                    }
                case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                case KeyEvent.KEYCODE_MEDIA_PLAY:
                case KeyEvent.KEYCODE_MEDIA_PAUSE:
                    if (e.getAction() == KeyEvent.ACTION_UP) {
                        togglePlayState();
                        return true;
                    }
            }
            //This listener is not consuming other keypad events. Let default handling in
            // playback activity to work.
            return false;
        }
    }
}
