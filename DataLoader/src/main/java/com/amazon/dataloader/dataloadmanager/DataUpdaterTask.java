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

import com.amazon.android.scheduler.ITask;
import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.utils.model.Data;

import android.util.Log;

import java.util.List;

/**
 * An implementation of {@link ITask} to execute data updates.
 * On every run it invalidates the cache and calls the updateListeners.onSuccess methods with an
 * empty Data object to indicate that it is time to reload data.
 */
public class DataUpdaterTask implements ITask {

    private static final String TAG = DataUpdaterTask.class.getName();
    /**
     * listeners for data updates
     */
    private final List<DataLoadManager.IDataUpdateListener> mUpdateListeners;
    /**
     * Instance of {@link CacheManagerAdapter} used by data updater
     */
    private final CacheManagerAdapter mCacheManagerAdapter;

    /**
     * Constructor.
     *
     * @param updateListeners     Listeners for data updates.
     * @param cacheManagerAdapter Adapter for cache manager.
     */
    public DataUpdaterTask(List<DataLoadManager.IDataUpdateListener> updateListeners,
                           CacheManagerAdapter cacheManagerAdapter) {

        mUpdateListeners = updateListeners;
        mCacheManagerAdapter = cacheManagerAdapter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void executeTask() {

        Log.d(TAG, "executing DataUpdaterTask");
        // Invalidate cache.
        mCacheManagerAdapter.clearCache();

        // Even though the list is synchronizedList, it only supports each operation to be atomic.
        // Iteration consists of multiple operations hence require outside synchronization.
        synchronized (mUpdateListeners) {
            for (DataLoadManager.IDataUpdateListener updateListener : mUpdateListeners) {
                updateListener.onSuccess(Data.createDataForPayload(""));
            }
        }
    }
}
