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

import com.amazon.ads.AdMetaData;
import com.amazon.analytics.ExtraContentAttributes;
import com.amazon.analytics.IAnalytics;
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
import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.AnalyticsManager;
import com.amazon.android.recipe.Recipe;

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
     * Gets the attributes that are necessary for tracking actions that involve content.
     *
     * @param content The content object.
     * @return A map of data key/values that are needed for tracking actions that involve content.
     */
    private static Map<String, Object> getBasicAnalyticsAttributesForContent(Content content) {

        HashMap<String, Object> analyticsAttributes = new HashMap<>();

        // If the app is launched from a playback request from launcher the content here may be null
        // because content browser has no selected content yet.
        if (content == null) {
            Log.e(TAG, "Content is null when trying to get basic analytics attributes.");
            return analyticsAttributes;
        }

        // Set up the movie attributes.
        analyticsAttributes.put(AnalyticsTags.ATTRIBUTE_TITLE, content.getTitle());

        String type = getContentType(content);
        // Add episode attributes, if content is an episode.
        if (TYPE_EPISODE.equals(type)) {
            analyticsAttributes.put(AnalyticsTags.ATTRIBUTE_SUBTITLE, content.getSubtitle());
        }
        // Record type of content.
        analyticsAttributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_TYPE, type);

        // Record content id.
        analyticsAttributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_ID, content.getId());

        return analyticsAttributes;
    }

    /**
     * Get basic analytics attributes plus more detailed attributes for the given Content.
     *
     * @param content Content to get attributes for.
     * @return Attributes for the given Content.
     */
    private static Map<String, Object> getDetailedContentAttributes(Content content) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_PUBLISHER_NAME, content.getStudio());
        attributes.put(AnalyticsTags.ATTRIBUTE_AIRDATE, content.getAvailableDate());
        return attributes;
    }

    /**
     * Get analytics attributes needed to identify the correct content or ad type.
     *
     * @param content Content to get attributes for.
     * @return Attributes needed for the classification of the given Content.
     */
    private static Map<String, Object> getClassificationTypeAttributes(Content content) {

        Map<String, Object> attributes = new HashMap<>();
        boolean liveContent = false;
        if (content != null) {
            liveContent = content.getExtraValue(Recipe.LIVE_FEED_TAG) != null &&
                    Boolean.valueOf(content.getExtraValue(Recipe.LIVE_FEED_TAG).toString());
        }
        attributes.put(AnalyticsTags.ATTRIBUTE_LIVE_FEED, liveContent);

        return attributes;
    }

    /**
     * Use the action and attributes to create the data to send to the analytics module.
     *
     * @param action     Action to send to the analytics module.
     * @param attributes Attributes to send to the analytics module.
     */
    private static void sendAnalytics(String action, Map<String, Object> attributes) {

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsTags.ACTION_NAME, action);
        data.put(AnalyticsTags.ATTRIBUTES, attributes);
        sendAnalytics(data);
    }

    /**
     * Send the data to the analytics module.
     *
     * @param data Data to send to the analytics module.
     */
    private static void sendAnalytics(HashMap<String, Object> data) {

        // This check is made in case AnalyticsManager is not used. This is possible when we
        // try to test individual components that rely on ContentBrowser.
        if (AnalyticsManager.getInstance(ContentBrowserApplication.getInstance()) != null) {

            IAnalytics analyticsInterface =
                    AnalyticsManager.getInstance(ContentBrowserApplication.getInstance())
                                    .getIAnalytics();

            if (analyticsInterface != null) {
                analyticsInterface.trackAction(data);
            }
        }
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
     * @param content  The content to use.
     * @param actionId Action id.
     */
    public static void trackContentDetailsAction(Content content, int actionId) {

        // Get the attributes for the selected movie.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);

        String action;

        // Record the action.
        switch (actionId) {
            case ContentBrowser.CONTENT_ACTION_RESUME:
            case ContentBrowser.CONTENT_ACTION_WATCH_NOW:
            case ContentBrowser.CONTENT_ACTION_WATCH_FROM_BEGINNING:
                // Playback action.
                action = AnalyticsTags.ACTION_PLAY_VIDEO;
                // Record what action started the playback.
                attributes.put(AnalyticsTags.ATTRIBUTE_PLAY_SOURCE,
                               getActionButtonText(ContentBrowserApplication.getInstance(),
                                                   actionId));
                break;
            case ContentBrowser.CONTENT_ACTION_SUBSCRIPTION:
            case ContentBrowser.CONTENT_ACTION_DAILY_PASS:
                action = AnalyticsTags.ACTION_PURCHASE_INITIATED;
                attributes.put(AnalyticsTags.ATTRIBUTE_PURCHASE_TYPE,
                               getActionButtonText(ContentBrowserApplication.getInstance(),
                                                   actionId));
                break;
            default:
                // We shouldn't reach this. If we do an unknown action occurred.
                Log.e(TAG, "Unknown action button with id " + actionId + " was clicked and " +
                        "analytics couldn't record it");
                return;
        }

        sendAnalytics(action, attributes);
    }


    /**
     * Track that the given content started or resumed playback.
     *
     * @param content         Content that started/resumed playback.
     * @param duration        Total duration of the content.
     * @param currentPosition The current playback position.
     * @param totalSegments   Total number of segments to be played.
     * @param currentSegment  Segment number of current content being played.
     */
    public static void trackPlaybackStarted(Content content, long duration, long currentPosition,
                                            int totalSegments, int currentSegment) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.putAll(getDetailedContentAttributes(content));
        attributes.putAll(getClassificationTypeAttributes(content));

        // Get Content extras
        attributes.putAll(ExtraContentAttributes.getExtraAttributes(content.getExtras()));
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION, duration);
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION, currentPosition);
        attributes.put(AnalyticsTags.ATTRIBUTE_NUMBER_OF_SEGMENTS, totalSegments);
        attributes.put(AnalyticsTags.ATTRIBUTE_SEGMENT_NUMBER, currentSegment);
        sendAnalytics(AnalyticsTags.ACTION_PLAYBACK_STARTED, attributes);
    }

    /**
     * Tracks that a content's playback finished. This could be that the content was played to
     * completion or that the player was exited.
     *
     * @param content          Content to track.
     * @param startingPosition The position that this playback session started at.
     * @param currentPosition  The current playback position.
     */
    public static void trackPlaybackFinished(Content content, long startingPosition,
                                             long currentPosition) {

        // Get the attributes for the selected movie.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_SECONDS_WATCHED,
                       currentPosition - startingPosition);
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION, currentPosition);

        sendAnalytics(AnalyticsTags.ACTION_PLAYBACK_FINISHED, attributes);
    }

    /**
     * Tracks purchase action result.
     *
     * @param sku            Sku being bought.
     * @param purchaseResult Result of the purchase.
     */
    public static void trackPurchaseResult(String sku, boolean purchaseResult) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_PURCHASE_RESULT, purchaseResult);
        attributes.put(AnalyticsTags.ATTRIBUTE_PURCHASE_SKU, sku);

        sendAnalytics(AnalyticsTags.ACTION_PURCHASE_COMPLETE, attributes);
    }

    /**
     * Tracks Authentication Request.
     *
     */
    public static void trackAuthenticationRequest() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHENTICATION_SUBMITTED, 1);

        sendAnalytics(AnalyticsTags.ACTION_AUTHENTICATION_REQUESTED, attributes);
    }

    /**
     * Tracks Authentication Successes.
     *
     */
    public static void trackAuthenticationResultSuccess() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHENTICATION_SUCCESS, 1);

        sendAnalytics(AnalyticsTags.ACTION_AUTHENTICATION_SUCCEEDED, attributes);
    }

    /**
     * Tracks Authentication Failures.
     *
     * @param failureReason     Failure Reason Registration/Network/Authentication/Authorization
     */
    public static void trackAuthenticationResultFailure(String failureReason) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHENTICATION_FAILURE, 1);
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHENTICATION_FAILURE_REASON, failureReason);

        sendAnalytics(AnalyticsTags.ACTION_AUTHENTICATION_FAILED, attributes);
    }

    /**
     * Tracks Authorization Requests.
     *
     * @param content selected content
     */
    public static void trackAuthorizationRequest(Content content) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHORIZATION_SUBMITTED, 1);

        sendAnalytics(AnalyticsTags.ACTION_AUTHORIZATION_REQUESTED, attributes);
    }

    /**
     * Tracks Authorization Successes.
     *
     * @param content selected content
     */
    public static void trackAuthorizationResultSuccess(Content content) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHORIZATION_SUCCESS, 1);

        sendAnalytics(AnalyticsTags.ACTION_AUTHORIZATION_SUCCEEDED, attributes);
    }

    /**
     * Tracks Authorization Failures.
     *
     * @param content selected content
     * @param failureReason     Failure Reason Action/Bad Request/Internal/Network/Unknown
     */
    public static void trackAuthorizationResultFailure(Content content, String failureReason) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHORIZATION_FAILURE, 1);
        attributes.put(AnalyticsTags.ATTRIBUTE_AUTHORIZATION_FAILURE_REASON, failureReason);

        sendAnalytics(AnalyticsTags.ACTION_AUTHORIZATION_FAILED, attributes);
    }

    /**
     * Tracks Log Out Request.
     *
     */
    public static void trackLogOutRequest() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_LOGOUT_SUBMITTED, 1);

        sendAnalytics(AnalyticsTags.ACTION_LOG_OUT_REQUESTED, attributes);
    }

    /**
     * Tracks Log Out Successes.
     *
     */
    public static void trackLogOutResultSuccess() {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_LOGOUT_SUCCESS, 1);

        sendAnalytics(AnalyticsTags.ACTION_LOG_OUT_SUCCEEDED, attributes);
    }

    /**
     * Tracks Log Out Failures.
     *
     * @param failureReason     Failure Reason Action/Bad Request/Internal/Network/Unknown
     */
    public static void trackLogOutResultFailure(String failureReason) {

        Map<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_LOGOUT_FAILURE, 1);
        attributes.put(AnalyticsTags.ATTRIBUTE_LOGOUT_FAILURE_REASON, failureReason);

        sendAnalytics(AnalyticsTags.ACTION_LOG_OUT_FAILED, attributes);
    }

    /**
     * Tracks when users interact with the playback controls of the player.
     *
     * @param action          Action taken on this content.
     * @param content         Content on which the action was taken.
     * @param currentPosition Current position in the content playback that the ad finished.
     */
    public static void trackPlaybackControlAction(String action, Content content,
                                                  long currentPosition) {

        // Get the attributes for the selected content.
        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION, currentPosition);

        sendAnalytics(action, attributes);
    }

    /**
     * Tracks unexpected situations or errors not generated via Exceptions.
     *
     * @param tag          TAG for logging the error.
     * @param errorMessage Error message to track.
     */
    public static void trackError(String tag, String errorMessage) {

        Log.e(tag, errorMessage);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_ERROR_MSG, errorMessage);

        sendAnalytics(AnalyticsTags.ACTION_ERROR, attributes);
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
     * @param content         Content during which the ad was started.
     * @param currentPosition Current position in the content playback that the ad started at.
     * @param adMetaData      metaData containing ad details.
     */
    public static void trackAdStarted(Content content, long currentPosition, AdMetaData
            adMetaData) {

        commonAdTrackingSteps(content, AnalyticsTags.ACTION_AD_START, new HashMap<>(),
                              currentPosition, adMetaData);
    }

    /**
     * Tracks Ending of ad.
     * TODO: test with an actual ad, see what happens if back is pressed while the ad is running
     * SDK 4326
     *
     * @param content         Content during which the ad was played.
     * @param currentPosition Current position in the content playback that the ad finished at.
     * @param adMetaData      metaData containing ad details.
     */
    public static void trackAdEnded(Content content, long currentPosition, AdMetaData adMetaData) {

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_AD_SECONDS_WATCHED, adMetaData.getDurationPlayed());

        commonAdTrackingSteps(content, AnalyticsTags.ACTION_AD_COMPLETE, attributes,
                              currentPosition, adMetaData);
    }

    /**
     * Performs common ad tracking steps like adding ad analytics attributes.
     *
     * @param content         Content during which the ad was played.
     * @param action          Action to track.
     * @param attributes      Attributes map to be added to action map.
     * @param currentPosition Current position in the content playback that the ad played.
     * @param adMetaData      metaData containing ad details.
     */
    private static void commonAdTrackingSteps(Content content, String action,
                                              HashMap<String, Object> attributes,
                                              long currentPosition, AdMetaData adMetaData) {

        attributes.put(AnalyticsTags.ATTRIBUTE_AD_ID, adMetaData.getAdId());
        attributes.put(AnalyticsTags.ATTRIBUTE_AD_DURATION, adMetaData.getDurationReceived());
        attributes.put(AnalyticsTags.ATTRIBUTE_ADVERTISEMENT_TYPE, adMetaData.getAdType());
        attributes.putAll(getBasicAnalyticsAttributesForContent(content));
        attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION, currentPosition);

        sendAnalytics(action, attributes);
    }

    /**
     * Track search query.
     *
     * @param query Search query.
     */
    public static void trackSearchQuery(String query) {

        sendAnalytics(AnalyticsActionBuilder.buildSearchActionData(query));
    }

    /**
     * Track application entry.
     */
    public static void trackAppEntry() {

        sendAnalytics(AnalyticsActionBuilder.buildInitActionData(
                ContentBrowserApplication.getInstance()));
    }

    /**
     * Tracks requests received from launcher to play content
     *
     * @param contentId     contentId of the content requested to be played
     * @param content       content corresponding to the contentId
     * @param requestSource Source of request, it could be Catalog integration or Recommendations
     */
    public static void trackLauncherRequest(String contentId, Content content,
                                            String requestSource) {

        HashMap<String, Object> attributes = new HashMap<>();
        if (requestSource != null) {
            attributes.put(AnalyticsTags.ATTRIBUTE_REQUEST_SOURCE, requestSource);
        }
        if (content != null) {
            attributes.put(AnalyticsTags.ATTRIBUTE_CONTENT_AVAILABLE, true);
            attributes.putAll(getBasicAnalyticsAttributesForContent(content));
        }
        else {
            attributes.put(AnalyticsTags.ATTRIBUTE_CONTENT_AVAILABLE, false);
            attributes.put(AnalyticsTags.ATTRIBUTE_VIDEO_ID, contentId);
        }
        sendAnalytics(AnalyticsTags.ACTION_REQUEST_FROM_LAUNCHER_TO_PLAY_CONTENT, attributes);
    }

    /**
     * Tracks broadcasts sent with Authentication status
     *
     * @param isUserAuthenticated is user authenticated?
     */
    public static void trackAppAuthenticationStatusBroadcasted(boolean isUserAuthenticated) {

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_APP_AUTHENTICATION_STATUS, isUserAuthenticated);
        sendAnalytics(AnalyticsTags.ACTION_APP_AUTHENTICATION_STATUS_BROADCAST, attributes);
    }

    /**
     * Tracks requests from launcher for the app's Authentication status
     */
    public static void trackAppAuthenticationStatusRequested() {

        HashMap<String, Object> attributes = new HashMap<>();
        sendAnalytics(AnalyticsTags.ACTION_APP_AUTHENTICATION_STATUS_REQUESTED_BY_LAUNCHER,
                      attributes);
    }

    /**
     * Track calls to Recommendation deletion service
     *
     * @param recommendationId recommendationId to be deleted
     */
    public static void trackDeleteRecommendationServiceCalled(int recommendationId) {

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_RECOMMENDATION_ID, recommendationId);
        sendAnalytics(AnalyticsTags.ACTION_DELETE_RECOMMENDATION_SERVICE_CALLED, attributes);
    }

    /**
     * Tracks requests to update global recommendations
     */
    public static void trackUpdateGlobalRecommendations() {

        HashMap<String, Object> attributes = new HashMap<>();
        sendAnalytics(AnalyticsTags.ACTION_UPDATE_GLOBAL_RECOMMENDATIONS, attributes);
    }

    /**
     * Tracks recommendation dismiss requests on content completion
     *
     * @param content content completed
     */
    public static void trackDismissRecommendationForCompleteContent(Content content) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        sendAnalytics(AnalyticsTags.ACTION_DISMISS_RECOMMENDATION_ON_CONTENT_COMPLETE, attributes);
    }

    /**
     * Tracks expired records being cleared
     */
    public static void trackExpiredRecommendations(int expiredRecommendationCount) {

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsTags.ATTRIBUTE_EXPIRED_RECOMMENDATIONS_COUNT,
                       expiredRecommendationCount);
        sendAnalytics(AnalyticsTags.ACTION_REMOVE_EXPIRED_RECOMMENDATIONS, attributes);
    }

    /**
     * Tracks requests to update related recommendations to the content
     *
     * @param content Content for which related recommendations need to be updated
     */
    public static void trackUpdateRelatedRecommendations(Content content) {

        Map<String, Object> attributes = getBasicAnalyticsAttributesForContent(content);
        sendAnalytics(AnalyticsTags.ACTION_UPDATE_RELATED_RECOMMENDATIONS, attributes);
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
