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
package com.amazon.analytics;

/**
 * This class defines a set of constants that are used to pass data between the app and the
 * interface implementation. The constants describe different actions, attributes, metrics,
 * and screens.
 */
public class AnalyticsTags {

    /////////////////////////////////////////////////////////////////////////
    //                      Customizable actions.                          //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Customizable action for when the app starts.
     */
    public static final String ACTION_START_APP = "ACTION_START_APP";

    /**
     * Customizable action for performing in-app searches.
     */
    public static final String ACTION_SEARCH = "ACTION_SEARCH";

    /**
     * Customizable action for when an error occurs.
     */
    public static final String ACTION_ERROR = "ACTION_ERROR";

    /**
     * Customizable action for playing a video from play from details page.
     */
    public static final String ACTION_PLAY_VIDEO = "ACTION_PLAY_VIDEO";

    /**
     * Customizable action for starting/resuming playback of a video.
     */
    public static final String ACTION_PLAYBACK_STARTED = "ACTION_PLAYBACK_STARTED";

    /**
     * Customizable action for starting a purchase.
     */
    public static final String ACTION_PURCHASE_INITIATED = "ACTION_PURCHASE_INITIATED";

    /**
     * Customizable action for completing a purchase.
     */
    public static final String ACTION_PURCHASE_COMPLETE = "ACTION_PURCHASE_COMPLETE";

    /**
     * Customizable action for when an ad starts playback.
     */
    public static final String ACTION_AD_START = "ACTION_AD_START";

    /**
     * Customizable action for when an ad completes playback.
     */
    public static final String ACTION_AD_COMPLETE = "ACTION_AD_COMPLETE";

    /**
     * Customizable action for rewinding video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_REWIND = "ACTION_PLAYBACK_CONTROL_REWIND";

    /**
     * Customizable action for fast forwarding video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_FF = "ACTION_PLAYBACK_CONTROL_FF";

    /**
     * Customizable action for going to previous video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PRE = "ACTION_PLAYBACK_CONTROL_PRE";

    /**
     * Customizable action for going to next video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_NEXT = "ACTION_PLAYBACK_CONTROL_NEXT";

    /**
     * Customizable action for pausing video playback.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PAUSE = "ACTION_PLAYBACK_CONTROL_PAUSE";

    /**
     * Customizable action for playing video playback.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PLAY = "ACTION_PLAYBACK_CONTROL_PLAY";

    /**
     * Customizable action for toggling closed captions on and off.
     */
    public static final String ACTION_PLAYBACK_CONTROL_TOGGLE_CC =
            "ACTION_PLAYBACK_CONTROL_TOGGLE_CC";

    /**
     * Customizable action for the multi action button of the playback overlay.
     */
    public static final String ACTION_PLAYBACK_CONTROL_MULTI_ACTION =
            "ACTION_PLAYBACK_CONTROL_MULTI_ACTION";

    /**
     * Customizable action for finishing video playback.
     */
    public static final String ACTION_PLAYBACK_FINISHED = "ACTION_PLAYBACK_FINISHED";

    /**
     * Customizable action for starting a buffer event.
     */
    public static final String ACTION_PLAYBACK_BUFFER_START = "ACTION_PLAYBACK_BUFFER_START";

    /**
     * Customizable action for ending a buffer event.
     */
    public static final String ACTION_PLAYBACK_BUFFER_END = "ACTION_PLAYBACK_BUFFER_END";

    /**
     * Customizable action for clicking recommended content.
     */
    public static final String ACTION_RECOMMENDED_CONTENT_CLICKED =
            "ACTION_RECOMMENDED_CONTENT_CLICKED";

    /**
     * Customizable action for request received from Launcher to broadcast app authentication status
     */
    public static final String ACTION_APP_AUTHENTICATION_STATUS_REQUESTED_BY_LAUNCHER =
            "ACTION_APP_AUTHENTICATION_STATUS_REQUESTED_BY_LAUNCHER";

    /**
     * Customizable action for request received from Launcher to play content
     */
    public static final String ACTION_REQUEST_FROM_LAUNCHER_TO_PLAY_CONTENT =
            "ACTION_REQUEST_FROM_LAUNCHER_TO_PLAY_CONTENT";
    /**
     * Customizable action for broadcasts sent with app authentication status
     */
    public static final String ACTION_APP_AUTHENTICATION_STATUS_BROADCAST =
            "ACTION_APP_AUTHENTICATION_STATUS_BROADCAST";
    /**
     * Customizable action for DeleteRecommendationService calls
     */
    public static final String ACTION_DELETE_RECOMMENDATION_SERVICE_CALLED =
            "ACTION_DELETE_RECOMMENDATION_SERVICE_CALLED";

    /**
     * Customizable action for updating global recommendations
     */
    public static final String ACTION_UPDATE_GLOBAL_RECOMMENDATIONS =
            "ACTION_UPDATE_GLOBAL_RECOMMENDATIONS";

    /**
     * Customizable action for dismissing recommendation on content complete
     */
    public static final String ACTION_DISMISS_RECOMMENDATION_ON_CONTENT_COMPLETE =
            "ACTION_DISMISS_RECOMMENDATION_ON_CONTENT_COMPLETE";

    /**
     * Customizable action for removing expired recommendations
     */
    public static final String ACTION_REMOVE_EXPIRED_RECOMMENDATIONS =
            "ACTION_REMOVE_EXPIRED_RECOMMENDATIONS";

    /**
     * Customizable action for updating related recommendations
     */
    public static final String ACTION_UPDATE_RELATED_RECOMMENDATIONS =
            "ACTION_UPDATE_RELATED_RECOMMENDATIONS";

    /**
     * Customizable action for user authentication requested.
     */
    public static final String ACTION_AUTHENTICATION_REQUESTED =  "ACTION_AUTHENTICATION_REQUESTED";

    /**
     * Customizable action for user authentication succeeded.
     */
    public static final String ACTION_AUTHENTICATION_SUCCEEDED =  "ACTION_AUTHENTICATION_SUCCEEDED";

    /**
     * Customizable action for user authentication failed.
     */
    public static final String ACTION_AUTHENTICATION_FAILED =  "ACTION_AUTHENTICATION_FAILED";

    /**
     * Customizable action for user Authorization requested.
     */
    public static final String ACTION_AUTHORIZATION_REQUESTED =  "ACTION_AUTHORIZATION_REQUESTED";

    /**
     * Customizable action for user Authorization succeeded.
     */
    public static final String ACTION_AUTHORIZATION_SUCCEEDED =  "ACTION_AUTHORIZATION_SUCCEEDED";

    /**
     * Customizable action for user Authorization failed.
     */
    public static final String ACTION_AUTHORIZATION_FAILED =  "ACTION_AUTHORIZATION_FAILED";

    /**
    * Customizable action for user log out requested.
    */
    public static final String ACTION_LOG_OUT_REQUESTED =  "ACTION_LOG_OUT_REQUESTED";

    /**
    * Customizable action for user log out succeeded.
    */
    public static final String ACTION_LOG_OUT_SUCCEEDED =  "ACTION_LOG_OUT_SUCCEEDED";

    /**
    * Customizable action for user log out failed.
    */
    public static final String ACTION_LOG_OUT_FAILED =  "ACTION_LOG_OUT_FAILED";

    /////////////////////////////////////////////////////////////////////////
    //                      Customizable attributes.                       //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Customizable attribute for the app's name.
     */
    public static final String ATTRIBUTE_APP_NAME = "ATTRIBUTE_APP_NAME";

    /**
     * Customizable attribute for the current minute that an action took place.
     */
    public static final String ATTRIBUTE_MINUTE = "ATTRIBUTE_MINUTE";

    /**
     * Customizable attribute for the current hour that an action took place.
     */
    public static final String ATTRIBUTE_HOUR = "ATTRIBUTE_HOUR";

    /**
     * Customizable attribute for the current day that an action took place.
     */
    public static final String ATTRIBUTE_DAY = "ATTRIBUTE_DAY";

    /**
     * Customizable attribute for the current date that an action took place.
     */
    public static final String ATTRIBUTE_DATE = "ATTRIBUTE_DATE";

    /**
     * Customizable attribute for the device platform.
     */
    public static final String ATTRIBUTE_PLATFORM = "ATTRIBUTE_PLATFORM";

    /**
     * Customizable attribute for search term used in a search action.
     */
    public static final String ATTRIBUTE_SEARCH_TERM = "ATTRIBUTE_SEARCH_TERM";

    /**
     * Customizable attribute for the error message when an error happens.
     */
    public static final String ATTRIBUTE_ERROR_MSG = "ATTRIBUTE_ERROR_MSG";

    /**
     * Customizable attribute for the button that started the playback in the details view.
     */
    public static final String ATTRIBUTE_PLAY_SOURCE = "ATTRIBUTE_PLAY_SOURCE";

    /**
     * Customizable attribute for the purchase type.
     */
    public static final String ATTRIBUTE_PURCHASE_TYPE = "ATTRIBUTE_PURCHASE_TYPE";

    /**
     * Customizable attribute for the purchase result.
     */
    public static final String ATTRIBUTE_PURCHASE_RESULT = "ATTRIBUTE_PURCHASE_RESULT";

    /**
     * Customizable attribute for the SKU of an item being purchased.
     */
    public static final String ATTRIBUTE_PURCHASE_SKU = "ATTRIBUTE_PURCHASE_SKU";

    /**
     * Customizable attribute for title of the content.
     */
    public static final String ATTRIBUTE_TITLE = "ATTRIBUTE_TITLE";

    /**
     * Customizable attribute for publisher of the content.
     */
    public static final String ATTRIBUTE_PUBLISHER_NAME = "ATTRIBUTE_PUBLISHER_NAME";

    /**
     * Customizable attribute for title of the program/show the content is a part of.
     */
    public static final String ATTRIBUTE_PROGRAM_TITLE = "ATTRIBUTE_PROGRAM_TITLE";

    /**
     * Customizable attribute for the season number of the content.
     */
    public static final String ATTRIBUTE_SEASON_NUMBER = "ATTRIBUTE_SEASON_NUMBER";

    /**
     * Customizable attribute for episode number of the content.
     */
    public static final String ATTRIBUTE_EPISODE_NUMBER = "ATTRIBUTE_EPISODE_NUMBER";

    /**
     * Customizable attribute for genre of the content.
     */
    public static final String ATTRIBUTE_GENRE = "ATTRIBUTE_GENRE";

    /**
     * Customizable attribute for airdate of the content.
     */
    public static final String ATTRIBUTE_AIRDATE = "ATTRIBUTE_AIRDATE";

    /**
     * Customizable attribute for segment number of the content. A segment is a section of
     * content delineated from other sections by advertisements during playback.
     */
    public static final String ATTRIBUTE_SEGMENT_NUMBER = "ATTRIBUTE_SEGMENT_NUMBER";

    /**
     * Customizable attribute for the total number of segments of the content.
     */
    public static final String ATTRIBUTE_NUMBER_OF_SEGMENTS = "ATTRIBUTE_NUMBER_OF_SEGMENTS";

    /**
     * Customizable attribute for the subtitle of the content.
     */
    public static final String ATTRIBUTE_SUBTITLE = "ATTRIBUTE_SUBTITLE";

    /**
     * Customizable attribute for the video type of the content.
     */
    public static final String ATTRIBUTE_VIDEO_TYPE = "ATTRIBUTE_VIDEO_TYPE";

    /**
     * Customizable attribute for the duration of the content.
     */
    public static final String ATTRIBUTE_VIDEO_DURATION = "ATTRIBUTE_VIDEO_DURATION";

    /**
     * Customizable attribute for the duration of the ad.
     */
    public static final String ATTRIBUTE_AD_DURATION = "ATTRIBUTE_AD_DURATION";

    /**
     * Customizable attribute for the ID of the content.
     */
    public static final String ATTRIBUTE_VIDEO_ID = "ATTRIBUTE_VIDEO_ID";

    /**
     * Customizable attribute for the ad ID.
     */
    public static final String ATTRIBUTE_AD_ID = "ATTRIBUTE_AD_ID";

    /**
     * Customizable attribute for advertisement type. Examples: pre-roll, mid-roll, post-roll
     */
    public static final String ATTRIBUTE_ADVERTISEMENT_TYPE = "ATTRIBUTE_ADVERTISEMENT_TYPE";

    /**
     * Customizable attribute to distinguish advertisement streams from content streams.
     */
    public static final String ATTRIBUTE_CLASSIFICATION_TYPE = "ATTRIBUTE_CLASSIFICATION_TYPE";

    /**
     * Customizable attribute for determining content stream is live.
     */
    public static final String ATTRIBUTE_LIVE_FEED = "ATTRIBUTE_LIVE_FEED";

    /**
     * Customizable attribute for the number of seconds of ad watched.
     */
    public static final String ATTRIBUTE_AD_SECONDS_WATCHED = "ATTRIBUTE_AD_SECONDS_WATCHED";

    /**
     * Customizable attribute for the number of seconds of video watched.
     */
    public static final String ATTRIBUTE_VIDEO_SECONDS_WATCHED = "ATTRIBUTE_VIDEO_SECONDS_WATCHED";

    /**
     * Customizable attribute for the current position of the video playback
     */
    public static final String ATTRIBUTE_VIDEO_CURRENT_POSITION =
            "ATTRIBUTE_VIDEO_CURRENT_POSITION";

    /**
     * Customizable attribute for the flag content available, this is requested by launcher
     */
    public static final String ATTRIBUTE_CONTENT_AVAILABLE = "ATTRIBUTE_CONTENT_AVAILABLE";

    /**
     * Customizable attribute for the source of request for the content to be played
     */
    public static final String ATTRIBUTE_REQUEST_SOURCE = "ATTRIBUTE_REQUEST_SOURCE";

    /**
     * Customizable attribute for app authentication status
     */
    public static final String ATTRIBUTE_APP_AUTHENTICATION_STATUS =
            "ATTRIBUTE_APP_AUTHENTICATION_STATUS";
    /**
     * Customizable attribute for expired recommendations count
     */
    public static final String ATTRIBUTE_EXPIRED_RECOMMENDATIONS_COUNT =
            "ATTRIBUTE_EXPIRED_RECOMMENDATIONS_COUNT";
    /**
     * Customizable attribute for Recommendation Id
     */
    public static final String ATTRIBUTE_RECOMMENDATION_ID = "ATTRIBUTE_RECOMMENDATION_ID";

    /**
     * Customizable attribute for the Authentication submitted
     */
    public static final String ATTRIBUTE_AUTHENTICATION_SUBMITTED =
            "ATTRIBUTE_AUTHENTICATION_SUBMITTED";

    /**
     * Customizable attribute for the Authentication success
     */
    public static final String ATTRIBUTE_AUTHENTICATION_SUCCESS =
            "ATTRIBUTE_AUTHENTICATION_SUCCESS";

    /**
     * Customizable attribute for the Authentication failure
     */
    public static final String ATTRIBUTE_AUTHENTICATION_FAILURE =
            "ATTRIBUTE_AUTHENTICATION_FAILURE";

    /**
     * Customizable attribute for the Authentication failure reason
     */
    public static final String ATTRIBUTE_AUTHENTICATION_FAILURE_REASON =
            "ATTRIBUTE_AUTHENTICATION_FAILURE_REASON";

    /**
     * Customizable attribute for the Logout submitted
     */
    public static final String ATTRIBUTE_LOGOUT_SUBMITTED =
            "ATTRIBUTE_LOGOUT_SUBMITTED";

    /**
     * Customizable attribute for the Logout success
     */
    public static final String ATTRIBUTE_LOGOUT_SUCCESS =
            "ATTRIBUTE_LOGOUT_SUCCESS";

    /**
     * Customizable attribute for the Logout failure
     */
    public static final String ATTRIBUTE_LOGOUT_FAILURE =
            "ATTRIBUTE_LOGOUT_FAILURE";

    /**
     * Customizable attribute for the Logout Failure reason
     */
    public static final String ATTRIBUTE_LOGOUT_FAILURE_REASON =
            "ATTRIBUTE_LOGOUT_FAILURE_REASON";

    /**
     * Customizable attribute for the Authorization submitted
     */
    public static final String ATTRIBUTE_AUTHORIZATION_SUBMITTED =
            "ATTRIBUTE_AUTHORIZATION_SUBMITTED";

    /**
     * Customizable attribute for the Authorization success
     */
    public static final String ATTRIBUTE_AUTHORIZATION_SUCCESS =
            "ATTRIBUTE_AUTHORIZATION_SUCCESS";

    /**
     * Customizable attribute for the Authorization failure
     */
    public static final String ATTRIBUTE_AUTHORIZATION_FAILURE =
            "ATTRIBUTE_AUTHORIZATION_FAILURE";

    /**
     * Customizable attribute for the Authorization failure reason
     */
    public static final String ATTRIBUTE_AUTHORIZATION_FAILURE_REASON =
            "ATTRIBUTE_AUTHORIZATION_FAILURE_REASON";

    /////////////////////////////////////////////////////////////////////////
    //                  Non-Customizable screen names.                     //
    /////////////////////////////////////////////////////////////////////////

    /**
     * The browse screen.
     */
    public static final String SCREEN_BROWSE = "SCREEN_BROWSE";

    /**
     * The splash screen.
     */
    public static final String SCREEN_SPLASH = "SCREEN_SPLASH";

    /**
     * The details screen.
     */
    public static final String SCREEN_DETAILS = "SCREEN_DETAILS";

    /**
     * The playback screen.
     */
    public static final String SCREEN_PLAYBACK = "SCREEN_PLAYBACK";

    /**
     * The search screen.
     */
    public static final String SCREEN_SEARCH = "SCREEN_SEARCH";

    /////////////////////////////////////////////////////////////////////////
    //                  Non-Customizable other constants.                  //
    /////////////////////////////////////////////////////////////////////////

    /**
     * Constant for logging.
     */
    private static final String TAG = AnalyticsTags.class.getSimpleName();

    /**
     * A tag for an action. An action is performed by the end user.
     */
    public static final String ACTION_NAME = "action";

    /**
     * A tag for attributes. Attributes describe specific aspects of an action.
     */
    public static final String ATTRIBUTES = "attributes";

}
