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
 * A class used for testing that extends {@link AModelTranslator} to translate {@link CommentModel}.
 */
public class CommentTranslator extends AModelTranslator<CommentModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public CommentModel instantiateModel() {

        return new CommentModel();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(CommentModel model, String field, Object value) {

        boolean isValueString = value instanceof String;

        switch (field) {
            case CommentModel.BODY_FIELD:
                model.setBody((String) value);
                break;
            case CommentModel.EMAIL_FIELD:
                model.setEmail((String) value);
                break;
            case CommentModel.ID_FIELD:
                model.setId(isValueString ? Integer.parseInt(value.toString()): (int) value);
                break;
            case CommentModel.NAME_FIELD:
                model.setName((String) value);
                break;
            case CommentModel.POST_ID_FIELD:
                model.setPostId(isValueString ? Integer.parseInt(value.toString()) : (int) value);
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
    public boolean validateModel(CommentModel model) {

        return model.getId() > 0 && model.getPostId() > 0
                && model.getName() != null && !model.getName().isEmpty()
                && model.getEmail() != null && !model.getEmail().isEmpty()
                && model.getBody() != null && !model.getBody().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return CommentTranslator.class.getSimpleName();
    }
}
