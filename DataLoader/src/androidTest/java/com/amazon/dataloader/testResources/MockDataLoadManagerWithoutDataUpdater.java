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

import com.amazon.android.recipe.Recipe;
import com.amazon.android.utils.FileHelper;

import android.content.Context;

import java.io.IOException;

/**
 * Mock Data load manager without data updater configured
 */
public class MockDataLoadManagerWithoutDataUpdater extends MockDataLoadManager {

    public MockDataLoadManagerWithoutDataUpdater(Context context) throws Exception {

        super(context);
    }

    @Override
    protected Recipe createDataLoadManagerConfigInstance(Context context) throws IOException {

        return Recipe.newInstance(FileHelper.readFile(context,
                                                      "configurations/DataLoadManagerConfigNoDataUpdater.json"));
    }
}
