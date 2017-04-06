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
import com.amazon.utils.StringManipulation;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * A helper class for the use of the content database. The content database is comprised of two
 * database tables: {@link RecentTable} and {@link RecommendationTable}. They are used to store
 * {@link RecentRecord} and {@link RecommendationRecord} respectively. This helper class contains
 * CRUD methods for both types of records.
 */
public class ContentDatabaseHelper extends SQLiteOpenHelper {

    /**
     * Debug tag.
     */
    private static final String TAG = ContentDatabaseHelper.class.getSimpleName();

    /**
     * The database name.
     */
    private static final String DATABASE_NAME = "content.db";

    /**
     * The database version. If this is changed onUpgrade will be called. Put any logic needed to
     * change or maintain database in that method.
     */
    private static int DATABASE_VERSION = 1;

    /**
     * The SQLiteDatabase instance.
     */
    private SQLiteDatabase mDB;

    /**
     * The Content Database instance.
     */
    private static ContentDatabaseHelper sInstance;

    /**
     * Get the content database helper instance.
     *
     * @param context The context.
     * @return The helper instance.
     */
    public static ContentDatabaseHelper getInstance(Context context) {

        if (sInstance == null) {
            synchronized (ContentDatabaseHelper.class) {
                if (sInstance == null) {
                    if (context == null) {
                        return null;
                    }
                    sInstance = new ContentDatabaseHelper(context);
                }
            }
        }
        return sInstance;
    }

    /**
     * Construct a database that has {@link com.amazon.android.model.content.Content} related
     * tables: one for saving playback states; one for saving recommendations.
     *
     * @param context The context.
     */
    private ContentDatabaseHelper(Context context) {

        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    /**
     * Get the full path string to the database file.
     *
     * @param context The context.
     * @return The full path to the database file.
     */
    String getDatabasePath(Context context) {
        // Database Name -->  '/data/data/com.amazon.android.calypso/databases/content.db'
        return context.getDatabasePath(ContentDatabaseHelper.DATABASE_NAME).getPath();
    }

    /**
     * Get the instance of a writable database.
     *
     * @return The database instance.
     */
    private SQLiteDatabase getDatabaseInstance() {

        if (mDB == null) {
            mDB = getWritableDatabase();
        }
        return mDB;
    }

    @Override
    public void onCreate(SQLiteDatabase db) {

        Log.i(TAG, "Creating database version " + DATABASE_VERSION);
        createTables(db);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Insert logic here if you need to update database. Please try not to destroy all old
        // user data.
    }

    /**
     * Create the tables for the database.
     *
     * @param db The SQLiteDatabase.
     * @return True if the tables were created without error; false otherwise.
     */
    private boolean createTables(SQLiteDatabase db) {

        try {
            db.execSQL(RecommendationTable.SQL_CREATE_TABLE);
            db.execSQL(RecentTable.SQL_CREATE_TABLE);
        }
        catch (Exception e) {
            Log.e(TAG, "Error creating database tables: " + e);
            return false;
        }
        return true;
    }

    /**
     * Delete the database.
     *
     * @return True if the database was deleted; false otherwise.
     */
    boolean deleteDatabase(Context context) {

        File databasePath = new File(getDatabasePath(context));

        Log.i(TAG, "deleteDatabase: " + databasePath.getAbsolutePath() + ", exists = " +
                databasePath.exists());

        return !databasePath.exists() || context.deleteDatabase(DATABASE_NAME);
    }

    /**
     * Clear all records from the database, for all tables.
     */
    public void clearDatabase() {

        SQLiteDatabase db = getDatabaseInstance();
        RecommendationTable.deleteAll(db);
        RecentTable.deleteAll(db);
    }

    /**
     * Returns if a recent record exists in the database given a content id.
     *
     * @param contentId The content id of the record.
     * @return True if the record exists; false otherwise.
     */
    public boolean recentRecordExists(String contentId) {

        SQLiteDatabase db = getDatabaseInstance();
        long rowId = RecentTable.findRowId(db, contentId);
        return (rowId != -1);
    }

    /**
     * Store or update a recently played content in the database. If an existing entry is found for
     * the given content id, the record is updated with the new information.
     *
     * @param contentId         The id of recently played content.
     * @param playbackLocation  The current playback location to store.
     * @param playbackCompleted True if the user played the whole content; false otherwise.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecent(String contentId, long playbackLocation, boolean playbackCompleted,
                             long lastWatchedTime) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when saving a recent content to database.");
            return false;
        }
        SQLiteDatabase db = getDatabaseInstance();

        long rowId = RecentTable.write(db, new RecentRecord(contentId, playbackLocation,
                                                            playbackCompleted, lastWatchedTime));

        return rowId != -1;
    }

    /**
     * Delete a recent record found using the given content id.
     *
     * @param contentId The content id.
     * @return True if the record was deleted; false otherwise.
     */
    boolean deleteRecent(String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null or empty when deleting a recent content from " +
                    "database");
            return false;
        }
        return RecentTable.delete(getDatabaseInstance(), contentId);
    }

    /**
     * Get the recent record from the database using the given content id.
     *
     * @param contentId The content id.
     * @return The recent record or null.
     */
    public RecentRecord getRecent(String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when reading a recent content from database.");
            return null;
        }

        SQLiteDatabase db = getDatabaseInstance();

        return RecentTable.read(db, contentId);
    }

    /**
     * Store or update a recommendation in the database. If an existing entry is found for
     * the given recommendation id, the record is updated with the new information.
     *
     * @param contentId        The content id.
     * @param recommendationId The recommendation id.
     * @param type             The recommendation type.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecommendation(String contentId, int recommendationId, String type) {

        if (StringManipulation.isNullOrEmpty(contentId) || recommendationId <= 0 ||
                StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Parameters cannot be null or 0 when saving a recommendation to " +
                    "database: contentId=" + contentId + ", recommendationId=" + recommendationId
                    + ", type=" + type);
            return false;
        }
        SQLiteDatabase db = getDatabaseInstance();

        long rowId = RecommendationTable.write(db, new RecommendationRecord(contentId,
                                                                            recommendationId,
                                                                            type));
        return rowId != -1;
    }

    /**
     * Store or update a recommendation in the database. If an existing entry is found for
     * the given recommendation id, the record is updated with the new information.
     *
     * @param record The record to update.
     * @return True if a record was entered or updated in the database; false otherwise.
     */
    public boolean addRecommendation(RecommendationRecord record) {

        return addRecommendation(record.getContentId(), record.getRecommendationId(),
                                 record.getType());
    }

    /**
     * Tests for the existence of a recommendation by content id.
     *
     * @param contentId The content id.
     * @return True if a recommendation record exists with the given id; false otherwise.
     */
    public boolean recommendationWithContentIdExists(String contentId) {

        SQLiteDatabase db = getDatabaseInstance();
        long rowId = RecommendationTable.findRowId(db, contentId);
        return (rowId != -1);
    }

    /**
     * Deletes the recommendation record for the given content id.
     *
     * @param contentId The content id.
     * @return True if the record was deleted; false otherwise.
     */
    boolean deleteRecommendationByContentId(String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null or empty when deleting a recommendation from " +
                    "database");
            return false;
        }
        return RecommendationTable.delete(getDatabaseInstance(), contentId);
    }

    /**
     * Deletes the recommendation record for the given recommendation id.
     *
     * @param recommendationId The recommendation id.
     * @return True if the record was deleted; false otherwise.
     */
    public boolean deleteRecommendationByRecId(long recommendationId) {

        return RecommendationTable.delete(getDatabaseInstance(), recommendationId);
    }

    /**
     * Deletes all recommendation records with the given type.
     *
     * @param type The recommendation type.
     * @return True if at least one record was deleted; false otherwise.
     */
    boolean deleteAllRecommendationsWithType(String type) {

        if (StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Type cannot be null or empty when deleting recommendations by type from " +
                    "database");
            return false;
        }

        return RecommendationTable.deleteRecommendationAllWithType(getDatabaseInstance(), type);
    }

    /**
     * Gets the recommendation record for the given content id.
     *
     * @param contentId The content id.
     * @return The recommendation record.
     */
    public RecommendationRecord getRecommendationByContentId(String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when reading a recommendation from database.");
            return null;
        }

        SQLiteDatabase db = getDatabaseInstance();

        return RecommendationTable.read(db, contentId);
    }

    /**
     * Gets the recommendation record for the given recommendation id.
     *
     * @param recommendationId The recommendation id.
     * @return The recommendation record.
     */
    RecommendationRecord getRecommendationByRecId(long recommendationId) {

        if (recommendationId <= 0) {
            Log.e(TAG, "Recommendation id cannot be 0 or negative when reading a recommendation " +
                    "from database.");
            return null;
        }

        SQLiteDatabase db = getDatabaseInstance();

        return RecommendationTable.read(db, recommendationId);
    }

    /**
     * Get all recommendation records with the given type.
     *
     * @param type The recommendation type.
     * @return List of recommendation records.
     */
    public List<RecommendationRecord> getRecommendationsWithType(String type) {

        if (StringManipulation.isNullOrEmpty(type)) {
            Log.e(TAG, "Type parameter cannot be null or empty when reading recommendations from " +
                    "database.");

            return null;
        }

        SQLiteDatabase db = getDatabaseInstance();

        return RecommendationTable.readMultipleRecords(db, RecommendationTable
                .getRecommendationTableSelectTypeQuery(type));
    }

    /**
     * Update a recommendation record.
     *
     * @param record The record.
     * @return The row of the updated record or -1 if there was an error when updating.
     */
    public long updateRecommendation(RecommendationRecord record) {

        return RecommendationTable.write(getDatabaseInstance(), record);
    }

    /**
     * Giving a list of content ids, return a list of records for any existing records with an id
     * from the list.
     *
     * @param contentIds List of content ids.
     * @return List of records containing the the same content ids.
     */
    public List<RecommendationRecord> getExistingRecommendationsByContentIds(List<String>
                                                                                     contentIds) {

        List<RecommendationRecord> matchingRecords = new ArrayList<>();
        for (String contentId : contentIds) {
            if (recommendationWithContentIdExists(contentId)) {
                matchingRecords.add(getRecommendationByContentId(contentId));
            }
        }
        return matchingRecords;
    }

    /**
     * Get the count of recommendation records in the database.
     *
     * @return The number of recommendations.
     */
    public int getRecommendationsCount() {

        SQLiteDatabase db = getDatabaseInstance();
        return RecommendationTable.getRecommendationsCount(db);
    }

    /**
     * Get the count of recent records in the database.
     *
     * @return The number of recent records.
     */
    int getRecentCount() {

        SQLiteDatabase db = getDatabaseInstance();
        return RecentTable.getRecentCount(db);
    }

    /**
     * Get the list of expired recommendation records from the database. Recommendations are
     * expired if their expiration date has been reached.
     *
     * @return The list of expired recommendations.
     */
    public List<RecommendationRecord> getExpiredRecommendations() {

        return RecommendationTable.getExpiredRecommendations(getDatabaseInstance(),
                                                             DateAndTimeHelper.getCurrentDate()
                                                                              .getTime());
    }

    /**
     * Removes expired records from all tables of the database. Recommendations are
     * expired if their expiration date has been reached.
     *
     * @return True if at least one record was removed; false otherwise.
     */
    public boolean removeExpiredRecords() {

        SQLiteDatabase db = getDatabaseInstance();
        return RecentTable.purge(db) && RecommendationTable.purge(db);

    }

    /**
     * Get a list of recommendation ids from the database.
     *
     * @return A list of ids.
     */
    public List<Integer> getAllRecommendationsIds() {

        return RecommendationTable.getRecommendationIds(getDatabaseInstance());
    }
}


