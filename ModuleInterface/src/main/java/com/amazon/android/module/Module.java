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
package com.amazon.android.module;

import java.util.HashMap;
import java.util.Map;

/**
 * Module class handles implementation creator(s) and implementations of
 * interface which defined by I.
 *
 * @param <I> Interface which this module responsible for.
 */
public class Module<I> {

    /**
     * This is the intent send by the app to modules for initialization.
     */
    public static final String INTENT_ACTION = "android.amazon.module.init";

    /**
     * Default ImplCreator.
     */
    private IImplCreator mImplCreator;

    /**
     * Map of implementations for the case where we have more then one.
     */
    private HashMap<String, I> mImpls = new HashMap<>();

    /**
     * Default implementation.
     */
    private I mImpl;

    /**
     * Map of ImplCreators for different implementations.
     */
    private HashMap<String, IImplCreator> mImplCreators = new HashMap<>();

    /**
     * Default constructor.
     */
    public Module() {

    }

    /**
     * Set ImplCreator by name.
     *
     * @param name        Name of the implementation creator which is set by the module.
     * @param implCreator Implementation creator instance.
     */
    public void setImplCreator(String name, IImplCreator implCreator) {
        // Set the first one as default.
        if (mImplCreator == null) {
            mImplCreator = implCreator;
        }
        mImplCreators.put(name, implCreator);
    }

    /**
     * Get the default ImplCreator.
     *
     * @return Implementation creator instance.
     */
    public IImplCreator getImplCreator() {

        return mImplCreator;
    }

    /**
     * Get ImplCreator by name.
     *
     * @param name Implementation creator name.
     * @return Implementation creator instance.
     */
    public IImplCreator getImplCreator(String name) {

        return mImplCreators.get(name);
    }

    /**
     * Create impl of I from the default ImplCreator.
     *
     * @return Implementation of interface I.
     */
    public I createImpl() {

        return (I) mImplCreator.createImpl();
    }

    /**
     * Create impl from the ImplCreator by name.
     *
     * @param name Implementation creator name.
     * @return Implementation of interface I.
     */
    public I createImpl(String name) {

        return (I) mImplCreators.get(name).createImpl();
    }

    /**
     * Set impl by name.
     *
     * @param name Implementation name.
     * @param i    Implementation interface.
     */
    public void setImpl(String name, I i) {
        // Set the first one as default.
        if (mImpl == null) {
            mImpl = i;
        }
        mImpls.put(name, i);
    }

    /**
     * Get default impl, create it depending on the flag.
     *
     * @param createIfNExists Defines if impl will be created if it does not exists.
     * @return Implementation interface.
     */
    public I getImpl(boolean createIfNExists) {

        if (createIfNExists && mImpl == null) {
            mImpl = createImpl();
        }
        return mImpl;
    }

    /**
     * Get impl by name.
     *
     * @param name Implementation name.
     * @return Implementation interface.
     */
    public I getImpl(String name) {

        return mImpls.get(name);
    }

    /**
     * Implementation iterator interface
     *
     * @param <I> Interface to be returned
     */
    public interface ImplIteratorListener<I> {

        void onImpl(I impl);
    }

    /**
     * Iterate through all implementations of this module.
     *
     * @param listener Implementation iterator listener function.
     */
    public void iterateImpls(ImplIteratorListener listener) {

        for (Map.Entry<String, I> impl : mImpls.entrySet()) {
            listener.onImpl(impl.getValue());
        }
    }
}
