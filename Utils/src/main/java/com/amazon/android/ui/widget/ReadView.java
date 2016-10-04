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
package com.amazon.android.ui.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.RelativeLayout;

/**
 * Renders the actual reading view.
 * <p>
 * Handles the IX for scrolling of the text.
 * Shows hint arrows if the user can scroll.
 * Shows orange arrows when the user is scrolling.
 * Fades the text at top and bottom to hint when scrolling is available.
 */
public class ReadView extends RelativeLayout {

    /**
     * Constructor.
     *
     * @param context The context.
     * @return The ReadView.
     */
    public ReadView(final Context context) {

        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public ReadView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    public ReadView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();
    }
}
