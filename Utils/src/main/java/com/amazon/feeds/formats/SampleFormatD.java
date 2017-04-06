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
import com.fasterxml.jackson.annotation.JsonValue;
import com.fasterxml.jackson.core.io.CharacterEscapes;

import java.util.ArrayList;
import java.util.List;

/**
 * This class specifies the format for the feed.
 */
public class SampleFormatD implements IFeedFormat {

    private final String PROVIDER = "Provider D";
    private int currentItemID;
    private List<Item> items;

    /**
     * Constructor.
     */
    public SampleFormatD() {
        init();
    }

    /**
     * Initializes object.
     */
    @Override
    public void init() {
        items = new ArrayList<>();
        currentItemID = 0;
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
     * Character escape rules for samplr feed.
     * @return CharacterEscapes object with escape rules.
     */
    @JsonIgnore
    @Override
    public CharacterEscapes getEscapeRules() {
        return null;
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
     * Use pretty print.
     * @return true
     */
    @Override
    public boolean usePrettyPrint() {
        return true;
    }

    /**
     * Get data from list of items.
     * @return List of items.
     */
    @JsonValue
    public List<Item> getItems() {
        return items;
    }

    /**
     * Format specification for feed item.
     */
    private class Item {
        @JsonProperty("MediaID")
        public String mediaID;
        @JsonProperty("MediaName")
        public String mediaName;
        @JsonProperty("MediaTypeID")
        public String mediaTypeID;
        @JsonProperty("MediaTypeName")
        public String mediaTypeName;
        @JsonProperty("Rating")
        public double rating;
        @JsonProperty("ViewCounter")
        public int viewCounter;
        @JsonProperty("Description")
        public String description;
        @JsonProperty("CreationDate")
        public String creationDate;
        @JsonProperty("LastWatchDate")
        public String lastWatchDate;
        @JsonProperty("StartDate")
        public String startDate;
        @JsonProperty("CatalogStartDate")
        public String catalogStartDate;
        @JsonProperty("PicURL")
        public String picURL;
        @JsonProperty("BackgroundURL")
        public String backgroundURL;
        @JsonProperty("URL")
        public String url;
        @JsonProperty("MediaWebLink")
        public String mediaWebLink;
        @JsonProperty("Duration")
        public String duration;
        @JsonProperty("FileID")
        public String fileID;
        @JsonProperty("MediaDynamicData")
        public String mediaDynamicData;
        @JsonProperty("SubDuration")
        public String subDuration;
        @JsonProperty("SubFileFormat")
        public String subFileFormat;
        @JsonProperty("SubFileID")
        public String subFileID;
        @JsonProperty("SubURL")
        public String subURL;
        @JsonProperty("GeoBlock")
        public String geoBlock;
        @JsonProperty("TotalItems")
        public int totalItems;
        @JsonProperty("like_counter")
        public int likes;
        @JsonProperty("EntryID")
        public String entryID;
        @JsonProperty("category")
        public String category;

        public Item() {
            mediaID = Integer.toString(currentItemID);
            mediaName = "Sample item " + currentItemID;
            mediaTypeID = "1";
            mediaTypeName = "Episode";
            rating = 0.0;
            viewCounter = 1;
            description = SampleFeedData.SAMPLE_DESCRIPTIION;
            creationDate = "1970-01-01T00:00:00";
            lastWatchDate = null;
            startDate = "1970-01-01T00:00:00";
            catalogStartDate = "1970-01-01T00:00:00";
            picURL = SampleFeedData.sampleImage(768);
            url = SampleFeedData.SAMPLE_VIDEO;
            backgroundURL = SampleFeedData.sampleImage(1280);
            mediaWebLink = "";
            duration = Integer.toString(10);
            fileID = "1";
            mediaDynamicData = null;
            subDuration = null;
            subFileFormat = null;
            subFileID = null;
            subURL = null;
            geoBlock = null;
            totalItems = 1;
            likes = 1;
            entryID = "Lorem_ipsum";
            category = "Reality";
        }
    }

}
