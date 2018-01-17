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

import com.amazon.android.contentbrowser.ContentLoader;
import com.amazon.android.contentbrowser.database.helpers.RecommendationDatabaseHelper;
import com.amazon.android.contentbrowser.database.records.RecommendationRecord;
import com.amazon.android.contentbrowser.helper.AnalyticsHelper;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;

import android.app.NotificationManager;
import android.content.Context;
import android.os.AsyncTask;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;
import rx.subscriptions.CompositeSubscription;

/**
 * The recommendation manager contains the logic required to download the recommendation feed, and
 * build and send recommendations.
 *
 * It contains two main methods:
 * {@link #updateGlobalRecommendations(Context)} and
 * {@link #executeRelatedRecommendationsTask(Context, Content)}. This class relies heavily on the
 * {@link RecommendationSender} and {@link RecommendationDatabaseHelper} classes.
 */
public class RecommendationManager {

    /**
     * Debug recipe chain flag.
     */
    private static final boolean DEBUG_RECIPE_CHAIN = false;

    /**
     * Debug tag.
     */
    private static final String TAG = RecommendationManager.class.getSimpleName();

    /**
     * The default max number of global recommendations to be sent.
     */
    private static int DEFAULT_MAX_GLOBAL_RECOMMENDATIONS = 5;

    /**
     * The default max number of related recommendations to be sent.
     */
    private static int DEFAULT_MAX_RELATED_RECOMMENDATIONS = 5;

    /**
     * Tag to get the notification id from intents.
     */
    static final String NOTIFICATION_ID_TAG = "notification_id";

    /**
     * The context.
     */
    private Context mContext;

    /**
     * Instance of the content loader.
     */
    private ContentLoader mContentLoader;

    /**
     * Composite subscription instance; single use only!!!
     */
    private CompositeSubscription mCompositeSubscription = new CompositeSubscription();

    /**
     * Instance of the recommendation sender.
     */
    private RecommendationSender mSender;

    /**
     * The maximum number of related recommendations to send.
     */
    private int mMaxRelated;

    /**
     * The maximum number of global recommendations to send.
     */
    private int mMaxGlobal;

    /**
     * Constructor.
     *
     * @param context The context.
     */
    public RecommendationManager(Context context) {

        mContentLoader = ContentLoader.getInstance(context);
        mContext = context;

        // Get the number of recommendations to send. Set to the default value if it was not
        // included in the navigator configuration file.
        mMaxRelated = mContentLoader.getNumberOfRelatedRecommendations();
        if (mMaxRelated == -1) {
            mMaxRelated = DEFAULT_MAX_RELATED_RECOMMENDATIONS;
        }
        mMaxGlobal = mContentLoader.getNumberOfGlobalRecommendations();
        if (mMaxGlobal == -1) {
            mMaxGlobal = DEFAULT_MAX_GLOBAL_RECOMMENDATIONS;
        }

        mSender = new RecommendationSender(mContext, mContentLoader.getRootContentContainer(),
                                           true);
    }

    /**
     * Updates the global recommendations. This method will load content data if necessary, then
     * runs the global recommendation recipes.
     *
     * @param context The context.
     */
    public void updateGlobalRecommendations(Context context) {

        if (mContentLoader.getNavigatorModel().getRecommendationRecipes() == null ||
                mContentLoader.getNavigatorModel().getRecommendationRecipes().size() == 0) {
            Log.d(TAG, "No global recommendation recipes to run");
            return;
        }

        Log.d(TAG, "Updating global recommendations.");
        if (mContentLoader.isContentLoaded()) {
            mSender.setRootContentContainer(mContentLoader.getRootContentContainer());
            runGlobalRecommendationRecipes(context);
        }
        else {
            loadDataForRecommendations(context);
        }
        AnalyticsHelper.trackUpdateGlobalRecommendations();
    }

    /**
     * Executes the task for sending related recommendations. Do not call if data is not loaded.
     *
     * @param context The context.
     * @param content The content to send recommendations for.
     */
    public void executeRelatedRecommendationsTask(Context context, Content content) {

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                Log.d(TAG, "Sending related recommendations");
                // Get the list of content ids to recommend.
                List<String> relatedIds = content.getRecommendations();
                mSender.sendRecommendationsForType(RecommendationRecord.RELATED,
                                                   relatedIds,
                                                   mMaxRelated);
                AnalyticsHelper.trackUpdateRelatedRecommendations(content);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                super.onPostExecute(aVoid);
                Log.d(TAG, "Done sending related recommendations.");
            }

        }).execute();
    }

    /**
     * Dismisses the recommendation for the given content id.
     *
     * @param id The content id.
     */
    public void dismissRecommendation(String id) {

        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        if (databaseHelper == null) {
            Log.e(TAG, "Cannot dismiss recommendation because database is null");
            return;
        }
        RecommendationRecord recommendation = databaseHelper.getRecord(mContext, id);
        if (recommendation == null) {
            Log.e(TAG, "Recommendation was not found in database. Can not dismiss notification " +
                    "for content " + id);
            return;
        }
        dismissRecommendation(recommendation.getRecommendationId());
    }

    /**
     * Cleans the database of any expired records, includes recommendations and recently watched
     * content. Tells notification manager to cancel the expired recommendations.
     */
    public void cleanDatabase() {

        Log.d(TAG, "Starting to clean database of old records");
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        if (databaseHelper == null) {
            Log.e(TAG, "Cannot clean database because database is null");
            return;
        }

        NotificationManager notificationManager = (NotificationManager) mContext.getSystemService
                (Context.NOTIFICATION_SERVICE);
        if (notificationManager == null) {
            Log.e(TAG, "Can not clean database because notification manager is null");
            return;
        }

        List<RecommendationRecord> records = databaseHelper.getExpiredRecommendations(mContext);
        for (RecommendationRecord record : records) {

            notificationManager.cancel(record.getRecommendationId());
        }
        databaseHelper.purgeExpiredRecords(mContext);
        AnalyticsHelper.trackExpiredRecommendations(records.size());
        Log.d(TAG, "Done cleaning database");
    }

    /**
     * Dismisses the recommendation with the given id.
     *
     * @param id The recommendation id.
     */
    void dismissRecommendation(int id) {

        if (id == -1) {
            Log.e(TAG, "Can't deleteByContentId notification with invalid id: " + id);
            return;
        }

        NotificationManager notificationManager =
                (NotificationManager) mContext.getSystemService(Context.NOTIFICATION_SERVICE);

        notificationManager.cancel(id);
    
        RecommendationDatabaseHelper databaseHelper = RecommendationDatabaseHelper.getInstance();
        if (databaseHelper != null && !databaseHelper.deleteByRecId(mContext, id)) {
            Log.d(TAG, "Error deleting recommendation from database with id " + id);
            return;
        }
        Log.d(TAG, "Deleted recommendation id: " + id);

    }

    /**
     * Get the content loader.
     *
     * @return The content loader.
     */
    ContentLoader getContentLoader() {

        return mContentLoader;
    }

    /**
     * Runs the app's global recipes in order to have content data loaded so the recommendations
     * can be built.
     *
     * @param context The context.
     */
    private void loadDataForRecommendations(Context context) {

        Log.d(TAG, "Loading data for recommendations");
        final ContentContainer root = new ContentContainer("Root");

        Subscription subscription =
                Observable.range(0, mContentLoader.getNavigatorModel().getGlobalRecipes().size())
                          // Do this first to make sure were running in new thread right a way.
                          .subscribeOn(Schedulers.newThread())
                          .concatMap(index -> mContentLoader.runGlobalRecipeAtIndex(index, root))
                          .onBackpressureBuffer() // This must be right after concatMap.
                          .doOnNext(o -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "doOnNext");
                              }
                          })
                          // This should be last so the rest is running on a separate thread.
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(objectPair -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "subscriber onNext called");
                              }
                          }, throwable -> {
                              Log.e(TAG, "Recipe chain failed:", throwable);

                          }, () -> {
                              Log.v(TAG, "Recipe chain completed");
                              // Remove empty sub containers.
                              root.removeEmptySubContainers();

                              mSender.setRootContentContainer(root);
                              mContentLoader.setContentReloadRequired(false);
                              mContentLoader.setContentLoaded(true);

                              updateGlobalRecommendations(context);
                          });

        mCompositeSubscription.add(subscription);
    }

    /**
     * Runs the recipes for global recommendations. Once all recipes are done running, an async
     * task for sending the recommendations is executed.
     */
    private void runGlobalRecommendationRecipes(Context context) {

        Log.d(TAG, "Running recommendation recipes");

        final ArrayList<String> root = new ArrayList<>();
        Subscription subscription =
                Observable.range(0, mContentLoader.getNavigatorModel().getRecommendationRecipes()
                                                  .size())
                          // Do this first to make sure were running in new thread right a way.
                          .subscribeOn(Schedulers.newThread())
                          .concatMap(index -> mContentLoader.runRecommendationRecipeAtIndex
                                  (index, root))
                          .onBackpressureBuffer() // This must be right after concatMap.
                          .doOnNext(o -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "doOnNext");
                              }
                          })
                          // This should be last so the rest is running on a separate thread.
                          .observeOn(AndroidSchedulers.mainThread())
                          .subscribe(objectPair -> {
                              if (DEBUG_RECIPE_CHAIN) {
                                  Log.d(TAG, "subscriber onNext called");
                              }
                          }, throwable -> {
                              Log.e(TAG, "Recipe chain failed:", throwable);

                          }, () -> {
                              Log.v(TAG, "Recipe chain completed");

                              // Make recommendations.
                              executeGlobalRecommendationsTask(context, root);

                          });

        mCompositeSubscription.add(subscription);
    }

    /**
     * Sends global recommendations in an async task.
     *
     * @param context    The context.
     * @param contentIds List of content ids to send recommendations for.
     */
    private void executeGlobalRecommendationsTask(Context context, ArrayList<String> contentIds) {

        (new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                mSender.sendRecommendationsForType(RecommendationRecord.GLOBAL, contentIds,
                                                   mMaxGlobal);
                return null;
            }

            @Override
            protected void onPostExecute(Void aVoid) {

                super.onPostExecute(aVoid);
                Log.d(TAG, "Done sending global recommendations");

                // Clean up observable subscriptions to avoid memory leaks.
                if (mCompositeSubscription.hasSubscriptions()) {
                    mCompositeSubscription.unsubscribe();
                    // CompositeSubscription is a single use, create a new one for next round.
                    mCompositeSubscription = null;
                    mCompositeSubscription = new CompositeSubscription();
                }
            }

        }).execute();
    }
}
