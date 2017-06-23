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

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.util.Log;

import com.amazon.adobepass.auth.utils.NetworkUtils;
import com.amazon.auth.AuthenticationConstants;
import com.amazon.auth.IAuthentication;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.json.JSONObject;

import cz.msebera.android.httpclient.Header;

/**
 * This class implements the {@link IAuthentication} interface for Adobe Pass authentication.
 */
public class AdobepassAuthentication implements IAuthentication {

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    final static String IMPL_CREATOR_NAME = AdobepassAuthentication.class.getSimpleName();
    private static final String TAG = AdobepassAuthentication.class.getName();


    /**
     * {@inheritDoc}
     *
     * @param context Context The application context.
     */
    @Override
    public void init(Context context) {
        // No configuration required.
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthenticationCanBeDoneLater() {

        return false;
    }

    /**
     * Returns the intent for Adobepass AuthenticationActivity. This activity has the
     * instructions for Adobepass second screen login instructions and verifying user login.
     *
     * @param context The context required to create the intent.
     * @return Intent to start the authentication activity.
     */
    @Override
    public Intent getAuthenticationActivityIntent(Context context) {

        return new Intent(context, AdobeAuthenticationActivity.class);
    }

    /**
     * Verifies if the user is logged in with Adobepass.
     * The API internally takes care of retrying failed calls
     *
     * @param context         The context to check if user is logged in.
     * @param responseHandler The callback listener for output of this web service request.
     */
    @Override
    public void isUserLoggedIn(Context context, final ResponseHandler responseHandler) {

        final Bundle bundle = new Bundle();
        try {
            if (NetworkUtils.isConnectedToNetwork(context)) {
                AdobepassRestClient.getAuthenticationTokenRequest(
                        context,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                super.onSuccess(statusCode, headers, response);
                                Log.i(TAG, "getAuthenticationTokenRequest succeeded");
                                responseHandler.onSuccess(bundle);
                            }

                            @Override
                            public void onFailure(int statusCode,
                                                  Header[] headers,
                                                  Throwable throwable,
                                                  JSONObject errorResponse) {

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                                Log.e(TAG, "User is not logged in. Status code: " + statusCode +
                                        " ErrorResponse" + errorResponse, throwable);
                                populateAuthenticationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String
                                    responseString, Throwable throwable) {

                                super.onFailure(statusCode, headers, responseString, throwable);
                                Log.e(TAG, "User is not logged in. Status code: " + statusCode +
                                        " ErrorResponse" + responseString, throwable);
                                populateAuthenticationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);
                            }

                        });
            }
            else {
                populateErrorBundle(bundle, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
                responseHandler.onFailure(bundle);
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "There was an exception when generating the authorization request. ", e);
            responseHandler.onFailure(bundle);
        }
        catch (Exception e) {
            Log.e(TAG,
                  "There was an exception when generating the authentication token request.", e);
            responseHandler.onFailure(bundle);
        }
    }

    /**
     * Verifies if the requested resource is allowed for playback for the current logged in user.
     * The API internally takes care of retrying failed calls
     *
     * @param context         The context to check for authorization.
     * @param resourceId      The id of the resource to verify authorization.
     * @param responseHandler The callback interface
     */
    @Override
    public void isResourceAuthorized(final Context context,
                                     String resourceId,
                                     final ResponseHandler responseHandler) {

        final Bundle bundle = new Bundle();
        try {
            if (NetworkUtils.isConnectedToNetwork(context)) {
                AdobepassRestClient.generateAuthorizationRequest(
                        context,
                        context.getString(R.string.adobe_pass_requestor_id),
                        new JsonHttpResponseHandler() {

                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                super.onSuccess(statusCode, headers, response);
                                Log.i(TAG, "generateAuthorizationRequest succeeded");
                                responseHandler.onSuccess(bundle);
                            }

                            @Override
                            public void onFailure(int statusCode,
                                                  Header[] headers,
                                                  Throwable throwable,
                                                  JSONObject errorResponse) {

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                                Log.e(TAG, "There was an error authorizing the user on second" +
                                        " screen. Status code: " + statusCode + " ErrorResponse " +
                                        errorResponse + " Error: " + throwable);
                                populateAuthorizationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);
                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String
                                    responseString, Throwable throwable) {

                                super.onFailure(statusCode, headers, responseString, throwable);
                                Log.e(TAG, "There was an error authorizing the user on second " +
                                        "screen. Status code: " + statusCode + " ErrorResponse " +
                                        responseString + " Error: " + throwable);
                                populateAuthorizationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);
                            }
                        });
            }
            else {
                populateErrorBundle(bundle, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
                responseHandler.onFailure(bundle);
            }

        }
        catch (Exception e) {
            Log.e(TAG, "There was an exception when generating the authorization request. ", e);
            responseHandler.onFailure(bundle);
        }
    }

    /**
     * Logs out the user by clearing the tokens for authentication/authorization.
     *
     * @param context         The context to logout the user.
     * @param responseHandler The callback interface
     */
    @Override
    public void logout(final Context context, final ResponseHandler responseHandler) {

        final Bundle bundle = new Bundle();
        try {
            if (NetworkUtils.isConnectedToNetwork(context)) {
                AdobepassRestClient.logoutRequest(
                        context,
                        new JsonHttpResponseHandler() {
                            @Override
                            public void onSuccess(int statusCode,
                                                  Header[] headers,
                                                  JSONObject response) {

                                super.onSuccess(statusCode, headers, response);
                                Log.i(TAG, "logoutRequest succeeded");
                                responseHandler.onSuccess(bundle);
                            }

                            @Override
                            public void onFailure(int statusCode,
                                                  Header[] headers,
                                                  Throwable throwable,
                                                  JSONObject errorResponse) {

                                super.onFailure(statusCode, headers, throwable, errorResponse);
                                Log.e(TAG, "There was an error logging out the user " +
                                        "Status code: " + statusCode + " ErrorResponse " +
                                        errorResponse + " Error: " + throwable);
                                populateAuthorizationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);

                            }

                            @Override
                            public void onFailure(int statusCode, Header[] headers, String
                                    responseString, Throwable throwable) {

                                super.onFailure(statusCode, headers, responseString, throwable);
                                Log.e(TAG, "There was an error logging out the user " +
                                        "Status code: " + statusCode + " ErrorResponse " +
                                        responseString + " Error: " + throwable);
                                populateAuthorizationFailureBundle(statusCode, bundle, throwable);
                                responseHandler.onFailure(bundle);
                            }
                        });
            }
            else {
                populateErrorBundle(bundle, AuthenticationConstants.NETWORK_ERROR_CATEGORY);
                responseHandler.onFailure(bundle);
            }
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "There was an exception when generating the authorization request. ", e);
            responseHandler.onFailure(bundle);
        }
        catch (Exception e) {
            Log.e(TAG, "There was an exception when requesting for logout. ", e);
            responseHandler.onFailure(bundle);
        }
    }

    /**
     * Bundle to be sent on authorization failure
     *
     * @param statusCode status code of failure
     * @param bundle     Bundle to populate
     * @param throwable  throwable thrown
     */
    private void populateAuthorizationFailureBundle(int statusCode, Bundle bundle, Throwable
            throwable) {

        Bundle errorBundle = new Bundle();
        errorBundle.putInt(ResponseHandler.STATUS_CODE, statusCode);
        errorBundle.putString(
                AuthenticationConstants.ERROR_CATEGORY,
                AuthenticationConstants.AUTHORIZATION_ERROR_CATEGORY);
        errorBundle.putSerializable(
                AuthenticationConstants.ERROR_CAUSE, throwable);
        bundle.putBundle(
                AuthenticationConstants.ERROR_BUNDLE, errorBundle);
    }

    /**
     * This method cancels all requests associated with the any context.
     */
    public void cancelAllRequests() {

        AdobepassRestClient.cancelAllRequests();
    }

    /**
     * Bundle to be sent on authentication failure
     *
     * @param statusCode status code of failure
     * @param bundle     Bundle to populate
     * @param throwable  throwable thrown
     */
    private void populateAuthenticationFailureBundle(int statusCode, Bundle bundle, Throwable
            throwable) {

        Bundle errorBundle = new Bundle();
        errorBundle.putInt(ResponseHandler.STATUS_CODE, statusCode);
        errorBundle.putString(
                AuthenticationConstants.ERROR_CATEGORY,
                AuthenticationConstants.AUTHENTICATION_ERROR_CATEGORY);
        errorBundle.putSerializable(
                AuthenticationConstants.ERROR_CAUSE, throwable);
        bundle.putBundle(
                AuthenticationConstants.ERROR_BUNDLE, errorBundle);
    }

    /**
     * Bundle to be sent on failures other than Authentication and Authorization
     *
     * @param bundle        Bundle to populate
     * @param errorCategory Error Category
     */
    private void populateErrorBundle(Bundle bundle, String errorCategory) {

        Bundle errorBundle = new Bundle();
        errorBundle.putString(
                AuthenticationConstants.ERROR_CATEGORY,
                errorCategory);
        bundle.putBundle(
                AuthenticationConstants.ERROR_BUNDLE, errorBundle);
    }
}
