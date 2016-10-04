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
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;

/**
 * Alert dialog fragment class.
 */
public class AlertDialogFragment extends DialogFragment {

    /**
     * Fragment TAG.
     */
    private static final String FRAGMENT_TAG_NAME = "alert_dialog_fragment";

    /**
     * Alert dialog listener.
     */
    public interface IAlertDialogListener {

        /**
         * On dialog positive button method.
         *
         * @param alertDialogFragment The alert dialog fragment to listen on.
         */
        void onDialogPositiveButton(AlertDialogFragment alertDialogFragment);

        /**
         * On dialog negative button method.
         *
         * @param alertDialogFragment The alert dialog fragment to listen on.
         */
        void onDialogNegativeButton(AlertDialogFragment alertDialogFragment);
    }

    /**
     * Alert dialog title.
     */
    private String mTitle;

    /**
     * Alert dialog message.
     */
    private String mMessage;

    /**
     * Alert dialog positive button text.
     */
    private String mOkString;

    /**
     * Alert dialog negative button text.
     */
    private String mCancelString;

    /**
     * Alert dialog listener reference.
     */
    private IAlertDialogListener mIAlertDialogListener;

    /**
     * Create and show alert dialog fragment.
     *
     * @param activity             Activity.
     * @param title                Alert dialog title.
     * @param message              Alert dialog message.
     * @param okString             Alert dialog positive button text.
     * @param cancelString         Alert dialog negative button text.
     * @param iAlertDialogListener Alert dialog listener reference.
     */
    public static void createAndShowAlertDialogFragment(Activity activity,
                                                        String title,
                                                        String message,
                                                        String okString,
                                                        String cancelString,
                                                        IAlertDialogListener iAlertDialogListener) {

        AlertDialogFragment alertDialogFragment = new AlertDialogFragment(title,
                                                                          message,
                                                                          okString,
                                                                          cancelString,
                                                                          iAlertDialogListener);

        FragmentManager fragmentManager = activity.getFragmentManager();
        alertDialogFragment.setCancelable(false);
        alertDialogFragment.show(fragmentManager, FRAGMENT_TAG_NAME);
    }

    /**
     * Default constructor.
     */
    public AlertDialogFragment() {

    }

    /**
     * Constructor.
     *
     * @param title                Alert dialog title.
     * @param message              Alert dialog message.
     * @param okString             Alert dialog positive button text.
     * @param cancelString         Alert dialog negative button text.
     * @param iAlertDialogListener Alert dialog listener reference.
     */
    public AlertDialogFragment(String title,
                               String message,
                               String okString,
                               String cancelString,
                               IAlertDialogListener iAlertDialogListener) {

        mTitle = title;
        mMessage = message;
        mOkString = okString;
        mCancelString = cancelString;
        mIAlertDialogListener = iAlertDialogListener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(mTitle);
        builder.setMessage(mMessage);

        if (mOkString != null) {
            builder.setPositiveButton(mOkString,
                                      (dialogInterface, i) ->
                                              mIAlertDialogListener.onDialogPositiveButton(this)
            );
        }

        if (mCancelString != null) {
            builder.setNegativeButton(mCancelString,
                                      (dialogInterface, i) ->
                                              mIAlertDialogListener.onDialogNegativeButton(this)
            );
        }

        return builder.create();
    }
}
