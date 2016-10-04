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
package com.amazon.dataloader.datadownloader;

import android.content.Context;

/**
 * Factory class to access instances of {@link AUrlGenerator}.
 */
public class UrlGeneratorFactory {

    /**
     * Exception class created for failures of UrlGeneratorFactory
     */
    public static class UrlGeneratorInitializationFailedException extends Exception {

        /**
         * {@inheritDoc}
         */
        public UrlGeneratorInitializationFailedException(String msg, Throwable cause) {

            super(msg, cause);
        }

    }

    /**
     * Debug tag.
     */
    private static final String TAG = UrlGeneratorFactory.class.getSimpleName();

    /**
     * Creates a {@link AUrlGenerator} instance. This class takes the full package name of the URL
     * generator class to be instantiated and calls the {@link AUrlGenerator#isSingleton()} method
     * to check if the class instance is Singleton or not. It then accordingly calls the {@link
     * AUrlGenerator#createInstance(Context)} method or the {@link AUrlGenerator#getInstance
     * (Context)}
     * method of the class to receive its instance, and returns that instance. This method uses
     * reflection to make method calls, making it rather expensive. Its recommended to save the
     * instance instead of calling this method again and again.
     *
     * @param context               The application context.
     * @param urlGeneratorClassPath The fully qualified class path of URL generator class to be
     *                              created.
     * @return An instance of {@link AUrlGenerator}.
     * @throws UrlGeneratorInitializationFailedException An exception generated while creating the
     *                                                   URL generator.
     */
    public static AUrlGenerator createUrlGenerator(Context context, String
            urlGeneratorClassPath) throws
            UrlGeneratorInitializationFailedException {

        try {
            return (AUrlGenerator) AObjectCreator.fetchInstanceViaReflection(context,
                                                                             urlGeneratorClassPath);
        }
        catch (Exception e) {
            throw new UrlGeneratorInitializationFailedException("Could not register url generator",
                                                                e);
        }
    }


}
