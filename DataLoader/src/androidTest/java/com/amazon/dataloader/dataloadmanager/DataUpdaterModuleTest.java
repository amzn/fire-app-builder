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

import com.amazon.dataloader.testResources.MockCacheManagerAdapter;
import com.amazon.dataloader.testResources.MockDataLoadManager;
import com.amazon.dataloader.testResources.MockDataLoadManagerWithoutDataUpdater;
import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import android.support.test.InstrumentationRegistry;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

/**
 * Tests for DataUpdaterModule
 */
public class DataUpdaterModuleTest extends DataLoadManagerTest {

    private DataLoadManager.IDataUpdateListener mUpdateListener;
    MockDataLoadManager mTestDataLoadManager;

    @Before
    public void setUp() throws Exception {

        super.setUp();
        mUpdateListener = mock(DataLoadManager.IDataUpdateListener.class);
    }

    /**
     * Sets up DataLoadManager as MockDataLoadManager
     */
    private void setUpManager() throws Exception {

        mTestDataLoadManager = new MockDataLoadManager(InstrumentationRegistry
                                                               .getTargetContext());
        mTestDataLoadManager.registerUpdateListener(mUpdateListener);
    }

    /**
     * Tests scheduled data updater, verifying that DataUpdaterTask is being executed by Scheduler
     */
    @Test
    public void testScheduledDataUpdater() throws Exception {

        setUpManager();
        Thread.sleep(5000);
        verify(MockCacheManagerAdapter.mockCacheManagerAdapter, atLeastOnce()).clearCache();
        verify(mUpdateListener, atLeastOnce()).onSuccess(any(Data.class));
    }

    /**
     * Tests that de-registered listeners do not receive updates anymore
     */
    @Test
    public void testDeRegisteredListenerNotListenUpdates() throws Exception {

        setUpManager();
        mTestDataLoadManager.deregisterUpdateListener(mUpdateListener);
        DataLoadManager.IDataUpdateListener mUpdateListener1 = mock(DataLoadManager
                                                                            .IDataUpdateListener
                                                                            .class);
        mTestDataLoadManager.registerUpdateListener(mUpdateListener1);
        Thread.sleep(5000);
        verify(mUpdateListener, never()).onSuccess(any(Data.class));
        verify(mUpdateListener1, atLeastOnce()).onSuccess(any(Data.class));
        verify(MockCacheManagerAdapter.mockCacheManagerAdapter, atLeastOnce()).clearCache();
    }

    /**
     * Tests non-scheduled data updater, verifying that DataUpdaterTask is not executed by
     * Scheduler
     */
    @Test
    public void testNonScheduledDataUpdater() throws Exception {

        MockDataLoadManager testDataLoadManagerNoUpdater = new
                MockDataLoadManagerWithoutDataUpdater(InstrumentationRegistry.getTargetContext());
        testDataLoadManagerNoUpdater.registerUpdateListener(mUpdateListener);
        Thread.sleep(5000);
        verify(MockCacheManagerAdapter.mockCacheManagerAdapter, never()).clearCache();
        verify(mUpdateListener, never()).onSuccess(any(Data.class));
    }
}
