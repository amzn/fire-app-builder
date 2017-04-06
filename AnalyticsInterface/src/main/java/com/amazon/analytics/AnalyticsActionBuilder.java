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


import android.content.Context;
import android.os.Build;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.Locale;
import java.util.TimeZone;

/**
 * This is a helper class to construct the data required for specific actions tracked by the
 * analytics interface.
 */
public class AnalyticsActionBuilder {

    /**
     * Builds the data object for tracking app init actions.
     *
     * @param context The application context.
     * @return A map of data key/values that are needed for tracking the app init action.
     */
    public static HashMap<String, Object> buildInitActionData(Context context) {

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> attributes = new HashMap<>();

        // Add the action name to the data map.
        data.put(AnalyticsTags.ACTION_NAME, AnalyticsTags.ACTION_START_APP);

        // Get application name
        attributes.put(AnalyticsTags.ATTRIBUTE_APP_NAME,
                       context.getApplicationInfo().loadLabel(context.getPackageManager()));

        // Save the platform that the app is running on.
        attributes.put(AnalyticsTags.ATTRIBUTE_PLATFORM, Build.MODEL);

        // Add the gathered attributes to the data map.
        data.put(AnalyticsTags.ATTRIBUTES, attributes);

        return data;
    }

    /**
     * Builds the data object for tracking time and date. This map is to be added to the
     * attributes data map for actions or states.
     *
     * @return A map of data key/values that are needed for tracking the date and time.
     */
    public static HashMap<String, Object> buildTimeDateData() {

        HashMap<String, Object> data = new HashMap<>();

        // Get the Date
        Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

        // Save the minute the app was opened (as close as we can)
        data.put(AnalyticsTags.ATTRIBUTE_MINUTE,
                 calendar.get(Calendar.MINUTE));

        // Save the hour the app was opened
        data.put(AnalyticsTags.ATTRIBUTE_HOUR,
                 calendar.get(Calendar.HOUR_OF_DAY));

        // Save the day the app was opened.
        data.put(AnalyticsTags.ATTRIBUTE_DAY,
                 calendar.getDisplayName(Calendar.DAY_OF_WEEK, Calendar.LONG, Locale.getDefault()));

        // Save the day of the week the app was opened.
        SimpleDateFormat date = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        data.put(AnalyticsTags.ATTRIBUTE_DATE, date.format(new Date()));

        return data;
    }

    /**
     * Builds the data object for tracking search actions.
     *
     * @param searchQuery The query the user searched for.
     * @return A map of data key/values that are needed for tracking the search action.
     */
    public static HashMap<String, Object> buildSearchActionData(String searchQuery) {

        HashMap<String, Object> data = new HashMap<>();
        HashMap<String, Object> attributes = new HashMap<>();

        // Add the action name to the data map.
        data.put(AnalyticsTags.ACTION_NAME, AnalyticsTags.ACTION_SEARCH);

        // Add the search query to the attributes.
        attributes.put(AnalyticsTags.ATTRIBUTE_SEARCH_TERM, searchQuery);

        // Add the gathered attributes to the data map.
        data.put(AnalyticsTags.ATTRIBUTES, attributes);

        return data;
    }
}

