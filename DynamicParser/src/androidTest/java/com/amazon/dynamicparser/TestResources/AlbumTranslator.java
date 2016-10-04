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
 * A class used for testing that extends {@link AModelTranslator} to translate {@link AlbumModel}.
 */
public class AlbumTranslator extends AModelTranslator<AlbumModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public AlbumModel instantiateModel() {

        return new AlbumModel();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(AlbumModel model, String field, Object value) {

        boolean isValueString = value instanceof String;

        switch (field) {
            case AlbumModel.ID_FIELD:
                model.setId(isValueString ? Integer.parseInt(value.toString()): (int) value);
                break;
            case AlbumModel.TITLE_FIELD:
                model.setTitle((String) value);
                break;
            case AlbumModel.USER_ID_FIELD:
                model.setUserId(isValueString ? Integer.parseInt(value.toString()): (int) value);
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
    public boolean validateModel(AlbumModel model) {

        return model.getId() > 0 && model.getUserId() > 0
                && model.getTitle() != null && !model.getTitle().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return AlbumTranslator.class.getSimpleName();
    }
}
