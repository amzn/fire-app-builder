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

import com.amazon.dataloader.R;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.utils.model.Data;

import android.content.Context;

/**
 * This class represents a basic file-based data downloader. It accepts a file path via a {@link
 * Recipe} containing the {@link #DATA_FILE_PATH} key, reads the data from that file, and returns a
 * {@link Data} object. If the recipe does not contain the file path, it looks for it in the
 * class's
 * {@link #mConfiguration}. An exception is thrown if file path is not found.
 */
public class BasicFileBasedDataDownloader extends ADataDownloader {

    /**
     * The key to locate the data file path.
     */
    protected static final String DATA_FILE_PATH = "data_file_path";

    /**
     * Constructs a {@link BasicFileBasedDataDownloader}.
     *
     * @param context The context.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public BasicFileBasedDataDownloader(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * Returns an instance of {@link BasicFileBasedDataDownloader}
     *
     * @param context The context.
     * @return The {@link #BasicFileBasedDataDownloader(Context)} instance.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public static ADataDownloader createInstance(Context context) throws ObjectCreatorException {

        return new BasicFileBasedDataDownloader(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return context.getString(R.string.basic_file_downloader_config_file_path);
    }

    /**
     * {@inheritDoc}
     *
     * Loads the data from the file defined in the recipe with the {@link #DATA_FILE_PATH} key.
     * If the key is not found in recipe, {@link #mConfiguration} is searched for the key.
     */
    @Override
    protected Data fetchData(Recipe dataLoadRecipe) throws Exception {
        // Get the file path.
        String filePath = getKey(dataLoadRecipe, DATA_FILE_PATH);
        String payload = FileHelper.readFile(mContext, filePath);
        return Data.createDataForPayload(payload);
    }
}
