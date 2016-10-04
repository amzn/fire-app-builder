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

import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Tests for {@link ObjectVerification}
 */
public class ObjectVerificationTest {

    /**
     * Tests Non null object verification.
     */
    @Test
    public void testNotNullWithNonNullObject() {

        String testStr = "testStr";
        assertEquals(ObjectVerification.notNull(testStr, "test exception"), testStr);
    }

    /**
     * Tests null object verification.
     */
    @Test
    public void testNotNullWithNullObject() {

        String testStr = null;
        try {
            ObjectVerification.notNull(testStr, "test exception");
        }
        catch (IllegalArgumentException ex) {
            assertEquals(ex.getMessage(), "test exception");
        }
    }
}
