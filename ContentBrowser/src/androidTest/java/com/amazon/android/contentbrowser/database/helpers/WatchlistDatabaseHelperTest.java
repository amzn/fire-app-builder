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
import com.amazon.android.contentbrowser.database.records.WatchlistRecord;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.lang.reflect.Field;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
/**
 * Test class for {@link WatchlistDatabaseHelper}.
 */
public class WatchlistDatabaseHelperTest {
    
    /**
     * Clear out the singleton instance of the database helper so each test has a clean slate.
     */
    @Before
    public void resetDatabase() throws NoSuchFieldException, IllegalAccessException {
        
        Field instance = ContentDatabase.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
        
        instance = WatchlistDatabaseHelper.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Tests getting an instance of the recommendation database helper and the SQLite Database.
     */
    @Test
    public void testGetInstance() throws Exception {
        
        WatchlistDatabaseHelper instance = WatchlistDatabaseHelper.getInstance();
        assertNotNull(instance);
        
        SQLiteDatabase contentDatabase = instance.getDatabase(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);
    }
    
    /**
     * Tests clearing the database of all records.
     */
    @Test
    public void testClearDatabase() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        watchlistDatabaseHelper.addRecord(context, "ContentId1");
        
        watchlistDatabaseHelper.clearDatabase(context);
        
        assertFalse("Record should not exist after clearing database",
                    watchlistDatabaseHelper.recordExists(context, "ContentId1"));
        
        watchlistDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Test adding a watchlist record to the database.
     */
    @Test
    public void testAddRecord() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        assertTrue("Record should have been added.",
                   watchlistDatabaseHelper.addRecord(context, "id1"));
        assertTrue("Record should have been added.",
                   watchlistDatabaseHelper.addRecord(context, "id2"));
        assertFalse("Record should not have been added.",
                    watchlistDatabaseHelper.addRecord(context, null));
        assertFalse("Record should not have been added.",
                    watchlistDatabaseHelper.addRecord(context, ""));
        
        watchlistDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Test getting watch list records from the database.
     */
    @Test
    public void testGetRecord() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        WatchlistRecord record1 = new WatchlistRecord("id1");
        
        watchlistDatabaseHelper.addRecord(context, "id1");
        
        assertEquals("Record id1 should have been found in database.",
                     record1, watchlistDatabaseHelper.getRecord(context, "id1"));
        assertEquals("No record should have been found.",
                     null, watchlistDatabaseHelper.getRecord(context, "badId"));
        assertEquals("No record should have been found.",
                     null, watchlistDatabaseHelper.getRecord(context, ""));
        assertEquals("No record should have been found.",
                     null, watchlistDatabaseHelper.getRecord(context, null));
        
        watchlistDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Test getting all the content ids for the records in the database.
     */
    @Test
    public void testGetWatchlistContentIds() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        watchlistDatabaseHelper.addRecord(context, "id1");
        watchlistDatabaseHelper.addRecord(context, "id2");
        
        List<String> idsFromDb = watchlistDatabaseHelper.getWatchlistContentIds(context);
        assertEquals("Incorrect number of ids found from database.", 2, idsFromDb.size());
        assertEquals("Content id was not found in list.", "id1", idsFromDb.get(0));
        assertEquals("Content id was not found in list.", "id2", idsFromDb.get(1));
        
        watchlistDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Test deleting records from the database.
     */
    @Test
    public void testDeleteRecord() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        watchlistDatabaseHelper.addRecord(context, "id1");
        
        assertTrue("Record should have been deleted.",
                   watchlistDatabaseHelper.deleteRecord(context, "id1"));
        assertFalse("Record should not have been deleted.",
                    watchlistDatabaseHelper.deleteRecord(context, "id1"));
        assertEquals("No record should have been found.",
                     null, watchlistDatabaseHelper.getRecord(context, "id1"));
        
        watchlistDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Test if records exist in the database.
     */
    @Test
    public void testRecordExists() throws Exception {
        
        WatchlistDatabaseHelper watchlistDatabaseHelper = WatchlistDatabaseHelper.getInstance();
        assertNotNull(watchlistDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        watchlistDatabaseHelper.addRecord(context, "id2");
        
        assertTrue("Record should exist in database.",
                   watchlistDatabaseHelper.recordExists(context, "id2"));
        assertFalse("Record should not exist in database.",
                    watchlistDatabaseHelper.recordExists(context, "id1"));
        assertFalse("Record should not exist in database.",
                    watchlistDatabaseHelper.recordExists(context, ""));
        assertFalse("Record should not exist in database.",
                    watchlistDatabaseHelper.recordExists(context, null));
        
        watchlistDatabaseHelper.getDatabase(context).close();
        
    }
}
