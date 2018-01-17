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
package com.amazon.mediaplayer.glue.brightcove;

import com.google.android.exoplayer.ExoPlayer;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.Surface;
import android.view.View;
import android.widget.FrameLayout;

import com.amazon.android.uamp.UAMP;
import com.amazon.mediaplayer.AMZNMediaPlayer;
import com.amazon.mediaplayer.playback.SeekRange;
import com.amazon.mediaplayer.playback.config.BaseContentPlaybackBufferConfig;
import com.amazon.mediaplayer.tracks.MediaFormat;
import com.amazon.mediaplayer.tracks.TrackType;
import com.brightcove.player.captioning.BrightcoveCaptionFormat;
import com.brightcove.player.display.VideoDisplayComponent;
import com.brightcove.player.event.Default;
import com.brightcove.player.display.ExoPlayerVideoDisplayComponent;
import com.brightcove.player.event.Event;
import com.brightcove.player.event.EventEmitter;
import com.brightcove.player.event.EventListener;
import com.brightcove.player.mediacontroller.BrightcoveMediaController;
import com.brightcove.player.model.Video;
import com.brightcove.player.view.BrightcoveClosedCaptioningView;

import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

/**
 * BrightCovePlayer glue which implements Amazon media player interface.
 */
public class BrightCovePlayer implements UAMP {

    final static String IMPL_CREATOR_NAME = BrightCovePlayer.class.getSimpleName();

    /**
     * Debug TAG.
     */
    private final static String TAG = BrightCovePlayer.class.getSimpleName();

    /**
     * Debug enable/disable flag.
     */
    private final static boolean DEBUG = true;

    /**
     * Context.
     */
    private Context mContext;

    /**
     * BrightCove player view ends up in this layout.
     */
    private FrameLayout mVideoFrameLayout;

    /**
     * BrightCove player view.
     */
    private BrightCoveViewInflaterHelper mBCVideoView;

    /**
     * BrightCove's event emitter.
     */
    private EventEmitter mEventEmitter;

    /**
     * Ads and Video info bundle.
     */
    private Bundle mExtras;

    /**
     * Called when player state changes.
     */
    private OnStateChangeListener mOnStateChangeListener;

    /**
     * Called when player state changes.
     */
    private OnErrorListener mOnErrorListener;

    /**
     * Track CC state internally.
     */
    private boolean mIsCCEnabled = false;

    /**
     * BrightCove player's prev state.
     */
    private AMZNMediaPlayer.PlayerState mPrevState = PlayerState.IDLE;

    /**
     * BrightCove player's current state.
     */
    private AMZNMediaPlayer.PlayerState mCurrentState = PlayerState.IDLE;

    /**
     * Current video duration.
     */
    private long mDuration = -1;

    /**
     * Identifier for closed caption track.
     */
    private static int CLOSED_CAPTION_INT = 2;

    /**
     * Identifier for subtitle track.
     */
    private static int SUBTITLE_INT = 3;

    /**
     * Set player state.
     *
     * @param state Player state defined by AMZNMediaPlayer.
     */
    private void setPlayerState(AMZNMediaPlayer.PlayerState state) {

        if (DEBUG) Log.v(TAG, "setPlayerState:" + state);
        mPrevState = mCurrentState;
        mCurrentState = state;

        if (mOnStateChangeListener != null) {
            Activity activity = (Activity) mContext;
            activity.runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    mOnStateChangeListener.onPlayerStateChange(mPrevState, mCurrentState, null);
                }
            });
        }
    }

    /**
     * Set event listeners for events received from Brightcove.
     */
    private void setEventListeners() {

        // This will be called after BrightCove starts content playback.
        mEventEmitter.on("didPlay", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@DID_PLAY@@@");
                if(event != null) {
                    mDuration = event.getIntegerProperty("duration");
                }
                setPlayerState(PlayerState.PLAYING);
                toggleCC();
            }
        });

        // This will be called after BrightCove stops content playback.
        mEventEmitter.on("didStop", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@DID_STOP@@@");
                setPlayerState(PlayerState.CLOSING);
                setPlayerState(PlayerState.IDLE);
            }
        });

        // This will be called after a content pause.
        mEventEmitter.on("didPause", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@DID_PAUSE@@@");
                setPlayerState(PlayerState.READY);
            }
        });

        // This will be called after content seeks to a point.
        mEventEmitter.on("didSeekTo", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@DID_SEEK_TO@@@");
                setPlayerState(PlayerState.PLAYING);
            }
        });

        // This will be called after content completion.
        mEventEmitter.on("completed", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@COMPLETED@@@");
                // BrightCove ExoPlayer impl seeks to zero and do pause.
                setPlayerState(PlayerState.ENDED);
            }
        });

        // This will be called when player is ready to play.
        mEventEmitter.on("readyToPlay", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@READY_TO_PLAY@@@");
                setPlayerState(PlayerState.READY);
            }
        });

        // This will be called after video duration changed.
        mEventEmitter.on("videoDurationChanged", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@VIDEO_DURATION_CHANGED@@@");
                if(event != null) {
                    mDuration = event.getIntegerProperty("duration");
                }
            }
        });

        // This will be called after buffering started.
        mEventEmitter.on("bufferingStarted", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@Buffering start event@@@");
                setPlayerState(PlayerState.BUFFERING);
            }
        });

        // This will be called after buffering end.
        mEventEmitter.on("bufferingCompleted", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@Buffering end event@@@");
                if (mBCVideoView.isPlaying()) {
                    setPlayerState(PlayerState.PLAYING);
                }
                else {
                    setPlayerState(PlayerState.READY);
                }
            }
        });

        // This will be called when error occurred during playback.
        mEventEmitter.on("error", new EventListener() {
            @Default
            public void processEvent(Event event) {

                if (DEBUG) Log.v(TAG, "@@@Error@@@");

                if(DEBUG && event != null) {
                    Map<String, Object> properties = event.properties;
                    if (properties != null && properties.containsKey("errorMessage")) {
                        Log.v(TAG, "Received following error message " + properties.get
                                ("errorMessage"));
                    }
                }
                setPlayerState(PlayerState.ERROR);
                if (mOnErrorListener != null) {
                    //As no error type is being received in error properties, setting player
                    // error as default.
                    AMZNMediaPlayer.Error e = new AMZNMediaPlayer.Error(AMZNMediaPlayer.ErrorType
                                                                                .PLAYER_ERROR);
                    mOnErrorListener.onError(e);
                }
            }
        });
    }

    /**
     * Check if current context is an activity context.
     *
     * @param context Context provided by the caller.
     */
    private boolean isActivityContext(Context context) {

        return context instanceof Activity;
    }

    /**
     * Init BrightCove media player.
     *
     * @param context     Context.
     * @param frameLayout FrameLayout for the BrightCove player view.
     * @param extras      Extra bundle for Ads and video info.
     */
    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extras) {

        if (DEBUG) Log.v(TAG, "Create instance");

        if(!isActivityContext(context)) {

            throw new ClassCastException("Context must be an activity context");
        }

        mContext = context;
        mVideoFrameLayout = frameLayout;
        mExtras = extras;
        // With the helper class we can create BrightCoveView correctly.
        mBCVideoView = new BrightCoveViewInflaterHelper(mContext);

        // Disable focuses.
        mBCVideoView.setClickable(false);
        mBCVideoView.setFocusable(false);
        mBCVideoView.setFocusableInTouchMode(false);

        // Finish the initialization of video view.
        mBCVideoView.finishInflate();
        // Event Emitter lets you send and receive events from BrightCove framework/plugins.
        mEventEmitter = mBCVideoView.getEventEmitter();
        // We don't use BrightCove's media controller.
        mBCVideoView.setMediaController((BrightcoveMediaController) null);

        // Add video view to the frame layout which is supplied from outside.
        mVideoFrameLayout.addView(mBCVideoView);
        mVideoFrameLayout.requestLayout();

        //Set Event listeners
        setEventListeners();
    }

    /**
     * Default constructor.
     */
    public BrightCovePlayer() {

        if (DEBUG) Log.v(TAG, "BrightCovePlayer");
    }

    /**
     * BrightCove handles Ads internally.
     *
     * @return True
     */
    @Override
    public boolean canRenderAds() {

        return false;
    }

    /**
     * BrightCove's CC is depending on CaptioningManager.
     * http://developer.android.com/reference/android/view/accessibility/CaptioningManager.html
     *
     * @return Can render CC?
     */
    @Override
    public boolean canRenderCC() {

        return true;
    }

    @Override
    public void detachSurfaceView() {
        // Does not apply to BrightCove.
    }

    @Override
    public void attachSurfaceView() {
        // Does not apply to BrightCove.
    }

    /**
     * Toggle CC rendering.
     */
    private void toggleCC() {

        if (mBCVideoView != null &&
                mBCVideoView.getClosedCaptioningView() != null) {
            if (mIsCCEnabled) {
                mBCVideoView.getClosedCaptioningView().setMode(BrightcoveClosedCaptioningView
                                                                       .ClosedCaptioningMode.ON);
            }
            else {
                mBCVideoView.getClosedCaptioningView().setMode(BrightcoveClosedCaptioningView
                                                                       .ClosedCaptioningMode.OFF);
                mBCVideoView.getClosedCaptioningView().clear();
            }
        }
    }

    @Override
    public void enableTextTrack(TrackType trackType, boolean isCCEnabled) {
        // Enable/Disable CC.
        if (trackType == TrackType.SUBTITLE || trackType == TrackType.CLOSED_CAPTION) {
            mIsCCEnabled = isCCEnabled;
            toggleCC();
        }
    }

    /**
     * Get corresponding numeric value for Android Media Player Interface track type.
     *
     * @param trackType trackType.
     * @return numeric track value.
     */
    private int getTrackValue(TrackType trackType) {

        switch (trackType) {
            case CLOSED_CAPTION:
                return CLOSED_CAPTION_INT;
            case SUBTITLE:
                return SUBTITLE_INT;
        }
        return -1;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getTrackCount(TrackType trackType) {

        if (DEBUG) Log.v(TAG, "getTrackCount");
        VideoDisplayComponent videoDisplayComponent = mBCVideoView.getVideoDisplay();
        if (videoDisplayComponent != null && videoDisplayComponent instanceof
                ExoPlayerVideoDisplayComponent) {
            ExoPlayerVideoDisplayComponent exoVideoDisplayComponent =
                    (ExoPlayerVideoDisplayComponent) videoDisplayComponent;
            ExoPlayer exoPlayer = exoVideoDisplayComponent.getExoPlayer();
            if (exoPlayer != null) {
                return exoPlayer.getTrackCount(getTrackValue(trackType));
            }
        }
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public MediaFormat getTrackFormat(TrackType trackType, int i) {

        if (DEBUG) Log.v(TAG, "getTrackFormat");
        return null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setSelectedTrack(TrackType trackType, int i) {

        if (DEBUG) Log.v(TAG, "setSelectedTrack");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getSelectedTrack(TrackType trackType) {

        if (DEBUG) Log.v(TAG, "getSelectedTrack");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public SeekRange getCurrentSeekRange(SeekRange seekRange) {

        if (DEBUG) Log.v(TAG, "getCurrentSeekRange");
        return null;
    }

    @Override
    public void removeCuesListener(OnCuesListener onCuesListener) {
        // BrightCove renders CC internally.
        if (DEBUG) Log.v(TAG, "removeCuesListener");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addId3MetadataListener(Id3MetadataListener id3MetadataListener) {

        if (DEBUG) Log.v(TAG, "addId3MetadataListener");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeId3MetadataListener(Id3MetadataListener id3MetadataListener) {

        if (DEBUG) Log.v(TAG, "removeId3MetadataListener");
    }

    @Override
    public void addCuesListener(OnCuesListener onCuesListener) {
        // BrightCove renders CC internally.
        if (DEBUG) Log.v(TAG, "addCuesListener");
    }


    /**
     * TODO: Check if this function is needed anymore during Video Quality implementation else
     * remove. DEVTECH-4979
     */
    public void setContentBufferConfig(
            BaseContentPlaybackBufferConfig.PlaybackContentBufferConfigType
                    playbackContentBufferConfigType,
            BaseContentPlaybackBufferConfig baseContentPlaybackBufferConfig) {
        // Does not apply to BrightCove.
        if (DEBUG) Log.v(TAG, "setContentBufferConfig");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addErrorListener(OnErrorListener onErrorListener) {

        if (DEBUG) Log.v(TAG, "addErrorListener");
        mOnErrorListener = onErrorListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeErrorListener(OnErrorListener onErrorListener) {

        if (DEBUG) Log.v(TAG, "removeErrorListener");
        mOnErrorListener = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addInfoListener(OnInfoListener onInfoListener) {

        if (DEBUG) Log.v(TAG, "addInfoListener");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeInfoListener(OnInfoListener onInfoListener) {

        if (DEBUG) Log.v(TAG, "removeInfoListener");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStateChangeListener(OnStateChangeListener onStateChangeListener) {

        if (DEBUG) Log.v(TAG, "addStateChangeListener");
        mOnStateChangeListener = onStateChangeListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeStateChangeListener(OnStateChangeListener onStateChangeListener) {

        if (DEBUG) Log.v(TAG, "removeStateChangeListener");
        mOnStateChangeListener = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setHlsAdaptiveSwitchMode(AdaptiveSwitchMode adaptiveSwitchMode) {

        if (DEBUG) Log.v(TAG, "setHlsAdaptiveSwitchMode");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setContentBufferConfig(BaseContentPlaybackBufferConfig
                                               baseContentPlaybackBufferConfig) {

        if (DEBUG) Log.v(TAG, "setContentBufferConfig");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(ContentParameters contentParameters) {

        if (DEBUG) Log.v(TAG, "open");
        open(contentParameters.url, contentParameters.mimeType, contentParameters.laurl,
             contentParameters.oobTextSources);
    }

    /**
     * {@inheritDoc}
     */
    public void open(String s, ContentMimeType contentMimeType) {

        if (DEBUG) Log.v(TAG, "open:" + s);
        Video v = Video.createVideo(s);
        mBCVideoView.add(v);
        setPlayerState(PlayerState.OPENING);
        setPlayerState(PlayerState.OPENED);
    }

    /**
     * {@inheritDoc}
     */
    public void open(String s, ContentMimeType contentMimeType, String s1) {

        open(s, contentMimeType);
    }

    public void open(String s, ContentMimeType contentMimeType, String s1, OutOfBandTextSource[]
            outOfBandTextSources) {
        // Add subtitles.
        for (int i = 0; outOfBandTextSources != null && i < outOfBandTextSources.length; i++) {
            String mimeType = "";
            if (outOfBandTextSources[i].mTextSourceMime != TextMimeType.TEXT_WTT &&
                    outOfBandTextSources[i].mTextSourceMime != TextMimeType.TEXT_TTML) {
                continue;
            }

            if (outOfBandTextSources[i].mTextSourceMime == TextMimeType.TEXT_WTT) {
                mimeType = "text/vtt";
            }
            else if (outOfBandTextSources[i].mTextSourceMime != TextMimeType.TEXT_TTML) {
                mimeType = "application/ttml+xml";
            }

            BrightcoveCaptionFormat brightcoveCaptionFormat = BrightcoveCaptionFormat
                    .createCaptionFormat(mimeType, outOfBandTextSources[i].mTextSourceLanguage);
            mBCVideoView.addSubtitleSource(Uri.parse(outOfBandTextSources[i].mOObcaptionsUrl),
                                           brightcoveCaptionFormat);
        }
        // Disable it initially.
        if (mBCVideoView != null &&
                mBCVideoView.getClosedCaptioningView() != null) {
            mBCVideoView.getClosedCaptioningView().setMode(BrightcoveClosedCaptioningView
                                                                   .ClosedCaptioningMode.OFF);
        }
        open(s, contentMimeType);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartingBitrate(int i) {

        if (DEBUG) Log.v(TAG, "setStartingBitrate:" + i);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoWidth() {

        if (DEBUG) Log.v(TAG, "getCurrentVideoWidth:" + mBCVideoView.getVideoWidth());
        return mBCVideoView.getVideoWidth();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoHeight() {

        if (DEBUG) Log.v(TAG, "getCurrentVideoHeight:" + mBCVideoView.getVideoHeight());
        return mBCVideoView.getVideoHeight();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getCurrentAspectRatio() {

        if (DEBUG) Log.v(TAG, "getCurrentAspectRatio");
        return 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserAgent(String s) {

        if (DEBUG) Log.v(TAG, "setUserAgent:" + s);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setSurface(Surface surface, boolean b) {

        if (DEBUG) Log.v(TAG, "setSurface:" + surface.toString() + " b:" + b);
        // No use for BrightCove player.
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVideoSurfaceContainerView(View view) {

        if (DEBUG) Log.v(TAG, "setVideoSurfaceContainerView");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare() {

        if (DEBUG) Log.v(TAG, "prepare");
        setPlayerState(PlayerState.PREPARING);
        setPlayerState(PlayerState.READY);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play() {

        if (DEBUG) Log.v(TAG, "play");
        mBCVideoView.start();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() {

        if (DEBUG) Log.v(TAG, "pause");
        if (mBCVideoView.isPlaying()) {
            mBCVideoView.pause();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekTo(long l) {

        Log.v(TAG, "seekTo:" + l);
        setPlayerState(PlayerState.SEEKING);
        // Convert long timestamp to int as BrightCove requires int.
        mBCVideoView.seekTo((int) l);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {

        if (DEBUG) Log.v(TAG, "stop");
        mBCVideoView.clear();
        mBCVideoView.stopPlayback();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {

        if (DEBUG) Log.v(TAG, "close");

        mBCVideoView.clear();
        mBCVideoView.stopPlayback();

        // BrightCove might have a bug, not sending DidStop event when contents end by itself.
        if (mCurrentState == PlayerState.ENDED) {
            Timer t = new Timer();
            t.schedule(new TimerTask() {
                @Override
                public void run() {

                    setPlayerState(PlayerState.CLOSING);
                    setPlayerState(PlayerState.IDLE);
                }
            }, 2000);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {

        if (DEBUG) Log.v(TAG, "release");
        mVideoFrameLayout.removeView(mBCVideoView);
        mBCVideoView = null;
        mContext = null;
        mPrevState = PlayerState.IDLE;
        mCurrentState = PlayerState.IDLE;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerState getPlayerState() {

        if (DEBUG) Log.v(TAG, "getPlayerState");
        AMZNMediaPlayer.PlayerState state = mCurrentState;
        if (mBCVideoView.isPlaying()) {
            state = PlayerState.PLAYING;
        }

        return state;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentPosition() {

        if (DEBUG) Log.v(TAG, "getCurrentPosition:" + mBCVideoView.getCurrentPosition());
        return mBCVideoView.getCurrentPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {

        if (mDuration == -1) {
            mDuration = mBCVideoView.getDuration();
        }
        if (DEBUG) Log.v(TAG, "getDuration:" + mDuration);
        return mDuration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBufferedPercentage() {

        if (DEBUG) Log.v(TAG, "getBufferedPercentage " + mBCVideoView.getBufferPercentage());
        return mBCVideoView.getBufferPercentage();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVolume(float v) {

        if (DEBUG) Log.v(TAG, "setVolume:" + v);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getRendererTrackMetric(TrackType trackType, RendererTrackMetric
            rendererTrackMetric) {

        if (DEBUG)
            Log.v(TAG, "getRendererTrackMetric:" + trackType.name() + " rendererTrackMetric:" +
                    rendererTrackMetric.name());
        return 0;
    }

    /**
     * Get extra bundle of BrightCove player.
     *
     * @return Bundle for Ads and video info.
     */
    @Override
    public Bundle getExtra() {

        return mExtras;
    }
}
