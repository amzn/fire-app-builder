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
package com.amazon.analytics.flurry;

import com.amazon.analytics.CustomAnalyticsTags;
import com.amazon.utils.security.ResourceObfuscator;
import com.flurry.android.FlurryAgent;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.support.annotation.NonNull;
import android.util.Log;

import java.io.UnsupportedEncodingException;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.NoSuchAlgorithmException;
import java.util.HashMap;

import com.amazon.analytics.AnalyticsTags;
import com.amazon.analytics.IAnalytics;

import javax.crypto.BadPaddingException;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;


/**
 * An analytics implementation using the
 * <a href="https://developer.yahoo.com/flurry/docs/analytics/">Yahoo flurry Analytics
 * framework</a>.
 *
 * NOTE: Flurry Analytics does not officially support apps that run on FireOS devices. Integrating
 * without at least the google play services analytics com.google.android
 * .gms:play-services-analytics is considered a non-standard integration.
 */
public class FlurryAnalytics implements IAnalytics {

    /**
     * Debug tag.
     */
    private static final String TAG = FlurryAnalytics.class.getSimpleName();

    /**
     * Default error ID.
     */
    private static final String errorId = "DEFAULT_ERROR_ID";

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    static final String IMPL_CREATOR_NAME = FlurryAnalytics.class.getSimpleName();

    private CustomAnalyticsTags mCustomTags = new CustomAnalyticsTags();

    /**
     * {@inheritDoc}
     */
    @Override
    public void configure(Context context) {
        // Set app version.
        try {
            PackageInfo pInfo = context.getPackageManager().getPackageInfo(context.getPackageName
                    (), 0);
            FlurryAgent.setVersionName(pInfo.versionName);
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "There was an exception when getting the app package info. ", e);
        }
        try {
            FlurryAgent.init(context, getFlurryApiKey(context));
        }
        catch (Exception e) {
            throw new RuntimeException("Could not configure FlurryAgent ", e);
        }
        mCustomTags.init(context, R.string.flurry_analytics_custom_tags);
        Log.d(TAG, "Flurry configuration complete.");
    }

    /**
     * Reads the encrypted Flurry API key and returns the decrypted version of it.
     *
     * @param context Application context.
     * @return The plain text flurry api key.
     * @throws UnsupportedEncodingException       Exception generated during decryption.
     * @throws InvalidAlgorithmParameterException Exception generated during decryption.
     * @throws NoSuchAlgorithmException           Exception generated during decryption.
     * @throws NoSuchPaddingException             Exception generated during decryption.
     * @throws BadPaddingException                Exception generated during decryption.
     * @throws IllegalBlockSizeException          Exception generated during decryption.
     * @throws InvalidKeyException                Exception generated during decryption.
     */
    @NonNull
    private String getFlurryApiKey(Context context) throws
            UnsupportedEncodingException, InvalidAlgorithmParameterException,
            NoSuchAlgorithmException, NoSuchPaddingException, BadPaddingException,
            IllegalBlockSizeException, InvalidKeyException {

        return ResourceObfuscator.unobfuscate(context.getString(R.string.encrypted_flurry_api_key),
                                              getRandomStringsForKey(context),
                                              getRandomStringsForIv(context));
    }

    /**
     * Get a list of "random" strings for key.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForKey(Context context) {

        return new String[]{
                context.getString(R.string.flurry_key_1),
                context.getString(R.string.flurry_key_2),
                context.getString(R.string.flurry_key_3)
        };
    }

    /**
     * Get random strings for use with initialization vector.
     *
     * @param context Context to access resources.
     * @return List of strings.
     */
    private static String[] getRandomStringsForIv(Context context) {

        return new String[]{
                context.getString(R.string.flurry_key_4),
                context.getString(R.string.flurry_key_5),
                context.getString(R.string.flurry_key_6)
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void collectLifeCycleData(Activity activity, boolean active) {

        String activityName = activity.getClass().getName();
        if (active) {
            Log.d(TAG, "Collecting lifecycle data for " + activityName);
            FlurryAgent.logEvent(activityName, true);
        }
        else {
            Log.d(TAG, "Ending lifecycle data collection for " + activityName);
            FlurryAgent.endTimedEvent(activityName);
        }
    }

    /**
     * {@inheritDoc}
     *
     * The values corresponding to the keys of the data map but be of type {@link String}.
     */
    @Override
    public void trackAction(HashMap<String, Object> data) {
        // Get the action name.
        String action = (String) data.get(AnalyticsTags.ACTION_NAME);
        // Get the attributes map.
        try {
            HashMap<String, String> contextData = new HashMap<>();
            HashMap<String, Object> contextDataObjectMap =
                    (HashMap<String, Object>) data.get(AnalyticsTags.ATTRIBUTES);
            for (String key : contextDataObjectMap.keySet()) {
                contextData.put(key, String.valueOf(contextDataObjectMap.get(key)));
            }
            // Record action.
            FlurryAgent.logEvent(mCustomTags.getCustomTag(action),
                    mCustomTags.getCustomTags(contextData));

            Log.d(TAG, "Tracking action " + mCustomTags.getCustomTag(action) + " with attributes: "
                    + mCustomTags.getCustomTags(contextData));
        }
        catch (Exception e) {
            Log.e(TAG, "The params map was not of type <String, String> for action " + action +
                    ", dropping the map and just logging the event", e);
            // Record action.
            FlurryAgent.logEvent(mCustomTags.getCustomTag(action));
            Log.d(TAG, "Tracking action " + mCustomTags.getCustomTag(action));
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackState(String screen) {

        Log.d(TAG, "Tracking state for screen " + screen);
        FlurryAgent.logEvent(screen);
        FlurryAgent.onPageView();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void trackCaughtError(String errorMessage, Throwable t) {

        FlurryAgent.onError(errorId, errorMessage, t);
    }
}
