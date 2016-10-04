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
import android.view.View;
import android.widget.ScrollView;

/**
 * Slight improvement to android.widget.ScrollView.
 * <p>
 * This exposes several features that may be useful canScroll setOnScrollListener.
 */
public class ScrollViewPlus extends ScrollView {

    private OnScrollListener mListener;
    private boolean mFirstLayout = true;
    private float mMaxScrollAmount = 0.0f;

    @SuppressWarnings("UnusedDeclaration")
    public ScrollViewPlus(final Context context) {

        super(context);
    }

    @SuppressWarnings("UnusedDeclaration")
    public ScrollViewPlus(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    public ScrollViewPlus(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    /**
     * Sets the on scroll listener.
     *
     * @param listener Attach a listener to receive events when the scroll
     *                 view scrolls.
     */
    public void setOnScrollListener(final OnScrollListener listener) {

        mListener = listener;

        // Trigger a layout so the item gets an initial call to onScroll.
        // If the listener is set on initialization a layout is already
        // required and this has no performance impact.
        mFirstLayout = true;
        requestLayout();
    }

    /**
     * Returns true this ScrollView can be scrolled.
     * This code is copied from a private method in ScrollView.
     */
    @SuppressWarnings("UnusedDeclaration")
    public boolean canScroll() {

        final View child = getChildAt(0);
        if (child != null) {
            final int childHeight = child.getHeight();
            return getHeight() < childHeight + getPaddingTop() + getPaddingBottom();
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onLayout(final boolean changed, final int l, final int t, final int r, final
    int b) {

        super.onLayout(changed, l, t, r, b);

        // Once the children are measured and we know their heights update the listener so it can
        // know if the bottom is visible.
        if (mFirstLayout && mListener != null && this.getChildCount() > 0 && getChildAt(0)
                .getHeight() > 0) {
            final int scrollHeight = getHeight();
            //noinspection ConstantConditions checked in if statement
            final int viewHeight = getChildAt(0).getHeight();
            final int currentY = getScrollY();
            mListener.onScroll(this, 0, currentY, currentY <= 0, viewHeight <= scrollHeight);
            mFirstLayout = false;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onScrollChanged(final int l, final int t, final int oldl, final int oldt) {

        super.onScrollChanged(l, t, oldl, oldt);

        if (mListener != null) {
            final int deltaY = t - oldt;
            final int range = getScrollRange();
            if (range > 0) {
                final float viewPercent = (float) t / (float) range;
                final boolean topVisible = t <= 0;
                final boolean bottomVisible = t >= getScrollRange();
                mListener.onScroll(this, deltaY, viewPercent, topVisible, bottomVisible);
            }
        }
    }

    /**
     * Returns the top value that represents a 100% scrolled view.
     * Note that the top may go past this in overscroll.
     * <p>
     * This code is copied from a private method in ScrollView.
     */
    private int getScrollRange() {

        int scrollRange = 0;
        if (getChildCount() > 0) {
            final View child = getChildAt(0);
            //noinspection ConstantConditions checked in if statement
            scrollRange = Math.max(0,
                                   child.getHeight() - (getHeight() - getPaddingBottom() -
                                           getPaddingTop()));
        }
        return scrollRange;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int getMaxScrollAmount() {

        if (mMaxScrollAmount <= 0.0f) {
            return super.getMaxScrollAmount();
        }
        // {@see android.widget.ScrollView@getMaxScrollAmount}
        // for formula, this allows the screen to scroll only mMaxScrollAmount
        return (int) (mMaxScrollAmount * getHeight());
    }

    /**
     * Interface for listening when the scrolling position of a ScrollView is changed.
     *
     * @param value Number of pages to scroll by when navigating, 0.0 or less will use default
     *              behavior. Valid ranges are (0-1] and represent the percentage of a 'page' that
     *              will scroll.
     */
    public void setMaxScrollAmount(float value) {

        mMaxScrollAmount = Math.max(0.0f, Math.min(1.0f, value));
    }

    /**
     * Interface for listening when the scrolling position of a ScrollView is changed.
     */
    public interface OnScrollListener {

        /**
         * Called the the scrolling position is changed.
         * <p>
         * If animation is enabled on the ScrollView this will be called several
         * during any animation.
         *
         * @param scroller      Reference to the scroller being scrolled.
         * @param deltaY        Change in y position during this scroll, positive is a scroll down
         *                      revealing more content at the bottom of the view.
         * @param scrollPercent Simple percentage of the amount the view has has scrolled. 0
         *                      indicates the top of the inner view is at the top of the scroll
         *                      view.
         *                      1 represents the bottom of the inner view is along the bottom
         *                      of the ScrollView.  NOTE- this value CAN be less than 0 and more
         *                      than 1 when overscrolling is allowed.
         * @param topVisible    Is the top of the innerView visible?
         * @param bottomVisible Is the bottom of the innerView visible?
         */
        void onScroll(ScrollViewPlus scroller, int deltaY, float scrollPercent,
                      boolean topVisible, boolean bottomVisible);
    }
}
