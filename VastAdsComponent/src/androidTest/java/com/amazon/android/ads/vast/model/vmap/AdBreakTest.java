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
package com.amazon.android.ads.vast.model.vmap;

import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Tests the {@link AdBreak} class.
 */
public class AdBreakTest {

    private AdBreak mAdBreak;

    @Before
    public void setUp() {

        mAdBreak = new AdBreak();
        mAdBreak.setTimeOffset("end");
    }

    @Test
    public void getTimeOffset() throws Exception {

        assertEquals("end", mAdBreak.getTimeOffset());
    }

    @Test
    public void setTimeOffset() throws Exception {

        mAdBreak.setTimeOffset("start");
        assertEquals("start", mAdBreak.getTimeOffset());
    }

    @Test
    public void getConvertedTimeOffset() throws Exception {

        assertTrue(-1 == mAdBreak.getConvertedTimeOffset());
        mAdBreak.setTimeOffset("00:00:15.000");
        assertTrue(15 == mAdBreak.getConvertedTimeOffset());
    }
}
