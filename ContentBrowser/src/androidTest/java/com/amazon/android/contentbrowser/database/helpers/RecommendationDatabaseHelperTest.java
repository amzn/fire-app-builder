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
import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.contentbrowser.database.tables.RecommendationTable;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.lang.reflect.Field;
import java.util.List;

import static com.amazon.android.utils.Helpers.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
/**
 * Test class for {@link RecommendationDatabaseHelper}.
 */
public class RecommendationDatabaseHelperTest {
    
    /**
     * Clear out the singleton instance of the database helper so each test has a clean slate.
     */
    @Before
    public void resetDatabase() throws NoSuchFieldException, IllegalAccessException {
        
        Field instance = ContentDatabase.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
        
        instance = RecommendationDatabaseHelper.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Tests getting an instance of the recommendation database helper and the SQLite Database.
     */
    @Test
    public void testGetInstance() throws Exception {
        
        RecommendationDatabaseHelper instance = RecommendationDatabaseHelper.getInstance();
        assertNotNull(instance);
        
        SQLiteDatabase contentDatabase = instance.getDatabase(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);
    }
    
    /**
     * Tests putting recommendation records in the database.
     */
    @Test
    public void testAddRecord() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        assertTrue("Record should have been successfully recorded in database.",
                   recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "type"));
        
        assertFalse("Record should not have been recorded in database since id is an empty string",
                    recommendationDatabaseHelper.addRecord(context, "", 10, "type"));
        
        assertFalse("Record should not have been recorded in database since id is null",
                    recommendationDatabaseHelper.addRecord(context, null, 10, "type"));
        
        assertFalse("Record should not have been recorded in database since recommendation id is 0",
                    recommendationDatabaseHelper.addRecord(context, "ContentId1", 0, "type"));
        
        assertFalse("Record should not have been recorded in database since recommendation id is 0",
                    recommendationDatabaseHelper.addRecord(context, "ContentId1", 100, null));
        
        assertTrue("ContentId1 should have been found in database.",
                   recommendationDatabaseHelper.recordExists(context, "ContentId1"));
        
        assertTrue("Record should have been successfully recorded in database.",
                   recommendationDatabaseHelper.addRecord(context, "ContentId2", 1000, "type"));
        
        assertTrue("ContentId2 should have been found in database.",
                   recommendationDatabaseHelper.recordExists(context, "ContentId2"));
        
        recommendationDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests deleting recommendation records from the database.
     */
    @Test
    public void testDeleteRecord() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "type");
        recommendationDatabaseHelper.addRecord(context, "ContentId2", 2, "type2");
        
        assertTrue("ContentId1 record should have been successfully deleted.",
                   recommendationDatabaseHelper.deleteRecord(context, "ContentId1"));
        assertFalse("ContentId1 record should not have been found in database.",
                    recommendationDatabaseHelper.recordExists(context, "ContentId1"));
        assertFalse("Record should not have been deleted since id is empty.",
                    recommendationDatabaseHelper.deleteRecord(context, ""));
        assertFalse("Record should have have been deleted since id is null",
                    recommendationDatabaseHelper.deleteRecord(context, null));
        assertFalse("Record should not have been deleted since id is 0.",
                    recommendationDatabaseHelper.deleteByRecId(context, 0));
        assertFalse("Record should not have been deleted since id is negative.",
                    recommendationDatabaseHelper.deleteByRecId(context, -1));
        assertTrue("ContentId2 record should have been successfully deleted.",
                   recommendationDatabaseHelper.deleteByRecId(context, 2));
        
        recommendationDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests reading recommendation records from the database.
     */
    @Test
    public void testGetRecord() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        RecommendationRecord expectedContentId1 = new RecommendationRecord("ContentId1", 10,
                                                                           "type");
        RecommendationRecord expectedContentId2 = new RecommendationRecord("ContentId2",
                                                                           123456789, "type");
        assertTrue("Recommendation should have been added.",
                   recommendationDatabaseHelper.addRecord(context, "ContentId1", 10, "type"));
        assertTrue("Recommendation should have been added.",
                   recommendationDatabaseHelper.addRecord(context, "ContentId2", 123456789,
                                                          "type"));
        
        RecommendationRecord actualContentId1 =
                recommendationDatabaseHelper.getRecord(context, "ContentId1");
        
        assertTrue("The retrieved content should have matched.",
                   actualContentId1.equals(expectedContentId1));
        
        RecommendationRecord actualContentId2 = recommendationDatabaseHelper.getRecByRecId(context,
                                                                                           123456789);
        assertTrue("The retrieved content should have matched.",
                   actualContentId2.equals(expectedContentId2));
        
        recommendationDatabaseHelper.getDatabase(context).close();
        
    }
    
    
    /**
     * Tests reading all of one type of recommendation from the database.
     */
    @Test
    public void testGetRecsWithType() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId2", 2, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId3", 3, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId4", 4, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId5", 5, "other");
        
        List<RecommendationRecord> records = recommendationDatabaseHelper.getRecsWithType(context,
                                                                                          "new");
        assertTrue("4 records should have been found but found " + records.size(),
                   records.size() == 4);
        assertTrue("Record of ContentId1 should be first in the list",
                   records.get(0).getContentId().equals("ContentId1"));
        assertTrue("Record of ContentId1 should be first in the list",
                   records.get(3).getContentId().equals("ContentId4"));
        assertTrue("No records should be found with that type.",
                   recommendationDatabaseHelper.getRecsWithType(context, "fake").isEmpty());
        assertTrue("1 record with that type should be found.",
                   recommendationDatabaseHelper.getRecsWithType(context, "other").size() == 1);
        
        recommendationDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests deleting all of one type of recommendation from the database.
     */
    @Test
    public void testDeleteRecommendationsWithType() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId2", 2, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId3", 3, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId4", 4, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId5", 5, "other");
        
        assertTrue("Should have deleted recommendations",
                   recommendationDatabaseHelper.deleteAllRecsWithType(context, "new"));
        assertTrue("Should not have found any recommendations with type 'new'",
                   recommendationDatabaseHelper.getRecsWithType(context, "new").isEmpty());
        
        recommendationDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests clearing the database of all records.
     */
    @Test
    public void testClearDatabase() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "new");
        
        recommendationDatabaseHelper.clearDatabase(context);
        
        assertFalse("Record should not exist after clearing database",
                    recommendationDatabaseHelper.recordExists(context, "ContentId1"));
        
        recommendationDatabaseHelper.getDatabase(context).close();
        
    }
    
    /**
     * Tests getting the count of recommendation records in the database.
     */
    @Test
    public void testGetCount() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId2", 2, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId3", 3, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId4", 4, "new");
        recommendationDatabaseHelper.addRecord(context, "ContentId5", 5, "other");
        
        assertEquals("There should be 5 records in the database for this table.",
                     5, recommendationDatabaseHelper.getCount(context));
        
        recommendationDatabaseHelper.getDatabase(context).close();
    }
    
    /**
     * Tests removing expired records from the database.
     */
    @Test
    public void testRemoveExpiredRecords() throws Exception {
        
        RecommendationDatabaseHelper recommendationDatabaseHelper = RecommendationDatabaseHelper
                .getInstance();
        assertNotNull(recommendationDatabaseHelper);
        Context context = InstrumentationRegistry.getContext();
        
        recommendationDatabaseHelper.clearDatabase(context);
        
        RecommendationTable.RECORD_TTL = 1;
        
        recommendationDatabaseHelper.addRecord(context, "ContentId1", 1, "new");
        
        RecommendationTable.RECORD_TTL = 100;
        recommendationDatabaseHelper.addRecord(context, "ContentId2", 2, "new");
        
        // Sleep for a second so the record with a TTL of 1 second can expire.
        sleep(1000);
        
        List<RecommendationRecord> records = recommendationDatabaseHelper
                .getExpiredRecommendations(context);
        assertEquals("List should contain 1 record", 1, records.size());
        assertTrue("Records should have been purged.", recommendationDatabaseHelper
                .purgeExpiredRecords(context));
        
        assertEquals("There should be 1 recommendation left in the db.",
                     1, recommendationDatabaseHelper.getCount(context));
        
        recommendationDatabaseHelper.clearDatabase(context);
        
        recommendationDatabaseHelper.getDatabase(context).close();
    }
}
