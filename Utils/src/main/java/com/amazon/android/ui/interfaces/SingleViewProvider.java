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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Interface to provide a single view for a given ViewGroup parent.
 */
public interface SingleViewProvider {

    /**
     * Returns a single view.
     *
     * @param context  The context
     * @param inflater The view Inflater
     * @param parent   The parent ViewGroup
     * @return A view to be used.
     */
    View getView(final Context context, final LayoutInflater inflater, final ViewGroup parent);
}
