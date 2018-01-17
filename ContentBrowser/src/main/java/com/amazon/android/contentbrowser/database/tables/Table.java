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

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.provider.BaseColumns;
import android.util.Log;

import com.amazon.android.contentbrowser.database.records.Record;

import java.util.ArrayList;
import java.util.List;

/**
 * Base class for the tables of the content database. Handles common functionality amongst all the
 * tables.
 */
public abstract class Table implements BaseColumns {

    private static final String TAG = Table.class.getSimpleName();

    /**
     * The content id column.
     */
    public static final String COLUMN_CONTENT_ID = "content_id";

    /**
     * The string used in a SQL query to select all the columns.
     */
    private static final String SQL_SELECT_ALL_COLUMNS = "SELECT * FROM ";

    /**
     * The table's name.
     */
    private String mTableName;

    /**
     * Constructor.
     *
     * @param tableName The table's name.
     */
    Table(String tableName) {

        mTableName = tableName;
    }

    /**
     * Gets the query statement for selecting all columns from the table.
     *
     * @return The query statement.
     */
    String getSqlSelectAllColumnsQuery() {

        return SQL_SELECT_ALL_COLUMNS + mTableName;
    }

    /**
     * Gets the string used in a SQL query to drop the table.
     *
     * @return The query statement.
     */
    String getSqlDropTableQuery() {

        return "DROP TABLE IF EXISTS " + mTableName;
    }

    /**
     * Find the row for the record in the database containing the given content id.
     *
     * @param db        The database to query.
     * @param contentId The content id of the row to find.
     * @return The row id, or -1 if the query resulted in zero results.
     */
    public long findRowId(SQLiteDatabase db, String contentId) {

        String query = "SELECT " + _ID +
                " FROM " + mTableName +
                " WHERE " + COLUMN_CONTENT_ID + "='" + contentId + "' ";

        Cursor cursor = db.rawQuery(query, null);

        long rowId = -1;

        if (cursor != null && cursor.moveToFirst()) {
            rowId = cursor.getLong(0);
        }

        if (cursor != null) {
            cursor.close();
        }

        return rowId;
    }

    /**
     * Deletes all records in the table.
     *
     * @param db The database.
     */
    public void deleteAll(SQLiteDatabase db) {

        Log.d(TAG, "Deleting all records from table " + mTableName);
        db.delete(mTableName, null, null);

    }

    /**
     * Delete the record by content id from the table.
     *
     * @param db        The database.
     * @param contentId The content id of the record to delete.
     * @return True if a row was deleted; false otherwise.
     */
    public boolean deleteByContentId(SQLiteDatabase db, String contentId) {

        Log.d(TAG, "Deleting from table " + mTableName + " record with content id " + contentId);
        int affectedRows = db.delete(mTableName, COLUMN_CONTENT_ID + "='" + contentId + "'", null);

        return (affectedRows > 0);
    }

    /**
     * Delete expired records from the table.
     *
     * @param db           The database.
     * @param expireColumn The expired column tag.
     * @param currentTime  The current time to use to determine if the record is expired.
     * @return True if at least one record was deleted; false otherwise.
     */
    boolean deleteExpired(SQLiteDatabase db, String expireColumn, long currentTime) {

        Log.d(TAG, "Deleting expired records from table " + mTableName);
        int affectedRows = db.delete(mTableName, expireColumn + " - " + currentTime + " <= 0",
                                     null);

        return (affectedRows > 0);
    }

    /**
     * Get the count of records in a table.
     *
     * @param db The database.
     * @return The count.
     */
    public int getCount(SQLiteDatabase db) {

        int count = -1;
        Cursor cursor = db.rawQuery(getSqlSelectAllColumnsQuery(), null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();

        }
        return count;
    }

    /**
     * Writes a record to the database. It first tries to find an existing record to update.
     * If no record was found, a new record is inserted into the database.
     *
     * @param db     The database.
     * @param record The record to write.
     * @return The row of the record or -1 if there was an error.
     */
    public long write(SQLiteDatabase db, Record record) {

        Log.d(TAG, "writing to database table " + mTableName + ": " + record.toString());

        ContentValues contentValues = writeContentValues(record);

        // Check if the row exists
        long rowId = findRowId(db, record.getContentId());
        if (rowId == -1) {
            rowId = db.insert(mTableName, null, contentValues);
        }
        else {
            rowId = db.update(mTableName, contentValues, _ID + "=" + rowId, null);
        }

        return rowId;
    }


    /**
     * Read a record from the database with the given content id.
     *
     * @param db        The database.
     * @param contentId The content id of the recommendation to read.
     * @return The record.
     */
    public Record read(SQLiteDatabase db, String contentId) {

        Cursor cursor = db.rawQuery(getSqlSelectAllColumnsQuery() + " WHERE " + COLUMN_CONTENT_ID
                                            + "='" +
                                            contentId + "' ", null);

        return readSingleRecord(cursor);
    }

    /**
     * Reads a record from a cursor, closes the cursor once finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The record.
     */
    Record readSingleRecord(Cursor cursor) {

        Record record = null;

        if (cursor != null && cursor.moveToFirst()) {

            record = readRecordFromCursor(cursor);

        }
        if (cursor != null) {
            cursor.close();
        }
        return record;

    }

    /**
     * Reads records from the database that are returned as a result of the query.
     *
     * @param db    The database.
     * @param query The query
     * @return List of records.
     */
    public List<? extends Record> readMultipleRecords(SQLiteDatabase db, String query) {

        List<Record> records = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                Record record = readRecordFromCursor(cursor);
                if (record != null) {
                    records.add(record);
                }
            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return records;
    }

    /**
     * Reads a record from a cursor. Does not close the cursor when finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The record.
     */
    public abstract Record readRecordFromCursor(Cursor cursor);

    /**
     * Fills the content values with the necessary information to save the record to the database.
     *
     * @param record The record.
     * @return The content values.
     */
    public abstract ContentValues writeContentValues(Record record);

    /**
     * Purges all expired records from the database.
     *
     * @param db The database.
     * @return True if at least one record was deleted; false otherwise.
     */
    public abstract boolean purge(SQLiteDatabase db);
}
