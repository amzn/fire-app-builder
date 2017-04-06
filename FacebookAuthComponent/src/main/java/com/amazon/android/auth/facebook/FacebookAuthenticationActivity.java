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

import com.amazon.android.ui.activities.SecondScreenAuthenticationActivity;
import com.amazon.android.utils.NetworkUtils;
import com.amazon.android.utils.Preferences;
import com.amazon.auth.AuthenticationConstants;
import com.amazon.auth.IAuthentication;

import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.Map;

/**
 * An android activity that displays the 2nd screen instructions for Facebook authentication.
 */
public class FacebookAuthenticationActivity extends SecondScreenAuthenticationActivity {

    /**
     * Debug tag.
     */
    private static final String TAG = FacebookAuthentication.class.getName();

    /**
     * A code received by getting the registration code. Its used to request the authentication
     * access token.
     */
    private String mAccessCode;

    /**
     * {@inheritDoc}
     */
    @Override
    public void getRegistrationCode() {

        getGetCodeButton().setEnabled(false);
        if (NetworkUtils.isConnectedToNetwork(this)) {
            (new AsyncTask<Void, Void, Map>() {

                @Override
                protected void onPostExecute(Map map) {

                    super.onPostExecute(map);

                    // Registration code retrieval was successful.
                    if (map != null && map.containsKey(FacebookApi.USER_CODE)
                            && map.containsKey(IAuthentication.CODE)) {

                        // Save the access code to use in later HTTP request.
                        mAccessCode = map.get(IAuthentication.CODE).toString();

                        // Display the user code in the UI.
                        String userCode = map.get(FacebookApi.USER_CODE).toString();
                        setUserRegistrationCode(userCode);
                        Log.d(TAG, "Getting the registration code succeeded.");
                    }
                    // Registration code retrieval failed. Errors previously logged.
                    else {
                        setResultAndReturn(null,
                                           AuthenticationConstants.REGISTRATION_ERROR_CATEGORY);
                    }
                    getGetCodeButton().setEnabled(true);
                }

                @Override
                protected Map doInBackground(Void... params) {

                    return FacebookApi.makeHttpCallForRegistration(getApplicationContext());
                }

            }).execute();
        }
        else {
            setResultAndReturn(null, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void getAuthenticationToken() {

        getSubmitButton().setEnabled(false);
        if (NetworkUtils.isConnectedToNetwork(this)) {
            (new AsyncTask<Void, Void, Map>() {

                @Override
                protected void onPostExecute(Map map) {

                    super.onPostExecute(map);

                    if (map != null && map.containsKey(IAuthentication.ACCESS_TOKEN)) {
                        // Successful login.
                        if (FacebookApi.DEBUG) {
                            Log.d(TAG, "Storing access token: " +
                                    map.get(IAuthentication.ACCESS_TOKEN).toString());
                        }

                        Preferences.setString(IAuthentication.ACCESS_TOKEN,
                                              map.get(IAuthentication.ACCESS_TOKEN).toString());

                        setResult(RESULT_OK);
                        getSubmitButton().setEnabled(true);
                        finish();
                    }

                    else {
                        getSubmitButton().setEnabled(true);
                        // There was an error authenticating the user entered token.
                        setResultAndReturn(null,
                                           AuthenticationConstants.AUTHENTICATION_ERROR_CATEGORY);
                    }
                }

                @Override
                protected Map doInBackground(Void... params) {

                    return FacebookApi.makeHttpCallForAuthToken(getApplicationContext(),
                                                                mAccessCode);
                }

            }).execute();
        }
        else {
            setResultAndReturn(null, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
        }
    }

    /**
     * Set the corresponding extras & finish this activity
     *
     * @param throwable contains detailed info about the cause of error
     * @param category  error cause
     */
    private void setResultAndReturn(Throwable throwable, String category) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(AuthenticationConstants.ERROR_CATEGORY, category);
        bundle.putSerializable(AuthenticationConstants.ERROR_CAUSE, throwable);
        setResult(RESULT_CANCELED, intent.putExtra(AuthenticationConstants.ERROR_BUNDLE, bundle));
        finish();
    }

    /**
     * Return the current access code.
     *
     * @return The access code string.
     */
    @VisibleForTesting
    String getAccessCode() {

        return mAccessCode;
    }

}