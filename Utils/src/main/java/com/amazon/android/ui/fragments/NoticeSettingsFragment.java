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
package com.amazon.android.ui.fragments;

import com.amazon.android.model.Action;
import com.amazon.android.ui.interfaces.ASettingsFragment;
import com.amazon.android.ui.interfaces.SingleViewProvider;
import com.amazon.android.utils.Helpers;
import com.amazon.utils.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.graphics.Point;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.text.Html;
import android.text.Spanned;
import android.util.Log;
import android.view.Display;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Provides fragment to be shown when "Terms of Use" settings item is clicked.
 */
public class NoticeSettingsFragment extends ASettingsFragment {

    private static final String TAG = NoticeSettingsFragment.class.getSimpleName();

    /**
     * The activity.
     */
    private Activity mActivity;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFragment(final Activity activity,
                               FragmentManager manager,
                               Action settingsItem) {

        final ReadDialogFragment dialog = new ReadDialogFragment();
        dialog.setContentViewProvider(getSingleViewProvider(activity));
        dialog.setArguments(getArguments(activity));
        commitFragment(manager, dialog, activity.getString(R.string.notice_settings_fragment_tag));
        mActivity = activity;

    }

    /**
     * Get the arguments for the fragment
     */
    @NonNull
    private Bundle getArguments(final Activity activity) {

        final Bundle args = new Bundle();
        Display display = activity.getWindowManager().getDefaultDisplay();
        Point size = new Point();
        display.getSize(size);
        args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_HEIGHT, size.y);
        args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_WIDTH, size.x);
        return args;
    }

    /*
     * Sets up the view to display the Notice Settings item content.
     */
    private SingleViewProvider getSingleViewProvider(Context context) {

        String content = "";
        try {
            content = Helpers.getContentFromFile(context,
                                                 context.getString(R.string.terms_of_use_file));
        }
        catch (Exception e) {
            Log.e(TAG, "could not read terms of use file", e);
        }
        final Spanned spanned = Html.fromHtml(content);
        return new SingleViewProvider() {

            @Override
            public View getView(final Context context, final LayoutInflater inflater, final
            ViewGroup parent) {

                // Inflate view with activity's inflater so Calligraphy font will be applied.
                final View result =
                        mActivity.getLayoutInflater().inflate(R.layout.read_dialog_default_layout,
                                                              parent);
                final TextView mainText = (TextView) result.findViewById(R.id.txt);
                mainText.setText(spanned.toString(), TextView.BufferType.SPANNABLE);

                return result;
            }
        };
    }
}
