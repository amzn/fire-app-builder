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
 * Copyright (C) 2014 The Android Open Source Project
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
package android.support.v17.leanback.widget;

import android.content.Context;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.View;

/**
 * Extension of {@link ControlBar} to to change the default highlighted button.
 */
public class TenFootControlBar extends ControlBar {

    private int mChildMarginFromCenter;
    private OnChildFocusedListener mOnChildFocusedListener;

    /**
     * Constructor
     * {@inheritDoc}
     */
    public TenFootControlBar(Context context, AttributeSet attrs) {

        super(context, attrs);
    }

    /**
     * Constructor
     * {@inheritDoc}
     */
    public TenFootControlBar(Context context, AttributeSet attrs, int defStyle) {

        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean requestFocus(int direction, Rect previouslyFocusedRect) {

        if (getChildCount() > 0) {
            if (getChildAt((getChildCount() - 1) / 2).requestFocus(direction,
                                                                   previouslyFocusedRect)) {
                return true;
            }
        }
        return super.requestFocus(direction, previouslyFocusedRect);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setOnChildFocusedListener(OnChildFocusedListener listener) {

        mOnChildFocusedListener = listener;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void setChildMarginFromCenter(int marginFromCenter) {

        mChildMarginFromCenter = marginFromCenter;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void requestChildFocus(View child, View focused) {

        super.requestChildFocus(child, focused);
        if (mOnChildFocusedListener != null) {
            mOnChildFocusedListener.onChildFocusedListener(child, focused);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {

        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        if (mChildMarginFromCenter <= 0) {
            return;
        }

        int totalExtraMargin = 0;
        for (int i = 0; i < getChildCount() - 1; i++) {
            View first = getChildAt(i);
            View second = getChildAt(i + 1);
            int measuredWidth = first.getMeasuredWidth() + second.getMeasuredWidth();
            int marginStart = mChildMarginFromCenter - measuredWidth / 2;
            LayoutParams lp = (LayoutParams) second.getLayoutParams();
            int extraMargin = marginStart - lp.getMarginStart();
            lp.setMarginStart(marginStart);
            second.setLayoutParams(lp);
            totalExtraMargin += extraMargin;
        }
        setMeasuredDimension(getMeasuredWidth() + totalExtraMargin, getMeasuredHeight());
    }
}
