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

import com.amazon.utils.ListUtils;

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
    private String mAvailableDate;

    /**
     * Content channel id.
     */
    private String mChannelId;

    /**
     * Content duration.
     */
    private long mDuration;

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
     * A list of content ids to recommend to the user after the content is played. A string
     * representation of a JSON array of strings.
     */
    private String mRecommendations;

    /**
     * Locale of the content; the default is English.
     */
    private Locale mLocale = Locale.ENGLISH;

    /**
     * Storage for extra data that the content might have.
     */
    private Map<String, Object> mExtras;

    /**
     * Constant for matching the title field name.
     */
    public static final String TITLE_FIELD_NAME = "mTitle";

    /**
     * Constant for matching the description field name.
     */
    public static final String DESCRIPTION_FIELD_NAME = "mDescription";

    /**
     * Constant for matching the id field name.
     */
    public static final String ID_FIELD_NAME = "mId";

    /**
     * Constant for matching the subtitle field name.
     */
    public static final String SUBTITLE_FIELD_NAME = "mSubtitle";

    /**
     * Constant for matching the url field name.
     */
    public static final String URL_FIELD_NAME = "mUrl";

    /**
     * Constant for matching the cardImageUrl field name.
     */
    public static final String CARD_IMAGE_URL_FIELD_NAME = "mCardImageUrl";

    /**
     * Constant for matching the backgroundImageUrl field name.
     */
    public static final String BACKGROUND_IMAGE_URL_FIELD_NAME = "mBackgroundImageUrl";

    /**
     * Constant for matching the closed captions urls field name.
     */
    public static final String CLOSED_CAPTION_FIELD_NAME = "mCloseCaptionUrls";

    /**
     * Constant for matching the tags field name.
     */
    public static final String TAGS_FIELD_NAME = "mTags";

    /**
     * Constant for matching the recommendations field name.
     */
    public static final String RECOMMENDATIONS_FIELD_NAME = "mRecommendations";

    /**
     * Constant for matching the available date field name.
     */
    public static final String AVAILABLE_DATE_FIELD_NAME = "mAvailableDate";

    /**
     * Constant for matching the subscription required field name.
     */
    public static final String SUBSCRIPTION_REQUIRED_FIELD_NAME = "mSubscriptionRequired";

    /**
     * Constant for matching the channel id field name.
     */
    public static final String CHANNEL_ID_FIELD_NAME = "mChannelId";

    /**
     * Constant for matching the duration field name.
     */
    public static final String DURATION_FIELD_NAME = "mDuration";

    /**
     * Constant for matching the ad cue points field name.
     */
    public static final String AD_CUE_POINTS_FIELD_NAME = "mAdCuePoints";

    /**
     * Constant for matching the studio field name.
     */
    public static final String STUDIO_FIELD_NAME = "mStudio";

    /**
     * Constant for matching the format field name.
     */
    public static final String FORMAT_FIELD_NAME = "mFormat";

    /**
     * Constant for getting adId out of extras.
     */
    public static final String AD_ID_FIELD_NAME = "adId";

    /**
     * Constant for getting the maturity rating out of extras.
     */
    public static final String MATURITY_RATING_TAG = "maturityRating";

    /**
     * Constant for getting the genres out of extras.
     */
    public static final String GENRES_TAG = "genres";

    /**
     * Constant for getting the live boolean value out of extras.
     */
    public static final String LIVE_TAG = "live";

    /**
     * Constant for getting the start time value out of extras. For live content only. This string
     * should point to the start time in milliseconds (EPOCH).
     */
    public static final String START_TIME_TAG = "startTime";

    /**
     * Constant for getting the end time value out of extras. For live content only. This string
     * should point to the end time in milliseconds (EPOCH).
     */
    public static final String END_TIME_TAG = "endTime";

    /**
     * Constant for getting the customer rating value out of extras.
     */
    public static final String CUSTOMER_RATING_TAG = "customerRating";

    /**
     * Constant for getting the customer rating count value out of extras.
     */
    public static final String CUSTOMER_RATING_COUNT_TAG = "customerRatingCount";

    /**
     * Constant for getting the video preview url value out of extras.
     */
    public static final String VIDEO_PREVIEW_URL_TAG = "videoPreviewUrl";

    /**
     * Constant for getting the IMDB rating value out of extras.
     */
    public static final String IMDB_ID_TAG = "imdbId";

    /**
     * Constant for getting the Fire TV categories out of extras. Should point to values such as
     * ["Home", "Your Videos"]
     */
    public static final String FIRE_TV_CATEGORIES_TAG = "fireTvCategories";

    /**
     * Constant for getting the content type array value out of extras.
     */
    public static final String CONTENT_TYPE_TAG = "contentTypes";

    /**
     * Constant for getting the recommendation action list out of extras.
     */
    public static final String RECOMMENDATION_ACTIONS_TAG = "recommendationActions";

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
        mDuration = 0;
        mAvailableDate = "";
        mTags = "[]";
        mRecommendations = "[]";
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
     * Get content id.
     *
     * @return Id.
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
    public String getAvailableDate() {

        return mAvailableDate;
    }

    /**
     * Set available date.
     *
     * @param availableDate Provided date.
     */
    public void setAvailableDate(String availableDate) {

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
    public long getDuration() {

        return mDuration;
    }

    /**
     * Set content duration.
     *
     * @param duration Content duration.
     */
    public void setDuration(long duration) {

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
     * Get the map of extra data.
     *
     * @return Extra data map.
     */
    public Map<String, Object> getExtras() {

        return mExtras;
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
     * Get a List from the extras map. Warning: This method expects that the value that the key
     * maps to is a {@link List}. If its not, errors may occur.
     *
     * @param key The key that leads to a list in the map.
     * @return A List. If the extras map is null or the there is no value for the key in the map,
     * an empty list is returned.
     */
    public List getExtraValueAsList(String key) {

        if (mExtras == null || mExtras.get(key) == null) {
            return new ArrayList<>();
        }
        return (List) mExtras.get(key);
    }

    /**
     * Get an extra string value as a list. Warning: This method expects the value returned for
     * the key to be in a list format that can be turned into a {@link JSONArray}. If its not
     * errors may occur.
     *
     * @param key Key value as string.
     * @return A list of strings. List will be empty if {@link #mExtras} is null or if there
     * was an error converting the string to a list.
     */
    public List getExtraStringValueAsList(String key) {

        if (mExtras == null) {
            return new ArrayList<>();
        }
        try {
            List list = ListUtils.stringToList((String) mExtras.get(key));
            if (list == null) {
                return new ArrayList<>();
            }
            return list;
        }
        catch (ListUtils.ExpectingJsonArrayException e) {
            Log.e(TAG, "Couldn't get extra string value as list. ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Get a value from the extras map as a boolean. Warning: This method expects that the value
     * that the key maps to is a boolean. If its not, errors may occur.
     *
     * @param key The key to the value.
     * @return A boolean value; false if the value doesn't exist in the extras.
     */
    public boolean getExtraValueAsBoolean(String key) {

        if (mExtras == null || mExtras.get(key) == null) {
            return false;
        }

        return (boolean) mExtras.get(key);
    }

    /**
     * Get a value from the extras map as a long. Warning: This method expects that the value that
     * the key maps to is a long. If its not, errors may occur.
     *
     * @param key The key to the value.
     * @return A long value; 0 if the value doesn't exist in the extras.
     */
    public long getExtraValueAsLong(String key) {

        if (mExtras == null || mExtras.get(key) == null) {
            return 0;
        }
        // When the value gets parsed, its parsed as an Integer. So cast the Object to Integer
        // then get it as a long value.
        Integer integer = (Integer) mExtras.get(key);
        return (long) integer;
    }

    /**
     * Get a value from the extras map as an int. Warning: This method expects that the value that
     * the key maps to is an int. If its not, errors may occur.
     *
     * @param key The key to the value.
     * @return A long value; 0 if the value doesn't exist in the extras.
     */
    public int getExtraValueAsInt(String key) {

        if (mExtras == null || mExtras.get(key) == null) {
            return 0;
        }

        return (int) mExtras.get(key);
    }

    /**
     * Get content tags.
     *
     * @return Content tags as string list or an empty list if there was an error.
     */
    public List<String> getTags() {

        try {
            return ListUtils.stringToList(mTags);
        }
        catch (ListUtils.ExpectingJsonArrayException e) {
            Log.e(TAG, "There was an error getting tags.", e);
            return new ArrayList<>();
        }
    }

    /**
     * Set content tags. Warning: This method expects the tags value to be in a list format that
     * can be turned into a {@link JSONArray}. If its not errors may occur.
     *
     * @param tags Content tags as a string representation of a JSON array.
     * @throws ListUtils.ExpectingJsonArrayException Thrown if tags is not a JSON array string.
     */
    public void setTags(String tags) throws ListUtils.ExpectingJsonArrayException {

        if (tags == null) {
            mTags = "[]";
            return;
        }
        // Make sure tags is a real JSON array string.
        try {
            new JSONArray(mTags);
        }
        catch (JSONException e) {
            throw new ListUtils.ExpectingJsonArrayException(mTags);
        }
        mTags = tags;
    }

    /**
     * Get the list of content to recommend.
     *
     * @return List of content ids, or an empty list if there was an error.
     */
    public List<String> getRecommendations() {

        try {
            return ListUtils.stringToList(mRecommendations);
        }
        catch (ListUtils.ExpectingJsonArrayException e) {
            Log.e(TAG, "Error getting recommendations. ", e);
            return new ArrayList<>();
        }
    }

    /**
     * Set the list of recommendations to recommend.
     *
     * @param recommendations List of content ids as a string representation of a JSON array.
     * @throws ListUtils.ExpectingJsonArrayException Thrown if the recommendations string is not a
     *                                               JSON array string.
     */
    public void setRecommendations(String recommendations) throws ListUtils
            .ExpectingJsonArrayException {

        if (recommendations == null) {
            mRecommendations = "[]";
            return;
        }
        // Make sure recommendations is a real JSON array string.
        try {
            new JSONArray(mRecommendations);
        }
        catch (JSONException e) {
            throw new ListUtils.ExpectingJsonArrayException(mRecommendations);
        }
        mRecommendations = recommendations;
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

    @Override
    public String toString() {

        return "Content{" +
                "mId='" + mId + '\'' +
                ", mTitle='" + mTitle + '\'' +
                ", mSubtitle='" + mSubtitle + '\'' +
                ", mUrl='" + mUrl + '\'' +
                ", mDescription='" + mDescription + '\'' +
                ", mCardImageUrl='" + mCardImageUrl + '\'' +
                ", mBackgroundImageUrl='" + mBackgroundImageUrl + '\'' +
                ", mSubscriptionRequired=" + mSubscriptionRequired +
                ", mStudio='" + mStudio + '\'' +
                ", mAvailableDate='" + mAvailableDate + '\'' +
                ", mChannelId='" + mChannelId + '\'' +
                ", mDuration=" + mDuration +
                ", mFormat='" + mFormat + '\'' +
                ", mAdCuePoints=" + mAdCuePoints +
                ", mCloseCaptionUrls=" + mCloseCaptionUrls +
                ", mTags='" + mTags + '\'' +
                ", mRecommendations='" + mRecommendations + '\'' +
                ", mLocale=" + mLocale +
                ", mExtras=" + mExtras +
                '}';
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
        if (getBackgroundImageUrl() != null ?
                !getBackgroundImageUrl().equals(content.getBackgroundImageUrl()) :
                content.getBackgroundImageUrl() != null)
            return false;
        if (getTags() != null ? !getTags().equals(content.getTags()) : content.getTags() != null)
            return false;
        if (getLocale() != null ? !getLocale().equals(content.getLocale()) : content.getLocale()
                != null)
            return false;
        return !(mExtras != null ? !mExtras.equals(content.mExtras) : content.mExtras != null);

    }

}
