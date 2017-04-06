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

import com.amazon.utils.DateAndTimeHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import java.util.Date;

/**
 * This class represents the database table that holds records for recently watched content. It
 * contains columns for the content id, the playback location, a playback completed flag, and the
 * expiration of the recommendation. There should be no duplicate recent records in the database.
 * Each content id should be unique.
 */
class RecentTable implements BaseColumns {

    /**
     * The debug tag.
     */
    private static final String TAG = RecentTable.class.getSimpleName();

    /**
     * The table name.
     */
    private static final String TABLE_NAME = "recent";

    /**
     * The content id column.
     */
    private static final String COLUMN_CONTENT_ID = "content_id";

    /**
     * The column for the saved playback location of the content.
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
     * Time to live value for database records in seconds. (12 days)
     */
    static int RECORD_TTL = 1037000;

    /**
     * The string used in a SQL query to create the recommendation table.
     */
    static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTENT_ID + " TEXT, " +
                    COLUMN_PLAYBACK_LOCATION + " INTEGER, " +
                    COLUMN_COMPLETED + " INTEGER, " +
                    COLUMN_EXPIRATION + " INTEGER, " +
                    COLUMN_LAST_WATCHED + " INTEGER)";
    /**
     * The string used in a SQL query to drop the table.
     */
    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * The string used in a SQL query to select all the columns.
     */
    private static final String SQL_SELECT_ALL_COLUMNS = "SELECT " +
            _ID + ", " +
            COLUMN_CONTENT_ID + ", " +
            COLUMN_PLAYBACK_LOCATION + ", " +
            COLUMN_COMPLETED + ", " +
            COLUMN_EXPIRATION + ", " +
            COLUMN_LAST_WATCHED +
            " FROM " + TABLE_NAME;

    /**
     * Find a record in the database containing the given content id.
     *
     * @param db        The database to query.
     * @param contentId The content id to search for.
     * @return The row id, or -1 if the content id match was not found.
     */
    static long findRowId(SQLiteDatabase db, String contentId) {

        return DatabaseUtil.findRowId(db, "SELECT " + _ID +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_CONTENT_ID + "='" + contentId + "' ");
    }

    /**
     * Writes a recent record to the database. It first tries to find an existing record to update.
     * If no record was found, a new record is inserted into the database.
     *
     * @param db     The database.
     * @param record The recent record to write.
     * @return The row of the record or -1 if there was an error.
     */
    static long write(SQLiteDatabase db, RecentRecord record) {

        Log.d(TAG, "writing to database: " + record.toString());

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_CONTENT_ID, record.getContentId());
        contentValues.put(COLUMN_PLAYBACK_LOCATION, record.getPlaybackLocation());
        contentValues.put(COLUMN_COMPLETED, record.isPlaybackComplete());
        Date expiration = DateAndTimeHelper.addSeconds(DateAndTimeHelper.getCurrentDate(),
                                                       RECORD_TTL);

        contentValues.put(COLUMN_EXPIRATION, expiration.getTime());// 12 days (in seconds)
        contentValues.put(COLUMN_LAST_WATCHED, record.getLastWatched());

        // Check if the row exists
        long rowId = findRowId(db, record.getContentId());
        if (rowId == -1) {
            rowId = db.insert(TABLE_NAME, null, contentValues);
        }
        else {
            rowId = db.update(TABLE_NAME, contentValues, _ID + "=" + rowId, null);
        }

        return rowId;
    }

    /**
     * Delete the record by content id.
     *
     * @param db        The database.
     * @param contentId The content id of the record to delete.
     * @return True if a row was deleted; false otherwise.
     */
    static boolean delete(SQLiteDatabase db, String contentId) {

        Log.d(TAG, "Deleting recent record for content id " + contentId);
        return DatabaseUtil.deleteByContentId(TABLE_NAME, COLUMN_CONTENT_ID, db, contentId);
    }

    /**
     * Read a recent record from the database with the given content id.
     *
     * @param db        The database.
     * @param contentId The content id of the recommendation to read.
     * @return The recent record.
     */
    static RecentRecord read(SQLiteDatabase db, String contentId) {

        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_COLUMNS + " WHERE " + COLUMN_CONTENT_ID + "='" +
                                            contentId + "' ", null);

        RecentRecord record = null;

        int column = 1; // skipping 0 since that's the row id and we don't need it right now.

        if (cursor != null && cursor.moveToFirst()) {

            record = new RecentRecord();

            record.setContentId(cursor.getString(column++));
            record.setPlaybackLocation(cursor.getLong(column++));
            record.setPlaybackComplete(cursor.getInt(column++) > 0);
            record.setLastWatched(cursor.getLong(column));

            Log.d(TAG, "read record: " + record.toString());
        }

        if (cursor != null) {
            cursor.close();
        }

        return record;
    }

    /**
     * Deletes all records in the table.
     *
     * @param db The database.
     */
    static void deleteAll(SQLiteDatabase db) {

        db.delete(TABLE_NAME, null, null);

    }

    /**
     * Purges all expired recommendation records from the database.
     *
     * @param db The database.
     * @return True if at least one record was delete; false otherwise.
     */
    public static boolean purge(SQLiteDatabase db) {

        Date current = DateAndTimeHelper.getCurrentDate();
        return DatabaseUtil.deleteExpired(db, TABLE_NAME, COLUMN_EXPIRATION, current.getTime());
    }

    /**
     * Get the count of recommendation records in the database.
     *
     * @param db The database.
     * @return The number of recommendation records.
     */
    public static int getRecentCount(SQLiteDatabase db) {

        return DatabaseUtil.getCount(db, TABLE_NAME);
    }
}
