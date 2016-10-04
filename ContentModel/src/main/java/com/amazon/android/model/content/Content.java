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
package com.amazon.android.model.content;

import org.json.JSONArray;
import org.json.JSONException;

import android.content.Intent;
import android.util.Log;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Content class is a common model for all different type of Contents like video, audio, photo, doc
 * and etc.
 */
public class Content implements Serializable {

    /**
     * Serial version UID.
     */
    static final long serialVersionUID = 727566175075960111L;

    /**
     * Debug TAG.
     */
    private static final String TAG = Content.class.getSimpleName();

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
     * Url for the card image of the content.
     */
    private String mCardImageUrl;

    /**
     * Url for the background image of the content.
     */
    private String mBackgroundImageUrl;

    /**
     * Flag to detect if a subscription is required for this content.
     */
    private boolean mSubscriptionRequired;

    /**
     * Studio name.
     */
    private String mStudio;

    /**
     * Available date.
     */
    private Integer mAvailableDate;

    /**
     * Content channel id.
     */
    private String mChannelId;

    /**
     * Content duration.
     */
    private String mDuration;

    /**
     * Content format.
     */
    private String mFormat;

    /**
     * Ad cue points.
     */
    private List<Integer> mAdCuePoints;

    /**
     * CC urls of this content.
     */
    private List<String> mCloseCaptionUrls;

    /**
     * Tags of the content. It is a string representation of Json array of strings.
     */
    private String mTags;

    /**
     * Locale of the content; the default is English.
     */
    private Locale mLocale = Locale.ENGLISH;

    /**
     * Storage for extra data that the content might have.
     */
    private Map<String, Object> mExtras;

    /**
     * Constant for title field name.
     */
    public static final String TITLE_FIELD_NAME = "title";

    /**
     * Constant for description field name.
     */
    public static final String DESCRIPTION_FIELD_NAME = "description";

    /**
     * Constant for id field name.
     */
    public static final String ID_FIELD_NAME = "id";

    /**
     * Constant for subtitle field name.
     */
    public static final String SUBTITLE_FIELD_NAME = "subtitle";

    /**
     * Constant for url field name.
     */
    public static final String URL_FIELD_NAME = "url";

    /**
     * Constant for cardImageUrl field name.
     */
    public static final String CARD_IMAGE_URL_FIELD_NAME = "cardImageUrl";

    /**
     * Constant for backgroundImageUrl field name.
     */
    public static final String BACKGROUND_IMAGE_URL_FIELD_NAME = "backgroundImageUrl";

    /**
     * Constant for tags field name.
     */
    public static final String TAGS_FIELD_NAME = "tags";

    /**
     * Creates a {@link Content} with empty values.
     */
    public Content() {

        mId = "0";
        mTitle = "";
        mSubtitle = "";
        mUrl = "";
        mDescription = "";
        mBackgroundImageUrl = "";
        mCardImageUrl = "";
        mStudio = "";
        mSubscriptionRequired = false;
        mDuration = "0";
        mAvailableDate = 0;
        mTags = "[]";
    }

    /**
     * Creates a {@link Content} by title.
     *
     * @param title Title of the content.
     */
    public Content(String title) {

        this();
        mTitle = title;
    }

    /**
     * Put {@link Content} instance into an intent.
     *
     * @param intent Intent.
     */
    public void toExtra(Intent intent) {

        intent.putExtra(Content.class.getSimpleName(), this);
    }

    /**
     * Get content id as long value.
     *
     * @return Id as a long value.
     */
    public long getId() {

        return Long.valueOf(mId);
    }

    /**
     * Set content id.
     *
     * @param id Content id as a long.
     */
    public void setId(Long id) {

        mId = String.valueOf(id);
    }

    /**
     * Get content title.
     *
     * @return Content title as a string.
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
     * Determines if a subscription is required for this content.
     *
     * @return True if subscription is required.
     */
    public boolean isSubscriptionRequired() {

        return mSubscriptionRequired;
    }

    /**
     * Set subscription required flag.
     *
     * @param subscriptionRequired Subscription required flag.
     */
    public void setSubscriptionRequired(boolean subscriptionRequired) {

        mSubscriptionRequired = subscriptionRequired;
    }

    /**
     * Get studio.
     *
     * @return Studio.
     */
    public String getStudio() {

        return mStudio;
    }

    /**
     * Set studio.
     *
     * @param studio Studio.
     */
    public void setStudio(String studio) {

        mStudio = studio;
    }

    /**
     * Get available date.
     *
     * @return Available date.
     */
    public Integer getAvailableDate() {

        return mAvailableDate;
    }

    /**
     * Set available date.
     *
     * @param availableDate Provided date.
     */
    public void setAvailableDate(Integer availableDate) {

        mAvailableDate = availableDate;
    }

    /**
     * Get channel id.
     *
     * @return Channel id.
     */
    public String getChannelId() {

        return mChannelId;
    }

    /**
     * Set channel id.
     *
     * @param channelId Channel id.
     */
    public void setChannelId(String channelId) {

        mChannelId = channelId;
    }

    /**
     * Get content format.
     *
     * @return Content format string.
     */
    public String getFormat() {

        return mFormat;
    }

    /**
     * Set content format.
     *
     * @param format Content format.
     */
    public void setFormat(String format) {

        mFormat = format;
    }

    /**
     * Get content duration.
     *
     * @return Content duration.
     */
    public String getDuration() {

        return mDuration;
    }

    /**
     * Set content duration.
     *
     * @param duration Content duration.
     */
    public void setDuration(String duration) {

        mDuration = duration;
    }

    /**
     * Get close caption urls.
     *
     * @return List of CC urls.
     */
    public List<String> getCloseCaptionUrls() {

        return mCloseCaptionUrls;
    }

    /**
     * Set close caption urls list.
     *
     * @param closeCaptionUrls CC url list.
     */
    public void setCloseCaptionUrls(List<String> closeCaptionUrls) {

        mCloseCaptionUrls = closeCaptionUrls;
    }

    /**
     * Get Ad Cue point list.
     *
     * @return Ad Cue points list.
     */
    public List<Integer> getAdCuePoints() {

        return mAdCuePoints;
    }

    /**
     * Set Ad Cue points list.
     *
     * @param adCuePoints Ad Cue points list.
     */
    public void setAdCuePoints(List<Integer> adCuePoints) {

        mAdCuePoints = adCuePoints;
    }

    /**
     * Determines if this content has closed captions.
     *
     * @return True if closed captions exist; false otherwise.
     */
    public boolean hasCloseCaption() {

        return mCloseCaptionUrls != null && !mCloseCaptionUrls.isEmpty();
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
     * Set extra data into a map. The map will be created the first time a value is set. If the key
     * already exists, its value will be overwritten with the newly supplied value.
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
     * @param tags Content tags as a string representation of a JSON array.
     * @throws JSONException Thrown if tags is not a JSON array string.
     */
    public void setTags(String tags) throws JSONException {

        if (tags == null) {
            mTags = "[]";
            return;
        }
        // Make sure tags is a real JSON array string.
        new JSONArray(mTags);
        mTags = tags;
    }

    /**
     * Check if a {@link Content} has similar tags.
     *
     * @param givenContent The {@link Content} to compare tags against.
     * @return True if they have similar tags; false otherwise.
     */
    public boolean hasSimilarTags(Content givenContent) {
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
     * @return True if the provided fields contain the query string; false otherwise.
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
     * Get string representation of {@link Content} object.
     *
     * @return String representation of {@link Content} object.
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
     * Tests that two {@link Content} objects are equal.
     *
     * @param o The reference object with which to compare.
     * @return True if this object is the same as the o argument; false otherwise.
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        Content content = (Content) o;

        if (getId() != content.getId())
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
