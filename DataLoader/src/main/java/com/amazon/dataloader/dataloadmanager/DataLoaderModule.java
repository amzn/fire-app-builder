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
import com.amazon.dataloader.datadownloader.IDataLoader;
import com.amazon.android.recipe.IRecipeCooker;
import com.amazon.android.recipe.IRecipeCookerCallbacks;
import com.amazon.android.recipe.Recipe;
import com.amazon.utils.ObjectVerification;
import com.amazon.utils.model.Data;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ThreadPoolExecutor;

import rx.Observable;

/**
 * Inner class of {@link DataLoadManager}, it manages on-demand data download from either source or
 * cache. Supports {@link Recipe} execution.
 */

/** package private */
class DataLoaderModule extends ADataModule implements IRecipeCooker {

    private static final String TAG = DataLoaderModule.class.getSimpleName();
    private static final ThreadPoolExecutor EXECUTOR
            = (ThreadPoolExecutor) AsyncTask.THREAD_POOL_EXECUTOR;
    private final List<LoadDataTask> mRunningAsyncTaskList;

    /**
     * {@inheritDoc}
     */
    DataLoaderModule(Context context, Recipe dataLoadManagerConfig, ADataDownloader
            dataDownloader, CacheManagerAdapter cacheManagerAdapter) {

        super(dataDownloader, cacheManagerAdapter, dataLoadManagerConfig, context);
        mRunningAsyncTaskList = Collections.synchronizedList(new ArrayList<>());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Observable<Object> cookRecipeObservable(Recipe recipe,
                                                   final Object data,
                                                   final Bundle bundle,
                                                   final String[] params) {

        // Wrap existing cook recipe to RX Java observable.
        Observable<Object> dataLoaderObservable
                = Observable.create(
                subscriber -> cookRecipe(recipe, data, new IRecipeCookerCallbacks() {
                    @Override
                    public void
                    onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

                    }

                    @Override
                    public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle,
                                   boolean done) {
                        // Get the data.
                        Data data = (Data) output;
                        // Check the subscription status.
                        if (!subscriber.isUnsubscribed()) {
                            // Provide the data.
                            subscriber.onNext(data.getContent().getPayload());
                            // Call onCompleted after providing the all data.
                            if (data.isComplete()) {
                                subscriber.onCompleted();
                            }
                        }
                    }

                    @Override
                    public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

                    }

                    @Override
                    public void onRecipeError(Recipe recipe, Exception e, String msg) {
                        // Propagate  the error.
                        subscriber.onError(e);
                    }
                }, bundle, params)
        );

        return dataLoaderObservable;
    }

    /**
     * {@inheritDoc}
     *
     * Fetches content based on the {@link Recipe}.It delegates the download call to {@link
     * ADataDownloader} or {@link CacheManagerAdapter} and provides a request handler for handling
     * responses. This provides functionality to support multiple calls at the same time without
     * blocking on a single big request.
     */
    @Override
    public boolean cookRecipe(final Recipe recipe, final Object data,
                              final IRecipeCookerCallbacks cb, final Bundle bundle,
                              final String[] params) {

        try {
            // Check if the recipe is for cancelling all ongoing tasks.
            if (cancelAllRecipes(recipe)) {
                return true;
            }
            LoadDataTask loadDataTask = new LoadDataTask(recipe, cb, bundle, params);

            // Check the task type, does it require a sync call or an async call.
            String taskType = recipe.getItemAsString(DataLoadManager.TASK_TYPE);
            if (taskType != null && taskType.equalsIgnoreCase(DataLoadManager.ASYNC)) {
                Log.d(TAG, "Async call requested");
                loadDataTask.loadDataAsync();
            }
            // Default is a sync task.
            else {
                loadDataTask.loadData();
            }
            return true;
        }
        catch (Exception e) {
            cb.onRecipeError(recipe, e, "Exception generated during fetching data");
            return false;
        }
    }

    /**
     * Checks if the recipe requests for all ongoing tasks to be cancelled. If its true it cancels
     * all tasks in the list and re-initializes the list.
     *
     * @param recipe Recipe to run.
     * @return True or false depending on whether the tasks were cancelled or not.
     */
    private boolean cancelAllRecipes(Recipe recipe) {

        if (!recipe.containsItem(DataLoadManager.TASK)) {
            return false;
        }
        String task = recipe.getItemAsString(DataLoadManager.TASK);
        if (task.equals(DataLoadManager.CANCEL_ALL)) {
            Log.d(TAG, "Cancel all recipes");
            // Loop through the whole list and cancel all tasks
            synchronized (mRunningAsyncTaskList) {
                for (LoadDataTask asyncTask : mRunningAsyncTaskList) {
                    asyncTask.cancel(true);
                }
                mRunningAsyncTaskList.clear();
            }
            return true;
        }
        return false;
    }


    /**
     * Class to load data synchronously or asynchronously.
     */
    class LoadDataTask extends AsyncTask<Void, Void, Void> {

        private Recipe mRecipe;
        private IRecipeCookerCallbacks mCb;
        private Bundle mBundle;
        private String[] mParams;

        /**
         * Constructor.
         *
         * @param recipe Recipe to load data for.
         * @param cb     Recipe call back.
         * @param bundle Recipe bundle.
         * @param params Recipe params.
         */
        public LoadDataTask(Recipe recipe, IRecipeCookerCallbacks cb, Bundle bundle, String[]
                params) {

            this.mRecipe = ObjectVerification.notNull(recipe, "Recipe cannot be null");
            this.mCb = ObjectVerification.notNull(cb, "IRecipeCookerCallbacks cannot be null");
            this.mBundle = bundle;
            this.mParams = params;
        }

        @Override
        protected Void doInBackground(Void... params) {

            loadData();
            return null;
        }

        @Override
        public void onPostExecute(Void data) {
            // Removing the task from list
            mRunningAsyncTaskList.remove(this);
        }

        @Override
        public void onCancelled() {
            // Removing the task from list
            mRunningAsyncTaskList.remove(this);
        }

        /**
         * This method evaluates the task to perform, and then either loads data from cache or
         * downloads data directly from the source.
         */
        public void loadData() {

            try {

                // Initialize request handler for data downloader.
                final IDataLoader.IDataLoadRequestHandler dataDownLoadRequestHandler =
                        initRequestHandlerForDataDownload(mCb, mBundle, this);

                // Initialize request handler for cacheLoader.
                final IDataLoader.IDataLoadRequestHandler cacheLoadRequestHandler =
                        requestHandlerForLoadingCache(mCb, mBundle, dataDownLoadRequestHandler,
                                                      this);

                // Get the task to perform.
                String task = mRecipe.getItemAsString(DataLoadManager.TASK);

                // If the task is to request to load data directly from source.
                if (shouldDownloadData(task)) {
                    Log.d(TAG, "Request to load data directly from source");
                    loadDataFromSource(mRecipe, mParams, dataDownLoadRequestHandler);
                }
                // Otherwise load the data from cache.
                else {
                    loadDataFromCache(mRecipe, mParams, cacheLoadRequestHandler);
                }
            }
            catch (Exception e) {

                mCb.onRecipeError(mRecipe, e, "Exception generated while fetching data");
            }
        }

        /**
         * Loads the data asynchronously. It stores a reference to this task in a list that is
         * later
         * removed when {@link #onPostExecute} or {@link #onCancelled} is called.
         */
        public void loadDataAsync() {

            mRunningAsyncTaskList.add(this);
            // Using the executor to allow parallel requests to run.
            this.executeOnExecutor(EXECUTOR);
        }

    }

    /**
     * This method evaluates whether the data needs to be downloaded from the source.
     * It is based on 2 factors:
     * a. Is the task specifically asking for downloading data from source (task = download_data)?
     * b. Is cacheManagerDisabled?
     *
     * @param task The task required to be executed based on the recipe.
     * @return True if the data needs to be downloaded from source; false otherwise.
     */
    private boolean shouldDownloadData(String task) {

        return task.equals(DataLoadManager.DOWNLOAD_DATA)
                || !mDataLoadManagerConfig.containsItem(DataLoadManager.IS_CACHE_MANAGER_ENABLED)
                || !mDataLoadManagerConfig.getItemAsBoolean(DataLoadManager
                                                                    .IS_CACHE_MANAGER_ENABLED);
    }

    /**
     * RequestHandler for the cache loader. It delegates the download request to the data
     * downloader
     * if the cache does not have the data or if cache manager is throws an error. Otherwise the
     * data is sent back to cookRecipe requester via {@link
     * IRecipeCookerCallbacks#onPostRecipeCooked(Recipe, Object, Bundle)} method.
     *
     * @param cb     The callback handler of the recipe being executed.
     * @param bundle The bundle received in cookRecipe call.
     * @return The request handler to use when loading data from the cache.
     */
    private IDataLoader.IDataLoadRequestHandler requestHandlerForLoadingCache(
            final IRecipeCookerCallbacks cb, final Bundle bundle,
            final IDataLoader.IDataLoadRequestHandler dataDownLoadRequestHandler,
            final AsyncTask asyncTask) {

        return new IDataLoader.IDataLoadRequestHandler() {
            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data data) {

                if (isTaskCancelled(asyncTask)) {
                    return;
                }
                if (data != null) {
                    if (isTaskCancelled(asyncTask)) {
                        return;
                    }
                    Log.d(TAG, "Received data successfully from cache for recipe " +
                            dataLoadRecipe);
                    cb.onRecipeCooked(dataLoadRecipe, data, bundle, data.isComplete());
                }
                else {
                    Log.d(TAG, "Cache does not have data for recipe " + dataLoadRecipe.toString());
                    loadDataFromSource(dataLoadRecipe, params, dataDownLoadRequestHandler);
                }
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                if (isTaskCancelled(asyncTask)) {
                    return;
                }
                Log.e(TAG, "Could not load data from cache ", throwable);
                loadDataFromSource(dataLoadRecipe, params, dataDownLoadRequestHandler);
            }
        };
    }

    /**
     * Checks if the async task has already been cancelled.
     *
     * @param asyncTask The task to check.
     * @return True if the task has been cancelled; false otherwise.
     */
    private boolean isTaskCancelled(AsyncTask asyncTask) {

        if (asyncTask.isCancelled()) {
            Log.i(TAG, "Async task is cancelled, do not continue with recipe");
            return true;
        }
        return false;
    }

    /**
     * Initializes the request handler for the data downloader. The download response is wrapped
     * and
     * sent to the cookRecipe requester via the {@link IRecipeCookerCallbacks} received.
     *
     * @param cb     The callback handler of the recipe being executed.
     * @param bundle The bundle received in cookRecipe call.
     * @return The request handler to be used for downloading data.
     */
    private IDataLoader.IDataLoadRequestHandler initRequestHandlerForDataDownload(
            final IRecipeCookerCallbacks cb, final Bundle bundle, final AsyncTask asyncTask) {

        return new IDataLoader.IDataLoadRequestHandler() {
            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data data) {

                if (isTaskCancelled(asyncTask)) {
                    return;
                }
                Log.d(TAG, "Received data successfully from data store for recipe " +
                        dataLoadRecipe);

                // Check if cache manager is enabled and then store the data in cache.
                if (mDataLoadManagerConfig.containsItem(DataLoadManager.IS_CACHE_MANAGER_ENABLED)
                        && mDataLoadManagerConfig
                    .getItemAsBoolean(DataLoadManager.IS_CACHE_MANAGER_ENABLED)) {
                    try {
                        Log.d(TAG, "Store data to cache");
                        mCacheManagerAdapter.storeData(dataLoadRecipe, params, data);
                    }
                    catch (Exception e) {
                        // Ignoring any error received while storing data in cache since it is not
                        // essential.
                        Log.e(TAG, "Could not store the data in cache ", e);
                    }
                }
                cb.onRecipeCooked(dataLoadRecipe, data, bundle, data.isComplete());
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                if (isTaskCancelled(asyncTask)) return;
                Log.e(TAG, "Could not load data from source ", throwable);
                cb.onRecipeError(dataLoadRecipe, new Exception(throwable), throwable.getMessage());
            }
        };
    }

    /**
     * Loads data from the {@link #mCacheManagerAdapter}.
     *
     * @param recipe                  The recipe required by {@link CacheManagerAdapter#loadData
     *                                (Recipe, String[], IDataLoader.IDataLoadRequestHandler)}.
     * @param params                  The params need by the recipe.
     * @param cacheLoadRequestHandler The data load request handler required by {@link
     *                                CacheManagerAdapter#loadData(Recipe, String[],
     *                                IDataLoader.IDataLoadRequestHandler)}.
     */
    private void loadDataFromCache(Recipe recipe, String[] params,
                                   IDataLoader.IDataLoadRequestHandler cacheLoadRequestHandler) {

        mCacheManagerAdapter.loadData(recipe, params, cacheLoadRequestHandler);
    }

    /**
     * Loads data from the {@link #mDataDownloader}.
     *
     * @param recipe                 The recipe required by {@link ADataDownloader#loadData(Recipe,
     *                               String[], IDataLoader.IDataLoadRequestHandler)}.
     * @param params                 The parameters need by the recipe.
     * @param dataLoadRequestHandler The data load request handler required by {@link
     *                               ADataDownloader#loadData(Recipe, String[], IDataLoader
     *                               .IDataLoadRequestHandler)}.
     */
    private void loadDataFromSource(Recipe recipe, String[] params,
                                    IDataLoader.IDataLoadRequestHandler dataLoadRequestHandler) {

        mDataDownloader.loadData(recipe, params, dataLoadRequestHandler);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String getName() {

        return DataLoaderModule.class.getSimpleName();
    }
}
