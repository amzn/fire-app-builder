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
import com.fasterxml.jackson.core.SerializableString;
import com.fasterxml.jackson.core.io.CharacterEscapes;
import com.fasterxml.jackson.core.io.SerializedString;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * This class specifies the format for the sample feed.
 */
public class SampleFormatB implements IFeedFormat {

    private final String PROVIDER = "Provider B";
    private int currentItemID;
    private int currentContainerID;
    private final int EXTRA_CATEGORIES_TO_GENERATE = 4;

    public String brand;
    public String version;
    public long creationDate;
    public List<Asset> assets;
    public List<Container> containers;

    /**
     * Constructor.
     */
    public SampleFormatB() {
        init();
    }

    /**
     * Initializes object.
     */
    @Override
    public void init() {
        currentItemID = 0;
        currentContainerID = 1;
        brand = "ChannelB";
        version = "1.0";
        creationDate = 1477683069;
        assets = new ArrayList<>();
        containers = new ArrayList<>();
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
            Asset asset = new Asset();
            assets.add(asset);
        }

        // add category with maximum items
        Container categoryMaxItems = new Container(size);
        containers.add(categoryMaxItems);
        currentContainerID++;
        // add category with a single item
        Container categorySingleItem = new Container(1);
        containers.add(categorySingleItem);
        currentContainerID++;
        // add categories with a random number of items
        for (int a = 0; a < EXTRA_CATEGORIES_TO_GENERATE; a++) {
            containers.add(new Container(1 + (int) Math.floor((size - 1) *  Math.random())));
            currentContainerID++;
        }
    }

    /**
     * This format doesn't use pretty print.
     * @return false
     */
    @Override
    public boolean usePrettyPrint() {
        return false;
    }

    /**
     * Escape rules for sample format.
     * @return CharacterEscapes object with escape rules.
     */
    @JsonIgnore
    @Override
    public CharacterEscapes getEscapeRules() {

        return new CharacterEscapes() {

            @Override
            public int[] getEscapeCodesForAscii() {

                int[] escapes = CharacterEscapes.standardAsciiEscapesForJSON();
                escapes['\\'] = CharacterEscapes.ESCAPE_CUSTOM;
                return escapes;
            }

            @Override
            public SerializableString getEscapeSequence(int ch) {

                switch (ch) {
                    case '\\':
                        return new SerializedString("\\");
                }
                return null;
            }
        };
    }

    /**
     * Private class for assets.
     */
    private class Asset {

        public String type;
        public String assetId;
        public long availableDate;
        public long expirationDate;
        public Video video;
        public Common common;

        /**
         * Inner class for video items.
         */
        public class Video {

            public String adId;
            public String videoURL;
            public String seasonNumber;
            public String episodeNumber;
            public String tvRating;
            public String mpaaRating;
            public String subtype;
            public int[] adCuePoints;
            public int totalDuration;
            public boolean hasClosedCaption;
            public String[] closedCaptionURL;
            public boolean requiresAuth;
            public long firstAiredDate;

            public Video() {
                adId = assetId;
                videoURL = SampleFeedData.SAMPLE_VIDEO;
                seasonNumber = Integer.toString(1);
                episodeNumber = Integer.toString(8);
                mpaaRating = "";
                tvRating = "TV-G";
                subtype = "episode";
                adCuePoints = new int[] {5000};
                totalDuration = 2520000;
                hasClosedCaption = false;
                closedCaptionURL = new String[]{SampleFeedData.SAMPLE_URL, SampleFeedData.SAMPLE_URL};
                requiresAuth = false;
                firstAiredDate = availableDate;
            }

        }

        public Asset() {

            type = "awe.Video";
            assetId = Integer.toString(currentItemID);
            availableDate = 1470000000;
            expirationDate = 2000000000;
            video = new Video();
            common = new Common();
        }
    }

    /**
     * Class for categories.
     */
    private class Container {

        public String type;
        public String subtype;
        public String assetId;
        public long availableDate;
        public long expirationDate;
        public Common common;
        public List<Movie> movies;
        public Map<String, Object> container;

        public Container() {

            type = "awe.Container";
            subtype = "movies";
            assetId = Integer.toString(currentContainerID);
            availableDate = 1400000000;
            expirationDate = 2000000000;
            common = new Common(true);
            movies = new ArrayList<>();
            movies.add(new Movie());        // add one movie as an example
            container = new HashMap<>();
            container.put("airingInformation", "Always available");
        }

        public Container(int items) {
            this();
            int itemsToAdd = Math.min(items, assets.size());
            String[] data = new String[itemsToAdd];
            for (int i = 1; i <= itemsToAdd; i++) {
                data[i - 1] = Integer.toString(i);
            }
            container.put("assets", data);
        }
    }

    /**
     * Inner class for common metadata.
     */
    private class Common {

        public String authId;
        public String title;
        public String subtitle;
        public String description;
        public String[] tags;
        public String promoText;
        public String shareURL;
        public Map<String, String> imageUrls;

        public Common() {
            this(false);
        }

        public Common(boolean isContainer) {
            setAssetSample(isContainer);
        }

        /**
         * Populate metadata with sample item.
         * @param isContainer true if populating item in a container.
         */
        public void setAssetSample(boolean isContainer) {

            String[] RESOLUTIONS = {"V_1_1044x788", "V_1_1100x620", "V_1_1120x1960",
                    "V_1_1200x1200", "V_1_1200x2048", "V_1_1440x2560",
                    "V_1_1536x866", "V_1_1600x666", "V_1_2048x1200", "V_1_2088x1576",
                    "V_1_2560x1066", "V_1_2560x1440", "V_1_340x504", "V_1_550x310",
                    "V_1_640x640", "V_1_720x1280", "V_1_750x1110", "V_1_958x538"};
            authId = "1";
            if (isContainer) {
                title = "Sample container " + currentContainerID;
            }
            else {
                title = "Sample item " + currentItemID;
            }
            subtitle = "Sample subtitle";
            description = SampleFeedData.SAMPLE_DESCRIPTIION;
            tags = new String[]{"Lorem", "ipsum", "sample", "tags"};
            promoText = "";
            shareURL = SampleFeedData.SAMPLE_URL;
            imageUrls = new HashMap<>();
            for (String res : RESOLUTIONS) {
                imageUrls.put(res, SampleFeedData.sampleImage(2560));
            }
        }

    }

    /**
     * Private class for movie items.
     */
    private class Movie {

        public String subtype;
        public String assetId;
        public long availableDate;
        public long expirationDate;
        public Common common;

        public Movie() {
            subtype = "movie";
            assetId = "1";
            availableDate = 1470000000;
            expirationDate = 2000000000;
            common = new Common();
        }

    }
}
