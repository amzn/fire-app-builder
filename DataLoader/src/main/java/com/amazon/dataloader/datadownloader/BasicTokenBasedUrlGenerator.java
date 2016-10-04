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
import com.amazon.android.utils.NetworkUtils;
import com.amazon.android.utils.PathHelper;

import android.content.Context;

import java.io.IOException;
import java.util.Map;

/**
 * Generates a URL string using a token that was fetched from another URL. It makes an HTTP call to
 * a {@link #TOKEN_GENERATION_URL} and receives the token. It then adds the token to the {@link
 * #BASE_URL} to generate the final URL. The base URL must include the location of where to inject
 * the token using the following pattern: {@code $$token$$}. An example base URL would look like:
 * {@code https://exampleurl.com/?token=$$token$$&&1=2}. The {@link #getUrl(Map)} method expects a
 * {@link #BASE_URL} and {@link #TOKEN_GENERATION_URL} in the param map. If they are not included
 * in the map, the configuration file is searched for these keys.
 */
public class BasicTokenBasedUrlGenerator extends AUrlGenerator {

    /**
     * Key for retrieving the base URL.
     */
    protected static final String BASE_URL = "base_url";

    /**
     * Key for retrieving the token generation URL.
     */
    protected static final String TOKEN_GENERATION_URL = "token_generation_url";

    /**
     * Constructs a {@link BasicTokenBasedUrlGenerator}.
     *
     * @param context The context.
     * @throws ObjectCreatorException Any exception generated while fetching this instance will be
     *                                wrapped in this exception.
     */
    public BasicTokenBasedUrlGenerator(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return context.getString(R.string.basic_token_based_url_generator_config_file_path);
    }

    /**
     * Provides a concrete implementation of {@link BasicTokenBasedUrlGenerator}.
     *
     * @param context The context.
     * @return The {@link BasicTokenBasedUrlGenerator} instance.
     * @throws ObjectCreatorException Any exception generated while fetching this instance.
     */
    public static AUrlGenerator createInstance(Context context) throws ObjectCreatorException {

        return new BasicTokenBasedUrlGenerator(context);
    }

    /**
     * {@inheritDoc}
     *
     * This method creates the required URL by fetching the token from the network using the
     * {@link #TOKEN_GENERATION_URL} and injecting it to the base URL.
     * The base URL must include the location to inject the token using the pattern: $$token$$.
     * An example base URL may look like: {@code https://exampleurl.com/?token=$$token$$&&1=2}.
     * {@link #BASE_URL} and {@link #TOKEN_GENERATION_URL} are expected to be in the param map
     * argument. If they are not, {@link #mConfiguration} is checked. If the keys could not be
     * found, an exception is thrown.
     */
    @Override
    public String getUrl(Map params) throws UrlGeneratorException {

        try {
            String token_generation_url = getKey(params, TOKEN_GENERATION_URL);
            // Fetch token using token_generation_url
            String token = requestToken(token_generation_url);
            String baseUrl = getKey(params, BASE_URL);

            String[] arr = new String[]{token};
            // The PathHelper.injectParameters requires the injection key to be in the form of 'par'
            baseUrl = baseUrl.replace("$$token$$", "$$par0$$");
            return PathHelper.injectParameters(baseUrl, arr);
        }
        catch (Exception e) {
            throw new UrlGeneratorException("Could not fetch Url", e);
        }
    }

    /**
     * Request the token from the token generation URL.
     *
     * @param tokenGenerationUrl The URL to request the token from.
     * @return The fetched token.
     * @throws IOException if an error occurred while getting the data located at the URL.
     */
    protected String requestToken(String tokenGenerationUrl) throws IOException {

        return NetworkUtils.getDataLocatedAtUrl(tokenGenerationUrl);
    }

}
