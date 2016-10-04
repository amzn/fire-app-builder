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

import rx.Observable;

/**
 * IRecipeCooker interface defines what a Recipe cooker is.
 */
public interface IRecipeCooker {

    /**
     * Cook recipe method, each cooker must implement.
     *
     * @param recipe                List of instructions to achieve certain cooking.
     * @param input                 Input object for cooking.
     * @param recipeCookerCallbacks Recipe cooking callbacks.
     * @param bundle                Extra data.
     * @param params                Parameters which is required within recipe instructions.
     * @return Result of cooking acceptance.
     */
    boolean cookRecipe(Recipe recipe, Object input, IRecipeCookerCallbacks recipeCookerCallbacks,
                       Bundle bundle, String[] params);

    /**
     * Cook recipe observable for reactive programming.
     *
     * @param recipe List of instructions to achieve certain cooking.
     * @param input  Input object for cooking.
     * @param bundle Extra data.
     * @param params Parameters which is required within recipe instructions.
     * @return Cook recipe RX Observable.
     */
    Observable<Object> cookRecipeObservable(Recipe recipe,
                                            final Object input,
                                            final Bundle bundle,
                                            final String[] params);

    /**
     * Get cooker name.
     *
     * @return Cooker name.
     */
    String getName();
}
