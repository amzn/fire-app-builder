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
package com.amazon.android.recipe;

import android.os.Bundle;

/**
 * IRecipeCookerCallbacks defines the callbacks which are triggered by Recipe cooker.
 */
public interface IRecipeCookerCallbacks {

    /**
     * This is called before cooker starts cooking the recipe.
     *
     * @param recipe Recipe passed for cooking.
     * @param output Result object of pre-cooking.
     * @param bundle Result extras.
     */
    void onPreRecipeCook(Recipe recipe, Object output, Bundle bundle);

    /**
     * This method is called when cooking starts generating results.
     *
     * @param recipe Recipe passed for cooking.
     * @param output Result object of cooking.
     * @param bundle Result extras.
     * @param done   True if this is a single callback for all results.
     */
    void onRecipeCooked(Recipe recipe, Object output, Bundle bundle, boolean done);

    /**
     * This method is called after cooking is done.
     *
     * @param recipe Recipe passed for cooking.
     * @param output Result object of post-cooking.
     * @param bundle Result extras.
     */
    void onPostRecipeCooked(Recipe recipe, Object output, Bundle bundle);

    /**
     * This method is called if an error happens during cooking.
     *
     * @param recipe Recipe passed for cooking.
     * @param e      Exception.
     * @param msg    User readable message if any.
     */
    void onRecipeError(Recipe recipe, Exception e, String msg);
}
