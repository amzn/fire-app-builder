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

import com.amazon.dataloader.testResources.VerifyUtil;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.util.Log;

import java.io.IOException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link BasicFileBasedDataDownloader} class.
 */
public class BasicFileBasedDataDownloaderTest {

    private Recipe recipe;
    private VerifyUtil verifyUtil;
    private Context context = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() throws DataDownloaderFactory.DownloaderInitializationFailedException {

        recipe = mock(Recipe.class);
        verifyUtil = mock(VerifyUtil.class);
    }

    /**
     * Tests the {@link BasicFileBasedDataDownloader#createInstance(Context)} method provides
     * correct object.
     */
    @Test
    public void testCreateInstance() throws AObjectCreator.ObjectCreatorException {

        assertNotNull(BasicFileBasedDataDownloader.createInstance(context));
        assertTrue(BasicFileBasedDataDownloader.createInstance(context) instanceof
                           BasicFileBasedDataDownloader);
    }

    /**
     * Tests the {@link BasicFileBasedDataDownloader#loadData(Recipe, String[],
     * IDataLoader.IDataLoadRequestHandler)} method with a valid data file in the recipe.
     */
    @Test
    public void testLoadDataWithValidFileInRecipe() throws AObjectCreator.ObjectCreatorException,
            IOException {

        when(recipe.getItemAsString(BasicFileBasedDataDownloader.DATA_FILE_PATH)).thenReturn
                ("SampleData.json");
        loadDataHelper(createSuccessfulDataLoadHandler());
    }


    /**
     * Tests the {@link BasicFileBasedDataDownloader#loadData(Recipe, String[],
     * IDataLoader.IDataLoadRequestHandler)} method with a recipe that's missing a valid data file.
     */
    @Test
    public void testLoadDataWithValidFileNotInRecipe() throws AObjectCreator.ObjectCreatorException,
            IOException {

        loadDataHelper(createSuccessfulDataLoadHandler());
    }


    /**
     * Tests the {@link BasicFileBasedDataDownloader#loadData(Recipe, String[],
     * IDataLoader.IDataLoadRequestHandler)} method with an invalid data file in the recipe.
     */

    @Test
    public void testLoadDataWithInValidFile() throws AObjectCreator.ObjectCreatorException {

        when(recipe.containsItem(BasicFileBasedDataDownloader.DATA_FILE_PATH)).thenReturn(true);
        when(recipe.getItemAsString(BasicFileBasedDataDownloader.DATA_FILE_PATH)).thenReturn
                ("random");
        loadDataHelper(createFailedDataLoadHandler());
    }

    /**
     * Helper method to call {@link BasicFileBasedDataDownloader#loadData(Recipe, String[],
     * IDataLoader.IDataLoadRequestHandler)} with a {@link com.amazon.dataloader.datadownloader
     * .IDataLoader.IDataLoadRequestHandler} and verify the handler was called.
     */
    private void loadDataHelper(IDataLoader.IDataLoadRequestHandler requestHandler) throws
            AObjectCreator.ObjectCreatorException {

        BasicFileBasedDataDownloader downloader = new BasicFileBasedDataDownloader(context);
        downloader.loadData(recipe, new String[0], requestHandler);
        verify(verifyUtil).verified();
    }

    /**
     * Creates a sample {@link com.amazon.dataloader.datadownloader.IDataLoader
     * .IDataLoadRequestHandler} for successful requests.
     *
     * @return A successful instance of {@link com.amazon.dataloader.datadownloader.IDataLoader
     * .IDataLoadRequestHandler}
     */
    private IDataLoader.IDataLoadRequestHandler createSuccessfulDataLoadHandler() {

        // The expected Data from the call to match with actual data received from the call
        final Data expectedData;
        try {
            expectedData = Data.createDataForPayload(FileHelper.readFile(context,
                                                                         "SampleData.json"));
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
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
     * Creates a sample {@link com.amazon.dataloader.datadownloader.IDataLoader
     * .IDataLoadRequestHandler} for failed requests.
     *
     * @return A failure based instance of {@link com.amazon.dataloader.datadownloader.IDataLoader
     * .IDataLoadRequestHandler}
     */
    private IDataLoader.IDataLoadRequestHandler createFailedDataLoadHandler() {

        return new IDataLoader.IDataLoadRequestHandler() {
            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data data) {

                fail("Should have thrown an error.");
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                verifyUtil.verified();
            }
        };
    }
}
