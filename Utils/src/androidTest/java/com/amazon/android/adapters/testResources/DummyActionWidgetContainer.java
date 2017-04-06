/**
 * This file was modified by Amazon:
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
/*
 * Copyright (C) 2016 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
package com.amazon.android.adapters.testResources;

import android.support.annotation.VisibleForTesting;
import android.support.v17.leanback.widget.HorizontalGridView;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;

import com.amazon.utils.R;

import com.amazon.android.adapters.ActionWidgetAdapter;


/**
 * This class is used in testing the {@link ActionWidgetAdapter}.
 */
public class DummyActionWidgetContainer extends AppCompatActivity {

    @VisibleForTesting
    public ActionWidgetAdapter mActionWidgetAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        // Create the view and inflate it.
        View view = getLayoutInflater().inflate(R.layout.action_widget_container, null, false);

        // Find the Horizontal Grid View.
        HorizontalGridView mHorizontalGridView = (HorizontalGridView) view.findViewById(
                R.id.widget_grid_view);

        // Create a new Action Widget Adapter.
        mActionWidgetAdapter = new ActionWidgetAdapter(mHorizontalGridView);

        // Set the view of this activity.
        setContentView(view);

    }
}