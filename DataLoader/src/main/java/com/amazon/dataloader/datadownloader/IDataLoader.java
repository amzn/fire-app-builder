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
package com.amazon.dataloader.datadownloader;

import com.amazon.android.recipe.Recipe;
import com.amazon.utils.model.Data;

/**
 * An interface for data loaders. Any component that wants to serve as a data provider needs to
 * implement this interface.
 */
public interface IDataLoader {

    /**
     * Exception class created for data loader failures.
     */
    class DataLoaderException extends Exception {

        /**
         * Constructs a {@link com.amazon.dataloader.datadownloader.IDataLoader
         * .DataLoaderException}.
         *
         * @param msg   The error message.
         * @param cause The cause of the exception.
         */
        public DataLoaderException(String msg, Throwable cause) {

            super(msg, cause);
        }

    }

    /**
     * This interface provides a way to add a call back for data load requests.
     */
    interface IDataLoadRequestHandler {

        /**
         * Callback for successful response from data loader.
         *
         * @param dataLoadRecipe {@link Recipe} for which this request was made.
         * @param params         Params passed on for the recipe.
         * @param data           {@link Data} received from data loader.
         */
        void onSuccess(Recipe dataLoadRecipe, String[] params, Data data);

        /**
         * Callback for failure response from data loader.
         *
         * @param dataLoadRecipe {@link Recipe} for which this request was made.
         * @param params         Params passed on for the recipe.
         * @param throwable      Throwable returned by the data loader.
         */
        void onFailure(Recipe dataLoadRecipe, String[] params, Throwable throwable);
    }

    /**
     * Requests that the data loader loads the data.
     *
     * @param dataLoadRecipe {@link Recipe} that defines the parameters for this load request.
     * @param params         The parameters required by the {@link Recipe}.
     * @param requestHandle  The call back mechanism for the data load request.
     * @return True if the data was loaded properly; false otherwise.
     */
    boolean loadData(Recipe dataLoadRecipe, String[] params, IDataLoadRequestHandler requestHandle);
}
