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
package com.amazon.android.contentbrowser.database.helpers;


import com.amazon.android.contentbrowser.database.records.RecentRecord;
import com.amazon.android.contentbrowser.database.tables.RecentTable;
import com.amazon.utils.StringManipulation;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Helper database class that has all the functionality specific to the {@link RecentTable}.
 */
public class RecentDatabaseHelper extends DatabaseHelper {
    
    private static final String TAG = RecentDatabaseHelper.class.getSimpleName();
    
    /**
     * Static instance of helper.
     */
    private static RecentDatabaseHelper sInstance;
    
    /**
     * Get the recent database helper instance.
     *
     * @return The helper instance.
     */
    public static RecentDatabaseHelper getInstance() {
        
        if (sInstance == null) {
            synchronized (RecentDatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new RecentDatabaseHelper();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * Constructor.
     */
    private RecentDatabaseHelper() {
        
        super(new RecentTable());
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public RecentRecord getRecord(Context context, String contentId) {
        
        return (RecentRecord) super.getRecord(context, contentId);
    }
    
    /**
     * Store or update a recently played content in the database. If an existing entry is found for
     * the given content id, the record is updated with the new information.
     *
     * @param contentId         The id of recently played content.
     * @param playbackLocation  The current playback location to store.
     * @param playbackCompleted True if the user played the whole content; false otherwise.
     * @param lastWatchedTime   The last watched time.
     * @param duration          The playback duration to store.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecord(Context context, String contentId, long playbackLocation,
                             boolean playbackCompleted, long lastWatchedTime, long duration) {
        
        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when saving a recent content to database.");
            return false;
        }

        return writeRecord(context, new RecentRecord(contentId, playbackLocation, playbackCompleted,
                                                     lastWatchedTime, duration));
    }
    
    /**
     * Get a list of recent records in which playback has started and has not been completed.
     *
     * @param gracePeriod The grace period for video playback.
     * @return A list of recent records.
     */
    public List<RecentRecord> getUnfinishedRecords(Context context, int gracePeriod) {
        
        return ((RecentTable) getTable()).getUnFinishedRecords(getDatabase(context), gracePeriod);
        
    }
}
