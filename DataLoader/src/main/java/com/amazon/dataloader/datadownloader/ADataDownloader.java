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

import com.amazon.android.recipe.Recipe;
import com.amazon.utils.model.Data;

import android.content.Context;

/**
 * Abstract class for data downloaders. All implementations must implement {@link
 * #createInstance(Context)} method to provide a concrete implementation for {@link
 * UrlGeneratorFactory#createUrlGenerator(Context, String)}. If the implementation intends to be a
 * singleton, override the {@link #isSingleton()} method to return true and provide the singleton
 * instance via {@link #getInstance(Context)}.
 */
public abstract class ADataDownloader extends AObjectCreator implements IDataLoader {

    /**
     * {@inheritDoc}
     *
     * Constructs an {@link ADataDownloader}.
     *
     * @param context The context.
     */
    public ADataDownloader(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean loadData(Recipe dataLoadRecipe, String[] params, IDataLoadRequestHandler
            requestHandle) {

        try {
            Data data = fetchData(dataLoadRecipe);
            requestHandle.onSuccess(dataLoadRecipe, params, data);
            return true;
        }
        catch (Exception e) {
            requestHandle.onFailure(dataLoadRecipe, params, e);
            return false;
        }
    }

    /**
     * Fetches the {@link Data} for this data downloader.
     *
     * @param dataLoadRecipe The data load recipe.
     * @return The downloaded {@link Data}.
     * @throws Exception if there was an error while fetching the data.
     */
    protected abstract Data fetchData(Recipe dataLoadRecipe) throws Exception;

    /**
     * Searches for a key in the recipe. If the key is not found in the recipe, it searches
     * for the key in the configuration map. If the key is not found in the configuration map, an
     * error is thrown.
     *
     * @param recipe The recipe in which to search for the key in.
     * @param key    The key to search for.
     * @return The value associated with the key.
     * @throws IllegalArgumentException If the key is not found.
     */
    protected String getKey(Recipe recipe, String key) {

        if (recipe.containsItem(key)) {
            return recipe.getItemAsString(key);
        }
        if (mConfiguration.containsItem(key)) {
            return mConfiguration.getItemAsString(key);
        }
        throw new IllegalArgumentException(key + " could not be found");
    }
}
