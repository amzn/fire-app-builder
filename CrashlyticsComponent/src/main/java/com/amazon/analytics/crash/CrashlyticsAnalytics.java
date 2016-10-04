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
package com.amazon.analytics.crash;

import android.app.Activity;
import android.content.Context;
import android.util.Log;

import java.util.HashMap;
import java.util.Map;

import com.crashlytics.android.Crashlytics;

import com.amazon.analytics.AnalyticsConstants;
import io.fabric.sdk.android.Fabric;
import com.amazon.analytics.IAnalytics;

/**
 * An analytics implementation using the
 * <a href="https://docs.fabric.io/android/crashlytics/index.html">Crashlytics framework</a>.
 * NOTE: It is currently not integrated with proguard or dexguard, hence the reports generated
 * will be obfuscated.
 */
public class CrashlyticsAnalytics implements IAnalytics {

    /**
     * Name used for implementation creator registration to the Module Manager.
     */
    public static final String IMPL_CREATOR_NAME = CrashlyticsAnalytics.class.getSimpleName();

    /**
     * Debug tag.
     */
    private static final String TAG = CrashlyticsAnalytics.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {

        Fabric.with(context, new Crashlytics());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        Crashlytics.log("Collecting life cycle data for activity: " + activity.toString() + ", " +
                                "active:" +
                                " " + active);
        Crashlytics.setBool(activity.toString(), active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {

        String action = (String) data.get(AnalyticsConstants.ACTION_NAME);
        Crashlytics.setString(AnalyticsConstants.ACTION_NAME, action);

        try {
            Map<String, Object> contextDataObjectMap = (Map<String, Object>) data.get
                    (AnalyticsConstants.ATTRIBUTES);
            for (String key : contextDataObjectMap.keySet()) {
                Crashlytics.setString(key, String.valueOf(contextDataObjectMap.get(key)));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "The params map was not of type <String, String> for action " + action +
                    ", dropping the map and just logging the event", e);
            // Record action.
            Crashlytics.setString(AnalyticsConstants.ACTION_NAME, action);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackState(String screen) {

        Crashlytics.log("Tracking screen " + screen);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {

        Crashlytics.logException(t);
    }

}
