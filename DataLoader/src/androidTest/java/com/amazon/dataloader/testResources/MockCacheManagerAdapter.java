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

import com.amazon.dataloader.cacheManager.CacheManagerAdapter;
import com.amazon.utils.model.Data;

import java.util.HashMap;
import java.util.Map;

/**
 * A class that mocks {@link CacheManagerAdapter} required for testing.
 */
public class MockCacheManagerAdapter extends CacheManagerAdapter {

    /**
     * An instance of {@link MockCacheManagerAdapter}.
     */
    public static MockCacheManagerAdapter mockCacheManagerAdapter;

    /**
     * The cache map.
     */
    public Map<String, Data> cacheMap = new HashMap<>();

    /**
     * Constructor. It requires the initial size of the cache. Send 0 if default size is desired.
     *
     * @param size Initial size of the cache, assign 0 if default size is desired.
     */
    public MockCacheManagerAdapter(int size) {

        super(size);
    }
}
