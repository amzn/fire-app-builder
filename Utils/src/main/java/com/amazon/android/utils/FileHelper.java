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
package com.amazon.android.utils;

import android.content.Context;
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;

/**
 * This class is a utility class to help with reading files.
 */
public class FileHelper {

    private static final String TAG = FileHelper.class.getSimpleName();

    /**
     * Reads a file from the assets directory and returns the file contents as {@link String}.
     *
     * @param context  The context to access the assets directory from.
     * @param filePath The path of the file in the assets directory.
     * @return The content of the file.
     * @throws IOException if file not found or problem occurred while reading file.
     */
    public static String readFile(Context context, String filePath) throws IOException {

        StringBuilder stringBuilder;

        try {
            stringBuilder = new StringBuilder();
            InputStream inputStream = context.getResources().getAssets().open(filePath);
            BufferedReader in = new BufferedReader(
                    new InputStreamReader(inputStream, Helpers.getDefaultAppCharset()));
            String text;
            while ((text = in.readLine()) != null) {
                stringBuilder.append(text);
            }

            in.close();
        }
        catch (IOException e) {
            Log.e(TAG, "Failed to load content from file " + filePath, e);
            throw new IOException("Failed to load content from file " + filePath, e);
        }

        return stringBuilder.toString();
    }

    /**
     * Tests if a file exists.
     *
     * @param context  The context to access the assets directory from.
     * @param filePath The path of the file in the assets directory.
     * @return True if the file exists; false otherwise.
     */
    public static boolean doesFileExist(Context context, String filePath) {

        try {
            InputStream inputStream = context.getAssets().open(filePath);
            if (inputStream != null) {
                inputStream.close();
            }
            return true;
        }
        catch (IOException e) {
            Log.d(TAG, "File does not exist or could not be opened: " + filePath);
        }
        return false;
    }
}