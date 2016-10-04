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
package com.amazon.purchase.model;

/**
 * Structure to represent user data involved with purchasing.
 */
public class UserData {

    String userId;
    String marketplace;

    /**
     * Constructor.
     *
     * @param userId      The user id of the user.
     * @param marketplace The marketplace of the user.
     */
    public UserData(String userId, String marketplace) {

        this.userId = userId;
        this.marketplace = marketplace;
    }

    /**
     * Gets the user id.
     *
     * @return The user id.
     */
    public String getUserId() {

        return userId;
    }

    /**
     * Sets the user id.
     *
     * @param userId The user id.
     */
    public void setUserId(String userId) {

        this.userId = userId;
    }

    /**
     * Gets the marketplace.
     *
     * @return The marketplace.
     */
    public String getMarketplace() {

        return marketplace;
    }

    /**
     * Gets the marketplace.
     *
     * @param marketplace The marketplace.
     */
    public void setMarketplace(String marketplace) {

        this.marketplace = marketplace;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {

        if (this == o) return true;
        if (!(o instanceof UserData)) return false;

        UserData userData = (UserData) o;

        if (getUserId() != null ? !getUserId().equals(userData.getUserId()) : userData.getUserId
                () != null)
            return false;
        return getMarketplace() != null ? getMarketplace().equals(userData.getMarketplace()) :
                userData.getMarketplace() == null;

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {

        int result = getUserId() != null ? getUserId().hashCode() : 0;
        result = 31 * result + (getMarketplace() != null ? getMarketplace().hashCode() : 0);
        return result;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "UserData{" +
                "userId='" + userId + '\'' +
                ", marketplace='" + marketplace + '\'' +
                '}';
    }
}
