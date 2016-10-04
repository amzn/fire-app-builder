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
package com.amazon.android.ui.interfaces;

import com.amazon.android.model.Action;
import com.amazon.android.ui.fragments.ReadDialogFragment;

import android.app.Activity;
import android.app.FragmentManager;
import android.app.FragmentTransaction;

/**
 * Abstract class for providing a Settings fragment.
 * Every settings item can have a unique dialog fragment, which is provided via this class.
 */
public abstract class ASettingsFragment {

    /**
     * Creates the fragment to show on clicking this settings item.
     *
     * @param activity       The activity on which this fragment was triggered.
     * @param manager        The fragment manager to attach the created fragment.
     * @param settingsAction Action for this corresponding fragment.
     */
    public abstract void createFragment(Activity activity, FragmentManager manager,
                                        Action settingsAction);

    /**
     * Commits the fragment.
     *
     * @param manager     The fragment manager.
     * @param dialog      The fragment to commit.
     * @param fragmentTag tag for the fragment.
     */
    public static void commitFragment(FragmentManager manager, ReadDialogFragment dialog,
                                      String fragmentTag) {

        final FragmentTransaction ft = (manager.beginTransaction());
        ft.add(dialog, fragmentTag);
        ft.commit();
    }
}
