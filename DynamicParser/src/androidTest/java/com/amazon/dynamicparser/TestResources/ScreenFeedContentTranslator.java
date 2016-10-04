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
 * A class used for testing that extends {@link AModelTranslator} to translate {@link ScreenFeedContent}.
 */
public class ScreenFeedContentTranslator extends AModelTranslator<ScreenFeedContent>{

    /**
     * {@inheritDoc}
     */
    @Override
    public ScreenFeedContent instantiateModel() {

        return new ScreenFeedContent();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(ScreenFeedContent model, String field, Object value) {

        switch (field) {
            case ScreenFeedContent.TITLE_FIELD_NAME:
                model.setTitle(value.toString());
                break;
            case ScreenFeedContent.DESCRIPTION_FIELD_NAME:
                model.setDescription(value.toString());
                break;
            case ScreenFeedContent.GUID_FIELD_NAME:
                model.setGuid(value.toString());
                break;
            case ScreenFeedContent.PUBDATE_FIELD_NAME:
                model.setPubDate(value.toString());
                break;
            case ScreenFeedContent.CATEGORY_FIELD_NAME:
                model.setCategory(value.toString());
                break;
            case ScreenFeedContent.URL_FIELD_NAME:
                model.setUrl(value.toString());
                break;
            case ScreenFeedContent.FILE_SIZE_FIELD_NAME:
                model.setFileSize(Long.parseLong(value.toString()));
                break;
            case ScreenFeedContent.TYPE_FIELD_NAME:
                model.setType(value.toString());
                break;
            case ScreenFeedContent.MEDIUM_FIELD_NAME:
                model.setMedium(value.toString());
                break;
            case ScreenFeedContent.DURATION_FIELD_NAME:
                model.setDuration(Double.parseDouble(value.toString()));
                break;
            case ScreenFeedContent.HEIGHT_FIELD_NAME:
                model.setHeight(Integer.parseInt(value.toString()));
                break;
            case ScreenFeedContent.WIDTH_FIELD_NAME:
                model.setWidth(Integer.parseInt(value.toString()));
                break;
            case ScreenFeedContent.CREDIT_FIELD_NAME:
                model.setCredit(value.toString());
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
    public boolean validateModel(ScreenFeedContent model) {

        return model.getTitle() != null && !model.getTitle().isEmpty()
                && model.getDescription() != null
                && model.getGuid() != null && !model.getGuid().isEmpty()
                && model.getPubDate() != null && !model.getPubDate().isEmpty()
                && model.getCategory() != null && !model.getCategory().isEmpty()
                && model.getUrl() != null && !model.getUrl().isEmpty()
                && model.getType() != null && !model.getType().isEmpty()
                && model.getMedium() != null && !model.getMedium().isEmpty()
                && model.getFileSize() >= 0 && model.getDuration() >= 0
                && model.getHeight() > 0 && model.getWidth() > 0
                && model.getCredit() != null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return ScreenFeedContentTranslator.class.getSimpleName();
    }
}
