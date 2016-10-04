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

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.RadioButton;
import android.widget.RadioGroup;

import com.amazon.android.model.Action;
import com.amazon.android.ui.interfaces.ASettingsFragment;
import com.amazon.android.ui.interfaces.SingleViewProvider;
import com.amazon.android.utils.Preferences;
import com.amazon.utils.R;

/**
 * Provides fragment to be shown when slide speed settings item is clicked.
 */
public class SlideShowSettingFragment extends ASettingsFragment {

    /**
     * Debug Tag.
     */
    private static final String TAG = SlideShowSettingFragment.class.getSimpleName();

    /**
     * Constant for slide show speed.
     */
    public static final String SLIDE_SHOW_SPEED = "SLIDE_SHOW_SPEED";

    /**
     * Context.
     */
    private Context mContext;

    /**
     * {@inheritDoc}
     */
    @Override
    public void createFragment(Activity activity, FragmentManager manager, Action settingsAction) {

        mContext = activity;

        final ReadDialogFragment dialog = new ReadDialogFragment();
        dialog.setDialogLayout(R.layout.simple_dialog_layout);
        dialog.setContentViewProvider(getSingleViewProvider(dialog,
                                                            R.layout.slide_show_setting_layout));
        dialog.setArguments(getArguments(getDefaultHeight(), getDefaultWidth()));
        commitFragment(manager, dialog,
                       mContext.getString(R.string.slideshow_settings_fragment_tag));
    }

    /**
     * Returns the default fragment width.
     *
     * @return The default fragment width.
     */
    private int getDefaultWidth() {

        return mContext.getResources().getDimensionPixelSize(R.dimen.slideshow_setting_width);
    }

    /**
     * Returns default fragment height based on layout.
     *
     * @return The default fragment width.
     */
    private int getDefaultHeight() {

        return mContext.getResources().getDimensionPixelSize(R.dimen.slideshow_setting_height);
    }

    /**
     * Get the arguments for the fragment.
     *
     * @param fragmentHeight The fragment height.
     * @param fragmentWidth  The fragment width.
     * @return The height and width stored in a {@link Bundle}.
     */
    private Bundle getArguments(int fragmentHeight, int fragmentWidth) {

        final Bundle args = new Bundle();
        args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_HEIGHT, fragmentHeight);
        args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_WIDTH, fragmentWidth);
        return args;
    }


    /**
     * Creates and returns a view based the layout.
     *
     * @param fragment The fragment where this view will be added.
     * @param layout   The layout to apply.
     * @return The created view.
     */
    private SingleViewProvider getSingleViewProvider(final ReadDialogFragment fragment, final int
            layout) {

        return new SingleViewProvider() {

            @Override
            public View getView(final Context context, final LayoutInflater inflater, final
            ViewGroup parent) {

                final View result = inflater.inflate(layout, parent);
                addActionForSpeedSelection(result);
                addActionForConfirmDialogButton(result, fragment);
                addActionsForDismissDialogButton(result, fragment);
                return result;
            }
        };
    }

    /**
     * Adds actions to the cancel button of the view.
     *
     * @param result   The view on which the cancel button exists.
     * @param fragment The fragment to dismiss.
     */
    private void addActionsForDismissDialogButton(View result, final ReadDialogFragment fragment) {

        Button cancel = (Button) result.findViewById(R.id.cancel_dialog);
        if (cancel == null) {
            return;
        }
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss the fragment
                fragment.dismiss();
            }
        });
    }

    /**
     * Adds actions to the confirm button of the view.
     *
     * @param result   The view on which the confirm button exists.
     * @param fragment The fragment to dismiss.
     */
    private void addActionForConfirmDialogButton(View result, final ReadDialogFragment fragment) {

        Button confirm = (Button) result.findViewById(R.id.confirm_dialog);
        if (confirm == null) {
            return;
        }
        confirm.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                RadioGroup radioGroup = (RadioGroup) result.findViewById(R.id.slide_speed);
                int selectedId = radioGroup.getCheckedRadioButtonId();

                if (selectedId == R.id.slow) {
                    Preferences.setLong(SLIDE_SHOW_SPEED,
                                        mContext.getResources()
                                                .getInteger(R.integer.slide_show_slow_speed));
                }
                else if (selectedId == R.id.fast) {
                    Preferences.setLong(SLIDE_SHOW_SPEED,
                                        mContext.getResources()
                                                .getInteger(R.integer.slide_show_fast_speed));
                }
                else {
                    Preferences.setLong(SLIDE_SHOW_SPEED,
                                        mContext.getResources()
                                                .getInteger(R.integer.slide_show_middle_speed));
                }
                //Dismiss the fragment
                fragment.dismiss();
            }
        });
    }

    /**
     * Get current speed setting.
     *
     * @param result The view on which the speed setting buttons exist.
     */
    private void addActionForSpeedSelection(View result) {

        Long speed = Preferences.getLong(SLIDE_SHOW_SPEED);
        RadioButton button;
        if (speed == mContext.getResources().getInteger(R.integer.slide_show_slow_speed)) {
            button = (RadioButton) result.findViewById(R.id.slow);
        }
        else if (speed == mContext.getResources().getInteger(R.integer.slide_show_fast_speed)) {
            button = (RadioButton) result.findViewById(R.id.fast);
        }
        else {
            button = (RadioButton) result.findViewById(R.id.middle);
        }
        button.setChecked(true);
        button.requestFocus();
    }
}
