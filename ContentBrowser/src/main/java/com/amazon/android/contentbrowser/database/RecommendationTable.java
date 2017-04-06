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

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * This class represents the database table that holds recommendation records. It contains columns
 * for the content id, the recommendation id, the type of recommendation, and the
 * expiration of the recommendation. There should be no duplicate recommendations in the database.
 * Each content id and recommendation id should be unique.
 */
class RecommendationTable implements BaseColumns {

    /**
     * Debug tag.
     */
    private static final String TAG = RecommendationTable.class.getSimpleName();

    /**
     * Name of table.
     */
    private static final String TABLE_NAME = "recommendation";

    /**
     * The content id column.
     */
    private static final String COLUMN_CONTENT_ID = "content_id";

    /**
     * The recommendation id column.
     */
    private static final String COLUMN_RECOMMENDATION_ID = "recommendation_id";

    /**
     * The type of recommendation column.
     */
    private static final String COLUMN_TYPE = "recommendation_type";

    /**
     * The expiration column. Based on the TTL, the expiration date/time in UTC time as an integer.
     */
    private static final String COLUMN_EXPIRATION = "expiration";

    /**
     * Time to live value for database records in seconds. (5 days)
     */
    static int RECORD_TTL = 432000;

    /**
     * The string used in a SQL query to create the recommendation table.
     */
    static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTENT_ID + " TEXT, " +
                    COLUMN_RECOMMENDATION_ID + " INTEGER, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_EXPIRATION + " INTEGER)";

    /**
     * The string used to select the recommendation id column.
     */
    private static final String SQL_SELECT_REC_ID_COLUMN = "SELECT " +
            COLUMN_RECOMMENDATION_ID +
            " FROM " + TABLE_NAME;

    /**
     * The string used in a SQL query to select all the columns.
     */
    private static final String SQL_SELECT_ALL_COLUMNS = "SELECT " +
            _ID + ", " +
            COLUMN_CONTENT_ID + ", " +
            COLUMN_RECOMMENDATION_ID + ", " +
            COLUMN_TYPE + ", " +
            COLUMN_EXPIRATION +
            " FROM " + TABLE_NAME;

    /**
     * Returns a query string that selects the given type of recommendations from the database
     * in ascending order.
     *
     * @param type The recommendation type.
     * @return The SQL query.
     */
    static String getRecommendationTableSelectTypeQuery(String type) {

        return SQL_SELECT_ALL_COLUMNS +
                " WHERE " + COLUMN_TYPE + "='" +
                type + "' ORDER BY " + COLUMN_EXPIRATION + " ASC ";
    }

    /**
     * Returns a query string that selects expired recommendations.
     *
     * @param currentTime The time to use to calculate if the recommendation is expired.
     * @return The SQL query.
     */
    private static String getSqlSelectExpiredQuery(long currentTime) {

        return SQL_SELECT_ALL_COLUMNS +
                " WHERE " + COLUMN_EXPIRATION + " - " + currentTime + " <= 0";
    }

    /**
     * The string used in a SQL query to drop the table.
     */
    static final String SQL_DROP_TABLE = "DROP TABLE IF EXISTS " + TABLE_NAME;

    /**
     * Find the record in the database containing the content id.
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
     * Find the record in the database containing the recommendation id.
     *
     * @param db               The database to query.
     * @param recommendationId The recommendation id to search for.
     * @return The row id, or -1 if the recommendation id match was not found.
     */
    static long findRowId(SQLiteDatabase db, long recommendationId) {

        return DatabaseUtil.findRowId(db, "SELECT " + _ID +
                " FROM " + TABLE_NAME +
                " WHERE " + COLUMN_RECOMMENDATION_ID + "='"
                + recommendationId + "' ");
    }

    /**
     * Writes a recommendation record to the database. This method adds the expiration to the
     * record. It tries to find an existing row in the database based on content id. If the row
     * exists, the record is just updated with the new info. If the row does not exist, a new row
     * is inserted in the database.
     *
     * @param db             The database.
     * @param recommendation The recommendation to write.
     * @return The row of the record or -1 if there was an error.
     */
    static long write(SQLiteDatabase db, RecommendationRecord recommendation) {

        ContentValues contentValues = new ContentValues();

        contentValues.put(COLUMN_CONTENT_ID, recommendation.getContentId());
        contentValues.put(COLUMN_RECOMMENDATION_ID, recommendation.getRecommendationId());
        contentValues.put(COLUMN_TYPE, recommendation.getType());

        int ttl = RECORD_TTL;
        Date expiration = DateAndTimeHelper.addSeconds(DateAndTimeHelper.getCurrentDate(), ttl);
        contentValues.put(COLUMN_EXPIRATION, expiration.getTime());

        // Check if the row exists
        long rowId = findRowId(db, recommendation.getContentId());
        if (rowId == -1) {
            rowId = db.insert(TABLE_NAME, null, contentValues);
            Log.d(TAG, "record inserted to database: " + recommendation.toString());
        }
        else {
            rowId = db.update(TABLE_NAME, contentValues, _ID + "=" + rowId, null);
            Log.d(TAG, "record updated in database: " + recommendation.toString());
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

        Log.d(TAG, "deleting recommendation with content id " + contentId);
        return DatabaseUtil.deleteByContentId(TABLE_NAME, COLUMN_CONTENT_ID, db, contentId);
    }

    /**
     * Delete the record by recommendation id.
     *
     * @param db               The database.
     * @param recommendationId The recommendation id of the record to delete.
     * @return True if a row was deleted; false otherwise.
     */
    static boolean delete(SQLiteDatabase db, long recommendationId) {

        int affectedRows = db.delete(TABLE_NAME, COLUMN_RECOMMENDATION_ID + "=" +
                recommendationId + " ", null);
        Log.d(TAG, "deleting recommendation with id " + recommendationId);
        return (affectedRows > 0);
    }

    /**
     * Delete all records with the given type.
     *
     * @param db   The database.
     * @param type The type of recommendation to delete.
     * @return True if at least one row was deleted; false otherwise.
     */
    static boolean deleteRecommendationAllWithType(SQLiteDatabase db, String type) {

        Log.d(TAG, "deleting all recommendation records with type " + type);
        int affectedRows = db.delete(TABLE_NAME, COLUMN_TYPE + "='" + type + "' ", null);

        return (affectedRows > 0);
    }

    /**
     * Read a recommendation record from the database with the given content id.
     *
     * @param db        The database.
     * @param contentId The content id of the recommendation to read.
     * @return The recommendation record.
     */
    static RecommendationRecord read(SQLiteDatabase db, String contentId) {

        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_COLUMNS + " WHERE " + COLUMN_CONTENT_ID + "='" +
                                            contentId + "' ", null);

        return readRecommendationRecord(cursor);
    }

    /**
     * Read a recommendation record from the database with the given recommendation id.
     *
     * @param db               The database.
     * @param recommendationId The recommendation id of the recommendation to read.
     * @return The recommendation record.
     */
    static RecommendationRecord read(SQLiteDatabase db, long recommendationId) {

        Cursor cursor = db.rawQuery(SQL_SELECT_ALL_COLUMNS + " WHERE " + COLUMN_RECOMMENDATION_ID
                                            + "=" + recommendationId + " ", null);

        return readRecommendationRecord(cursor);
    }

    /**
     * Reads recommendation records from the database that are returned as a result of the query.
     *
     * @param db    The database.
     * @param query The query
     * @return List of recommendation records.
     */
    static List<RecommendationRecord> readMultipleRecords(SQLiteDatabase db, String query) {

        List<RecommendationRecord> records = new ArrayList<>();

        Cursor cursor = db.rawQuery(query, null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                RecommendationRecord record = readRecommendationRecordFromCursor(cursor);
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
     * Reads a recommendation record from a cursor, closes the cursor once finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The recommendation record.
     */
    private static RecommendationRecord readRecommendationRecord(Cursor cursor) {

        RecommendationRecord record = null;

        if (cursor != null && cursor.moveToFirst()) {

            record = readRecommendationRecordFromCursor(cursor);

        }
        if (cursor != null) {
            cursor.close();
        }
        return record;

    }

    /**
     * Reads a recommendation record from a cursor. Does not close the cursor when finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The recommendation record.
     */
    private static RecommendationRecord readRecommendationRecordFromCursor(Cursor cursor) {

        if (cursor == null) {
            return null;
        }

        int column = 1; // skipping 0 since that's the row id and we don't need it right now.

        RecommendationRecord record = new RecommendationRecord();

        record.setContentId(cursor.getString(column++));
        record.setRecommendationId(cursor.getInt(column++));
        record.setType(cursor.getString(column));

        Log.d(TAG, "read record: " + record.toString());

        return record;
    }

    /**
     * Get a list of recommendation ids from the database.
     *
     * @param db The database.
     * @return A list of recommendation ids.
     */
    static List<Integer> getRecommendationIds(SQLiteDatabase db) {

        List<Integer> ids = new ArrayList<>();

        Cursor cursor = db.rawQuery(SQL_SELECT_REC_ID_COLUMN, null);

        if (cursor != null && cursor.moveToFirst()) {

            do {
                ids.add(cursor.getInt(0));

            } while (cursor.moveToNext());
        }

        if (cursor != null) {
            cursor.close();
        }

        return ids;
    }

    /**
     * Delete all records from the table.
     *
     * @param db The database.
     */
    static void deleteAll(SQLiteDatabase db) {

        db.delete(TABLE_NAME, null, null);
    }

    /**
     * Get the count of recommendation records in the database.
     *
     * @param db The database.
     * @return The number of recommendation records.
     */
    static int getRecommendationsCount(SQLiteDatabase db) {

        return DatabaseUtil.getCount(db, TABLE_NAME);

    }

    /**
     * Get the list of expired recommendation records from the database.
     *
     * @param db          The database.
     * @param currentTime The time to use to calculate if the recommendation is expired.
     * @return The list of expired recommendation records.
     */
    static List<RecommendationRecord> getExpiredRecommendations(SQLiteDatabase db,
                                                                long currentTime) {

        return readMultipleRecords(db, getSqlSelectExpiredQuery(currentTime));
    }

    /**
     * Purges all expired recommendation records from the database.
     *
     * @param db The database.
     * @return True if at least one record was delete; false otherwise.
     */
    static boolean purge(SQLiteDatabase db) {

        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();

        List<RecommendationRecord> records = getExpiredRecommendations(db, currentTime);

        return records.size() > 0 && DatabaseUtil.deleteExpired(db, TABLE_NAME,
                                                                COLUMN_EXPIRATION, currentTime);
    }


}
