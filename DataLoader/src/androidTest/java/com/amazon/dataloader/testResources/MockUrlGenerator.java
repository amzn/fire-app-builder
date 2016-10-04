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
package com.amazon.dataloader.testResources;

import com.amazon.dataloader.datadownloader.AUrlGenerator;

import android.content.Context;

import java.util.Map;

/**
 * Mock implementation of {@link AUrlGenerator} used for testing.
 */
public class MockUrlGenerator extends AUrlGenerator {

    /**
     * An instance of {@link MockUrlGenerator}.
     */
    public static MockUrlGenerator mMockUrlGenerator;

    /**
     * Constructs a {@link MockUrlGenerator} object.
     *
     * @param context The context.
     */
    public MockUrlGenerator(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * Returns the instance of {@link AUrlGenerator} as a {@link MockUrlGenerator}.
     *
     * @param context The context.
     * @return An {@link AUrlGenerator} implementation.
     */
    public static MockUrlGenerator createInstance(Context context) {

        return mMockUrlGenerator;
    }

    /**
     * {@inheritDoc}
     *
     * @param context The application context.
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return "configurations/TestConfig.json";
    }

    /**
     * {@inheritDoc}
     *
     * @param params Any params that need to be passed to this method can be passed via this map.
     * @return This implementation returns null.
     */
    @Override
    public String getUrl(Map params) throws UrlGeneratorException {

        return null;
    }

}
