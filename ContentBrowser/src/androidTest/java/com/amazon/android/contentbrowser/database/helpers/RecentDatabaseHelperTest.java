/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *      http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.contentbrowser.database.helpers;

import com.amazon.android.contentbrowser.database.ContentDatabase;
import com.amazon.android.contentbrowser.database.records.RecentRecord;
import com.amazon.android.contentbrowser.database.tables.RecentTable;
import com.amazon.utils.DateAndTimeHelper;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.lang.reflect.Field;

import static com.amazon.android.utils.Helpers.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
/**
 * Test class for {@link RecentDatabaseHelper}.
 */
public class RecentDatabaseHelperTest {
    
    /**
     * Clear out the singleton instance of the database helper so each test has a clean slate.
     */
    @Before
    public void resetDatabase() throws NoSuchFieldException, IllegalAccessException {
        
        Field instance = ContentDatabase.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
        
        instance = RecentDatabaseHelper.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Tests getting an instance of the recent database helper and the SQLite Database.
     */
    @Test
    public void testGetInstance() throws Exception {
        
        RecentDatabaseHelper instance = RecentDatabaseHelper.getInstance();
        assertNotNull(instance);
        
        SQLiteDatabase contentDatabase = instance.getDatabase(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);
    }
    
    /**
     * Tests putting a recent record in the database.
     */
    @Test
    public void testAddRecord() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();
        
        assertTrue("Record should have been successfully recorded in database.",
                   recentDatabaseHelper.addRecord(context, "ContentId1", 10, false, currentTime,
                                                  20));
        
        assertFalse("Record should not have been recorded in database since id is an empty string",
                    recentDatabaseHelper.addRecord(context, "", 0, true, currentTime, 0));
        
        assertFalse("Record should not have been recorded in database since id is null",
                    recentDatabaseHelper.addRecord(context, null, 0, false, currentTime, 0));
        
        assertTrue("ContentId1 should have been found in database.",
                   recentDatabaseHelper.recordExists(context, "ContentId1"));
        
        assertTrue("Record should have been successfully recorded in database.",
                   recentDatabaseHelper.addRecord(context, "ContentId2", 1000, true, currentTime,
                                                  2000));
        
        assertTrue("ContentId2 should have been found in database.",
                   recentDatabaseHelper.recordExists(context, "ContentId2"));
        
        recentDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests deleting recent records from the database.
     */
    @Test
    public void testDeleteRecord() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recentDatabaseHelper.addRecord(context, "ContentId1", 10, false,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20);
        
        assertTrue("ContentId1 should have been deleted from database.",
                   recentDatabaseHelper.deleteRecord(context, "ContentId1"));
        assertFalse("ContentId1 should not have been found in database.",
                    recentDatabaseHelper.recordExists(context, "ContentId1"));
        assertFalse("ContentId1 should not have been deleted from database since it doesn't exist.",
                    recentDatabaseHelper.deleteRecord(context, "ContentId1"));
        assertFalse("ContentId1 should have been deleted from database.",
                    recentDatabaseHelper.deleteRecord(context, ""));
        assertFalse("ContentId1 should have been deleted from database.",
                    recentDatabaseHelper.deleteRecord(context, null));
        
        recentDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests reading recent records from the database.
     */
    @Test
    public void testGetRecord() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();
        
        RecentRecord expectedContentId1 = new RecentRecord("ContentId1", 10, false, currentTime,
                                                           20);
        RecentRecord expectedContentId2 = new RecentRecord("ContentId2", 123456789, true,
                                                           currentTime, 987654321);
        
        recentDatabaseHelper.addRecord(context, "ContentId1", 10, false, currentTime, 20);
        recentDatabaseHelper.addRecord(context, "ContentId2", 123456789, true, currentTime,
                                       987654321);
        
        RecentRecord actualContentId1 = recentDatabaseHelper.getRecord(context, "ContentId1");
        assertTrue("The retrieved content should have matched.",
                   actualContentId1.equals(expectedContentId1));
        
        RecentRecord actualContentId2 = recentDatabaseHelper.getRecord(context, "ContentId2");
        assertTrue("The retrieved content should have matched.",
                   actualContentId2.equals(expectedContentId2));
        
        recentDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests clearing the database of all records.
     */
    @Test
    public void testClearDatabase() throws Exception {
        
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recentDatabaseHelper.addRecord(context, "ContentId1", 10, false,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20);
        
        recentDatabaseHelper.clearDatabase(context);
        
        assertFalse("Record should not exist after clearing database",
                    recentDatabaseHelper.recordExists(context, "ContentId1"));
        
        recentDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests getting the count of recent records in the database.
     */
    @Test
    public void testGetCount() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();
        
        recentDatabaseHelper.addRecord(context, "ContentId1", 10, false, currentTime, 20);
        recentDatabaseHelper.addRecord(context, "ContentId2", 10, false, currentTime, 20);
        recentDatabaseHelper.addRecord(context, "ContentId3", 10, false, currentTime, 20);
        
        assertEquals("There should be 3 records in the database for this table.",
                     3, recentDatabaseHelper.getCount(context));
        
        recentDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests removing expired records from the database.
     */
    @Test
    public void testPurgeExpiredRecords() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recentDatabaseHelper.clearDatabase(context);
        
        RecentTable.RECORD_TTL = 1;
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();
        
        recentDatabaseHelper.addRecord(context, "ContentId1", 10, false, currentTime, 20);
        
        // Sleep for a second so the record can expire.
        sleep(1000);
        
        assertTrue("At least 1 record should have been purged from database.",
                   recentDatabaseHelper.purgeExpiredRecords(context));
        
        assertEquals("There should be no records left in the db",
                     0, recentDatabaseHelper.getCount(context));
        
        recentDatabaseHelper.clearDatabase(context);
        
        RecentTable.RECORD_TTL = 1037000;
        recentDatabaseHelper.addRecord(context, "ContentId2", 10, false, currentTime, 20);
        recentDatabaseHelper.addRecord(context, "ContentId3", 10, false, currentTime, 20);
        recentDatabaseHelper.addRecord(context, "ContentId4", 10, false, currentTime, 20);
        
        assertFalse("No records should have expired.",
                    recentDatabaseHelper.purgeExpiredRecords(context));
        
        recentDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests getting records that are for videos that have not been fully played.
     */
    @Test
    public void testGetUnfinishedRecords() throws Exception {
        
        RecentDatabaseHelper recentDatabaseHelper = RecentDatabaseHelper.getInstance();
        assertNotNull(recentDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recentDatabaseHelper.clearDatabase(context);
        
        // Test no unfinished records.
        assertEquals(0, recentDatabaseHelper.getUnfinishedRecords(context, 10000).size());
        
        // Test 3 unfinished records, 1 barely watched.
        recentDatabaseHelper.addRecord(context, "ContentId1", 500, false,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        recentDatabaseHelper.addRecord(context, "ContentId2", 15000, false,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        recentDatabaseHelper.addRecord(context, "ContentId3", 15000, false,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        
        assertEquals(2, recentDatabaseHelper.getUnfinishedRecords(context, 10000).size());
        
        // Change 1 to finished.
        recentDatabaseHelper.addRecord(context, "ContentId1", 20000, true,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        
        assertEquals(2, recentDatabaseHelper.getUnfinishedRecords(context, 10000).size());
        
        // Change 2 & 3 to finished.
        recentDatabaseHelper.addRecord(context, "ContentId2", 20000, true,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        recentDatabaseHelper.addRecord(context, "ContentId3", 20000, true,
                                       DateAndTimeHelper.getCurrentDate().getTime(), 20000);
        
        assertEquals(0, recentDatabaseHelper.getUnfinishedRecords(context, 10000).size());
        
    }
}
