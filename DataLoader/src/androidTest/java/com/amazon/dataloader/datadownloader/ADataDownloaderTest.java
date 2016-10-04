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

import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

/**
 * This class tests the {@link ADataDownloader} class.
 */
public class ADataDownloaderTest {

    private Context context = InstrumentationRegistry.getTargetContext();

    /**
     * Tests whether the constructor is initializing the required objects correctly.
     */
    @Test
    public void testConstructor() throws IDataLoader.DataLoaderException, AObjectCreator
            .ObjectCreatorException {

        ADataDownloader downloader = new MockDataDownloader(context);
        assertEquals(context.getApplicationContext(), downloader.mContext);
        assertNotNull(downloader.mConfiguration);
        assertEquals("testValue", downloader.mConfiguration.getItemAsString("testKey"));
    }
}
