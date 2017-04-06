/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */

package com.amazon.feeds;

/**
 * This class contains sample data for use in the feed generator. In the event that a resource
 * becomes unavailable, it can be easily replaced.
 */
public class SampleFeedData {

    /**
     * Generates a URL to an example image with a 4:3 aspect ratio.
     *
     * @param width Width of image.
     * @return URL to example image.
     */
    public static String sampleImage(int width) {
        // base image: https://commons.wikimedia.org/wiki/File:Aspect_ratio_-_4x3.svg
        return "https://upload.wikimedia.org/wikipedia/commons/thumb/a/a2/Aspect_ratio_-_4x3" +
                ".svg/" + width + "px-Aspect_ratio_-_4x3.svg.png";
    }

    /**
     * URL to sample video - should be 16:9 if possible.
     */
    public static final String SAMPLE_VIDEO = "https://devimages.apple.com.edgekey" +
            ".net/streaming/examples/bipbop_16x9/bipbop_16x9_variant.m3u8";

    /**
     * URL to sample video description page.
     */
    public static final String SAMPLE_VIDEO_PAGE = "https://developer.apple.com/streaming/examples";

    /**
     * Generic sample URL.
     */
    public static final String SAMPLE_URL = "http://example.com";

    /**
     * Sample description.
     */
    public static final String SAMPLE_DESCRIPTIION = "Lorem ipsum dolor sit amet, consectetur adipiscing" +
            " elit, sed do eiusmod tempor incididunt ut labore et dolore magna aliqua.";
}
