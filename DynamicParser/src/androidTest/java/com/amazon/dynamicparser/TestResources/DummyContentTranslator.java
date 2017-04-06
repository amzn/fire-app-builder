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

import org.json.JSONException;

import android.util.Log;

/**
 * A class used for testing that extends {@link AModelTranslator} to translate {@link DummyContent}.
 */
public class DummyContentTranslator extends AModelTranslator<DummyContent> {

    private static final String TAG = DummyContentTranslator.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    public DummyContent instantiateModel() {

        return new DummyContent();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(DummyContent model, String field, Object value) {

        switch (field) {
            case DummyContent.ID_FIELD_NAME:
                model.setId(value.toString());
                break;
            case DummyContent.TITLE_FIELD_NAME:
                model.setTitle(value.toString());
                break;
            case DummyContent.SUBTITLE_FIELD_NAME:
                model.setSubtitle(value.toString());
                break;
            case DummyContent.URL_FIELD_NAME:
                model.setUrl(value.toString());
                break;
            case DummyContent.DESCRIPTION_FIELD_NAME:
                model.setDescription(value.toString());
                break;
            case DummyContent.BACKGROUND_IMAGE_URL_FIELD_NAME:
                model.setBackgroundImageUrl(value.toString());
                break;
            case DummyContent.CARD_IMAGE_URL_FIELD_NAME:
                model.setCardImageUrl(value.toString());
                break;
            case DummyContent.TAGS_FIELD_NAME:
                try {
                    model.setTags(value.toString());
                }
                catch (JSONException e) {
                    Log.e(TAG, "Error translating tags: " + value.toString(), e);
                }
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
    public boolean validateModel(DummyContent model) {

        return !model.getId().equals("0") && model.getTitle() != null && !model.getTitle().isEmpty()
                && model.getUrl() != null && !model.getUrl().isEmpty()
                && model.getDescription() != null && !model.getDescription().isEmpty()
                && model.getBackgroundImageUrl() != null && !model.getBackgroundImageUrl().isEmpty()
                && model.getCardImageUrl() != null && !model.getCardImageUrl().isEmpty()
                && model.getTags() != null && !model.getTags().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DummyContentTranslator.class.getSimpleName();
    }
}
