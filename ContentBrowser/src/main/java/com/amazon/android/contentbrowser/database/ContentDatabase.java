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

import com.amazon.android.contentbrowser.database.tables.RecentTable;
import com.amazon.android.contentbrowser.database.tables.RecommendationTable;
import com.amazon.android.contentbrowser.database.tables.WatchlistTable;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;

import java.io.File;

/**
 * The content database. Contains the SQLiteDatabase instance that has three tables in it.
 */
public class ContentDatabase extends SQLiteOpenHelper {
    
    /**
     * Debug tag.
     */
    private static final String TAG = ContentDatabase.class.getSimpleName();
    
    /**
     * The database name.
     */
    public static final String DATABASE_NAME = "content.db";
    
    /**
     * The database version. If this is changed onUpgrade will be called. Put any logic needed to
     * change or maintain database in that method.
     */
    private static int DATABASE_VERSION = 3;
    
    /**
     * The SQLiteDatabase instance.
     */
    private SQLiteDatabase mDB;
    
    /**
     * The static instance.
     */
    private static ContentDatabase sInstance;
    
    /**
     * Constructor
     *
     * @param context The context.
     */
    private ContentDatabase(Context context) {
        
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    
    /**
     * Get the content database helper instance.
     *
     * @param context The context.
     * @return The helper instance.
     */
    public static ContentDatabase getInstance(Context context) {
        
        if (sInstance == null) {
            synchronized (ContentDatabase.class) {
                if (sInstance == null) {
                    if (context == null) {
                        return null;
                    }
                    sInstance = new ContentDatabase(context);
                }
            }
        }
        return sInstance;
    }
    
    /**
     * Get the instance of a writable database.
     *
     * @return The database instance.
     */
    public SQLiteDatabase getDatabaseInstance() {
        
        if (mDB == null) {
            mDB = getWritableDatabase();
        }
        return mDB;
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(SQLiteDatabase db) {
        
        Log.i(TAG, "Creating database version " + DATABASE_VERSION);
        createTables(db);
    }
    
    /**
     * {@inheritDoc}
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        
        Log.i(TAG, "Upgrading database from version " + oldVersion + " to " + newVersion);
        // Insert logic here if you need to update database. Please try not to destroy all old
        // user data.
        if ((oldVersion < 2) && newVersion >= 2) {
            db.execSQL(RecentTable.SQL_ALTER_TO_VERSION_2);
        }
        if (oldVersion < 3 && newVersion >= 3) {
            db.execSQL(WatchlistTable.SQL_CREATE_TABLE);
        }
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
            db.execSQL(WatchlistTable.SQL_CREATE_TABLE);
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
        
        return !databasePath.exists() || context.deleteDatabase(ContentDatabase.DATABASE_NAME);
    }
    
    /**
     * Get the full path string to the database file.
     *
     * @param context The context.
     * @return The full path to the database file.
     */
    String getDatabasePath(Context context) {
        // Database Name -->  '/data/data/com.fireappbuilder.android.calypso/databases/content.db'
        return context.getDatabasePath(ContentDatabase.DATABASE_NAME).getPath();
    }
}
