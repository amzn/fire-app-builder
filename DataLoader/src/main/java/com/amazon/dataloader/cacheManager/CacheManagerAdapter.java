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

import com.amazon.android.cache.ICacheManager;
import com.amazon.android.cache.MemoryBasedCacheManager;
import com.amazon.android.utils.Helpers;
import com.amazon.dataloader.datadownloader.IDataLoader;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.JsonHelper;
import com.amazon.utils.model.Data;

import android.os.AsyncTask;
import android.util.Base64;
import android.util.Log;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * This class is an adapter to be used in {@link com.amazon.dataloader.dataloadmanager
 * .DataLoadManager} to handle managing the cache. It uses {@link MemoryBasedCacheManager} as
 * underlying cache manager mechanism.
 */
public class CacheManagerAdapter implements IDataLoader {

    private static final String TAG = CacheManagerAdapter.class.getName();

    /**
     * The cache manager.
     */
    protected final ICacheManager<String, Data> mCacheManager;

    /**
     * Constructor. It requires the initial size of the cache. Send 0 if default size is desired.
     *
     * @param size Initial size of the cache, assign 0 if default size is desired.
     */
    public CacheManagerAdapter(int size) {

        if (size == 0) {
            mCacheManager = new MemoryBasedCacheManager<>();
        }
        else {
            mCacheManager = new MemoryBasedCacheManager<>(size);
        }
    }

    /**
     * Clears data from the cache.
     */
    public void clearCache() {

        mCacheManager.clear();
    }

    /**
     * Resize the cache manager.
     *
     * @param size The new cache size.
     */
    public void setCacheSize(int size) {

        mCacheManager.resize(size);
    }

    /**
     * Get the current size of the cache manager.
     *
     * @return The current cache size.
     */
    public long getCacheSize() {

        return mCacheManager.size();
    }

    /**
     * Get maximum size of the cache manager.
     *
     * @return The max size of the cache.
     */
    public long getMaxCacheSize() {

        return mCacheManager.maxSize();
    }

    /**
     * {@inheritDoc}
     *
     * Loads the data from the cache and returns it via the {@link com.amazon.dataloader
     * .datadownloader.IDataLoader.IDataLoadRequestHandler}. A null value is returned if the key is
     * not present in the cache. Any exception received is communicated via
     * {@link com.amazon.dataloader.datadownloader.IDataLoader.IDataLoadRequestHandler#onFailure
     * (Recipe, String[], Throwable)}.
     */
    @Override
    public boolean loadData(Recipe dataLoadRecipe, String[] params, IDataLoadRequestHandler
            requestHandle) {

        try {
            String key = generateKey(dataLoadRecipe, params);
            Data data = mCacheManager.get(key);
            requestHandle.onSuccess(dataLoadRecipe, params, data);
            return true;
        }
        catch (Exception e) {
            requestHandle.onFailure(dataLoadRecipe, params, e);
            return false;
        }
    }

    /**
     * Stores the data as specified by the {@link Recipe}.
     *
     * @param dataLoadRecipe The recipe.
     * @param params         Parameters that are required for the recipe.
     * @param data           The data that was fetched for the recipe.
     */
    public void storeData(Recipe dataLoadRecipe, String[] params, Data data) throws
            NoSuchAlgorithmException {

        String key = generateKey(dataLoadRecipe, params);
        mCacheManager.put(key, data);
    }

    /**
     * Stores the data corresponding to the {@link Recipe} asynchronously.
     * This methods uses an {@link android.os.AsyncTask} to perform the storing of the data.
     * This method fails silently (with logging) if the data storage fails.
     *
     * @param dataLoadRecipe The recipe.
     * @param recipeParams   Parameters that are required for the recipe.
     * @param data           The data that was fetched for the recipe.
     */
    public void storeDataAsync(final Recipe dataLoadRecipe, final String[] recipeParams,
                               final Data data) {

        new AsyncTask<Void, Void, Void>() {
            @Override
            protected Void doInBackground(Void... params) {

                try {
                    storeData(dataLoadRecipe, recipeParams, data);
                }
                catch (Exception e) {
                    Log.e(TAG, "Failed to put data in cache for recipe ", e);
                }
                return null;
            }
        }.executeOnExecutor(AsyncTask.THREAD_POOL_EXECUTOR);
    }

    /**
     * Computes a key for the recipe and parameters required by the recipe.
     * It calculates the Base63 encoded SHA1 of the recipe and each parameter and appends them
     * together to get the final key.
     *
     * @param dataLoadRecipe The recipe.
     * @param params         Parameters that are required for the recipe.
     * @return Key generated for recipe and params.
     */
    protected static String generateKey(Recipe dataLoadRecipe, String[] params) throws
            NoSuchAlgorithmException {

        String sha1ForRecipe = computeSha1(JsonHelper.mapToString(dataLoadRecipe.getMap()));
        StringBuilder builder = new StringBuilder(sha1ForRecipe);

        if (params != null) {
            for (String param : params) {
                builder.append(computeSha1(param));

            }
        }
        return builder.toString();
    }

    /**
     * Algorithm to compute sha1 of a string and return a Base64 encoding.
     *
     * @param str String to compute the sha1 for.
     * @return Base64 encoding of the sha1.
     */
    private static String computeSha1(String str) throws NoSuchAlgorithmException {

        MessageDigest digest = MessageDigest.getInstance("SHA-1");
        digest.reset();
        digest.update(str.getBytes(Helpers.getDefaultAppCharset()));
        return Base64.encodeToString(digest.digest(), Base64.DEFAULT);
    }
}
