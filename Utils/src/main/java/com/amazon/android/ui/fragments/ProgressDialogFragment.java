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

import com.amazon.android.model.event.ProgressOverlayDismissEvent;
import com.amazon.utils.R;

import org.greenrobot.eventbus.EventBus;
import org.greenrobot.eventbus.Subscribe;

import android.app.Activity;
import android.app.DialogFragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

/**
 * Progress dialog fragment class.
 */
public class ProgressDialogFragment extends DialogFragment {

    /**
     * Debug TAG.
     */
    private static final String TAG = ProgressDialogFragment.class.getName();

    /**
     * Fragment TAG.
     */
    private static final String FRAGMENT_TAG_NAME = "progress_dialog_fragment";

    /**
     * Progress text.
     */
    private String mText;

    /**
     * Create and show a progress dialog fragment.
     *
     * @param activity Activity.
     * @param text     Progress text to be shown on screen.
     */
    public static void createAndShow(Activity activity, String text) {

        Log.d(TAG, "createAndShow called with:" + text);

        FragmentManager fragmentManager = activity.getFragmentManager();
        ProgressDialogFragment progressFragment = new ProgressDialogFragment();
        progressFragment.setCancelable(false);
        progressFragment.setText(text);
        progressFragment.show(fragmentManager, FRAGMENT_TAG_NAME);
    }

    /**
     * Set progress text.
     *
     * @param text Progress text.
     */
    public void setText(String text) {

        mText = text;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment.
        View view = inflater.inflate(R.layout.progress_dialog_fragment, container, false);
        TextView progressText = (TextView) view.findViewById(R.id.progress_text);
        progressText.setText(mText);
        return view;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        // Set style to get full screen, no title and custom transparent dialog.
        setStyle(DialogFragment.STYLE_NO_TITLE, R.style.progress_dialog);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStart() {

        super.onStart();
        EventBus.getDefault().register(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {

        EventBus.getDefault().unregister(this);
        super.onStop();
    }

    /**
     * Event bus event listener method to detect dismiss broadcast.
     *
     * @param progressOverlayDismissEvent Broadcast event for progress overlay dismiss.
     */
    @Subscribe
    public void onProgressOverlayDismissEvent(ProgressOverlayDismissEvent
                                                      progressOverlayDismissEvent) {

        this.dismiss();
    }
}
