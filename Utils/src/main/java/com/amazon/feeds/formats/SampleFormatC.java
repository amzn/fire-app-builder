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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the format for the sample feed.
 */
public class SampleFormatC implements IFeedFormat {

    private final String PROVIDER = "Provider C";
    private int currentItemID;
    private List<Item> items;

    public SampleFormatC() {
        init();
    }

    /**
     * Get data from list of sample items.
     * @return List of feed items.
     */
    @JsonValue
    public List<Item> getItems() {
        return items;
    }

    /**
     * Initializes object.
     */
    @Override
    public void init() {
        currentItemID = 0;
        items = new ArrayList<>();
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
     * This particular format doesn't use pretty print.
     * @return false
     */
    @Override
    public boolean usePrettyPrint() {
        return false;
    }

    /**
     * Character escape rules for sample feed.
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
     * Populate sample feed with dummy items.
     *
     * @param size The size of the feed.
     */
    @Override
    public void populate(int size) {
        for (int i = 0; i < size; i++) {
            currentItemID++;
            items.add(new Item());
        }
    }

    /**
     * Format specification for feed item.
     */
    private class Item {
        public String id;
        public String title;
        public String description;
        public String duration;
        public String thumbURL;
        public String imgURL;
        public String videoURL;
        public String[] categories;
        public String channel_id;

        public Item() {
            id = Integer.toString(currentItemID);
            title = "Sample video " + currentItemID;
            description = SampleFeedData.SAMPLE_DESCRIPTIION;
            thumbURL = SampleFeedData.sampleImage(460);
            imgURL = thumbURL;
            videoURL = SampleFeedData.SAMPLE_VIDEO;
            categories = new String[] {"Sample string"};
            channel_id = "1";
        }
    }
}
