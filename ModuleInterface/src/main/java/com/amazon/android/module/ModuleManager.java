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

/**
 * Singleton Module manager class
 */
public class ModuleManager {

    private static final String TAG = ModuleManager.class.getSimpleName();

    /**
     * Singleton.
     */
    private static ModuleManager instance = new ModuleManager();

    /**
     * Map of modules.
     */
    private HashMap<String, Module> mModules = new HashMap<>();

    /**
     * Singleton constructor.
     */
    private ModuleManager() {

    }

    /**
     * Get singleton instance.
     *
     * @return Singleton instance.
     */
    public static ModuleManager getInstance() {

        return instance;
    }

    /**
     * Set module by name.
     *
     * @param name   Module name.
     * @param module Module instance.
     */
    public void setModule(String name, Module module) {

        mModules.put(name, module);
    }

    /**
     * Get module by name.
     *
     * @param name Module name.
     * @return Module instance.
     */
    public Module getModule(String name) {

        return mModules.get(name);
    }

}
