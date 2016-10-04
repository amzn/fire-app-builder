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

package com.amazon.android.contentbrowser.helper;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.R;
import com.amazon.android.contentbrowser.app.ContentBrowserApplication;
import com.amazon.android.model.content.Content;

import android.content.Context;
import android.support.annotation.NonNull;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.amazon.analytics.AnalyticsActionBuilder;
import com.amazon.analytics.AnalyticsConstants;
import com.amazon.analytics.AnalyticsManager;

/**
 * Analytics helper class.
 */
public class AnalyticsHelper {

    /**
     * Debug TAG.
     */
    private static final String TAG = AnalyticsHelper.class.getSimpleName();

    /**
     * Video type.
     */
    private static final String TYPE_VIDEO = "video";

    /**
     * Episode type.
     */
    private static final String TYPE_EPISODE = "episode";

    /**
     * Gets the attributes that are necessary for tracking actions that involve a Movie.
     *
     * @param content The Movie content object.
     * @return A map of data key/values that are needed for tracking actions that involve a Movie.
     */
    private static Map<String, Object> getBasicAnalyticsAttributesForContent(Content content) {

        HashMap<String, Object> analyticsAttributes = new HashMap<>();
        // Set up the movie attributes.
        analyticsAttributes.put(AnalyticsConstants.ATTRIBUTE_TITLE, content.getTitle());
        String type = getContentType(content);
        // Add episode attributes, if content is an episode.
        if (TYPE_EPISODE.equals(type)) {
            analyticsAttributes.put(AnalyticsConstants.ATTRIBUTE_SUBTITLE, content.getSubtitle());
        }
        // Record type of content.
        analyticsAttributes.put(AnalyticsConstants.ATTRIBUTE_VIDEO_TYPE, type);

        // Record content id.
        analyticsAttributes.put(AnalyticsConstants.ATTRIBUTE_VIDEO_ID, content.getId());


        return analyticsAttributes;
    }

    /**
     * Returns what type of content this is; full-length video or episode.
     *
     * @param content Content for which type needs to be determined.
     * @return Content type of this content which could be full length video or episode.
     */
    @NonNull
    private static String getContentType(Content content) {

        String type = TYPE_VIDEO;
        if (content.getSubtitle() != null && !content.getSubtitle().isEmpty()) {
            type = TYPE_EPISODE;
        }
        return type;
    }

    /**
     * Track actions taken from Content details page.
     *
     * @param content  Content.
     * @param actionId Action id.
     */
    public static void trackContentDetailsAction(Content content, int actionId) {

        HashMap<String, Object> data = new HashMap<>();

        // Get the attributes for the selected movie.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);

        // Record the action.
        switch (actionId) {
            case ContentBrowser.CONTENT_ACTION_RESUME:
            case ContentBrowser.CONTENT_ACTION_WATCH_NOW:
            case ContentBrowser.CONTENT_ACTION_WATCH_FROM_BEGINNING:
                // Playback action.
                data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_PLAY_VIDEO);
                // Record what action started the playback.
                attributes.put(AnalyticsConstants.ATTRIBUTE_PLAY_SOURCE,
                               getActionButtonText(ContentBrowserApplication.getInstance(),
                                                   actionId));
                break;
            case ContentBrowser.CONTENT_ACTION_SUBSCRIPTION:
            case ContentBrowser.CONTENT_ACTION_DAILY_PASS:
                data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants
                        .ACTION_PURCHASE_INITIATED);
                attributes.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_TYPE,
                               getActionButtonText(ContentBrowserApplication.getInstance(),
                                                   actionId));
                break;
            default:
                // We shouldn't reach this. If we do an unknown action occurred.
                Log.e(TAG, "Unknown action button with id " + actionId + " was clicked and " +
                        "analytics couldn't record it");
                return;
        }

        // Add the attributes to the data and track the action.
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Tracks the ending of a content.
     *
     * @param content  Content to track.
     * @param duration The duration for which the content was played.
     */
    public static void trackContentFinished(Content content, long duration) {

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_PLAYBACK_FINISHED);
        // Get the attributes for the selected movie.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsConstants.ATTRIBUTE_VIDEO_SECONDS_WATCHED, duration);
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Tracks purchase action result.
     *
     * @param sku            Sku being bought.
     * @param purchaseResult Result of the purchase.
     */
    public static void trackPurchaseResult(String sku, boolean purchaseResult) {

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_PURCHASE_COMPLETE);

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_RESULT, purchaseResult);
        attributes.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_SKU, sku);
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Tracks playback actions of the content.
     *
     * @param action  Action taken on this content.
     * @param content Content on which the action was taken.
     */
    public static void trackContentAction(String action, Content content) {

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, action);
        // Get the attributes for the selected content.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Tracks unexpected situations or errors not generated via Exceptions.
     *
     * @param tag          TAG for logging the error.
     * @param errorMessage Error message to track.
     */
    public static void trackError(String tag, String errorMessage) {

        Log.e(tag, errorMessage);
        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_ERROR);
        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsConstants.ATTRIBUTE_ERROR_MSG, errorMessage);
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Tracks Exceptions generated anytime during the app execution.
     *
     * @param tag          TAG for logging the error.
     * @param errorMessage Error message to track.
     * @param e            Exception to log.
     */
    public static void trackError(String tag, String errorMessage, Exception e) {

        Log.e(tag, errorMessage, e);
        trackError(tag, errorMessage);
    }

    /**
     * Tracks starting of ad.
     *
     * @param content Content during which the ad was started.
     */
    public static void trackAdStarted(Content content) {

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_AD_START);
        HashMap<String, Object> attributes = new HashMap<>();
        commonAdTrackingSteps(content, data, attributes);
    }

    /**
     * Tracks Ending of ad.
     * TODO: test with an actual ad, see what happens if back is pressed while the ad is running
     * SDK 4326
     *
     * @param content  Content during which the ad was played.
     * @param duration Duration of ad run.
     */
    public static void trackAdEnded(Content content, long duration) {

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> attributes = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_AD_COMPLETE);
        attributes.put(AnalyticsConstants.ATTRIBUTE_AD_SECONDS_WATCHED, duration);
        commonAdTrackingSteps(content, data, attributes);
    }

    /**
     * Performs common ad tracking steps like adding ad analytics attributes.
     *
     * @param content    Content during which the ad was played.
     * @param data       Action map to track.
     * @param attributes Attributes map to be added to action map.
     */
    private static void commonAdTrackingSteps(Content content, HashMap<String, Object> data,
                                              HashMap<String, Object> attributes) {
        //TODO: add ad id SDK 4326
        attributes.put(AnalyticsConstants.ATTRIBUTE_AD_ID, "");
        attributes.putAll(getBasicAnalyticsAttributesForContent(content));
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);
        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(data);
    }

    /**
     * Track search query.
     *
     * @param query Search query.
     */
    public static void trackSearchQuery(String query) {

        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(AnalyticsActionBuilder.buildSearchActionData(query));
    }

    /**
     * Track application entry.
     */
    public static void trackAppEntry() {

        AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                        .getIAnalytics()
                        .trackAction(AnalyticsActionBuilder.buildInitActionData
                                (ContentBrowserApplication.getInstance()));
    }

    /**
     * Get action button's text.
     *
     * @param context  Context.
     * @param actionId Action id.
     * @return Action button's text.
     */
    private static String getActionButtonText(Context context, int actionId) {

        StringBuilder sb = new StringBuilder();

        if (actionId == ContentBrowser.CONTENT_ACTION_WATCH_NOW) {
            sb.append(context.getString(R.string.watch_now_1))
              .append(context.getString(R.string.watch_now_2));
        }
        else if (actionId == ContentBrowser.CONTENT_ACTION_WATCH_FROM_BEGINNING) {
            sb.append(context.getString(R.string.watch_from_beginning_1))
              .append(context.getString(R.string.watch_from_beginning_2));
        }
        else if (actionId == ContentBrowser.CONTENT_ACTION_RESUME) {
            sb.append(context.getString(R.string.resume_1))
              .append(context.getString(R.string.resume_2));

        }
        else if (actionId == ContentBrowser.CONTENT_ACTION_DAILY_PASS) {
            sb.append(context.getString(R.string.daily_pass_1))
              .append(context.getString(R.string.daily_pass_2));
        }
        else if (actionId == ContentBrowser.CONTENT_ACTION_SUBSCRIPTION) {
            sb.append(context.getString(R.string.premium_1))
              .append(context.getString(R.string.premium_2));
        }

        return sb.toString();
    }
}
