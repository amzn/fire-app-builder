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

import com.amazon.android.scheduler.ScheduledBackgroundTask;
import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.android.recipe.Recipe;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Inner class of {@link DataLoadManager}, it triggers the updater on instantiation.
 * All listeners registered with this module will receive the updates.
 *
 * Any component that needs to listen to updates should register itself via {@link
 * DataLoadManager}.
 */

/** package private */
class DataUpdaterModule extends ADataModule {

    private static final String TAG = DataUpdaterModule.class.getName();
    private static final String DATA_UPDATER_DURATION = "data_updater.duration";
    private final List<DataLoadManager.IDataUpdateListener> mUpdateListeners;

    /**
     * {@inheritDoc}
     *
     * It triggers the updater based on params given in config.
     */
    DataUpdaterModule(Context context, Recipe dataLoadManagerConfig, ADataDownloader
            contentDownloader, CacheManagerAdapter cacheManagerAdapter) {

        super(contentDownloader, cacheManagerAdapter, dataLoadManagerConfig, context);
        // synchronizedList to make sure addition and deletion does not break iteration over list.
        mUpdateListeners = Collections.synchronizedList(new ArrayList<>());
        triggerUpdater();
    }

    /**
     * Trigger the scheduler for data updates.
     * It reads the update duration from config file, create {@link ScheduledBackgroundTask} instance,
     * and triggers the scheduler for this instance.
     * If the duration is not present or is <= 0, it ignores the call and does not do anything.
     */
    private void triggerUpdater() {

        long duration = 0;
        if (mDataLoadManagerConfig.containsItem(DATA_UPDATER_DURATION)) {
            duration = mDataLoadManagerConfig.getItemAsInt(DATA_UPDATER_DURATION);
        }
        if (duration <= 0) {
            Log.d(TAG, "data updater not configured, not triggering");
            return;
        }
        ScheduledBackgroundTask executor = new ScheduledBackgroundTask(new DataUpdaterTask
                                                                               (mUpdateListeners,
                                                                                mCacheManagerAdapter)
                , duration, duration, TimeUnit.SECONDS);
        executor.start();
    }

    /**
     * Registers update listeners. It does not de-duplicate listeners. If same listener is
     * registered multiple times, it will receive the same update multiple times.
     *
     * @param dataUpdateListener The listener to be registered.
     */
    void registerUpdateListener(DataLoadManager.IDataUpdateListener dataUpdateListener) {

        mUpdateListeners.add(dataUpdateListener);
    }

    /**
     * Deregisters data update listener. This listener will no longer receive any more updates.
     *
     * @param dataUpdateListener The listener to be de-registered.
     */
    void deregisterUpdateListener(DataLoadManager.IDataUpdateListener dataUpdateListener) {

        mUpdateListeners.remove(dataUpdateListener);
    }

    /**
     * Returns whether the {@link DataLoadManager.IDataUpdateListener} is registered with the
     * {@link DataUpdaterModule}.
     *
     * @return True if the listener is registered; false otherwise.
     */
    boolean isUpdateListenerRegistered(DataLoadManager.IDataUpdateListener dataUpdateListener) {

        return mUpdateListeners.contains(dataUpdateListener);
    }
}
