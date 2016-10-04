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
 * A model class used to test object creation with the "100AlbumsFeed.json".
 */
public class AlbumModel {

    /**
     * Constant for id field.
     */
    public static final String ID_FIELD = "id";
    /**
     * Constant for user id field.
     */
    public static final String USER_ID_FIELD = "userId";
    /**
     * Constant for title field.
     */
    public static final String TITLE_FIELD = "title";

    private int id;
    private int userId;
    private String title;

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
     * Get the user id.
     *
     * @return The user id.
     */
    public int getUserId() {

        return userId;
    }

    /**
     * Set the user id.
     *
     * @param userId The user id.
     */
    public void setUserId(int userId) {

        this.userId = userId;
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
}
