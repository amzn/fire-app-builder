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
package com.amazon.auth;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * An interface for Authentication for an application.
 */
public interface IAuthentication {

    /**
     * Constant used to retrieve the access token.
     */
    String ACCESS_TOKEN = "access_token";

    /**
     * Constant used to retrieve the code.
     */
    String CODE = "code";

    /**
     * Constant used to define an error.
     */
    String ERROR = "error";

    /**
     * Initialize the authentication framework.
     *
     * @param context Context The application context.
     */
    void init(Context context);

    /**
     * Check if authentication can be done later.
     *
     * @return Return true if login process can be skipped.
     */
    boolean isAuthenticationCanBeDoneLater();

    /**
     * This returns the intent that should be started for authentication of the user.
     * Start the intent from an activity for result.
     * If activity result is RESULT_OK, start playback.
     *
     * @param context The context required to create the intent.
     * @return The intent for the authentication activity.
     */
    Intent getAuthenticationActivityIntent(Context context);

    /**
     * This method checks if the user is already logged in.
     *
     * @param context         The context to check if user is logged in.
     * @param responseHandler The callback interface
     */
    void isUserLoggedIn(Context context, ResponseHandler responseHandler);

    /**
     * This method checks if the resource is authorized for playback.
     *
     * @param context         The context to check for authorization.
     * @param resourceId      The id of the resource to verify authorization.
     * @param responseHandler The callback interface
     */
    void isResourceAuthorized(Context context, String resourceId, ResponseHandler responseHandler);

    /**
     * This method will log out the user. Resets or deletes any user login information.
     *
     * @param context         The context to logout the user.
     * @param responseHandler The callback interface
     */
    void logout(Context context, ResponseHandler responseHandler);

    /**
     * This method cancels all requests
     */
    void cancelAllRequests();

    /**
     * An interface to publish response for the request.
     */
    interface ResponseHandler {

        /**
         * Key passed in bundle for sending status code.
         */
        String STATUS_CODE = "status_code";
        /**
         * Value for no authorization response for requested resource.
         */
        int NO_AUTHORIZATION = 403;
        /**
         * Key passed in bundle for sending message.
         */
        String MESSAGE = "message";

        /**
         * This method is called upon successful execution of the request.
         *
         * @param extras The extras passed for successful execution.
         */
        void onSuccess(Bundle extras);

        /**
         * This method is called upon failed execution of the request.
         *
         * @param extras The extras passed for failed execution.
         */
        void onFailure(Bundle extras);
    }
}
