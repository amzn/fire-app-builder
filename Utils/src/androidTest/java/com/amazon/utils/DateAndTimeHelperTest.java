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

import org.junit.Test;

import static org.junit.Assert.assertTrue;

/**
 * Tests the {@link DateAndTimeHelper} class.
 */
public class DateAndTimeHelperTest {

    /**
     * Tests the {@link DateAndTimeHelper#convertDateFormatToSeconds(String)} method.
     */
    @Test
    public void testConvertTimeOffsetFromDateFormat() throws Exception {

        assertTrue(60 == DateAndTimeHelper.convertDateFormatToSeconds("00:01:00.000"));
        assertTrue(0 == DateAndTimeHelper.convertDateFormatToSeconds("00:00:00.000"));
        assertTrue(10 == DateAndTimeHelper.convertDateFormatToSeconds("00:00:10.000"));
        assertTrue(100 == DateAndTimeHelper.convertDateFormatToSeconds("00:01:40.000"));
        assertTrue(0 == DateAndTimeHelper.convertDateFormatToSeconds("00:00:00.100"));
        assertTrue(36000 == DateAndTimeHelper.convertDateFormatToSeconds("10:00:00.000"));
        assertTrue(60 == DateAndTimeHelper.convertDateFormatToSeconds("00:01:00"));
        assertTrue(-1 == DateAndTimeHelper.convertDateFormatToSeconds("00:00:0000"));
        assertTrue(360000 == DateAndTimeHelper.convertDateFormatToSeconds("100:00:00"));
    }
}
