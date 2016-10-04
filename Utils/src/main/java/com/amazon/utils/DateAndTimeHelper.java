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
package com.amazon.utils;

import java.util.Calendar;
import java.util.Date;

/**
 * Utility for Data and time helpers
 */
public class DateAndTimeHelper {

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
}
