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
package com.amazon.loginwithamazon;

import com.amazon.auth.IAuthentication;
import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;


public class LoginWithAmazonAuthenticationTest {

    private LoginWithAmazonAuthentication mLoginWithAmazonAuthentication;

    @Before
    public void setUp() throws Exception {

        mLoginWithAmazonAuthentication = new LoginWithAmazonAuthentication();
        mLoginWithAmazonAuthentication.init(InstrumentationRegistry.getContext());
    }

    /**
     * This test currently does nothing.
     */
    @Test
    public void testConfigure() throws Exception {

        mLoginWithAmazonAuthentication.init(InstrumentationRegistry.getContext());
    }

    /**
     * This test ensures that the right activity is returned.
     */
    @Test
    public void testGetAuthenticationActivityIntent() throws Exception {

        Intent tempIntent = mLoginWithAmazonAuthentication
                .getAuthenticationActivityIntent(InstrumentationRegistry.getContext());
        assertTrue("We should get back a LoginInWithAmazonActivity",
                   tempIntent.getComponent()
                             .getClassName()
                             .equals("com.amazon.loginwithamazon.LoginWithAmazonActivity"));

    }

    /**
     * Test the is user logged in functionality.
     */
    @Test
    public void testIsUserLoggedIn() throws Exception {

        // Check if the user is logged in. This should be false.
        mLoginWithAmazonAuthentication.isUserLoggedIn(
                InstrumentationRegistry.getContext(),
                new IAuthentication.ResponseHandler() {

                    @Override
                    public void onSuccess(Bundle extras) {

                        // Fail if the user is logged in.
                        fail("The user is logged in");
                    }

                    @Override
                    public void onFailure(Bundle extras) {

                        // Pass if the user is not logged in.
                        assertTrue(true);
                    }
                });
    }

    /**
     * The method {@link LoginWithAmazonAuthentication#isResourceAuthorized(Context, String,
     * IAuthentication.ResponseHandler)} is not needed by Login With Amazon.
     * The method should behave like {@link LoginWithAmazonAuthenticationTest#testIsUserLoggedIn()}
     */
    @Test
    public void testIsResourceAuthorized() throws Exception {

        // Check if the user is logged in. This should be false.
        mLoginWithAmazonAuthentication.isResourceAuthorized(
                InstrumentationRegistry.getContext(),
                "temp",
                new IAuthentication.ResponseHandler() {

                    @Override
                    public void onSuccess(Bundle extras) {

                        assertTrue(true);
                    }

                    @Override
                    public void onFailure(Bundle extras) {

                        fail("Returned on Failure");
                    }
                });
    }

    /**
     * Test the logout functionality.
     * To use this method testers need to have a separate API with the package name set to:
     * "amazon.loginwithamazon.test"
     */
    @Test
    public void testLogout() throws Exception {

        mLoginWithAmazonAuthentication.logout(
                InstrumentationRegistry.getContext(),
                new IAuthentication.ResponseHandler() {

                    @Override
                    public void onSuccess(Bundle extras) {

                        // Fail is the user is able to log out.
                        assertFalse(true);
                    }

                    @Override
                    public void onFailure(Bundle extras) {

                        // Pass as we should not be able to logout.
                        assertTrue(true);
                    }
                });


        // Create the scope of the data we want back.
        final String[] APP_SCOPES = {"profile"};

        // Create an auth manager
        AmazonAuthorizationManager authManager =
                new AmazonAuthorizationManager(InstrumentationRegistry.getTargetContext(),
                                               Bundle.EMPTY);


        // Login With Amazon.
        authManager.authorize(APP_SCOPES, Bundle.EMPTY, new AuthListener());

        // We should be able to logout now.
        mLoginWithAmazonAuthentication.logout(
                InstrumentationRegistry.getContext(),
                new IAuthentication.ResponseHandler() {

                    @Override
                    public void onSuccess(Bundle extras) {

                        // Pass as we have logged out.
                        assertTrue(true);
                    }

                    @Override
                    public void onFailure(Bundle extras) {

                        // Fail if we are unable to logout.
                        assertFalse(true);
                    }
                });
    }

    /**
     * This is a dummy class that is needed for {@link #testLogout()}
     */
    private class AuthListener implements AuthorizationListener {

        @Override
        public void onCancel(Bundle bundle) {

        }

        @Override
        public void onSuccess(Bundle bundle) {

        }

        @Override
        public void onError(AuthError authError) {

        }
    }
}