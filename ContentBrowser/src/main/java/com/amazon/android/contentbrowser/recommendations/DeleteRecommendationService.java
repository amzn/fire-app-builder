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


import com.amazon.android.contentbrowser.helper.AnalyticsHelper;

import android.app.IntentService;
import android.content.Intent;
import android.util.Log;

/**
 * This is a service class for deleting recommendations. It expects to find the id of the
 * recommendation to delete in the intent. If the id was present the service will do the following:
 * delete the recommendation from the app's database, and inform the notification manager to cancel
 * the recommendation.
 */
public class DeleteRecommendationService extends IntentService {


    private static final String TAG = DeleteRecommendationService.class.getSimpleName();

    /**
     * Creates a delete recommendation service.
     */
    public DeleteRecommendationService() {

        super(TAG);
    }

    @Override
    protected void onHandleIntent(Intent intent) {

        Log.d(TAG, "Delete recommendation service called.");
        int id = intent.getIntExtra(RecommendationManager.NOTIFICATION_ID_TAG, -1);

        RecommendationManager manager = new RecommendationManager(getApplicationContext());
        manager.dismissRecommendation(id);
        AnalyticsHelper.trackDeleteRecommendationServiceCalled(id);
    }
}