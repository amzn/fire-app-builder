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

package com.amazon.auth.testresources;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.test.mock.MockContext;

import com.amazon.auth.IAuthentication;

/**
 * Mock authentication class for testing
 */
public class MockAuthentication implements IAuthentication {

    private String mAuthString = "";

    // Authentication strings to which the module will respond.
    private final String AUTH_OKAY = "authenticated";
    private final String AUTH_NOT_FOUND = "not_found";
    private final String AUTH_UNAUTHORIZED = "unauthorized";

    // Using HTTP status codes for the time being.
    private final int STATUS_AUTHENTICATED = 200;
    private final int STATUS_NOT_FOUND = 404;
    private final int STATUS_UNAUTHORIZED = 401;
    private final int STATUS_UNKNOWN = 520;

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllRequests() {
        // Not applicable in this case
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void init (Context context) {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthenticationCanBeDoneLater() {
        return true;
    }

    @Override
    public void isUserLoggedIn(final Context context, final ResponseHandler responseHandler) {
        final Bundle bundle = new Bundle();
        if (!mAuthString.equals("")) {
            responseHandler.onSuccess(bundle);
        } else {
            responseHandler.onFailure(bundle);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void logout(Context context, ResponseHandler responseHandler) {
        setAuthString("");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Intent getAuthenticationActivityIntent(Context context) {
        return new Intent(context, MockContext.class);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void isResourceAuthorized(Context context, String resourceId, ResponseHandler
            responseHandler) {
        // Not applicable in this case
        responseHandler.onSuccess(new Bundle());
    }

    /**
     * @return the authentication string
     */
    public String getAuthString() {
        return mAuthString;
    }

    /**
     * @param newString the authentication string to be set
     */
    public void setAuthString(String newString) {
        mAuthString = newString;
    }

    /**
     * @param req String specifying request type
     * @return the status code
     */
    public int getResponse(String req) {
        switch (req) {
            case AUTH_OKAY:
                return STATUS_AUTHENTICATED;
            case AUTH_NOT_FOUND:
                return STATUS_NOT_FOUND;
            case AUTH_UNAUTHORIZED:
                return STATUS_UNAUTHORIZED;
        }
        return STATUS_UNKNOWN;
    }

}
