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
import com.amazon.dataloader.testResources.MockCacheManagerAdapter;
import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.List;

import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * Tests for {@link DataUpdaterTask}
 */
public class DataUpdaterTaskTest {

    private CacheManagerAdapter mCacheManagerAdapter;
    private DataUpdaterTask mDataUpdaterTask;
    private List<DataLoadManager.IDataUpdateListener> mUpdateListeners;

    @Before
    public void setUp() throws NoSuchAlgorithmException {

        mCacheManagerAdapter = mock(MockCacheManagerAdapter.class);
        mUpdateListeners = new ArrayList<>();
        mUpdateListeners.add(mock(DataLoadManager.IDataUpdateListener.class));
        mUpdateListeners.add(mock(DataLoadManager.IDataUpdateListener.class));
        mDataUpdaterTask = new DataUpdaterTask(mUpdateListeners, mCacheManagerAdapter);
    }

    /**
     * Tests {@link DataUpdaterTask#executeTask()}
     * this method tests the execute task by executing the task and making sure each registered
     * listener receives a call to onSuccess
     */
    @Test
    public void testExecute() {

        mDataUpdaterTask.executeTask();
        verify(mCacheManagerAdapter).clearCache();
        for (DataLoadManager.IDataUpdateListener iDataUpdateListener : mUpdateListeners) {
            verify(iDataUpdateListener).onSuccess(any(Data.class));
        }
    }
}
