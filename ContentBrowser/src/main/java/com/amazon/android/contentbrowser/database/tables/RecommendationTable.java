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

import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.contentbrowser.database.records.Record;
import com.amazon.utils.DateAndTimeHelper;

import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
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
public class RecommendationTable extends Table {
    
    /**
     * Debug tag.
     */
    private static final String TAG = RecommendationTable.class.getSimpleName();
    
    /**
     * Name of table.
     */
    private static final String TABLE_NAME = "recommendation";
    
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
    public static int RECORD_TTL = 432000;
    
    /**
     * The string used in a SQL query to create the recommendation table.
     */
    public static final String SQL_CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME + " (" +
                    _ID + " INTEGER PRIMARY KEY, " +
                    COLUMN_CONTENT_ID + " TEXT, " +
                    COLUMN_RECOMMENDATION_ID + " INTEGER, " +
                    COLUMN_TYPE + " TEXT, " +
                    COLUMN_EXPIRATION + " INTEGER)";
    
    /**
     * The string used to select the recommendation id column.
     */
    private final String SQL_SELECT_REC_ID_COLUMN = "SELECT " + COLUMN_RECOMMENDATION_ID +
            " FROM " + TABLE_NAME;
    
    /**
     * Constructor.
     */
    public RecommendationTable() {
        
        super(TABLE_NAME);
    }
    
    /**
     * Returns a query string that selects the given type of recommendations from the database
     * in ascending order.
     *
     * @param type The recommendation type.
     * @return The SQL query.
     */
    public String getRecommendationTableSelectTypeQuery(String type) {
        
        return getSqlSelectAllColumnsQuery() + " WHERE " + COLUMN_TYPE + "='" +
                type + "' ORDER BY " + COLUMN_EXPIRATION + " ASC ";
    }
    
    /**
     * Returns a query string that selects expired recommendations.
     *
     * @param currentTime The time to use to calculate if the recommendation is expired.
     * @return The SQL query.
     */
    private String getSqlSelectExpiredQuery(long currentTime) {
        
        return getSqlSelectAllColumnsQuery() +
                " WHERE " + COLUMN_EXPIRATION + " - " + currentTime + " <= 0";
    }
    
    
    /**
     * Delete the record by recommendation id.
     *
     * @param db               The database.
     * @param recommendationId The recommendation id of the record to deleteByRecommendationId.
     * @return True if a row was deleted; false otherwise.
     */
    public boolean deleteByRecommendationId(SQLiteDatabase db, long recommendationId) {
        
        int affectedRows = db.delete(TABLE_NAME, COLUMN_RECOMMENDATION_ID + "=" +
                recommendationId + " ", null);
        Log.d(TAG, "deleting recommendation with id " + recommendationId);
        return (affectedRows > 0);
    }
    
    /**
     * Delete all records with the given type.
     *
     * @param db   The database.
     * @param type The type of recommendation to deleteByRecommendationId.
     * @return True if at least one row was deleted; false otherwise.
     */
    public boolean deleteAllRecordsWithType(SQLiteDatabase db, String type) {
        
        Log.d(TAG, "deleting all recommendation records with type " + type);
        int affectedRows = db.delete(TABLE_NAME, COLUMN_TYPE + "='" + type + "' ", null);
        
        return (affectedRows > 0);
    }
    
    /**
     * Read a recommendation record from the database with the given recommendation id.
     *
     * @param db               The database.
     * @param recommendationId The recommendation id of the recommendation to read.
     * @return The recommendation record.
     */
    public RecommendationRecord read(SQLiteDatabase db, long recommendationId) {
        
        Cursor cursor = db.rawQuery(getSqlSelectAllColumnsQuery() + " WHERE " +
                                            COLUMN_RECOMMENDATION_ID + "=" + recommendationId +
                                            " ", null);
        
        return (RecommendationRecord) readSingleRecord(cursor);
    }
    
    
    /**
     * Reads a recommendation record from a cursor. Does not close the cursor when finished.
     *
     * @param cursor The cursor containing the data to read.
     * @return The recommendation record.
     */
    @Override
    public RecommendationRecord readRecordFromCursor(Cursor cursor) {
        
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
     * Fills the content values with the necessary information to save the recommendation record to
     * the database.
     *
     * @param record The record.
     * @return The content values.
     */
    @Override
    public ContentValues writeContentValues(Record record) {
        
        RecommendationRecord recommendation = (RecommendationRecord) record;
        
        ContentValues contentValues = new ContentValues();
        
        contentValues.put(COLUMN_CONTENT_ID, recommendation.getContentId());
        contentValues.put(COLUMN_RECOMMENDATION_ID, recommendation.getRecommendationId());
        contentValues.put(COLUMN_TYPE, recommendation.getType());
        
        int ttl = RECORD_TTL;
        Date expiration = DateAndTimeHelper.addSeconds(DateAndTimeHelper.getCurrentDate(), ttl);
        contentValues.put(COLUMN_EXPIRATION, expiration.getTime());
        
        return contentValues;
        
        
    }
    
    /**
     * Get a list of recommendation ids from the database.
     *
     * @param db The database.
     * @return A list of recommendation ids.
     */
    public List<Integer> getRecommendationIds(SQLiteDatabase db) {
        
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
     * Get the list of expired recommendation records from the database.
     *
     * @param db          The database.
     * @param currentTime The time to use to calculate if the recommendation is expired.
     * @return The list of expired recommendation records.
     */
    public List<RecommendationRecord> getExpiredRecommendations(SQLiteDatabase db,
                                                                long currentTime) {
        
        return (List<RecommendationRecord>)
                readMultipleRecords(db, getSqlSelectExpiredQuery(currentTime));
    }
    
    /**
     * Purges all expired recommendation records from the database.
     *
     * @param db The database.
     * @return True if at least one record was deleted; false otherwise.
     */
    @Override
    public boolean purge(SQLiteDatabase db) {
        
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();
        return deleteExpired(db, COLUMN_EXPIRATION, currentTime);
    }
    
    
}
