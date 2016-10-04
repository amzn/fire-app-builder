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


import com.amazon.android.ui.fragments.ReadDialogFragment;
import com.amazon.android.ui.interfaces.SingleViewProvider;
import com.amazon.utils.R;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.res.TypedArray;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.text.Layout;
import android.text.Spannable;
import android.text.SpannableString;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.style.DynamicDrawableSpan;
import android.text.style.ImageSpan;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.text.BreakIterator;

/**
 * This view is used to display text that will be too long to fit in the available space. The
 * text becomes focusable in the navigation and clicking on the text will open a dialog that
 * allows the user to read the full text.
 */
@SuppressLint("WrongCall")
//suppressing to remove suspicious method call warning for using 'onMeasure' instead of measure
public class EllipsizedTextView extends TextView {

    private static final String TAG = EllipsizedTextView.class.getSimpleName();
    private static final char ELLIPSIS = '\u2026';
    private String mSetText;
    private boolean mIsEllipsized = false;
    private StateImageSpan mEllipsisImage;
    private CharSequence mCharSequence;
    private int mGuillemetDrawableId;
    private int mReadDialogWidth;
    private int mReadDialogHeight;

    private SingleViewProvider mExpandedContentViewProvider;

    /**
     * {@inheritDoc}
     */
    public EllipsizedTextView(final Context context) {

        super(context);
        mGuillemetDrawableId = R.drawable.guillemet;
    }

    /**
     * {@inheritDoc}
     */
    public EllipsizedTextView(final Context context, final AttributeSet attrs) {

        super(context, attrs);
        init(attrs);
    }

    /**
     * {@inheritDoc}
     */
    public EllipsizedTextView(final Context context, final AttributeSet attrs, final int defStyle) {

        super(context, attrs, defStyle);
        init(attrs);
    }

    /**
     * Initializes the view's attributes.
     *
     * @param attrs The attribute set.
     */
    private void init(final AttributeSet attrs) {
        // Load the layout parameters from attrs.
        final TypedArray styledAttributes = getContext().obtainStyledAttributes(attrs, R
                .styleable.EllipsizedTextView);

        mGuillemetDrawableId =
                styledAttributes.getResourceId(R.styleable.EllipsizedTextView_guillemetDrawable,
                                               R.drawable.guillemet);

        mReadDialogWidth = (int) styledAttributes.getDimension(
                R.styleable.EllipsizedTextView_readDialogWidth, 0);

        mReadDialogHeight = (int) styledAttributes.getDimension(
                R.styleable.EllipsizedTextView_readDialogHeight, 0);

        styledAttributes.recycle();
    }

    /**
     * Creates the read dialog fragment and adds it to the fragment.
     */
    private void showReadDialog() {
        // Show the dialog
        final ReadDialogFragment dialog = new ReadDialogFragment();
        if (mExpandedContentViewProvider == null) {
            dialog.setContentViewProvider(getDefaultExpandedContentProvider(this));
        }
        else {
            dialog.setContentViewProvider(mExpandedContentViewProvider);
        }

        if (mReadDialogHeight != 0 && mReadDialogWidth != 0) {
            final Bundle args = new Bundle();
            args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_WIDTH, mReadDialogWidth);
            args.putInt(ReadDialogFragment.INTENT_EXTRA_DIALOG_HEIGHT, mReadDialogHeight);

            dialog.setArguments(args);
        }

        // Commit allowing state loss, in case our activity is being destroyed we won't be in an
        // illegal state.
        final FragmentTransaction ft = ((Activity) getContext()).getFragmentManager()
                                                                .beginTransaction();
        ft.disallowAddToBackStack();
        ft.add(dialog, "read text");
        ft.commitAllowingStateLoss();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onFinishInflate() {

        super.onFinishInflate();

        setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(final View v) {

                showReadDialog();
            }
        });

        // Force a state change so the ellipsis image is in the right state.
        drawableStateChanged();
    }

    /**
     * Default implementation of a SingleViewProvider.
     */
    private static SingleViewProvider getDefaultExpandedContentProvider(final EllipsizedTextView
                                                                                textView) {

        return new SingleViewProvider() {

            @Override
            public View getView(final Context context, final LayoutInflater inflater, final
            ViewGroup parent) {

                final View result = inflater.inflate(R.layout.read_dialog_default_layout, parent);
                final TextView mainText = (TextView) result.findViewById(R.id.txt);
                mainText.setText(textView.mSetText);
                return result;
            }
        };
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void onMeasure(final int widthMeasureSpec, final int heightMeasureSpec) {

        reflow(widthMeasureSpec, heightMeasureSpec);
    }

    /**
     * This function accomplishes two goals
     * <p>
     * # Measure all text using a staticLayout to determine where the text should be ellipsized
     * # Moves the ellipsis back one word to create more space for the image
     * # Replaces the ellipsis with a guillemet image that changes on focus
     * # Measures/Renders only the text visible
     * <p>
     * This function is slow and should only be called when needed
     * <p>
     * There is a bug in android that doesn't allow ellipsis to appear in multiline text fields
     * when the text is specified by Spannable.
     * <p>
     * To get around this we:
     * # Force this to use just text
     * # Layout twice once with just text, the second time with a Spannable with an image for an
     * ellipse
     */
    private void reflow(final int widthMeasureSpec, final int heightMeasureSpec) {
        // First layout
        super.setText(mCharSequence, BufferType.SPANNABLE);
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        final Layout layout = getLayout();
        if (layout == null) {
            // This will occur if the text is empty
            return;
        }

        final int lastLine = layout.getLineForOffset(mSetText.length());
        final int maxLines = getMaxLines();

        // Check if text should have an ellipsis.
        if (lastLine >= maxLines) {
            setEllipsis(widthMeasureSpec, heightMeasureSpec, layout, lastLine, maxLines);
        }
        else {
            // Reapply spans.
            if (mCharSequence instanceof Spannable) {
                final Spannable span = (Spannable) mCharSequence;
                super.setText(span, BufferType.SPANNABLE);
                super.onMeasure(widthMeasureSpec, heightMeasureSpec);
            }
            // No ellipsis, make this not focusable.
            removeEllipsisProperties();
        }
    }

    /**
     * Removes ellipsis properties.
     */
    private void removeEllipsisProperties() {

        mIsEllipsized = false;
        setFocusable(false);
        setClickable(false);
        mEllipsisImage = null;
    }

    /**
     * Sets ellipsis properties.
     *
     * @param widthMeasureSpec  Ellipsis width.
     * @param heightMeasureSpec Ellipsis height.
     * @param layout            Layout for ellipsis.
     * @param lastLine          Last line length for ellipsis.
     * @param maxLines          Max lines in ellipsis.
     */
    private void setEllipsis(int widthMeasureSpec, int heightMeasureSpec, Layout layout,
                             int lastLine, int maxLines) {

        mIsEllipsized = true;
        setFocusable(true);
        setClickable(true);
        final SpannableString ss = new SpannableString(mCharSequence);
        String visibleText = mCharSequence.toString();


        mEllipsisImage = new StateImageSpan(
                getContext(),
                mGuillemetDrawableId,
                ImageSpan.ALIGN_BASELINE);


        final SpannableStringBuilder spannedText = new SpannableStringBuilder();
        int ellipsisIndex = layout.getLineStart(Math.min(lastLine + 1, maxLines));

        // Keep chopping words off until the ellipsis is on a visible line or there is only one
        // line left.
        do {
            // Only truncate the last line for long description.
            if (lastLine >= maxLines) {
                // Getting the first word break index before the last index of maxline.
                int safeBreakIndex = breakBefore(visibleText, ellipsisIndex, BreakIterator
                        .getWordInstance());
                final int maxLineStart = layout.getLineStart(maxLines - 1);

                // If this check pass, it means we just truncated a word that is longer than a line.
                if (safeBreakIndex < maxLineStart) {
                    // Need to check character by character and break in the middle now. Checking
                    // word by word should cover most cases, only do this if a word is longer than
                    // line width.
                    safeBreakIndex = breakBefore(visibleText, ellipsisIndex,
                                                 BreakIterator.getCharacterInstance());
                }
                ellipsisIndex = safeBreakIndex;
            }

            visibleText = visibleText.substring(0, ellipsisIndex);
            final CharSequence charOutput = ss.subSequence(0, ellipsisIndex);

            // Re-add ellipsis and convert to image
            spannedText.replace(0, spannedText.length(), charOutput);
            spannedText.append(ELLIPSIS);

            spannedText.setSpan(mEllipsisImage, ellipsisIndex, ellipsisIndex + 1,
                                Spanned.SPAN_INCLUSIVE_INCLUSIVE);


            // Reset text and re-measure.
            super.setText(spannedText, BufferType.SPANNABLE);
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);

        } while (getLineCount() > getMaxLines() && getLineCount() > 1);
        requestFocus();
    }

    /**
     * Find the first word/character break index before ellipsisIndex.
     */
    private int breakBefore(final String displayText, final int ellipsisIndex,
                            final BreakIterator iterator) {

        iterator.setText(displayText);
        return iterator.preceding(ellipsisIndex);
    }


    /**
     * Sets the text please note that text must be a normal CharSequence.
     * The text will be truncated if goes beyond max_characters_allowed characters
     */
    @Override
    public void setText(final CharSequence text, final BufferType type) {

        int maxCharactersAllowed = getContext()
                .getResources().getInteger(R.integer.ellipsized_text_view_max_characters);

        if (text.length() > maxCharactersAllowed) {
            mCharSequence = text.subSequence(0, maxCharactersAllowed);
        }
        else {
            mCharSequence = text;
        }
        mSetText = mCharSequence.toString();
        super.setText(mCharSequence, type);
    }

    @Override
    protected void drawableStateChanged() {

        super.drawableStateChanged();

        // Notify the ellipsis of the state change so it can redraw.
        if (mEllipsisImage != null) {
            final int[] state = getDrawableState();
            mEllipsisImage.setState(state);
            invalidate();
        }
    }

    /**
     * This class is used to draw the guillemet at the end of the text instead of the ellipsis.
     * <p>
     * This code is a near duplicate of ImageSpan with the addition of a function setState that
     * sets the guillemet into the focus/unfocused state and lots of unneeded code to handle other
     * constructors removed.
     * <p>
     * Currently there enough edge cases that I am not inclined to make this a public class (i.e.
     * how the system will respond if the states of the images are different sizes).
     */
    private static class StateImageSpan extends DynamicDrawableSpan {

        private final StateListDrawable mStateDrawable;

        public StateImageSpan(final Context context, final int resourceId, final int
                verticalAlignment) {

            super(verticalAlignment);
            mStateDrawable = (StateListDrawable) ContextCompat.getDrawable(context, resourceId);
            if (mStateDrawable != null) {
                mStateDrawable.setBounds(0, 0, mStateDrawable.getIntrinsicWidth(), mStateDrawable
                        .getIntrinsicHeight());
                mStateDrawable.setState(FOCUSED_STATE_SET);
            }
        }

        public void setState(final int[] state) {

            if (mStateDrawable != null) {
                mStateDrawable.setState(state);
            }
        }

        @Override
        public Drawable getDrawable() {

            return mStateDrawable;
        }
    }
}
