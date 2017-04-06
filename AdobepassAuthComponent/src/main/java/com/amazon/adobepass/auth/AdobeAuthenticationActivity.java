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
package com.amazon.adobepass.auth;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import com.amazon.adobepass.auth.utils.NetworkUtils;
import com.amazon.android.ui.activities.SecondScreenAuthenticationActivity;
import com.amazon.auth.AuthenticationConstants;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONException;
import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * An android activity that displays the 2nd screen instructions for Adobe Pass authentication.
 */
public class AdobeAuthenticationActivity extends SecondScreenAuthenticationActivity {

    private static final String TAG = AdobeAuthenticationActivity.class.getName();
    private static final String REGISTRATION_CODE = "code";

    /**
     * {@inheritDoc}
     * Method to get authentication token for the device.
     * The method retries on its own to take care of failed calls, caller does not need to retry.
     */
    @Override
    public void getAuthenticationToken() {

        try {
            getSubmitButton().setEnabled(false);
            if (NetworkUtils.isConnectedToNetwork(this)) {

                AdobepassRestClient.getAuthenticationTokenRequest(this, new
                        JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode, Header[] headers, JSONObject
                                    response) {

                                super.onSuccess(statusCode, headers, response);
                                Log.i(TAG, "getAuthenticationTokenRequest succeeded");
                                getSubmitButton().setEnabled(true);
                                try {
                                    String mvpd = response.getString(AuthenticationConstants.MVPD);
                                    Log.d(TAG, "Logged in with the following provider: " + mvpd);
                                    Intent intent = new Intent();
                                    Bundle bundle = new Bundle();
                                    bundle.putString(AuthenticationConstants.MVPD, mvpd);
                                    setResult(RESULT_OK,
                                              intent.putExtra(AuthenticationConstants.MVPD_BUNDLE,
                                                              bundle));
                                }
                                catch (Exception e) {
                                    Log.e(TAG, "There was an exception when getting mvpd name.", e);
                                    setResult(RESULT_OK);
                                }

                                finish();
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, Throwable
                                    throwable,
                                                  JSONObject errorResponse) {

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                                Log.e(TAG, "There was an error authenticating the user on second " +
                                              "screen. Status code: " + statusCode + " Error: " +
                                              errorResponse,
                                      throwable);
                                getSubmitButton().setEnabled(true);
                                setResultAndReturn(throwable, AuthenticationConstants
                                        .AUTHENTICATION_ERROR_CATEGORY);
                            }
                        });
            }
            else {
                getSubmitButton().setEnabled(true);
                setResultAndReturn(null, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "There was an exception when requesting the authentication token. ", e);
            getSubmitButton().setEnabled(true);
        }

    }


    /**
     * {@inheritDoc}
     * Method to get the registration code and display to user on the screen.
     * The method retries on its own to take care of failed calls, caller does not need to retry.
     */
    @Override
    public void getRegistrationCode() {

        try {
            getGetCodeButton().setEnabled(false);
            if (NetworkUtils.isConnectedToNetwork(this)) {
                AdobepassRestClient.getRegistrationCode(this, new JsonHttpResponseHandler() {
                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject response) {

                        super.onSuccess(statusCode, headers, response);
                        try {
                            Log.i(TAG, "getRegistrationCode succeeded");
                            setUserRegistrationCode(response.getString(REGISTRATION_CODE));
                        }
                        catch (JSONException e) {
                            Log.e(TAG, "Failed to get the registration code from JSON response ",
                                  e);
                        }
                        getGetCodeButton().setEnabled(true);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject errorResponse) {

                        super.onFailure(statusCode, headers, throwable, errorResponse);
                        onFailureAction(statusCode, errorResponse.toString(), throwable);
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, String
                            responseString, Throwable throwable) {

                        super.onFailure(statusCode, headers, responseString, throwable);
                        onFailureAction(statusCode, responseString, throwable);
                    }

                    /**
                     * Actions to be performed on failure
                     *
                     * @param statusCode status code of failure
                     * @param errorResponseMsg error message
                     * @param throwable throwable thrown
                     */
                    private void onFailureAction(int statusCode, String errorResponseMsg,
                                                 Throwable throwable) {

                        Log.e(TAG, "There was an error getting the registration code " +
                                      "Status code: " + statusCode + " Error: " + errorResponseMsg,
                              throwable);
                        getGetCodeButton().setEnabled(true);
                        setResultAndReturn(throwable,
                                           AuthenticationConstants.REGISTRATION_ERROR_CATEGORY);
                    }
                });
            }
            else {
                getGetCodeButton().setEnabled(true);
                setResultAndReturn(null, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
            }
        }
        catch (Exception e) {
            Log.e(TAG, "There was an exception when requesting the registration code. ", e);
            getGetCodeButton().setEnabled(true);
        }
    }

    /**
     * Set the corresponding extras & finish this activity
     *
     * @param throwable Contains detailed info about the cause of error.
     * @param category  Error cause category.
     */
    private void setResultAndReturn(Throwable throwable, String category) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(AuthenticationConstants.ERROR_CATEGORY, category);
        bundle.putSerializable(AuthenticationConstants.ERROR_CAUSE, throwable);
        setResult(RESULT_CANCELED, intent.putExtra(AuthenticationConstants.ERROR_BUNDLE, bundle));
        finish();
    }

    @Override
    public void onPause() {

        super.onPause();
        Log.d(TAG, "OnPause called, cancelling all authentication requests");
        AdobepassRestClient.cancelAllRequests();
    }

}