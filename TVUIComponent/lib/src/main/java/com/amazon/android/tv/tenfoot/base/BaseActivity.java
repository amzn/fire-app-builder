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
package com.amazon.android.tv.tenfoot.base;

import com.amazon.android.adapters.ActionWidgetAdapter;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.Action;
import com.amazon.android.tv.tenfoot.R;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v17.leanback.widget.OnChildViewHolderSelectedListener;
import android.support.v7.widget.RecyclerView;
import android.util.Log;

import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

/**
 * BaseActivity class that handles common actions such as setting the font.
 */
public abstract class BaseActivity extends Activity {

    /**
     * Debug TAG.
     */
    private static final String TAG = BaseActivity.class.getSimpleName();

    // This is the currently selected action.
    private Action mSelectedAction;

    /**
     * Action widget adapter.
     */
    private ActionWidgetAdapter mActionWidgetAdapter;

    /**
     * {@inheritDoc}
     */
    @Override
    protected void attachBaseContext(Context newBase) {
        // This lets us get global font support.
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onSearchRequested() {

        Log.v(TAG, "onSearchRequested called.");
        ContentBrowser.getInstance(this)
                      .switchToScreen(ContentBrowser.CONTENT_SEARCH_SCREEN);
        return true;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {

        Log.v(TAG, "onActivityResult called with requestCode:" + requestCode +
                " resultCode:" + requestCode + " intent:" + data);
        super.onActivityResult(requestCode, resultCode, data);

        ContentBrowser.getInstance(this)
                      .handleOnActivityResult(this, requestCode, resultCode, data);
    }

    /**
     * This variable responds to items being selected in the view. It updates the selected action.
     */
    private final OnChildViewHolderSelectedListener mRowSelectedListener =
            new OnChildViewHolderSelectedListener() {
                @Override
                public void onChildViewHolderSelected(RecyclerView parent,
                                                      RecyclerView.ViewHolder view, int position,
                                                      int subposition) {

                    mSelectedAction = mActionWidgetAdapter.getAction(position);

                }
            };

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onStart() {

        super.onStart();

        // Get the Action widget container.
        HorizontalGridView actionWidgetContainer =
                (HorizontalGridView) findViewById(R.id.widget_grid_view);

        if (actionWidgetContainer != null) {

            // Create a new Action Widget Adapter
            mActionWidgetAdapter = new ActionWidgetAdapter(actionWidgetContainer);

            // Set adapter.
            actionWidgetContainer.setAdapter(mActionWidgetAdapter);

            // Add the actions to the widget adapter.
            mActionWidgetAdapter.addActions(ContentBrowser
                                                    .getInstance(this).getWidgetActionsList());

            // Set the selected listener for the child view of the selected listener.
            actionWidgetContainer.setOnChildViewHolderSelectedListener(mRowSelectedListener);

            // Set the on click listener for this widget container.
            actionWidgetContainer.setOnClickListener(
                    v -> ContentBrowser.getInstance(BaseActivity.this).actionTriggered
                            (BaseActivity.this, mSelectedAction));
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        setRestoreActivityValues();
    }

    /**
     * Use this method to set the
     * {@link com.amazon.android.ui.constants.PreferencesConstants#LAST_ACTIVITY}
     * and {@link com.amazon.android.ui.constants.PreferencesConstants#TIME_LAST_SAVED} values in
     * the {@link com.amazon.android.utils.Preferences} instance. This will allow the activity to be
     * restored when the app launches.
     */
    public abstract void setRestoreActivityValues();
}

