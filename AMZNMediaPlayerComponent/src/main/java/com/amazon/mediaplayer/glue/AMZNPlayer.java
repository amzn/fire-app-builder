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

package com.amazon.mediaplayer.glue;

import android.content.Context;
import android.os.Bundle;
import android.provider.Settings;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.amazon.android.uamp.UAMP;
import com.amazon.mediaplayer.AMZNMediaPlayer;
import com.amazon.mediaplayer.AMZNMediaPlayerFactory;
import com.amazon.mediaplayer.playback.SeekRange;
import com.amazon.mediaplayer.playback.config.BaseContentPlaybackBufferConfig;
import com.amazon.mediaplayer.playback.config.ExoAdaptivePlaybackBufferConfig;
import com.amazon.mediaplayer.playback.config.ExoExtendedAdaptivePlaybackBufferConfig;
import com.amazon.mediaplayer.tracks.MediaFormat;
import com.amazon.mediaplayer.tracks.TrackType;

import static android.provider.Settings.Global.getInt;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_BEST;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_BETTER;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_GOOD;

/**
 * Amazon Media Player glue class.
 */
public class AMZNPlayer implements UAMP, SurfaceHolder.Callback {

    /**
     * Debug TAG.
     */
    private static final String TAG = AMZNPlayer.class.getSimpleName();

    /**
     * The bandwidth cap value that will represent VIDEO_QUALITY_GOOD setting option, valued at
     * 1350kbps.
     * TODO: These values are only initial estimates. We need to get the final versions from PM
     * or find a dynamic way to get the values from the device.
     */
    private static final int GOOD_BITRATE = 1350000;

    /**
     * The bandwidth cap value that will represent the VIDEO_QUALITY_BETTER setting option, valued
     * at 4000kbps.
     * TODO: These values are only initial estimates. We need to get the final versions from PM
     * or find a dynamic way to get the values from the device.
     */
    private static final int BETTER_BITRATE = 4000000;

    /**
     * Amazon media player.
     */
    private AMZNMediaPlayer mPlayer;

    /**
     * Pass through bundle.
     */
    private Bundle mExtras;

    /**
     * Internal reference to FrameLayout.
     */
    private FrameLayout mFrameLayout;

    /**
     * Internal reference to context.
     */
    private Context mContext;

    /**
     * SurfaceView for video playback.
     */
    private SurfaceView mSurfaceView;

    /**
     * Flag for Surface View attached status.
     */
    private boolean mSurfaceViewAttached = false;

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRenderCC() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean canRenderAds() {

        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        Log.d(TAG, "surface created");
        // Must handle this.
        this.setSurface(holder.getSurface(), false); // Non blocking.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {

        Log.d(TAG, "surface changed");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void surfaceDestroyed(SurfaceHolder holder) {

        Log.d(TAG, "surface destroyed");
        this.setSurface(null, true); // Blocking
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void attachSurfaceView() {

        if (!mSurfaceViewAttached) {
            mFrameLayout.addView(mSurfaceView);
            mSurfaceViewAttached = true;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void detachSurfaceView() {

        if (mSurfaceViewAttached) {
            mFrameLayout.removeView(mSurfaceView);
            mSurfaceViewAttached = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extras) {

        mContext = context;
        mFrameLayout = frameLayout;
        mExtras = extras;

        mPlayer = AMZNMediaPlayerFactory.createMediaPlayer(
                AMZNMediaPlayerFactory.PlayerType.EXOPLAYER, context);

        setVideoQuality();


        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().addCallback(this);
        mFrameLayout.addView(mSurfaceView);
        mSurfaceViewAttached = true;
    }

    /**
     * Reads user settings for any bit rate caps set by user and sends that value to player.
     */
    private void setVideoQuality() {

        int videoQualityValue = readVideoQualityValue();
        // Set the bandwidth cap only if we get a valid cap on video quality, else let the player
        // use the default value.
        if (videoQualityValue != -1) {
            // TODO: Even though we are asking the player to set the maximum bandwidth, it is not
            // always necessary that player will be able to abide by it. We need to listen to
            // callback from player about the exact bit rate that is being played. If it is more
            // than the one set, we should show some sort of warning to the user. Devtech 2277
            // Set the extended adaptive playback buffer's max bitrate cap.
            ExoExtendedAdaptivePlaybackBufferConfig extAdaptiveBufConfig =
                    new ExoExtendedAdaptivePlaybackBufferConfig();

            extAdaptiveBufConfig.mMaxBitrateCap = videoQualityValue;

            mPlayer.setContentBufferConfig(extAdaptiveBufConfig);

            // Set the adaptive playback buffer's max bitrate cap.
            ExoAdaptivePlaybackBufferConfig adaptiveBufConfig =
                    new ExoAdaptivePlaybackBufferConfig();

            adaptiveBufConfig.mMaxBitrateCap = videoQualityValue;
            mPlayer.setContentBufferConfig(adaptiveBufConfig);
        }
    }

    /**
     * Reads the video quality type from settings and returns the corresponding video quality
     * value. Returns -1 if no specific value is set in the settings.
     *
     * @return video quality bit rate value, -1 if no specific value is set.
     */
    private int readVideoQualityValue() {

        int defaultVideoQualityType = VIDEO_QUALITY_BEST;
        // Try to retrieve the video quality setting from global settings.
        try {
            defaultVideoQualityType = getInt(mContext.getContentResolver(),
                                             VIDEO_QUALITY);
        }
        catch (Settings.SettingNotFoundException e) {
            Log.i(TAG, "Settings do not contain any video quality preferences");
        }
        // Set the bandwidth cap only if we get a valid cap on video quality, else let the player
        // use the default value.
        switch (defaultVideoQualityType) {
            case VIDEO_QUALITY_GOOD:
                return GOOD_BITRATE;
            case VIDEO_QUALITY_BETTER:
                return BETTER_BITRATE;
            default:
                return -1;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Bundle getExtra() {

        return mExtras;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addErrorListener(OnErrorListener onErrorListener) {

        mPlayer.addErrorListener(onErrorListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeErrorListener(OnErrorListener onErrorListener) {

        mPlayer.removeErrorListener(onErrorListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInfoListener(OnInfoListener onInfoListener) {

        mPlayer.addInfoListener(onInfoListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInfoListener(OnInfoListener onInfoListener) {

        mPlayer.removeInfoListener(onInfoListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStateChangeListener(OnStateChangeListener onStateChangeListener) {

        mPlayer.addStateChangeListener(onStateChangeListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeStateChangeListener(OnStateChangeListener onStateChangeListener) {

        mPlayer.removeStateChangeListener(onStateChangeListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHlsAdaptiveSwitchMode(AdaptiveSwitchMode adaptiveSwitchMode) {

        mPlayer.setHlsAdaptiveSwitchMode(adaptiveSwitchMode);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentBufferConfig(BaseContentPlaybackBufferConfig
                                               baseContentPlaybackBufferConfig) {

        mPlayer.setContentBufferConfig(baseContentPlaybackBufferConfig);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(ContentParameters contentParameters) {

        mPlayer.open(contentParameters);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCuesListener(OnCuesListener cuesListener) {

        mPlayer.addCuesListener(cuesListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCuesListener(OnCuesListener cuesListener) {

        mPlayer.removeCuesListener(cuesListener);
    }

    @Override
    public void addId3MetadataListener(Id3MetadataListener id3MetadataListener) {

    }

    @Override
    public void removeId3MetadataListener(Id3MetadataListener id3MetadataListener) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableTextTrack(TrackType trackType, boolean b) {

        mPlayer.enableTextTrack(trackType, b);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getTrackCount(TrackType trackType) {

        return mPlayer.getTrackCount(trackType);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public MediaFormat getTrackFormat(TrackType trackType, int i) {

        return mPlayer.getTrackFormat(trackType, i);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public void setSelectedTrack(TrackType trackType, int i) {

        mPlayer.setSelectedTrack(trackType, i);
    }

    /*
     * {@inheritDoc}
     */
    @Override
    public int getSelectedTrack(TrackType trackType) {

        return mPlayer.getSelectedTrack(trackType);
    }

    /*
     * TODO: Not implemented yet DEVTECH-2280
     */
    @Override
    public SeekRange getCurrentSeekRange(SeekRange seekRange) {

        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartingBitrate(int i) {

        mPlayer.setStartingBitrate(i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoWidth() {

        return mPlayer.getCurrentVideoWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoHeight() {

        return mPlayer.getCurrentVideoHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getCurrentAspectRatio() {

        return mPlayer.getCurrentAspectRatio();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserAgent(String s) {

        mPlayer.setUserAgent(s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setSurface(Surface surface, boolean b) {

        return mPlayer.setSurface(surface, b);
    }

    /*
     * TODO: Not implemented yet DEVTECH-2280
     */
    @Override
    public void setVideoSurfaceContainerView(View view) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare() {

        mPlayer.prepare();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play() {

        mPlayer.play();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() {

        mPlayer.pause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekTo(long l) {

        mPlayer.seekTo(l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {

        mPlayer.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

        mPlayer.close();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {

        mPlayer.release();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerState getPlayerState() {

        return mPlayer.getPlayerState();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentPosition() {

        return mPlayer.getCurrentPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {

        return mPlayer.getDuration();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBufferedPercentage() {

        return mPlayer.getBufferedPercentage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVolume(float v) {

        mPlayer.setVolume(v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRendererTrackMetric(TrackType trackType,
                                      RendererTrackMetric rendererTrackMetric) {

        return mPlayer.getRendererTrackMetric(trackType, rendererTrackMetric);
    }
}
