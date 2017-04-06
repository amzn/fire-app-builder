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
package com.amazon.android.contentbrowser.recommendations;

import com.amazon.android.contentbrowser.helper.LauncherIntegrationManager;
import com.amazon.android.navigator.Navigator;
import com.amazon.android.utils.Preferences;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * This service starts the recommendation update process.
 */
public class UpdateRecommendationsService extends IntentService {

    private static final String TAG = UpdateRecommendationsService.class.getSimpleName();

    /**
     * Constructs the update service.
     */
    public UpdateRecommendationsService() {

        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Updating recommendations");

        RecommendationManager manager = new RecommendationManager(getApplicationContext());

        manager.cleanDatabase();

        // Send recommendations if authentication is not required or the user is logged in.
        if (!Navigator.isScreenAccessVerificationRequired(
                manager.getContentLoader().getNavigatorModel()) ||
                Preferences.getBoolean(
                        LauncherIntegrationManager.PREFERENCE_KEY_USER_AUTHENTICATED)) {

            manager.updateGlobalRecommendations(getApplicationContext());
        }
    }

}

