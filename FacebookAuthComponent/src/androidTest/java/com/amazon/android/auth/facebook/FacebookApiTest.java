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

import com.amazon.auth.IAuthentication;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.util.Map;

import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertNotNull;
import static junit.framework.TestCase.assertTrue;

/**
 * This class tests the {@link FacebookApi} class. We can only test these methods if we are using a
 * real app id and client token in the custom.xml file. The tests will pass if the default values
 * are being used.
 */
@RunWith(AndroidJUnit4.class)
public class FacebookApiTest {

    Context mContext;

    @Before
    public void setUp() {

        mContext = InstrumentationRegistry.getInstrumentation().getTargetContext();
    }

    /**
     * Test the {@link FacebookApi#makeHttpCallForRegistration(Context)} method.
     */
    @Test
    public void makeHttpCallForRegistrationTest() throws Exception {

        Map result = FacebookApi.makeHttpCallForRegistration(mContext);

        // If we have real key values then we can test the positive case.
        if (!usingDefaultKeys()) {

            // The result should not be null and the result should contain the access token.
            assertNotNull(result);
            assertTrue(result.containsKey(IAuthentication.CODE));
            assertTrue(result.containsKey(FacebookApi.USER_CODE));
        }
        // Otherwise we can test the negative case where the app id and client token are bad so
        // we expect to fail.
        else {
            // The result should contain a status code and message for the error.
            assertTrue(result.containsKey(IAuthentication.ResponseHandler.STATUS_CODE));
            assertTrue(result.containsKey(IAuthentication.ResponseHandler.MESSAGE));
        }

    }

    /**
     * Tests the {@link FacebookApi#makeHttpCallForAuthToken(Context, String)} method. We can only
     * test the negative case because we cannot emulate a user entering the user code to validate
     * the access code, which would result in an access token.
     */
    @Test
    public void makeHttpCallForAuthTokenTest() throws Exception {

        // Request the auth token using a fake access code.
        Map result = FacebookApi.makeHttpCallForAuthToken(mContext, "fakeCode");

        // Expecting the result map to have an error status code and error message.
        assertNotNull(result);
        assertTrue(result.containsKey(IAuthentication.ResponseHandler.STATUS_CODE));
        assertTrue(result.containsKey(IAuthentication.ResponseHandler.MESSAGE));
        // Not expecting an http status code because the test will not reach the http call
        // because decrypting the fake key fails first.
        assertEquals(-1, result.get(IAuthentication.ResponseHandler.STATUS_CODE));
    }

    /**
     * Tests the {@link FacebookApi#checkAccessToken(Context, String)} method. We can only test the
     * negative case because we cannot be sure the stored access token is valid and we cannot
     * request a valid access token at this point.
     */
    @Test
    public void checkAccessTokenTest() throws Exception {

        // Check a fake access token.
        Map result = FacebookApi.checkAccessToken(mContext, "fakeAccessToken");

        // Expecting the result map to have an error status code and error message.
        assertNotNull(result);
        assertTrue(result.containsKey(IAuthentication.ResponseHandler.STATUS_CODE));
        assertTrue(result.containsKey(IAuthentication.ResponseHandler.MESSAGE));
        assertEquals(400, result.get(IAuthentication.ResponseHandler.STATUS_CODE));
    }

    /**
     * Tests if the module is using default values for the app id and client token.
     *
     * @return True if using default values, false otherwise.
     */
    private boolean usingDefaultKeys() {

        return mContext.getString(R.string.encrypted_authentication_app_id)
                       .equals(mContext.getString(R.string.encrypted_authentication_app_id)) ||
                mContext.getString(R.string.encrypted_authentication_client_token)
                        .equals(mContext.getString(R.string.encrypted_authentication_client_token));
    }
}
