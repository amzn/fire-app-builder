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

import java.util.Set;

/**
 * Interface for Cache System. All cache systems must be wrapped with this interface before being
 * used.
 */
public interface ICacheManager<Key, Value> {

    /**
     * Put an object in Cache with an infinite expiration time.
     *
     * @param key   The key of the object.
     * @param value The value of the object.
     */
    void put(Key key, Value value);

    /**
     * Put an object in Cache with an expiration time.
     *
     * @param key             The key of the object.
     * @param value           The value of the object.
     * @param expirationInSec The expiration time.
     */
    void put(Key key, Value value, long expirationInSec);

    /**
     * Gets the value corresponding to the key. If the value has expired the key is removed from
     * cache and null is returned.
     *
     * @param key The key of the object.
     * @return The value for this key.
     */
    Value get(Key key);

    /**
     * Current size of cache. Implementations have the choice to define how the size is calculated.
     *
     * @return The current size of cache.
     */
    long size();

    /**
     * Update the size of the cache.
     *
     * @param newSize The new cache size.
     */
    void resize(int newSize);

    /**
     * Returns the current set max size of the cache.
     *
     * @return The max size set for the cache.
     */
    int maxSize();

    /**
     * Determines if the caches contains a valid value for the given key.
     *
     * @param key The key to be checked.
     * @return True if the key is present in cache and is not expired; false otherwise.
     */
    boolean containsKey(Key key);

    /**
     * Returns a set that contains valid keys from the cache whose values match the value in
     * question.
     *
     * @param value The value to be checked.
     * @return A set of keys whose value is the same as the value argument in the cache and is not
     * expired.
     */
    Set<Key> containsValue(Value value);

    /**
     * Removes the entry corresponding to key argument.
     *
     * @param key The key for which the entry needs to be removed.
     * @return The value of the removed key.
     */
    Value removeKey(Key key);

    /**
     * Clear the whole cache by removing all entries.
     */
    void clear();


}
