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
package com.amazon.android.contentbrowser.database.records;

/**
 * Base class for a record that belongs to the content database.
 */
public class Record {

    /**
     * The content id.
     */
    private String mContentId;

    /**
     * Default constructor.
     */
    public Record() {

    }

    /**
     * Constructor.
     *
     * @param contentId The content id.
     */
    public Record(String contentId) {

        mContentId = contentId;
    }

    /**
     * Get the content id.
     *
     * @return The content id.
     */
    public String getContentId() {

        return mContentId;
    }

    /**
     * Set the content id.
     *
     * @param contentId The content id.
     */
    public void setContentId(String contentId) {

        mContentId = contentId;
    }
}
