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

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Tests the {@link VmapResponse} class.
 */
public class VmapResponseTest {

    VmapResponse mVmapResponse;

    @Before
    public void setUp() {

        mVmapResponse = new VmapResponse();

        List<AdBreak> adBreaks = new ArrayList<>();
        // pre-roll test values
        adBreaks.add(new AdBreak().setTimeOffset("start"));
        adBreaks.add(new AdBreak().setTimeOffset("0%"));
        adBreaks.add(new AdBreak().setTimeOffset("00:00:00"));
        adBreaks.add(new AdBreak().setTimeOffset("00:00:00.000"));
        // mid-roll test values
        adBreaks.add(new AdBreak().setTimeOffset("25%"));
        adBreaks.add(new AdBreak().setTimeOffset("50%"));
        adBreaks.add(new AdBreak().setTimeOffset("90%"));
        adBreaks.add(new AdBreak().setTimeOffset("1:40:00")); // 6000 seconds
        adBreaks.add(new AdBreak().setTimeOffset("0:00:10.100"));
        adBreaks.add(new AdBreak().setTimeOffset("5%"));
        // post-roll test values
        adBreaks.add(new AdBreak().setTimeOffset("20:10:00.000")); // 72600 seconds
        adBreaks.add(new AdBreak().setTimeOffset("100%"));
        adBreaks.add(new AdBreak().setTimeOffset("end"));

        mVmapResponse.setAdBreaks(adBreaks);
    }

    /**
     * Tests the {@link VmapResponse#getPreRollAdBreaks(long)} method.
     */
    @Test
    public void testGetPreRollAdBreaks() throws Exception {

        assertEquals(4, mVmapResponse.getPreRollAdBreaks(0).size());
        assertEquals(4, mVmapResponse.getPreRollAdBreaks(60).size()); // 1 minute duration
        assertEquals(4, mVmapResponse.getPreRollAdBreaks(72600).size()); // 20 hour & 10 min
    }

    /**
     * Tests the {@link VmapResponse#getMidRollAdBreaks(long)} method.
     */
    @Test
    public void testGetMidRollAdBreaks() throws Exception {

        assertEquals(0, mVmapResponse.getMidRollAdBreaks(0).size());
        assertEquals(5, mVmapResponse.getMidRollAdBreaks(60).size()); // 1 minute duration
        assertEquals(6, mVmapResponse.getMidRollAdBreaks(72600).size()); // 20 hour & 10 min
    }

    /**
     * Tests the {@link VmapResponse#getPostRollAdBreaks(long)} method.
     */
    @Test
    public void testGetPostRollAdBreaks() throws Exception {

        assertEquals(0, mVmapResponse.getPostRollAdBreaks(0).size());
        assertEquals(2, mVmapResponse.getPostRollAdBreaks(60).size()); // 1 minute duration
        assertEquals(3, mVmapResponse.getPostRollAdBreaks(72600).size()); // 20 hour & 10 min
    }
}
