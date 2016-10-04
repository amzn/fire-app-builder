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
 * A model class used in testing the {@link com.amazon.dynamicparser.DynamicParser} with the
 * "500CommentsFeed.json" file.
 */
public class CommentModel {

    /**
     * Constant for post id field.
     */
    public static final String POST_ID_FIELD = "mPostId";
    /**
     * Constant for id field.
     */
    public static final String ID_FIELD = "mId";
    /**
     * Constant for name field.
     */
    public static final String NAME_FIELD = "mName";
    /**
     * Constant for email field.
     */
    public static final String EMAIL_FIELD = "mEmail";
    /**
     * Constant for body field.
     */
    public static final String BODY_FIELD = "mBody";

    private int mPostId;
    private int mId;
    private String mName;
    private String mEmail;
    private String mBody;

    /**
     * Get post id.
     *
     * @return The post id.
     */
    public int getPostId() {

        return mPostId;
    }

    /**
     * Set post id.
     *
     * @param postId The post id.
     */
    public void setPostId(int postId) {

        mPostId = postId;
    }

    /**
     * Get the id.
     *
     * @return The id.
     */
    public int getId() {

        return mId;
    }

    /**
     * Set the id.
     *
     * @param id The id.
     */
    public void setId(int id) {

        mId = id;
    }

    /**
     * Get the name.
     *
     * @return The name.
     */
    public String getName() {

        return mName;
    }

    /**
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name) {

        mName = name;
    }

    /**
     * Get the email.
     *
     * @return The email.
     */
    public String getEmail() {

        return mEmail;
    }

    /**
     * Set the email.
     *
     * @param email The email.
     */
    public void setEmail(String email) {

        mEmail = email;
    }

    /**
     * Get the body.
     *
     * @return The body.
     */
    public String getBody() {

        return mBody;
    }

    /**
     * Set the body.
     *
     * @param body The body.
     */
    public void setBody(String body) {

        mBody = body;
    }
}
