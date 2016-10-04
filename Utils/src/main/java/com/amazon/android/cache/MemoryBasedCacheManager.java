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
package com.amazon.android.cache;

import android.util.LruCache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * CacheManager which stores the cache in memory.
 * It does not provide support for key expiration.
 * It is backed by {@link LruCache}
 */
public class MemoryBasedCacheManager<Key, Value> implements ICacheManager<Key, Value> {

    private static final int DEFAULT_MAX_SIZE = 4 * 1024 * 1024; //4MiB
    protected final LruCache<Key, Value> mLruCache;

    /**
     * Default Constructor which sets the size of cache to {@link #DEFAULT_MAX_SIZE}
     */
    public MemoryBasedCacheManager() {

        mLruCache = new LruCache<>(DEFAULT_MAX_SIZE);
    }

    /**
     * Constructor which takes the initial size of the cache as parameter.
     * If the size of the cache tends to go beyond this, the cache starts removing the oldest
     * elements.
     * The size can be increased over time using {@link #resize(int)}
     *
     * @param size Initial size of cache to start with
     */
    public MemoryBasedCacheManager(int size) {

        mLruCache = new LruCache<>(size);
    }

    /**
     * If the size of the cache tends to go beyond the defined size, the cache first removes the
     * oldest elements, then adds this new value.
     * This method can also be used to override existing keys.
     * {@inheritDoc}
     */
    @Override
    public void put(Key key, Value value) {

        mLruCache.put(key, value);
    }

    /**
     * Not supported in this version
     */
    @Override
    public void put(Key key, Value value, long expirationInSec) {

        throw new UnsupportedOperationException("This method is not supported in this version");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value get(Key key) {

        return mLruCache.get(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public long size() {

        return mLruCache.size();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void resize(int newSize) {

        mLruCache.resize(newSize);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int maxSize() {

        return mLruCache.maxSize();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean containsKey(Key key) {

        return mLruCache.get(key) != null;
    }

    /**
     * This method goes through the whole cache to find the keys with desired values, and hence is
     * quite expensive.
     * {@inheritDoc}
     */
    @Override
    public Set<Key> containsValue(Value value) {

        Set<Key> keySet = new HashSet<>();

        //get a snapshot of the cache and loop through it to find desired keys
        Map<Key, Value> cacheMap = mLruCache.snapshot();
        for (Key key : cacheMap.keySet()) {
            if (cacheMap.get(key) != null && cacheMap.get(key).equals(value)) {
                keySet.add(key);
            }
        }
        return keySet;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Value removeKey(Key key) {

        return mLruCache.remove(key);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void clear() {

        mLruCache.evictAll();
    }
}
