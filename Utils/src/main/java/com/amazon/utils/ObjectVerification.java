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
package com.amazon.utils;

/**
 * Utility methods for verifications.
 */
public class ObjectVerification {

    /**
     * Validation method to check if an object is null. The method returns the same object if it is
     * not null.
     *
     * @param t   Object to be validated.
     * @param ex  The exception message.
     * @param <T> The type to be used in this method.
     * @return An object received.
     * @throws IllegalArgumentException if the argument is null.
     */
    public static <T> T notNull(T t, String ex) {

        if (t == null) {
            throw new IllegalArgumentException(ex);
        }
        return t;
    }
}
