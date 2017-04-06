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

import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.model.Action;
import com.amazon.android.ui.constants.PreferencesConstants;
import com.amazon.android.ui.interfaces.ASettingsFragment;
import com.amazon.android.ui.constants.ConfigurationConstants;

import com.amazon.android.ui.interfaces.SingleViewProvider;
import com.amazon.android.utils.Preferences;
import com.amazon.utils.R;

import android.app.Activity;
import android.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Provides fragment to be shown when logout settings item is clicked.
 */
public class LogoutSettingsFragment extends ASettingsFragment {

    /**
     * Debug tag.
     */
    private static final String TAG = LogoutSettingsFragment.class.getSimpleName();

    /**
     * A constant for the logout button broadcast intent action.
     */
    public static final String LOGOUT_BUTTON_BROADCAST_INTENT_ACTION =
            "LOGOUT_BUTTON_BROADCAST_INTENT_ACTION";

    /**
     * A value for login status.
     */
    public static final int TYPE_LOGIN = 0;

    /**
     * A value for logout status.
     */
    public static final int TYPE_LOGOUT = 1;

    /**
     * The activity.
     */
    private Activity mActivity;

    /**
     * {@inheritDoc}
     *
     * Creates the fragment based on user's login/logout status.
     */
    @Override
    public void createFragment(final Activity activity, final FragmentManager manager,
                               Action settingsAction) {

        int layoutId = R.layout.login_layout;
        if (settingsAction.getState() == TYPE_LOGOUT) {
            layoutId = R.layout.logout_layout;
        }
        mActivity = activity;
        setUpDialog(activity, manager, layoutId);
    }

    /**
     * Creates a {@link ReadDialogFragment} and attaches it to the fragment manager.
     *
     * @param activity The activity.
     * @param layout   The layout id.
     * @param manager  The fragment manager.
     */
    private void setUpDialog(Activity activity, FragmentManager manager, int layout) {

        final ReadDialogFragment dialog = new ReadDialogFragment();
        dialog.setDialogLayout(R.layout.simple_dialog_layout);
        dialog.setContentViewProvider(getSingleViewProvider(dialog, activity, layout));
        dialog.setArguments(getArguments(getDefaultHeight(activity, layout),
                                         getDefaultWidth(activity)));
        commitFragment(manager, dialog, activity.getString(R.string.logout_settings_fragment_tag));

    }


    /**
     * Returns the default fragment width.
     *
     * @param context The activity context.
     * @return The default fragment width.
     */
    private int getDefaultWidth(Context context) {

        return context.getResources().getDimensionPixelSize(R.dimen.logout_fragment_width);
    }

    /**
     * Returns default fragment height based on layout.
     *
     * @param context The activity context.
     * @param layout  The layout for which height is required.
     * @return The default fragment width.
     */
    private int getDefaultHeight(Context context, int layout) {

        if (layout == R.layout.login_layout) {
            return context.getResources().getDimensionPixelSize(R.dimen.login_fragment_height);
        }
        else if (layout == R.layout.logout_layout) {
            return context.getResources().getDimensionPixelSize(R.dimen.logout_fragment_height);
        }
        else {
            return 0;
        }
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
     * @param context  The application context.
     * @param layout   The layout to apply.
     * @return The created view.
     */
    private SingleViewProvider getSingleViewProvider(final ReadDialogFragment fragment,
                                                     Context context, final int layout) {

        return new SingleViewProvider() {

            @Override
            public View getView(final Context context, final LayoutInflater inflater, final
            ViewGroup parent) {

                // Inflate view with activity's inflater so Calligraphy font is applied.
                final View result = mActivity.getLayoutInflater().inflate(layout, parent);
                addActionsForAcceptLogoutButton(context, result, fragment);
                addActionsForDismissDialogButton(context, result, fragment);

                if (result == null) {
                    return null;
                }

                TextView title = (TextView) result.findViewById(R.id.logout_title);
                CalligraphyUtils.applyFontToTextView(context, title, ConfigurationManager
                        .getInstance(context).getTypefacePath(ConfigurationConstants.BOLD_FONT));

                TextView description = (TextView) result.findViewById(R.id.logout_description);
                CalligraphyUtils.applyFontToTextView(context, description, ConfigurationManager
                        .getInstance(context).getTypefacePath(ConfigurationConstants.LIGHT_FONT));

                return result;
            }
        };
    }

    /**
     * Adds actions to the reject logout button of the view.
     *
     * @param context  The application context.
     * @param result   The view on which the reject button exists.
     * @param fragment The fragment to dismiss.
     */
    private void addActionsForDismissDialogButton(final Context context, View result,
                                                  final ReadDialogFragment fragment) {

        Button rejectLogout = (Button) result.findViewById(R.id.cancel_dialog);
        if (rejectLogout == null) {
            return;
        }
        // Font needs to be applied since this was created dynamically.
        CalligraphyUtils.applyFontToTextView(context, rejectLogout, ConfigurationManager
                .getInstance(context).getTypefacePath(ConfigurationConstants.REGULAR_FONT));

        rejectLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //Dismiss the fragment
                fragment.dismiss();
            }
        });
    }

    /**
     * Adds actions to the accept logout button of the view.
     *
     * @param context  The application context.
     * @param result   The view on which the reject button exists.
     * @param fragment The fragment ot dismiss.
     */
    private void addActionsForAcceptLogoutButton(final Context context, View result,
                                                 final ReadDialogFragment fragment) {

        Button acceptLogout = (Button) result.findViewById(R.id.accept_logout);
        if (acceptLogout == null) {
            return;
        }

        // Font needs to be applied since this was created dynamically.
        CalligraphyUtils.applyFontToTextView(context, acceptLogout, ConfigurationManager
                .getInstance(context).getTypefacePath(ConfigurationConstants.REGULAR_FONT));

        acceptLogout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                // Remove MVPD logo from preferences.
                Preferences.setString(PreferencesConstants.MVPD_LOGO_URL, "");

                // Hide MVPD logo because the user logged out.
                try {
                    ImageView poweredByLogoImage =
                            (ImageView) fragment.getActivity().findViewById(R.id.mvpd_logo);

                    poweredByLogoImage.setVisibility(View.INVISIBLE);
                }
                catch (Exception e) {
                    Log.e(TAG, "Couldn't hide the powered by layout!!!", e);
                }

                LocalBroadcastManager.getInstance(context)
                                     .sendBroadcast(new Intent().setAction
                                             (LOGOUT_BUTTON_BROADCAST_INTENT_ACTION));

                fragment.dismiss();
            }
        });
    }
}
