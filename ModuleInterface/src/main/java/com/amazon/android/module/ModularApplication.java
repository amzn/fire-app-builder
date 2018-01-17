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
package com.amazon.android.module;

import com.amazon.android.utils.Preferences;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;


/**
 * Base class for a modular application.
 */
public abstract class ModularApplication extends Application {

    /**
     * Debug TAG.
     */
    private static final String TAG = ModularApplication.class.getSimpleName();

    /**
     * Modules loaded flag.
     */
    private boolean mModulesLoaded = false;
    /**
     * Amazon Android plugin tag.
     */
    private static final String AMZN_ANDROID_PLUGIN_TAG = "AMZNAP";

    /**
     * Amazon plugin data separator.
     */
    private static final String AMZN_PLUGIN_DATA_SEPARATOR = "@";

    /**
     * Key for retrieving the number of app crashes from Shared Preferences.
     */
    public static final String APP_CRASHES_KEY = "appCrashes";

    /**
     * onCreate method.
     */
    @Override
    public void onCreate() {

        super.onCreate();
        Log.d(TAG, "onCreate called.");
        Thread.setDefaultUncaughtExceptionHandler(new Thread.UncaughtExceptionHandler() {

            @Override
            public void uncaughtException(Thread thread, Throwable exception) {

                Log.e(TAG, "Uncaught Exception in thread " + thread.toString(), exception);
                PackageManager manager = getPackageManager();
                Intent intent = manager.getLaunchIntentForPackage(getPackageName());
                if (intent == null) {
                    Log.e(TAG, "Could not generate Intent for package " + getPackageName());
                    android.os.Process.killProcess(android.os.Process.myPid());
                    System.exit(0);
                }

                long crashes = Preferences.getLong(APP_CRASHES_KEY);
                crashes++;
                // If less than 3 uncaught exceptions were already caught try restarting the app.
                if (crashes < 3) {
                    Preferences.setLong(APP_CRASHES_KEY, crashes);
                    Log.e(TAG, "This is crash number " + crashes);

                    intent.addCategory(Intent.CATEGORY_LAUNCHER);
                    Log.i(TAG, "Launching intent " + intent.toString());
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK
                                            | Intent.FLAG_ACTIVITY_NEW_TASK);
                    startActivity(intent);
                }
                // Otherwise clear the app crash counter and just kill the app for manual restart.
                else {
                    Preferences.setLong(APP_CRASHES_KEY, 0);
                }
                android.os.Process.killProcess(android.os.Process.myPid());
            }
        });
    }

    /**
     * Get module loading state.
     *
     * @return True if modules are loaded.
     */
    public boolean areModulesLoaded() {

        return mModulesLoaded;
    }

    /**
     * This method will be called after all modules loaded.
     */
    abstract public void onModulesLoaded();

    /**
     * Initialize the modules to the module manager.
     *
     * @param context Application context.
     */
    protected void initAllModules(Context context) {

        initPluginsByManifestMetadata(context);
    }

    /**
     * Reads the metadata from all AndroidManifests included in the application and find android
     * plugins to initialize.
     *
     * @param context Application context.
     */
    private void initPluginsByManifestMetadata(Context context) {

        try {
            ApplicationInfo applicationInfo = context.getPackageManager()
                                                     .getApplicationInfo(context.getPackageName()
                                                             , PackageManager.GET_META_DATA);

            Bundle bundle = applicationInfo.metaData;

            for (String key : bundle.keySet()) {
                if (key.startsWith(AMZN_ANDROID_PLUGIN_TAG)) {
                    String pluginName = key.substring(key.indexOf(AMZN_PLUGIN_DATA_SEPARATOR) + 1);

                    String keyValue = bundle.getString(key);
                    if (keyValue == null) {
                        Log.e(TAG, "Null value received for key: " + key);
                        continue;
                    }
                    String interfaceName = keyValue.substring(0, keyValue.indexOf
                            (AMZN_PLUGIN_DATA_SEPARATOR));
                    String implCreatorName = keyValue.substring(keyValue.indexOf
                            (AMZN_PLUGIN_DATA_SEPARATOR) + 1);
                    Log.d(TAG, "pluginName:" + pluginName +
                            " interfaceName:" + interfaceName +
                            " implCreatorName:" + implCreatorName);

                    Class<?> clazz = Class.forName(implCreatorName);

                    if (ModuleManager.getInstance().getModule(interfaceName) == null) {
                        setModuleForInterface(interfaceName);
                    }

                    ModuleManager.getInstance()
                                 .getModule(interfaceName)
                                 .setImplCreator(pluginName, (IImplCreator) clazz.newInstance());
                }
            }
            Log.i(TAG, "All Modules Initialized!");
            mModulesLoaded = true;
            onModulesLoaded();
        }
        catch (PackageManager.NameNotFoundException e) {
            Log.e(TAG, "PackageManager.NameNotFoundException during plugin initialization", e);
        }
        catch (ClassNotFoundException e) {
            Log.e(TAG, "ClassNotFoundException during plugin initialization", e);
        }
        catch (InstantiationException e) {
            Log.e(TAG, "InstantiationException during plugin initialization", e);
        }
        catch (IllegalAccessException e) {
            Log.e(TAG, "IllegalAccessException during plugin initialization", e);
        }
    }

    /**
     * Creates and sets the modules for the param interfaceName.
     *
     * @param interfaceName Interface for the module that needs to be set.
     */
    protected abstract void setModuleForInterface(String interfaceName);

}
