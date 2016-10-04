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

import com.amazon.android.ui.interfaces.SingleViewProvider;
import com.amazon.utils.R;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

/**
 * Dialog allows user to read text,
 *
 * Used by ellipsized text to display text that may be too long to fit in the space allotted.
 */
public class ReadDialogFragment extends DialogFragment {

    private View mView;

    public static final String INTENT_EXTRA_DIALOG_WIDTH = "DIALOG_WIDTH";
    public static final String INTENT_EXTRA_DIALOG_HEIGHT = "DIALOG_HEIGHT";

    private SingleViewProvider mContentViewProvider;

    private ViewGroup mContentViewContainer;
    private int mDialogLayout = 0;

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(final LayoutInflater inflater, final ViewGroup container, final
    Bundle savedInstanceState) {
        // Using the default layout read_dialog if custom layout is not set.
        int read_dialog = mDialogLayout == 0 ? R.layout.read_dialog : mDialogLayout;
        mView = inflater.inflate(read_dialog, container);

        return mView;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onPause() {
        // This makes sure, if we leave the 1-D list by pressing Home or some other way,
        // it always dismisses the fragment.
        dismissAllowingStateLoss();
        super.onPause();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDismiss(final DialogInterface dialog) {

        super.onDismiss(dialog);

        // This might be superfluous, but we don't want to be hanging onto anything that our
        // provider might be attached to.
        mContentViewProvider = null;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onViewCreated(final View view, final Bundle savedInstanceState) {

        super.onViewCreated(view, savedInstanceState);

        mContentViewContainer = (ViewGroup) mView.findViewById(R.id.view_container);

        showContent();
    }

    /**
     * Set the provider of the views for the read dialog.
     *
     * @param viewProvider The view provider for the dialog.
     */
    public void setContentViewProvider(final SingleViewProvider viewProvider) {
        // Only update if different as swapping out the view might be heavy handed.
        if (mContentViewProvider != viewProvider) {
            mContentViewProvider = viewProvider;
            showContent();
        }
    }

    /**
     * Method to override the default layout
     *
     * @param dialogLayout layout Id to attach to the dialog
     */
    public void setDialogLayout(final int dialogLayout) {

        this.mDialogLayout = dialogLayout;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        final Dialog dialog = builder.show();

        // Move the window to the top of the screen.
        final Window window = dialog.getWindow();
        final WindowManager.LayoutParams wlp = window.getAttributes();
        wlp.gravity = Gravity.CENTER;
        // Manually set the width, something deep in android is overriding the layout
        // width to 55% of screen width, unless we specify here.
        final Bundle extras = getArguments();
        if (extras != null) {
            wlp.width = extras.getInt(INTENT_EXTRA_DIALOG_WIDTH, window.getContext().getResources
                    ().getDimensionPixelSize(R.dimen.read_dialog_width));
            wlp.height = extras.getInt(INTENT_EXTRA_DIALOG_HEIGHT,
                                       window.getContext()
                                             .getResources()
                                             .getDimensionPixelSize(R.dimen.read_dialog_height));
        }
        else {
            wlp.width = window.getContext().getResources()
                              .getDimensionPixelSize(R.dimen.read_dialog_width);
            wlp.height = window.getContext().getResources()
                               .getDimensionPixelSize(R.dimen.read_dialog_height);
        }
        window.setAttributes(wlp);

        return dialog;
    }

    /**
     * shows content on the content view container
     */
    private void showContent() {

        if (mContentViewContainer != null) {
            mContentViewContainer.removeAllViews();

            if (mContentViewProvider != null) {
                // Due to a WebView bug in AOSP, inflating a WebView with activity context would
                // cause an activity memory leak even after activity is finished, the activity will
                // never get GCed.
                // See: http://stackoverflow.com/questions/5299693/webview-memory-leak
                final Context context = getActivity().getApplicationContext();
                final View contentView = mContentViewProvider.getView(context,
                                                                      LayoutInflater.from(context),
                                                                      mContentViewContainer);
                if (contentView != null && contentView.getParent() == null) {
                    mContentViewContainer.addView(contentView);
                }
            }
        }
    }
}
