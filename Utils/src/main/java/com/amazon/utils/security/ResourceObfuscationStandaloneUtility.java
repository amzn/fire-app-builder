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

/**
 * Utility to encrypt keys out of the app
 * Usage:
 * Return the text to be encrypted from method {@link #getPlainTextToEncrypt()}
 * Run the main method and copy the output from command line output.
 * NOTE: {@link #getRandomStringsForKey} and {@link #getRandomStringsForIv()} return the random
 * keys which are used to generate the encryption key and Iv. You can use the default ones, but
 * it is always more secure to provide your own random keys. You need to remember or store these
 * keys if you ever want to decrypt the text encrypted with these keys.
 */
public class ResourceObfuscationStandaloneUtility {

    /**
     * Main method which performs the actual encryption/decryption and provides the result.
     *
     * @param args The arguments required by main method, are not really used anywhere.
     */
    public static void main(String[] args) {

        String plainKeyToEncrypt = getPlainTextToEncrypt();

        try {
            String progEncrypted = ResourceObfuscator.obfuscate(plainKeyToEncrypt,
                                                                getRandomStringsForKey(),
                                                                getRandomStringsForIv());

            System.out.println("Encrypted version of plain text " + plainKeyToEncrypt + " is " +
                                       progEncrypted);
        }
        catch (Exception e) {
            System.out.println("Could not encrypt key " + plainKeyToEncrypt);
        }
    }

    /**
     * Use this method provide the plain text to be encrypted.
     *
     * @return The plain text to be encrypted.
     */
    private static String getPlainTextToEncrypt() {

        return "Encrypt_this_text";
    }

    /**
     * Random keys used to generate encryption key. Update this method to return your custom random
     * strings.
     *
     * @return The random strings used to generate encryption key.
     */
    private static String[] getRandomStringsForKey() {

        return new String[]{
                "calypso",
                "something_random",
                "more_random_stuff"
        };
    }

    /**
     * Random keys used to generate encryption Iv. Update this method to return your custom random
     * strings.
     *
     * @return The random strings used to generate encryption Iv.
     */
    private static String[] getRandomStringsForIv() {

        return new String[]{
                "dadadadada",
                "qwertfghyufksld"
        };
    }
}
