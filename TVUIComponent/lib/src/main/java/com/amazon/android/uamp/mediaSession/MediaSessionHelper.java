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

import android.content.Context;
import android.media.session.MediaSession;
import android.media.session.PlaybackState;
import android.os.Bundle;
import android.util.Log;

/**
 * This is media session helper code. This is used to integrate playback activity with the the
 * android media session and help in enabling voice command and control through Alexa.
 */
class MediaSessionHelper {

    /**
     * DEBUG Tag.
     */
    private static final String TAG = MediaSessionHelper.class.getSimpleName();

    /**
     * Key for the media length
     */
    private static final String KEY_MEDIA_LENGTH = "com.amazon.media.MEDIA_LENGTH";

    /**
     * Media session instance.
     */
    private MediaSession mMediaSession;

    /**
     * Capabilities bundle. All the capabilities of player supports as well as the
     * player preference bundle are stored in this.
     */
    private final Bundle mCapabilitiesBundle;

    /**
     * Available Media session actions.
     */
    private long mActions;

    /**
     * Initializes a new MediaSession Helper
     *
     * @param actions The set of the {@link PlaybackState} actions
     *                which this media session supports
     */
    protected MediaSessionHelper(final long actions) {

        // Create the capabilities bundle
        mCapabilitiesBundle = new Bundle();
        //Set the actions
        mActions = actions;
    }

    /**
     * Creates the media session
     *
     * @param callback The callback for the media session for each of the various commands
     */
    protected void createMediaSession(Context context, MediaSession.Callback callback) {

        if (mMediaSession != null) {
            Log.e(TAG, "Media session is already created. Setting to active.");
            return;
        }

        mMediaSession = new MediaSession(context, TAG);
        mMediaSession.setCallback(callback);
        mMediaSession.setExtras(mCapabilitiesBundle);

        //Set this to indicate whether it can handle media button events
        //and transport control commands through the media session callback
        mMediaSession.setFlags(MediaSession.FLAG_HANDLES_MEDIA_BUTTONS |
                                       MediaSession.FLAG_HANDLES_TRANSPORT_CONTROLS);

        setMediaSessionPlaybackState(PlaybackState.STATE_NONE, 0);

        Log.d(TAG, "MediaSession created and active");

    }

    /**
     * Set the player state
     *
     * @param state            The current state of the player
     * @param playbackPosition The current position of the player
     */
    protected void setPlayerState(int state, long playbackPosition) {

        if (mMediaSession == null) {
            Log.e(TAG, "Unable to set player state. Media Session doesn't exist");
            return;
        }

        if (!mMediaSession.isActive()) {
            Log.e(TAG, "Unable to set player state. Media Session is set to inactive");
            return;
        }

        setMediaSessionPlaybackState(state, playbackPosition);
        Log.d(TAG, "Media session active in setPlayerState, playbackPosition:" + playbackPosition);
    }

    /**
     * Set active flag of media session.
     *
     * @param active Active flag.
     */
    protected void setActive(boolean active) {

        if (mMediaSession == null) {
            Log.e(TAG, "Unable to set new active state. Media Session doesn't exist");
            return;
        }

        if (mMediaSession.isActive() == active) {
            Log.e(TAG, "Unable to set new active state. Current active state " +
                    "is the same as the new active state");
            return;
        }

        Log.d(TAG, "MediaSession setActive:" + active);
        mMediaSession.setActive(active);
    }

    /**
     * Release media session.
     */
    protected void release() {

        if (mMediaSession == null) {
            return;
        }

        mMediaSession.release();
        mMediaSession = null;
        Log.d(TAG, "Media session released");
    }

    /**
     * Helper function to help build the playback state
     *
     * @param state            The playback state
     * @param playbackPosition The playback position of the current media
     */
    private void setMediaSessionPlaybackState(final int state, final long playbackPosition) {

        if (mMediaSession == null) {
            return;
        }
        PlaybackState.Builder builder = new PlaybackState.Builder();
        builder.setState(state, playbackPosition, 1.0f);
        builder.setActions(mActions);

        mMediaSession.setPlaybackState(builder.build());
    }

}