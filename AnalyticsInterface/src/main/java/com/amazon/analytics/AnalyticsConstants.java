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

import java.util.HashMap;
import java.util.Map;

/**
 * This class defines a set of constants that are used to pass data between the app and the
 * interface implementation. The constants describe different actions, attributes, metrics,
 * and screens.
 */
public class AnalyticsConstants {

    /**
     * An action is performed by the end user.
     */
    public static final String ACTION_NAME = "action";

    /**
     * Attributes describe specific aspects of an action.
     */
    public static final String ATTRIBUTES = "attributes";

    /**
     * Metrics describe specific values that are related to an action.
     */
    public static final String METRICS = "metrics";

    /**
     * To be used for configuration values.
     */
    public Map<String, String> config = new HashMap<>();

    /**
     * Action and attributes for when app starts.
     */
    public static final String ACTION_START_APP = "App Start";
    public static final String ATTRIBUTE_APP_NAME = "App";
    public static final String ATTRIBUTE_MINUTE = "Minute";
    public static final String ATTRIBUTE_HOUR = "Hour";
    public static final String ATTRIBUTE_DAY = "Day";
    public static final String ATTRIBUTE_DATE = "Date";
    public static final String ATTRIBUTE_PLATFORM = "Platform";

    /**
     * Search action and attribute.
     */
    public static final String ACTION_SEARCH = "Search";
    public static final String ATTRIBUTE_SEARCH_TERM = "Term";

    /**
     * Error action and attribute.
     */
    public static final String ACTION_ERROR = "Error";
    public static final String ATTRIBUTE_ERROR_MSG = "Message";

    /**
     * Play video action and attributes.
     */
    public static final String ACTION_PLAY_VIDEO = "Play";
    public static final String ATTRIBUTE_PLAY_SOURCE = "Playback Source";

    /**
     * Purchase initiated action and attributes
     */
    public static final String ACTION_PURCHASE_INITIATED = "Purchase Initiated";
    public static final String ATTRIBUTE_PURCHASE_TYPE = "Purchase Type";

    /**
     * Purchase completed action and attributes
     */
    public static final String ACTION_PURCHASE_COMPLETE = "Purchase Complete";
    public static final String ATTRIBUTE_PURCHASE_RESULT = "Purchase Result";
    public static final String ATTRIBUTE_PURCHASE_SKU = "Purchase SKU";

    /**
     * Common Video attributes
     */
    public static final String ATTRIBUTE_TITLE = "Title";
    public static final String ATTRIBUTE_SUBTITLE = "Subtitle";
    public static final String ATTRIBUTE_VIDEO_TYPE = "Type";
    public static final String ATTRIBUTE_VIDEO_ID = "Video ID";

    /**
     * Common Ad attributes
     */
    public static final String ATTRIBUTE_AD_ID = "Ad ID";

    /**
     * Playback and ad ended attributes.
     */
    public static final String ATTRIBUTE_AD_SECONDS_WATCHED = "Ad Seconds";
    public static final String ATTRIBUTE_VIDEO_SECONDS_WATCHED = "Video Seconds";

    /**
     * Actions for ad start and completion.
     */
    public static final String ACTION_AD_START = "Ad Start";
    public static final String ACTION_AD_COMPLETE = "Ad Complete";

    /**
     * Action for rewinding video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_REWIND = "Playback Control Rewind";

    /**
     * Action for fast forwarding video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_FF = "Playback Control Forward";

    /**
     * Action for going to previous video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PRE = "Playback Control Previous";

    /**
     * Action for going to next video.
     */
    public static final String ACTION_PLAYBACK_CONTROL_NEXT = "Playback Control Next";

    /**
     * Action for pausing video playback.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PAUSE = "Playback Control Pause";

    /**
     * Action for playing video playback.
     */
    public static final String ACTION_PLAYBACK_CONTROL_PLAY = "Playback Control Play";

    /**
     * Action for toggling CC.
     */
    public static final String ACTION_PLAYBACK_CONTROL_TOGGLE_CC = "Playback Control Toggle CC";

    /**
     * Action for multi actions.
     */
    public static final String ACTION_PLAYBACK_CONTROL_MULTI_ACTION = "Playback Control Multi " +
            "Action";

    /**
     * Action for finishing video playback.
     */
    public static final String ACTION_PLAYBACK_FINISHED = "Playback Finished";

    /**
     * Action for clicking recommended content
     */
    public static final String ACTION_RECOMMENDED_CONTENT_CLICKED = "Recommended Movie Clicked";

    /**
     * Action for authentication login and attributes.
     */
    public static String ACTION_LOGIN = "Login";
    public static String ATTRIBUTE_ADOBE_PASS_NETWORK = "Network";
    public static String ATTRIBUTE_ADOBE_PASS_MVPD = "MVPD";
    public static String ATTRIBUTE_ADOBE_PASS_AUTHENTICATION_STATUS = "Authentication";
    public static String ATTRIBUTE_ADOBE_PASS_AUTHORIZATION_STATUS = "Authorization";
    public static String ATTRIBUTE_ADOBE_PASS_USER_ID = "User ID";

    /**
     * Screen names.
     */
    public static final String SCREEN_BROWSE = "BROWSE";
    public static final String SCREEN_SPLASH = "SPLASH";
    public static final String SCREEN_DETAILS = "DETAILS";
    public static final String SCREEN_PLAYBACK = "PLAYBACK";
    public static final String SCREEN_ERROR = "ERROR";
    public static final String SCREEN_AUTH = "AUTHENTICATION";
    public static final String SCREEN_SEARCH = "SEARCH";
}
