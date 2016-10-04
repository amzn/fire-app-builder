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

import java.util.ArrayList;
import java.util.List;

/**
 * This is a screenfeed content container model used to test the object creation using
 * the xml feeds from www.screenfeed.com.
 */
public class ScreenFeedContainer {

    /**
     * Constant for title field.
     */
    public static final String TITLE_FIELD_NAME = "mTitle";

    /**
     * Constant for link field.
     */
    public static final String LINK_FIELD_NAME = "mLink";

    /**
     * Constant for description field.
     */
    public static final String DESCRIPTION_FIELD_NAME = "mDescription";

    /**
     * Constant for docs field.
     */
    public static final String DOCS_FIELD_NAME = "mDocs";

    /**
     * Constant for generator field.
     */
    public static final String GENERATOR_FIELD_NAME = "mGenerator";

    /**
     * Constant for last build data field.
     */
    public static final String LAST_BUILD_DATE_FIELD_NAME = "mLastBuildDate";

    /**
     * Constant for ttl field.
     */
    public static final String TTL_FIELD_NAME = "mTtl";

    /**
     * The member variables. Added one of each primitive type for testing purposes.
     */
    private String mTitle;
    private String mLink;
    private String mDescription;
    private String mDocs;
    private String mGenerator;
    private String mLastBuildDate;
    private long mTtl;
    private List<ScreenFeedContent> mContent = new ArrayList<>();

    /**
     * Get the title.
     *
     * @return The title.
     */
    public String getTitle() {

        return mTitle;
    }

    /**
     * Set the title.
     *
     * @param title The title as string.
     */
    public void setTitle(String title) {

        mTitle = title;
    }

    /**
     * Get the link.
     *
     * @return The link.
     */
    public String getLink() {

        return mLink;
    }

    /**
     * Set the link.
     *
     * @param link The link as string.
     */
    public void setLink(String link) {

        mLink = link;
    }

    /**
     * Get the description.
     *
     * @return The description.
     */
    public String getDescription() {

        return mDescription;
    }

    /**
     * Set the description.
     *
     * @param description The description as string.
     */
    public void setDescription(String description) {

        mDescription = description;
    }

    /**
     * Get the docs.
     *
     * @return The docs.
     */
    public String getDocs() {

        return mDocs;
    }

    /**
     * Set the docs.
     *
     * @param docs The docs as string.
     */
    public void setDocs(String docs) {

        mDocs = docs;
    }

    /**
     * Get the generator.
     *
     * @return The generator.
     */
    public String getGenerator() {

        return mGenerator;
    }

    /**
     * Set the generator.
     *
     * @param generator The generator as string.
     */
    public void setGenerator(String generator) {

        mGenerator = generator;
    }

    /**
     * Get the last build date.
     *
     * @return The last build data.
     */
    public String getLastBuildDate() {

        return mLastBuildDate;
    }

    /**
     * Set the last build date.
     *
     * @param lastBuildDate The last build date as string.
     */
    public void setLastBuildDate(String lastBuildDate) {

        mLastBuildDate = lastBuildDate;
    }

    /**
     * Get the ttl.
     *
     * @return The ttl.
     */
    public long getTtl() {

        return mTtl;
    }

    /**
     * Set the ttl.
     *
     * @param ttl The ttl as long.
     */
    public void setTtl(long ttl) {

        mTtl = ttl;
    }

    /**
     * Get the content list.
     *
     * @return The content list.
     */
    public List<ScreenFeedContent> getContent() {

        return mContent;
    }

    /**
     * Set the content list.
     *
     * @param content The content list.
     */
    public void setContent(List<ScreenFeedContent> content) {

        mContent = content;
    }
}
