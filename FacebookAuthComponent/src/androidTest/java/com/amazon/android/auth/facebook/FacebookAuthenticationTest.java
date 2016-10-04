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

package com.amazon.android.auth.facebook;

import com.amazon.android.utils.Preferences;
import com.amazon.auth.IAuthentication;
import com.facebook.FacebookSdk;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.net.HttpURLConnection;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the {@link FacebookAuthentication} class.
 */
@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
public class FacebookAuthenticationTest {

    FacebookAuthentication mFBAuth;
    Context mContext;

    @Before
    public void setUp() {


        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();

        Preferences.setContext(mContext);

        mFBAuth = new FacebookAuthentication();
    }

    /**
     * Tests the {@link FacebookAuthentication#init(Context)} method.
     */
    @Test
    public void testConfigure() throws Exception {

        mFBAuth.init(mContext);

        assertTrue("SDK should be initialized.", FacebookSdk.isInitialized());
    }

    /**
     * Tests the {@link FacebookAuthentication#getAuthenticationActivityIntent(Context)} method.
     */
    @Test
    public void testGetAuthenticationActivityIntent() {

        Intent intent = mFBAuth.getAuthenticationActivityIntent(mContext);
        assertNotNull("Activity intent should not be null", intent);
    }

    /**
     * Tests the {@link FacebookAuthentication#logout(Context, IAuthentication.ResponseHandler)}
     * method.
     */
    @Test
    public void testLogout() {

        mFBAuth.logout(mContext, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                assertTrue("Logout should have been successful.", true);

                assertEquals("Token should be empty string after logout.",
                             "",
                             mFBAuth.getAccessToken());
            }

            @Override
            public void onFailure(Bundle extras) {

                assertTrue("Logout should not have failed.", false);
            }
        });
    }

    /**
     * Tests the {@link FacebookAuthentication#isUserLoggedIn(Context,
     * IAuthentication.ResponseHandler)} method. Can only really test the negative cases for the
     * method, because we'd need a permanently valid access token to know that the user is logged
     * in.
     */
    @Test
    public void testIsUserLoggedIn() {

        // Test if the user is logged in when the access token isn't valid.
        mFBAuth.setAccessToken("aFakeToken");

        // The user is expected to not be logged in, therefore an onFailure should be called.
        mFBAuth.isUserLoggedIn(mContext, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                assertTrue("User should not be logged in with the fake token", false);
            }

            @Override
            public void onFailure(Bundle extras) {
                // Expected login to fail because token is fake.
                int statusCode = extras.getInt(IAuthentication.ResponseHandler.STATUS_CODE);

                assertNotEquals("Status code should not be 200",
                                HttpURLConnection.HTTP_OK,
                                statusCode);
            }
        });

        // Test if the user is logged in when the access token is null.
        mFBAuth.setAccessToken(null);

        // The user is expected to not be logged in, therefore an onFailure should be called.
        mFBAuth.isUserLoggedIn(mContext, new IAuthentication.ResponseHandler() {
            @Override
            public void onSuccess(Bundle extras) {

                assertTrue("User should not be logged in with a null token", false);
            }

            @Override
            public void onFailure(Bundle extras) {
                // Nothing special expected to be sent on failure if the token is null when checked.
            }
        });


    }
}

