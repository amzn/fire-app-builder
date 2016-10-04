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
package com.amazon.dynamicparser.testResources;

/**
 * The model class used to test the {@link com.amazon.dynamicparser.DynamicParser} with the
 * "5000PhotosFeed.json".
 */
public class PhotoModel {

    /**
     * Constant for album field.
     */
    public static final String ALBUM_ID_FIELD_NAME = "albumId";
    /**
     * Constant for id field.
     */
    public static final String ID_FIELD_NAME = "id";
    /**
     * Constant for title field.
     */
    public static final String TITLE_FIELD_NAME = "title";
    /**
     * Constant for url field.
     */
    public static final String URL_FIELD_NAME = "url";
    /**
     * Constant for thumbnail url field
     */
    public static final String THUMBNAIL_URL_FIELD_NAME = "thumbnailUrl";

    private int albumId;
    private int id;
    private String title;
    private String url;
    private String thumbnailUrl;

    /**
     * Get album id.
     *
     * @return The ablum id.
     */
    int getAlbumId() {

        return albumId;
    }

    /**
     * Set album id.
     *
     * @param albumId The album id.
     */
    public void setAlbumId(int albumId) {

        this.albumId = albumId;
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public int getId() {

        return id;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(int id) {

        this.id = id;
    }

    /**
     * Get the title.
     *
     * @return The title.
     */
    public String getTitle() {

        return title;
    }

    /**
     * Set the title.
     *
     * @param title The title.
     */
    public void setTitle(String title) {

        this.title = title;
    }

    /**
     * Get the url.
     *
     * @return The url.
     */
    public String getUrl() {

        return url;
    }

    /**
     * Set the url.
     *
     * @param url The url.+
     */
    public void setUrl(String url) {

        this.url = url;
    }

    /**
     * Get the thumbnail url.
     *
     * @return The thumbnail url.
     */
    public String getThumbnailUrl() {

        return thumbnailUrl;
    }

    /**
     * Set the thumbnail url.
     *
     * @param thumbnailUrl The thumbnail url.
     */
    public void setThumbnailUrl(String thumbnailUrl) {

        this.thumbnailUrl = thumbnailUrl;
    }
}
