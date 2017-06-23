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
import com.amazon.android.ads.vast.model.vast.Creative;
import com.amazon.android.ads.vast.model.vast.Inline;
import com.amazon.android.ads.vast.model.vast.LinearAd;
import com.amazon.android.ads.vast.model.vast.VastAd;
import com.amazon.android.ads.vast.model.vast.VastResponse;
import com.amazon.android.ads.vast.model.vmap.AdBreak;
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
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

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
        SurfaceHolder.Callback {

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
    private SurfaceHolder mSurfaceHolder;

    private int mVideoHeight;
    private int mVideoWidth;
    private int mScreenWidth;
    private int mScreenHeight;
    private boolean mIsVideoPaused = false;
    private boolean mIsPlayBackError = false;
    private boolean mIsProcessedImpressions = false;
    private boolean mIsCompleted = false;

    private Timer mTrackingEventTimer;
    private int mQuartile = 0;

    private ActivityState mActivityState;

    private VmapResponse mAdResponse;
    private AdTagProcessor.AdTagType mAdType;
    private List<AdBreak> mMidRollAds;
    private List<AdBreak> mPostRollAds;
    private List<Boolean> mPlayedMidRollAds;
    private int mAdPlayedCount;
    private String mCurrentAdType;

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

        Log.d(TAG, "Init called, version:" + VERSION);
    }

    @Override
    public void showPreRollAd() {
        Log.d(TAG, "showPreRollAd called");
        cleanUpMediaPlayer();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        createSurface(params);
        mCurrentAdType = IAds.PRE_ROLL_AD;
        mAdPlayedCount = 0;

        loadAdFromUrl();
    }

    @Override
    public void setIAdsEvents(IAdsEvents iAdsEvents) {

        mIAdsEvents = iAdsEvents;
    }

    @Override
    public void setCurrentVideoPosition(double position) {

        if (mMidRollAds != null && mMidRollAds.size() > 0 &&
                mPlayedMidRollAds.contains(false)) {

            int i = 0;
            List<AdBreak> midRollsToPlayNow = new ArrayList<>();
            for (AdBreak adBreak : mMidRollAds) {

                // Queue the add if the time offset matches the playback position
                // and the ad hasn't been played yet.
                if (adBreak.getConvertedTimeOffset() == ((int) position / 1000)
                        && !mPlayedMidRollAds.get(i)) {

                    Log.d(TAG, "Mid roll " + i + " played=" + mPlayedMidRollAds.get(i) +
                            " compare :" + adBreak.getConvertedTimeOffset() + " == "
                            + (int) (position / 1000));

                    midRollsToPlayNow.add(adBreak);
                    mPlayedMidRollAds.set(i, true);
                }
                i++;
            }
            // Play any mid-roll ads that matched the playback position
            if (!midRollsToPlayNow.isEmpty()) {
                createMediaPlayer();

                Log.d(TAG, "Play mid-rolls!!");
                mCurrentAdType = IAds.MID_ROLL_AD;
                mAdListener.startAdPod(0, midRollsToPlayNow);
            }
        }
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
     * Tasks to perform on change of activity state.
     *
     * @param activityState new activity state
     */
    private void activityStateChanged(ActivityState activityState) {
        //TODO: VAST might need to do something with other states as well, go through the docs
        // and implement them Devtech-2630
        if (activityState == ActivityState.PAUSE) {
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            cleanUpMediaPlayer();
        }
    }

    @Override
    public void setPlayerState(PlayerState playerState) {

        if (playerState == PlayerState.COMPLETED) {
            if (mPostRollAds != null && mPostRollAds.size() > 0) {
                if (mAdListener != null) {
                    Log.d(TAG, "Start post roll ads");
                    mCurrentAdType = IAds.POST_ROLL_AD;
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

    /**
     * return inline ads list
     *
     * @return List of inline ads.
     */
    private List<Inline> getInlineAds(AdBreak currentAd){

        List<Inline> inlineAds = null;
        VastResponse vastResponse = currentAd.getAdSource().getVastResponse();
        //need to check here for null as we can also get custom ad data in a vmap response
        if (vastResponse != null) {
            inlineAds = vastResponse.getInlineAds();
        }
        return inlineAds;
    }

    /**
     * provide ad id from a inline ad
     *
     * @return String ad id.
     */
    private String getCurrentAdId(AdBreak currentAd) {

        String adId = null;
        //ToDo: we need to change this after we are having single inline in each ad
        //instead of single ad with multiple inlines.
        List<Inline> inlineAds = getInlineAds(currentAd);
        //We may possibly get a wrapper ad instead of a inline ad attribute
        if (inlineAds != null && !inlineAds.isEmpty()) {
            adId = inlineAds.get(0).getId();
        }
        return adId;
    }

    /**
     * provide duration from a linear ad
     *
     * @return String ad duration.
     */
    private String getCurrentAdDuration(AdBreak currentAd) {

        String duration = null;
        List<Inline> inlineAds = getInlineAds(currentAd);
        //We may possibly get a wrapper ad instead of a inline ad attribute
        if (inlineAds != null && !inlineAds.isEmpty()) {
            //ToDo: we may need to change this if we start selecting the best format
            // in muliple creatives
            List<Creative> mCreatives = inlineAds.get(0).getCreatives();
            //creatives is a required field in linear ad
            VastAd mVastAd = mCreatives.get(0).getVastAd();
            if (mVastAd instanceof LinearAd) {
                duration = ((LinearAd) mVastAd).getDuration();
            }
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
            AdBreak currentAd = mAdResponse.getCurrentAd();
            if (currentAd != null) {
                mAdDetail.putString(ID, getCurrentAdId(currentAd));
                mAdDetail.putString(AD_TYPE, mCurrentAdType);
                String duration = getCurrentAdDuration(currentAd);
                if (duration != null) {
                    mAdDetail.putLong(DURATION_RECEIVED, (long) (convertDateFormatToSeconds
                            (duration) * MILLISECONDS_IN_SECOND));
                }
            }
        }
        return mAdDetail;
    }

    /**
     * Load data from the ad tag string resource and process the data to get the ad response.
     */
    private void loadAdFromUrl() {

        Log.d(TAG, "Loading the ad model from url.");
        mAdResponse = null;

        if (NetworkTools.connectedToInternet(mContext)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {

                    MediaPicker mediaPicker = new DefaultMediaPicker(mContext);
                    AdTagProcessor adTagProcessor = new AdTagProcessor(mediaPicker);

                    String adUrl = mContext.getResources().getString(R.string.ad_tag);

                    // Try to add a correlator value to the url if needed.
                    adUrl = NetworkUtils.addParameterToUrl(adUrl, IAds.CORRELATOR_PARAMETER,
                                                           "" + System.currentTimeMillis());

                    mAdType = adTagProcessor.process(adUrl);

                    if (mAdType != AdTagProcessor.AdTagType.error) {
                        mAdResponse = adTagProcessor.getAdResponse();
                        mAdListener.adsReady();
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

        List<AdBreak> adList;
        int adIdx;

        @Override
        public void adsReady() {

            Log.d(TAG, "Ad models are ready");
            mMidRollAds = mAdResponse.getMidRollAdBreaks();
            mPostRollAds = mAdResponse.getPostRollAdBreaks();
            mPlayedMidRollAds = new ArrayList<>();
            for (int i = 0; i < mMidRollAds.size(); i++) {
                mPlayedMidRollAds.add(false);
            }
            Log.d(TAG, "Starting pre-roll ads");
            startAdPod(0, mAdResponse.getPreRollAdBreaks());
        }

        @Override
        public void startAdPod(int adIdx, List<AdBreak> adList) {

            this.adIdx = adIdx;
            this.adList = adList;

            // Capture ad start time.
            mAdSlotStartTime = SystemClock.elapsedRealtime();
            startAd();
        }

        @Override
        public void startAd() {

            Log.d(TAG, "start ad with index " + adIdx);
            if (adIdx < adList.size()) {
                if (mMediaPlayer == null) {
                    createMediaPlayer();
                }
                mAdResponse.setCurrentAd(adList.get(adIdx));
                if (ResponseValidator.validateAdBreak(mAdResponse.getCurrentAd())) {
                    mVASTPlayerListener.vastReady();
                }
                else {
                    Log.e(TAG, "Skipping invalid ad");
                    adIdx++;
                    if (adIdx < adList.size()) {
                        startAd();
                    }
                    else {
                        adPodComplete();
                    }
                }
            }
        }

        @Override
        public void adComplete() {

            Log.d(TAG, "ad complete with index " + adIdx);
            adIdx++;
            mAdPlayedCount++;
            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            cleanUpMediaPlayer();
            if (mIAdsEvents != null) {
                // Calculate how long Ads played.
                long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                Bundle extras = getBasicAdDetailBundle();
                extras.putLong(DURATION_PLAYED, adSlotTime);
                extras.putBoolean(IAds.AD_POD_COMPLETE, adIdx == adList.size());
                // Let listener know about Ad slot stop event.
                mIAdsEvents.onAdSlotEnded(extras);
            }
            if (adIdx < adList.size()) {
                startAd();
            }
            else {
                adPodComplete();
            }
        }

        @Override
        public void adPodComplete() {
            Log.d(TAG, "ad pod complete");
            Log.d(TAG, "Played " + mAdPlayedCount + " of " + mAdResponse.getAdBreaks().size()
                    + " ads");

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

                mTrackingEventMap = mAdResponse.getCurrentAd().getTrackingUrls();

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
                        // Calculate how long Ads played.
                        long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                        Bundle extras = getBasicAdDetailBundle();
                        extras.putLong(DURATION_PLAYED, adSlotTime);
                        mIAdsEvents.onAdSlotEnded(extras);
                    }
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
            mMediaPlayer.setDisplay(mSurfaceHolder);
        }
        catch (Exception e) {
            Log.e(TAG, e.getMessage(), e);
        }
    }

    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int format, int width, int height) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {

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

        mMediaPlayer.start();

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

    private void createSurface(FrameLayout.LayoutParams params) {

        Log.d(TAG, "Creating surface");
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setLayoutParams(params);

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

        ((Activity)mContext).runOnUiThread(new Runnable() {
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

            mMediaPlayer.release();
            mMediaPlayer = null;

            ((Activity)mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {
                    mFrameLayout.removeView(mSurfaceView);
                }
            });

            mIsVideoPaused = false;
            mIsPlayBackError = false;
            mIsProcessedImpressions = false;
            mIsCompleted = false;
            mQuartile = 0;
        }

    }

    /**
     * Clean up the surface view. Should only be done after we're done using the player.
     */
    private void cleanUpSurface() {
        mSurfaceHolder.removeCallback(this);
        mSurfaceHolder.getSurface().release();
        mSurfaceView = null;
        mSurfaceHolder = null;
    }

    private void processEvent(String eventName) {

        Log.i(TAG, "entered Processing Event: " + eventName);
        List<String> urls = mTrackingEventMap.get(eventName);

        fireUrls(urls);
    }

    private void processErrorEvent() {

        Log.i(TAG, "entered processErrorEvent");

        List<String> errorUrls = mAdResponse.getCurrentAd().getErrorUrls();
        fireUrls(errorUrls);
    }

    private void processImpressions() {

        Log.i(TAG, "entered processImpressions");

        mIsProcessedImpressions = true;
        List<String> impressions = mAdResponse.getCurrentAd().getImpressions();
        fireUrls(impressions);
    }

    private void fireUrls(List<String> urls) {

        Log.i(TAG, "entered fireUrls");

        if (urls != null) {
            for (String url : urls) {
                Log.i(TAG, "\tfiring url:" + url);
                HttpTools.httpGetURL(url);
            }
        }
        else {
            Log.i(TAG, "\turl list is null");
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
}
