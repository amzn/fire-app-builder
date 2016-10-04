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
 * This is a screen feed content model used to test object creation using xml feeds
 * from www.screenfeed.com.
 */
public class ScreenFeedContent {

    /**
     * Title of the content.
     */
    private String mTitle;

    /**
     * Description of the content.
     */
    private String mDescription;

    /**
     * Guid of of the content.
     */
    private String mGuid;

    /**
     * Publish date of the content.
     */
    private String mPubDate;

    /**
     * Category of the content.
     */
    private String mCategory;

    /**
     * URL of the video content.
     */
    private String mUrl;

    /**
     * File size of the content.
     */
    private String mFileSize;

    /**
     * Type of the content.
     */
    private String mType;

    /**
     * Medium type of the content.
     */
    private String mMedium;

    /**
     * Duration of the content.
     */
    private String mDuration;

    /**
     * Height of the content.
     */
    private String mHeight;

    /**
     * Width of the content.
     */
    private String mWidth;

    /**
     * Credit of the content.
     */
    private String mCredit;

    /**
     * Constant for title field name.
     */
    public static final String TITLE_FIELD_NAME = "mTitle";

    /**
     * Constant for description field name.
     */
    public static final String DESCRIPTION_FIELD_NAME = "mDescription";

    /**
     * Constant for guid field name.
     */
    public static final String GUID_FIELD_NAME = "mGuid";

    /**
     * Constant for pubDate field name.
     */
    public static final String PUBDATE_FIELD_NAME = "mPubDate";

    /**
     * Constant for category field name.
     */
    public static final String CATEGORY_FIELD_NAME = "mCategory";

    /**
     * Constant for url field name.
     */
    public static final String URL_FIELD_NAME = "mUrl";

    /**
     * Constant for fileSize field name.
     */
    public static final String FILE_SIZE_FIELD_NAME = "mFileSize";

    /**
     * Constant for type field name.
     */
    public static final String TYPE_FIELD_NAME = "mType";

    /**
     * Constant for medium field name.
     */
    public static final String MEDIUM_FIELD_NAME = "mMedium";

    /**
     * Constant for duration field name.
     */
    public static final String DURATION_FIELD_NAME = "mDuration";

    /**
     * Constant for height field name.
     */
    public static final String HEIGHT_FIELD_NAME = "mHeight";

    /**
     * Constant for width field name.
     */
    public static final String WIDTH_FIELD_NAME = "mWidth";

    /**
     * Constant for credit field name.
     */
    public static final String CREDIT_FIELD_NAME = "mCredit";

    /**
     * Constructor.
     */
    ScreenFeedContent() {

        mTitle = "";
        mDescription = "";
        mGuid = "";
        mCategory = "";
        mPubDate = "";
        mUrl = "";
        mFileSize = "0";
        mDuration = "0.0";
        mHeight = "1280";
        mWidth = "720";
        mType = "";
        mMedium = "video";
        mCredit = "";
    }

    /**
     * Get content title.
     *
     * @return Content title.
     */
    public String getTitle() {

        return mTitle;
    }

    /**
     * Set content title.
     *
     * @param title Content title as string.
     */
    public void setTitle(String title) {

        mTitle = title;
    }

    /**
     * Get content description.
     *
     * @return Content description.
     */
    public String getDescription() {

        return mDescription;
    }

    /**
     * Set content description.
     *
     * @param description Content description as string.
     */
    public void setDescription(String description) {

        mDescription = description;
    }

    /**
     * Get content guid.
     *
     * @return Content guid.
     */
    public String getGuid() {

        return mGuid;
    }

    /**
     * Set content guid.
     *
     * @param guid Content guid as string.
     */
    public void setGuid(String guid) {

        mGuid = guid;
    }

    /**
     * Get content publish date.
     *
     * @return Content publish date.
     */
    public String getPubDate() {

        return mPubDate;
    }

    /**
     * Set content publish date.
     *
     * @param pubDate Content publish date as string.
     */
    public void setPubDate(String pubDate) {

        mPubDate = pubDate;
    }

    /**
     * Get content category.
     *
     * @return Content category.
     */
    public String getCategory() {

        return mCategory;
    }

    /**
     * Set content category.
     *
     * @param category Content category as string.
     */
    public void setCategory(String category) {

        mCategory = category;
    }

    /**
     * Get content url.
     *
     * @return Content url.
     */
    public String getUrl() {

        return mUrl;
    }

    /**
     * Set content url.
     *
     * @param url Content url as string.
     */
    public void setUrl(String url) {

        mUrl = url;
    }

    /**
     * Get content file size as long value.
     *
     * @return Content file size as long value.
     */
    public long getFileSize() {

        return Long.valueOf(mFileSize);
    }

    /**
     * Set content file size.
     *
     * @param fileSize Content file size as long
     */
    public void setFileSize(Long fileSize) {

        mFileSize = String.valueOf(fileSize);
    }

    /**
     * Get content type.
     *
     * @return Content type.
     */
    public String getType() {

        return mType;
    }

    /**
     * Set content type.
     *
     * @param type Content type as string.
     */
    public void setType(String type) {

        mType = type;
    }

    /**
     * Get content medium.
     *
     * @return Content medium.
     */
    public String getMedium() {

        return mMedium;
    }

    /**
     * Set content medium.
     *
     * @param medium Content medium as string.
     */
    public void setMedium(String medium) {

        mMedium = medium;
    }

    /**
     * Get content duration.
     *
     * @return Content duration as double.
     */
    public double getDuration() {

        return Double.valueOf(mDuration);
    }

    /**
     * Set content duration.
     *
     * @param duration Content duration as double.
     */
    public void setDuration(Double duration) {

        mDuration = String.valueOf(duration);
    }

    /**
     * Get content height
     *
     * @return Content height as int.
     */
    public int getHeight() {

        return Integer.valueOf(mHeight);
    }

    /**
     * Set content height.
     *
     * @param height Content height as int.
     */
    public void setHeight(Integer height) {

        mHeight = String.valueOf(height);
    }

    /**
     * Get content width.
     *
     * @return Content width as int.
     */
    public int getWidth() {

        return Integer.valueOf(mWidth);
    }

    /**
     * Set content width.
     *
     * @param width Content width as int.
     */
    public void setWidth(Integer width) {

        mWidth = String.valueOf(width);
    }

    /**
     * Get content credit.
     *
     * @return Content credit.
     */
    public String getCredit() {

        return mCredit;
    }

    /**
     * Set content credit.
     *
     * @param credit Content credit as string.
     */
    public void setCredit(String credit) {

        mCredit = credit;
    }
}

