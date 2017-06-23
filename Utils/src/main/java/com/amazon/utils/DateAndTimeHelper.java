/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *    http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.utils;

import android.util.Log;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * Utility for Data and time helpers
 */
public class DateAndTimeHelper {

    public static final String TAG = DateAndTimeHelper.class.getSimpleName();

    /**
     * Returns current date.
     *
     * @return The current date.
     */
    public static Date getCurrentDate() {
        //TODO: Find a better way SDK-4263
        return new Date();
    }

    /**
     * Adds seconds to the given date.
     *
     * @param date    The initial date.
     * @param seconds The seconds to add.
     * @return The new date.
     */
    public static Date addSeconds(Date date, int seconds) {

        Calendar cal = Calendar.getInstance();
        cal.setTime(date);
        cal.add(Calendar.SECOND, seconds);
        return cal.getTime();
    }

    /**
     * Compares an old date and new date and returns true if the old date is older than new date.
     *
     * @param oldDate The old date.
     * @param newDate The new date.
     * @return True if the old date is older than the new date; false otherwise.
     */
    public static boolean compareDates(Date oldDate, Date newDate) {

        return oldDate == null || oldDate.before(newDate);
    }

    /**
     * Convert a simple date time with the format "HH:mm:ss.SSS" or "HH:mm:ss" to seconds.
     *
     * @param timeToConvert Time to convert. Expecting format of "HH:mm:ss.SSS" or "HH:mm:ss".
     * @return The converted time in seconds.
     */
    public static double convertDateFormatToSeconds(String timeToConvert) {

        if (timeToConvert == null) {
            return -1;
        }

        try {
            SimpleDateFormat sdf;

            if (timeToConvert.matches("\\d+:\\d{2}:\\d{2}.\\d{3}")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS");
            }
            else if (timeToConvert.matches("\\d+:\\d{2}:\\d{2}")) {
                sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
            }
            else {
                throw new ParseException("Time format does not match expected.", 0);
            }

            double time1 = sdf.parse("1970-01-01 00:00:00.000").getTime() / 1000;

            // if not valid, it will throw ParseException
            Date date = sdf.parse("1970-01-01 " + timeToConvert);
            return (date.getTime() / 1000) - time1;

        }
        catch (ParseException e) {
            Log.e(TAG, "Date to convert is not of a valid date format. Expecting \"HH:mm:ss.SSS\"" +
                    "or \"HH:mm:ss\"");

            return -1;
        }
    }
}
