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

import com.amazon.dataloader.testResources.MockDataDownloader;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;

@RunWith(AndroidJUnit4.class)
public class DataDownloaderFactoryTest {

    private String downloaderName = MockDataDownloader.class.getName();
    private Context context = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() {

        MockDataDownloader.dataDownloader = mock(MockDataDownloader.class);
    }

    /**
     * Test to validate successful creation of an {@link ADataDownloader} implementation via {@link
     * DataDownloaderFactory#createDataDownloader(Context, String)}.
     */
    @Test
    public void testCreateDataDownloader() throws DataDownloaderFactory
            .DownloaderInitializationFailedException {

        DataDownloaderFactory.createDataDownloader(context, downloaderName);
        assertEquals(MockDataDownloader.dataDownloader, DataDownloaderFactory.createDataDownloader
                (context, downloaderName));
    }

    /**
     * Tests the {@link DataDownloaderFactory#createDataDownloader(Context, String)} method using
     * a non-existent {@link ADataDownloader} implementation.
     */
    @Test(expected = DataDownloaderFactory.DownloaderInitializationFailedException.class)
    public void testDownloaderNotFoundException() throws DataDownloaderFactory
            .DownloaderInitializationFailedException {

        DataDownloaderFactory.createDataDownloader(context, "NonExistentDownloader");
    }
}
