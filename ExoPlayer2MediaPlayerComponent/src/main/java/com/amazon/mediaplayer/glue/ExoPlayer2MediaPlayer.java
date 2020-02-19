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

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.FrameLayout;

import com.amazon.android.uamp.UAMP;
import com.amazon.mediaplayer.AMZNMediaPlayer;
import com.amazon.mediaplayer.playback.SeekRange;
import com.amazon.mediaplayer.playback.config.BaseContentPlaybackBufferConfig;
import com.amazon.mediaplayer.tracks.MediaFormat;
import com.amazon.mediaplayer.tracks.TrackType;
import com.google.android.exoplayer.smoothstreaming.SmoothStreamingChunkSource;
import com.google.android.exoplayer2.C;
import com.google.android.exoplayer2.ExoPlaybackException;
import com.google.android.exoplayer2.ExoPlayerFactory;
import com.google.android.exoplayer2.Format;
import com.google.android.exoplayer2.PlaybackParameters;
import com.google.android.exoplayer2.Player;
import com.google.android.exoplayer2.Player.DiscontinuityReason;
import com.google.android.exoplayer2.RenderersFactory;
import com.google.android.exoplayer2.DefaultRenderersFactory;
import com.google.android.exoplayer2.SimpleExoPlayer;
import com.google.android.exoplayer2.Timeline;
import com.google.android.exoplayer2.drm.DefaultDrmSessionManager;
import com.google.android.exoplayer2.drm.FrameworkMediaCrypto;
import com.google.android.exoplayer2.source.MediaSource;
import com.google.android.exoplayer2.source.ExtractorMediaSource;
import com.google.android.exoplayer2.source.TrackGroup;
import com.google.android.exoplayer2.source.TrackGroupArray;
import com.google.android.exoplayer2.source.dash.DashChunkSource;
import com.google.android.exoplayer2.source.dash.DashMediaSource;
import com.google.android.exoplayer2.source.dash.DefaultDashChunkSource;
import com.google.android.exoplayer2.source.hls.HlsMediaSource;
import com.google.android.exoplayer2.source.smoothstreaming.DefaultSsChunkSource;
import com.google.android.exoplayer2.source.smoothstreaming.SsMediaSource;
import com.google.android.exoplayer2.text.Cue;
import com.google.android.exoplayer2.text.TextOutput;
import com.google.android.exoplayer2.trackselection.AdaptiveTrackSelection;
import com.google.android.exoplayer2.trackselection.DefaultTrackSelector;
import com.google.android.exoplayer2.trackselection.MappingTrackSelector;
import com.google.android.exoplayer2.trackselection.TrackSelection;
import com.google.android.exoplayer2.trackselection.TrackSelectionArray;
import com.google.android.exoplayer2.upstream.BandwidthMeter;
import com.google.android.exoplayer2.upstream.DataSource;
import com.google.android.exoplayer2.upstream.DefaultBandwidthMeter;
import com.google.android.exoplayer2.upstream.DefaultDataSourceFactory;
import com.google.android.exoplayer2.upstream.DefaultHttpDataSourceFactory;
import com.google.android.exoplayer2.upstream.HttpDataSource;
import com.google.android.exoplayer2.upstream.TransferListener;
import com.google.android.exoplayer2.util.EventLogger;
import com.google.android.exoplayer2.util.Util;
import com.google.android.exoplayer2.video.VideoListener;

import java.util.ArrayList;
import java.util.EventListener;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static android.provider.Settings.Global.getInt;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_BEST;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_BETTER;
import static com.amazon.tv.Settings.Global.VIDEO_QUALITY_GOOD;

/**
 * Amazon Media Player glue class.
 */
public class ExoPlayer2MediaPlayer implements UAMP, SurfaceHolder.Callback, EventListener, VideoListener, TextOutput {

    /**
     * Debug TAG.
     */
    private static final String TAG = ExoPlayer2MediaPlayer.class.getSimpleName();

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
     * Pass through bundle.
     */
    private Bundle mExtras;

    /**
     * Internal reference to FrameLayout.
     */
    private FrameLayout mFrameLayout;

    private SimpleExoPlayer mPlayer;
    private DataSource.Factory mDataSourceFactory;
    private MediaSource mCurrentMediaSource;
    private PlayerState mPlayerState;
    private int mVideoWidth = -1;
    private int mVideoHeight = -1;
    private float mVideoAspect = 1.0f;
    private DefaultTrackSelector mTrackSelector;
    private TrackSelection.Factory mTrackSelectionFactory;
    private Timeline mCurrentTimeline;

    private Set<OnErrorListener> mErrorListeners;
    private Set<OnStateChangeListener> mStateListeners;
    private Set<OnCuesListener> mCuesListeners;
    private Set<OnInfoListener> mInfoListeners;

    /**
     * Static bandwidth meter so that we get a universal view from all transfers.
     */
    private static final DefaultBandwidthMeter BANDWIDTH_METER = new DefaultBandwidthMeter();

    private Handler mMediaSourceHandler;
    private EventLogger eventLogger;



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

    public ExoPlayer2MediaPlayer() {
        super();
        mErrorListeners = new HashSet<>();
        mStateListeners = new HashSet<>();
        mCuesListeners = new HashSet<>();
        mInfoListeners = new HashSet<>();
        mMediaSourceHandler = new Handler();
    }

    protected String mUserAgent;

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
        Log.d(TAG, "attachSurfaceView");
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
        Log.d(TAG, "detachSurfaceView");
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
        Log.v(TAG, "init");
        mContext = context;
        mFrameLayout = frameLayout;
        mExtras = extras;
        mUserAgent = "CustomExoPlayer";

        mDataSourceFactory = buildDataSourceFactory(true);

        /*
         * AdaptiveTrackSelection must be driven by the same instance of
         * DefaultBandwidthMeter that is set to listen to media source
         */
        mTrackSelectionFactory = new AdaptiveTrackSelection.Factory(BANDWIDTH_METER);
        mTrackSelector = new DefaultTrackSelector(mTrackSelectionFactory);
        eventLogger = new EventLogger(mTrackSelector);

        mPlayer = ExoPlayerFactory.newSimpleInstance(mContext, mTrackSelector);
        mPlayer.addListener(eventLogger);
        mPlayer.addListener(new PlayerEventListener());
        mPlayer.addVideoListener(this);
        mPlayer.addTextOutput(this);
        mPlayer.addMetadataOutput(eventLogger);
        mPlayer.addAudioDebugListener(eventLogger);
        mPlayer.addVideoDebugListener(eventLogger);
        setVideoQuality();

        Player.VideoComponent newVideoComponent = mPlayer.getVideoComponent();
        mSurfaceView = new SurfaceView(mContext);
        mSurfaceView.getHolder().addCallback(this);
        mFrameLayout.addView(mSurfaceView);
        mSurfaceViewAttached = true;
        mPlayer.setVideoSurfaceView(mSurfaceView);
        mPlayerState = PlayerState.IDLE;
    }

    @Override
    public void onCues(List<Cue> cues) {
        Log.v(TAG, "onCues");
        if (cues != null) {
            List<com.amazon.mediaplayer.playback.text.Cue> amznCues = new ArrayList<com.amazon.mediaplayer.playback.text.Cue>();
            for (Cue cue : cues) {
                com.amazon.mediaplayer.playback.text.Cue amznCue = new com.amazon.mediaplayer.playback.text.Cue(
                    cue.text,
                    cue.textAlignment,
                    cue.line,
                    cue.lineType,
                    cue.lineAnchor,
                    cue.position,
                    cue.positionAnchor,
                    cue.size
                );
                amznCues.add(amznCue);
            }
            for (OnCuesListener cuesListener : mCuesListeners) {
                cuesListener.onCues(amznCues);
            }
        }
    }

    /**
     * Returns a new DataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new DataSource factory.
     */
    private DataSource.Factory buildDataSourceFactory(boolean useBandwidthMeter) {
        TransferListener<? super DataSource> listener = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultDataSourceFactory(mContext, listener,
                buildHttpDataSourceFactory(useBandwidthMeter));
    }

    /**
     * Returns a new HttpDataSource factory.
     *
     * @param useBandwidthMeter Whether to set {@link #BANDWIDTH_METER} as a listener to the new
     *     DataSource factory.
     * @return A new HttpDataSource factory.
     */
    private HttpDataSource.Factory buildHttpDataSourceFactory(boolean useBandwidthMeter) {
        TransferListener<? super DataSource> listener = useBandwidthMeter ? BANDWIDTH_METER : null;
        return new DefaultHttpDataSourceFactory(mUserAgent, listener);
    }

    private class PlayerEventListener implements Player.EventListener {

        @Override
        public void onPlayerStateChanged(boolean playWhenReady, int playbackState) {
            Log.v(TAG, "onPlayerStateChanged " + playbackState);
            // Convert ExoPlayer states to AMZNPlayer states
            switch (playbackState) {
                case Player.STATE_IDLE:
                    Log.v(TAG, "STATE_IDLE");
                    setPlayerState(PlayerState.IDLE);
                    break;

                case Player.STATE_BUFFERING:
                    Log.v(TAG, "STATE_BUFFERING");
                    setPlayerState(PlayerState.BUFFERING);
                    break;

                case Player.STATE_READY:
                    Log.v(TAG, "STATE_READY");
                    if (playWhenReady) {
                        setPlayerState(PlayerState.PLAYING);
                    }
                    else {
                        setPlayerState(PlayerState.READY);
                    }
                    break;

                case Player.STATE_ENDED:
                    Log.v(TAG, "STATE_ENDED");
                    setPlayerState(PlayerState.ENDED);
                    break;
           }
        }

        @Override
        public void onRepeatModeChanged(int repeatMode) {
            Log.v(TAG, "onRepeatModeChanged");
        }

        @Override
        public void onShuffleModeEnabledChanged(boolean shuffleModeEnabled) {
            Log.v(TAG, "onShuffleModeEnabledChanged");
        }

        @Override
        public void onPlayerError(ExoPlaybackException e) {
            Log.v(TAG, "onPlayerError");
            setPlayerState(PlayerState.ERROR);
            for (OnErrorListener errorListener : mErrorListeners) {
                errorListener.onError(new Error(ErrorType.PLAYER_ERROR, e, null));
            }
        }

        @Override
        public void onPositionDiscontinuity(int reason) {
            Log.v(TAG, "onPositionDiscontinuity " + reason);
        }

        @Override
        public void onPlaybackParametersChanged(PlaybackParameters playbackParameters) {
            Log.v(TAG, "onPlaybackParametersChanged");
        }

        @Override
        public void onSeekProcessed() {
            Log.v(TAG, "onSeekProcessed");
        }

        @Override
        public void onTimelineChanged(Timeline timeline, Object manifest, int reason) {
            Log.v(TAG, "onTimelineChanged ");
            mCurrentTimeline = timeline;
        }

        @Override
        @SuppressWarnings("ReferenceEquality")
        public void onTracksChanged(TrackGroupArray trackGroups, TrackSelectionArray trackSelections) {
            Log.v(TAG, "onTracksChanged");
        }

        @Override
        public void onLoadingChanged(boolean isLoading) {
            Log.v(TAG, "onLoadingChanged");
        }
    }

    /**
     * Reads user settings for any bit rate caps set by user and sends that value to player.
     */
    private void setVideoQuality() {

        int videoQualityValue = readVideoQualityValue();
        // Set the bandwidth cap only if we get a valid cap on video quality, else let the player
        // use the default value.
        if (videoQualityValue != -1) {
            // TODO: set mMaxBitrateCap in ExoAdaptivePlaybackBufferConfig and
            //       ExoExtendedAdaptivePlaybackBufferConfig
            // Like:
            //      mPlayer.setContentBufferConfig(adaptiveBufConfig);
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
        Log.v(TAG, "addErrorListener");
        if (!mErrorListeners.contains(onErrorListener)) {
            mErrorListeners.add(onErrorListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeErrorListener(OnErrorListener onErrorListener) {
        Log.v(TAG, "removeErrorListener");
        mErrorListeners.remove(onErrorListener);
    }

    /**
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public void addInfoListener(OnInfoListener onInfoListener) {
        Log.v(TAG, "addInfoListener");
        if (!mInfoListeners.contains(onInfoListener)) {
            mInfoListeners.add(onInfoListener);
        }
    }

    /**
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public void removeInfoListener(OnInfoListener onInfoListener) {
        Log.v(TAG, "removeInfoListener");
        mInfoListeners.remove(onInfoListener);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addStateChangeListener(OnStateChangeListener onStateChangeListener) {
        Log.v(TAG, "addStateChangeListener");
        if (!mStateListeners.contains(onStateChangeListener)) {
            mStateListeners.add(onStateChangeListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeStateChangeListener(OnStateChangeListener onStateChangeListener) {
        Log.v(TAG, "removeStateChangeListener");
        mStateListeners.remove(onStateChangeListener);
    }

    /**
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public void setHlsAdaptiveSwitchMode(AdaptiveSwitchMode adaptiveSwitchMode) {
        Log.w(TAG, "setHlsAdaptiveSwitchMode not implemented");
    }

    /**
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public void setContentBufferConfig(BaseContentPlaybackBufferConfig
                                               baseContentPlaybackBufferConfig) {
        Log.w(TAG, "setContentBufferConfig not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void open(ContentParameters contentParameters) {
        Log.v(TAG, "open " + contentParameters.url);
        mPlayer.setPlayWhenReady(true);

        if (contentParameters.url == null || contentParameters.url.isEmpty()) {
            Log.w(TAG, "Invalid or missing URL in ContentParameters");
            return;
        }

        Uri url = Uri.parse(contentParameters.url);

        int mediaSourceType = -1;

        if (contentParameters.mimeType != null) {
            // We have a mime type passed in so try to use it
            switch (contentParameters.mimeType) {
                case CONTENT_DASH:
                    mediaSourceType = C.TYPE_DASH;
                    break;

                case CONTENT_HLS:
                    mediaSourceType = C.TYPE_HLS;
                    break;

                case CONTENT_MP4:
                case CONTENT_M4A:
                case CONTENT_MKV:
                case CONTENT_WEBM:
                case CONTENT_OGG:
                case CONTENT_MP3:
                case CONTENT_AAC:
                case CONTENT_TS:
                case CONTENT_FLV:
                case CONTENT_WAV:
                    mediaSourceType = C.TYPE_OTHER;
                    break;

                case CONTENT_SMOOTH_STREAMING:
                    mediaSourceType = C.TYPE_SS;
                    break;

                case CONTENT_TYPE_UNKNOWN:
                default:
                    // no-op to try inferring from uri
                    break;
            }
        }

        // Parse the URI to try to determine the type.
        // NOTE: inferContentType returns C.TYPE_OTHER if no matching signatures found
        if (mediaSourceType == -1) {
            mediaSourceType = Util.inferContentType(Uri.parse(contentParameters.url));
        }

        // Build the appropriate MediaSource
        MediaSource mediaSource;
        DataSource.Factory manifestDataSourceFactory =
                new DefaultHttpDataSourceFactory(mUserAgent);

        switch (mediaSourceType) {
            case C.TYPE_DASH:
                mediaSource = new DashMediaSource.Factory(
                        new DefaultDashChunkSource.Factory(mDataSourceFactory),
                        buildDataSourceFactory(true))
                        .createMediaSource(url, mMediaSourceHandler, eventLogger);
                break;

            case C.TYPE_SS:
                mediaSource = new SsMediaSource.Factory(
                        new DefaultSsChunkSource.Factory(mDataSourceFactory),
                        buildDataSourceFactory(true))
                        .createMediaSource(url, mMediaSourceHandler, eventLogger);
                break;

            case C.TYPE_HLS:
                mediaSource = new HlsMediaSource.Factory(mDataSourceFactory)
                        .createMediaSource(url, mMediaSourceHandler, eventLogger);
                break;

            case C.TYPE_OTHER:
            default:
                mediaSource = new ExtractorMediaSource.Factory(mDataSourceFactory)
                        .createMediaSource(url, mMediaSourceHandler, eventLogger);
        }

        setPlayerState(PlayerState.OPENING);
        if (mediaSource != null) {
            mCurrentMediaSource = mediaSource;
            setPlayerState(PlayerState.OPENED);
        }
        else {
            setPlayerState(PlayerState.ERROR);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void addCuesListener(OnCuesListener cuesListener) {
        Log.v(TAG, "addCuesListener");
        if (!mCuesListeners.contains(cuesListener)) {
            mCuesListeners.add(cuesListener);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void removeCuesListener(OnCuesListener cuesListener) {
        mCuesListeners.remove(cuesListener);
    }

    @Override
    public void addId3MetadataListener(Id3MetadataListener id3MetadataListener) {
        Log.w(TAG, "addId3MetadataListener not implemented");
    }

    @Override
    public void removeId3MetadataListener(Id3MetadataListener id3MetadataListener) {
        Log.w(TAG, "removeId3MetadataListener not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void enableTextTrack(TrackType trackType, boolean b) {
        Log.v(TAG, "enableTextTrack " + b);
        int count = getTrackCount(trackType);
        if (count > 0 && (trackType == TrackType.SUBTITLE || trackType == TrackType.CLOSED_CAPTION)) {
            if (b) {
                int rendererIndex = getRendererIndexByTrackType(trackType);
                TrackGroupArray textGroups = mTrackSelector.getCurrentMappedTrackInfo().getTrackGroups(rendererIndex);
                mTrackSelector.setRendererDisabled(rendererIndex, false);
                MappingTrackSelector.SelectionOverride override = new MappingTrackSelector.SelectionOverride(mTrackSelectionFactory,0, 0);
                mTrackSelector.setSelectionOverride(rendererIndex, textGroups, override);
                mPlayer.addTextOutput(this);
            }
            else {
                int rendererIndex = getRendererIndexByTrackType(trackType);
                mTrackSelector.setRendererDisabled(rendererIndex, true);
                mPlayer.removeTextOutput(this);
            }
        }
    }


    /**
     * Helper to find the ExoPlayer TrackGroup by the TrackType
     * @param trackType
     * @return
     */
    private int getRendererIndexByTrackType(TrackType trackType) {
        boolean found = false;
        for (int i = 0; i < mPlayer.getRendererCount(); i++) {
            int type = mPlayer.getRendererType(i);
            if (type == C.TRACK_TYPE_AUDIO && trackType == TrackType.AUDIO) {
                return i;
            }
            else if (type == C.TRACK_TYPE_VIDEO && trackType == TrackType.VIDEO) {
                return i;
            }
            else if (type == C.TRACK_TYPE_TEXT && (trackType == TrackType.CLOSED_CAPTION || trackType == TrackType.SUBTITLE)) {
                return i;
            }
            else if (type == C.TRACK_TYPE_METADATA && trackType == TrackType.META_DATA) {
                return i;
            }
        }
        return -1;
    }
    /*
     * {@inheritDoc}
     */
    @Override
    public int getTrackCount(TrackType trackType) {
        Log.v(TAG, "getTrackCount");
        int trackCount = 0;
        int rendererIndex = getRendererIndexByTrackType(trackType);
        MappingTrackSelector.MappedTrackInfo mappedTrackInfo = mTrackSelector.getCurrentMappedTrackInfo();
        if (mappedTrackInfo != null) {
            TrackGroupArray groups = mappedTrackInfo.getTrackGroups(rendererIndex);
            if (groups != null) {
                // most likely just one TrackGroup and one Format here
                for (int i=0; i<groups.length; i++) {
                    TrackGroup group = groups.get(i);
                    if (group != null) {
                        for (int j=0; j<group.length; j++) {
                            Format format = group.getFormat(j);
                            if (trackType == TrackType.CLOSED_CAPTION && format.containerMimeType == "application/cea-608") {
                                trackCount++;
                            } else if (trackType == TrackType.SUBTITLE && format.containerMimeType != "application/cea-608") {
                                trackCount++;
                            }
                        }
                    }
                }
            }
        }
        return trackCount;
    }

    /*
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public MediaFormat getTrackFormat(TrackType trackType, int i) {
        return null;
    }

    /**
     * Track selection changed in ExoPlayer2.
     * See https://medium.com/google-exoplayer/exoplayer-2-x-track-selection-2b62ff712cc9
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public void setSelectedTrack(TrackType trackType, int i) {
        Log.w(TAG, "setSelectedTrack not implemented");
    }

    /*
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public int getSelectedTrack(TrackType trackType) {
        Log.w(TAG, "getSelectedTrack not implemented");
        return 0;
    }

    /*
     * NOTE: FAB PlaybackActivity does not use this
     */
    @Override
    public SeekRange getCurrentSeekRange(SeekRange seekRange) {
        Log.w(TAG, "getCurrentSeekRange not implemented");
        return new SeekRange();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setStartingBitrate(int i) {
        Log.w(TAG, "setStartingBitrate not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoWidth() {
        return mVideoWidth;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentVideoHeight() {
        return mVideoHeight;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public float getCurrentAspectRatio() {
        return mVideoAspect;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setUserAgent(String s) {
        mUserAgent = s;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setSurface(Surface surface, boolean b) {
        Log.w(TAG, "setSurface not implemented");
        mPlayer.setVideoSurface(surface);
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setVideoSurfaceContainerView(View view) {
        Log.w(TAG, "setVideoSurfaceContainerView not implemented");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void prepare() {
        Log.v(TAG, "prepare");
        if (mCurrentMediaSource != null) {
            mPlayer.prepare(mCurrentMediaSource, true, true);
            /* Calling prepare() causes a BUFFERING state change, but for PlaybackActivity
             * to operate correctly it MUST transition from PREPARING to READY directly.
             */
            setPlayerState(PlayerState.PREPARING);
            setPlayerState(PlayerState.READY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void play() {
        Log.v(TAG, "play");
        mPlayer.setPlayWhenReady(true);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void pause() {
        Log.v(TAG, "pause");
        mPlayer.setPlayWhenReady(false);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void seekTo(long l) {
        Log.v(TAG, "seekTo " + l);
        int windowIndex = mPlayer.getCurrentWindowIndex();
        long duration = mPlayer.getDuration();
        long seekPosition = l;
        if (duration != C.TIME_UNSET) {
            seekPosition = Math.min(seekPosition, duration);
        }
        seekPosition = Math.max(seekPosition, 0);
        setPlayerState(PlayerState.SEEKING);
        mPlayer.seekTo(windowIndex, seekPosition);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void stop() {
        Log.v(TAG, "stop");
        mPlayer.stop();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void close() {
        Log.v(TAG, "close");
        mPlayer.stop();
        mCurrentMediaSource = null;
        mCurrentTimeline = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void release() {
        Log.v(TAG, "release");
        mPlayer.stop();
        mPlayer.release();
        mCurrentMediaSource = null;
        mCurrentTimeline = null;
        setPlayerState(PlayerState.IDLE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public PlayerState getPlayerState() {
        return mPlayerState;
    }

    private void setPlayerState(final PlayerState newState) {
        Log.v(TAG, "setPlayerState " + newState.toString());
        final PlayerState oldState = mPlayerState;
        if (oldState != newState) {
            mPlayerState = newState;
            for (final OnStateChangeListener onStateChangeListener : mStateListeners) {
                Activity activity = (Activity) mContext;
                activity.runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        onStateChangeListener.onPlayerStateChange(oldState, newState, null);
                    }
                });
            }
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getCurrentPosition() {
        Log.v(TAG, "getCurrentPosition " + mPlayer.getCurrentPosition());
        return mPlayer.getCurrentPosition();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long getDuration() {
        Log.v(TAG, "getDuration " + mPlayer.getDuration());
        long duration = mPlayer.getDuration();
        if (duration != C.TIME_UNSET) {
            return duration;
        }
        else {
            return AMZNMediaPlayer.UNKNOWN_TIME_US;
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBufferedPercentage() {
        Log.v(TAG, "getBufferedPercentage " + mPlayer.getBufferedPercentage());
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
        Log.v(TAG, "getRendererTrackMetric not implemented");
        return -1;
    }

    @Override
    public void onVideoSizeChanged(int width, int height, int unappliedRotationDegrees, float pixelWidthHeightRatio) {
       mVideoWidth = width;
       mVideoHeight = height;
       mVideoAspect = pixelWidthHeightRatio;
    }

    @Override
    public void onRenderedFirstFrame() {

    }
}
