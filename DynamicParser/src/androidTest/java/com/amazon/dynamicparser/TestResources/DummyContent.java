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

import org.json.JSONArray;
import org.json.JSONException;

import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * This is a dummy content model used to test the object creation using the "SampleVideoFeed.json"
 * file.
 */
public class DummyContent {

    private static final String TAG = DummyContent.class.getSimpleName();
    /**
     * Id of of the content.
     */
    private String mId;

    /**
     * Title of the content.
     */
    private String mTitle;

    /**
     * Subtitle of the content.
     */
    private String mSubtitle;

    /**
     * Url of the content.
     */
    private String mUrl;

    /**
     * Description of the content.
     */
    private String mDescription;

    /**
     * CardImageUrl of the content.
     */
    private String mCardImageUrl;

    /**
     * BackgroundImageUrl of the content.
     */
    private String mBackgroundImageUrl;

    /**
     * Tags of the content. It is a string representation of Json array of strings.
     */
    private String mTags;

    /**
     * Locale of the content, default is English.
     */
    private Locale mLocale = Locale.ENGLISH;

    /**
     * Helper extra data storage.
     */
    private Map<String, Object> mExtras;

    /**
     * Constant for title field name.
     */
    public static final String TITLE_FIELD_NAME = "mTitle";

    /**
     * Constant for description field name.
     */
    public static final String DESCRIPTION_FIELD_NAME = "mDescription";

    /**
     * Constant for id field name.
     */
    public static final String ID_FIELD_NAME = "mId";

    /**
     * Constant for subtitle field name.
     */
    public static final String SUBTITLE_FIELD_NAME = "mSubtitle";

    /**
     * Constant for url field name.
     */
    public static final String URL_FIELD_NAME = "mUrl";


    /**
     * Constant for cardImageUrl field name.
     */
    public static final String CARD_IMAGE_URL_FIELD_NAME = "mCardImageUrl";

    /**
     * Constant for backgroundImageUrl field name.
     */
    public static final String BACKGROUND_IMAGE_URL_FIELD_NAME = "mBackgroundImageUrl";

    /**
     * Constant for tags field name.
     */
    public static final String TAGS_FIELD_NAME = "mTags";

    /**
     * Constructor.
     */
    public DummyContent() {

        mId = "0";
        mTitle = "";
        mSubtitle = "";
        mUrl = "";
        mDescription = "";
        mBackgroundImageUrl = "";
        mCardImageUrl = "";
        mTags = "[]";
    }

    /**
     * Constructor by title.
     *
     * @param title Title of the content.
     */
    public DummyContent(String title) {

        this();
        mTitle = title;
    }

    /**
     * Get content id.
     *
     * @return Content id.
     */
    public String getId() {

        return mId;
    }

    /**
     * Set content id.
     *
     * @param id Content id.
     */
    public void setId(String id) {

        mId = id;
    }

    /**
     * Get content title.
     *
     * @return Content title as string.
     */
    public String getTitle() {

        return mTitle;
    }

    /**
     * Set content title.
     *
     * @param title Content title.
     */
    public void setTitle(String title) {

        mTitle = title;
    }

    /**
     * Get content subtitle.
     *
     * @return Content subtitle.
     */
    public String getSubtitle() {

        return mSubtitle;
    }

    /**
     * Set content subtitle.
     *
     * @param subtitle Content subtitle.
     */
    public void setSubtitle(String subtitle) {

        mSubtitle = subtitle;
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
     * @param url Content url.
     */
    public void setUrl(String url) {

        mUrl = url;
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
     * @param description Content description.
     */
    public void setDescription(String description) {

        mDescription = description;
    }

    /**
     * Get content background image url.
     *
     * @return Content background image url.
     */
    public String getBackgroundImageUrl() {

        return mBackgroundImageUrl;
    }

    /**
     * Set content background image url.
     *
     * @param backgroundImageUrl Content background image url.
     */
    public void setBackgroundImageUrl(String backgroundImageUrl) {

        mBackgroundImageUrl = backgroundImageUrl;
    }

    /**
     * Get content card image url.
     *
     * @return Content card image url.
     */
    public String getCardImageUrl() {

        return mCardImageUrl;
    }

    /**
     * Set content card image url.
     *
     * @param cardImageUrl Content card image url.
     */
    public void setCardImageUrl(String cardImageUrl) {

        mCardImageUrl = cardImageUrl;
    }

    /**
     * Get Locale of the content object.
     *
     * @return Locale of the content object.
     */
    public Locale getLocale() {

        return mLocale;
    }

    /**
     * Set Locale of the content object.
     *
     * @param locale Locale of the content object.
     */
    public void setLocale(Locale locale) {

        mLocale = locale;
    }

    /**
     * Get extra data as object from internal map.
     *
     * @param key Key value as string.
     * @return Value as object.
     */
    public Object getExtraValue(String key) {

        if (mExtras == null) {
            return null;
        }

        return mExtras.get(key);
    }

    /**
     * Set extra data into a map, Map will be created with first setting. If the key already
     * exists, its value will be overwritten with the newly supplied value.
     *
     * @param key   Key value as string.
     * @param value Value as object.
     */
    public void setExtraValue(String key, Object value) {

        if (mExtras == null) {
            mExtras = new HashMap<>();
        }

        mExtras.put(key, value);
    }

    /**
     * Get content tags.
     *
     * @return Content tags as String list.
     */
    public List<String> getTags() {

        List<String> list = new ArrayList<>();
        try {
            JSONArray jsonArray = new JSONArray(mTags);
            for (int i = 0; i < jsonArray.length(); i++) {
                list.add(jsonArray.getString(i));
            }
        }
        catch (JSONException e) {
            Log.e(TAG, "There was an error", e);
            return list;
        }
        return list;
    }

    /**
     * Set content tags.
     *
     * @param tags Content tags as a string representation of a json array.
     * @throws JSONException Throws JsonException if tags is not a Json array string.
     */
    public void setTags(String tags) throws JSONException {

        if (tags == null) {
            mTags = "[]";
            return;
        }
        // Make sure tags is a real json array string.
        new JSONArray(mTags);
        mTags = tags;
    }

    /**
     * Check if givenContent has similar tags as this.
     *
     * @param givenContent Given content object
     * @return True if content's are similar.
     */
    public boolean hasSimilarTags(DummyContent givenContent) {
        // Get list of givenContentTags.
        List<String> givenContentTags = givenContent.getTags();

        // Get list of thisContentTags.
        List<String> thisContentTags = getTags();

        // If any of the lists are empty then return false.
        if (givenContentTags.size() == 0 ||
                thisContentTags.size() == 0) {
            return false;
        }

        // O(n2) loop for match checking.
        for (String givenTag : givenContentTags) {
            for (String thisTag : thisContentTags) {
                // If there is a common tag then return true.
                if (givenTag.equals(thisTag)) {
                    return true;
                }
            }
        }

        // If we reached this point that means there is no similar tag.
        return false;
    }

    /**
     * Get string field by name from content object fields.
     *
     * @param name Name of the field.
     * @return Field value as string.
     */
    public String getStringFieldByName(String name) {

        if (name == null) {
            return null;
        }

        if (name.equals(TITLE_FIELD_NAME)) {
            return mTitle;
        }
        else if (name.equals(DESCRIPTION_FIELD_NAME)) {
            return mDescription;
        }
        return null;
    }

    /**
     * Search query under provided fieldNames.
     *
     * @param query      Query string.
     * @param fieldNames Field names to be searched.
     * @return True if there provided fields of content object contains query string.
     */
    public boolean searchInFields(String query, String[] fieldNames) {

        boolean result = false;

        if (fieldNames == null ||
                fieldNames.length == 0) {
            return false;
        }

        for (String fieldName : fieldNames) {
            if (getStringFieldByName(fieldName) != null) {
                result |= getStringFieldByName(fieldName)
                        .toLowerCase(mLocale)
                        .contains(query.toLowerCase(mLocale));
            }
        }
        return result;
    }

    /**
     * Get string representation of Content object.
     *
     * @return String representation of Content object.
     */
    @Override
    public String toString() {

        return "Content-> id:" + mId +
                " title: " + mTitle + " " + mSubtitle +
                " Description: " + mDescription +
                " CardImageUrl:" + mCardImageUrl +
                " tags:" + mTags;
    }

    /**
     * Tests that two {@link DummyContent} objects are equal.
     *
     * @param o The reference object with which to compare.
     * @return true if this object is the same as the o argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        DummyContent content = (DummyContent) o;

        if (getId() != null ? !getId().equals(content.getId()) : content.getId() !=
                null)
            return false;
        if (getTitle() != null ? !getTitle().equals(content.getTitle()) : content.getTitle() !=
                null)
            return false;
        if (getSubtitle() != null ? !getSubtitle().equals(content.getSubtitle()) : content
                .getSubtitle() != null)
            return false;
        if (getUrl() != null ? !getUrl().equals(content.getUrl()) : content.getUrl() != null)
            return false;
        if (getDescription() != null ? !getDescription().equals(content.getDescription()) :
                content.getDescription() != null)
            return false;
        if (getCardImageUrl() != null ? !getCardImageUrl().equals(content.getCardImageUrl()) :
                content.getCardImageUrl() != null)
            return false;
        if (getBackgroundImageUrl() != null ? !getBackgroundImageUrl().equals(content.getBackgroundImageUrl()) : content.getBackgroundImageUrl() != null)
            return false;
        if (getTags() != null ? !getTags().equals(content.getTags()) : content.getTags() != null)
            return false;
        if (getLocale() != null ? !getLocale().equals(content.getLocale()) : content.getLocale()
                != null)
            return false;
        return !(mExtras != null ? !mExtras.equals(content.mExtras) : content.mExtras != null);

    }
}
