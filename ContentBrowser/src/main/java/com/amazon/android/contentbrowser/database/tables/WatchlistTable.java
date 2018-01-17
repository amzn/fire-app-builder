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

import com.amazon.android.contentbrowser.database.records.Record;
import com.amazon.android.contentbrowser.database.records.WatchlistRecord;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

/**
 * This class represents the database table that holds records for watchlist items. It contains a
 * column for the content id. Each content id should be unique.
 */
public class WatchlistTable extends Table {

    /**
     * Debug tag.
     */
    private static final String TAG = WatchlistTable.class.getSimpleName();

    /**
     * Name of table.
     */
    private static final String TABLE_NAME = "watchlist";

    /**
     * The string used in a SQL query to create the watchlist table.
     */
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" + _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTENT_ID + " TEXT)";

    /**
     * Constructor.
     */
    public WatchlistTable() {

        super(TABLE_NAME);
    }

    /**
     * Reads a watchlist record from a cursor. Does not close the cursor when finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The watchlist record.
     */
    @Override
    public WatchlistRecord readRecordFromCursor(Cursor cursor) {

        if (cursor == null) {
            return null;
        }

        WatchlistRecord record = new WatchlistRecord();
        record.setContentId(cursor.getString(cursor.getColumnIndex(COLUMN_CONTENT_ID)));

        Log.d(TAG, "read record: " + record.toString());

        return record;
    }

    /**
     * Fills the content values with the necessary information to save the watchlist record to
     * the database.
     *
     * @param record The record.
     * @return The content values.
     */
    @Override
    public ContentValues writeContentValues(Record record) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_CONTENT_ID, record.getContentId());

        return contentValues;
    }

    /**
     * Currently no expiration exists in this database table.
     *
     * @param db The database.
     * @return False.
     */
    @Override
    public boolean purge(SQLiteDatabase db) {

        return false;
    }

    /**
     * Get a list of watchlist content ids.
     *
     * @param db The database instance.
     * @return List of strings.
     */
    public List<String> getContentIds(SQLiteDatabase db) {

        List<String> ids = new ArrayList<>();

        Cursor cursor = db.rawQuery(getSqlSelectAllColumnsQuery(), null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                ids.add(cursor.getString(1));
            } while (cursor.moveToNext());
        }
        if (cursor != null) {
            cursor.close();
        }

        return ids;

    }
}
