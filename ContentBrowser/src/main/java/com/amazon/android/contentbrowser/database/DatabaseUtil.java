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

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

/**
 * Util class for database functionality.
 */
class DatabaseUtil {

    /**
     * A helper function to query the database for a specific row.
     *
     * @param db    The database to query.
     * @param query The query to run.
     * @return The row id, or -1 if the query resulted in zero results.
     */
    static long findRowId(SQLiteDatabase db, String query) {

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
     * Delete the record by content id from the table.
     *
     * @param tableName The name of the table.
     * @param column    The name of the content id column.
     * @param db        The database.
     * @param contentId The content id of the record to deleteByContentId.
     * @return True if a row was deleted; false otherwise.
     */
    static boolean deleteByContentId(String tableName, String column, SQLiteDatabase db,
                                     String contentId) {

        int affectedRows = db.delete(tableName, column + "='" + contentId + "'", null);

        return (affectedRows > 0);
    }

    /**
     * Delete expired records from the table.
     *
     * @param db           The database.
     * @param tableName    The table name.
     * @param expireColumn The expired column tag.
     * @param currentTime  The current time to use to determine if the record is expired.
     * @return True if at least one record was deleted; false otherwise.
     */
    static boolean deleteExpired(SQLiteDatabase db, String tableName, String expireColumn, long
            currentTime) {

        int affectedRows = db.delete(tableName, expireColumn + " - " + currentTime + " <= 0", null);

        return (affectedRows > 0);
    }

    /**
     * Get the count of records in a table.
     *
     * @param db        The database.
     * @param tableName The table name.
     * @return The count.
     */
    static int getCount(SQLiteDatabase db, String tableName) {

        int count = -1;
        Cursor cursor = db.rawQuery("SELECT * FROM " + tableName, null);
        if (cursor != null) {
            count = cursor.getCount();
            cursor.close();

        }
        return count;
    }
}
