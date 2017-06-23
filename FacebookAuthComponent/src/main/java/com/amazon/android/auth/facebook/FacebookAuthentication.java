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
import com.amazon.auth.AuthenticationConstants;
import com.facebook.FacebookSdk;

import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.VisibleForTesting;
import android.util.Log;

import java.util.Map;


/**
 * This class handles the authentication process for Facebook.
 */
public class FacebookAuthentication implements IAuthentication {

    /**
     * Name used for implementation creator registration to the Module Manager.
     */
    public static final String IMPL_CREATOR_NAME = FacebookAuthentication.class.getSimpleName();

    /**
     * Debug tag.
     */
    private static final String TAG = FacebookAuthentication.class.getSimpleName();

    /**
     * The access token is used to see if the user is authenticated or not.
     */
    private String mAccessToken;

    /**
     * Context reference.
     */
    private Context mContext;

    /**
     * {@inheritDoc}
     * Initializes the Facebook SDK and retrieves the stored access token.
     *
     * @param context The application context.
     */
    @Override
    public void init(Context context) {

        mContext = context;

        FacebookSdk.sdkInitialize(context);

        // Load access token from shared preferences.
        mAccessToken = Preferences.getString(IAuthentication.ACCESS_TOKEN);
        if (FacebookApi.DEBUG) {
            Log.d(TAG, "Facebook configured and previous access token is: " + mAccessToken);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean isAuthenticationCanBeDoneLater() {

        return mContext.getResources().getBoolean(R.bool.is_authentication_can_be_done_later);
    }

    /**
     * Returns the intent for Facebook authentication activity. This activity has the
     * instructions for Facebook second screen login instructions.
     *
     * @param context The context required to create the intent.
     * @return Intent to start the authentication activity.
     */
    @Override
    public Intent getAuthenticationActivityIntent(Context context) {

        return new Intent(context, FacebookAuthenticationActivity.class);
    }

    /**
     * {@inheritDoc}
     *
     * @param context         The context to check if user is logged in.
     * @param responseHandler The callback interface
     */
    @Override
    public void isUserLoggedIn(final Context context, final ResponseHandler responseHandler) {

        Log.d(TAG, "Checking if user is logged in");

        final Bundle bundle = new Bundle();

        mAccessToken = Preferences.getString(IAuthentication.ACCESS_TOKEN);

        if (mAccessToken != null && !mAccessToken.isEmpty()) {

            // Check that the access token is still valid.
            // Needs to be on a separate thread since it requires an HTTP call.
            (new AsyncTask<Void, Void, Map>() {

                @Override
                protected void onPostExecute(Map map) {

                    super.onPostExecute(map);

                    // Encountered an unknown error while checking access token.
                    if (map == null) {
                        populateErrorBundle(bundle, context.getString(R.string.error_checking_token));
                        responseHandler.onFailure(bundle);
                        return;
                    }

                    // User is already logged in and access token is still valid.
                    if (map.containsKey(FacebookApi.NAME)
                            && map.containsKey(FacebookApi.ID)) {
                        responseHandler.onSuccess(bundle);
                    }
                    // Access token is not valid so user is not logged in.
                    else {
                        if (map.containsKey(ResponseHandler.STATUS_CODE)
                                && map.containsKey(IAuthentication.ERROR)) {

                            // Add error message and status code.
                            populateAuthenticationFailureBundle(Integer.parseInt(map.get
                                    (ResponseHandler.STATUS_CODE).toString()), bundle, map.get
                                    (IAuthentication.ERROR).toString());
                        }
                        responseHandler.onFailure(bundle);
                    }
                }

                @Override
                protected Map doInBackground(Void... params) {

                    return FacebookApi.checkAccessToken(context, mAccessToken);
                }

            }).execute();
        }
        // Access token is null or empty so we know its not valid.
        else {
            Log.d(TAG, context.getString(R.string.access_token_null));
            populateErrorBundle(bundle, context.getString(R.string.access_token_null));
            responseHandler.onFailure(bundle);
        }
    }

    /**
     * Not applicable to Facebook authentication.
     * {@inheritDoc}
     *
     * @param context         The context to check for authorization.
     * @param resourceId      The id of the resource to verify authorization.
     * @param responseHandler The callback interface
     */
    @Override
    public void isResourceAuthorized(Context context, String resourceId, ResponseHandler
            responseHandler) {
        // Not applicable to Facebook authentication.
        responseHandler.onSuccess(new Bundle());
    }

    /**
     * {@inheritDoc}
     *
     * @param context         The context to logout the user.
     * @param responseHandler The callback interface
     */
    @Override
    public void logout(Context context, ResponseHandler responseHandler) {

        mAccessToken = "";
        Preferences.setString(IAuthentication.ACCESS_TOKEN, mAccessToken);
        responseHandler.onSuccess(new Bundle());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void cancelAllRequests() {

    }

    /**
     * Gets the access token.
     *
     * @return The access token.
     */
    @VisibleForTesting
    String getAccessToken() {

        return mAccessToken;
    }

    /**
     * Sets the access token.
     *
     * @param accessToken The access token.
     */
    @VisibleForTesting
    void setAccessToken(String accessToken) {

        mAccessToken = accessToken;
    }

    /**
     * Bundle to be sent on failures other than Authentication and Authorization
     *
     * @param bundle Bundle to populate
     * @param errorMessage error message received
     */
    private void populateErrorBundle(Bundle bundle, String errorMessage) {

        Bundle errorBundle = new Bundle();
        errorBundle.putString(ResponseHandler.MESSAGE,
                              errorMessage);
        errorBundle.putString(
                AuthenticationConstants.ERROR_CATEGORY,
                AuthenticationConstants.AUTHENTICATION_ERROR_CATEGORY);
        bundle.putBundle(
                AuthenticationConstants.ERROR_BUNDLE, errorBundle);
    }

    /**
     * Bundle to be sent on failures other than Authentication and Authorization
     *
     * @param statusCode status code received because of failure
     * @param bundle Bundle to populate
     * @param errorMessage error message received
     */
    private void populateAuthenticationFailureBundle(int statusCode, Bundle bundle, String
            errorMessage) {

        populateErrorBundle(bundle, errorMessage);
        bundle.getBundle(
                AuthenticationConstants.ERROR_BUNDLE).putInt(ResponseHandler.STATUS_CODE,
                                                             statusCode);
    }
}
