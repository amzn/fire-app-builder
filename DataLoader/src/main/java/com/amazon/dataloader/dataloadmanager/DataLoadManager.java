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

import com.amazon.dataloader.R;
import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.dataloader.datadownloader.DataDownloaderFactory;
import com.amazon.android.recipe.IRecipeCooker;
import com.amazon.android.recipe.IRecipeCookerCallbacks;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.utils.model.Data;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.io.IOException;

import rx.Observable;

/**
 * This is the central controller for accessing data. It internally manages external data sources
 * and cache systems. Its provides two major functions:
 * 1. It provides hooks to run on-demand {@link Recipe}s via {@link IRecipeCooker}
 * Any component that needs to access any data can use one of the supported recipes and trigger a
 * data request. The request is executed off the UI thread and the corresponding handler is also
 * not called on UI thread.
 * 2. It also provides support for periodical data updates. Components that want to listen to
 * these updates need to implement the {@link IDataUpdateListener} interface and register
 * themselves with the {@link #registerUpdateListener(IDataUpdateListener)}.
 * There is a single instance of this controller in the system that is initialized at the
 * application start-up time and lives throughout the system.
 * Note: This system requires a {@link Context} to access different system resources. Do not
 * provide
 * activity context here since that would die along with the Activity. Instead provide the {@link
 * android.app.Application} instance since that lives throughout the system lifecycle.
 */
public class DataLoadManager implements IRecipeCooker {

    /**
     * Configuration key for data downloader implementation.
     */
    public static final String DATA_DOWNLOADER_IMPL = "data_downloader.impl";

    /**
     * Configuration key for the "is cache manager enabled" setting.
     */
    public static final String IS_CACHE_MANAGER_ENABLED = "is_cache_manager_enabled";

    /**
     * Configuration key for the task.
     */
    public static final String TASK = "task";

    /**
     * Configuration key for load data.
     */
    public static final String LOAD_DATA = "load_data";

    /**
     * Configuration key for download data.
     */
    public static final String DOWNLOAD_DATA = "download_data";

    /**
     * Configuration key for task type.
     */
    public static final String TASK_TYPE = "task_type";

    /**
     * Configuration key for an async task.
     */
    public static final String ASYNC = "async";

    /**
     * Configuration key for the cache size.
     */
    public static final String CACHE_SIZE = "cache_size";

    /**
     * Configuration key for cancelling all requests.
     */
    public static final String CANCEL_ALL = "cancel_all";

    /**
     * Debug tag.
     */
    private static final String TAG = DataLoadManager.class.getName();

    /**
     * Instance of the internal class {@link DataLoaderModule}.
     * It is used to perform all data loading operations.
     */
    private final DataLoaderModule mDataLoaderModule;

    /**
     * Instance of the internal class {@link DataUpdaterModule}.
     * It is used for all data update jobs.
     */
    private final DataUpdaterModule mDataUpdaterModule;

    /**
     * Configurations required by data loading operations.
     */
    private final Recipe mDataManagerConfig;

    /**
     * {@link ADataDownloader} instance to be used for downloading data from source.
     */
    private final ADataDownloader mDataDownloader;

    /**
     * CacheManagerAdapter instance to be used to access cache.
     */
    private final CacheManagerAdapter mCacheManagerAdapter;

    /**
     * Static instance required for the singleton.
     */
    private static DataLoadManager sInstance = null;

    /**
     * The application context.
     */
    private static Context sContext;

    /**
     * Accessor method for retrieving the singleton {@link DataLoadManager} instance.
     * Note: There is a single instance of this controller in the system that is initialized at
     * the application start up time and lives throughout the system.
     * This system requires {@link Context} to access different system resources. Irrespective of
     * which context is provided, it extracts the application context from the provided context and
     * utilizes that for all functions.
     *
     * @param context The application context.
     * @return The {@link DataLoadManager} instance.
     * @throws Exception Exceptions generated during construction.
     */
    public static DataLoadManager getInstance(Context context) throws Exception {

        // Singleton pattern
        if (sInstance == null) {
            Log.d(TAG, "DataLoadManager not initialized");
            synchronized (DataLoadManager.class) {
                if (sInstance == null) {
                    sContext = context.getApplicationContext();
                    sInstance = new DataLoadManager(sContext);
                }
            }
        }
        return sInstance;
    }

    /**
     * Requests {@link DataDownloaderFactory} for an instance of {@link ADataDownloader} as defined
     * in the configuration.
     *
     * @param context           The application context.
     * @param dataManagerConfig The configuration object.
     * @return {@link ADataDownloader} instance based on config file.
     * @throws DataDownloaderFactory.DownloaderInitializationFailedException if initialization
     *                                                                       fails.
     */
    @VisibleForTesting
    protected ADataDownloader createDataDownloaderInstance(Context context, Recipe
            dataManagerConfig) throws DataDownloaderFactory
            .DownloaderInitializationFailedException {

        return DataDownloaderFactory.createDataDownloader(context, dataManagerConfig
                .getItemAsString(DATA_DOWNLOADER_IMPL));
    }

    /**
     * Returns a new {@link CacheManagerAdapter} instance.
     *
     * @param context           The application context.
     * @param dataManagerConfig The configuration object.
     * @return The new {@link CacheManagerAdapter} instance.
     */
    @VisibleForTesting
    protected CacheManagerAdapter createCacheManagerAdapterInstance(Context context, Recipe
            dataManagerConfig) {
        // Start with a default size.
        int size = 0;
        // If config contains the size key, update the size with the config value.
        if (dataManagerConfig.containsItem(CACHE_SIZE)) {
            size = dataManagerConfig.getItemAsInt(CACHE_SIZE);
        }
        return new CacheManagerAdapter(size);
    }

    /**
     * Returns a new {@link Recipe} instance for configuration.
     *
     * @param context The application context.
     * @return The configuration {@link Recipe} instance.
     * @throws IOException if there was a problem reading the configuration file.
     */
    @VisibleForTesting
    protected Recipe createDataLoadManagerConfigInstance(Context context) throws IOException {

        return Recipe.newInstance(
                FileHelper.readFile(context, context.getString(R.string.data_loader_config_file)));
    }

    /**
     * Constructor for {@link DataLoadManager}
     * It expects the application context and builds the various objects required using the helper
     * methods.
     *
     * @param context The application context.
     * @throws Exception Exceptions generated during construction.
     */
    @VisibleForTesting
    protected DataLoadManager(Context context) throws Exception {

        this.mDataManagerConfig = createDataLoadManagerConfigInstance(context);
        this.mDataDownloader = createDataDownloaderInstance(context, mDataManagerConfig);
        this.mCacheManagerAdapter = createCacheManagerAdapterInstance(context, mDataManagerConfig);
        this.mDataLoaderModule = new DataLoaderModule(context, mDataManagerConfig,
                                                      mDataDownloader, mCacheManagerAdapter);
        this.mDataUpdaterModule = new DataUpdaterModule(context, mDataManagerConfig,
                                                        mDataDownloader, mCacheManagerAdapter);
    }

    /**
     * Interface for listening to updates. Components that need to listen to data updates need to
     * implement this interface and register themselves via {@link #registerUpdateListener
     * (IDataUpdateListener)}.
     */
    public interface IDataUpdateListener {

        /**
         * Callback handler for successful response.
         *
         * @param data Data returned by updater.
         */
        void onSuccess(Data data);

        /**
         * Callback handler for failure response.
         *
         * @param throwable Throwable thrown by updater.
         */
        void onFailure(Throwable throwable);
    }

    /**
     * Provides hook to register update listeners. Once registered, these listeners will receive
     * any updates received via the updater.
     *
     * @param dataUpdateListener The concrete implementation of {@link com.amazon.dataloader
     *                           .dataloadmanager.DataLoadManager.IDataUpdateListener}.
     */
    public void registerUpdateListener(IDataUpdateListener dataUpdateListener) {

        mDataUpdaterModule.registerUpdateListener(dataUpdateListener);
    }

    /**
     * Provides hook to deregister update listeners. Once deregistered, the listeners will not
     * receive any updates.
     *
     * @param dataUpdateListener The concrete implementation of {@link com.amazon.dataloader
     *                           .dataloadmanager.DataLoadManager.IDataUpdateListener}.
     */
    public void deregisterUpdateListener(IDataUpdateListener dataUpdateListener) {

        mDataUpdaterModule.deregisterUpdateListener(dataUpdateListener);
    }

    /**
     * Checks if the listener is registered with the {@link DataUpdaterModule}.
     *
     * @param dataUpdateListener The listener to check.
     * @return True if the listener is registered; false otherwise.
     */
    public boolean isUpdateListenerRegistered(IDataUpdateListener
                                                      dataUpdateListener) {

        return mDataUpdaterModule.isUpdateListenerRegistered(dataUpdateListener);
    }

    /**
     * {@inheritDoc}
     *
     * Fetches data based on {@link Recipe}. It delegates the download call to {@link
     * ADataDownloader} or {@link CacheManagerAdapter}. The data is fetched via an async task to
     * prevent blocking of main thread. The request is executed off the UI thread and the
     * corresponding handler is also not called on UI thread.
     */
    @Override
    public boolean cookRecipe(Recipe recipe, Object data, IRecipeCookerCallbacks cb, Bundle
            bundle, String[] params) {

        return mDataLoaderModule.cookRecipe(recipe, data, cb, bundle, params);
    }

    /**
     * {@inheritDoc}
     *
     * Fetches data based on {@link Recipe}. It delegates the download call to {@link
     * ADataDownloader} or {@link CacheManagerAdapter}. Reactive coding friendly API.
     */
    @Override
    public Observable<Object> cookRecipeObservable(Recipe recipe, Object data, Bundle bundle,
                                                   String[] params) {

        return mDataLoaderModule.cookRecipeObservable(recipe, data, bundle, params);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DataLoadManager.class.getName();
    }
}
