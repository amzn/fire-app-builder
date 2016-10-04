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
 * DummyContainer}.
 */
public class DummyContainerTranslator extends AModelTranslator<DummyContainer> {

    /**
     * {@inheritDoc}
     */
    @Override
    public DummyContainer instantiateModel() {

        return new DummyContainer();
    }

    /**
     * {@inheritDoc}
     *
     * @param model The {@link Object} to set the field on.
     * @param field The {@link String} describing what member variable to set. If the field is not
     *              found, the value is stored in the mExtras map.
     * @param value The {@link Object} value to set the member variable.
     */
    @Override
    public boolean setMemberVariable(DummyContainer model, String field, Object value) {

        switch (field) {
            case DummyContainer.BOOLEAN_FIELD_NAME:
                model.setBoolean(Boolean.parseBoolean(value.toString()));
                break;
            case DummyContainer.BYTE_FIELD_NAME:
                model.setByte(Byte.parseByte(value.toString()));
                break;
            case DummyContainer.CHAR_FIELD_NAME:
                model.setChar(value.toString().charAt(0));
                break;
            case DummyContainer.DOUBLE_FIELD_NAME:
                model.setDouble(Double.parseDouble(value.toString()));
                break;
            case DummyContainer.FLOAT_FIELD_NAME:
                model.setFloat(Float.parseFloat(value.toString()));
                break;
            case DummyContainer.ID_FIELD_NAME:
                model.setId(Integer.parseInt((String) value));
                break;
            case DummyContainer.LONG_FIELD_NAME:
                model.setLong(Long.parseLong(value.toString()));
                break;
            case DummyContainer.NAME_FIELD_NAME:
                model.setName((String) value);
                break;
            case DummyContainer.SHORT_FIELD_NAME:
                model.setShort(Short.parseShort(value.toString()));
                break;
            default:
                model.setExtraValue(field, value);
        }
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean validateModel(DummyContainer model) {

        return model.getByte() > 0 && model.getChar() > 0 && model.getDouble() > 0
                && model.getFloat() > 0 && model.getId() > 0 && model.getLong() > 0
                && model.getName() != null && !model.getName().isEmpty() && model.getShort() > 0;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DummyContainerTranslator.class.getSimpleName();
    }
}
