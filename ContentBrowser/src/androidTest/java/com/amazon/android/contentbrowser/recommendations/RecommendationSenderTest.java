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
package com.amazon.android.contentbrowser.recommendations;


import com.amazon.android.contentbrowser.database.ContentDatabase;
import com.amazon.android.contentbrowser.database.helpers.RecommendationDatabaseHelper;
import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.utils.Helpers;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import android.app.Notification;
import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@RunWith(AndroidJUnit4.class)
/**
 * Test class for {@link RecommendationSender}.
 */
public class RecommendationSenderTest {

    private final String LOGCAT_COMMAND = "logcat -d RecommendationSender:D *:S";
    private final String RECOMMENDATION_ERROR_MESSAGE = "Unable to build recommendation";
    private final int CLEAR_LOG_DELAY = 2000;
    private RecommendationSender mSender;
    private ContentContainer mRoot;

    /**
     * Create test content, recommendation sender, and reset database singleton.
     */
    @Before
    public void setUp() throws Exception {

        Helpers.clearLogs(CLEAR_LOG_DELAY);
        mRoot = new ContentContainer("Root");
        ContentContainer container = new ContentContainer("Hardcoded Category Name");

        container.addContent(createNewContent("1"));
        container.addContent(createNewContent("2"));
        container.addContent(createNewContent("3"));
        container.addContent(createNewContent("4"));
        container.addContent(createNewContent("5"));

        mRoot.addContentContainer(container);

        mSender = new RecommendationSender(InstrumentationRegistry.getContext(), mRoot, false);

        Field instance = ContentDatabase.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }

    /**
     * Test setting the root content container for a {@link RecommendationSender}.
     */
    @Test
    public void testSetRootContentContainer() throws Exception {

        ContentContainer newRoot = new ContentContainer("newRoot");
        mSender.setRootContentContainer(newRoot);
        assertEquals("The root container should now be newRoot", "newRoot",
                     mSender.getRootContentContainer().getName());
    }

    /**
     * Test getting a content from the sender's root.
     */
    @Test
    public void testGetContentFromRoot() throws Exception {

        Content content = mSender.getContentFromRoot("1");
        assertNotNull("Content should not be null", content);
        assertEquals("Content id should be 1", "1", content.getId());
    }

    /**
     * Test the logic for sending recommendations.
     */
    @Test
    public void testSendNewRecommendations() throws Exception {

        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
        String type = RecommendationRecord.GLOBAL;

        List<String> contendIds = new ArrayList<>(Arrays.asList("1", "2", "3"));
        List<Integer> recommendationIds = new ArrayList<>(Arrays.asList(1, 2, 3));

        // Happy case. Send three recommendations.
        assertTrue("Recommendations should have been sent successfully.",
                   mSender.sendNewRecommendations(type, recommendationIds, contendIds));
        assertEquals("3 global recommendations should be found in the database.", 3,
                     databaseHelper.getRecsWithType(context, RecommendationRecord.GLOBAL).size());

        // Test sending empty list of content ids.
        assertTrue("Recommendations should have been sent successfully.",
                   mSender.sendNewRecommendations(type, recommendationIds, new ArrayList<>()));
        assertEquals("No new recommendations should have been saved to the database.", 3,
                     databaseHelper.getRecsWithType(context, RecommendationRecord.GLOBAL).size());

        // Test that not enough rec ids is handled by sending 2 rec ids for 3 content ids.
        recommendationIds = new ArrayList<>(Arrays.asList(1, 2, 3));
        recommendationIds.remove(0);
        assertFalse("Recommendations should not have been sent successfully",
                    mSender.sendNewRecommendations(type, recommendationIds, contendIds));

        // Test null input is handled.
        assertFalse("Recommendations should not have been sent successfully",
                    mSender.sendNewRecommendations(type, null, null));

        databaseHelper.getDatabase(context).close();

    }

    /**
     * Test deleting recommendations.
     */
    @Test
    public void testDeleteRecommendations() throws Exception {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
    
        assertNotNull(databaseHelper);
        databaseHelper.clearDatabase(context);

        RecommendationRecord r1 = new RecommendationRecord("1", 1, RecommendationRecord.GLOBAL);
        RecommendationRecord r2 = new RecommendationRecord("2", 2, RecommendationRecord.GLOBAL);
        RecommendationRecord r3 = new RecommendationRecord("3", 3, RecommendationRecord.GLOBAL);

        databaseHelper.addRecord(context, r1);
        databaseHelper.addRecord(context, r2);
        databaseHelper.addRecord(context, r3);

        List<RecommendationRecord> list = new ArrayList<>(Arrays.asList(r1, r2, r3));

        mSender.deleteRecommendations(list);

        assertEquals("Recommendations should have been deleted", 0,
                     databaseHelper.getRecsWithType(context, RecommendationRecord.GLOBAL).size());

        databaseHelper.getDatabase(context).close();
    }

    /**
     * Test updating recommendations.
     */
    @Test
    public void testUpdateRecommendations() throws Exception {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
        String type = RecommendationRecord.GLOBAL;

        RecommendationRecord r1 = new RecommendationRecord("1", 1, RecommendationRecord.RELATED);

        assertNotNull(databaseHelper);
        databaseHelper.addRecord(context, r1);

        List<RecommendationRecord> list = new ArrayList<>();
        list.add(r1);

        List<Integer> idsForNewRecs = new ArrayList<>(Arrays.asList(1, 2, 3));

        List<String> contentIdsForNewRecs = new ArrayList<>(Arrays.asList("1", "2", "3"));

        assertTrue("Recommendations should have been updated successfully",
                   mSender.updateExistingRecommendations(type, list, contentIdsForNewRecs,
                                                         idsForNewRecs));

        assertFalse("Id should have been removed.", idsForNewRecs.contains(1));
        assertFalse("Id should have been removed.", contentIdsForNewRecs.contains("1"));

        RecommendationRecord updatedR1 = databaseHelper.getRecord(context, "1");
        assertTrue("Record should have been updated", updatedR1.getType().equals
                (RecommendationRecord.GLOBAL));

        assertFalse("Recommendations should not be updated with null input parameters",
                    mSender.updateExistingRecommendations(type, null, null, null));
        databaseHelper.getDatabase(context).close();
    }

    /**
     * Tests getting the list of records to delete.
     */
    @Test
    public void testGetRecordsToDelete() throws Exception {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
        String type = RecommendationRecord.GLOBAL;

        // Case 1:
        // Database empty; new 5; update 0; max 5
        assertNotNull(databaseHelper);
        databaseHelper.clearDatabase(context);
        List<RecommendationRecord> toDelete = mSender.getRecordsToDelete(type, 5, new ArrayList<>(),
                                                                         5);
        assertTrue("No recommendations should be deleted", toDelete.isEmpty());

        // Case 2:
        // Database 3; new 4; update 0; max 5; expecting to deleteByContentId 2
        RecommendationRecord r1 = new RecommendationRecord("1", 1, RecommendationRecord.GLOBAL);
        RecommendationRecord r2 = new RecommendationRecord("2", 2, RecommendationRecord.GLOBAL);
        RecommendationRecord r3 = new RecommendationRecord("3", 3, RecommendationRecord.GLOBAL);

        databaseHelper.addRecord(context, r1);
        databaseHelper.addRecord(context, r2);
        databaseHelper.addRecord(context, r3);

        toDelete = mSender.getRecordsToDelete(type, 4, new ArrayList<>(), 5);
        assertEquals("3 recommendations should be deleted", 2, toDelete.size());
        assertTrue("Rec with id 1 should be deleted", toDelete.contains(r1));
        assertTrue("Rec with id 2 should be deleted", toDelete.contains(r2));

        // Case 3:
        // Database 3, new 5, update 3; max 5; expected to deleteByContentId 0
        List<RecommendationRecord> toUpdate = new ArrayList<>();
        toUpdate.add(r1);
        toUpdate.add(r2);
        toUpdate.add(r3);

        toDelete = mSender.getRecordsToDelete(type, 5, toUpdate, 5);
        assertTrue("No recommendations should be deleted", toDelete.isEmpty());

        // Case 4
        // Database 3, new 10, update 0; max 5
        toDelete = mSender.getRecordsToDelete(type, 10, new ArrayList<>(), 5);
        assertEquals("3 recommendations should be deleted", 3, toDelete.size());

        // Case 5
        // Database 3, new 1, update 0; max 5
        toDelete = mSender.getRecordsToDelete(type, 1, new ArrayList<>(), 5);
        assertTrue("No recommendations should be deleted", toDelete.isEmpty());

        databaseHelper.getDatabase(context).close();
    }

    /**
     * Test sending global recommendations.
     */
    @Test
    public void testBuildGlobalRecommendations() throws Exception {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
        String type = RecommendationRecord.GLOBAL;

        // Build one recommendation. Database starts off empty.
        assertNotNull(databaseHelper);
        databaseHelper.clearDatabase(context);
        List<String> ids = new ArrayList<>();
        ids.add("1");
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 5));
        assertEquals("Database should contain one recommendation", 1,
                     databaseHelper.getCount(context));
        assertTrue("Recommendation 1 should exist in database",
                   databaseHelper.recordExists(context, "1"));

        // Build three more recommendations. The max # of recommendations at one time is 5.
        ids = new ArrayList<>(Arrays.asList("1", "2", "3", "4"));
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 5));
        assertEquals("Database should contain 4 recommendations", 4,
                     databaseHelper.getCount(context));
        assertTrue("Recommendation 1 should exist in database",
                   databaseHelper.recordExists(context, "1"));
        assertTrue("Recommendation 2 should exist in database",
                   databaseHelper.recordExists(context, "2"));
        assertTrue("Recommendation 3 should exist in database",
                   databaseHelper.recordExists(context, "3"));
        assertTrue("Recommendation 4 should exist in database",
                   databaseHelper.recordExists(context, "4"));

        // In this case we want to update 2 recommendations (1 and 4) and build 4 new ones.
        // Since the max is 5 at one time, we're expected 8 to not be included. Expecting recs
        // 2 and 3 to be deleted from database, and 5, 6, 7 to be added.
        ids = new ArrayList<>(Arrays.asList("1", "5", "6", "4", "7", "8"));
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 5));
        assertEquals("Database should contain 5 recommendations", 5,
                     databaseHelper.getCount(context));
        assertTrue("Recommendation 1 should exist in database",
                   databaseHelper.recordExists(context, "1"));
        assertTrue("Recommendation 4 should exist in database",
                   databaseHelper.recordExists(context, "4"));
        assertTrue("Recommendation 5 should exist in database",
                   databaseHelper.recordExists(context, "5"));
        assertTrue("Recommendation 6 should exist in database",
                   databaseHelper.recordExists(context, "6"));
        assertTrue("Recommendation 7 should exist in database",
                   databaseHelper.recordExists(context, "7"));
        assertFalse("Recommendation 2 should not exist in database",
                    databaseHelper.recordExists(context, "2"));
        assertFalse("Recommendation 3 should not exist in database",
                    databaseHelper.recordExists(context, "3"));

        assertFalse("Bad parameters should have been caught",
                    mSender.sendRecommendationsForType(null, null, 0));

        databaseHelper.getDatabase(context).close();
    }

    /**
     * Test sending related recommendations.
     */
    @Test
    public void testBuildRelatedRecommendations() throws Exception {

        ContentContainer root = new ContentContainer("root");
        Content c1 = createNewContent("1");
        Content c2 = createNewContent("2");
        Content c3 = createNewContent("3");
        Content c4 = createNewContent("4");
        root.addContent(c1).addContent(c2).addContent(c3);
        mSender.setRootContentContainer(root);
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        Context context = InstrumentationRegistry.getContext();
        
        String type = RecommendationRecord.RELATED;
        assertNotNull(databaseHelper);
        databaseHelper.clearDatabase(context);

        // Send recommendations for content 1 --> Should recommend content 2.
        List<String> ids = c1.getExtraStringValueAsList(Content.RECOMMENDATIONS_FIELD_NAME);
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 3));
        assertTrue("Recommendation for content 2 should exist",
                   databaseHelper.recordExists(context, "2"));

        // Send related recommendations for content 1, 2, 3 --> Should recommend content 2, 3, 4
        ids.addAll(c2.getExtraStringValueAsList(Content.RECOMMENDATIONS_FIELD_NAME));
        ids.addAll(c3.getExtraStringValueAsList(Content.RECOMMENDATIONS_FIELD_NAME));
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 3));
        assertTrue("Recommendation for content 3 should exist",
                   databaseHelper.recordExists(context, "3"));
        assertTrue("Recommendation for content 4 should exist",
                   databaseHelper.recordExists(context, "4"));

        // Send related recommendations for content 1, 2, 3, 4 --> Should recommend content 2, 3, 4,
        // since max is only 3.
        ids.addAll(c1.getExtraStringValueAsList(Content.RECOMMENDATIONS_FIELD_NAME));
        ids.addAll(c4.getExtraStringValueAsList(Content.RECOMMENDATIONS_FIELD_NAME));
        assertTrue("Recommendations should build with no problems",
                   mSender.sendRecommendationsForType(type, ids, 3));
        assertTrue("Recommendation for content 2 should exist",
                   databaseHelper.recordExists(context,"2"));
        assertTrue("Recommendation for content 3 should exist",
                   databaseHelper.recordExists(context, "3"));
        assertTrue("Recommendation for content 4 should exist",
                   databaseHelper.recordExists(context, "4"));
        assertFalse("Recommendation for content 5 should not exist",
                    databaseHelper.recordExists(context, "5"));

    }

    /**
     * Test building a recommendation.
     */
    @Test
    public void testBuildRecommendation() throws Exception {

        // A content with a bad genre set.
        Content content = createNewContent("10");
        content.setExtraValue(Content.GENRES_TAG, "bad genre input");
        mRoot.addContent(content);

        // A content with incorrect content types set.
        Content content2 = createNewContent("11");
        content2.setExtraValue(Content.CONTENT_TYPE_TAG, "not; a good type, 3");
        mRoot.addContent(content2);

        // A content with incorrect actions set.
        Content content3 = createNewContent("12");
        content3.setExtraValue(Content.RECOMMENDATION_ACTIONS_TAG, "noninteger");
        mRoot.addContent(content3);

        // A content with incorrect fire tv categories set.
        Content content4 = createNewContent("13");
        content4.setExtraValue(Content.FIRE_TV_CATEGORIES_TAG, 102);
        mRoot.addContent(content4);

        RecommendationSender sender =
                new RecommendationSender(InstrumentationRegistry.getTargetContext(), mRoot, false);

        Notification notification = sender.buildRecommendation("1", 1, RecommendationRecord.GLOBAL);

        assertEquals("Notification should have Global group type.",
                     RecommendationRecord.GLOBAL, notification.getGroup());

        Bundle extras = notification.extras;
        assertEquals("The content id should be set in the extras map.",
                     "1", extras.getString(RecommendationExtras.EXTRA_AMAZON_CONTENT_ID));

        testBadInputForRecommendation(sender, "10", 10);
        testBadInputForRecommendation(sender, "11", 11);
        testBadInputForRecommendation(sender, "12", 12);
        testBadInputForRecommendation(sender, "13", 13);
    }

    /**
     * Helper method to test bad input with building a recommendation. It checks the logs for the
     * log that states the recommendation was unable to be built.
     *
     * @param sender The recommendation sender instance.
     * @param id     The id of the content
     * @param recId  The id of the recommendation.
     */
    private void testBadInputForRecommendation(RecommendationSender sender, String id, int recId)
            throws Exception {

        Helpers.clearLogs(CLEAR_LOG_DELAY);

        Notification notification = sender.buildRecommendation(id, recId,
                                                               RecommendationRecord.GLOBAL);

        assertNull("Notification should be null", notification);
        assertTrue("Should have found \"" + RECOMMENDATION_ERROR_MESSAGE + "\" in the logs for" +
                           "recommendation with id " + recId,
                   Helpers.checkConsole(LOGCAT_COMMAND, RECOMMENDATION_ERROR_MESSAGE));
    }

    /**
     * Helper method to create a new test content.
     *
     * @param id The content id. Numeric only: 1, 2, 3, ...
     * @return The test content.
     */
    private Content createNewContent(String id) {

        Content content = new Content(id);
        content.setId(id);
        // Need to set a real image url. Using one from our GitHub :)
        content.setCardImageUrl("https://raw.githubusercontent.com/amzn/fire-app-builder/master/" +
                                        "TVUIComponent/lib/src/main/res/drawable/movie.png");
        // Add the next content as a recommendation (based on int ids)
        int rec = Integer.valueOf(id) + 1;
        content.setExtraValue(Content.RECOMMENDATIONS_FIELD_NAME, "[" + rec + "]");
        return content;
    }
}
