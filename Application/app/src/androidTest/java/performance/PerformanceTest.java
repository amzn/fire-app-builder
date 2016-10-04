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

package performance;

import com.amazon.android.tv.tenfoot.ui.activities.SplashActivity;
import com.robotium.solo.Solo;

import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.amazon.android.tv.tenfoot.R;

/**
 * This is a test case for measuring performance data.
 */
public class PerformanceTest extends ActivityInstrumentationTestCase2<SplashActivity> {

    private Solo solo;

    private final String TAG = PerformanceTest.class.getName();

    // allow 15 seconds for app to initialize; it can take this long
    // in the event of a slow network connection
    final int SPLASH_LOAD_TIME_LIMIT_MS = 15000;
    long startTime;

    public PerformanceTest() {

        super(SplashActivity.class);
    }

    @Override
    public void setUp() throws Exception {

        solo = new Solo(getInstrumentation(), getActivity());
        startTime = System.currentTimeMillis();
    }

    @Override
    public void tearDown() throws Exception {

        solo.finishOpenedActivities();
    }

    /**
     * Check time it takes for main Activity to load
     */
    public void testPerformanceSplashScreen() {

        solo.waitForView(R.id.full_content_browse_fragment);
        int loadTime = (int) (System.currentTimeMillis() - startTime);
        Log.d(TAG, "Loading time: " + loadTime + " ms");
        if (loadTime > SPLASH_LOAD_TIME_LIMIT_MS) {
            Log.w(TAG, "Splash screen took longer to load than " + SPLASH_LOAD_TIME_LIMIT_MS
                    + " ms");
        }
    }


}
