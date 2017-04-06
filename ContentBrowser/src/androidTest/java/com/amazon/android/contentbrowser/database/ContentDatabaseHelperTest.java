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

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;


import static com.amazon.android.utils.Helpers.sleep;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.File;
import java.lang.reflect.Field;
import java.util.List;

/**
 * Test class for {@link ContentDatabaseHelper}.
 */
@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
public class ContentDatabaseHelperTest {

    /**
     * Clear out the singleton instance of the database helper so each test has a clean slate.
     */
    @Before
    public void resetDatabase() throws NoSuchFieldException, IllegalAccessException {

        Field instance = ContentDatabaseHelper.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    /**
     * Tests creating the database.
     */
    @Test
    public void testCreateDatabase() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());

        assertNotNull("ContentDatabaseHelper should have been constructed", contentDatabase);
    }

    /**
     * Tests deleting the database.
     */
    @Test
    public void testDeleteDatabase() throws Exception {

        Context context = InstrumentationRegistry.getContext();

        ContentDatabaseHelper contentDatabase = ContentDatabaseHelper.getInstance(context);
        assertNotNull(contentDatabase);

        String databaseFilePath = contentDatabase.getDatabasePath(context);
        assertTrue(contentDatabase.deleteDatabase(context));

        File databaseFile = new File(databaseFilePath);
        assertFalse(databaseFilePath + " should not exist.", databaseFile.exists());

        contentDatabase.close();
    }

    /**
     * Tests putting a recent record in the database.
     */
    @Test
    public void testRecentPut() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();

        assertTrue("Record should have been successfully recorded in database.",
                   contentDatabase.addRecent("ContentId1", 10, false, currentTime));

        assertFalse("Record should not have been recorded in database since id is an empty string",
                    contentDatabase.addRecent("", 0, true, currentTime));

        assertFalse("Record should not have been recorded in database since id is null",
                    contentDatabase.addRecent(null, 0, false, currentTime));

        assertTrue("ContentId1 should have been found in database.",
                   contentDatabase.recentRecordExists("ContentId1"));

        assertTrue("Record should have been successfully recorded in database.",
                   contentDatabase.addRecent("ContentId2", 1000, true, currentTime));

        assertTrue("ContentId2 should have been found in database.",
                   contentDatabase.recentRecordExists("ContentId2"));

        contentDatabase.close();

    }

    /**
     * Tests deleting recent records from the database.
     */
    @Test
    public void testRecentDelete() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecent("ContentId1", 10, false,
                                  DateAndTimeHelper.getCurrentDate().getTime());

        assertTrue("ContentId1 should have been deleted from database.",
                   contentDatabase.deleteRecent("ContentId1"));
        assertFalse("ContentId1 should not have been found in database.",
                    contentDatabase.recentRecordExists("ContentId1"));
        assertFalse("ContentId1 should not have been deleted from database since it doesn't exist.",
                    contentDatabase.deleteRecent("ContentId1"));
        assertFalse("ContentId1 should have been deleted from database.",
                    contentDatabase.deleteRecent(""));
        assertFalse("ContentId1 should have been deleted from database.",
                    contentDatabase.deleteRecent(null));

        contentDatabase.close();

    }

    /**
     * Tests reading recent records from the database.
     */
    @Test
    public void testRecentRead() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();

        RecentRecord expectedContentId1 = new RecentRecord("ContentId1", 10, false, currentTime);
        RecentRecord expectedContentId2 = new RecentRecord("ContentId2", 123456789, true,
                                                           currentTime);
        contentDatabase.addRecent("ContentId1", 10, false, currentTime);
        contentDatabase.addRecent("ContentId2", 123456789, true, currentTime);

        RecentRecord actualContentId1 = contentDatabase.getRecent("ContentId1");
        assertTrue("The retrieved content should have matched.",
                   actualContentId1.equals(expectedContentId1));

        RecentRecord actualContentId2 = contentDatabase.getRecent("ContentId2");
        assertTrue("The retrieved content should have matched.",
                   actualContentId2.equals(expectedContentId2));

        contentDatabase.close();

    }

    /**
     * Tests putting recommendation records in the database.
     */
    @Test
    public void testRecommendationPut() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);


        assertTrue("Record should have been successfully recorded in database.",
                   contentDatabase.addRecommendation("ContentId1", 1, "type"));

        assertFalse("Record should not have been recorded in database since id is an empty string",
                    contentDatabase.addRecommendation("", 10, "type"));

        assertFalse("Record should not have been recorded in database since id is null",
                    contentDatabase.addRecommendation(null, 10, "type"));

        assertFalse("Record should not have been recorded in database since recommendation id is 0",
                    contentDatabase.addRecommendation("ContentId1", 0, "type"));

        assertFalse("Record should not have been recorded in database since recommendation id is 0",
                    contentDatabase.addRecommendation("ContentId1", 100, null));

        assertTrue("ContentId1 should have been found in database.",
                   contentDatabase.recommendationWithContentIdExists("ContentId1"));

        assertTrue("Record should have been successfully recorded in database.",
                   contentDatabase.addRecommendation("ContentId2", 1000, "type"));

        assertTrue("ContentId2 should have been found in database.",
                   contentDatabase.recommendationWithContentIdExists("ContentId2"));

        contentDatabase.close();
    }

    /**
     * Tests deleting recommendation records from the database.
     */
    @Test
    public void testRecommendationDelete() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecommendation("ContentId1", 1, "type");
        contentDatabase.addRecommendation("ContentId2", 2, "type2");

        assertTrue("ContentId1 record should have been successfully deleted.",
                   contentDatabase.deleteRecommendationByContentId("ContentId1"));
        assertFalse("ContentId1 record should not have been found in database.",
                    contentDatabase.recommendationWithContentIdExists("ContentId1"));
        assertFalse("Record should not have been deleted since id is empty.",
                    contentDatabase.deleteRecommendationByContentId(""));
        assertFalse("Record should have have been deleted since id is null",
                    contentDatabase.deleteRecommendationByContentId(null));
        assertFalse("Record should not have been deleted since id is 0.",
                    contentDatabase.deleteRecommendationByRecId(0));
        assertFalse("Record should not have been deleted since id is negative.",
                    contentDatabase.deleteRecommendationByRecId(-1));
        assertTrue("ContentId2 record should have been successfully deleted.",
                   contentDatabase.deleteRecommendationByRecId(2));

        contentDatabase.close();

    }

    /**
     * Tests reading recommendation records from the database.
     */
    @Test
    public void testRecommendationRead() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        RecommendationRecord expectedContentId1 = new RecommendationRecord("ContentId1", 10,
                                                                           "type");
        RecommendationRecord expectedContentId2 = new RecommendationRecord("ContentId2",
                                                                           123456789, "type");
        assertTrue("Recommendation should have been added.",
                   contentDatabase.addRecommendation("ContentId1", 10, "type"));
        assertTrue("Recommendation should have been added.",
                   contentDatabase.addRecommendation("ContentId2", 123456789, "type"));

        RecommendationRecord actualContentId1 = contentDatabase.getRecommendationByContentId("ContentId1");
        assertTrue("The retrieved content should have matched.",
                   actualContentId1.equals(expectedContentId1));

        RecommendationRecord actualContentId2 = contentDatabase.getRecommendationByRecId(123456789);
        assertTrue("The retrieved content should have matched.",
                   actualContentId2.equals(expectedContentId2));

        contentDatabase.close();

    }

    /**
     * Tests reading all of one type of recommendation from the database.
     */
    @Test
    public void testRecommendationGetAllOfType() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecommendation("ContentId1", 1, "new");
        contentDatabase.addRecommendation("ContentId2", 2, "new");
        contentDatabase.addRecommendation("ContentId3", 3, "new");
        contentDatabase.addRecommendation("ContentId4", 4, "new");
        contentDatabase.addRecommendation("ContentId5", 5, "other");

        List<RecommendationRecord> records = contentDatabase.getRecommendationsWithType("new");
        assertTrue("4 records should have been found but found " + records.size(),
                   records.size() == 4);
        assertTrue("Record of ContentId1 should be first in the list",
                   records.get(0).getContentId().equals("ContentId1"));
        assertTrue("Record of ContentId1 should be first in the list",
                   records.get(3).getContentId().equals("ContentId4"));
        assertTrue("No records should be found with that type.",
                   contentDatabase.getRecommendationsWithType("fake").isEmpty());
        assertTrue("1 record with that type should be found.",
                   contentDatabase.getRecommendationsWithType("other").size() == 1);
        contentDatabase.close();
    }

    /**
     * Tests deleting all of one type of recommendation from the database.
     */
    @Test
    public void testDeleteRecommendationsWithType() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecommendation("ContentId1", 1, "new");
        contentDatabase.addRecommendation("ContentId2", 2, "new");
        contentDatabase.addRecommendation("ContentId3", 3, "new");
        contentDatabase.addRecommendation("ContentId4", 4, "new");
        contentDatabase.addRecommendation("ContentId5", 5, "other");

        assertTrue("Should have deleted recommendations",
                   contentDatabase.deleteAllRecommendationsWithType("new"));
        assertTrue("Should not have found any recommendations with type 'new'",
                   contentDatabase.getRecommendationsWithType("new").isEmpty());

        contentDatabase.close();
    }

    /**
     * Tests clearing the database of all records.
     */
    @Test
    public void testClearDatabase() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecommendation("ContentId1", 1, "new");

        contentDatabase.addRecent("ContentId1", 10, false,
                                  DateAndTimeHelper.getCurrentDate().getTime());

        contentDatabase.clearDatabase();

        assertFalse("Record should not exist after clearing database",
                    contentDatabase.recentRecordExists("ContentId1"));
        assertFalse("Record should not exist after clearing database",
                    contentDatabase.recommendationWithContentIdExists("ContentId1"));

        contentDatabase.close();
    }

    /**
     * Tests getting the count of recommendations records in the database.
     */
    @Test
    public void testGetRecommendationsCount() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.addRecommendation("ContentId1", 1, "new");
        contentDatabase.addRecommendation("ContentId2", 2, "new");
        contentDatabase.addRecommendation("ContentId3", 3, "new");
        contentDatabase.addRecommendation("ContentId4", 4, "new");
        contentDatabase.addRecommendation("ContentId5", 5, "other");

        assertEquals("There should be 5 records in the database for this table.",
                     5, contentDatabase.getRecommendationsCount());

        contentDatabase.close();
    }

    /**
     * Tests getting the count of recent records in the database.
     */
    @Test
    public void testGetRecentCount() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();

        contentDatabase.addRecent("ContentId1", 10, false, currentTime);
        contentDatabase.addRecent("ContentId2", 10, false, currentTime);
        contentDatabase.addRecent("ContentId3", 10, false, currentTime);

        assertEquals("There should be 3 records in the database for this table.",
                     3, contentDatabase.getRecentCount());

        contentDatabase.close();
    }

    /**
     * Tests removing expired records from the database.
     */
    @Test
    public void testRemoveExpiredRecords() throws Exception {

        ContentDatabaseHelper contentDatabase =
                ContentDatabaseHelper.getInstance(InstrumentationRegistry.getContext());
        assertNotNull(contentDatabase);

        contentDatabase.clearDatabase();

        RecommendationTable.RECORD_TTL = 1;
        RecentTable.RECORD_TTL = 1;
        long currentTime = DateAndTimeHelper.getCurrentDate().getTime();

        contentDatabase.addRecommendation("ContentId1", 1, "new");
        contentDatabase.addRecent("ContentId1", 10, false, currentTime);

        RecommendationTable.RECORD_TTL = 100;
        contentDatabase.addRecommendation("ContentId2", 2, "new");

        // Sleep for a second so the records can expire.
        sleep(1000);

        List<RecommendationRecord> records = contentDatabase.getExpiredRecommendations();
        assertTrue("List should contain 1 record", records.size() == 1);
        assertTrue("Records should have been purged.", contentDatabase.removeExpiredRecords());

        assertEquals("There should be 1 recommendation left in the db.",
                     1, contentDatabase.getRecommendationsCount());
        assertEquals("There should be no recent records left in the db",
                     0, contentDatabase.getRecentCount());

        contentDatabase.clearDatabase();

        RecentTable.RECORD_TTL = 1037000;
        contentDatabase.addRecent("ContentId2", 10, false, currentTime);
        contentDatabase.addRecent("ContentId3", 10, false, currentTime);
        contentDatabase.addRecent("ContentId4", 10, false, currentTime);

        assertFalse("No records should have expired.", contentDatabase.removeExpiredRecords());

        contentDatabase.close();
    }

}
