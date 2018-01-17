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

import com.amazon.android.contentbrowser.database.ContentDatabase;
import com.amazon.android.contentbrowser.database.records.Record;
import com.amazon.android.contentbrowser.database.tables.Table;
import com.amazon.utils.StringManipulation;


import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

/**
 * A base helper class for the use of the content database. This helper class contains method
 * signatures for basic CRUD methods for the database. Other classes should extend this class and
 * implement the methods for a specific table in the content database.
 */
public abstract class DatabaseHelper {

    private static final String TAG = DatabaseHelper.class.getSimpleName();

    /**
     * The database table.
     */
    private Table mTable;

    /**
     * Constructor.
     * @param table The database table.
     */
    DatabaseHelper(Table table) {

        mTable = table;
    }

    /**
     * Get the database table instance.
     * @return The table.
     */
    Table getTable() {
        
        return mTable;
    }
    
    /**
     * Get the database.
     *
     * @param context The context.
     * @return The database instance.
     */
    public SQLiteDatabase getDatabase(Context context) {
        
        ContentDatabase database = ContentDatabase.getInstance(context);
        if (database != null) {
            return database.getDatabaseInstance();
        }
        return null;
    }
    
    /**
     * Delete a record from the database that matches the content id.
     *
     * @param context   The context.
     * @param contentId The content id.
     * @return True if the record was deleted; false otherwise.
     */
    public boolean deleteRecord(Context context, String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null or empty when deleting a record from database");
            return false;
        }
        return mTable.deleteByContentId(getDatabase(context), contentId);
    }
    
    /**
     * Clear the database of all records.
     *
     * @param context The context.
     */
    public void clearDatabase(Context context) {

        mTable.deleteAll(getDatabase(context));
    }
    
    /**
     * Get a record from the database that matches the content id.
     *
     * @param context   The context.
     * @param contentId The content id.
     * @return The record.
     */
    public Record getRecord(Context context, String contentId) {

        if (StringManipulation.isNullOrEmpty(contentId)) {
            Log.e(TAG, "Content id cannot be null when reading a recent content from database.");
            return null;
        }

        return mTable.read(getDatabase(context), contentId);
    }
    
    /**
     * Test for a existence of a record with the given content id in the database.
     *
     * @param context   The context.
     * @param contentId The content id.
     * @return True if a record was found for the content id; false otherwise.
     */
    public boolean recordExists(Context context, String contentId) {

        long rowId = mTable.findRowId(getDatabase(context), contentId);
        return (rowId != -1);
    }
    
    /**
     * Get the count of records in the database.
     *
     * @param context The context.
     * @return The number of records in the database.
     */
    public int getCount(Context context) {

        return mTable.getCount(getDatabase(context));
    }
    
    /**
     * Delete all expired records from the database.
     *
     * @param context The context.
     * @return True if at least one record was deleted; false otherwise.
     */
    public boolean purgeExpiredRecords(Context context) {

        return mTable.purge(getDatabase(context));
    }
    
    /**
     * Write the record to the table.
     *
     * @param context The context.
     * @param record The record to write.
     * @return True if the record was added; false otherwise.
     */
    boolean writeRecord(Context context, Record record) {

        long rowId = getTable().write(getDatabase(context), record);
        return (rowId != -1);
    }
}
