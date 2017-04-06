/**
 * This file was modified by Amazon:
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
/*
 * Copyright (C) 2014 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.amazon.android.tv.tenfoot.ui.activities;


import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.helper.FontManager;
import com.amazon.android.interfaces.ICancellableLoad;
import com.amazon.android.ui.constants.ConfigurationConstants;
import com.amazon.android.utils.Helpers;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.base.BaseActivity;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * This is a splash activity to load when the app is initializing and loading its content.
 */
public class SplashActivity extends BaseActivity implements ICancellableLoad {

    private static final String TAG = SplashActivity.class.getSimpleName();
    private ImageView mAppLogo;
    private ProgressBar mProgress;
    private boolean isLoadingCancelled = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.splash_activity_layout);
        mAppLogo = (ImageView) findViewById(R.id.main_logo);
        mProgress = (ProgressBar) findViewById(R.id.feed_progress);
        TextView mProgressText = (TextView) findViewById(R.id.feed_loader);
        TextView copyrightTextView = (TextView) findViewById(R.id.copyright);

        try {
            // Update copyright text with app version.
            PackageInfo pInfo = getPackageManager().getPackageInfo(getPackageName(), 0);
            copyrightTextView.append("\nVersion " + pInfo.versionName);
        }
        catch (Resources.NotFoundException exception) {
            Log.e(TAG, "Resource not found: ", exception);
        }
        catch (PackageManager.NameNotFoundException exception) {
            Log.e(TAG, "Package name not found: ", exception);
        }
        // Check to see if this activity is not called from the TenFootApp.
        if (!getIntent().hasExtra(ContentBrowser.CONTENT_WILL_UPDATE)) {
            mProgressText.setText(R.string.feed_loading);
        }
        // If this activity was called from the TenFootApp call activity method.
        else {
            mProgressText.setText(R.string.feed_reloading);
        }
    }

    @Override
    protected void onNewIntent(Intent intent) {

        if (Helpers.DEBUG) {
            Log.d(TAG, "Splash onNewIntent:" + intent);
        }
        this.setIntent(intent);
    }

    @Override
    public void onResume() {

        super.onResume();
        isLoadingCancelled = false;
        if (!getIntent().hasExtra(ContentBrowser.CONTENT_WILL_UPDATE)) {
            Log.d(TAG, "First loading");
            new AsyncTask<Activity, Void, Void>() {

                @Override
                protected Void doInBackground(Activity... activity) {

                    ContentBrowser contentBrowser = ContentBrowser.getInstance(activity[0]);
                    try {
                        configureFonts(contentBrowser);
                        contentBrowser.onAllModulesLoaded();
                        contentBrowser.runGlobalRecipes(activity[0], (SplashActivity) activity[0]);
                    }
                    catch (Exception e) {
                        Log.e(TAG, "Failed to put data in cache for recipe ", e);
                    }

                    return null;
                }
            }.execute(this);

        }
    }

    /**
     * Returns if the loading request is cancelled or not.
     * For this class it will never be cancelled.
     *
     * @return True if loading is cancelled
     */
    public boolean isLoadingCancelled() {

        return isLoadingCancelled;
    }

    @Override
    public void onPause() {

        isLoadingCancelled = true;
        super.onPause();
    }

    /**
     * Configures the fonts using the configuration settings from Navigator.json if present.
     * Otherwise is uses default fonts defined in custom.xml.
     *
     * @param contentBrowser The instance of {@link ContentBrowser}.
     */
    private void configureFonts(ContentBrowser contentBrowser) {

        configureFontPath(ConfigurationConstants.LIGHT_FONT,
                          contentBrowser.getLightFontPath(),
                          getResources().getString(R.string.default_light_font));

        configureFontPath(ConfigurationConstants.BOLD_FONT,
                          contentBrowser.getBoldFontPath(),
                          getResources().getString(R.string.default_bold_font));

        String regularFontPath =
                configureFontPath(ConfigurationConstants.REGULAR_FONT,
                                  contentBrowser.getRegularFontPath(),
                                  getResources().getString(R.string.default_regular_font));

        // Set the default font for the app.
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                                              .setDefaultFontPath(regularFontPath)
                                              .setFontAttrId(R.attr.fontPath)
                                              .build());

    }

    /**
     * Adds a font path to the Configuration Manager.
     *
     * @param pathKey         The key to use to retrieve the font path from the Configuration
     *                        Manager.
     * @param settingFontPath The font path that was given from Navigator.json config file.
     * @param defaultFontPath A default font to use if settings was not provided in the config
     *                        file.
     * @return The path that was stored in the Configuration Manager.
     */
    private String configureFontPath(String pathKey, String settingFontPath, String
            defaultFontPath) {

        ConfigurationManager manager = ConfigurationManager.getInstance(this);

        // Get all device local fonts.
        Map<String, String> fonts = FontManager.enumerateFonts();

        // Figure out if default font path is needed.
        String fontPath = settingFontPath == null ? defaultFontPath : settingFontPath;

        // If the font path specifies a local device font name, replace it with the
        // absolute path of the font. Otherwise, just handle it as a custom font path.
        if (fonts.containsKey(fontPath)) {
            fontPath = fonts.get(fontPath);
        }

        manager.setTypefacePathValue(pathKey, fontPath);

        return fontPath;
    }


}
