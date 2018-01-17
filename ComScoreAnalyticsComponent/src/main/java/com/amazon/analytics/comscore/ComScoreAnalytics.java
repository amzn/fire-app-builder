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
package com.amazon.analytics.comscore;

import android.app.Activity;
import android.content.Context;
import android.content.res.Resources;
import android.util.Log;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import com.amazon.ads.IAds;
import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.CustomAnalyticsTags;
import com.amazon.android.utils.FileHelper;
import com.amazon.android.utils.MapHelper;
import com.amazon.utils.security.ResourceObfuscator;
import com.amazon.analytics.IAnalytics;

import com.comscore.PublisherConfiguration;
import com.comscore.Analytics;
import com.comscore.UsagePropertiesAutoUpdateMode;
import com.comscore.streaming.StreamingAnalytics;

/**
 * An analytics implementation using the comScore framework.
 */
public class ComScoreAnalytics implements IAnalytics {

    /**
     * Debug tag.
     */
    private static final String TAG = ComScoreAnalytics.class.getSimpleName();

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    static final String IMPL_CREATOR_NAME = ComScoreAnalytics.class.getSimpleName();

    /**
     * String used for adding Action in ComScore hidden and screen view events
     */
    private static final String ACTION = "ACTION";

    /**
     * String used for tracking hidden events parsed from json event tags file
     */
    private static final String HIDDEN = "hidden";

    /**
     * String used for tracking screen view events parsed from json event tags file
     */
    private static final String VIEW = "view";

    /**
     * ComScore analytics object
     */
    private StreamingAnalytics mStreamingAnalytics;

    /**
     * Metadata for the current content and ads
     */
    private HashMap<String, String> mCurrentContentMetadata;
    private HashMap<String, String> mCurrentAdMetadata;

    /**
     * HashSets for custom event tags received from parsing custom events json file
     */
    private Set<String> mHiddenEventSet;
    private Set<String> mViewEventSet;

    /**
     * Resource context to be used to get classification type string
     */
    private Resources mComScoreRes;

    /**
     * time in milliseconds to check for long duration video
     */
    private static final int DURATION_LONG_VIDEO = 60000;

    // Is the playback screen the current screen?
    private boolean mOnPlaybackScreen = false;

    private CustomAnalyticsTags mCustomTags = new CustomAnalyticsTags();

    /**
     * HashMap containing string as keys and HashSets of custom event tags as values
     */
    private HashMap<String, HashSet<String>> mCustomEventTags = new HashMap<>();

    /**
     * Initialize the custom event map from the given file.
     *
     * @param context    Context.
     * @param fileNameId File name ID of the file that contains the custom tag mapping.
     */
    private void initEventTags(Context context, int fileNameId) {

        // Read the file only if it exists.
        if (FileHelper.doesFileExist(context, context.getResources().getString(fileNameId))) {
            mCustomEventTags = MapHelper.loadArrayMappingFromJsonFile(context, fileNameId);
            if (mCustomEventTags != null) {
                mHiddenEventSet = mCustomEventTags.get(HIDDEN);
                mViewEventSet = mCustomEventTags.get(VIEW);
            }
            Log.d(TAG, "Custom event tags initialized");
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {

        try {
            // Setup comScore object
            String id = getClientId(context);
            String secret = getPublisherSecret(context);
            PublisherConfiguration myPublisherConfig = new PublisherConfiguration.Builder()
                    .publisherId(getClientId(context))
                    .publisherSecret(getPublisherSecret(context))
                    .usagePropertiesAutoUpdateMode(UsagePropertiesAutoUpdateMode.FOREGROUND_ONLY)
                    .build();
            Analytics.getConfiguration().addClient(myPublisherConfig);
            Analytics.start(context);

        }
        catch (Exception e) {
            Log.e(TAG, "Configuration failed", e);
            return;
        }

        // Setup StreamingAnalytics object
        mStreamingAnalytics = new StreamingAnalytics();

        mCustomTags.init(context, R.string.comscore_analytics_custom_tags);
        initEventTags(context, R.string.comscore_analytics_event_tags);
        mComScoreRes = context.getResources();

        Log.d(TAG, "Configuration complete");
    }

    /**
     * Unobfuscate the ComScore client id.
     *
     * @param context Context.
     * @return The unobfuscated ComScore client id.
     */
    private String getClientId(Context context) throws Exception {

        return ResourceObfuscator.unobfuscate(context.getString(R.string.encrypted_comscore_client_id),
                                              getRandomStringsForKey(context),
                                              getRandomStringsForIv(context));
    }

    /**
     * Unobfuscate the ComScore publisher secret.
     *
     * @param context Context.
     * @return The unobfuscated Comscore publisher secret.
     */
    private String getPublisherSecret(Context context) throws Exception {

        return ResourceObfuscator.unobfuscate(context.getString(R.string.encrypted_comscore_publisher_secret),
                                              getRandomStringsForKey(context),
                                              getRandomStringsForIv(context));
    }

    /**
     * Get a list of "random" strings for key.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForKey(Context context) {

        return new String[]{
                context.getString(R.string.comscore_key_1),
                context.getString(R.string.comscore_key_2),
                context.getString(R.string.comscore_key_3)
        };
    }

    /**
     * Get random strings for use with initialization vector.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForIv(Context context) {

        return new String[]{
                context.getString(R.string.comscore_key_4),
                context.getString(R.string.comscore_key_5),
                context.getString(R.string.comscore_key_6)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        String activityName = activity.getClass().getName();
        if (active) {
            Log.d(TAG, "Collecting lifecycle data for " + activityName);
            Analytics.notifyEnterForeground();
        }
        else {
            Log.d(TAG, "Ending lifecycle data collection for " + activityName);
            Analytics.notifyExitForeground();
        }
    }

    /**
     * Check if current content is live content.
     *
     * @param attributes attributes received from analytics helper.
     * @return is live content or not.
     */
    boolean isLiveContent(HashMap<String, String> attributes) {

        return (attributes.containsKey(AnalyticsTags.ATTRIBUTE_LIVE_FEED) &&
                Boolean.valueOf(attributes.get(AnalyticsTags.ATTRIBUTE_LIVE_FEED)));
    }

    /**
     * check if current content is long duration content as per ComScore definition
     *
     * @param attributes attributes received from analytics helper.
     * @return is long duration video or not.
     */
    boolean isLongFormVideoContent(HashMap<String, String> attributes) {

        return (attributes.containsKey(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION) &&
                Integer.valueOf(attributes.get(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION)) >=
                        DURATION_LONG_VIDEO);
    }

    /**
     * Get content classification type label to be added with other attributes for Com Score.
     * Currently checking only for Live vs on demand video content
     *
     * @param attributes attributes received from analytics helper.
     * @return classification type label.
     */
    private String getContentClassificationTypeLabel(HashMap<String, String> attributes) {

        if (attributes != null) {

            //This is a live streaming content
            if (isLiveContent(attributes)) {
                return mComScoreRes.getString(R.string.premium_live_streaming_content);
            }

            //checking for long or short duration video on demand content
            if (isLongFormVideoContent(attributes)) {
                return mComScoreRes.getString(R.string.premium_long_form_video_on_demand_content);
            }
            else {
                return mComScoreRes.getString(R.string.premium_short_form_video_on_demand_content);
            }
        }
        return mComScoreRes.getString(R.string.other_content);
    }

    /**
     * Get Ad classification type label to be added with other attributes for Com Score.
     * Currently checking only for ads during Live vs on demand video
     *
     * @param attributes attributes received from analytics helper.
     * @return classification type label.
     */
    private String getAdClassificationTypeLabel(HashMap<String, String> attributes) {

        if (attributes != null) {

            //This is a live streaming content
            if (isLiveContent(attributes)) {
                return mComScoreRes.getString(R.string.linear_live_ad);
            }

            //checking for current Ad type
            if (attributes.containsKey(AnalyticsTags.ATTRIBUTE_ADVERTISEMENT_TYPE)) {
                switch (attributes.get(AnalyticsTags.ATTRIBUTE_ADVERTISEMENT_TYPE)) {
                    case IAds.PRE_ROLL_AD:
                        return mComScoreRes.getString(R.string.linear_pre_roll_video_on_demand_ad);
                    case IAds.MID_ROLL_AD:
                        return mComScoreRes.getString(R.string.linear_mid_roll_video_on_demand_ad);
                    case IAds.POST_ROLL_AD:
                        return mComScoreRes.getString(R.string.linear_post_roll_video_on_demand_ad);
                }
            }
        }
        return mComScoreRes.getString(R.string.other_ad);
    }

    /**
     * Remove content tags from ad analytics tags as ComScore doesn't provide separate
     * id and duration tags for content and ad.
     *
     * @param attributes attributes received from analytics helper.
     */
    private void removeOverlappedTagsInAdData(HashMap<String, String> attributes) {

        if (attributes.containsKey(AnalyticsTags.ATTRIBUTE_VIDEO_ID)) {
            attributes.remove(AnalyticsTags.ATTRIBUTE_VIDEO_ID);
        }
        if (attributes.containsKey(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION)) {
            attributes.remove(AnalyticsTags.ATTRIBUTE_VIDEO_DURATION);
        }
    }

    /**
     * Check for hidden event (Fire app builder track these events internally with name action,
     * While ComScore treat them as hidden / screen view events)
     *
     * @param event current event happened in system which need to be provided to analytics module.
     * @return boolean response whether the current event is a hidden event
     */
    private boolean isHiddenEvent(String event) {

        return (mHiddenEventSet != null && mHiddenEventSet.contains(event));
    }

    /**
     * Check for screen view event (Fire app builder track these events internally with name action,
     * While ComScore treat them as hidden / screen view events)
     *
     * @param event current event happened in system which need to be provided to analytics module.
     * @return boolean response whether the current event is a screen view event
     */
    private boolean isScreenViewEvent(String event) {

        return (mViewEventSet != null && mViewEventSet.contains(event));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {

        String action = (String) data.get(AnalyticsTags.ACTION_NAME);
        Log.d(TAG, "Tracking Action: " + action);

        @SuppressWarnings("unchecked")
        HashMap<String, Object> attributes =
                (HashMap<String, Object>) data.get(AnalyticsTags.ATTRIBUTES);

        if (attributes == null) {
            Log.d(TAG, "Action has no attribute/value pairs");
            return;
        }
        // Log action attributes
        Log.d(TAG, "Action has the following attributes: " + attributes.toString());

        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_STARTED)) {
            trackPlaybackStarted(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_AD_START)) {
            trackAdStarted(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_AD_COMPLETE)) {
            trackAdFinished(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_CONTROL_PAUSE)) {
            trackPlaybackPaused(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_CONTROL_PLAY)) {
            trackPlaybackResumed(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_BUFFER_START)) {
            trackBufferingStarted(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_BUFFER_END)) {
            trackBufferingEnded(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_CONTROL_FF) ||
                action.equals(AnalyticsTags.ACTION_PLAYBACK_CONTROL_REWIND)) {
            trackSeekStarted(attributes);
            return;
        }
        if (action.equals(AnalyticsTags.ACTION_PLAYBACK_FINISHED)) {
            trackPlaybackFinished(attributes);
            return;
        }
        if (isHiddenEvent(action)) {
            attributes.put(ACTION, action);
            trackHiddenEvents(attributes);
            return;
        }
        if (isScreenViewEvent(action)) {
            attributes.put(ACTION, action);
            trackScreenViewEvents(attributes);
            return;
        }
    }

    /**
     * Tell ComScore that a hidden event has occurred.
     * Any event that is not considered to be a app start event or screen view event
     *
     * @param attributes Attributes of the hidden event.
     */
    private void trackHiddenEvents(HashMap<String, Object> attributes) {

        HashMap<String, String> hiddenEventData = convertMapToStrings(attributes);
        Analytics.notifyHiddenEvent(hiddenEventData);
    }

    /**
     * Tell ComScore that a view event has occurred.
     * A screen view event indicates that application screen has changed substantially (50% or more)
     *
     * @param attributes Attributes of the screen view event.
     */
    private void trackScreenViewEvents(HashMap<String, Object> attributes) {

        HashMap<String, String> viewEventData = convertMapToStrings(attributes);
        Analytics.notifyViewEvent(viewEventData);
    }

    /**
     * Tell ComScore that new content started playing.
     *
     * @param attributes Attributes of the new content.
     */
    private void trackPlaybackStarted(HashMap<String, Object> attributes) {

        mCurrentContentMetadata = convertMapToStrings(attributes);
        mCurrentContentMetadata.put(AnalyticsTags.ATTRIBUTE_CLASSIFICATION_TYPE,
                                    getContentClassificationTypeLabel(mCurrentContentMetadata));
        mStreamingAnalytics.createPlaybackSession();
        mStreamingAnalytics.getPlaybackSession().setAsset(mCustomTags.getCustomTags
                (mCurrentContentMetadata, false));
        mStreamingAnalytics.notifyPlay(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that an ad started playing.
     *
     * @param attributes Attributes for the ad.
     */
    private void trackAdStarted(HashMap<String, Object> attributes) {

        mCurrentAdMetadata = convertMapToStrings(attributes);
        //Remove Video Content id and duration as ComScore does not provide separate tags for Ad.
        removeOverlappedTagsInAdData(mCurrentAdMetadata);
        mCurrentAdMetadata.put(AnalyticsTags.ATTRIBUTE_CLASSIFICATION_TYPE,
                               getAdClassificationTypeLabel(mCurrentAdMetadata));
        mStreamingAnalytics.getPlaybackSession().setAsset(mCustomTags.getCustomTags
                (mCurrentAdMetadata, false));
        mStreamingAnalytics.notifyPlay(0);
    }

    /**
     * Tell ComScore that an ad finished playing.
     *
     * @param attributes Attributes for the ad.
     */
    private void trackAdFinished(HashMap<String, Object> attributes) {

        mCurrentAdMetadata = convertMapToStrings(attributes);
        //Remove Video Content id and duration as ComScore does not provide separate tags for Ad.
        removeOverlappedTagsInAdData(mCurrentAdMetadata);
        mStreamingAnalytics.notifyEnd(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that playback was paused.
     *
     * @param attributes Attributes for the content that was paused.
     */
    private void trackPlaybackPaused(HashMap<String, Object> attributes) {

        mStreamingAnalytics.notifyPause(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that playback was resumed.
     *
     * @param attributes Attributes for the content that was resumed.
     */
    private void trackPlaybackResumed(HashMap<String, Object> attributes) {

        mStreamingAnalytics.notifyPlay(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that a buffer event started during playback.
     *
     * @param attributes Attributes for the content that was playing when the buffer event started.
     */
    private void trackBufferingStarted(HashMap<String, Object> attributes) {

        mStreamingAnalytics.notifyBufferStart(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that a buffer event ended.
     *
     * @param attributes Attributes for the content that was playing when the buffer event started.
     */
    private void trackBufferingEnded(HashMap<String, Object> attributes) {

        mStreamingAnalytics.notifyBufferStop(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that a seek event started during playback.
     *
     * @param attributes Attributes for the content that was playing when the seek event started.
     */
    private void trackSeekStarted(HashMap<String, Object> attributes) {

        mStreamingAnalytics.notifySeekStart(getCurrentPosition(attributes));
    }

    /**
     * Tell ComScore that playback finished for the current content.
     */
    private void trackPlaybackFinished(HashMap<String, Object> attributes) {

        mCurrentContentMetadata = null;
        mCurrentAdMetadata = null;
        mStreamingAnalytics.notifyEnd(getCurrentPosition(attributes));
    }

    /**
     * Get the current position attribute.
     *
     * @param attributes Attributes for the current content.
     * @return The current position attribute, or 0 if it is not defined.
     */
    private long getCurrentPosition(HashMap<String, Object> attributes) {

        if (attributes.containsKey(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION)) {
            return Long.parseLong(
                    String.valueOf(attributes.get(AnalyticsTags.ATTRIBUTE_VIDEO_CURRENT_POSITION)));
        }
        return 0;
    }

    /**
     * Convert the given attributes map types from <String, Object> to <String, String>
     *
     * @param attributes Original attributes map to convert.
     * @return The original attributes map converted to types <String, String>
     */
    private HashMap<String, String> convertMapToStrings(HashMap<String, Object> attributes) {

        HashMap<String, String> result = new HashMap<>();
        for (Map.Entry<String, Object> entry : attributes.entrySet()) {
            result.put(entry.getKey(), String.valueOf(entry.getValue()));
        }
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackState(String screen) {

        Log.d(TAG, "Tracking screen " + screen);
        // User experience started
        if (screen.equals(AnalyticsTags.SCREEN_PLAYBACK)) {
            mOnPlaybackScreen = true;
            Analytics.notifyUxActive();
            return;
        }
        // User experience stopped
        if (!screen.equals(AnalyticsTags.SCREEN_PLAYBACK) && mOnPlaybackScreen) {
            mOnPlaybackScreen = false;
            Analytics.notifyUxInactive();
            return;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {

        Log.e(TAG, errorMessage, t);
        if(mStreamingAnalytics != null) {
            mStreamingAnalytics.notifyEnd(0);
        }
    }
}
