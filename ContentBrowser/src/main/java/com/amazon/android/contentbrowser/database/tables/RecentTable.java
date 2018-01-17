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
package com.amazon.android.contentbrowser.database.tables;

import com.amazon.android.contentbrowser.database.records.RecentRecord;
import com.amazon.android.contentbrowser.database.records.Record;
import com.amazon.utils.DateAndTimeHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.Date;
import java.util.List;

/**
 * This class represents the database table that holds records for recently watched content. It
 * contains columns for the content id, the playback location, a playback completed flag, and the
 * expiration of the recent. There should be no duplicate recent records in the database.
 * Each content id should be unique.
 */
public class RecentTable extends Table {
    
    /**
     * The debug tag.
     */
    private static final String TAG = RecentTable.class.getSimpleName();
    
    /**
     * The table name.
     */
    private static final String TABLE_NAME = "recent";
    
    /**
     * The column for the saved playback location of the content in milliseconds.
     */
    private static final String COLUMN_PLAYBACK_LOCATION = "playback_location";
    
    /**
     * The column for the flag for if playback is complete or not.
     */
    private static final String COLUMN_COMPLETED = "completed";
    
    /**
     * The column for the expiration. Based on the TTL, the expiration date/time in UTC time as an
     * integer.
     */
    private static final String COLUMN_EXPIRATION = "expiration";
    
    /**
     * The column for the last watched time of the content in milliseconds (EPOCH).
     */
    private static final String COLUMN_LAST_WATCHED = "last_watched";
    
    /**
     * The column for the duration of the content in milliseconds.
     */
    private static final String COLUMN_DURATION = "duration";
    
    /**
     * Time to live value for database records in seconds. (12 days)
     */
    public static int RECORD_TTL = 1037000;
    
    /**
     * The string used in a SQL query to create the recent table.
     */
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTENT_ID + " TEXT, " +
                    COLUMN_PLAYBACK_LOCATION + " INTEGER, " +
                    COLUMN_COMPLETED + " INTEGER, " +
                    COLUMN_EXPIRATION + " INTEGER, " +
                    COLUMN_LAST_WATCHED + " INTEGER, " +
                    COLUMN_DURATION + " INTEGER)";
    
    /**
     * The string used in a SQL query to alter recent table for version 2.
     */
    public static final String SQL_ALTER_TO_VERSION_2 = "ALTER TABLE " + TABLE_NAME +
            " ADD COLUMN " + COLUMN_DURATION + " INTEGER DEFAULT 0";
    
    /**
     * Constructor.
     */
    public RecentTable() {
        
        super(TABLE_NAME);
    }
    
    /**
     * Purges all expired recent records from the database.
     *
     * @param db The database.
     * @return True if at least one record was deleteByRecommendationId; false otherwise.
     */
    @Override
    public boolean purge(SQLiteDatabase db) {
        
        Date current = DateAndTimeHelper.getCurrentDate();
        return deleteExpired(db, COLUMN_EXPIRATION, current.getTime());
    }
    
    /**
     * Get a list of recent records that have a playback greater than the grace period provided and
     * have not been completed.
     *
     * @param db          The database.
     * @param gracePeriod The grace period in milliseconds.
     * @return A list of recent records.
     */
    public List<RecentRecord> getUnFinishedRecords(SQLiteDatabase db, int gracePeriod) {
        
        return (List<RecentRecord>) readMultipleRecords(db, getSqlSelectAllColumnsQuery() + " " +
                "WHERE " + COLUMN_COMPLETED + "='0' AND " + COLUMN_PLAYBACK_LOCATION + ">'" +
                gracePeriod + "' ORDER BY " + COLUMN_LAST_WATCHED + " DESC ");
        
    }
    
    /**
     * Reads a recent record from a cursor. Does not close the cursor when finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The recent record.
     */
    @Override
    public RecentRecord readRecordFromCursor(Cursor cursor) {
        
        if (cursor == null) {
            return null;
        }
        
        int column = 1; // skipping 0 since that's the row id and we don't need it right now.
        
        RecentRecord record = new RecentRecord();
        
        record.setContentId(cursor.getString(column++));
        record.setPlaybackLocation(cursor.getLong(column++));
        record.setPlaybackComplete(cursor.getInt(column++) > 0);
        // Skip expiration
        column++;
        record.setLastWatched(cursor.getLong(column++));
        record.setDuration(cursor.getLong(column));
        
        Log.d(TAG, "read recent record: " + record.toString());
        
        return record;
    }
    
    /**
     * Fills the content values with the necessary information to save the recent record to the
     * database.
     *
     * @param record The record.
     * @return The content values.
     */
    @Override
    public ContentValues writeContentValues(Record record) {
        
        ContentValues contentValues = new ContentValues();
        
        
        RecentRecord recentRecord = (RecentRecord) record;
        
        contentValues.put(COLUMN_CONTENT_ID, recentRecord.getContentId());
        contentValues.put(COLUMN_PLAYBACK_LOCATION, recentRecord.getPlaybackLocation());
        contentValues.put(COLUMN_COMPLETED, recentRecord.isPlaybackComplete());
        Date expiration = DateAndTimeHelper.addSeconds(DateAndTimeHelper.getCurrentDate(),
                                                       RECORD_TTL);
        
        contentValues.put(COLUMN_EXPIRATION, expiration.getTime());// 12 days (in seconds)
        contentValues.put(COLUMN_LAST_WATCHED, recentRecord.getLastWatched());
        contentValues.put(COLUMN_DURATION, recentRecord.getDuration());
        return contentValues;
    }
}
