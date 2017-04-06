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


import com.amazon.android.utils.ErrorUtils;
import com.amazon.android.ui.fragments.ErrorDialogFragment;
import com.amazon.android.utils.Helpers;

import android.app.Activity;
import android.app.FragmentManager;
import android.util.Log;

/**
 * Error helper class.
 */
public class ErrorHelper {

    /**
     * Debug TAG.
     */
    private static final String TAG = ErrorHelper.class.getSimpleName();

    /**
     * Inject error fragment to activity.
     *
     * @param activity                    Activity.
     * @param error_category              Error category.
     * @param errorDialogFragmentListener Error dialog fragment listener.
     */
    public static void injectErrorFragment(Activity activity,
                                           ErrorUtils.ERROR_CATEGORY error_category,
                                           ErrorDialogFragment.ErrorDialogFragmentListener
                                                   errorDialogFragmentListener) {

        ErrorDialogFragment errorDialogFragment;
        FragmentManager fm = activity.getFragmentManager();
        if (!Helpers.isConnectedToNetwork(activity)) {
            Log.d(TAG, "Experiencing a network error.");
            errorDialogFragment =
                    ErrorDialogFragment.newInstance(
                            activity,
                            ErrorUtils.ERROR_CATEGORY.NETWORK_ERROR,
                            (errorDialogFragment1, errorButtonType, errorCategory) -> {
                                if (ErrorUtils.ERROR_BUTTON_TYPE.NETWORK_SETTINGS ==
                                        errorButtonType) {
                                    ErrorUtils.showNetworkSettings(activity);
                                }
                                else if (ErrorUtils.ERROR_BUTTON_TYPE.DISMISS == errorButtonType) {
                                    errorDialogFragment1.dismiss();
                                }
                            });
        }
        else {
            Log.d(TAG, "Experiencing a non-network error");
            errorDialogFragment = ErrorDialogFragment.newInstance(activity, error_category,
                                                                  errorDialogFragmentListener);
        }
        errorDialogFragment.show(fm, ErrorDialogFragment.FRAGMENT_TAG_NAME);

        // Inform analytics that an error occurred.
        AnalyticsHelper.trackError(activity.getLocalClassName(),
                                   ErrorUtils.getErrorMessage(activity, error_category));
    }
}
