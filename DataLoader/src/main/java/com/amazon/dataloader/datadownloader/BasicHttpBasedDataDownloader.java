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

import com.amazon.dataloader.R;
import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.utils.model.Data;

import android.content.Context;
import android.util.Log;

import java.io.IOException;
import java.util.Collections;
import java.util.Map;

/**
 * This class represents a basic HTTP-based data downloader. It receives a URL from the URL
 * generator, fetches the content from that URL, and returns the content.
 */
public class BasicHttpBasedDataDownloader extends ADataDownloader {

    /**
     * Key to locate the URL generator implementation.
     */
    protected static final String URL_GENERATOR_IMPL = "url_generator_impl";

    /**
     * Key to locate the URL generator.
     */
    protected static final String URL_GENERATOR_RECIPE = "url_generator";

    /**
     * Debug tag.
     */
    private static final String TAG = BasicHttpBasedDataDownloader.class.getSimpleName();

    /**
     * {@link AUrlGenerator} instance.
     */
    private final AUrlGenerator mUrlGenerator;

    /**
     * Constructor for {@link BasicHttpBasedDataDownloader}. It initializes the URL generator using
     * the URL generator implementation defined in the configuration.
     *
     * @param context The context.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     * @throws DataLoaderException    If there was an error while creating the URL generator.
     */
    public BasicHttpBasedDataDownloader(Context context) throws ObjectCreatorException,
            DataLoaderException {

        super(context);
        try {
            String urlGeneratorClassPath = mConfiguration.getItemAsString(URL_GENERATOR_IMPL);

            this.mUrlGenerator = UrlGeneratorFactory.createUrlGenerator(mContext,
                                                                        urlGeneratorClassPath);
        }
        catch (UrlGeneratorFactory.UrlGeneratorInitializationFailedException e) {
            throw new DataLoaderException("Exception in initialization of " +
                                                  "BasicHttpBasedDataDownloader ", e);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return mContext.getString(R.string.basic_http_downloader_config_file_path);
    }

    /**
     * Creates an instance of this class.
     *
     * @param context The context.
     * @return The {@link BasicHttpBasedDataDownloader} instance.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public static ADataDownloader createInstance(Context context) throws ObjectCreatorException {

        try {
            return new BasicHttpBasedDataDownloader(context);
        }
        catch (DataLoaderException e) {
            throw new ObjectCreatorException("Exception while creating instance ", e);
        }
    }

    /**
     * This downloader is quite basic and does not require any parameters for itself from the
     * recipe. However, the url generator may require parameters from recipe so it locates the
     * url_generator map from the recipe and sends it to the URL generator. It accepts a URL from
     * the URL generator and downloads the data from that URL.
     */
    protected Data fetchData(Recipe dataLoadRecipe) throws AUrlGenerator.UrlGeneratorException,
            IOException {

        // Starting with an empty map and replacing it with a map from recipe if one exists.
        Map urlGeneratorRecipeMap = Collections.emptyMap();
        if (dataLoadRecipe.getMap().containsKey(URL_GENERATOR_RECIPE)) {
            urlGeneratorRecipeMap = (Map) dataLoadRecipe.getMap().get(URL_GENERATOR_RECIPE);
        }
        // Get the url.
        String url = mUrlGenerator.getUrl(urlGeneratorRecipeMap);
        Log.d(TAG, "url: " + url);
        return Data.createDataForPayload(NetworkUtils.getDataLocatedAtUrl(url));
    }

}
