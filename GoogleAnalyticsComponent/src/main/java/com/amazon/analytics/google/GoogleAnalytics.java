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

import com.google.android.gms.analytics.HitBuilders;
import com.google.android.gms.analytics.Tracker;

import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.CustomAnalyticsTags;
import com.amazon.analytics.IAnalytics;

import android.app.Activity;
import android.content.Context;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

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

    private CustomAnalyticsTags mCustomDimensionTags = new CustomAnalyticsTags();
    private CustomAnalyticsTags mCustomMetricTags = new CustomAnalyticsTags();

    /**
     * HashMap for storing mEvents.
     */
    private Map<String, String> mEvents;

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

        mEvents = null;

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
        mCustomDimensionTags.init(context, R.string.google_analytics_custom_dimension_tags);
        mCustomMetricTags.init(context, R.string.google_analytics_custom_metric_tags);

        Log.d(TAG, "Google analytics configuration complete.");
    }

    /**
     * Tests whether or not the given key has been configured to a custom dimension/metric value.
     *
     * @param key The key to test.
     * @return True if a configuration exists; false otherwise.
     */
    private boolean isKeyConfigured(String key) {

        return mCustomDimensionTags.tagCustomized(key) || mCustomMetricTags.tagCustomized(key);
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
        String action = String.valueOf(data.get(AnalyticsTags.ACTION_NAME));

        HitBuilders.EventBuilder eventBuilder = new HitBuilders.EventBuilder();
        eventBuilder.setAction(action);

        // Get the attributes map.
        Map<String, Object> contextDataObjectMap =
                (Map<String, Object>) data.get(AnalyticsTags.ATTRIBUTES);

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
                        if (mCustomDimensionTags.tagCustomized(key)) {

                            eventBuilder.setCustomDimension(
                                    Integer.valueOf(mCustomDimensionTags.getCustomTag(key)), value);
                        }
                        // If the key is a metric, add a custom metric value
                        else if (mCustomMetricTags.tagCustomized(key)) {

                            int intValue = Integer.parseInt(value);
                            eventBuilder.setCustomMetric(
                                    Integer.valueOf(mCustomMetricTags.getCustomTag(key)), intValue);
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
        mEvents = eventBuilder.build();
        tracker.send(mEvents);

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

    /**
     * Get the data sent to the Google Analytics service.
     * @return HashMap containing the analytics data.
     */
    @VisibleForTesting
    public Map<String, String> getEvents() {

        return mEvents;
    }
}
