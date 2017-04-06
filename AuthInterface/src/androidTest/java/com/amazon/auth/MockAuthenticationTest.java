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
import android.os.Bundle;
import android.test.InstrumentationTestCase;

import com.amazon.auth.testresources.MockAuthentication;

/**
 * Unit tests for mock authentication module
 */
public class MockAuthenticationTest extends InstrumentationTestCase {

    private MockAuthentication mAuth;
    private Context mContext;

    private final String TEST_STRING = "test";

    // Authentication strings to which the module will respond.
    private final String AUTH_OKAY = "authenticated";
    private final String AUTH_NOT_FOUND = "not_found";
    private final String AUTH_UNAUTHORIZED = "unauthorized";

    // Using HTTP status codes for the time being.
    private final int STATUS_AUTHENTICATED = 200;
    private final int STATUS_NOT_FOUND = 404;
    private final int STATUS_UNAUTHORIZED = 401;
    private final int STATUS_UNKNOWN = 520;

    public void setUp() throws Exception {

        super.setUp();
        mAuth = new MockAuthentication();
        mContext = getInstrumentation().getContext();
    }

    public void tearDown() throws Exception {

        mAuth = null;
        mContext = null;
    }

    /**
     * Tests the {@link MockAuthentication#isUserLoggedIn(Context, IAuthentication.ResponseHandler)}
     * method
     */
    public void testIsUserLoggedIn() throws Exception {

        mAuth.setAuthString("test");
        mAuth.isUserLoggedIn(mContext, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                assertTrue("User should be logged in", true);
            }

            @Override
            public void onFailure(Bundle extras) {

                fail("Used should not be logged out");
            }
        });
    }

    /**
     * Tests the {@link MockAuthentication#logout(Context, IAuthentication.ResponseHandler)} method
     */
    public void testLogout() throws Exception {

        mAuth.logout(mContext, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                assertTrue("onSuccess() in logout() should be triggered", true);
                assertEquals("Authentication data should be empty", "", mAuth.getAuthString());
            }

            @Override
            public void onFailure(Bundle extras) {

                fail("onFailure() in logout() should not be triggered");
            }
        });
    }

    /**
     * Tests the {@link MockAuthentication#isAuthenticationCanBeDoneLater()} method
     */
    public void testIsAuthenticationCanBeDoneLater() throws Exception {

        assertTrue("Testing: sAuthenticationCanBeDoneLater",
                   mAuth.isAuthenticationCanBeDoneLater());
    }

    /**
     * Tests the {@link MockAuthentication#getAuthenticationActivityIntent(Context)} method
     */
    public void testGetAuthenticationActivityIntent() throws Exception {

        assertTrue("getAuthenticationActivityIntent() should return an Intent",
                   mAuth.getAuthenticationActivityIntent(mContext) != null);
    }

    /**
     * Tests the authentication String getter
     */
    public void testGetAuthString() throws Exception {

        assertEquals("Checking getAuthString()", mAuth.getAuthString(), "");
    }

    /**
     * Tests the authentication String setter
     */
    public void testSetAuthString() throws Exception {

        mAuth.setAuthString(TEST_STRING);
        assertEquals("Checking that setAuthString() works", mAuth.getAuthString(), TEST_STRING);
    }

    /**
     * Tests mock authentication
     */
    public void testGetResponse() throws Exception {

        assertEquals("Testing authentication response: OK",
                     mAuth.getResponse(AUTH_OKAY), STATUS_AUTHENTICATED);
        assertEquals("Testing authentication response: Not found",
                     mAuth.getResponse(AUTH_NOT_FOUND), STATUS_NOT_FOUND);
        assertEquals("Testing authentication response: Unauthorized",
                     mAuth.getResponse(AUTH_UNAUTHORIZED), STATUS_UNAUTHORIZED);
        assertEquals("Testing authentication response: Unknown",
                     mAuth.getResponse("random string"), STATUS_UNKNOWN);

    }
}