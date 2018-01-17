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
package com.amazon.android.contentbrowser.recommendations;

import com.amazon.android.contentbrowser.R;
import com.bumptech.glide.Glide;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.support.app.recommendation.ContentRecommendation;

import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

/**
 * This follows the builder pattern and is responsible for building recommendations to be sent to
 * the notification manager. See {@link RecommendationExtras} for further explanation of how the
 * member variables are used when the recommendation is displayed.
 */
class RecommendationBuilder {

    /**
     * Debug tag.
     */
    private static final String TAG = RecommendationBuilder.class.getSimpleName();

    /**
     * Width for large icon bitmap. Wanting 16:9 aspect ratio.
     */
    private static final int CARD_WIDTH = 450;

    /**
     * Height for large icon bitmap. Wanting 16:9 aspect ratio.
     */
    private static final int CARD_HEIGHT = 252;

    /**
     * The background URL string.
     */
    private String mBackgroundUrl;

    /**
     * The app context.
     */
    private Context mContext;

    /**
     * The id of the content that's being recommended.
     */
    private String mContentId;

    /**
     * A list of content categories, may include: movie, tv, short (specific strings are known to
     * Fire TV Services).
     */
    private ArrayList<String> mContentCategories;

    /**
     * A flag representing if the content has closed captions.
     */
    private int mContentClosedCaptions;

    /**
     * The customer rating.
     */
    private int mContentCustomerRating;

    /**
     * The customer rating count.
     */
    private int mContentCustomerRatingCount;

    /**
     * The content's duration.
     */
    private long mContentDuration;

    /**
     * The intent that is sent when the recommendation is clicked.
     */
    private Intent mContentIntent;

    /**
     * The content's release date.
     */
    private String mContentReleaseDate;

    /**
     * The content's start time (for live only).
     */
    private long mContentStartTime;

    /**
     * The content's end time (for live only).
     */
    private long mContentEndTime;

    /**
     * A description for the content.
     */
    private String mDescription;

    /**
     * The intent that is sent when the recommendation is dismissed by notification manager.
     */
    private Intent mDismissIntent;

    /**
     * The content's IMDB rating.
     */
    private String mImdbId;

    /**
     * The large icon URL string.
     */
    private String mLargeIconUrl;

    /**
     * A flag representing if the content is live.
     */
    private int mLiveContent;

    /**
     * The content's maturity rating.
     */
    private String mMaturityRating;

    /**
     * A URL to show a preview of the video.
     */
    private String mPreviewVideoUrl;

    /**
     * The priority rank of the notification.
     */
    private int mRank;

    /**
     * The recommendation id.
     */
    private int mRecommendationId;

    /**
     * The content text, may be content's description.
     */
    private String mText;

    /**
     * The content's title.
     */
    private String mTitle;

    /**
     * A label used when grouping recommendations together.
     */
    private String mGroup;

    /**
     * The current playback progress of the content.
     */
    private int mPlaybackProgress;

    /**
     * The content's genres.
     */
    private String[] mGenres;

    /**
     * The content's types.
     */
    private String[] mContentTypes;

    /**
     * The recommendation action list.
     */
    private ArrayList<Integer> mActions;

    /**
     * The date time of the recommended content's last viewing.
     */
    private long mLastWatchedDateTime;

    /**
     * Builds the notification.
     *
     * @return The notification.
     * @throws ExecutionException   A possible exception when loading the large icon URL into a
     *                              bitmap.
     * @throws InterruptedException A possible exception when loading the large icon URL into a
     *                              bitmap.
     */
    public Notification build() throws ExecutionException, InterruptedException {

        // This line may cause the exceptions if there's an issue with the network or the URL.
        Bitmap largeIconBitmap = Glide.with(mContext)
                                      .load(mLargeIconUrl)
                                      .asBitmap()
                                      .into(CARD_WIDTH, CARD_HEIGHT)
                                      .get();

        // Build the basic recommendation.
        ContentRecommendation.Builder builder = new ContentRecommendation.Builder()
                .setIdTag(String.valueOf(mRecommendationId))
                .setTitle(mTitle)
                .setText(mText)
                .setContentImage(largeIconBitmap)
                .setBadgeIcon(R.drawable.app_logo)
                .setGroup(mGroup)
                .setBackgroundImageUri(mBackgroundUrl)
                .setRunningTime(mContentDuration)
                .setProgress((int) mContentDuration, mPlaybackProgress)
                .setGenres(mGenres)
                .setContentTypes(mContentTypes)
                .setMaturityRating(mMaturityRating);
        if (mContentIntent != null) {
            builder.setContentIntentData(ContentRecommendation.INTENT_TYPE_ACTIVITY, mContentIntent,
                                         mRecommendationId, null);
        }
        if (mDismissIntent != null) {
            builder.setDismissIntentData(ContentRecommendation.INTENT_TYPE_SERVICE, mDismissIntent,
                                         mRecommendationId, null);
        }

        ContentRecommendation recommendation = builder.build();

        Notification notification = recommendation.getNotificationObject(mContext);

        // Add Amazon extras.
        notification.extras.putStringArrayList(RecommendationExtras.EXTRA_TAGS, mContentCategories);
        notification.extras.putString(RecommendationExtras.EXTRA_AMAZON_CONTENT_ID, mContentId);
        notification.extras.putString(RecommendationExtras.EXTRA_MATURITY_RATING, mMaturityRating);
        notification.extras.putString(RecommendationExtras.EXTRA_LONG_DESCRIPTION, mDescription);
        notification.extras.putString(RecommendationExtras.EXTRA_APP_NAME,
                                      mContext.getString(R.string.app_name_short));
        notification.extras.putInt(RecommendationExtras.EXTRA_RANK, mRank);
        notification.extras.putInt(RecommendationExtras.EXTRA_LIVE_CONTENT, mLiveContent);
        notification.extras.putLong(RecommendationExtras.EXTRA_START_TIME, mContentStartTime);
        notification.extras.putLong(RecommendationExtras.EXTRA_END_TIME, mContentEndTime);
        notification.extras.putString(RecommendationExtras.EXTRA_CONTENT_RELEASE_DATE,
                                      mContentReleaseDate);
        notification.extras.putInt(RecommendationExtras.EXTRA_CAPTION_OPTION,
                                   mContentClosedCaptions);
        notification.extras.putInt(RecommendationExtras.EXTRA_CONTENT_CUSTOMER_RATING,
                                   mContentCustomerRating);
        notification.extras.putInt(RecommendationExtras.EXTRA_CONTENT_CUSTOMER_RATING_COUNT,
                                   mContentCustomerRatingCount);
        notification.extras.putString(RecommendationExtras.EXTRA_PREVIEW_VIDEO_URL,
                                      mPreviewVideoUrl);
        notification.extras.putString(RecommendationExtras.EXTRA_IMDB_ID, mImdbId);
        notification.extras.putIntegerArrayList(RecommendationExtras.EXTRA_ACTION_OPTION, mActions);
        notification.extras.putLong(RecommendationExtras.EXTRA_LAST_WATCHED, mLastWatchedDateTime);
        return notification;
    }

    /**
     * Set the background image URL.
     *
     * @param backgroundUrl The backgroun URL.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setBackgroundUrl(String backgroundUrl) {

        mBackgroundUrl = backgroundUrl;
        return this;
    }

    /**
     * Set the context.
     *
     * @param context The context.
     * @return The {@link RecommendationBuilder} instance.
     */
    public RecommendationBuilder setContext(Context context) {

        mContext = context;
        return this;
    }

    /**
     * Set the content id.
     *
     * @param contentId The content id.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentId(String contentId) {

        mContentId = contentId;
        return this;
    }

    /**
     * Set the content closed captions flag. Set to 1 if the content has closed captions and 0 if
     * it does not.
     *
     * @param closedCaptions The content closed captions flag.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentClosedCaptions(int closedCaptions) {

        mContentClosedCaptions = closedCaptions;
        return this;
    }

    /**
     * Set the content's customer rating.
     *
     * @param customerRating The customer rating.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentCustomerRating(int customerRating) {

        mContentCustomerRating = customerRating;
        return this;
    }

    /**
     * Set the content's customer rating count.
     *
     * @param customerRatingCount The customer rating count.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentCustomerRatingCount(int customerRatingCount) {

        mContentCustomerRatingCount = customerRatingCount;
        return this;
    }

    /**
     * Set the content's duration.
     *
     * @param duration The duration.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentDuration(long duration) {

        mContentDuration = duration;
        return this;
    }

    /**
     * Set the content's end time.
     *
     * @param endTime The end time.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentEndTime(long endTime) {

        mContentEndTime = endTime;
        return this;
    }

    /**
     * Set the content's release date.
     *
     * @param releaseDate The release date.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentReleaseDate(String releaseDate) {

        mContentReleaseDate = releaseDate;
        return this;
    }

    /**
     * Set the content intent. This intent is started when the content's recommendation is clicked.
     *
     * @param contentIntent The content intent.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentIntent(Intent contentIntent) {

        mContentIntent = contentIntent;
        return this;
    }

    /**
     * Set the content's start time.
     *
     * @param startTime The start time.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentStartTime(long startTime) {

        mContentStartTime = startTime;
        return this;
    }

    /**
     * Set the dismiss intent. This intent is started when the content's recommendation is
     * dismissed.
     *
     * @param dismissIntent The dismiss intent.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setDismissIntent(Intent dismissIntent) {

        mDismissIntent = dismissIntent;
        return this;
    }

    /**
     * Set the content's IMDB id.
     *
     * @param imdbId The IMDV id.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setImdbId(String imdbId) {

        mImdbId = imdbId;
        return this;
    }

    /**
     * Set the recommendation id.
     *
     * @param recommendationId The recommendation id.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setRecommendationId(int recommendationId) {

        mRecommendationId = recommendationId;
        return this;
    }

    /**
     * Set the large icon URL.
     *
     * @param largeIconUrl The large icon URL.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setLargeIconUrl(String largeIconUrl) {

        mLargeIconUrl = largeIconUrl;
        return this;
    }

    /**
     * Set the live content flag. Set it to 1 if the content should be treated as live content,
     * and 0 if its not.
     *
     * @param liveContent The live content flag.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setLiveContent(int liveContent) {

        mLiveContent = liveContent;
        return this;
    }

    /**
     * Set the long description of the content.
     *
     * @param description The long description.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setDescription(String description) {

        mDescription = description;
        return this;
    }

    /**
     * Set the maturity rating of the content.
     *
     * @param maturityRating The maturity rating.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setMaturityRating(String maturityRating) {

        mMaturityRating = maturityRating;
        return this;
    }

    /**
     * Set the content's preview video URL.
     *
     * @param previewVideoUrl The preview video URL.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setPreviewVideoUrl(String previewVideoUrl) {

        mPreviewVideoUrl = previewVideoUrl;
        return this;
    }

    /**
     * Set the recommendation rank.
     *
     * @param rank The rank.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setRank(int rank) {

        mRank = rank;
        return this;
    }

    /**
     * Set the content's description as the recommendation text.
     *
     * @param text The text.
     * @return The {@link RecommendationBuilder} instance.
     */
    public RecommendationBuilder setText(String text) {

        mText = text;
        return this;
    }

    /**
     * Set the content's title as the recommendation's title.
     *
     * @param title The title.
     * @return The {@link RecommendationBuilder} instance.
     */
    public RecommendationBuilder setTitle(String title) {

        mTitle = title;
        return this;
    }

    /**
     * Set the group.
     *
     * @param group The group.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setGroup(String group) {

        mGroup = group;
        return this;
    }

    /**
     * Set the content categories.
     *
     * @param contentCategories A string representation of a list of content categories.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentCategories(ArrayList<String> contentCategories) {

        mContentCategories = contentCategories;
        return this;
    }

    /**
     * Set the playback progress of the content.
     *
     * @param playbackProgress The playback progress position.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setPlaybackProgress(int playbackProgress) {

        mPlaybackProgress = playbackProgress;
        return this;
    }

    /**
     * Set the content's genres.
     *
     * @param genres The genres.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setGenres(String[] genres) {

        mGenres = genres;
        return this;
    }

    /**
     * Set the content's types.
     *
     * @param contentTypes The content types.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setContentTypes(String[] contentTypes) {

        mContentTypes = contentTypes;
        return this;
    }

    /**
     * Set the recommendation actions.
     *
     * @param actions The actions.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setActions(ArrayList<Integer> actions) {

        mActions = actions;
        return this;
    }

    /**
     * Set the last watch date time.
     *
     * @param lastWatchedDateTime The date time of the last watch.
     * @return The {@link RecommendationBuilder} instance.
     */
    RecommendationBuilder setLastWatchedDateTime(long lastWatchedDateTime) {

        mLastWatchedDateTime = lastWatchedDateTime;
        return this;
    }
}
