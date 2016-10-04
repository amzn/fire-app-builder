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
package com.amazon.analytics.omniture;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import com.adobe.mobile.Analytics;
import com.adobe.mobile.Config;
import com.amazon.analytics.AnalyticsActionBuilder;
import com.amazon.analytics.AnalyticsConstants;
import com.amazon.analytics.IAnalytics;

import java.util.HashMap;


/**
 * An analytics implementation using the Adobe Omniture Analytics framework.
 */
public class OmnitureAnalytics implements IAnalytics {

    private static final String TAG = OmnitureAnalytics.class.getSimpleName();

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    static final String IMPL_CREATOR_NAME = OmnitureAnalytics.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {

        Config.setContext(context);
        Log.d(TAG, "Omniture configuration complete");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        if (active) {
            Config.collectLifecycleData(activity);
        }
        else {
            Config.pauseCollectingLifecycleData();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {

        if (data.get(AnalyticsConstants.ACTION_NAME) != null
                && data.get(AnalyticsConstants.ATTRIBUTES) != null) {

            // Get the action name.
            String action = data.get(AnalyticsConstants.ACTION_NAME).toString();

            // Get the attributes map
            HashMap<String, Object> contextData =
                    (HashMap<String, Object>) data.get(AnalyticsConstants.ATTRIBUTES);

            // Add time and data attributes
            contextData.putAll(AnalyticsActionBuilder.buildTimeDateData());

            // Record action
            Analytics.trackAction(action, contextData);

            Log.d(TAG, "Tracking action " + action + " with context data: " + contextData
                    .toString());
        }
        else {
            Log.e(TAG, "Error tracking action. Data map not set properly.");
        }

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackState(String screen) {

        HashMap<String, Object> data = AnalyticsActionBuilder.buildTimeDateData();
        Log.d(TAG, "Tracking state for screen " + screen + " with context data: " + data.toString
                ());
        Analytics.trackState(screen, data);
    }

    /**
     * {@inheritDoc}
     * NOTE: Omniture does not provide specific method for tracking caught exceptions, so the
     * dashboard will not show the complete exception trace.
     */
    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {

        Log.d(TAG, "Tracking a caught error.");

        HashMap<String, Object> data = new HashMap<>();
        data.put(AnalyticsConstants.ACTION_NAME, AnalyticsConstants.ACTION_ERROR);

        HashMap<String, Object> attributes = new HashMap<>();
        attributes.put(AnalyticsConstants.ATTRIBUTE_ERROR_MSG, errorMessage +
                t.getLocalizedMessage());
        data.put(AnalyticsConstants.ATTRIBUTES, attributes);

        trackAction(data);
    }
}
