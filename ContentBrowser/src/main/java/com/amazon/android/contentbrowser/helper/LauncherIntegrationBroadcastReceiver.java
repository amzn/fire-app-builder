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

import com.amazon.android.utils.Preferences;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

/**
 * Broadcast receiver to receive App authorization status requests from Launcher
 */
public class LauncherIntegrationBroadcastReceiver extends BroadcastReceiver {

    private static final String TAG = LauncherIntegrationBroadcastReceiver.class.getSimpleName();

    /**
     * {@inheritDoc}
     */
    @Override
    public void onReceive(Context context, Intent intent) {
        AnalyticsHelper.trackAppAuthenticationStatusRequested();
        boolean userAuthenticated = Preferences.getBoolean(LauncherIntegrationManager
                                                                .PREFERENCE_KEY_USER_AUTHENTICATED);
        LauncherIntegrationManager.sendAppAuthenticationStatusBroadcast(context, userAuthenticated);
    }
}
