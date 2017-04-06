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

package com.amazon.feeds.formats;

import com.amazon.feeds.IFeedFormat;
import com.amazon.feeds.SampleFeedData;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the format for the sample feed.
 */
public class SampleFormatA implements IFeedFormat {

    private final String PROVIDER = "Provider A";
    private int currentItemID;

    public String total;
    public List<Item> results;

    /**
     * Constructor.
     */
    public SampleFormatA() {
        init();
    }

    /**
     * Reset object.
     */
    @Override
    public void init() {
        total = "0";
        currentItemID = 0;
        results = new ArrayList<>();
    }

    /**
     * Get the name of the feed format.
     *
     * @return The feed format.
     */
    @JsonIgnore
    @Override
    public String getFeedFormat() {

        return this.getClass().getSimpleName();
    }

    /**
     * Get the name of the provider.
     *
     * @return The content provider.
     */
    @JsonIgnore
    @Override
    public String getProvider() {

        return PROVIDER;
    }

    /**
     * Populate sample feed with dummy items.
     *
     * @param size The size of the feed.
     */
    @Override
    public void populate(int size) {

        for (int i = 0; i < size; i++) {
            currentItemID++;
            Item item = new Item();
            results.add(item);
        }
        total = Integer.toString(currentItemID);
    }

    /**
     * This particular format doesn't use pretty print.
     * @return false
     */
    @Override
    public boolean usePrettyPrint() {
        return false;
    }

    /**
     * This feed serializes forward slashes.
     * @return CharacterEscapes object with escape rules.
     */
    @JsonIgnore
    @Override
    public CharacterEscapes getEscapeRules() {

        return new CharacterEscapes() {

            @Override
            public int[] getEscapeCodesForAscii() {

                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['/'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {

                switch (ch) {
                    case '/':
                        return new SerializedString("\\/");
                }
                return null;
            }
        };
    }

    /**
     * Format specification for feed item.
     */
    private class Item {

        public String id;
        public String title;
        public String link;
        public String pubDate;
        @JsonProperty("media:thumbnail")
        public String thumbnail;
        @JsonProperty("media:duration")
        public String duration;
        public String description;
        @JsonProperty("media:filepath")
        public String filepath;
        @JsonProperty("media:rtmp")
        public String rtmp;
        @JsonProperty("media:fullimage")
        public String fullimage;
        @JsonProperty("media:keywords")
        public String keywords;

        public Item() {

            id = Integer.toString(currentItemID);
            title = "Sample item " + currentItemID;
            link = SampleFeedData.SAMPLE_VIDEO_PAGE;
            pubDate = "Fri, 28 Oct 2016 19:59:03 +0530";
            thumbnail = SampleFeedData.sampleImage(160);
            duration = Integer.toString(10);
            description = SampleFeedData.SAMPLE_DESCRIPTIION;
            filepath = SampleFeedData.SAMPLE_VIDEO;
            rtmp = Integer.toString(0);
            fullimage = SampleFeedData.sampleImage(640);
            keywords = "Lorem,ipsum";
        }
    }
}
