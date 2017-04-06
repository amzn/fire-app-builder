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
import com.fasterxml.jackson.core.io.CharacterEscapes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the format for the sample feed.
 */
public class SampleFormatE implements IFeedFormat {

    private final String PROVIDER = "Provider E";
    private int currentItemID;
    private List<Item> items;

    /**
     * Constructor.
     */
    public SampleFormatE() {
        init();
    }

    /**
     * Get ArrayList of items.
     * @return List of items.
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
     * Character escape rules for sample feed.
     * @return CharacterEscapes object with escape rules.
     */
    @JsonIgnore
    @Override
    public CharacterEscapes getEscapeRules() {

        return null;
    }

    /**
     * THis particular feed doesn't use pretty print.
     * @return false
     */
    @Override
    public boolean usePrettyPrint() {

        return false;
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
        public String channel_id;
        public String[] categories;

        public Item() {
            id = Integer.toString(currentItemID);
            title = "Sample item " + currentItemID;
            description = SampleFeedData.SAMPLE_DESCRIPTIION;
            duration = "0";
            thumbURL = SampleFeedData.sampleImage(640);
            imgURL = SampleFeedData.sampleImage(320);
            videoURL = SampleFeedData.SAMPLE_VIDEO;
            channel_id = "200";
            categories = new String[] {"Latest Live"};
        }
    }
}
