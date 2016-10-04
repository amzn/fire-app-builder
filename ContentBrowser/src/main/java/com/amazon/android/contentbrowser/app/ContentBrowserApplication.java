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

package com.amazon.android.contentbrowser.app;

import com.amazon.ads.IAds;
import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.R;
import com.amazon.android.contentbrowser.helper.AnalyticsHelper;
import com.amazon.android.contentbrowser.helper.FontManager;
import com.amazon.android.module.ModularApplication;
import com.amazon.android.module.Module;
import com.amazon.android.module.ModuleManager;
import com.amazon.android.uamp.UAMP;
import com.amazon.android.ui.constants.ConfigurationConstants;
import com.amazon.android.utils.Preferences;
import com.amazon.auth.IAuthentication;
import com.amazon.purchase.IPurchase;
import com.squareup.leakcanary.LeakCanary;
import com.squareup.leakcanary.RefWatcher;

import android.os.Bundle;
import android.util.Log;

import com.amazon.analytics.AnalyticsManager;
import com.amazon.analytics.IAnalytics;

import java.util.Map;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

/**
 * Content browser application class.
 */
public class ContentBrowserApplication extends ModularApplication {

    /**
     * Debug tag.
     */
    private static final String TAG = ContentBrowserApplication.class.getSimpleName();

    /**
     * Module Manager singleton reference.
     */
    private static ModuleManager mModuleManager = ModuleManager.getInstance();

    /**
     * Content browser application singleton reference.
     */
    private static ContentBrowserApplication sInstance;

    /**
     * Reference watcher for debugging.
     */
    private RefWatcher mRefWatcher;

    /**
     * Analytics manager reference.
     */
    protected AnalyticsManager mAnalyticsManager;

    /**
     * Get singleton instance.
     *
     * @return Content browser application singleton instance.
     */
    public static ContentBrowserApplication getInstance() {

        return sInstance;
    }

    /**
     * Get reference watcher.
     *
     * @return Reference watcher.
     */
    public static RefWatcher getRefWatcher() {

        return ContentBrowserApplication.getInstance().mRefWatcher;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate() {

        super.onCreate();

        sInstance = (ContentBrowserApplication) getApplicationContext();

        Preferences.setContext(this);

        mAnalyticsManager = AnalyticsManager.getInstance(this);


        initAllModules(this.getApplicationContext());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onModulesLoaded() {
        // Setup Ads.
        IAds ads = (IAds) ModuleManager.getInstance()
                                       .getModule(IAds.class.getSimpleName())
                                       .getImpl(true);
        ads.setExtra(new Bundle());

        // Setup Analytics.
        IAnalytics analytics =
                (IAnalytics) ModuleManager.getInstance()
                                          .getModule(IAnalytics.class.getSimpleName())
                                          .getImpl(true);

        mAnalyticsManager.setAnalyticsInterface(analytics);
        sInstance.registerActivityLifecycleCallbacks(mAnalyticsManager);
        AnalyticsHelper.trackAppEntry();

        // TODO: When the pass through component is ready remove try catch block.
        try {
            // Init authentication module.
            IAuthentication authentication =
                    (IAuthentication) ModuleManager.getInstance()
                                                   .getModule(IAuthentication.class.getSimpleName())
                                                   .getImpl(true);

            authentication.init(this);
        }
        catch (Exception e) {
            Log.e(TAG, "IAuthentication implementation is not found!!!");
        }

        // Last call.

        postModulesLoaded();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void setModuleForInterface(String interfaceName) {

        if (UAMP.class.getSimpleName().equals(interfaceName)) {
            ModuleManager.getInstance().setModule(interfaceName, new Module<UAMP>());
        }
        else if (IAnalytics.class.getSimpleName().equals(interfaceName)) {
            ModuleManager.getInstance().setModule(interfaceName, new Module<IAnalytics>());
        }
        else if (IAds.class.getSimpleName().equals(interfaceName)) {
            ModuleManager.getInstance().setModule(interfaceName, new Module<IAds>());
        }
        else if (IAuthentication.class.getSimpleName().equals(interfaceName)) {
            ModuleManager.getInstance().setModule(interfaceName, new Module<IAuthentication>());
        }
        else if (IPurchase.class.getSimpleName().equals(interfaceName)) {
            ModuleManager.getInstance().setModule(interfaceName, new Module<IPurchase>());
        }
        else {
            Log.w(TAG, "Unknown interface found with name " + interfaceName);
            ModuleManager.getInstance().setModule(interfaceName, new Module<>());
        }
    }

    /**
     * Method to handle post module loaded steps.
     */
    protected void postModulesLoaded() {

    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onLowMemory() {

        super.onLowMemory();
        Log.e(TAG, "onLowMemory!!!");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onTerminate() {

        super.onTerminate();
        Log.e(TAG, "onTerminate!!!");
    }




}
