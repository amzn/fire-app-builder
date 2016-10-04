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
import com.amazon.android.ads.vast.model.TRACKING_EVENTS_TYPE;
import com.amazon.android.ads.vast.model.VASTModel;
import com.amazon.android.ads.vast.processor.VASTMediaPicker;
import com.amazon.android.ads.vast.processor.VASTProcessor;
import com.amazon.android.ads.vast.util.DefaultMediaPicker;
import com.amazon.android.ads.vast.util.HttpTools;
import com.amazon.android.ads.vast.util.NetworkTools;
import com.amazon.android.ads.vast.util.VASTLog;

import android.app.Activity;
import android.content.Context;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.HashMap;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * This implementation of VAST is experimental and under development.  Very limited support and
 * documentation is provided.  This can be used as a proof of concept for VAST but all code and
 * interfaces are subject to change.
 */
public class VASTAdsPlayer implements IAds,
        MediaPlayer.OnCompletionListener,
        MediaPlayer.OnErrorListener,
        MediaPlayer.OnPreparedListener,
        MediaPlayer.OnVideoSizeChangedListener,
        SurfaceHolder.Callback {

    private static final String TAG = VASTAdsPlayer.class.getSimpleName();
    public static final String VERSION = "1.3";
    public static final String VAST_TAG_BUNDLE_KEY = "VASTAdTag";

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

    private static final long QUARTILE_TIMER_INTERVAL = 250;

    private Context mContext;
    private FrameLayout mFrameLayout;
    private Bundle mExtras;
    private IAdsEvents mIAdsEvents;

    private VASTModel mVASTModel;
    private HashMap<TRACKING_EVENTS_TYPE, List<String>> mTrackingEventMap;

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

    private double mCurrentVideoPosition;
    private PlayerState mPlayerState;
    private ActivityState mActivityState;

    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extras) {

        mContext = context;
        mFrameLayout = frameLayout;
        mExtras = extras;

        mExtras.putString(VASTAdsPlayer.VAST_TAG_BUNDLE_KEY,
                          mContext.getResources().getString(R.string.vast_preroll_tag));

        DisplayMetrics displayMetrics = mContext.getResources()
                                                .getDisplayMetrics();


        mScreenWidth = displayMetrics.widthPixels;
        mScreenHeight = displayMetrics.heightPixels;

        VASTLog.d(TAG, "Init called, version:" + VERSION);
    }

    @Override
    public void showPreRollAd() {

        cleanUpMediaPlayer();

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                FrameLayout.LayoutParams.MATCH_PARENT,
                FrameLayout.LayoutParams.MATCH_PARENT);

        createSurface(params);

        loadVideoWithUrl(mExtras.getString(VAST_TAG_BUNDLE_KEY));
    }

    @Override
    public void setIAdsEvents(IAdsEvents iAdsEvents) {

        mIAdsEvents = iAdsEvents;
    }

    @Override
    public void setCurrentVideoPosition(double position) {

        mCurrentVideoPosition = position;
    }

    @Override
    public void setActivityState(ActivityState activityState) {
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

        mPlayerState = playerState;
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

    private void loadVideoWithUrl(final String urlString) {

        VASTLog.d(TAG, "loadVideoWithUrl " + urlString);
        mVASTModel = null;
        if (NetworkTools.connectedToInternet(mContext)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {

                    BufferedReader in = null;
                    StringBuffer sb;
                    try {
                        URL url = new URL(urlString);

                        in = new BufferedReader(new InputStreamReader(url.openStream()));
                        sb = new StringBuffer();
                        String line;
                        while ((line = in.readLine()) != null) {
                            sb.append(line).append(System.getProperty("line.separator"));
                        }
                    } catch (Exception e) {
                        mVASTPlayerListener.vastError(ERROR_XML_OPEN_OR_READ);
                        VASTLog.e(TAG, e.getMessage(), e);
                        return;
                    } finally {
                        try {
                            if (in != null) {
                                in.close();
                            }
                        } catch (IOException e) {
                            // ignore
                        }
                    }
                    loadVideoWithData(sb.toString());
                }
            })).start();
        } else {
            mVASTPlayerListener.vastError(ERROR_NO_NETWORK);
        }
    }

    private void loadVideoWithData(final String xmlData) {

        VASTLog.v(TAG, "loadVideoWithData\n" + xmlData);
        mVASTModel = null;
        if (NetworkTools.connectedToInternet(mContext)) {
            (new Thread(new Runnable() {
                @Override
                public void run() {

                    VASTMediaPicker mediaPicker = new DefaultMediaPicker(mContext);
                    VASTProcessor processor = new VASTProcessor(mediaPicker);
                    int error = processor.process(xmlData);
                    if (error == ERROR_NONE) {
                        mVASTModel = processor.getModel();
                        mVASTPlayerListener.vastReady();
                    } else {
                        mVASTPlayerListener.vastError(error);
                    }
                }
            })).start();
        } else {
            mVASTPlayerListener.vastError(ERROR_NO_NETWORK);
        }
    }

    // NOT BEING CALLED IN UI THREAD!!!
    private VASTPlayerListener mVASTPlayerListener = new VASTPlayerListener() {
        @Override
        public void vastReady() {

            mTrackingEventMap = mVASTModel.getTrackingUrls();

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    String url = mVASTModel.getPickedMediaFileURL();

                    VASTLog.d(TAG, "URL for media file:" + url);
                    try {
                        mMediaPlayer.setDataSource(url);
                    } catch (IOException e) {
                        VASTLog.e(TAG, "Could not set data source for VAST ad",e);
                    }
                    mMediaPlayer.prepareAsync();
                }
            });
        }

        @Override
        public void vastError(int error) {

            VASTLog.e(TAG, "vastComplete:" + error);

            ((Activity) mContext).runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    if (mMediaPlayer != null) {
                        mMediaPlayer.stop();
                    }
                    cleanUpMediaPlayer();

                    if (mIAdsEvents != null) {
                        mIAdsEvents.onAdSlotEnded(null);
                    }
                }
            });
        }

        @Override
        public void vastClick() {

        }

        @Override
        public void vastComplete() {

            VASTLog.e(TAG, "vastComplete");

            if (mMediaPlayer != null) {
                mMediaPlayer.stop();
            }
            cleanUpMediaPlayer();

            if (mIAdsEvents != null) {
                mIAdsEvents.onAdSlotEnded(null);
            }
        }

        @Override
        public void vastDismiss() {
            VASTLog.d(TAG, "vastDismiss");
        }
    };

    @Override
    public void surfaceCreated(SurfaceHolder surfaceHolder) {

        VASTLog.d(TAG, "surfaceCreated -- (SurfaceHolder callback)");
        try {
            if (mMediaPlayer == null) {
                createMediaPlayer();
            }
            mMediaPlayer.setDisplay(mSurfaceHolder);
        } catch (Exception e) {
            VASTLog.e(TAG, e.getMessage(), e);
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

        VASTLog.e(TAG, "entered onError -- (MediaPlayer callback)");

        if (!mIsPlayBackError && !mIsCompleted) {
            mIsCompleted = true;
            this.processEvent(TRACKING_EVENTS_TYPE.complete);

            if (mVASTPlayerListener != null) {
                mVASTPlayerListener.vastComplete();
            }
        }
    }

    @Override
    public boolean onError(MediaPlayer mediaPlayer, int what, int extra) {

        VASTLog.e(TAG, "entered onError -- (MediaPlayer callback)");
        mIsPlayBackError = true;
        VASTLog.e(TAG, "Shutting down Activity due to Media Player errors: WHAT:" + what + ": " +
                "EXTRA:" + extra + ":");

        processErrorEvent();

        return true;
    }

    @Override
    public void onPrepared(MediaPlayer mediaPlayer) {

        calculateAspectRatio();

        mIAdsEvents.onAdSlotStarted(null);

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

        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.setLayoutParams(params);

        mSurfaceHolder = mSurfaceView.getHolder();
        mSurfaceHolder.addCallback(this);
        mFrameLayout.addView(mSurfaceView);
    }

    private void createMediaPlayer() {

        mMediaPlayer = new MediaPlayer();
        mMediaPlayer.setOnCompletionListener(this);
        mMediaPlayer.setOnErrorListener(this);
        mMediaPlayer.setOnPreparedListener(this);
        mMediaPlayer.setOnVideoSizeChangedListener(this);
        mMediaPlayer.setAudioStreamType(AudioManager.STREAM_MUSIC);
    }

    private void cleanUpMediaPlayer() {

        VASTLog.d(TAG, "entered cleanUpMediaPlayer ");

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

            mFrameLayout.removeView(mSurfaceView);
            mSurfaceHolder.removeCallback(this);
            mSurfaceHolder.getSurface().release();
            mSurfaceView = null;
            mSurfaceHolder = null;

            mIsVideoPaused = false;
            mIsPlayBackError = false;
            mIsProcessedImpressions = false;
            mIsCompleted = false;
        }

    }

    private void processEvent(TRACKING_EVENTS_TYPE eventName) {

        VASTLog.i(TAG, "entered Processing Event: " + eventName);
        List<String> urls = mTrackingEventMap.get(eventName);

        fireUrls(urls);
    }

    private void processErrorEvent() {

        VASTLog.d(TAG, "entered processErrorEvent");

        List<String> errorUrls = mVASTModel.getErrorUrl();
        fireUrls(errorUrls);
    }

    private void processImpressions() {

        VASTLog.d(TAG, "entered processImpressions");

        mIsProcessedImpressions = true;
        List<String> impressions = mVASTModel.getImpressions();
        fireUrls(impressions);
    }

    private void fireUrls(List<String> urls) {

        VASTLog.d(TAG, "entered fireUrls");

        if (urls != null) {

            for (String url : urls) {
                VASTLog.v(TAG, "\tfiring url:" + url);
                HttpTools.httpGetURL(url);
            }
        } else {
            VASTLog.d(TAG, "\turl list is null");
        }
    }

    private void startQuartileTimer() {

        VASTLog.d(TAG, "entered startQuartileTimer");
        stopQuartileTimer();

        if (mIsCompleted) {
            VASTLog.d(TAG, "ending quartileTimer because the video has been replayed");
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
                } catch (Exception e) {
                    VASTLog.w(TAG,
                            "mediaPlayer.getCurrentPosition exception: "
                                    + e.getMessage());
                    this.cancel();
                    return;
                }

                if (percentage >= 25 * mQuartile) {
                    if (mQuartile == 0) {
                        VASTLog.i(TAG, "Video at start: (" + percentage
                                + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.start);
                    } else if (mQuartile == 1) {
                        VASTLog.i(TAG, "Video at first quartile: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.firstQuartile);
                    } else if (mQuartile == 2) {
                        VASTLog.i(TAG, "Video at midpoint: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.midpoint);
                    } else if (mQuartile == 3) {
                        VASTLog.i(TAG, "Video at third quartile: ("
                                + percentage + "%)");
                        processEvent(TRACKING_EVENTS_TYPE.thirdQuartile);
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

        VASTLog.d(TAG, "entered calculateAspectRatio");

        if (mVideoWidth == 0 || mVideoHeight == 0) {
            VASTLog.w(TAG, "mVideoWidth or mVideoHeight is 0, skipping calculateAspectRatio");
            return;
        }

        VASTLog.d(TAG, "calculating aspect ratio");
        double widthRatio = 1.0 * mScreenWidth / mVideoWidth;
        double heightRatio = 1.0 * mScreenHeight / mVideoHeight;

        double scale = Math.min(widthRatio, heightRatio);

        int surfaceWidth = (int) (scale * mVideoWidth);
        int surfaceHeight = (int) (scale * mVideoHeight);

        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                surfaceWidth, surfaceHeight);
        //params.addRule(RelativeLayout.CENTER_IN_PARENT);
        mSurfaceView.setLayoutParams(params);

        mSurfaceHolder.setFixedSize(surfaceWidth, surfaceHeight);

        VASTLog.d(TAG, " screen size: " + mScreenWidth + "x" + mScreenHeight);
        VASTLog.d(TAG, " video size:  " + mVideoWidth + "x" + mVideoHeight);
        VASTLog.d(TAG, " widthRatio:   " + widthRatio);
        VASTLog.d(TAG, " heightRatio:   " + heightRatio);
        VASTLog.d(TAG, "surface size: " + surfaceWidth + "x" + surfaceHeight);
    }
}
