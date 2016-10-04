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
import com.amazon.android.ui.constants.ConfigurationConstants;
import com.amazon.android.utils.ErrorUtils;
import com.amazon.android.utils.Helpers;
import com.amazon.utils.R;

import android.app.DialogFragment;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Dialog fragment that handles all the exception handling related UI.
 */
public class ErrorDialogFragment extends DialogFragment {

    private static final String TAG = ErrorDialogFragment.class.getName();
    public static final String FRAGMENT_TAG_NAME = "error_dialog_fragment";

    private static final String ARG_ACTION_LABELS = "actionLabels";
    public static final String ARG_ERROR_MESSAGE = "errorMessage";
    public static final String ARG_ERROR_CATEGORY = "errorCategory";
    private List<Button> mButtonsList;
    private ErrorDialogFragmentListener mListener;
    private boolean mIsPaused;

    /**
     * Interface to implement for exception related actions.
     */
    public interface ErrorDialogFragmentListener {

        /**
         * Callback method to define the button behaviour for this activity.
         *
         * @param errorButtonType     The display text on the button
         * @param errorCategory       The error category determined by the client.
         * @param errorDialogFragment The fragment listener.
         */
        void doButtonClick(ErrorDialogFragment errorDialogFragment, ErrorUtils.ERROR_BUTTON_TYPE
                errorButtonType, ErrorUtils.ERROR_CATEGORY errorCategory);
    }

    public ErrorDialogFragment() {
        // Empty constructor is required for DialogFragment.
    }

    /**
     * Creates a new instance of the dialog fragment.
     *
     * @param context                     The relevant context.
     * @param errorCategory               To help determine the error specific parameters.
     * @param errorDialogFragmentListener The fragment listener.
     * @return The ErrorDialogFragment instance.
     */
    public static ErrorDialogFragment newInstance(Context context,
                                                  ErrorUtils.ERROR_CATEGORY errorCategory,
                                                  ErrorDialogFragmentListener
                                                          errorDialogFragmentListener) {

        ErrorDialogFragment errorDialogFragment = new ErrorDialogFragment();
        errorDialogFragment.mListener = errorDialogFragmentListener;
        errorDialogFragment.setStyle(DialogFragment.STYLE_NO_TITLE, R.style.error_dialog);
        // Setting it as non-cancelable prevents back button press issues.
        errorDialogFragment.setCancelable(false);
        Bundle args = new Bundle();
        // Set the error category.
        args.putSerializable(ARG_ERROR_CATEGORY, errorCategory);
        // Get the error message.
        args.putString(ARG_ERROR_MESSAGE, ErrorUtils.getErrorMessage(context, errorCategory));
        // Get the button details.
        args.putStringArrayList(ARG_ACTION_LABELS, (ArrayList<String>) ErrorUtils
                .getButtonLabelsList(context, errorCategory));
        // Get the button behavior.
        errorDialogFragment.setArguments(args);
        return errorDialogFragment;
    }

    /**
     * {@inheritDoc}
     *
     * @return The view created.
     */
    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        return inflater.inflate(R.layout.error_fragment, container);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(View view, Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);
        // Set the error message.
        setMessage(view, getArguments().getString(ARG_ERROR_MESSAGE));
        // Set custom dialog actions.
        setDialogActions(view, getArguments().getStringArrayList(ARG_ACTION_LABELS));
    }

    /**
     * Set the message to display on the dialog-fragment.
     *
     * @param view    The container for the message.
     * @param message The text to be displayed.
     */
    public void setMessage(View view, String message) {

        if (view != null) {
            ((TextView) view.findViewById(R.id.message)).setText(message);
        }

    }

    /**
     * Set the dialog actions to display on the dialog-fragment
     *
     * @param view         Container for the actions.
     * @param actionLabels Text to be displayed on the action buttons.
     */
    private void setDialogActions(View view, List<String> actionLabels) {

        LinearLayout errorButtonRow = (LinearLayout) view.findViewById(R.id.error_button_row);
        // Add & configure action buttons to the error row.
        createAndConfigureActionButtons(actionLabels, errorButtonRow);
    }

    private UIUpdateListener mUIUpdateListener;

    /**
     * Sets the {@link #mUIUpdateListener}.
     *
     * @param listener Value for {@link #mUIUpdateListener}.
     */
    public void setUIUpdateListener(UIUpdateListener listener) {

        mUIUpdateListener = listener;
    }

    /**
     * Interface for UI update listeners.
     */
    public interface UIUpdateListener {

        void onResumeCalled();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {
        // Get existing layout params for the window.
        ViewGroup.LayoutParams params = getDialog().getWindow().getAttributes();
        // Assign window properties to fill the parent.
        params.width = WindowManager.LayoutParams.MATCH_PARENT;
        params.height = WindowManager.LayoutParams.MATCH_PARENT;
        getDialog().getWindow().setAttributes((android.view.WindowManager.LayoutParams) params);

        // Call super onResume after sizing.
        super.onResume();

        // Requesting focus to the first button within the error button row.
        if (mButtonsList != null && mButtonsList.get(0) != null) {
            mButtonsList.get(0).requestFocus();
        }

        // Given that we had a network issue that was resolved by connecting to a network and
        // assuming that we are now back at the dialog fragment on pressing back from the wifi
        // settings card, dismiss the dialog
        if (isPaused() && getArguments().get(ARG_ERROR_CATEGORY) != null &&
                getArguments().get(ARG_ERROR_CATEGORY)
                              .equals(ErrorUtils.ERROR_CATEGORY.NETWORK_ERROR)
                && Helpers.isConnectedToNetwork(getActivity())) {
            this.dismiss();
            setIsPaused(false);
            Log.i(TAG, "Dismissing the error dialog fragment since network connection is now " +
                    "detected");
        }

        if (mUIUpdateListener != null) {
            mUIUpdateListener.onResumeCalled();
        }
    }

    @Override
    public void onPause() {

        super.onPause();
        setIsPaused(true);
    }

    /**
     * Generate the buttons dynamically.
     *
     * @param actionLabels   The list containing the text to be displayed on the action buttons.
     * @param errorButtonRow The view group containing the buttons.
     */
    private void createAndConfigureActionButtons(List<String> actionLabels, ViewGroup
            errorButtonRow) {

        Button button;

        int buttonPosition = 0;
        for (String actionLabel : actionLabels) {
            if (mButtonsList == null) {
                mButtonsList = new ArrayList<>();
            }
            // Create the button with the correct text.
            button = createActionButton(actionLabel);
            // Add the button to the layout.
            errorButtonRow.addView(button);
            // Set the left focus for the buttons.
            setLeftFocus(button, mButtonsList, buttonPosition);
            if (buttonPosition == 0) {
                button.requestFocus();
            }
            buttonPosition++;
            // Add the button to the button list.
            mButtonsList.add(button);
        }
        // Set the right focus for the buttons.
        setRightFocus(mButtonsList);
    }

    /**
     * Set the right focus for the buttons as they are generated.
     *
     * @param buttonsList The list of action buttons.
     */
    private void setRightFocus(List<Button> buttonsList) {

        Button button;
        for (int j = 0; j < (buttonsList.size() - 1); j++) {
            button = buttonsList.get(j);
            button.setNextFocusRightId(buttonsList.get(j + 1).getId());
        }
    }

    /**
     * Set the left focus for the buttons as they are generated.
     *
     * @param button      The button on which we want to configure the left focus.
     * @param buttonsList The list of action buttons.
     * @param i           The location flag.
     */
    private void setLeftFocus(Button button, List<Button> buttonsList, int i) {

        if (i == 0) {
            button.requestFocus();
            button.setNextFocusLeftId(button.getId());
        }
        else {
            button.setNextFocusLeftId(buttonsList.get(buttonsList.size() - 1).getId());
        }
    }

    /**
     * Creates the action button.
     *
     * @param actionLabel The text to be displayed on the action button.
     * @return The instantiated button.
     */
    private Button createActionButton(String actionLabel) {

        Button button = new Button(getActivity());
        button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable
                .action_button_background));
        button.setText(actionLabel);
        CalligraphyUtils.applyFontToTextView(getActivity(), button, ConfigurationManager
                .getInstance(getActivity()).getTypefacePath(ConfigurationConstants.REGULAR_FONT));

        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT,
                                              LinearLayout.LayoutParams.WRAP_CONTENT);
        int margin = (int) getResources().getDimension(R.dimen.error_button_side_margin);
        params.setMargins(margin, 0, margin, 0);
        button.setLayoutParams(params);

        button.setTextColor(ContextCompat
                                    .getColorStateList(getActivity(), R.color
                                            .action_button_text_color_selector));
        button.setFocusable(true);
        button.setFocusableInTouchMode(true);
        button.setNextFocusDownId(button.getId());
        button.setNextFocusUpId(button.getId());

        button.setBackground(ContextCompat.getDrawable(getActivity(), R.drawable
                .action_button_background));

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                mListener.doButtonClick(ErrorDialogFragment.this,
                                        ErrorUtils.getErrorButtonType(
                                                ErrorDialogFragment.this.getActivity(),
                                                (((TextView) v).getText()).toString()),
                                        (ErrorUtils.ERROR_CATEGORY) getArguments().get
                                                (ARG_ERROR_CATEGORY));
            }
        });

        return button;
    }

    /**
     * Determine if we're in a paused state.
     *
     * @return True if paused; false otherwise.
     */
    private boolean isPaused() {

        return mIsPaused;
    }

    /**
     * Set the pause state.
     *
     * @param isPaused True if paused; false otherwise.
     */
    private void setIsPaused(boolean isPaused) {

        this.mIsPaused = isPaused;
    }
}
