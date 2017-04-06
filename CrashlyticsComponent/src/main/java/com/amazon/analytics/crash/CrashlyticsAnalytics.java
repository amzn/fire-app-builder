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

import com.amazon.analytics.CustomAnalyticsTags;
import com.crashlytics.android.Crashlytics;

import com.amazon.analytics.AnalyticsTags;
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

    private CustomAnalyticsTags mCustomTags = new CustomAnalyticsTags();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {

        Fabric.with(context, new Crashlytics());

        mCustomTags.init(context, R.string.crashlytics_analytics_custom_tags);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        Crashlytics.log("Collecting life cycle data for activity: " + activity.toString() +
                                ", active:" + active);
        Crashlytics.setBool(activity.toString(), active);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {

        String action = (String) data.get(AnalyticsTags.ACTION_NAME);
        Crashlytics.setString(AnalyticsTags.ACTION_NAME, mCustomTags.getCustomTag(action));

        try {
            Map<String, Object> contextDataObjectMap =
                    (Map<String, Object>) data.get(AnalyticsTags.ATTRIBUTES);

            for (String key : contextDataObjectMap.keySet()) {
                Crashlytics.setString(mCustomTags.getCustomTag(key),
                        String.valueOf(contextDataObjectMap.get(key)));
            }
        }
        catch (Exception e) {
            Log.e(TAG, "The params map was not of type <String, String> for action " + action +
                    ", dropping the map and just logging the event", e);
            // Record action.
            Crashlytics.setString(AnalyticsTags.ACTION_NAME, mCustomTags.getCustomTag(action));
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
