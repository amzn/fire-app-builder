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
package com.amazon.android.contentbrowser.helper;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.R;
import com.amazon.android.utils.Preferences;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.content.Context;
import android.content.Intent;
import android.util.Log;

/**
 * Manager class to manage launcher integration. It interacts with ContentBrowser and listens to
 * updates from AuthHelper to keep track of user authentication status. It
 * also sends the status updates to Launcher every time the status is modified.
 * NOTE: launcher integration requires the user authentication system initialized before this
 * manager is initialized. Otherwise it will incorrectly set that authentication is not required for
 * content playing.
 */
public class LauncherIntegrationManager {

    /**
     * Constants required by Launcher integration
     */
    // Replace COM_AMAZON_TV_LAUNCHER value with "com.amazon.tv.integrationtestonly" when testing
    // with integration test app.
    private static final String COM_AMAZON_TV_LAUNCHER = "com.amazon.tv.launcher";
    private static final String COM_AMAZON_DEVICE_CAPABILITIES = "com.amazon.device.CAPABILITIES";
    private static final String AMAZON_INTENT_EXTRA_PLAY_INTENT_ACTION = "amazon.intent.extra" +
            ".PLAY_INTENT_ACTION";
    private static final String AMAZON_INTENT_EXTRA_PLAY_INTENT_PACKAGE = "amazon.intent.extra" +
            ".PLAY_INTENT_PACKAGE";
    private static final String AMAZON_INTENT_EXTRA_PLAY_INTENT_CLASS = "amazon.intent.extra" +
            ".PLAY_INTENT_CLASS";
    private static final String AMAZON_INTENT_EXTRA_PLAY_INTENT_FLAGS = "amazon.intent.extra" +
            ".PLAY_INTENT_FLAGS";
    private static final String AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_ACTION = "amazon.intent.extra" +
            ".SIGNIN_INTENT_ACTION";
    private static final String AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_PACKAGE = "amazon.intent.extra" +
            ".SIGNIN_INTENT_PACKAGE";
    private static final String AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_CLASS = "amazon.intent.extra" +
            ".SIGNIN_INTENT_CLASS";
    private static final String AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_FLAGS = "amazon.intent.extra" +
            ".SIGNIN_INTENT_FLAGS";
    private static final String AMAZON_INTENT_EXTRA_PARTNER_ID = "amazon.intent.extra.PARTNER_ID";
    private static final String AMAZON_INTENT_EXTRA_DATA_EXTRA_NAME = "amazon.intent.extra" +
            ".DATA_EXTRA_NAME";
    private static final String AMAZON_INTENT_EXTRA_DISPLAY_NAME = "amazon.intent.extra" +
            ".DISPLAY_NAME";
    /**
     * Intent action to be sent to launcher to indicate that the user is authorized to play content
     */
    public static final String PLAY_CONTENT_FROM_LAUNCHER_ACTION =
            "PLAY_CONTENT_FROM_LAUNCHER_ACTION";
    /**
     * Intent action to be sent to launcher to indicate that the user is not authorized to play
     * content
     */
    private static final String SIGN_IN_FROM_LAUNCHER_ACTION = "SIGN_IN_FROM_LAUNCHER_ACTION";

    /**
     * Constant to store the user authentication status in preferences
     */
    public static final String PREFERENCE_KEY_USER_AUTHENTICATED = "li_user_authenticated";
    /**
     * Constant for content source flag in the intent
     */
    public static final String CONTENT_SOURCE = "CONTENT_SOURCE";
    /**
     * Constant for indicating recommended content as the source of content play request
     */
    public static final String RECOMMENDED_CONTENT = "RECOMMENDED_CONTENT";
    /**
     * Constant for indicating Catalog content as the source of content play request
     */
    public static final String CATALOG_CONTENT = "CATALOG_CONTENT";
    /**
     * Debug tag.
     */
    private static final String TAG = LauncherIntegrationManager.class.getName();

    /**
     * The application context.
     */
    protected final Context mContext;

    /**
     * Content browser instance
     */
    private final ContentBrowser mContentBrowser;

    /**
     * Constructor.
     * It registers the instance as a listener to event bus so that it can listen to updates from
     * {@link PurchaseHelper} and {@link AuthHelper}. It also sends the initial authorization
     * status to Launcher.
     *
     * @param context        The application context.
     * @param contentBrowser ContentBrowser instance
     */
    public LauncherIntegrationManager(Context context, ContentBrowser contentBrowser) {

        this.mContext = context;
        this.mContentBrowser = contentBrowser;
        EventBus.getDefault().register(this);
        calculateAndSendAuthenticationStatus(context);
    }

    /**
     * Checks with content browser whether user authentication is mandatory and
     * accordingly sets the preferences
     */
    private void calculateAndSendAuthenticationStatus(Context context) {

        if (mContentBrowser != null) {
            // Authentication is not mandatory, send user authenticated status as true
            if (!mContentBrowser.isUserAuthenticationMandatory()) {
                Preferences.setBoolean(LauncherIntegrationManager.PREFERENCE_KEY_USER_AUTHENTICATED,
                                       true);
                sendAppAuthenticationStatusBroadcast(context, true);
            }
            else {
                // Authentication is mandatory, check with AuthHelper for authenticated status
                AuthHelper authHelper = mContentBrowser.getAuthHelper();
                authHelper.isAuthenticated()
                          .subscribe(authenticatedResultBundle -> {
                              if (authenticatedResultBundle.getBoolean(AuthHelper.RESULT)) {
                                  Preferences.setBoolean(LauncherIntegrationManager
                                                                 .PREFERENCE_KEY_USER_AUTHENTICATED,
                                                         true);
                                  sendAppAuthenticationStatusBroadcast(context, true);
                              }
                              else {
                                  Preferences.setBoolean(LauncherIntegrationManager
                                                                 .PREFERENCE_KEY_USER_AUTHENTICATED,
                                                         false);
                                  sendAppAuthenticationStatusBroadcast(context, false);
                              }
                          });
            }
        }
    }

    /**
     * Event bus listener method to listen for authentication updates from AuthHelper and send the
     * authentication status to Launcher.
     *
     * @param authenticationStatusUpdateEvent Broadcast event for update in authentication status.
     */
    @Subscribe
    public void onAuthenticationStatusUpdateEvent(AuthHelper.AuthenticationStatusUpdateEvent
                                                          authenticationStatusUpdateEvent) {

        sendAppAuthenticationStatusBroadcast(mContext, authenticationStatusUpdateEvent
                .isUserAuthenticated());
    }

    /**
     * Method to send app authentication status broadcast
     * @param context application context
     */
    public static void sendAppAuthenticationStatusBroadcast(Context context,
                                                            boolean isUserAuthenticated) {

        Intent intent = new Intent();
        intent.setPackage(COM_AMAZON_TV_LAUNCHER);
        intent.setAction(COM_AMAZON_DEVICE_CAPABILITIES);
        intent.putExtra(AMAZON_INTENT_EXTRA_PARTNER_ID,
                        context.getString(R.string.launcher_integration_partner_id));
        intent.putExtra(AMAZON_INTENT_EXTRA_DATA_EXTRA_NAME,
                        context.getString(R.string.launcher_integration_content_id_key));
        intent.putExtra(CONTENT_SOURCE, CATALOG_CONTENT);

        if (isUserAuthenticated) {
            intent.putExtra(AMAZON_INTENT_EXTRA_PLAY_INTENT_ACTION,
                            PLAY_CONTENT_FROM_LAUNCHER_ACTION);

            intent.putExtra(AMAZON_INTENT_EXTRA_PLAY_INTENT_PACKAGE,
                            context.getString(R.string.launcher_integration_intent_package_value));

            intent.putExtra(AMAZON_INTENT_EXTRA_PLAY_INTENT_CLASS,
                            context.getString(R.string.launcher_integration_intent_class_value));

            intent.putExtra(AMAZON_INTENT_EXTRA_PLAY_INTENT_FLAGS, Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra(AMAZON_INTENT_EXTRA_DISPLAY_NAME,
                            context.getString(R.string.launcher_integration_app_display_name));
        }
        else {
            intent.putExtra(AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_ACTION,
                            SIGN_IN_FROM_LAUNCHER_ACTION);

            intent.putExtra(AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_PACKAGE,
                            context.getString(R.string.launcher_integration_intent_package_value));

            intent.putExtra(AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_CLASS,
                            context.getString(R.string.launcher_integration_intent_class_value));

            intent.putExtra(AMAZON_INTENT_EXTRA_SIGN_IN_INTENT_FLAGS,
                            Intent.FLAG_ACTIVITY_NEW_TASK);

            intent.putExtra(AMAZON_INTENT_EXTRA_DISPLAY_NAME,
                            context.getString(R.string.launcher_integration_app_display_name));
        }
        // Send the intent to the Launcher
        context.sendBroadcast(intent);
        AnalyticsHelper.trackAppAuthenticationStatusBroadcasted(isUserAuthenticated);
    }

    /**
     * Examines the intent to identify if this call is from launcher via catalog integration
     *
     * @param intent Intent to examine
     * @return True if the call is from launcher, false otherwise.
     */
    public static boolean isCallFromLauncher(Intent intent) {

        Log.d(TAG, "Intent received to examine for launcher integration " + intent);
        if (intent == null) {
            return false;
        }
        // If the intent action is one of the launcher actions, it is from launcher.
        return PLAY_CONTENT_FROM_LAUNCHER_ACTION.equals(intent.getAction()) ||
                SIGN_IN_FROM_LAUNCHER_ACTION.equals(intent.getAction());
    }

    /**
     * Extracts the content Id value to be played from intent
     *
     * @param context Application context
     * @param intent  Intent to read
     * @return The content id to be played, "0" if no content id exists in the intent.
     */
    public static String getContentIdToPlay(Context context, Intent intent) {

        String content_id_key = context.getString(R.string.launcher_integration_content_id_key);
        if (intent == null || intent.getStringExtra(content_id_key) == null) {
            return "0";
        }
        return intent.getStringExtra(content_id_key);
    }

    /**
     * Extracts the source of content play request from the intent
     *
     * @param intent Intent to read
     * @return source of content play request
     */
    public static String getSourceOfContentPlayRequest(Intent intent) {

        if (intent == null || intent.getStringExtra(CONTENT_SOURCE) == null) {
            return null;
        }
        return intent.getStringExtra(CONTENT_SOURCE);
    }
}