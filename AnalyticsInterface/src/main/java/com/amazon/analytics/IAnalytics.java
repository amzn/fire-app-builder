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
package com.amazon.analytics;

import android.app.Activity;
import android.content.Context;

import java.util.HashMap;

/**
 * The Analytics Interface. This class should be used throughout the
 * application to collect analytics.
 */
public interface IAnalytics {

    /**
     * Major version number.
     */
    int major = 0;

    /**
     * Minor version number.
     */
    int minor = 1;

    /**
     * Configure the analytics framework.
     *
     * @param context The application context.
     */
    void configure(Context context);

    /**
     * Collects activity life cycle data.
     *
     * @param activity The activity to collect lifecycle data on.
     * @param active   True if data collecting should be active; false if collecting should
     *                 be paused.
     */
    void collectLifeCycleData(Activity activity, boolean active);

    /**
     * Tracks an action within the app.
     *
     * @param data Map of Strings to Objects that represent data that is necessary for the tracked
     *             action.
     */
    void trackAction(HashMap<String, Object> data);

    /**
     * Tracks a screen of the app.
     *
     * @param screen The screen that is displayed.
     */
    void trackState(String screen);

    /**
     * Tracks caught exceptions of the app.
     *
     * @param errorMessage The error message corresponding for this error.
     * @param t            The error that needs to be tracked.
     */
    void trackCaughtError(String errorMessage, Throwable t);
}
