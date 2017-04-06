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
 * Factory class to access instances of {@link ADataDownloader}.
 */
public class DataDownloaderFactory {

    /**
     * Exception class created for initialization failure of the downloader
     */
    public static class DownloaderInitializationFailedException extends Exception {

        /**
         * Constructs a {@link com.amazon.dataloader.datadownloader.DataDownloaderFactory
         * .DownloaderInitializationFailedException}.
         *
         * @param msg   The error message.
         * @param cause The cause of the execption.
         */
        public DownloaderInitializationFailedException(String msg, Throwable cause) {

            super(msg, cause);
        }

    }

    /**
     * Debug tag.
     */
    private static final String TAG = DataDownloaderFactory.class.getSimpleName();

    /**
     * Creates a {@link ADataDownloader} instance. This method takes the full package name of the
     * data downloader class to be instantiated, calls the {@link ADataDownloader#isSingleton()}
     * method to check if the class instance is a singleton or not. It then accordingly calls the
     * {@link ADataDownloader#createInstance(Context)} method or the {@link
     * ADataDownloader#getInstance(Context)} method of the class to receive its instance, and then
     * returns that instance. This method uses reflection to make method calls and hence is
     * expensive, its best to save the instance instead of calling this factory again and again.
     *
     * @param context                 The application context.
     * @param dataDownloaderClassPath The fully qualified class path of the URL generator class to
     *                                be created.
     * @return The {@link ADataDownloader} instance.
     * @throws DownloaderInitializationFailedException An exception generated while creating the
     *                                                 data downloader.
     */
    public static ADataDownloader createDataDownloader(Context context, String
            dataDownloaderClassPath) throws DownloaderInitializationFailedException {

        try {
            return (ADataDownloader) AObjectCreator.fetchInstanceViaReflection(
                    context, dataDownloaderClassPath);

        }
        catch (Exception e) {
            throw new DownloaderInitializationFailedException("Could not register data downloader"
                    + " for classpath " + dataDownloaderClassPath, e);
        }
    }
}
