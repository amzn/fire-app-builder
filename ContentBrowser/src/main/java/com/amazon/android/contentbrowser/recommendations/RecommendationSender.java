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

import com.amazon.android.contentbrowser.R;
import com.amazon.android.contentbrowser.database.helpers.RecentDatabaseHelper;
import com.amazon.android.contentbrowser.database.helpers.RecommendationDatabaseHelper;
import com.amazon.android.contentbrowser.database.records.RecentRecord;
import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.contentbrowser.helper.LauncherIntegrationManager;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;

import android.app.Notification;
import android.app.NotificationManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static com.amazon.android.contentbrowser.helper.LauncherIntegrationManager.CONTENT_SOURCE;
import static com.amazon.android.contentbrowser.helper.LauncherIntegrationManager
        .RECOMMENDED_CONTENT;

/**
 * This class contains functionality to send, build, update, and dismiss recommendations. This is a
 * helper class for {@link RecommendationManager}. The method containing the main logic for sending
 * recommendations is {@link #sendRecommendationsForType(String, List, int)}.
 * This class relies heavily on {@link RecommendationDatabaseHelper} for database transactions.
 */
class RecommendationSender {

    private static final String TAG = RecommendationSender.class.getSimpleName();
    private ContentContainer mRootContentContainer = new ContentContainer("Root");
    private NotificationManager mNotificationManager;
    private Context mContext;
    private boolean mSendToNotificationManager;

    /**
     * Constructor.
     *
     * @param context                   The context.
     * @param rootContentContainer      Content container that contains all the content to be used
     *                                  in creating recommendations.
     * @param sendToNotificationManager If true, notification manager will be used. Turn to false
     *                                  for local testing purposes.
     */
    RecommendationSender(Context context, ContentContainer rootContentContainer, boolean
            sendToNotificationManager) {

        mRootContentContainer = rootContentContainer;
        mContext = context;
        mSendToNotificationManager = sendToNotificationManager;

        if (mSendToNotificationManager) {
            mNotificationManager =
                    (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        }
    }

    /**
     * Goes through the process for sending recommendations for the given type. Recommendations
     * will be dismissed, updated, and created as necessary.
     *
     * @param type       The type of recommendation. Types found in {@link RecommendationRecord}.
     * @param contentIds A list of content ids. Recommendations for these contents will be sent.
     * @param max        The maximum number of recommendations to send during this process.
     * @return False if any exceptions occurred throughout the process.
     */
    boolean sendRecommendationsForType(String type, List<String>
            contentIds, int max) {

        try {
            RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
            if (databaseHelper == null || type == null || type.isEmpty() ||
                    contentIds == null || contentIds.isEmpty() || max <= 0) {
                Log.e(TAG, "Bad parameters for building global recommendations");
                return false;
            }

            // Trim down any excess content ids since we can only send max recommendations.
            List<String> contentIdsOfNewRecs = contentIds;
            if (contentIds.size() > max) {
                contentIdsOfNewRecs = contentIds.subList(0, max);
            }

            // A list of ids to use for new recommendations.
            ArrayList<Integer> idsForNewRecs = buildRecommendationIdList(max);

            // Get the list of recommendations records that will be updated.
            List<RecommendationRecord> recsToUpdate =
                    databaseHelper.getExistingRecommendationsByContentIds(mContext, contentIdsOfNewRecs);

            // Get the list of recommendations that need to be deleted to send the new ones
            List<RecommendationRecord> recsToDelete =
                    getRecordsToDelete(type, contentIdsOfNewRecs.size(), recsToUpdate,
                                       max);

            updateExistingRecommendations(type, recsToUpdate, contentIdsOfNewRecs,
                                          idsForNewRecs);

            deleteRecommendations(recsToDelete);

            sendNewRecommendations(type, idsForNewRecs, contentIdsOfNewRecs);

        }
        catch (Exception e) {
            Log.e(TAG, "Unable to update recommendation", e);
            return false;
        }
        return true;
    }

    /**
     * Creates recommendations, stores them to the database, and sends them to notification
     * manager (if {@link #mSendToNotificationManager} is true. If the list of recommendation ids
     * is smaller than the list of content ids, no recommendations are sent. We need an id for each
     * new recommendation to send.
     *
     * If the app runs into an error during this process, it is possible that the app's
     * database becomes out of sync with the device's database regarding notifications. However,
     * this does not pose much of a threat to user experience.
     *
     * @param type                The type of recommendation.
     * @param idsForNewRecs       A list of available content ids.
     * @param contentIdsOfNewRecs Ids of the content to recommend.
     * @return True if recommendations were sent; false otherwise.
     */
    boolean sendNewRecommendations(String type, List<Integer>
            idsForNewRecs, List<String> contentIdsOfNewRecs) {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
    
        if (contentIdsOfNewRecs == null || idsForNewRecs == null || databaseHelper == null) {
            Log.e(TAG, "Parameters should not be null");
            return false;
        }
        if (contentIdsOfNewRecs.size() > idsForNewRecs.size()) {
            Log.e(TAG, "Not enough recommendation ids sent to send all new recommendations. Not " +
                    "sending any.");
            return false;
        }
        for (String contentId : contentIdsOfNewRecs) {

            Integer recommendationId = idsForNewRecs.remove(0);

            Notification notification = buildRecommendation(contentId,
                                                            recommendationId, type);
    
            databaseHelper.addRecord(mContext, contentId, recommendationId, type);

            if (mSendToNotificationManager) {
                sendToNotificationManager(mContext, recommendationId, notification);
            }
        }
        return true;
    }

    /**
     * Deletes the list of recommendation records from the database. Sends notification manager a
     * cancel request if {@link #mSendToNotificationManager} is true.
     *
     * If the app runs into an error during this process, it is possible that the app's
     * database becomes out of sync with the device's database regarding notifications. However,
     * this does not pose much of a threat to user experience.
     *
     * @param recsToDelete The recommendations to deleteByContentId.
     */
    void deleteRecommendations(List<RecommendationRecord> recsToDelete) {
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        if (databaseHelper == null) {
            Log.e(TAG, "Database can not be instantiated so cannot delete recommendations.");
            return;
        }
    
        for (RecommendationRecord record : recsToDelete) {
            databaseHelper.deleteByRecId(mContext, record.getRecommendationId());
            if (mSendToNotificationManager) {
                mNotificationManager.cancel(record.getRecommendationId());
            }
        }
    }

    /**
     * Updates the given recommendations records with a new type. Removes the updated
     * recommendation id from the list of possible ids for new recommendations. Builds the new
     * notification and sends it to notification manager if ({@link #mSendToNotificationManager}
     * is true.
     *
     * If the app runs into an error during this process, it is possible that the app's
     * database becomes out of sync with the device's database regarding notifications. However,
     * this does not pose much of a threat to user experience.
     *
     * @param type                The type of recommendation.
     * @param recsToUpdate        The recommendation records to update in the database.
     * @param contentIdsOfNewRecs A list of content ids that need to be recommended.
     * @param idsForNewRecs       A list of ids to use for new recommendations.
     * @return False if the parameters were bad; true otherwise.
     */
    boolean updateExistingRecommendations(String type,
                                          List<RecommendationRecord> recsToUpdate,
                                          List<String> contentIdsOfNewRecs,
                                          List<Integer> idsForNewRecs) {
    
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
    
        if (recsToUpdate == null || databaseHelper == null || idsForNewRecs == null ||
                contentIdsOfNewRecs == null) {
            Log.e(TAG, "Parameters should not be null when updating recommendations");
            return false;
        }

        for (RecommendationRecord record : recsToUpdate) {

            // Update the record data. We can use same rec id but should update the type.
            record.setType(type);
            databaseHelper.updateRecord(mContext, record);

            // Remove the rec id so its not used later.
            idsForNewRecs.remove(Integer.valueOf(record.getRecommendationId()));

            // Remove the content id so another recommendation isn't sent later.
            contentIdsOfNewRecs.remove(record.getContentId());

            // Build new notification
            Notification notification = buildRecommendation(record.getContentId(),
                                                            record.getRecommendationId(), type);
            // Cancel old notification and send the new
            if (mSendToNotificationManager) {
                mNotificationManager.cancel(record.getRecommendationId());
                if (notification != null) {
                    mNotificationManager.notify(record.getRecommendationId(), notification);
                }
            }
        }

        return true;
    }

    /**
     * Gets a list of recommendation records to delete, given the number of new recommendations to
     * be created, the recommendations records to be updated, and the max number of recommendations
     * to send.
     *
     * @param type       The type of recommendation.
     * @param numNewRecs The number of new recommendations to send.
     * @param updateRecs The recommendation records that will be updated.
     * @param max        The max number of recommendations to send.
     * @return A list of records to delete so the new recommendations can be sent.
     */
    List<RecommendationRecord> getRecordsToDelete(String type, int numNewRecs,
                                                  List<RecommendationRecord>
                                                          updateRecs, int max) {
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();

        List<RecommendationRecord> recsFromDb = databaseHelper.getRecsWithType(mContext, type);

        List<RecommendationRecord> records = new ArrayList<>();
        int numToUpdate = updateRecs.size();
        int numToDelete = recsFromDb.size() + numNewRecs - numToUpdate;
        int index = 0;
        while (numToDelete > max && index < recsFromDb.size()) {

            // Add record to delete list if its not in the update list.
            if (!updateRecs.contains(recsFromDb.get(index))) {
                records.add(recsFromDb.get(index));
                numToDelete--;
            }
            index++;

        }

        return records;
    }

    /**
     * Builds the recommendation.
     *
     * @param contentId        The content id.
     * @param recommendationId The recommendation id.
     * @param group            The recommendation type.
     * @return The recommendation.
     */
    Notification buildRecommendation(String contentId, int
            recommendationId, String group) {

        Content content = getContentFromRoot(contentId);
        if (content == null) {
            Log.e(TAG, "Could not build recommendation for content with id " + contentId + " " +
                    "because content not found");
            return null;
        }

        // Try getting the content's playback progress (if it exists)
        int playbackProgress = 0;
        long lastWatchedDateTime = 0;
        RecentDatabaseHelper database = RecentDatabaseHelper.getInstance();
        if (database != null) {
            if (database.recordExists(mContext, contentId)) { // Need to get recent db from content browser. Maybe shoudl do that for all instead of passing?
                RecentRecord record = database.getRecord(mContext, contentId);
                playbackProgress = (int) record.getPlaybackLocation();
                lastWatchedDateTime = record.getLastWatched();
            }
        }
        else {
            Log.e(TAG, "Could not get recent playback progress for content because database is " +
                    "null");
        }

        // Create the recommendation builder.
        RecommendationBuilder builder = new RecommendationBuilder().setContext(mContext);

        Log.d(TAG, "Built recommendation - " + content.getTitle());

        try {
            int live = content.getExtraValueAsBoolean(Content.LIVE_TAG) ? 1 : 0;
            List genres = content.getExtraValueAsList(Content.GENRES_TAG);
            List contentType = content.getExtraValueAsList(Content.CONTENT_TYPE_TAG);
            ArrayList<Integer> actions = (ArrayList<Integer>)
                    content.getExtraValueAsList(Content.RECOMMENDATION_ACTIONS_TAG);
            ArrayList<String> contentCategories = (ArrayList<String>)
                    content.getExtraValueAsList(Content.FIRE_TV_CATEGORIES_TAG);

            return builder.setBackgroundUrl(content.getBackgroundImageUrl())
                          .setRecommendationId(recommendationId)
                          .setTitle(content.getTitle())
                          .setText(content.getDescription())
                          .setLargeIconUrl(content.getCardImageUrl())
                          .setContentIntent(buildContentIntent(mContext, content.getId()))
                          .setDismissIntent(buildDismissIntent(mContext, recommendationId))
                          .setContentDuration(content.getDuration())
                          .setMaturityRating(String.valueOf(
                                  content.getExtraValue(Content.MATURITY_RATING_TAG)))
                          .setContentId(content.getId())
                          .setContentCategories(contentCategories)
                          .setDescription(content.getDescription())
                          .setRank(0) // highest priority
                          .setLiveContent(live)
                          .setContentStartTime(content.getExtraValueAsLong(Content.START_TIME_TAG))
                          .setContentEndTime(content.getExtraValueAsLong(Content.END_TIME_TAG))
                          .setContentReleaseDate(String.valueOf(content.getAvailableDate()))
                          .setContentClosedCaptions(content.hasCloseCaption() ? 1 : 0)
                          .setGroup(group)
                          .setContentCustomerRating(
                                  content.getExtraValueAsInt(Content.CUSTOMER_RATING_TAG))
                          .setContentCustomerRatingCount(
                                  content.getExtraValueAsInt(Content.CUSTOMER_RATING_COUNT_TAG))
                          .setPreviewVideoUrl(String.valueOf(
                                  content.getExtraValue(Content.VIDEO_PREVIEW_URL_TAG)))
                          .setImdbId(String.valueOf(content.getExtraValue(Content.IMDB_ID_TAG)))
                          .setPlaybackProgress(playbackProgress)
                          .setGenres((String[]) genres.toArray(new String[genres.size()]))
                          .setContentTypes(
                                  (String[]) contentType.toArray(new String[contentType.size()]))
                          .setActions(actions)
                          .setLastWatchedDateTime(lastWatchedDateTime)
                          .build();
        }
        catch (Exception e) {
            Log.e(TAG, "Unable to build recommendation", e);
        }
        return null;
    }

    /**
     * Finds the content within the root content container.
     *
     * @param contentId The id of the content to find.
     * @return The content, or null if it was not found.
     */
    Content getContentFromRoot(String contentId) {

        Content content;

        if (mRootContentContainer == null) {
            Log.e(TAG, "Cannot find content from empty root.");
            return null;
        }

        content = mRootContentContainer.findContentById(contentId);
        return content;
    }

    /**
     * Set the root content container.
     *
     * @param rootContentContainer The root content container.
     */
    void setRootContentContainer(ContentContainer rootContentContainer) {

        mRootContentContainer = rootContentContainer;
    }

    /**
     * Get the root content container.
     *
     * @return The root content container.
     */
    ContentContainer getRootContentContainer() {

        return mRootContentContainer;
    }

    /**
     * Generates of list of recommendation ids to use from 1 to the n, the max number of
     * recommendations to send at one time.
     *
     * @param max      The max number of recommendations to send at one time.
     * @return List of recommendation ids.
     */
    private ArrayList<Integer> buildRecommendationIdList(int max) {

        ArrayList<Integer> ids = new ArrayList<>();
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        
        List<Integer> usedIds = databaseHelper.getAllRecommendationsIds(mContext);
        int i = 1;
        while (ids.size() < max) {
            if (!usedIds.contains(i)) {
                ids.add(i);
            }
            i++;
        }
        return ids;
    }

    /**
     * Builds the content intent for the recommendation. Needs to use reflection to get the Class
     * of the activity to be launched with the Intent, since this module may not know about the
     * class that's needed.
     *
     * @param context   The context.
     * @param contentId The id of the content to recommend.
     * @return The content intent.
     */
    private Intent buildContentIntent(Context context, String contentId) {

        String packageName = context.getPackageName();
        PackageManager pm = context.getPackageManager();
        Intent intent = pm.getLaunchIntentForPackage(packageName);

        if (intent != null) {
            intent.setAction(LauncherIntegrationManager.PLAY_CONTENT_FROM_LAUNCHER_ACTION);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            intent.putExtra(RecommendationExtras.EXTRA_AMAZON_CONTENT_ID, contentId);
            intent.putExtra(CONTENT_SOURCE, RECOMMENDED_CONTENT);
            intent.putExtra(context.getString(R.string.launcher_integration_content_id_key),
                            contentId);
        }

        return intent;
    }

    /**
     * Builds the dismiss intent for the recommendation.
     *
     * @param context        The context.
     * @param notificationId The notification id.
     * @return The dismiss intent.
     */
    private Intent buildDismissIntent(Context context, long notificationId) {

        Intent serviceToLaunch = new Intent(context, DeleteRecommendationService.class);
        serviceToLaunch.putExtra(RecommendationManager.NOTIFICATION_ID_TAG, notificationId);
        return serviceToLaunch;
    }

    /**
     * Sends the recommendation as a notification to the notification manager.
     *
     * @param recommendationId The recommendation id.
     * @param notification     The recommendation.
     */
    private void sendToNotificationManager(Context context, int recommendationId,
                                           Notification notification) {

        if (notification == null) {
            Log.d(TAG, "Not sending recommendation to notification manager because notification " +
                    "is null");
            return;
        }
        NotificationManager notificationManager =
                (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.notify(recommendationId, notification);
    }
}
