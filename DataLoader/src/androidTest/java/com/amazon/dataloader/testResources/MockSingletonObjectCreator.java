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
 * A mock implementation of the {@link AObjectCreator} abstract class that uses the singleton
 * pattern.
 */
public class MockSingletonObjectCreator extends AObjectCreator {

    /**
     * Constructs a {@link MockSingletonObjectCreator} object.
     * @param context
     * @throws ObjectCreatorException
     */
    public MockSingletonObjectCreator(Context context) throws ObjectCreatorException {

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
     * Returns the instance of {@link AObjectCreator} as a {@link MockSingletonObjectCreator}.
     *
     * @param context The context.
     * @return An {@link AObjectCreator} implementation.
     */
    public static AObjectCreator getInstance(Context context) throws ObjectCreatorException {

        return new MockSingletonObjectCreator(context);
    }

    /**
     * Determines if the calling instance is a singleton or not.
     * @return True.
     */
    public static boolean isSingleton() {

        return true;
    }
}
