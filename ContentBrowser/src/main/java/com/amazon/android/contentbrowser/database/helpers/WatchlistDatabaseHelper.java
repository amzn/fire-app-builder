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

import com.amazon.android.contentbrowser.database.records.WatchlistRecord;
import com.amazon.android.contentbrowser.database.tables.WatchlistTable;
import com.amazon.utils.StringManipulation;

import android.content.Context;
import android.util.Log;

import java.util.List;

/**
 * Helper database class that has all the functionality specific to the {@link WatchlistTable}.
 */
public class WatchlistDatabaseHelper extends DatabaseHelper {
    
    private static final String TAG = WatchlistDatabaseHelper.class.getSimpleName();
    
    /**
     * The static helper instance.
     */
    private static WatchlistDatabaseHelper sInstance;
    
    /**
     * Get the watchlist database helper instance.
     *
     * @return The helper instance.
     */
    public static WatchlistDatabaseHelper getInstance() {
        
        if (sInstance == null) {
            synchronized (WatchlistDatabaseHelper.class) {
                if (sInstance == null) {
                    sInstance = new WatchlistDatabaseHelper();
                }
            }
        }
        return sInstance;
    }
    
    /**
     * Constructor.
     */
    private WatchlistDatabaseHelper() {
        
        super(new WatchlistTable());
    }
    
    /**
     * Add a watchlist record to the database for the given content id.
     *
     * @param contentId The content id.
     * @return True if the record was added; false otherwise.
     */
    public boolean addRecord(Context context, String contentId) {
        
        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when saving a recent content to database.");
            return false;
        }
        return writeRecord(context, new WatchlistRecord(contentId));
    }
    
    /**
     * Get a list of content ids from the watchlist records.
     *
     * @return A list containing all the content ids from the watchlist records.
     */
    public List<String> getWatchlistContentIds(Context context) {
        
        return ((WatchlistTable) getTable()).getContentIds(getDatabase(context));
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public WatchlistRecord getRecord(Context context, String contentId) {
        
        return (WatchlistRecord) super.getRecord(context, contentId);
    }
    
    
}
