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
package com.amazon.analytics.google;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.amazon.analytics.AnalyticsConstants;
import com.amazon.analytics.IAnalytics;

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

/**
 * An analytics implementation using the
 * <a href="https://developers.google.com/analytics/devguides/collection/android/v4/">Google
 * Analytics framework</a>.
 *
 * NOTE: Google Analytics does not officially support apps that run on FireOS devices. Integrating
 * without at least the google play services analytics com.google.android
 * .gms:play-services-analytics is considered a non-standard integration.
 */
public class GoogleAnalytics implements IAnalytics {

    /**
     * Debug tag.
     */
    private static final String TAG = GoogleAnalytics.class.getSimpleName();

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    static final String IMPL_CREATOR_NAME = GoogleAnalytics.class.getSimpleName();

    /**
     * The Google Analytics tracker.
     */
    private Tracker mTracker;

    /**
     * Map of custom dimension names to index.
     */
    private Map<String, Integer> mDimensionIndexMap;

    /**
     * Map of custom metric names to index.
     */
    private Map<String, Integer> mMetricIndexMap;

    /**
     * Custom dimension indexes.
     */
    private final int PLATFORM_IDX = 1;
    private final int SEARCH_IDX = 2;
    private final int ERROR_MSG_IDX = 10;
    private final int PLAYBACK_SOURCE_IDX = 3;
    private final int PURCHASE_RESULT_IDX = 8;
    private final int PURCHASE_SKU_IDX = 9;
    private final int TITLE_IDX = 4;
    private final int SUBTITLE_IDX = 5;
    private final int VIDEO_TYPE_IDX = 6;
    private final int PURCHASE_TYPE_IDX = 7;

    /**
     * Custom metric indexes.
     */
    private final int AD_SECONDS_WATCHED_IDX = 1;
    private final int VIDEO_SECONDS_WATCHED_IDX = 2;
    private final int VIDEO_ID_IDX = 3;
    private final int AD_ID_IDX = 4;

    /**
     * Gets the default {@link Tracker}.
     *
     * @return tracker
     */
    synchronized private Tracker getDefaultTracker() {

        return mTracker;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {

        if (mTracker == null) {
            com.google.android.gms.analytics.GoogleAnalytics analytics = com.google.android.gms
                    .analytics.GoogleAnalytics.getInstance(context);

            // Set the dispatch period in seconds. Needed for non-Google Play devices.
            analytics.setLocalDispatchPeriod(context.getResources()
                                                    .getInteger(R.integer.ga_dispatchPeriod));

            // To enable debug logging use: adb shell setprop log.tag.GAv4 DEBUG
            mTracker = analytics.newTracker(R.xml.global_tracker);
        }

        // Create map of analytics constants to custom Google Analytics indexes.
        initializeIndexMap();

        Log.d(TAG, "Google analytics configuration complete.");
    }

    /**
     * Initializes the maps that hold the mappings between the strings from {@link
     * AnalyticsConstants} to the index values assigned to the custom dimensions and metrics within
     * the Google Analytics dashboard.
     */
    private void initializeIndexMap() {

        mDimensionIndexMap = new HashMap<>();
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_PLATFORM, PLATFORM_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_SEARCH_TERM, SEARCH_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_PLAY_SOURCE, PLAYBACK_SOURCE_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_ERROR_MSG, ERROR_MSG_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_TYPE, PURCHASE_TYPE_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_RESULT, PURCHASE_RESULT_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_PURCHASE_SKU, PURCHASE_SKU_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_TITLE, TITLE_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_SUBTITLE, SUBTITLE_IDX);
        mDimensionIndexMap.put(AnalyticsConstants.ATTRIBUTE_VIDEO_TYPE, VIDEO_TYPE_IDX);

        mMetricIndexMap = new HashMap<>();
        mMetricIndexMap.put(AnalyticsConstants.ATTRIBUTE_AD_SECONDS_WATCHED,
                            AD_SECONDS_WATCHED_IDX);
        mMetricIndexMap.put(AnalyticsConstants.ATTRIBUTE_VIDEO_SECONDS_WATCHED,
                            VIDEO_SECONDS_WATCHED_IDX);
        mMetricIndexMap.put(AnalyticsConstants.ATTRIBUTE_VIDEO_ID, VIDEO_ID_IDX);
        mMetricIndexMap.put(AnalyticsConstants.ATTRIBUTE_AD_ID, AD_ID_IDX);

    }

    /**
     * Tests whether or not the given key has been configured to a custom dimension/metric value.
     *
     * @param key The key to test.
     * @return True if a configuration exists; false otherwise.
     */
    private boolean isKeyConfigured(String key) {

        return mDimensionIndexMap.containsKey(key) || mMetricIndexMap.containsKey(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        Log.d(TAG, "Collecting lifecycle data not defined for Google Analytics");

    }

    /**
     * {@inheritDoc}
     *
     * Any attributes that are to be sent along with the action need to be configured properly.
     * Firstly they need to be created on the Google Analytics website; here you will find the
     * index that is associated to the custom variable. Next the custom variable needs to be added
     * to the {@code mMetricsIndexMap} or the {@code mDimensionIndexMap}. If the variable isn't
     * configured in one of those maps, the track action call will ignore the variable.
     */
    @SuppressWarnings("unchecked")
    @Override
    public void trackAction(HashMap<String, Object> data) {

        // Get tracker.
        Tracker tracker = getDefaultTracker();

        // Get the action name.
        String action = String.valueOf(data.get(AnalyticsConstants.ACTION_NAME));

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction(action);

        // Get the attributes map.
        Map<String, Object> contextDataObjectMap =
                (Map<String, Object>) data.get(AnalyticsConstants.ATTRIBUTES);

        if (contextDataObjectMap != null) {
            Log.d(TAG, "Tracking action " + action + " with attributes: "
                    + contextDataObjectMap.toString());

            // Record the action attribute values
            for (String key : contextDataObjectMap.keySet()) {

                // Check that the key is configured.
                if (isKeyConfigured(key)) {

                    String value = String.valueOf(contextDataObjectMap.get(key));

                    // Check that we don't send an empty value.
                    if (value != null && value.length() > 0) {

                        // If the key is a dimension, add a custom dimension value
                        if (mDimensionIndexMap.containsKey(key)) {

                            eventBuilder.setCustomDimension(mDimensionIndexMap.get(key), value);
                        }
                        // If the key is a metric, add a custom metric value
                        else if (mMetricIndexMap.containsKey(key)) {

                            int intValue = Integer.parseInt(value);
                            eventBuilder.setCustomMetric(mMetricIndexMap.get(key), intValue);
                        }
                    }
                    else {
                        Log.w(TAG, "Tried sending an empty value for " + key + " tracking point");
                    }
                }
                else {
                    Log.w(TAG, "Attribute key not configured as a custom dimension or metric: " +
                            key);

                }
            }
        }

        // Record action
        tracker.send(eventBuilder.build());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackState(String screen) {

        Log.d(TAG, "Tracking state for screen " + screen);
        mTracker.setScreenName(screen);
        mTracker.send(new HitBuilders.ScreenViewBuilder().build());

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {

        Log.d(TAG, "Tracking a caught error: " + errorMessage);
        Tracker tracker = getDefaultTracker();
        tracker.send(new HitBuilders.ExceptionBuilder().setDescription(errorMessage).build());
    }
}
