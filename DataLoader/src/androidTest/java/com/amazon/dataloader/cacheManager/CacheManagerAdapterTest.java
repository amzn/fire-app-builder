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
package com.amazon.dataloader.cacheManager;

import com.amazon.dataloader.datadownloader.IDataLoader;
import com.amazon.dataloader.testResources.VerifyUtil;
import com.amazon.android.recipe.Recipe;
import com.amazon.utils.model.Data;

import org.junit.Before;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.fail;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

/**
 * This class tests the {@link CacheManagerAdapter} class.
 */
public class CacheManagerAdapterTest {

    private CacheManagerAdapter mCacheManagerAdapter;

    private String recipeString = "{\n" +
            "\t\"testKey\" : \"testValue\",\n" +
            "\t\"testKey1\" : [\n" +
            "\t\t\"testValue1\",\n" +
            "\t\t\"testValue2\"\n" +
            " \t],\n" +
            "\t\"testKey3\" : {\n" +
            "\t\t\"testKey4\" : “testValue4\"\n" +
            "\t }\n" +
            "}";

    private Recipe recipe = Recipe.newInstance(recipeString);
    private String[] params = new String[]{"param1", "param2", "param3"};
    private VerifyUtil verifyUtil;
    private Data testPayload = Data.createDataForPayload("testPayload");

    @Before
    public void setUp() {

        mCacheManagerAdapter = new CacheManagerAdapter(0);
        verifyUtil = mock(VerifyUtil.class);
    }

    /**
     * Tests that the default cache size for {@link CacheManagerAdapter} is not 0 even if its
     * constructed with a size of 0.
     */
    @Test
    public void testCacheManagerAdapterConstructorWithSizeZero() {

        CacheManagerAdapter adapter = new CacheManagerAdapter(0);
        assertNotEquals(0, adapter.getMaxCacheSize());
    }

    /**
     * Tests the default cache size is overridden if the {@link CacheManagerAdapter} is constructed
     * with a cache size greater than 0.
     */
    @Test
    public void testCreateCacheManagerInstanceWithConfigSize() throws Exception {

        CacheManagerAdapter adapter = new CacheManagerAdapter(10);
        assertEquals(10, adapter.getMaxCacheSize());
    }

    /**
     * Tests for non existent key.
     */
    @Test
    public void testLoadDataWithNonExistentKey() {

        mCacheManagerAdapter.loadData(recipe, params, createSuccessfulRequestHandle(null));
        verify(verifyUtil).verified();
    }

    /**
     * Tests the {@link CacheManagerAdapter#storeData(Recipe, String[], Data)} and {@link
     * CacheManagerAdapter#loadData(Recipe, String[], IDataLoader.IDataLoadRequestHandler)}
     * methods.
     */
    @Test
    public void testStoreDataAndLoadData() throws NoSuchAlgorithmException {

        mCacheManagerAdapter.storeData(recipe, params, testPayload);
        assertEquals(mCacheManagerAdapter.mCacheManager.get(mCacheManagerAdapter.generateKey
                (recipe, params)), testPayload);
        mCacheManagerAdapter.loadData(recipe, params, createSuccessfulRequestHandle(testPayload));
        verify(verifyUtil).verified();
    }

    /**
     * Tests the {@link CacheManagerAdapter#storeDataAsync(Recipe, String[], Data)} method.
     */
    @Test
    public void testStoreDataAsync() throws NoSuchAlgorithmException, InterruptedException {

        mCacheManagerAdapter.storeDataAsync(recipe, params, testPayload);
        Thread.sleep(1000);
        assertEquals(mCacheManagerAdapter.mCacheManager.get(mCacheManagerAdapter.generateKey
                (recipe, params)), testPayload);
    }

    /**
     * Tests that the {@link CacheManagerAdapter#loadData(Recipe, String[],
     * IDataLoader.IDataLoadRequestHandler)} method failure is propagated via recipe handler.
     */
    /*@Test
    public void testLoadDataFailure() {

        mCacheManagerAdapter.loadData(null, null, createFailedRequestHandle());
        verify(verifyUtil).verified();
    }*/

    /**
     * Test the {@link CacheManagerAdapter#setCacheSize(int)}} method.
     */
    @Test
    public void testReSize() {

        mCacheManagerAdapter.setCacheSize(10);
        assertEquals(mCacheManagerAdapter.mCacheManager.maxSize(), 10);
    }

    /**
     * Creates a request handler for a successful request completion.
     *
     * @param data Data expected to be received.
     * @return A successful request handler.
     */
    private IDataLoader.IDataLoadRequestHandler createSuccessfulRequestHandle(final Data data) {

        return new IDataLoader.IDataLoadRequestHandler() {

            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data actualData) {

                assertEquals(data, actualData);
                verifyUtil.verified();
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                fail(throwable.getMessage());
            }
        };
    }

    /**
     * Creates a request handler for a failed request completion.
     *
     * @return A failed request handler.
     */
    private IDataLoader.IDataLoadRequestHandler createFailedRequestHandle() {

        return new IDataLoader.IDataLoadRequestHandler() {

            @Override
            public void onSuccess(Recipe dataLoadRecipe, String[] params, Data actualData) {

                fail("Should have failed");
            }

            @Override
            public void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable) {

                verifyUtil.verified();
            }
        };
    }

}
