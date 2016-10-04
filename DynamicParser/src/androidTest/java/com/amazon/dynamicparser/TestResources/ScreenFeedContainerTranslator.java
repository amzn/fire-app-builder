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

import com.amazon.android.model.AModelTranslator;

/**
 * A class used for testing that extends {@link AModelTranslator} to translate {@link
 * ScreenFeedContainer}.
 */
public class ScreenFeedContainerTranslator extends AModelTranslator<ScreenFeedContainer>{

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenFeedContainer instantiateModel() {

        return new ScreenFeedContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(ScreenFeedContainer model, String field, Object value) {

        switch (field) {
            case ScreenFeedContainer.TITLE_FIELD_NAME:
                model.setTitle(value.toString());
                break;
            case ScreenFeedContainer.LINK_FIELD_NAME:
                model.setLink(value.toString());
                break;
            case ScreenFeedContainer.DESCRIPTION_FIELD_NAME:
                model.setDescription(value.toString());
                break;
            case ScreenFeedContainer.DOCS_FIELD_NAME:
                model.setDocs(value.toString());
                break;
            case ScreenFeedContainer.GENERATOR_FIELD_NAME:
                model.setGenerator(value.toString());
                break;
            case ScreenFeedContainer.LAST_BUILD_DATE_FIELD_NAME:
                model.setLastBuildDate(value.toString());
                break;
            case ScreenFeedContainer.TTL_FIELD_NAME:
                model.setTtl(Long.parseLong(value.toString()));
                break;
            default:
                return false;
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateModel(ScreenFeedContainer model) {

        return model.getTitle() != null && !model.getTitle().isEmpty()
                && model.getLink() != null && !model.getLink().isEmpty()
                && model.getDescription() != null && !model.getDescription().isEmpty()
                && model.getDocs() != null && !model.getDocs().isEmpty()
                && model.getGenerator() != null && !model.getGenerator().isEmpty()
                && model.getLastBuildDate() != null && !model.getLastBuildDate().isEmpty()
                && model.getTtl() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return ScreenFeedContentTranslator.class.getSimpleName();
    }
}
