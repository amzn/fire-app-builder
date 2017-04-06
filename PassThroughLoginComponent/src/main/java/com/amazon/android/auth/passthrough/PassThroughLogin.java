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
package com.amazon.android.auth.passthrough;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.auth.IAuthentication;

/**
 * An authentication module that does not authenticate with any backend service. It should be
 * used when no authentication is required within the app.
 */
public class PassThroughLogin implements IAuthentication {

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    final static String IMPL_CREATOR_NAME = PassThroughLogin.class.getSimpleName();

    @Override
    public void init(Context context) {

    }

    /**
     * Since this is a pass through authentication module, login is not really required and can
     * therefore be done later.
     * @return True if authentication can be done later.
     */
    @Override
    public boolean isAuthenticationCanBeDoneLater() {

        return true;
    }

    @Override
    public Intent getAuthenticationActivityIntent(Context context) {

        return null;
    }

    @Override
    public void isUserLoggedIn(Context context, ResponseHandler responseHandler) {

        responseHandler.onSuccess(new Bundle());
    }

    @Override
    public void isResourceAuthorized(Context context, String resourceId, ResponseHandler
            responseHandler) {

        responseHandler.onSuccess(new Bundle());
    }

    @Override
    public void logout(Context context, ResponseHandler responseHandler) {

        responseHandler.onSuccess(new Bundle());
    }

    @Override
    public void cancelAllRequests() {

    }
}
