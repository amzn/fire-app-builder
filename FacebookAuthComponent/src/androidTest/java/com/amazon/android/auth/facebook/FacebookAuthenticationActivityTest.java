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

import org.junit.Before;
import org.junit.Test;

import android.content.Context;
import android.test.ActivityInstrumentationTestCase2;

import static com.amazon.android.utils.Helpers.sleep;

/**
 * This test class exercises the {@link FacebookAuthenticationActivity} class. Some these tests
 * will only properly pass if custom.xml in this module has a real client key and app id set. The
 * tests that need real keys will test if the keys match the default value and simply pass if they
 * do.
 */
public class FacebookAuthenticationActivityTest extends
        ActivityInstrumentationTestCase2<FacebookAuthenticationActivity> {

    private static final String TAG = FacebookAuthenticationActivityTest.class.getSimpleName();
    private FacebookAuthenticationActivity mAuthActivity;

    /**
     * Constructor used to setup the activity for testing.
     */
    public FacebookAuthenticationActivityTest() {

        super(FacebookAuthenticationActivity.class);
    }

    @Before
    public void setUp() throws Exception {

        mAuthActivity = getActivity();
    }

    /**
     * Tests the {@link FacebookAuthenticationActivity#getRegistrationCode()} method. Ensures that
     * a new access code and a new user code were retrieved from the call.
     */
    @Test
    public void testGetRegistrationCode() throws Throwable {

        if (!usingDefaultKeys()) {

            // Get the old access code and user code.
            String oldAccessCode = mAuthActivity.getAccessCode();
            String oldUserCode = mAuthActivity.getUserRegistrationCode();

            runTestOnUiThread(new Runnable() {
                @Override
                public void run() {
                    // Request a new user registration code. This also updates the access code.
                    mAuthActivity.getRegistrationCode();
                }
            });

            // Wait for call and UI to update.
            sleep(5000);

            // Get the new access code and user code.
            String newAccessCode = mAuthActivity.getAccessCode();
            String newUserCode = mAuthActivity.getUserRegistrationCode();

            // Make sure the old codes and the new codes are not the same.
            assertNotSame(oldAccessCode, newAccessCode);
            assertNotSame(oldUserCode, newUserCode);
        }
    }

    /**
     * Tests if the module is using default values for the app id and client token.
     *
     * @return True if using default values, false otherwise.
     */
    private boolean usingDefaultKeys() {

        Context context = getInstrumentation().getTargetContext();

        return context.getString(R.string.encrypted_authentication_app_id)
                      .equals(context.getString(R.string.encrypted_authentication_app_id)) ||
                context.getString(R.string.encrypted_authentication_client_token)
                       .equals(context.getString(R.string.encrypted_authentication_client_token));
    }
}
