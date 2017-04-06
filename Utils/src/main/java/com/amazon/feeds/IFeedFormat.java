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

package com.amazon.feeds;

import com.fasterxml.jackson.core.io.CharacterEscapes;

/**
 * Interface for feed format specification classes.
 */
public interface IFeedFormat {

    /**
     * Initializes object.
     */
    void init();

    /**
     * Get the name of the format.
     *
     * @return The feed format.
     */
    String getFeedFormat();

    /**
     * Get the name of the provider.
     *
     * @return The content provider.
     */
    String getProvider();

    /**
     * Create sample feed with the given number of dummy items.
     *
     * @param size The size of the feed.
     */
    void populate(int size);

    /**
     * Get rules for escaping characters.
     * @return CharacterEscapes object containing escape rules.
     */
    CharacterEscapes getEscapeRules();

    /**
     * Pretty printing options.
     * @return Whether to use pretty print.
     */
    boolean usePrettyPrint();

}
