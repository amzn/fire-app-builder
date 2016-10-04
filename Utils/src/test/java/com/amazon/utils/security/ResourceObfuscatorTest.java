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
package com.amazon.utils.security;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

/**
 * Tests for {@link ResourceObfuscator}
 */
public class ResourceObfuscatorTest {

    private String expectedPlainString = "Encrypt_this_text";
    private String expectedObfuscatedString = "MVfm5qWBLRaEOgocp2ovMaAr0/pB7Pan9ijvQ8MtP3k=";

    /**
     * Test for {@link ResourceObfuscator#obfuscate(String, String[], String[])}
     */
    @Test
    public void testObfuscate() {

        String actualObfuscatedString = null;
        try {
            actualObfuscatedString = ResourceObfuscator.obfuscate(expectedPlainString,
                                                                  getRandomStringsForKey(),
                                                                  getRandomStringsForIv());
        }
        catch (Exception e) {
            fail(e.getMessage());
        }
        assertEquals(actualObfuscatedString, expectedObfuscatedString);
    }



    /**
     * Random keys used to generate encryption key.
     *
     * @return random strings used to generate encryption key.
     */
    private static String[] getRandomStringsForKey() {

        return new String[]{
                "calypso",
                "something_random",
                "more_random_stuff"
        };
    }

    /**
     * Random keys used to generate encryption Iv.
     *
     * @return random strings used to generate encryption Iv.
     */
    private static String[] getRandomStringsForIv() {

        return new String[]{
                "dadadadada",
                "qwertfghyufksld"
        };
    }
}
