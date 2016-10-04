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
 * A class used for testing that extends {@link AModelTranslator} to translate {@link UserModel}.
 */
public class UserTranslator extends AModelTranslator<UserModel> {

    /**
     * {@inheritDoc}
     */
    @Override
    public UserModel instantiateModel() {

        return new UserModel();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean setMemberVariable(UserModel model, String field, Object value) {

        switch (field) {
            case UserModel.ADDRESS_FIELD:
                model.setAddress((String) value);
                break;
            case UserModel.COMPANY_FIELD:
                model.setCompany((String) value);
                break;
            case UserModel.EMAIL_FIELD:
                model.setEmail((String) value);
                break;
            case UserModel.NAME_FIELD:
                model.setName((String) value);
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
    public boolean validateModel(UserModel model) {

        return model.getAddress() != null && !model.getAddress().isEmpty()
                && model.getCompany() != null && !model.getCompany().isEmpty()
                && model.getEmail() != null && !model.getEmail().isEmpty()
                && model.getName() != null && !model.getName().isEmpty();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return UserTranslator.class.getSimpleName();
    }
}
