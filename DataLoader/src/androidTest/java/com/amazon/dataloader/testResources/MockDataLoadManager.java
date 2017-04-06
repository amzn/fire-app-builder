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

import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.dataloader.dataloadmanager.DataLoadManager;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;

import android.content.Context;

import java.io.IOException;

/**
 * This class extends {@link DataLoadManager} to inject some mock dependencies for testing.
 */
public class MockDataLoadManager extends DataLoadManager {

    /**
     * Constructs a {@link MockDataDownloader}.
     *
     * @param context The context.
     */
    public MockDataLoadManager(Context context) throws Exception {

        super(context);
    }

    /**
     * Used for injecting mock data downloader.
     *
     * @return The {@link MockDataDownloader}.
     */
    @Override
    protected ADataDownloader createDataDownloaderInstance(Context context, Recipe
            dataManagerConfig) {

        return MockDataDownloader.dataDownloader;
    }

    /**
     * Used for injecting mock cache manager adapter.
     *
     * @return The {@link MockCacheManagerAdapter}.
     */
    @Override
    protected CacheManagerAdapter createCacheManagerAdapterInstance(Context context, Recipe
            dataManagerConfig) {

        return MockCacheManagerAdapter.mockCacheManagerAdapter;
    }

    /**
     * {@inheritDoc}
     *
     * @param context application context
     */
    @Override
    protected Recipe createDataLoadManagerConfigInstance(Context context) throws IOException {

        return Recipe.newInstance(FileHelper.readFile(context,
                                                      "configurations/DataLoadManagerConfig.json"));
    }

}