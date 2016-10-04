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
 * A class used for testing that extends {@link AModelTranslator} to translate {@link PhotoModel}
 */
public class PhotoTranslator extends AModelTranslator<PhotoModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public PhotoModel instantiateModel() {

        return new PhotoModel();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(PhotoModel model, String field, Object value) {

        boolean isValueString = value instanceof String;

        switch (field) {
            case PhotoModel.ALBUM_ID_FIELD_NAME:
                model.setAlbumId(isValueString ? Integer.parseInt(value.toString()) : (int) value);
                break;
            case PhotoModel.ID_FIELD_NAME:
                model.setId(isValueString ? Integer.parseInt(value.toString()) : (int) value);
                break;
            case PhotoModel.TITLE_FIELD_NAME:
                model.setTitle((String) value);
                break;
            case PhotoModel.URL_FIELD_NAME:
                model.setUrl((String) value);
                break;
            case PhotoModel.THUMBNAIL_URL_FIELD_NAME:
                model.setThumbnailUrl((String) value);
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
    public boolean validateModel(PhotoModel model) {

        return model.getAlbumId() > 0 && model.getId() > 0 && model.getThumbnailUrl() != null
                && !model.getThumbnailUrl().isEmpty() && model.getTitle() != null
                && !model.getTitle().isEmpty() && model.getUrl() != null
                && !model.getUrl().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return PhotoTranslator.class.getSimpleName();
    }
}
