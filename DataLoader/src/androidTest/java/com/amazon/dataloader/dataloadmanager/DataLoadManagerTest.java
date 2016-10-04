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

import com.amazon.android.recipe.IRecipeCookerCallbacks;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;
import com.amazon.dataloader.R;
import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.dataloader.datadownloader.ADataDownloader;
import com.amazon.dataloader.datadownloader.IDataLoader.IDataLoadRequestHandler;
import com.amazon.dataloader.testResources.MockCacheManagerAdapter;
import com.amazon.dataloader.testResources.MockDataDownloader;
import com.amazon.dataloader.testResources.MockDataLoadManager;
import com.amazon.dataloader.testResources.VerifyUtil;
import com.amazon.utils.model.Data;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import android.content.Context;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;
import android.util.Log;

import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import static com.amazon.dataloader.testResources.MockCacheManagerAdapter.mockCacheManagerAdapter;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * This class tests the {@link DataLoadManager} class.
 */
@RunWith(AndroidJUnit4.class)
public class DataLoadManagerTest {

    protected Recipe recipe;
    protected VerifyUtil verifyUtil;
    protected Data mData;
    protected Context mContext = InstrumentationRegistry.getTargetContext();

    @Before
    public void setUp() throws Exception {

        recipe = mock(Recipe.class);
        verifyUtil = mock(VerifyUtil.class);
        MockDataDownloader.dataDownloader = mock(MockDataDownloader.class);
        MockCacheManagerAdapter.mockCacheManagerAdapter = mock(MockCacheManagerAdapter.class);
        MockCacheManagerAdapter.mockCacheManagerAdapter.cacheMap = new HashMap<>();
        mockCacheManagerToStoreData();
        mData = new Data();
        Data.Record content = new Data.Record();
        content.setPayload("Test Data");
        mData.setContent(content);
        mData.setIsComplete(true);
    }

    /**
     * This method mocks the process of a {@link CacheManagerAdapter} storing data.
     */
    private void mockCacheManagerToStoreData() throws NoSuchAlgorithmException {

        doAnswer(new Answer<Void>() {
            public Void answer(InvocationOnMock invocation) throws Throwable {

                mockCacheManagerAdapter.cacheMap.put("key", mData);
                return null;
            }
        }).when(mockCacheManagerAdapter).storeData(any(Recipe.class),
                                                   any(String[].class), any(Data.class));
    }

    /**
     * Tests that the default cache size is set even if cache size is not included in the
     * configuration.
     */
    @Test
    public void testCreateCacheManagerInstanceAndDownloaderInstance() throws Exception {

        Recipe config =
                Recipe.newInstance(FileHelper.readFile(mContext,
                                                       mContext.getString(
                                                               R.string.data_loader_config_file)));

        CacheManagerAdapter adapter = DataLoadManager.getInstance(mContext)
                                                     .createCacheManagerAdapterInstance(mContext,
                                                                                        config);
        ADataDownloader downloader = DataLoadManager.getInstance(mContext)
                                                    .createDataDownloaderInstance(mContext, config);
        assertNotEquals(0, adapter.getMaxCacheSize());
        assertEquals(downloader, MockDataDownloader.dataDownloader);
    }

    /**
     * Tests that the default cache size is overridden if the configuration specifies a cache size.
     */
    @Test
    public void testCreateCacheManagerInstanceWithConfigSize() throws Exception {

        Recipe config = mock(Recipe.class);
        when(config.containsItem(DataLoadManager.CACHE_SIZE)).thenReturn(true);
        when(config.getItemAsInt(DataLoadManager.CACHE_SIZE)).thenReturn(10);
        CacheManagerAdapter adapter = DataLoadManager.getInstance(mContext)
                                                     .createCacheManagerAdapterInstance(mContext,
                                                                                        config);
        assertEquals(10, adapter.getMaxCacheSize());
    }


    /**
     * Tests that data can be successfully stored in the cache and retrieved from the cache.
     */
    @Test
    public void testDataPresentInCache() throws Exception {

        mockRecipeForTask(DataLoadManager.LOAD_DATA);
        mockCacheManagerToReturnData();
        runRecipeOnTestDataLoadManager(createSuccessfulCookedRecipe(), 2);

    }


    /**
     * Tests the case where the cache does not contain the desired data, so the data must be
     * downloaded from the actual source. The source then returns the desired data.
     */
    @Test
    public void testDataNotPresentInCache() throws Exception {

        mockRecipeForTask(DataLoadManager.LOAD_DATA);
        mockCacheManagerToReturnNull();
        mockDataDownloaderToSucceed();
        runRecipeOnTestDataLoadManager(createSuccessfulCookedRecipe(), 3);
        assertTrue(mockCacheManagerAdapter.cacheMap.containsValue(mData));
    }

    /**
     * Tests the case where data retrieval from the cache fails, but the call to fetch data from
     * the source is successful.
     */
    @Test
    public void testCacheManagerFailure() throws Exception {

        mockRecipeForTask(DataLoadManager.LOAD_DATA);
        mockCacheManagerToFail();
        mockDataDownloaderToSucceed();
        runRecipeOnTestDataLoadManager(createSuccessfulCookedRecipe(), 3);
        assertTrue(mockCacheManagerAdapter.cacheMap.containsValue(mData));

    }

    /**
     * Tests the case where downloading data from source is successful.
     */
    @Test
    public void testSuccessfulDataDownload() throws Exception {

        mockRecipeForTask(DataLoadManager.DOWNLOAD_DATA);
        mockDataDownloaderToSucceed();
        runRecipeOnTestDataLoadManager(createSuccessfulCookedRecipe(), 2);
        assertTrue(mockCacheManagerAdapter.cacheMap.containsValue(mData));
    }

    /**
     * Tests the case where downloading data from source fails.
     */
    @Test
    public void testFailedDataDownload() throws Exception {

        mockRecipeForTask(DataLoadManager.DOWNLOAD_DATA);
        mockDataDownloaderToFail();
        runRecipeOnTestDataLoadManager(errorRecipeCallback(), 2);
    }

    /**
     * Tests successful registration and de-registration of {@link com.amazon.dataloader
     * .dataloadmanager.DataLoadManager.IDataUpdateListener}s.
     */
    @Test
    public void testUpdaterListenerRegisterationAndDeregistration() throws Exception {

        MockDataLoadManager.IDataUpdateListener dataUpdateListener =
                mock(DataLoadManager.IDataUpdateListener.class);

        MockDataLoadManager testDataLoadManager =
                new MockDataLoadManager(InstrumentationRegistry.getTargetContext());

        // Register update listener and validate successful registration
        testDataLoadManager.registerUpdateListener(dataUpdateListener);
        assertTrue("Listener not registered ", testDataLoadManager.isUpdateListenerRegistered
                (dataUpdateListener));

        // Deregister update listener and validate successful de-registration
        testDataLoadManager.deregisterUpdateListener(dataUpdateListener);
        assertFalse("Listener still registered ", testDataLoadManager.isUpdateListenerRegistered
                (dataUpdateListener));
    }

    /**
     * Helper method to return the task string when the recipe is asked for the task type.
     *
     * @param taskString Value of task type string to be returned.
     */
    private void mockRecipeForTask(String taskString) {

        when(recipe.getItemAsString(DataLoadManager.TASK)).thenReturn(taskString);
    }

    /**
     * Mocks {@link CacheManagerAdapter} to return successfully with actual data.
     */
    private void mockCacheManagerToReturnData() {

        when(MockCacheManagerAdapter.mockCacheManagerAdapter.loadData(any(Recipe.class), any
                (String[].class), any(IDataLoadRequestHandler.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {

                        Object[] args = invocation.getArguments();
                        IDataLoadRequestHandler requestHandle = (IDataLoadRequestHandler) args[2];
                        requestHandle.onSuccess((Recipe) args[0], (String[]) args[1], mData);
                        verifyUtil.verified();
                        return true;
                    }
                });
    }

    /**
     * Mocks {@link CacheManagerAdapter} to return an error.
     */
    private void mockCacheManagerToFail() {

        when(MockCacheManagerAdapter.mockCacheManagerAdapter.loadData(any(Recipe.class), any
                (String[].class), any(IDataLoadRequestHandler.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {

                        Object[] args = invocation.getArguments();
                        IDataLoadRequestHandler requestHandle = (IDataLoadRequestHandler) args[2];
                        requestHandle.onFailure((Recipe) args[0], (String[]) args[1],
                                                new Exception("CacheManager failed"));
                        verifyUtil.verified();
                        return false;
                    }
                });
    }

    /**
     * Mocks {@link CacheManagerAdapter} to return successfully with null data.
     */
    private void mockCacheManagerToReturnNull() {

        when(MockCacheManagerAdapter.mockCacheManagerAdapter.loadData(any(Recipe.class), any
                (String[].class), any(IDataLoadRequestHandler.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {

                        Object[] args = invocation.getArguments();
                        IDataLoadRequestHandler requestHandle = (IDataLoadRequestHandler) args[2];
                        requestHandle.onSuccess((Recipe) args[0], (String[]) args[1], null);
                        verifyUtil.verified();
                        return true;
                    }
                });
    }

    /**
     * Creates a recipe callback handler that expects a successful response with mock data. It
     * fails the test if its {@link IRecipeCookerCallbacks#onRecipeError(Recipe, Exception,
     * String)}
     * method is called
     *
     * @return A recipe callback handler which expects successful response with mock data.
     */
    private IRecipeCookerCallbacks createSuccessfulCookedRecipe() {

        return new IRecipeCookerCallbacks() {
            @Override
            public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done) {

                Data data = (Data) output;
                assertTrue(mData.equals(data));
                assertTrue(done);
                verifyUtil.verified();
            }

            @Override
            public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeError(Recipe recipe, Exception e, String msg) {

                Log.e("Test failed", msg, e);
                fail(e.getMessage());
            }
        };
    }

    /**
     * Creates a recipe callback handler that expects a failure response. It fails the test if its
     * {@link IRecipeCookerCallbacks#onRecipeCooked(Recipe, Object, Bundle, boolean)} method is
     * called.
     *
     * @return A recipe callback handler which expects failure response.
     */
    private IRecipeCookerCallbacks errorRecipeCallback() {

        return new IRecipeCookerCallbacks() {
            @Override
            public void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done) {

                fail("Should have thrown an error");
            }

            @Override
            public void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle) {

            }

            @Override
            public void onRecipeError(Recipe recipe, Exception e, String msg) {

                verifyUtil.verified();
            }
        };
    }

    /**
     * Calls the {@link MockDataLoadManager#cookRecipe(Recipe, Object, IRecipeCookerCallbacks,
     * Bundle, String[])} with the mock recipe and the param recipeCallback. It also verifies that
     * the {@link VerifyUtil#verified()} is called the required number of times.
     *
     * @param recipeCallback    The recipe callback handler to be passed to the {@link
     *                          DataLoadManager}.
     * @param verifyCalledCount Number of times the verified call should have been called.
     */
    private void runRecipeOnTestDataLoadManager(IRecipeCookerCallbacks recipeCallback, int
            verifyCalledCount) throws Exception {

        new MockDataLoadManager(mContext).cookRecipe(recipe, null, recipeCallback, null, null);
        Thread.sleep(1000);
        verify(verifyUtil, times(verifyCalledCount)).verified();
    }

    /**
     * Mocks {@link ADataDownloader#loadData(Recipe, String[], IDataLoadRequestHandler)} to return
     * successfully with actual data.
     */
    private void mockDataDownloaderToSucceed() {

        when(MockDataDownloader.dataDownloader.loadData(any(Recipe.class), any(String[].class),
                                                        any(IDataLoadRequestHandler.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {

                        Object[] args = invocation.getArguments();
                        IDataLoadRequestHandler requestHandle = (IDataLoadRequestHandler) args[2];
                        requestHandle.onSuccess((Recipe) args[0], (String[]) args[1], mData);
                        verifyUtil.verified();
                        return true;
                    }
                });
    }

    /**
     * Mocks {@link ADataDownloader#loadData(Recipe, String[], IDataLoadRequestHandler)} to return
     * a failure response.
     */
    private void mockDataDownloaderToFail() {

        when(MockDataDownloader.dataDownloader.loadData(any(Recipe.class), any(String[].class),
                                                        any(IDataLoadRequestHandler.class)))
                .thenAnswer(new Answer<Boolean>() {
                    @Override
                    public Boolean answer(InvocationOnMock invocation) throws Throwable {

                        Object[] args = invocation.getArguments();
                        IDataLoadRequestHandler requestHandle = (IDataLoadRequestHandler) args[2];
                        requestHandle.onFailure((Recipe) args[0], (String[]) args[1],
                                                new Exception("Failed data download"));
                        verifyUtil.verified();
                        return true;
                    }
                });
    }

}
