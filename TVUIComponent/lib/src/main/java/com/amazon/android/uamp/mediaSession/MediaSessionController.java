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
package com.amazon.android.uamp.mediaSession;

import android.content.Intent;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.support.annotation.NonNull;
import android.support.v17.leanback.app.TenFootPlaybackOverlayFragment;
import android.util.Log;
import android.content.Context;

/**
 * Media session controller acts as intermediate layer between UI and model(MediaSessionHelper).
 * It defines actions and callback for media session and interface to be implemented by playback
 * fragments.
 */
public class MediaSessionController {

    private static final String TAG = MediaSessionController.class.getSimpleName();

    private MediaSessionHelper mMediaSessionHelper;
    private OnMediaSessionEventListener mPlaybackFragmentCallback;
    private int mMediaSessionPlaybackState;

    /**
     * Public constructor.
     */
    public MediaSessionController(TenFootPlaybackOverlayFragment playbackFragmentCallback) {

        setPlaybackFragmentCallback(playbackFragmentCallback);
        initializeMediaSessionHelper();
    }

    /**
     * Set OnMediaSessionEventListener callback.
     *
     * @param playbackFragment Playback fragment which implements the OnMediaSessionEventListener.
     */
    public void setPlaybackFragmentCallback(TenFootPlaybackOverlayFragment playbackFragment) {

        // This makes sure that the playback fragment has implemented the callback interface. If
        // not, it throws an exception.
        try {
            mPlaybackFragmentCallback = (OnMediaSessionEventListener) playbackFragment;
        }
        catch (ClassCastException e) {
            throw new ClassCastException("PlaybackFragment must implement " +
                                                 "OnMediaSessionEventListener: " + e);
        }
    }

    /**
     * Initializes the MediaSessionHelper
     */
    private void initializeMediaSessionHelper() {

        Log.d(TAG, "initializeMediaSessionHelper called");

        mMediaSessionHelper = new MediaSessionHelper(getActions());
    }

    /**
     * The android media session action set with this media player. Alexa currently
     * supports ONLY the following actions.
     *
     * @return a long specifying the bits of the actions which are enabled
     */
    private long getActions() {

        long mediaSessionActions = 0;
        mediaSessionActions |= PlaybackState.ACTION_PLAY;
        mediaSessionActions |= PlaybackState.ACTION_PAUSE;
        mediaSessionActions |= PlaybackState.ACTION_PLAY_PAUSE;
        mediaSessionActions |= PlaybackState.ACTION_SKIP_TO_PREVIOUS;
        mediaSessionActions |= PlaybackState.ACTION_SKIP_TO_NEXT;
        mediaSessionActions |= PlaybackState.ACTION_SEEK_TO;
        return mediaSessionActions;
    }


    /**
     * Updates the playback position in the media session
     *
     * @param playbackPosition The current playbackPosition
     */
    public void updatePlaybackState(long playbackPosition) {

        if (mMediaSessionHelper != null) {
            mMediaSessionHelper.setPlayerState(mMediaSessionPlaybackState, playbackPosition);
            Log.d(TAG, "current playback state: " + mMediaSessionPlaybackState);
        }
    }

    /**
     * Updates the playback state in the media session as well as the playback position
     *
     * @param playbackState    The current playback state
     * @param playbackPosition The current playbackPosition
     */
    public void updatePlaybackState(int playbackState, long playbackPosition) {

        if (mMediaSessionHelper != null) {
            mMediaSessionPlaybackState = playbackState;
            mMediaSessionHelper.setPlayerState(playbackState, playbackPosition);
            Log.d(TAG, "current playback state: " + playbackState);
        }
    }

    /**
     * Creates the media session
     *
     * @param context Application context
     */
    public void createMediaSession(Context context) {

        if (mMediaSessionHelper != null) {
            mMediaSessionHelper.createMediaSession(context, getMediaSessionCallback());
        }
    }

    /**
     * Enable/disable the media session
     *
     * @param active boolean to either set media session active or inactive.
     */
    public void setMediaSessionActive(boolean active) {

        if (mMediaSessionHelper != null) {
            mMediaSessionHelper.setActive(active);
        }
    }

    /**
     * Release the media session
     */
    public void releaseMediaSession() {

        if (mMediaSessionHelper != null) {
            mMediaSessionHelper.release();
            mMediaSessionHelper = null;
        }
    }

    /**
     * Playback fragment must implement this interface
     */
    public interface OnMediaSessionEventListener {

        void onMediaSessionPlayPause(boolean playPause);

        void onMediaSessionSeekTo(int position);

        void onMediaSessionSkipToNext();

        void onMediaSessionSkipToPrev();

        int getCurrentPosition();
    }

    /**
     * Get the MediaSession callback
     */
    private MediaSession.Callback getMediaSessionCallback() {

        return new MediaSession.Callback() {

            @Override
            public void onPlay() {

                Log.d(TAG, "Received PLAY");
                mPlaybackFragmentCallback.onMediaSessionPlayPause(true);
            }

            @Override
            public void onPause() {

                Log.d(TAG, "Received PAUSE");
                mPlaybackFragmentCallback.onMediaSessionPlayPause(false);
            }

            @Override
            public void onSkipToNext() {

                Log.d(TAG, "Received SKIP_TO_NEXT");
                updatePlaybackState(PlaybackState.STATE_SKIPPING_TO_NEXT,
                                    mPlaybackFragmentCallback.getCurrentPosition());
                mPlaybackFragmentCallback.onMediaSessionSkipToNext();
            }

            @Override
            public void onSkipToPrevious() {

                Log.d(TAG, "Received SKIP_TO_PREVIOUS");
                updatePlaybackState(PlaybackState.STATE_SKIPPING_TO_PREVIOUS,
                                    mPlaybackFragmentCallback.getCurrentPosition());
                mPlaybackFragmentCallback.onMediaSessionSkipToPrev();
            }

            @Override
            public void onFastForward() {

                Log.d(TAG, "Received FF");
                int currPosition = mPlaybackFragmentCallback.getCurrentPosition();
                updatePlaybackState(PlaybackState.STATE_FAST_FORWARDING, currPosition);
                mPlaybackFragmentCallback.onMediaSessionSeekTo(currPosition + 10 * 1000);
            }

            @Override
            public void onRewind() {

                Log.d(TAG, "Received RW");
                int currPosition = mPlaybackFragmentCallback.getCurrentPosition();
                updatePlaybackState(PlaybackState.STATE_REWINDING, currPosition);
                mPlaybackFragmentCallback.onMediaSessionSeekTo(currPosition - 10 * 1000);
            }

            @Override
            public void onSeekTo(long pos) {

                Log.d(TAG, "Current duration is "
                        + mPlaybackFragmentCallback.getCurrentPosition() +
                        " milliseconds. Received SEEK_TO to " + pos + " milliseconds.");
                mPlaybackFragmentCallback.onMediaSessionSeekTo((int) pos);
            }

            @Override
            public void onStop() {

                Log.d(TAG, "Received STOP. Not yet implemented!!");
            }

            /**
             * This suppresses the media session events to be fired during media button
             * events. Added new JIRA DEVTECH-5027 to check if we need to primarily use
             * media session for the key handling as well.
             */
            @Override
            public boolean onMediaButtonEvent(@NonNull Intent mediaButtonIntent) {

                Log.d(TAG, "Don't handle media button events from media session!!");
                return false;
            }
        };
    }
}
