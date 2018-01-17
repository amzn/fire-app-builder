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
package com.amazon.android.navigator;

import com.amazon.android.interfaces.IActivityTransition;
import com.amazon.utils.StringManipulation;
import com.amazon.utils.ds.Graph;

import android.app.Activity;
import android.app.Application;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Navigator class that tracks activity life cycle
 */
public class Navigator implements Application.ActivityLifecycleCallbacks {

    /**
     * Debug TAG.
     */
    private static final String TAG = Navigator.class.getSimpleName();

    /**
     * Navigator file name.
     */
    public static final String NAVIGATOR_FILE = "Navigator.json";

    /**
     * Context.
     */
    private Context mContext;

    /**
     * Navigator graph.
     */
    private Graph mNavigatorGraph = new Graph(TAG);

    /**
     * Current activity reference.
     */
    private Activity mCurrentActivity;

    /**
     * Navigator model.
     */
    private NavigatorModel mNavigatorModel;

    /**
     * List of runnable to run on upcoming activity.
     */
    private List<Runnable> mRunOnUpcomingActivityList = new ArrayList<>();

    /**
     * Navigation listener callback.
     */
    private INavigationListener mINavigationListener;

    /**
     * Screen name map.
     * Key is activity name.
     * Value is screen name.
     */
    private Map<String, String> mScreenNameMap = new HashMap<>();

    /**
     * Screen class map.
     * Key is screen name.
     * Value is class of that screen.
     */
    private Map<String, Class> mScreenNameClassMap = new HashMap<>();

    /**
     * Is app in background flag.
     */
    private boolean mAppInBackground = true;

    /**
     * Started activity count.
     * TODO: mStartedActivityCount missed Splash as Navigator wasn't loaded.
     * Set to 1 to account for this.
     * This is confusing, fix in DEVTECH-2972
     */
    private int mStartedActivityCount = 1;

    /**
     * Activity switch listener interface.
     */
    public interface ActivitySwitchListener {

        // This method will be called before activity switch happens to let us add intent values.
        void onPreActivitySwitch(Intent intent);
    }

    /**
     * Navigation listener.
     */
    public interface INavigationListener {

        void onSetTheme(Activity activity);

        void onScreenCreate(Activity activity, String screenName);

        void onScreenGotFocus(Activity activity, String screenName);

        void onScreenLostFocus(Activity activity, String screenName);

        void onApplicationGoesToBackground();
    }

    /**
     * Constructor.
     *
     * @param activity The active activity.
     */
    public Navigator(Activity activity) {

        this(activity, NAVIGATOR_FILE);
    }

    public Navigator(Activity activity, String navigatorFilePath) {

        mCurrentActivity = activity;
        mContext = activity.getApplicationContext();
        ((Application) mContext.getApplicationContext()).registerActivityLifecycleCallbacks(this);

        try {
            mNavigatorModel = NavigatorModelParser.parse(mContext, navigatorFilePath);

            // Traverse graph nodes.
            for (String entry : mNavigatorModel.getGraph().keySet()) {
                Graph.Node node = mNavigatorGraph.getNodeByName(entry);
                if (node == null) {
                    node = new Graph.Node(entry);
                    mNavigatorGraph.addNode(node);
                }

                // Add UI node.
                UINode uiNode = mNavigatorModel.getGraph().get(entry);
                node.setObject(uiNode);

                // Set screen name maps.
                mScreenNameClassMap.put(uiNode.getOnAction(), Class.forName(entry));
                mScreenNameMap.put(entry, uiNode.getOnAction());

                List<String> connections = uiNode.getNodes();
                if (connections != null) {
                    for (String connection : connections) {
                        Graph.Node connectionNode = mNavigatorGraph.getNodeByName(connection);
                        if (connectionNode == null) {
                            connectionNode = new Graph.Node(connection);
                        }
                        mNavigatorGraph.addConnection(node, connectionNode);
                    }
                }
            }
        }
        catch (Exception e) {
            Log.e(TAG, "Error traversing the graph nodes. ", e);
        }

        Log.v(TAG, "Navigator Graph: " + mNavigatorGraph);
    }

    /**
     * Is app in background.
     *
     * @return True if app is in background.
     */
    public boolean isAppInBackground() {

        return mAppInBackground;
    }

    /**
     * Run provided runnable on upcoming activity.
     *
     * @param runnable Runnable.
     */
    public void runOnUpcomingActivity(Runnable runnable) {

        mRunOnUpcomingActivityList.add(runnable);
    }

    /**
     * Get navigator model.
     *
     * @return Navigator model.
     */
    public NavigatorModel getNavigatorModel() {

        return mNavigatorModel;
    }

    /**
     * Set navigation listener.
     *
     * @param listener Navigation listener.
     */
    public void setINavigationListener(INavigationListener listener) {

        mINavigationListener = listener;
    }

    /**
     * Get node object by screen name.
     *
     * @param screenName Screen name.
     * @return Node object.
     */
    public Object getNodeObjectByScreenName(String screenName) {

        return mNavigatorGraph.getNodeByName(mScreenNameClassMap.get(screenName).getName())
                              .getObject();
    }

    /**
     * Returns true if any node of the graph for the given navigator model has verifyScreenAccess
     * set to true.
     *
     * @param model The navigator model to check.
     * @return True if screen access verification is required; false otherwise.
     */
    public static boolean isScreenAccessVerificationRequired(NavigatorModel model) {

        if (model != null && model.getGraph() != null) {
            // Get the UINodes and check if any of them require verify screen access.
            for (UINode node : model.getGraph().values()) {
                // If verification is required then we know were using authentication so add the
                // setting
                if (node.isVerifyScreenAccess()) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * Get active activity.
     *
     * @return Active activity.
     */
    public Activity getActiveActivity() {

        return mCurrentActivity;
    }

    /**
     * Start activity.
     *
     * @param screenName Screen name.
     * @param bundle     Bundle to be passed to start activity.
     */
    public void startActivity(String screenName, Bundle bundle) {

        startActivity(mCurrentActivity, screenName, bundle, null);
    }

    /**
     * Start activity.
     *
     * @param screenName             Screen name.
     * @param activitySwitchListener Activity switch listener.
     */
    public void startActivity(String screenName,
                              ActivitySwitchListener activitySwitchListener) {

        startActivity(mCurrentActivity, screenName, null, activitySwitchListener);
    }

    /**
     * Start activity.
     *
     * @param currentActivity Current activity reference.
     * @param screenName      Screen name.
     * @param providedBundle  Provided bundle.
     */
    private void startActivity(Activity currentActivity,
                               String screenName,
                               Bundle providedBundle,
                               ActivitySwitchListener activitySwitchListener) {

        Class newActivityClass = mScreenNameClassMap.get(screenName);

        if (mCurrentActivity == null) {
            Log.d(TAG, "mCurrentActivity is null, switching to:" + screenName);
            Intent intent = new Intent();

            intent.setComponent(new ComponentName(mContext, newActivityClass));
            if (activitySwitchListener != null) {
                activitySwitchListener.onPreActivitySwitch(intent);
            }
            mContext.startActivity(intent);
            return;
        }

        Intent intent = new Intent(currentActivity, newActivityClass);

        if (activitySwitchListener != null) {
            activitySwitchListener.onPreActivitySwitch(intent);
        }

        Bundle bundle = providedBundle;

        IActivityTransition activityTransition = null;
        if (currentActivity instanceof IActivityTransition) {
            activityTransition = (IActivityTransition) currentActivity;

            activityTransition.onBeforeActivityTransition();
        }

        Log.d(TAG, "Activity: " + getActivityName(currentActivity) + " is starting a new " +
                "activity" + intent);
        if (bundle != null) {
            currentActivity.startActivity(intent, bundle);
        }
        else {
            currentActivity.startActivity(intent);
        }

        if (activityTransition != null) {
            activityTransition.onPostStartActivity();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityCreated(Activity activity, Bundle savedInstanceState) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityCreated");

        String screenName = mScreenNameMap.get(activity.getClass().getName());
        mCurrentActivity = activity;
        if (mINavigationListener != null) {
            mINavigationListener.onScreenCreate(activity, screenName);
            mINavigationListener.onSetTheme(activity);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStarted(Activity activity) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityStarted");
        mStartedActivityCount++;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResumed(Activity activity) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityResumed " + activity.getClass().getName());

        mAppInBackground = true;
        String screenName = mScreenNameMap.get(activity.getClass().getName());
        mCurrentActivity = activity;
        if (screenName != null) {
            mINavigationListener.onScreenGotFocus(activity, screenName);
        }

        for (Runnable runnable : mRunOnUpcomingActivityList) {
            mCurrentActivity.runOnUiThread(runnable);
        }
        mRunOnUpcomingActivityList.clear();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityPaused(Activity activity) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityPaused");

        String screenName = mScreenNameMap.get(activity.getClass().getName());
        if (screenName != null) {
            mINavigationListener.onScreenLostFocus(activity, screenName);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityStopped(Activity activity) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityStopped");
        mStartedActivityCount--;
        Log.d(TAG, "startedActivityCount:" + mStartedActivityCount);
        if (mStartedActivityCount == 0 && mINavigationListener != null) {
            mAppInBackground = false;
            Log.d(TAG, "application going in background");
            mINavigationListener.onApplicationGoesToBackground();
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivitySaveInstanceState(Activity activity, Bundle outState) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivitySaveInstanceState");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityDestroyed(Activity activity) {

        String activityName = getActivityName(activity);
        Log.d(TAG, activityName + " onActivityDestroyed");

        if (mCurrentActivity == activity) {
            mCurrentActivity = null;
            Log.w(TAG, "Current activity is becoming null, as it is getting destroyed!!!");
        }
    }

    /**
     * {@inheritDoc}
     */
    private static String getActivityName(Activity activity) {

        return StringManipulation.getExtension(activity.getLocalClassName());
    }
}
