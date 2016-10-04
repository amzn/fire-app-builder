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
package com.amazon.android.utils;

import android.app.Activity;
import android.app.Fragment;
import android.app.FragmentTransaction;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;

/**
 * Helper class to inject fragment.
 */
public class FragmentInjectionHelper {

    public static final String FRAGMENT_CLASS_NAME_INTENT_FIELD = "injectedFragment";

    /**
     * Injects fragment on the activity for given resId.
     *
     * @param activity Activity on which fragment needs to be injected.
     * @param resId    ResId on which fragment needs to be injected.
     */
    public static void injectFragment(Activity activity, int resId) {

        String injectedFragmentClassName =
                activity.getIntent().getStringExtra(FRAGMENT_CLASS_NAME_INTENT_FIELD);
        try {
            Class<?> clazz = Class.forName(injectedFragmentClassName);
            Constructor<?> ctor = clazz.getConstructor();

            Fragment fragment = (Fragment) ctor.newInstance();
            FragmentTransaction fragmentTransaction = activity.getFragmentManager()
                                                              .beginTransaction();
            fragmentTransaction.add(resId, fragment).commit();

        }
        catch (InstantiationException | IllegalAccessException | InvocationTargetException |
                ClassNotFoundException | NoSuchMethodException e) {
            e.printStackTrace();
        }
    }
}
