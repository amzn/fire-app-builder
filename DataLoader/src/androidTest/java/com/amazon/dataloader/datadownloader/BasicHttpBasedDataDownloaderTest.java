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

import com.amazon.dataloader.testResources.MockUrlGenerator;
import com.amazon.dataloader.testResources.VerifyUtil;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link BasicHttpBasedDataDownloader} class.
 */
public class BasicHttpBasedDataDownloaderTest {

    private Recipe recipe;
    private VerifyUtil verifyUtil;
    private Context context = InstrumentationRegistry.getTargetContext();
    private ADataDownloader dataDownloader;

    @Before
    public void setUp() throws DataDownloaderFactory.DownloaderInitializationFailedException {

        recipe = mock(Recipe.class);
        verifyUtil = mock(VerifyUtil.class);
        MockUrlGenerator.mMockUrlGenerator = mock(MockUrlGenerator.class);
        dataDownloader = DataDownloaderFactory.createDataDownloader(context,
                                                                    BasicHttpBasedDataDownloader
                                                                            .class
                                                                            .getName());
    }

    /**
     * Tests the positive case of passing a valid URL and successfully reading its data.
     */
    @Test
    public void testSingleValidUrl() throws IOException, AUrlGenerator.UrlGeneratorException {

        String url = "http://www.lightcast.com/api/firetv/channels" +
                ".php?app_id=257&app_key=0ojbgtfcsq12&action=channels_videos";

        Data data = Data.createDataForPayload(NetworkUtils.getDataLocatedAtUrl(url));
        when(MockUrlGenerator.mMockUrlGenerator.getUrl(any(Map.class))).thenReturn(url);

        dataDownloader.loadData(recipe, new String[0], createSuccessfulDataLoadHandler(data));

        // Validating that verifyUtil.verified() was called in requestHandler
        verify(verifyUtil).verified();
    }

    /**
     * Tests the negative case of passing an invalid URL being from a {@link AUrlGenerator} object.
     */
    @Test
    public void testInvalidUrl() throws DataDownloaderFactory
            .DownloaderInitializationFailedException, AUrlGenerator.UrlGeneratorException {

        List<String> singleValidUrl = Collections.singletonList("invalidUrl");

        when(MockUrlGenerator.mMockUrlGenerator.getUrl(any(Map.class)))
                .thenReturn(singleValidUrl.get(0));

        ADataDownloader dataDownloader =
                DataDownloaderFactory.createDataDownloader(context,
                                                           BasicHttpBasedDataDownloader.class
                                                                   .getName());

        dataDownloader.loadData(recipe, new String[0], createFailedDataLoadHandler());

        // Validating that verifyUtil.verified() was called in requestHandler
        verify(verifyUtil).verified();
    }

    /**
     * Creates a data load handler for a successful request.
     *
     * This is necessary because the data load call happens via requestHandler so we can not be
     * sure whether the call completed or silently died. Adding the verify latch will ensure that
     * the call was actually successfully completed.
     *
     * @param expectedData Expected data from the call.
     * @return The handler instance.
     */
    private IDataLoader.IDataLoadRequestHandler createSuccessfulDataLoadHandler(
            final Data expectedData) {

        return new IDataLoader.IDataLoadRequestHandler() {
            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data data) {

                assertEquals(expectedData, data);
                verifyUtil.verified();
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                Log.e("Test failed", throwable.getMessage(), throwable);
                fail(throwable.getMessage());
            }
        };
    }

    /**
     * Creates a data load handler for a failed request.
     *
     * This is necessary because the data load call happens via requestHandler so we can not be
     * sure whether the call completed or silently died. Adding the verify latch will ensure that
     * the call was actually successfully completed.
     *
     * @return The handler instance.
     */
    private IDataLoader.IDataLoadRequestHandler createFailedDataLoadHandler() {

        return new IDataLoader.IDataLoadRequestHandler() {
            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data data) {

                fail("Should have thrown an error");
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                verifyUtil.verified();
            }
        };
    }

}
