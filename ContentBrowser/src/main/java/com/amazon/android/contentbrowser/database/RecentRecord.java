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
package com.amazon.android.contentbrowser.database;

/**
 * A record for the recent database that represents a content that was recently played.
 */
public class RecentRecord {

    /**
     * The content id.
     */
    private String mContentId;

    /**
     * The last-known playback location
     */
    private long mPlaybackLocation;

    /**
     * Flag for playback completion.
     */
    private boolean mPlaybackComplete;

    /**
     * The date time of when the content was last watched.
     */
    private long mLastWatched;

    /**
     * The recent record constructor.
     *
     * @param id       The content id.
     * @param location The playback location.
     * @param complete True if playback is complete; false otherwise.
     */
    RecentRecord(String id, long location, boolean complete, long lastWatched) {

        mContentId = id;
        mPlaybackComplete = complete;
        mPlaybackLocation = location;
        mLastWatched = lastWatched;
    }

    /**
     * The recent record constructor.
     */
    RecentRecord() {

    }

    /**
     * Get the content id.
     *
     * @return The content id.
     */
    public String getContentId() {

        return mContentId;
    }

    /**
     * Set the content id.
     *
     * @param contentId The content id.
     */
    public void setContentId(String contentId) {

        mContentId = contentId;
    }

    /**
     * Get the last known playback location.
     *
     * @return The playback location.
     */
    public long getPlaybackLocation() {

        return mPlaybackLocation;
    }

    /**
     * Set the last known playback location.
     *
     * @param playbackLocation The playback location.
     */
    public void setPlaybackLocation(long playbackLocation) {

        mPlaybackLocation = playbackLocation;
    }

    /**
     * Test for playback completion.
     *
     * @return True if playback is completed; false otherwise.
     */
    public boolean isPlaybackComplete() {

        return mPlaybackComplete;
    }

    /**
     * Set playback completed.
     *
     * @param playbackComplete True if playback is completed; false otherwise.
     */
    public void setPlaybackComplete(boolean playbackComplete) {

        mPlaybackComplete = playbackComplete;
    }

    /**
     * Get the last watched time of the content in milliseconds (EPOCH).
     *
     * @return The last watched time.
     */
    public long getLastWatched() {

        return mLastWatched;
    }

    /**
     * Set the last watched time of the content in milliseconds (EPOCH).
     *
     * @param lastWatched The last watched time.
     */
    public void setLastWatched(long lastWatched) {

        mLastWatched = lastWatched;
    }

    @Override
    public boolean equals(Object object) {

        if (this == object) return true;
        if (object == null || getClass() != object.getClass()) return false;

        RecentRecord record = (RecentRecord) object;

        return getContentId() != null ? getContentId().equals(record.getContentId()) : record
                .getContentId() == null && getPlaybackLocation() == record.getPlaybackLocation()
                && isPlaybackComplete() == record.isPlaybackComplete()
                && getLastWatched() == record.getLastWatched();
    }

    @Override
    public String toString() {

        return "RecentRecord{" +
                "mContentId='" + mContentId + '\'' +
                ", mPlaybackLocation=" + mPlaybackLocation +
                ", mPlaybackComplete=" + mPlaybackComplete +
                ", mLastWatched=" + mLastWatched +
                '}';
    }
}
