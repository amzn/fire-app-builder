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

package com.amazon.feeds;

import com.amazon.feeds.formats.*;

/**
 * Uses the {@link SampleFeedGenerator} class to generate samples feeds.
 */
public class SampleFeedGeneratorRunner {

    private static final int[] SIZES = {10, 100, 500, 5000};
    private static final IFeedFormat[] FORMATS = {
            new SampleFormatA(),
            new SampleFormatB(),
            new SampleFormatC(),
            new SampleFormatD(),
            new SampleFormatE()
    };

    public static void main(String[] args) throws Exception {
        makeFeeds(SIZES, FORMATS);
    }

    /**
     * Generates sample feeds given arrays of sizes and formats.
     */
    public static void makeFeeds(int[] sizes, IFeedFormat[] formats) throws Exception {
        SampleFeedGenerator sfg = new SampleFeedGenerator();
        for (IFeedFormat f : formats) {
            for (int s : sizes) {
                // reset object so previous data does not get added to the next set
                f.init();
                sfg.createSampleFeed(f, s);
            }
        }
    }
}