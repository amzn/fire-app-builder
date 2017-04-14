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
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */

package com.amazon.android.uamp.ui;

import com.amazon.ads.IAds;
import com.amazon.analytics.AnalyticsTags;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.database.ContentDatabaseHelper;
import com.amazon.android.contentbrowser.database.RecentRecord;
import com.amazon.android.contentbrowser.helper.AnalyticsHelper;
import com.amazon.android.model.content.Content;
import com.amazon.android.module.ModuleManager;

import com.amazon.android.recipe.Recipe;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.uamp.DrmProvider;
import com.amazon.android.uamp.UAMP;
import com.amazon.android.uamp.constants.PreferencesConstants;
import com.amazon.android.uamp.textrenderer.SubtitleLayout;
import com.amazon.android.ui.fragments.ErrorDialogFragment;
import com.amazon.android.utils.ErrorUtils;
import com.amazon.android.utils.Helpers;
import com.amazon.android.utils.Preferences;
import com.amazon.mediaplayer.AMZNMediaPlayer;
import com.amazon.mediaplayer.AMZNMediaPlayer.PlayerState;
import com.amazon.mediaplayer.playback.text.Cue;
import com.amazon.mediaplayer.tracks.TrackType;
import com.amazon.utils.DateAndTimeHelper;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.FrameLayout;
import android.widget.ProgressBar;

import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * PlaybackOverlayActivity for content playback that loads PlaybackOverlayFragment
 */
public class PlaybackActivity extends Activity implements
        PlaybackOverlayFragment.OnPlayPauseClickedListener
        , AMZNMediaPlayer.OnStateChangeListener
        , AMZNMediaPlayer.OnErrorListener
        , AMZNMediaPlayer.OnInfoListener
        , AudioManager.OnAudioFocusChangeListener
        , AMZNMediaPlayer.OnCuesListener,
        ErrorDialogFragment.ErrorDialogFragmentListener {

    private static final int TRANSPORT_CONTROLS_DELAY_PERIOD = 50;
    private static final String TAG = PlaybackActivity.class.getSimpleName();
    private static final String HLS_VIDEO_FORMAT = "HLS";

    private static final int HDMI_AUDIO_STATE_UNPLUGGED = 0;
    private static final int HDMI_AUDIO_STATE_PLUGGED = 1;

    private static final float AUDIO_FOCUS_DUCK_VOLUME = 0.1f;
    private static final float AUDIO_FOCUS_DEFAULT_VOLUME = 1.0f;

    private FrameLayout mVideoView;
    private SubtitleLayout mSubtitleLayout;
    private FrameLayout mAdsView;
    private UAMP mPlayer;
    private Content mSelectedContent;
    private LeanbackPlaybackState mPlaybackState = LeanbackPlaybackState.IDLE;
    private PlayerState mPrevState;
    private PlayerState mCurrentState;
    private boolean mIsActivityResumed;
    private boolean mIsContentChangeRequested;

    private ProgressBar mProgressBar;
    private Window mWindow;
    private long mCurrentPlaybackPosition;
    private long mStartingPlaybackPosition;

    private AudioManager mAudioManager;
    private AudioFocusState mAudioFocusState = AudioFocusState.NoFocusNoDuck;
    private PlaybackOverlayFragment mPlaybackOverlayFragment;
    private ErrorDialogFragment mErrorDialogFragment = null;

    private Handler mTransportControlsUpdateHandler;
    private ContinualFwdUpdater mContinualFwdUpdater;
    private ContinualRewindUpdater mContinualRewindUpdater;
    private boolean mIsLongPress;
    private boolean mAutoPlay = false;
    private boolean mIsNetworkError;

    /**
     * State of CC in Subtitle view.
     */
    private boolean mIsCloseCaptionEnabled = false;

    /**
     * Is Content support close caption flag.
     */
    private boolean mIsContentSupportCC = false;

    enum AudioFocusState {
        Focused,
        NoFocusNoDuck,
        NoFocusCanDuck
    }

    /**
     * Video position tracking handler.
     */
    private Handler mVideoPositionTrackingHandler;

    /**
     * Video position tracking runnable.
     */
    private Runnable mVideoPositionTrackingRunnable;

    /**
     * Ads implementation reference.
     */
    private IAds mAdsImplementation;

    /**
     * Video position tracking poll time in ms.
     */
    private static final int VIDEO_POSITION_TRACKING_POLL_TIME_MS = 1000;

    /**
     * Called when the activity is first created.
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Create video position tracking handler.
        mVideoPositionTrackingHandler = new Handler();

        // Create a runnable for video position tracking.
        mVideoPositionTrackingRunnable = new Runnable() {
            @Override
            public void run() {

                try {
                    // If player exists and playing then set video position to ads implementation.
                    if (mPlayer != null && isPlaying()) {
                        if (mAdsImplementation != null) {
                            mAdsImplementation.setCurrentVideoPosition(
                                    mPlayer.getCurrentPosition());
                        }
                    }
                }
                catch (Exception e) {
                    Log.e(TAG, "Video position tracking failed.", e);
                }
                mVideoPositionTrackingHandler.postDelayed(this,
                                                          VIDEO_POSITION_TRACKING_POLL_TIME_MS);
            }
        };

        // Trigger GC to clean up prev player.
        // In regular Java vm this is not advised but for Android we need this.
        // This will let things going as CC became a must and this code will be handled out of
        // TenFootUI in a different way.
        System.gc();

        setContentView(R.layout.playback_controls);

        mWindow = getWindow();

        mProgressBar = (ProgressBar) findViewById(R.id.playback_progress);
        mPlaybackOverlayFragment =
                (PlaybackOverlayFragment) getFragmentManager()
                        .findFragmentById(R.id.playback_controls_fragment);

        mSelectedContent =
                (Content) getIntent().getSerializableExtra(Content.class.getSimpleName());

        if (mSelectedContent == null || TextUtils.isEmpty(mSelectedContent.getUrl())) {
            AnalyticsHelper.trackError(TAG, "Received an Intent to play content without a " +
                    "content object or content URL");
            finish();
        }

        loadViews();
        createPlayerAndInitializeListeners();
        mAudioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        mCurrentPlaybackPosition = 0;
        mTransportControlsUpdateHandler = new Handler(Looper.getMainLooper());
        mContinualFwdUpdater = new ContinualFwdUpdater();
        mContinualRewindUpdater = new ContinualRewindUpdater();
        mIsLongPress = false;
        mIsNetworkError = false;

        // Auto-play the selected content.
        mAutoPlay = true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {

        super.onStart();

        registerHDMIUnpluggedStateChangeBroadcast();
        requestAudioFocus();
        openSelectedContent();

        // Let ads implementation track player activity lifecycle.
        if (mAdsImplementation != null) {
            mAdsImplementation.setActivityState(IAds.ActivityState.START);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {

        super.onResume();

        // Start tracking video position changes.
        mVideoPositionTrackingHandler.post(mVideoPositionTrackingRunnable);

        // Check to see if a previous network failure was now rectified.
        if (!mIsActivityResumed && mIsNetworkError && Helpers.isConnectedToNetwork(this)) {
            mIsActivityResumed = true;
            mIsNetworkError = false;
            finish();
            Log.i(TAG, "Traversing to details page since network connection is now detected");
            return;
        }

        // Get persisted state of CC.
        mIsCloseCaptionEnabled =
                Preferences.getBoolean(PreferencesConstants.IS_CLOSE_CAPTION_FLAG_PERSISTED);

        // Reset playback position to 0.
        mCurrentPlaybackPosition = 0;

        // Live content can't be resumed.
        if (!isContentLive(mSelectedContent)) {
            loadContentPlaybackState();
        }
        mStartingPlaybackPosition = mCurrentPlaybackPosition;
        mIsActivityResumed = true;
        Log.d(TAG, "onResume() current state is " + mCurrentState);
        switch (mCurrentState) {
            case READY:
                if (mCurrentPlaybackPosition > 0) {
                    mPlayer.seekTo(mCurrentPlaybackPosition);
                }
                else {
                    if (mAutoPlay) {
                        play();
                        mAutoPlay = false;
                    }
                }
                break;
            case OPENED:
                mPlayer.prepare();
                break;
        }

        long duration = getDuration();
        // Duration wasn't found using the player, try getting it directly from the content.
        if (duration == 0) {
            duration = mSelectedContent.getDuration();
        }
        AnalyticsHelper.trackPlaybackStarted(mSelectedContent, duration, mCurrentPlaybackPosition);

        // Let ads implementation track player activity lifecycle.
        if (mAdsImplementation != null) {
            mAdsImplementation.setActivityState(IAds.ActivityState.RESUME);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {

        super.onPause();

        // Persist CC state.
        Preferences.setBoolean(PreferencesConstants.IS_CLOSE_CAPTION_FLAG_PERSISTED,
                               mIsCloseCaptionEnabled);

        if (mPlayer.getCurrentPosition() > 0) {

            storeContentPlaybackState();
            // User has stopped watching content so track it with analytics
            AnalyticsHelper.trackPlaybackFinished(mSelectedContent, mStartingPlaybackPosition,
                                                  getCurrentPosition());

            // After the user has stopped watching the content, send recommendations for related
            // content of the selected content if any exist.
            if (mSelectedContent.getRecommendations().size() > 0) {
                ContentBrowser.getInstance(this).getRecommendationManager()
                              .executeRelatedRecommendationsTask(getApplicationContext(),
                                                                 mSelectedContent);
            }
        }
        mIsActivityResumed = false;
        pause();

        // Stop tracking video position changes.
        mVideoPositionTrackingHandler.removeCallbacks(mVideoPositionTrackingRunnable);

        // Let ads implementation track player activity lifecycle.
        if (mAdsImplementation != null) {
            mAdsImplementation.setActivityState(IAds.ActivityState.PAUSE);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStop() {

        super.onStop();

        unregisterHDMIUnpluggedStateChangeBroadcast();
        abandonAudioFocus();
        mIsContentChangeRequested = false;

        if (mPlayer != null) {
            if (!isContentLive(mSelectedContent)) {
                mCurrentPlaybackPosition = getCurrentPosition();
            }
            mPlayer.close();
        }

        // Let ads implementation track player activity lifecycle.
        if (mAdsImplementation != null) {
            mAdsImplementation.setActivityState(IAds.ActivityState.STOP);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        // This lets us get global font support.
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * Inner class implementing repeating fast-forward media key transport control
     */
    private final class ContinualFwdUpdater implements Runnable {

        @Override
        public void run() {

            mPlaybackOverlayFragment.fastForward();
            mTransportControlsUpdateHandler.postDelayed(new ContinualFwdUpdater(),
                                                        TRANSPORT_CONTROLS_DELAY_PERIOD);
        }
    }

    /**
     * Inner class implementing repeating rewind media key transport control
     */
    private final class ContinualRewindUpdater implements Runnable {

        @Override
        public void run() {

            mPlaybackOverlayFragment.fastRewind();
            mTransportControlsUpdateHandler.postDelayed(new ContinualRewindUpdater(),
                                                        TRANSPORT_CONTROLS_DELAY_PERIOD);
        }
    }

    /**
     * Starts the repeating fast-forward media transport control action
     */
    private void startContinualFastForward() {

        mTransportControlsUpdateHandler.post(mContinualFwdUpdater);
        mIsLongPress = true;
    }

    /**
     * Starts the repeating rewind media transport control action
     */
    private void startContinualRewind() {

        mTransportControlsUpdateHandler.post(mContinualRewindUpdater);
        mIsLongPress = true;
    }

    /**
     * Stops the currently on-going (if any) media transport control action since the press &
     * hold of corresponding transport control ceased or {@link @KeyEvent.KEYCODE_HOME} was pressed
     */
    private void stopTransportControlAction() {

        mTransportControlsUpdateHandler.removeCallbacksAndMessages(null);
        mIsLongPress = false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroy() {

        super.onDestroy();
        releasePlayer();
    }

    private void showProgress() {

        mProgressBar.setVisibility(View.VISIBLE);
    }

    private void hideProgress() {

        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void play() {

        if (mPlayer != null) {
            if (mAudioFocusState == AudioFocusState.Focused) {
                mPlayer.play();
            }
            else {
                if (requestAudioFocus()) {
                    mPlayer.play();
                }
                else {
                    showProgress();
                    mPlaybackState = LeanbackPlaybackState.PLAYING;
                    if (mPlaybackOverlayFragment != null) {
                        mPlaybackOverlayFragment.togglePlaybackUI(true);
                    }
                }
            }
        }
    }

    private void pause() {

        if (mPlayer != null) {
            mPlayer.pause();
        }
        mPlaybackState = LeanbackPlaybackState.PAUSED;
        if (mPlaybackOverlayFragment != null) {
            mPlaybackOverlayFragment.togglePlaybackUI(false);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getDuration() {

        long duration = 0;
        if (mPlayer != null) {
            duration = mPlayer.getDuration();
            if (duration == AMZNMediaPlayer.UNKNOWN_TIME) {
                Log.i(TAG, "Content duration is unknown. Returning 0.");
                duration = 0;
            }
        }
        return (int) duration;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getCurrentPosition() {

        if (mPlayer != null) {
            return (int) mPlayer.getCurrentPosition();
        }
        return 0;
    }

    private void seekTo(int pos) {

        if (mPlayer != null) {
            mPlayer.seekTo(pos);
        }
    }

    /**
     * Returns true if the video is playing, else false
     *
     * @return true if the video is playing, else false
     */
    public boolean isPlaying() {

        boolean isPlaying = false;
        if (mPlayer != null) {
            isPlaying = (mPlayer.getPlayerState() == PlayerState.PLAYING);
        }
        return isPlaying;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getBufferProgressPosition() {

        if (mPlayer != null) {
            return (mPlayer.getBufferedPercentage() * getDuration()) / 100;
        }
        return 0;
    }

    /**
     * Implementation of OnPlayPauseClickedListener
     */
    @Override
    public void changeContent(Content content) {

        if (!mIsContentChangeRequested && !content.equals(mSelectedContent)) {
            Log.d(TAG, "In changeContent");
            showProgress();

            // Save previous content's state before changing.
            storeContentPlaybackState();

            // User has stopped watching this content so track it with analytics.
            AnalyticsHelper.trackPlaybackFinished(mSelectedContent, mStartingPlaybackPosition,
                                                  getCurrentPosition());

            // Since the user is done watching this content, send recommendations for related
            // content of the selected content (if any exist) before changing to the next content.
            if (mSelectedContent.getRecommendations().size() > 0) {
                ContentBrowser.getInstance(this).getRecommendationManager()
                              .executeRelatedRecommendationsTask(getApplicationContext(),
                                                                 mSelectedContent);
            }

            mIsContentChangeRequested = true;
            mCurrentPlaybackPosition = 0;
            mSelectedContent = content;

            loadContentPlaybackState();
            mStartingPlaybackPosition = mCurrentPlaybackPosition;
            // User will start watching this new content so track it with analytics.
            AnalyticsHelper.trackPlaybackStarted(mSelectedContent, mStartingPlaybackPosition,
                                                 mCurrentPlaybackPosition);

            if (mPlayer != null) {
                mPlayer.close();
            }
        }
    }

    /**
     * Uses the {@link ContentDatabaseHelper} to check the database for a stored playback state
     * of the current selected content. If the state exists and playback is not complete,
     * it loads the content's current playback position.
     */
    private void loadContentPlaybackState() {

        ContentDatabaseHelper database = ContentDatabaseHelper.getInstance(getApplicationContext());
        if (database != null) {
            // Check database for content's previously watched position.
            if (database.recentRecordExists(mSelectedContent.getId())) {

                RecentRecord record = database.getRecent(mSelectedContent.getId());
                // Set the playback position to the stored position if a recent position
                // exists for this content and playback is not complete.
                if (record != null && !record.isPlaybackComplete()) {
                    mCurrentPlaybackPosition = record.getPlaybackLocation();
                }
            }
        }
        else {
            Log.e(TAG, "Unable to load content playback state because database is null");
        }
    }

    /**
     * Store the current playback state of the selected content to the database. Calculates if
     * playback was finished or not using the  {@link ContentBrowser#GRACE_TIME_MS}.
     * If the content playback is finished, recommendation manager will dismiss the recommendation
     * for this content (if it exists).
     */
    private void storeContentPlaybackState() {

        // Save the recently played content to database
        ContentDatabaseHelper database = ContentDatabaseHelper.getInstance(getApplicationContext());
        if (database != null && !isContentLive(mSelectedContent)) {
            // Calculate if the content has finished playing
            boolean isFinished = (mPlayer.getDuration() - ContentBrowser.GRACE_TIME_MS)
                    <= mPlayer.getCurrentPosition();

            if (isFinished) {
                // Dismiss the notification for content (if exists)
                if (database.recommendationWithContentIdExists(mSelectedContent.getId())) {
                    ContentBrowser.getInstance(this).getRecommendationManager()
                                  .dismissRecommendation(mSelectedContent.getId());
                    AnalyticsHelper.trackDismissRecommendationForCompleteContent(mSelectedContent);
                }
            }

            database.addRecent(mSelectedContent.getId(), mPlayer.getCurrentPosition(), isFinished,
                               DateAndTimeHelper.getCurrentDate().getTime());
        }
        else {
            Log.e(TAG, "Cannot update recent content playback state. Database is null");
        }
    }

    /**
     * Implementation of OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentPlayPause(boolean playPause) {

        if (playPause) {
            play();
        }
        else {
            pause();
        }
    }

    /**
     * Implementation of OnPlayPauseClickedListener
     */
    @Override
    public void onFragmentFfwRwd(int position) {

        if (position >= 0) {
            seekTo(position);
            if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                play();
            }
        }
    }

    /**
     * Triggered by overlay when there is a CC button state change.
     *
     * @param state CC state
     */
    @Override
    public void onCloseCaptionButtonStateChanged(boolean state) {

        if (mPlayer != null && mPlaybackOverlayFragment != null) {
            if (mIsContentSupportCC) {
                // Enable CC.
                mPlayer.enableTextTrack(TrackType.SUBTITLE, state);
                // Update internal state.
                mIsCloseCaptionEnabled = state;
                mPlaybackOverlayFragment.updateCCButtonState(state, mIsContentSupportCC);
                Log.d(TAG, "Content support CC. Change CC state to " + state);
            }
            else {
                // Disable CC button back.
                mPlaybackOverlayFragment.updateCCButtonState(false, mIsContentSupportCC);
                // Do not disable mIsCloseCaptionEnabled as we want it persistent.
                Log.d(TAG, "Content does not support CC. Change CC state to false");
            }
        }
    }

    private void loadViews() {

        mVideoView = (FrameLayout) findViewById(R.id.videoView);
        // Avoid focus stealing.
        mVideoView.setFocusable(false);
        mVideoView.setFocusableInTouchMode(false);
        mVideoView.setClickable(false);

        mSubtitleLayout = (SubtitleLayout) findViewById(R.id.subtitles);

        mAdsView = (FrameLayout) findViewById(R.id.adsView);
        // Avoid focus stealing.
        mAdsView.setFocusable(false);
        mAdsView.setFocusableInTouchMode(false);
        mAdsView.setClickable(false);

        // Make Ads visible and video invisible.
        mAdsView.setVisibility(View.VISIBLE);
        mVideoView.setVisibility(View.INVISIBLE);
    }

    /**
     * Set visibility of a view group with its child surface views.
     *
     * @param viewGroup  View group object.
     * @param visibility Visibility flag to be set.
     */
    private void setVisibilityOfViewGroupWithInnerSurfaceView(ViewGroup viewGroup, int
            visibility) {

        // Hide the view group.
        viewGroup.setVisibility(visibility);
        // Traverse all the views and hide the child surface views.
        for (int i = 0; i < viewGroup.getChildCount(); i++) {
            View v = viewGroup.getChildAt(i);
            if (v instanceof SurfaceView) {
                v.setVisibility(visibility);
            }
        }
    }

    private void switch2VideoView() {
        // Show Video view.
        setVisibilityOfViewGroupWithInnerSurfaceView(mVideoView, View.VISIBLE);
        // Show Subtitle view.
        mSubtitleLayout.setVisibility(View.VISIBLE);
        // Hide Ads view.
        setVisibilityOfViewGroupWithInnerSurfaceView(mAdsView, View.GONE);
    }

    private void switch2AdsView() {
        // Show Ads view.
        setVisibilityOfViewGroupWithInnerSurfaceView(mAdsView, View.VISIBLE);
        // Hide Video view.
        setVisibilityOfViewGroupWithInnerSurfaceView(mVideoView, View.GONE);
        // Hide Subtitle view.
        mSubtitleLayout.setVisibility(View.GONE);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onVisibleBehindCanceled() {

        super.onVisibleBehindCanceled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        // If ad is in focus then don't respond to key events.
        if (mAdsView.getVisibility() == View.VISIBLE) {
            return super.onKeyDown(keyCode, event);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                event.startTracking();
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                event.startTracking();
                return true;
            default:
                return super.onKeyDown(keyCode, event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyLongPress(int keyCode, KeyEvent event) {

        // If ad is in focus then don't respond to key events.
        if (mAdsView.getVisibility() == View.VISIBLE) {
            return super.onKeyLongPress(keyCode, event);
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                startContinualFastForward();
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                startContinualRewind();
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                startContinualFastForward();
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                startContinualRewind();
                return true;
            default:
                return super.onKeyLongPress(keyCode, event);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {


        // If ad is in focus then don't respond to key events.
        if (mAdsView.getVisibility() == View.VISIBLE) {
            return super.onKeyUp(keyCode, event);
        }

        PlaybackOverlayFragment playbackOverlayFragment = (PlaybackOverlayFragment)
                getFragmentManager().findFragmentById(R.id.playback_controls_fragment);
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_PLAY:
                playbackOverlayFragment.togglePlayback(false);
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                   .ACTION_PLAYBACK_CONTROL_PLAY,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            case KeyEvent.KEYCODE_MEDIA_PAUSE:
                playbackOverlayFragment.togglePlayback(false);
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                   .ACTION_PLAYBACK_CONTROL_PAUSE,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    playbackOverlayFragment.togglePlayback(false);
                    AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                       .ACTION_PLAYBACK_CONTROL_PAUSE,
                                                               mSelectedContent,
                                                               getCurrentPosition());
                }
                else {
                    playbackOverlayFragment.togglePlayback(true);
                    AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                       .ACTION_PLAYBACK_CONTROL_PLAY,
                                                               mSelectedContent,
                                                               getCurrentPosition());
                }
                return true;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                if (mIsLongPress) {
                    stopTransportControlAction();
                }
                else {
                    playbackOverlayFragment.fastForward();
                }
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_FF,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                if (mIsLongPress) {
                    stopTransportControlAction();
                }
                else {
                    playbackOverlayFragment.fastRewind();
                }
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                   .ACTION_PLAYBACK_CONTROL_REWIND,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            case KeyEvent.KEYCODE_BUTTON_R1:
                if (mIsLongPress) {
                    stopTransportControlAction();
                }
                else {
                    playbackOverlayFragment.fastForward();
                }
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_FF,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            case KeyEvent.KEYCODE_BUTTON_L1:
                if (mIsLongPress) {
                    stopTransportControlAction();
                }
                else {
                    playbackOverlayFragment.fastRewind();
                }
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                   .ACTION_PLAYBACK_CONTROL_REWIND,
                                                           mSelectedContent, getCurrentPosition());
                return true;
            default:
                return super.onKeyUp(keyCode, event);
        }
    }

    private void registerHDMIUnpluggedStateChangeBroadcast() {

        IntentFilter intentFilter = new IntentFilter(AudioManager.ACTION_HDMI_AUDIO_PLUG);
        registerReceiver(mHDMIUnpluggedStateChangeReceiver, intentFilter);
    }

    private void unregisterHDMIUnpluggedStateChangeBroadcast() {

        unregisterReceiver(mHDMIUnpluggedStateChangeReceiver);
    }

    private BroadcastReceiver mHDMIUnpluggedStateChangeReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "mHDMIUnpluggedStateChangeReceiver " + intent);
            if (isInitialStickyBroadcast()) {
                // Ignore initial sticky broadcast.
                return;
            }

            int plugState = intent.getIntExtra(AudioManager.EXTRA_AUDIO_PLUG_STATE, -1);
            if (plugState == HDMI_AUDIO_STATE_UNPLUGGED) {
                if (mPlayer != null) {
                    if (!isContentLive(mSelectedContent)) {
                        mCurrentPlaybackPosition = getCurrentPosition();
                    }
                    if (isPlaying()) {
                        pause();// No audio focus, pause media!
                    }
                }
            }
        }
    };

    /*
     * List of various states that we can be in.
     */
    public enum LeanbackPlaybackState {
        PLAYING, PAUSED, BUFFERING, IDLE
    }

    private void createPlayerAndInitializeListeners() {

        if (mPlayer == null) {
            Log.d(TAG, "Create Player and Initialize Listeners");
            mPrevState = PlayerState.IDLE;
            mCurrentState = PlayerState.IDLE;
            Bundle playerExtras = new Bundle();
            // Get default Ads implementation without creating a new one.
            try {
                mAdsImplementation = (IAds) ModuleManager.getInstance()
                                                         .getModule(IAds.class.getSimpleName())
                                                         .getImpl(false);
                playerExtras.putBundle("ads", mAdsImplementation.getExtra());
            }
            catch (Exception e) {
                Log.e(TAG, "No Ads interface attached.", e);
            }

            // Create a player interface by using the default hooked implementation.
            String playerInterfaceName = UAMP.class.getSimpleName();
            mPlayer = (UAMP) ModuleManager.getInstance()
                                          .getModule(playerInterfaceName)
                                          .createImpl();

            // Init player interface, this is where it is fully created.
            mPlayer.init(this, mVideoView, playerExtras);

            // Initialize ads.
            if (mAdsImplementation != null) {
                mAdsImplementation.init(this, mAdsView, playerExtras);
            }

            mPlayer.setUserAgent(System.getProperty("http.agent"));
            mPlayer.addStateChangeListener(this);
            mPlayer.addErrorListener(this);
            mPlayer.addInfoListener(this);
            mPlayer.addCuesListener(this);
        }
    }

    private void clearPlayerCallbacks() {

        if (mPlayer != null) {
            Log.d(TAG, "Clear playback callbacks");
            mPlayer.removeStateChangeListener(this);
            mPlayer.removeErrorListener(this);
            mPlayer.removeInfoListener(this);
            mPlayer.removeCuesListener(this);
        }
    }

    /**
     * This is the entry point for Subtitle rendering.
     *
     * @param cues Subtitle cues.
     */
    @Override
    public void onCues(List<Cue> cues) {

        mSubtitleLayout.setCues(cues);
    }

    private IAds.IAdsEvents mIAdsEvents = new IAds.IAdsEvents() {
        @Override
        public void onAdSlotStarted(Bundle extras) {

            Log.d(TAG, "onAdSlotStarted");
            AnalyticsHelper.trackAdStarted(mSelectedContent, getCurrentPosition());
            // Hide media controller.
            if (mPlaybackOverlayFragment != null && mPlaybackOverlayFragment.getView() != null) {
                mPlaybackOverlayFragment.getView().setVisibility(View.INVISIBLE);
            }

            // Pause the video if we are already playing a content.
            if (mPlayer != null && isPlaying()) {
                mPlayer.pause();
            }

            // Hide progress bar.
            hideProgress();
            // Show Ads view.
            switch2AdsView();
        }

        @Override
        public void onAdSlotEnded(final Bundle extras) {

            Log.d(TAG, "onAdSlotEnded");
            // Show progress
            showProgress();
            // Show video View.
            switch2VideoView();

            // Will be used for Analytics.
            long duration = 0;
            if (extras != null) {
                duration = extras.getLong(IAds.DURATION);
                boolean wasAMidRoll = extras.getBoolean(IAds.WAS_A_MID_ROLL);
                if (wasAMidRoll) {
                    // Show media controller.
                    if (mPlaybackOverlayFragment != null && mPlaybackOverlayFragment.getView() !=
                            null) {
                        mPlaybackOverlayFragment.getView().setVisibility(View.VISIBLE);
                    }

                    // Resume movie after a mid roll.
                    mPlayer.play();
                }
                else {
                    // Open Movie.
                    openContentHelper(mSelectedContent);
                }

                Log.d(TAG, "Ad Played for " + duration + " ms");
            }
            else {
                // Open Movie.
                openContentHelper(mSelectedContent);
            }
            AnalyticsHelper.trackAdEnded(mSelectedContent, duration, getCurrentPosition());
        }
    };

    /**
     * Get video extras bundle.
     *
     * @param content Content object.
     * @return Result bundle.
     */
    private Bundle getVideoExtrasBundle(Content content) {

        Bundle videoExtras = new Bundle();
        videoExtras.putString(Content.ID_FIELD_NAME, content.getId());
        String adId = (String) content.getExtraValue(Content.AD_ID_FIELD_NAME);

        // Adding this to keep the code backward compatible
        if (adId == null) {
            adId = content.getId();
        }
        videoExtras.putString(Content.AD_ID_FIELD_NAME, adId);
        videoExtras.putLong(Content.DURATION_FIELD_NAME, content.getDuration());

        if (content.getAdCuePoints() != null) {
            int[] adCuePoints = new int[content.getAdCuePoints().size()];
            for (int i = 0; i < adCuePoints.length; i++) {
                adCuePoints[i] = content.getAdCuePoints().get(i);
            }
            videoExtras.putIntArray(Content.AD_CUE_POINTS_FIELD_NAME, adCuePoints);
        }
        return videoExtras;
    }

    private void openContentHelper(Content content) {

        if (mPlayer != null && mPlayer.getPlayerState() == PlayerState.IDLE) {
            String url = content.getUrl();
            if (TextUtils.isEmpty(url)) {
                AnalyticsHelper.trackError(TAG, "Content URL is either null or empty for content " +
                        content.toString());
                return;
            }

            AMZNMediaPlayer.ContentMimeType type = AMZNMediaPlayer.ContentMimeType
                    .CONTENT_TYPE_UNKNOWN;
            // If the content object contains the video format type, set the ContentMimeType
            // accordingly.
            if (!TextUtils.isEmpty(content.getFormat())) {
                if (content.getFormat().equalsIgnoreCase(HLS_VIDEO_FORMAT)) {
                    type = AMZNMediaPlayer.ContentMimeType.CONTENT_HLS;
                }
            }

            mIsContentSupportCC = false;
            AMZNMediaPlayer.TextMimeType ccType = AMZNMediaPlayer.TextMimeType.TEXT_WTT;

            List<String> closeCaptionUrls = content.getCloseCaptionUrls();
            String closeCaptionUrl = null;

            if (content.hasCloseCaption() && closeCaptionUrls.size() > 0) {
                // We prefer first selection against others.
                closeCaptionUrl = closeCaptionUrls.get(0);
            }

            // If there is a close caption url then find the extension and call
            // open accordingly.
            if (content.hasCloseCaption() &&
                    closeCaptionUrl != null &&
                    closeCaptionUrl.length() > 4) {

                int lastDot = closeCaptionUrl.lastIndexOf('.');
                if (lastDot > 0) {
                    String ext = closeCaptionUrl.substring(lastDot + 1);
                    if (ext.equals("vtt")) {
                        mIsContentSupportCC = true;
                        ccType = AMZNMediaPlayer.TextMimeType.TEXT_WTT;
                        Log.d(TAG, "Close captioning is enabled & its format is TextWTT");
                    }
                    else if (ext.equals("xml")) {
                        mIsContentSupportCC = true;
                        ccType = AMZNMediaPlayer.TextMimeType.TEXT_TTML;
                        Log.d(TAG, "Close captioning is enabled & its format is TextTTML");
                    }
                }
            }

            if (mPlaybackOverlayFragment != null) {
                mPlaybackOverlayFragment.updateCCButtonState(mIsContentSupportCC &&
                                                                     mIsCloseCaptionEnabled,
                                                             mIsContentSupportCC);
                mPlaybackOverlayFragment.updateCurrentContent(mSelectedContent);
            }

            // TODO: refactor out the Amazon media player code to make this activity player
            // agnostic, Devtech-2634
            AMZNMediaPlayer.ContentParameters contentParameters =
                    new AMZNMediaPlayer.ContentParameters(url, type);
            DrmProvider drmProvider = new DrmProvider(content, this);
            contentParameters.laurl = drmProvider.fetchLaUrl();
            contentParameters.encryptionSchema = getAmznMediaEncryptionSchema(drmProvider);

            if (mIsContentSupportCC) {

                contentParameters.oobTextSources = new AMZNMediaPlayer.OutOfBandTextSource[]{new
                        AMZNMediaPlayer.OutOfBandTextSource(closeCaptionUrl, ccType, "en")};
                mPlayer.open(contentParameters);
                Log.d(TAG, "Media player opened with close captioning support");
            }
            else {
                mPlayer.open(contentParameters);
                Log.d(TAG, "Media player opened without close captioning support");
            }

        }
    }

    /**
     * Fetches the encryption schema from the resources. If the schema is not available default is
     * sent.
     *
     * @param drmProvider DrmProvider instance
     * @return encryption schema
     */
    private AMZNMediaPlayer.EncryptionSchema getAmznMediaEncryptionSchema(DrmProvider drmProvider) {

        String encryptionSchema = drmProvider.getEncryptionSchema();

        switch (encryptionSchema) {
            case "ENCRYPTION_PLAYREADY":
                return AMZNMediaPlayer.EncryptionSchema.ENCRYPTION_PLAYREADY;
            case "ENCRYPTION_WIDEVINE":
                return AMZNMediaPlayer.EncryptionSchema.ENCRYPTION_WIDEVINE;
            default:
                return AMZNMediaPlayer.EncryptionSchema.ENCRYPTION_DEFAULT;
        }
    }

    private void openSelectedContent() {

        Log.d(TAG, "Open content");

        mAdsImplementation.setIAdsEvents(mIAdsEvents);

        // Hide videoView which make adsView visible.
        switch2AdsView();

        // Hide media controller.
        if (mPlaybackOverlayFragment != null && mPlaybackOverlayFragment.getView() != null) {
            mPlaybackOverlayFragment.getView().setVisibility(View.INVISIBLE);
        }
        // Show progress before pre roll ad.
        showProgress();

        // Get video extras bundle.
        Bundle videoExtras = getVideoExtrasBundle(mSelectedContent);
        // We need to pass id, duration and adCuePoints to player interface.
        // Usually required by ads support.
        // As Player interface doesn't know about video model, we are using Bundles.
        mPlayer.getExtra().putBundle("video", videoExtras);

        // Set Ads video extras.
        mAdsImplementation.getExtra().putBundle("video", videoExtras);
        // Show pre roll ad.
        mAdsImplementation.showPreRollAd();
    }

    private void releasePlayer() {

        // Remove ads event listener.
        if (mAdsImplementation != null) {
            mAdsImplementation.setIAdsEvents(null);
        }

        if (mPlayer != null) {
            Log.d(TAG, "Release player");
            clearPlayerCallbacks();
            mPlayer.close();
            mPlayer.release();
            mPlayer = null;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPlayerStateChange(PlayerState oldState, PlayerState newState, Bundle extra) {

        mPrevState = mCurrentState;
        mCurrentState = newState;
        Log.d(TAG, "State change event! Oldstate= " + oldState + " NewState= " + newState);
        if (mPrevState == mCurrentState) {
            // Just to catch this while under dev
            Log.w(TAG, "Duplicate state change message!!! ");
        }
        // If buffering stopped
        if (mPrevState == PlayerState.BUFFERING && mCurrentState != PlayerState.BUFFERING) {
            AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags.ACTION_PLAYBACK_BUFFER_END,
                                                       mSelectedContent, getCurrentPosition());
        }
        switch (newState) {
            case IDLE:
                mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (mIsContentChangeRequested) {
                    openSelectedContent();
                }
                break;
            case OPENING:
                break;
            case OPENED:
                if (mPlayer != null && mIsActivityResumed) {
                    mPlayer.prepare();
                }
                else {
                    mIsContentChangeRequested = false;
                }
                // Remember CC state for playbacks.
                if (mIsContentSupportCC && mIsCloseCaptionEnabled) {
                    if (mPlaybackOverlayFragment != null) {
                        mPlaybackOverlayFragment.updateCCButtonState(true, true);
                    }
                    if (mPlayer != null) {
                        mPlayer.enableTextTrack(TrackType.SUBTITLE, true);
                    }
                }
                break;
            case PREPARING:
                // Show media controller.
                if (mPlaybackOverlayFragment != null && mPlaybackOverlayFragment.getView() !=
                        null) {
                    mPlaybackOverlayFragment.getView().setVisibility(View.VISIBLE);
                }
                break;
            case READY:
                mPlaybackState = LeanbackPlaybackState.PAUSED;
                if (mPlaybackOverlayFragment != null) {
                    mPlaybackOverlayFragment.togglePlaybackUI(false);
                }
                hideProgress();
                mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                if (mPrevState == PlayerState.PREPARING) {
                    if (mPlaybackOverlayFragment != null) {
                        mPlaybackOverlayFragment.updatePlayback();
                        mPlaybackOverlayFragment.startProgressAutomation();
                    }
                    if (mCurrentPlaybackPosition > 0) {
                        mPlayer.seekTo(mCurrentPlaybackPosition);
                    }
                    // One of the causes for the player state transition might be due to
                    // a new content being selected from recommended content.
                    if (mAutoPlay || mIsContentChangeRequested) {
                        play();
                        mAutoPlay = false;
                        if (mIsContentChangeRequested) {
                            mIsContentChangeRequested = false;
                        }
                    }
                }
                else if (mAudioFocusState == AudioFocusState.NoFocusNoDuck) {
                    play();
                }

                // Let ads implementation track player state.
                if (mAdsImplementation != null) {
                    mAdsImplementation.setPlayerState(IAds.PlayerState.PAUSED);
                }
                break;
            case PLAYING:
                mPlaybackState = LeanbackPlaybackState.PLAYING;
                if (mPlaybackOverlayFragment != null) {
                    mPlaybackOverlayFragment.togglePlaybackUI(true);
                }
                hideProgress();
                mWindow.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Let ads implementation track player state.
                if (mAdsImplementation != null) {
                    mAdsImplementation.setPlayerState(IAds.PlayerState.PLAYING);
                }
                break;
            case BUFFERING:
                showProgress();
                AnalyticsHelper.trackPlaybackControlAction(AnalyticsTags
                                                                   .ACTION_PLAYBACK_BUFFER_START,
                                                           mSelectedContent, getCurrentPosition());
                break;
            case SEEKING:
                showProgress();
                break;
            case ENDED:
                hideProgress();
                if (mPlaybackOverlayFragment != null) {
                    mPlaybackOverlayFragment.playbackFinished();
                }
                mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

                // Let ads implementation track player state.
                if (mAdsImplementation != null) {
                    mAdsImplementation.setPlayerState(IAds.PlayerState.COMPLETED);
                }
                break;
            case CLOSING:
                if (mPlaybackOverlayFragment != null) {
                    mPlaybackOverlayFragment.stopProgressAutomation();
                }
                break;
            case ERROR:
                hideProgress();
                mWindow.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
                Log.e(TAG, "Player encountered an error!");
                break;
            default:
                Log.e(TAG, "Unknown state!!!!!");
                break;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onInfo(AMZNMediaPlayer.Info info) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onError(AMZNMediaPlayer.Error e) {

        if (Helpers.isConnectedToNetwork(this)) {
            Log.e(TAG, "Media Player error during playback", e.mException);
            mErrorDialogFragment = ErrorDialogFragment.newInstance(this, ErrorUtils
                    .ERROR_CATEGORY.PLAYER_ERROR, this);
        }
        else {
            Log.e(TAG, "Network error during playback", e.mException);
            mErrorDialogFragment = ErrorDialogFragment.newInstance(this, ErrorUtils
                    .ERROR_CATEGORY.NETWORK_ERROR, this);
            mIsNetworkError = true;
        }
        mErrorDialogFragment.show(getFragmentManager(), ErrorDialogFragment.FRAGMENT_TAG_NAME);
    }

    private boolean requestAudioFocus() {

        if (mAudioManager == null) {
            Log.e(TAG, "mAudionManager is null in requestAudioFocus");
            return false;
        }
        boolean focus = AudioManager.AUDIOFOCUS_REQUEST_GRANTED ==
                mAudioManager.requestAudioFocus(this, AudioManager.STREAM_MUSIC, AudioManager
                        .AUDIOFOCUS_GAIN);
        if (focus) {
            mAudioFocusState = AudioFocusState.Focused;
        }
        return focus;
    }

    private boolean abandonAudioFocus() {

        if (mAudioManager == null) {
            Log.e(TAG, "mAudionManager is null in abandonAudioFocus");
            return false;
        }
        boolean focus = AudioManager.AUDIOFOCUS_REQUEST_GRANTED == mAudioManager
                .abandonAudioFocus(this);
        if (focus) {
            mAudioFocusState = AudioFocusState.NoFocusNoDuck;
        }
        return focus;
    }

    /**
     * Checks if the content is live as specified by the recipe.
     *
     * @param content The Content to check.
     * @return True if the content is live, false if not live.
     */
    private boolean isContentLive(Content content) {

        return content.getExtraValue(Recipe.LIVE_FEED_TAG) != null &&
                Boolean.valueOf(content.getExtraValue(Recipe.LIVE_FEED_TAG).toString());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAudioFocusChange(int focusChange) {

        Log.d(TAG, "onAudioFocusChange() focusChange? " + focusChange);
        switch (focusChange) {
            case AudioManager.AUDIOFOCUS_GAIN:
                mAudioFocusState = AudioFocusState.Focused;
                if (mPlayer != null) {
                    mPlayer.setVolume(AUDIO_FOCUS_DEFAULT_VOLUME);
                }
                if (mPlaybackState == LeanbackPlaybackState.PLAYING) {
                    play();
                }
                hideProgress();
                break;
            case AudioManager.AUDIOFOCUS_LOSS:
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT:
                mAudioFocusState = AudioFocusState.NoFocusNoDuck;
                if (isPlaying()) {
                    pause();// No audio focus, pause media!
                }
                break;
            case AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK:
                mAudioFocusState = AudioFocusState.NoFocusCanDuck;
                if (isPlaying()) {
                    mPlayer.setVolume(AUDIO_FOCUS_DUCK_VOLUME);
                }
                break;
            default:
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void doButtonClick(ErrorDialogFragment errorDialogFragment, ErrorUtils
            .ERROR_BUTTON_TYPE errorButtonType, ErrorUtils.ERROR_CATEGORY errorCategory) {

        switch (errorCategory) {
            case PLAYER_ERROR:
                // Dismiss the dialog & finish the activity
                if (mErrorDialogFragment != null) {
                    mErrorDialogFragment.dismiss();
                    // Finish the player activity and go back to details page
                    finish();
                }
                break;
            case NETWORK_ERROR:
                if (errorButtonType == ErrorUtils.ERROR_BUTTON_TYPE.NETWORK_SETTINGS) {
                    ErrorUtils.showNetworkSettings(this);
                }
                break;
        }

    }
}
