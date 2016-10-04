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
package com.amazon.dataloader.testResources;

import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.dataloader.datadownloader.IDataLoader;
import com.amazon.android.recipe.Recipe;
import com.amazon.utils.model.Data;

import android.content.Context;

/**
 * Mock implementation of {@link ADataDownloader} required for testing.
 */
public class MockDataDownloader extends ADataDownloader {

    /**
     * An instance of {@link MockDataDownloader}.
     */
    public static MockDataDownloader dataDownloader;

    /**
     * Constructs a {@link MockDataDownloader} object.
     *
     * @param context The context.
     */
    public MockDataDownloader(Context context) throws IDataLoader.DataLoaderException,
            ObjectCreatorException {

        super(context);
    }

    /**
     * Returns the instance of {@link ADataDownloader} as a {@link MockDataDownloader}.
     *
     * @param context The context.
     * @return An {@link ADataDownloader} implementation.
     */
    public static ADataDownloader createInstance(Context context) {

        return dataDownloader;
    }

    /**
     * {@inheritDoc}
     *
     * @param context application context
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return "configurations/TestConfig.json";
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadData(Recipe dataLoadRecipe, String[] params, IDataLoader
            .IDataLoadRequestHandler
            requestHandle) {

        return false;
    }

    /**
     * {@inheritDoc}
     *
     * @param dataLoadRecipe recipe to load data
     * @return This implementation returns null.
     */
    @Override
    protected Data fetchData(Recipe dataLoadRecipe) throws Exception {

        return null;
    }
}

