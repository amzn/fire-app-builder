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
package com.amazon.dataloader.dataloadmanager;

import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.android.recipe.Recipe;

import android.content.Context;


/**
 * Base class for the {@link DataLoaderModule} and {@link DataUpdaterModule} classes.
 */
abstract class ADataModule {

    /**
     * {@link CacheManagerAdapter} instance to be used for accessing the cache.
     */
    protected final CacheManagerAdapter mCacheManagerAdapter;

    /**
     * {@link ADataDownloader} instance to be used for downloading data from the source.
     */
    protected final ADataDownloader mDataDownloader;

    /**
     * Configuration object to access various configuration parameters.
     */
    protected final Recipe mDataLoadManagerConfig;

    /**
     * The context.
     */
    protected final Context mContext;

    /**
     * Base constructor for data modules.
     *
     * @param dataDownloader        The data downloader implementation to be used for downloading
     *                              data from the source; cannot be null.
     * @param cacheManagerAdapter   The {@link CacheManagerAdapter} implementation to be used to
     *                              access the cache. It can be null only if the configuration
     *                              defines that the cache is disabled.
     * @param dataLoadManagerConfig The configuration recipe for accessing different configuration
     *                              parameters; cannot be null.
     * @param context               The application context; cannot be null.
     */
    public ADataModule(ADataDownloader dataDownloader, CacheManagerAdapter
            cacheManagerAdapter, Recipe dataLoadManagerConfig, Context context) {

        if (dataLoadManagerConfig == null || dataDownloader == null || context == null ||
                validateCacheArguments(cacheManagerAdapter, dataLoadManagerConfig)) {
            throw new IllegalArgumentException("One of the required parameters is null");
        }
        this.mCacheManagerAdapter = cacheManagerAdapter;
        this.mDataDownloader = dataDownloader;
        this.mDataLoadManagerConfig = dataLoadManagerConfig;
        this.mContext = context;
    }

    /**
     * Validates the cache configuration arguments. If the configuration defines that cache is
     * enabled, the cache manager should not be null.
     *
     * @return True or false if the cache arguments are valid or invalid, respectively.
     */
    private boolean validateCacheArguments(CacheManagerAdapter cacheManagerAdapter,
                                           Recipe dataLoadManagerConfig) {

        return dataLoadManagerConfig.containsItem(DataLoadManager.IS_CACHE_MANAGER_ENABLED) &&
                dataLoadManagerConfig.getItemAsBoolean(DataLoadManager.IS_CACHE_MANAGER_ENABLED)
                && cacheManagerAdapter == null;
    }
}
