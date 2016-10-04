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

import com.amazon.dataloader.datadownloader.AObjectCreator;

import android.content.Context;

/**
 * Mock implementation of the {@link com.amazon.dataloader.datadownloader.AObjectCreator} abstract
 * class used for testing.
 */
public class MockObjectCreator extends AObjectCreator {

    /**
     * Constructs a {@link MockObjectCreator}.
     *
     * @param context The context.
     */
    public MockObjectCreator(Context context) throws ObjectCreatorException {

        super(context);
    }

    /**
     * {@inheritDoc}
     * @param context application context
     * @return
     */
    @Override
    protected String getConfigFilePath(Context context) {

        return "configurations/TestConfig.json";
    }

    /**
     * Returns the instance of {@link AObjectCreator} as a {@link MockObjectCreator}.
     *
     * @param context The context.
     * @return An {@link AObjectCreator} implementation.
     */
    public static AObjectCreator createInstance(Context context) throws ObjectCreatorException {

        return new MockObjectCreator(context);
    }
}
