/**
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 * http://aws.amazon.com/apache2.0/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.tv.tenfoot.utils;

import com.amazon.android.model.content.Content;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.utils.StringManipulation;

import android.content.Context;

/**
 * A helper class that performs operations using the {@link Content} class that are needed
 * throughout the TVUIComponent.
 */
public class ContentHelper {

    /**
     * A constant to get the season number from a Content's extras map.
     */
    public static final String SEASON_NUMBER = "seasonNumber";

    /**
     * A constant to get the episode number from a Content's extras map.
     */
    public static final String EPISODE_NUMBER = "episodeNumber";

    /**
     * Gets a string to display as the subtitle on the card image view. We first try to get the
     * content's season and episode. If no season and episode are present, we default to using
     * the content's subtitle, which may also not be present, in which case we return an empty
     * string.
     *
     * @param context The context.
     * @param content The content.
     * @return The season and episode of the content, the subtitle of the content, or an empty
     * string if the content has no season and episode or subtitle.
     */
    public static String getCardViewSubtitle(Context context, Content content) {

        String season = (String) content.getExtraValue(SEASON_NUMBER);
        String episode = (String) content.getExtraValue(EPISODE_NUMBER);

        // Add the season and episode if they exist.
        if (!StringManipulation.isNullOrEmpty(season) && !StringManipulation.isNullOrEmpty(episode)) {

            return context.getString(R.string.season) + " " + season + " "
                    + context.getString(R.string.episode) + " " + episode;
        }
        // If there is no season and episode, use the content's subtitle.
        if (!StringManipulation.isNullOrEmpty(content.getSubtitle())) {
            return content.getSubtitle();
        }

        return "";
    }

    /**
     * Gets a string to display as a descriptive subtitle, currently used on the {@link com
     * .amazon.android.tv.tenfoot.presenter.DetailsDescriptionPresenter} and {@link com.amazon
     * .android.uamp.ui.PlaybackOverlayFragment}. We first try to get the content's season and
     * episode. If no season and episode are present, we default to using the content's subtitle,
     * which may also not be present, in which case we return an empty string. If season, episode,
     * and subtitle are all present we separate the season/episode and subtitle with a dash (-).
     *
     * @param context The context.
     * @param content The content.
     * @return The season, episode, and subtitle of the content, if present in the content. An
     * empty string if the content has no season and episode or subtitle.
     */
    public static String getDescriptiveSubtitle(Context context, Content content) {

        // Either season/episode, subtitle, or empty.
        String subtitle = getCardViewSubtitle(context, content);

        // Add the content's subtitle if there's an episode/season.
        if (!StringManipulation.isNullOrEmpty(subtitle) &&
                !StringManipulation.isNullOrEmpty(content.getSubtitle()) &&
                !StringManipulation.areStringsEqual(subtitle, content.getSubtitle())) {

            return subtitle + " - " + content.getSubtitle();
        }

        return subtitle;
    }
}
