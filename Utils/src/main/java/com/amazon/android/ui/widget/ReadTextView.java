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

import com.amazon.utils.R;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.util.AttributeSet;
import android.widget.TextView;

import uk.co.chrisjenx.calligraphy.CalligraphyConfig;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * The default text view for the read dialog.
 * <p>
 * Fades the text at top and bottom to hint when scrolling is available and makes sure it fades
 * text, not just empty lines.
 */
public class ReadTextView extends TextView {

    /**
     * Defines the percentage of a page that the system will scroll when receive a Up/Down event
     * <p>
     * Set to 18% this was determined with inspection with the current size and font size to be
     * two lines of text.  A better solution would be to get the current height and inspect
     * the text metrics and calculate dynamically.
     */
    private static final float MAX_SCROLL_AMOUNT = 0.18f;

    private TextView mText;
    private ScrollViewPlus mScrollView;

    /**
     * {@inheritDoc}
     */
    public ReadTextView(final Context context) {

        super(context);
    }

    /**
     * {@inheritDoc}
     */
    public ReadTextView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
    }

    /**
     * {@inheritDoc}
     */
    public ReadTextView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        mText = (TextView) findViewById(R.id.txt);

        // Font needs to be applied since this was created dynamically.
        CalligraphyUtils.applyFontToTextView(getContext(), mText, CalligraphyConfig.get(), null);
        mText.setTextColor(ContextCompat.getColor(getContext(), R.color.primary_text));
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onAttachedToWindow() {

        super.onAttachedToWindow();

        if (mText != null && mText.getParent() != null && mText.getParent() instanceof
                ScrollViewPlus) {

            // For some reason setScrollbarFadingEnabled is not working,
            // hence adding very long duration before the scroll bar starts fading.
            mScrollView = (ScrollViewPlus) mText.getParent();
            mScrollView.setMaxScrollAmount(MAX_SCROLL_AMOUNT);
            mScrollView.setScrollBarFadeDuration(500000);
            mScrollView.setScrollBarStyle(SCROLLBARS_OUTSIDE_INSET);
            mScrollView.setPadding(14, 0, 14, 0);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onDraw(final Canvas canvas) {
        // Update the gradient each time the text is drawn.
        updateGradient();

        super.onDraw(canvas);
    }

    /**
     * Draw a gradient on the text so that the bottom and top line have a faded effect if the user
     * can scroll in that direction.
     */
    private void updateGradient() {

        if (mText == null || mScrollView == null) {
            return;
        }

        final Layout layout = mText.getLayout();
        assert layout != null;

        final Rect bounds = new Rect();
        getTextBounds(bounds);
        final int topLine = topVisibleLineOfText(layout, bounds);
        final int bottomLine = bottomVisibleLineOfText(layout, bounds);
        final int fullHeight = layout.getHeight();

        if (fullHeight > 0) {

            final float bottomLineHeight = layout.getLineBottom(bottomLine) - layout.getLineTop
                    (bottomLine);
            final float topLineHeight = layout.getLineBottom(topLine) - layout.getLineTop(topLine);
            this.setFadingEdgeLength((int) Math.max(topLineHeight, bottomLineHeight));
        }
    }


    /**
     * Which line in the layout is the top visible line.
     *
     * @param layout Calculated text layout.
     * @param bounds Visible bounds in the coordinates of the TextView and layout.
     * @return The top visible line.
     */
    private int topVisibleLineOfText(final Layout layout, final Rect bounds) {

        final int lineCount = layout.getLineCount();
        for (int lineNum = 0; lineNum < lineCount; lineNum++) {
            final int top = layout.getLineTop(lineNum);
            if (top >= bounds.top && top <= bounds.bottom) {
                return lineNum;
            }
        }
        return lineCount;
    }

    /**
     * Which line in the layout is the bottom visible line.
     *
     * @param layout Calculated text layout.
     * @param bounds Visible bounds in the coordinates of the TextView and layout.
     * @return The bottom visible line
     */
    private int bottomVisibleLineOfText(final Layout layout, final Rect bounds) {

        final int lineCount = layout.getLineCount();
        for (int lineNum = lineCount - 1; lineNum >= 0; lineNum--) {
            final int bottom = layout.getLineBottom(lineNum);
            if (bottom >= bounds.top && bottom <= bounds.bottom) {
                return lineNum;
            }
        }
        return 0;
    }

    /**
     * The out Rect will contain the visible area of the scrolled mText in mText coordinates
     *
     * @param outRect The outer rectangle.
     */
    private void getTextBounds(final Rect outRect) {

        mScrollView.getDrawingRect(outRect);
        mScrollView.offsetRectIntoDescendantCoords(mText, outRect);
    }
}
