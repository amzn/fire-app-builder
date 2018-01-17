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

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.amazon.auth.AuthenticationConstants;
import com.amazon.identity.auth.device.AuthError;
import com.amazon.identity.auth.device.authorization.api.AmazonAuthorizationManager;
import com.amazon.identity.auth.device.authorization.api.AuthorizationListener;
import com.amazon.identity.auth.device.authorization.api.AuthzConstants;
import com.amazon.identity.auth.device.shared.APIListener;

import com.amazon.android.utils.Preferences;

/**
 * This activity allows users to login with amazon.
 */
public class LoginWithAmazonActivity extends Activity {

    private static final String TAG = LoginWithAmazonActivity.class.getName();

    private String[] APP_SCOPES;
    private static final String IS_LOGGED_IN = "isLoggedIn";

    private TextView mProfileText;
    private ImageButton mLoginButton;
    private Button mReturnToAppButton;
    private AmazonAuthorizationManager mAuthManager;
    private ProgressBar mLogInProgress;

    /* (non-Javadoc)
     * @see android.app.Activity#onCreate(android.os.Bundle)
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Confirm that we have the correct API Key.
        try {
            mAuthManager = new AmazonAuthorizationManager(this, Bundle.EMPTY);
        }
        catch (IllegalArgumentException e) {
            showAuthToast(getString(R.string.incorrect_api_key));
            Log.e(TAG, getString(R.string.incorrect_api_key), e);
        }
        setContentView(R.layout.activity_main);

        APP_SCOPES = new String[]{getString(R.string.profile_Login)};

        initializeUI();
    }

    /**
     * Initializes all of the UI elements in the activity.
     */
    private void initializeUI() {

        mProfileText = (TextView) findViewById(R.id.profile_info);

        // Setup the listener on the login button.
        mLoginButton = (ImageButton) findViewById(R.id.login_with_amazon);
        mLoginButton.setOnClickListener(new View.OnClickListener() {

            @Override
            public void onClick(View v) {

                setLoggingInState(true);
                mAuthManager.authorize(APP_SCOPES, Bundle.EMPTY, new AuthListener());
            }
        });

        mLogInProgress = (ProgressBar) findViewById(R.id.log_in_progress);

        // Set the listener on the return button.
        mReturnToAppButton = (Button) findViewById(R.id.return_to_Mask);
        mReturnToAppButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                setResult(RESULT_OK);
                finish();
            }
        });
    }

    /**
     * {@link AuthorizationListener} which is passed in to authorize calls made on the {@link
     * AmazonAuthorizationManager} member.
     * Starts getToken workflow if the authorization was successful, or displays a toast if the
     * user cancels authorization.
     */
    private class AuthListener implements AuthorizationListener {

        /**
         * Authorization was completed successfully.
         * Display the profile of the user who just completed authorization.
         *
         * @param response The bundle containing authorization response. Not used.
         */
        @Override
        public void onSuccess(Bundle response) {

            mAuthManager.getProfile(new ProfileListener());
        }


        /**
         * There was an error during the attempt to authorize the application.
         * Log the error, and reset the profile text view.
         *
         * @param ae The error that occurred during authorization.
         */
        @Override
        public void onError(final AuthError ae) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showAuthToast(getString(R.string.error_during_auth));
                    resetProfileView();
                    setLoggingInState(false);
                    LoginWithAmazonActivity.this
                            .setResultAndReturn(
                                    ae.getCause(),
                                    AuthenticationConstants.AUTHENTICATION_ERROR_CATEGORY);
                }
            });
        }

        /**
         * Authorization was cancelled before it could be completed.
         * A toast is shown to the user, to confirm that the operation was cancelled, and the
         * profile text view is reset.
         *
         * @param cause The bundle containing the cause of the cancellation. Not used.
         */
        @Override
        public void onCancel(Bundle cause) {

            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    showAuthToast(getString(R.string.auth_cancelled));
                    resetProfileView();
                }
            });
        }

    }

    /**
     * Sets the text in the mProfileText {@link TextView} to the value of the provided String.
     *
     * @param profileInfo the String with which to update the {@link TextView}.
     */
    private void updateProfileView(String profileInfo) {

        mProfileText.setText(profileInfo);
    }

    /**
     * Sets the text in the mProfileText {@link TextView} to the prompt it originally displayed.
     */
    private void resetProfileView() {

        setLoggingInState(false);
        mProfileText.setText(getString(R.string.default_message));
    }

    /**
     * Sets the state of the application to reflect that the user is not currently authorized.
     */
    private void setLoggedOutState() {

        mLoginButton.setVisibility(Button.VISIBLE);
        Preferences.setBoolean(IS_LOGGED_IN, false);
        resetProfileView();
    }

    /**
     * Sets the state of the application to reflect that the user is currently authorized.
     */
    private void setLoggedInState() {

        mLoginButton.setVisibility(Button.GONE);
        mReturnToAppButton.setVisibility(Button.VISIBLE);
        Preferences.setBoolean(IS_LOGGED_IN, true);
        setLoggingInState(false);
    }

    /**
     * Turns on/off display elements which indicate that the user is currently in the process of
     * logging in.
     *
     * @param loggingIn Whether or not the user is currently in the process of logging in.
     */
    private void setLoggingInState(final boolean loggingIn) {

        if (loggingIn) {
            mLoginButton.setVisibility(Button.GONE);
            mLogInProgress.setVisibility(ProgressBar.VISIBLE);
            mProfileText.setVisibility(TextView.GONE);
        }
        else {
            if (!Preferences.getBoolean(IS_LOGGED_IN)) {
                mLoginButton.setVisibility(Button.VISIBLE);
            }
            mLogInProgress.setVisibility(ProgressBar.GONE);
            mProfileText.setVisibility(TextView.VISIBLE);
        }
    }

    /**
     * This method handles toasts messages.
     *
     * @param authToastMessage The message to be posted.
     */
    private void showAuthToast(String authToastMessage) {

        Toast authToast = Toast.makeText(getApplicationContext(), authToastMessage, Toast
                .LENGTH_LONG);
        authToast.setGravity(Gravity.CENTER, 0, 0);
        authToast.show();
    }

    /**
     * {@link AuthListener} which is passed in to the {@link AmazonAuthorizationManager}
     * getProfile api call.
     */
    private class ProfileListener implements APIListener {

        /**
         * Updates the profile view with data from the successful getProfile response.
         * Sets app state to logged in.
         */
        @Override
        public void onSuccess(Bundle response) {

            Bundle profileBundle = response.getBundle(AuthzConstants.BUNDLE_KEY.PROFILE.val);
            if (profileBundle == null) {
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        setLoggedOutState();
                        String errorMessage = getString(R.string.error_during_auth);
                        Toast errorToast = Toast.makeText(getApplicationContext(), errorMessage,
                                                          Toast.LENGTH_LONG);
                        errorToast.setGravity(Gravity.CENTER, 0, 0);
                        errorToast.show();
                    }
                });
            }
            else {

                StringBuilder profileBuilder = new StringBuilder();

                profileBuilder.append(String.format(getString(R.string.welcome), profileBundle
                        .getString(AuthzConstants.PROFILE_KEY.NAME.val)));
                profileBuilder.append(" ");
                profileBuilder.append(String.format(getString(R.string.your_email_is),
                                                    profileBundle.getString(
                                                            AuthzConstants.PROFILE_KEY.EMAIL.val)));

                final String profile = profileBuilder.toString();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {

                        updateProfileView(profile);
                        setLoggedInState();
                    }
                });
            }
        }

        /**
         * Updates profile view to reflect that there was an error while retrieving profile
         * information.
         */
        @Override
        public void onError(AuthError ae) {

            Log.e(TAG, ae.getMessage(), ae);
            runOnUiThread(new Runnable() {
                @Override
                public void run() {

                    updateProfileView(getString(R.string.error_getting_profile));
                    setLoggingInState(false);
                }
            });
        }
    }

    /**
     * Set the corresponding extras and finish this activity.
     *
     * @param throwable Contains detailed info about the cause of error.
     * @param category  The error cause.
     */
    private void setResultAndReturn(Throwable throwable, String category) {

        Intent intent = new Intent();
        Bundle bundle = new Bundle();
        bundle.putString(AuthenticationConstants.ERROR_CATEGORY, category);
        bundle.putSerializable(AuthenticationConstants.ERROR_CAUSE, throwable);
        setResult(RESULT_CANCELED, intent.putExtra(AuthenticationConstants.ERROR_BUNDLE, bundle));
        finish();
    }
}