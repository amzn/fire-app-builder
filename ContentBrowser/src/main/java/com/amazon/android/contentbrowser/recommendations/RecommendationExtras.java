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

/**
 * The class contains constants that are used by recommendations. The descriptions of the constants
 * explain how they might be used within the "Recommended By Your Apps" row of the Fire TV.
 */
class RecommendationExtras {

    /**
     * The business name of the application. Stored in the Notification's 'extras' Bundle. Business
     * name will be displayed as part of the text label of the 'Watch Now with ….'UI button.  It
     * will be truncated past the length of 15 characters.
     */
    static final String EXTRA_APP_NAME = "com.amazon.extra.DISPLAY_NAME";

    /**
     * The maturity rating for the recommendation. Stored in the Notification's 'extras' Bundle.
     * This rating will be used by the Parental Control settings on the Fire TV device to
     * determine if a PIN is required for content playback. Any recommendation without this
     * value will be treated as Matured content and may require a PIN subject to the Parental
     * Control settings on the device. Guidelines to how maturity ratings across different
     * markets will be treated can be found at amazon.de/pin or amazon.co.uk/pin or amazon.co
     * .jp/pin or amazon.com/pin under viewing restrictions for supported values.
     */
    static final String EXTRA_MATURITY_RATING = "com.amazon.extra.MATURITY_RATING_TAG";

    /**
     * The rank of a recommendation. Stored in the Notification's 'extras' Bundle. This rank
     * will be priority of the recommendation. 0 is the highest priority. The recommendation
     * position in the row will be decided based on this value.
     */
    static final String EXTRA_RANK = "com.amazon.extra.RANK";

    /**
     * The long description of the recommendation. Stored in the Notification's 'extras' Bundle. It
     * is used to show the detail description of the recommendation when any recommendation is
     * selected by the user. The description will be truncated after 512 characters.
     */
    static final String EXTRA_LONG_DESCRIPTION = "com.amazon.extra.LONG_DESCRIPTION";

    /**
     * The content ID for the recommendation. Stored in the Notification's 'extras' Bundle. This is
     * not required for V0 release. For catalog integrated apps, this content ID must be the same
     * as the ID used for catalog integration. It's an ID that's persistent across reboots.
     */
    static final String EXTRA_AMAZON_CONTENT_ID = "com.amazon.extra.CONTENT_ID";

    /**
     * Add tags to a recommendation. Stored in the Notification's 'extras' Bundle. The tags are
     * used at display time to provide metadata to the Fire TV Home Screen so it can know how to
     * display the recommendations. Separate each tag with a comma, semicolon or colon. Examples
     * of content category include: movie, tv, short (specific strings are known to services).
     */
    static final String EXTRA_TAGS = "com.amazon.extra.TAGS";

    /**
     * The live content flag for the recommendation. Stored in the Notification's 'extras' Bundle.
     * This will be used to show that the content is live. A value of 0 means the content is not
     * live (the default value); value of 1 means the content is live.
     */
    static final String EXTRA_LIVE_CONTENT = "com.amazon.extra.LIVE_CONTENT";

    /**
     * The release date of the recommendation's content. Stored in the Notifications 'extras'
     * Bundle. The date will be displayed if release was in last 365 days, otherwise the year will
     * be displayed.
     */
    static final String EXTRA_CONTENT_RELEASE_DATE = "com.amazon.extra.CONTENT_RELEASE_DATE";

    /**
     * The closed caption flag for the recommendation. Stored in the Notification's 'extras' Bundle.
     * This will be used to display if the caption option available or not. A value of 0 means
     * captioning is not available (default value); a value of 1 means captioning is available for
     * the content.
     */
    static final String EXTRA_CAPTION_OPTION = "com.amazon.extra.CONTENT_CAPTION_AVAILABILITY";

    /**
     * The content's customer rating to be displayed in the recommendation. Stored in the
     * Notification's 'extras' Bundle. This will be used to display the customer rating; a value
     * between 0 and 5.
     */
    static final String EXTRA_CONTENT_CUSTOMER_RATING = "com.amazon.extra.CONTENT_CUSTOMER_RATING";

    /**
     * The customer rating count for the recommendation. Stored in the Notification's 'extras'
     * Bundle. This will be used to display customer rating count in parentheses next to the star
     * rating for the recommendation.
     */
    static final String EXTRA_CONTENT_CUSTOMER_RATING_COUNT =
            "com.amazon.extra.CONTENT_CUSTOMER_RATING_COUNT";

    /**
     * The IMDB ID for the recommendation's content. Stored in the Notification's 'extras' Bundle.
     */
    static final String EXTRA_IMDB_ID = "com.amazon.extra.IMDB_ID";

    /**
     * A URL to the content's preview video or image. Stored in the Notification's 'extras'
     * Bundle. This will be used to play the preview of the recommended content.
     */
    static final String EXTRA_PREVIEW_VIDEO_URL = "com.amazon.extra.PREVIEW_URL";

    /**
     * The recommendation's content's start time. Stored in the Notification's 'extras' Bundle. This
     * will be used to show the start time of the recommendation. For live content only.
     */
    static final String EXTRA_START_TIME = "com.amazon.extra.CONTENT_START_TIME";

    /**
     * The recommendation's content's end time. Stored in the Notification's 'extras' Bundle. This
     * will be used to show the end time of the recommendation. For live content only.
     */
    static final String EXTRA_END_TIME = "com.amazon.extra.CONTENT_END_TIME";

    /**
     * The recommendation's action options. Helps determine the context menu options displayed for
     * each recommendation.
     */
    static final String EXTRA_ACTION_OPTION = "com.amazon.extra.ACTION_OPTION";

    /**
     * The last time the recommended item was watched.
     */
    static final String EXTRA_LAST_WATCHED = "com.amazon.extra.LAST_WATCHED_DATETIME";
}
