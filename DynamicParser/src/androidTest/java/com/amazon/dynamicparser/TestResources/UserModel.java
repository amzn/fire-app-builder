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

/**
 * A model used to test the {@link com.amazon.dynamicparser.DynamicParser} with "10UsersFeed.json."
 */
public class UserModel {

    /**
     * Constant for the name field.
     */
    public static final String NAME_FIELD = "name";
    /**
     * Constant for the email field.
     */
    public static final String EMAIL_FIELD = "email";
    /**
     * Constant for the company field.
     */
    public static final String COMPANY_FIELD = "company";
    /**
     * Constant for the address field.
     */
    public static final String ADDRESS_FIELD = "address";

    private String name;
    private String email;
    private String company;
    private String address;

    /**
     * Return the name.
     *
     * @return The name.
     */
    public String getName() {

        return name;
    }

    /**
     * Set the name.
     *
     * @param name The name.
     */
    public void setName(String name) {

        this.name = name;
    }

    /**
     * Return the email.
     *
     * @return The email.
     */
    public String getEmail() {

        return email;
    }

    /**
     * Set the email.
     *
     * @param email The email.
     */
    public void setEmail(String email) {

        this.email = email;
    }

    /**
     * Get the company.
     *
     * @return The company.
     */
    public String getCompany() {

        return company;
    }

    /**
     * Set the company.
     *
     * @param company The company.
     */
    public void setCompany(String company) {

        this.company = company;
    }

    /**
     * Get the address.
     *
     * @return The address.
     */
    public String getAddress() {

        return address;
    }

    /**
     * Set the address.
     *
     * @param address The address.
     */
    public void setAddress(String address) {

        this.address = address;
    }
}
