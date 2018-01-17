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
package android.support.v17.leanback.app;


import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.ui.constants.ConfigurationConstants;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.animation.TimeInterpolator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v17.leanback.animation.LogAccelerateInterpolator;
import android.support.v17.leanback.animation.LogDecelerateInterpolator;
import android.support.v17.leanback.widget.ItemBridgeAdapter;
import android.support.v17.leanback.widget.ObjectAdapter;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.VerticalGridView;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.InputEvent;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AccelerateInterpolator;
import android.widget.TextView;

import java.util.ArrayList;

import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * Playback fragment modified according to TenFootUI UX requirements.
 */
public class TenFootPlaybackOverlayFragment extends DetailsFragment {

    /**
     * No background.
     */
    private static final int BG_NONE = 0;

    /**
     * A dark translucent background.
     */
    private static final int BG_DARK = 1;

    /**
     * A light translucent background.
     */
    protected static final int BG_LIGHT = 2;
    private static final float DISABLED_BUTTON_ALPHA_VALUE = 0.3F;
    private static final float ENABLED_BUTTON_ALPHA_VALUE = 1.0F;
    private boolean mOtherRowDisplayed = false;

    /**
     * Listener allowing the application to receive notification of fade in and/or fade out
     * completion events.
     */
    public static class OnFadeCompleteListener {

        public void onFadeInComplete() {

        }

        public void onFadeOutComplete() {

        }
    }

    /**
     * Interface allowing the application to handle input events.
     */
    public interface InputEventHandler {

        /**
         * Called when an {@link InputEvent} is received.
         *
         * @param event The input event.
         * @return If the event should be consumed, return true. To allow the event to
         * continue on to the next handler, return false.
         */
        boolean handleInputEvent(InputEvent event);
    }

    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final boolean DEBUG = false;
    /**
     * animation multiplier
     */
    private static final int ANIMATION_MULTIPLIER = 1;

    private static final int START_FADE_OUT = 1;

    // Fading status
    private static final int IDLE = 0;
    private static final int IN = 1;
    private static final int OUT = 2;

    private int mAlignPosition;
    private int mPaddingBottom;
    private View mRootView;
    private int mBackgroundType = BG_DARK;
    private int mBgDarkColor;
    private int mBgLightColor;
    private int mShowTimeMs;
    private int mMajorFadeTranslateY, mMinorFadeTranslateY;
    private int mAnimationTranslateY;
    private OnFadeCompleteListener mFadeCompleteListener;
    private InputEventHandler mInputEventHandler;
    private boolean mFadingEnabled = true;
    private int mFadingStatus = IDLE;
    private int mBgAlpha;
    private boolean mControlViewsInitialized = false;
    private ValueAnimator mBgFadeInAnimator, mBgFadeOutAnimator;
    private ValueAnimator mControlRowFadeInAnimator, mControlRowFadeOutAnimator;
    private ValueAnimator mVideoTitleFadeInAnimator, mVideoTitleFadeOutAnimator;
    private ValueAnimator mDescriptionFadeInAnimator, mDescriptionFadeOutAnimator;
    private ValueAnimator mOtherRowFadeInAnimator, mOtherRowFadeOutAnimator;
    private boolean mResetControlsToPrimaryActionsPending;
    private View mPlayPauseView = null;
    private View mFastForwardView = null;
    private View mFastForwardViewIcon = null;
    private View mSkipPreviousView = null;
    private View mSkipPreviousViewIcon = null;
    private View mSkipNextView = null;
    private View mSkipNextViewIcon = null;
    private View mRewindView = null;
    private View mRewindViewIcon = null;
    private View mVideoDetailsSectionView = null;
    private boolean mKeyPressed = false;
    private int mKeyCode = -1;

    private final Animator.AnimatorListener mFadeListener =
            new Animator.AnimatorListener() {
                @Override
                public void onAnimationStart(Animator animation) {

                    enableVerticalGridAnimations(false);
                }

                @Override
                public void onAnimationRepeat(Animator animation) {

                }

                @Override
                public void onAnimationCancel(Animator animation) {

                }

                @Override
                public void onAnimationEnd(Animator animation) {

                    if (DEBUG) Log.v(TAG, "onAnimationEnd " + mBgAlpha);
                    if (mBgAlpha > 0) {
                        enableVerticalGridAnimations(true);
                        startFadeTimer();
                        if (mFadeCompleteListener != null) {
                            mFadeCompleteListener.onFadeInComplete();
                        }
                    }
                    else {
                        if (getVerticalGridView() != null) {
                            // Reset focus to the controls row
                            getVerticalGridView().setSelectedPosition(0);
                            resetControlsToPrimaryActions(null, true);
                        }
                        if (mFadeCompleteListener != null) {
                            mFadeCompleteListener.onFadeOutComplete();
                        }
                    }
                    mFadingStatus = IDLE;
                }
            };

    private final Handler mHandler = new Handler() {
        @Override
        public void handleMessage(Message message) {

            if (message.what == START_FADE_OUT && mFadingEnabled) {
                fade(false);
            }
        }
    };

    private final VerticalGridView.OnTouchInterceptListener mOnTouchInterceptListener =
            new VerticalGridView.OnTouchInterceptListener() {
                public boolean onInterceptTouchEvent(MotionEvent event) {

                    return onInterceptInputEvent(event);
                }
            };

    private final VerticalGridView.OnKeyInterceptListener mOnKeyInterceptListener =
            new VerticalGridView.OnKeyInterceptListener() {
                public boolean onInterceptKeyEvent(KeyEvent event) {

                    return onInterceptInputEvent(event);
                }
            };

    private void setBgAlpha(int alpha) {

        mBgAlpha = alpha;
        if (mRootView != null) {
            mRootView.getBackground().setAlpha(alpha);
        }
    }

    private void enableVerticalGridAnimations(boolean enable) {

        if (getVerticalGridView() != null) {
            getVerticalGridView().setAnimateChildLayout(enable);
        }
    }

    private void resetControlsToPrimaryActions(ItemBridgeAdapter.ViewHolder vh, boolean
            clearFocus) {

        if (vh == null && getVerticalGridView() != null) {
            vh = (ItemBridgeAdapter.ViewHolder) getVerticalGridView().findViewHolderForPosition(0);
        }
        if (vh == null) {
            mResetControlsToPrimaryActionsPending = true;
        }
        else if (vh.getPresenter() instanceof PlaybackControlsRowPresenter) {
            mResetControlsToPrimaryActionsPending = false;
            ((PlaybackControlsRowPresenter) vh.getPresenter()).showPrimaryActions(
                    (PlaybackControlsRowPresenter.ViewHolder) vh.getViewHolder());
            if (clearFocus) {
                vh.getViewHolder().view.clearFocus();
            }
        }
    }

    /**
     * Enables or disables view fading. If enabled, the view will be faded in when the fragment
     * starts, and will fade out after a time period. The timeout period is reset each time {@link
     * #tickle} is called.
     *
     * @param enabled True if view fading should be enabled; false otherwise.
     */
    protected void setFadingEnabled(boolean enabled) {

        if (DEBUG) Log.v(TAG, "setFadingEnabled " + enabled);
        Log.d(TAG, "setFadingEnabled? " + enabled);
        if (enabled != mFadingEnabled) {
            mFadingEnabled = enabled;
            if (mFadingEnabled) {
                if (isResumed() && mFadingStatus == IDLE
                        && !mHandler.hasMessages(START_FADE_OUT)) {
                    startFadeTimer();
                }
            }
            else {
                // Ensure fully opaque
                mHandler.removeMessages(START_FADE_OUT);
                fade(true);
            }
        }
    }

    /**
     * Determine if view fading is enabled.
     *
     * @return True if view fading is enabled; false otherwise.
     */
    public boolean isFadingEnabled() {

        return mFadingEnabled;
    }

    /**
     * Sets the listener to be called when fade in or out has completed.
     *
     * @param listener The listener.
     */
    protected void setFadeCompleteListener(OnFadeCompleteListener listener) {

        mFadeCompleteListener = listener;
    }

    /**
     * Returns the listener to be called when fade in or out has completed.
     *
     * @return The listener.
     */
    public OnFadeCompleteListener getFadeCompleteListener() {

        return mFadeCompleteListener;
    }

    /**
     * Sets the input event handler.
     *
     * @param handler The input event handler.
     */
    public final void setInputEventHandler(InputEventHandler handler) {

        mInputEventHandler = handler;
    }

    /**
     * Returns the input event handler.
     *
     * @return The input event handler.
     */
    public final InputEventHandler getInputEventHandler() {

        return mInputEventHandler;
    }

    /**
     * Tickles the playback controls. Fades in the view if it was faded out, otherwise resets the
     * fade out timer. Tickling on input events is handled by the fragment.
     */
    protected void tickle() {

        if (DEBUG) Log.v(TAG, "tickle enabled " + mFadingEnabled + " isResumed " + isResumed());
        if (!mFadingEnabled || !isResumed()) {
            return;
        }
        if (mHandler.hasMessages(START_FADE_OUT)) {
            // Restart the timer
            startFadeTimer();
            mOtherRowDisplayed = true;
        }
        else {
            fade(true);
            mOtherRowDisplayed = false;
        }
        initializeControlViews();
    }

    /**
     * Initializes the control views of the content. This method will only initialize the views once
     * and multiple calls to this method will not change anything. If for some reason you need to
     * re-initialize the views set mPlayPauseView = null before calling this method again.
     */
    private void initializeControlViews() {
        // If the play/pause view is null, we will try to the currently focused view and walk
        // its parent's parent to get to the skip forward and skip back buttons.
        if (mPlayPauseView == null) {
            if (getControlRowView() == null) {
                Log.e(TAG, "getControlRowView() is null in initializeControlViews");
                return;
            }
            mPlayPauseView = getControlRowView().findFocus();
            if (mPlayPauseView == null || mPlayPauseView.getParent() == null) {
                Log.e(TAG, "Either mPlayPauseView or mPlayPauseView.getParent() is null in " +
                        "initializeControlViews");
                return;
            }
            int index = ((ViewGroup) mPlayPauseView.getParent()).indexOfChild(mPlayPauseView);
            ViewGroup parent = (ViewGroup) mPlayPauseView.getParent().getParent();
            if (parent != null) {
                try {
                    int playPauseIndex = parent.indexOfChild((View) mPlayPauseView.getParent());

                    mRewindView = ((ViewGroup) parent.getChildAt(playPauseIndex - 1))
                            .getChildAt(index);
                    mRewindViewIcon = ((ViewGroup) parent.getChildAt(playPauseIndex - 1))
                            .getChildAt(index + 1);
                    mFastForwardView = ((ViewGroup) parent.getChildAt(playPauseIndex + 1))
                            .getChildAt(index);
                    mFastForwardViewIcon = ((ViewGroup) parent.getChildAt(playPauseIndex + 1))
                            .getChildAt(index + 1);
                    mSkipNextView = ((ViewGroup) parent.getChildAt(playPauseIndex + 2))
                            .getChildAt(index);
                    mSkipNextViewIcon = ((ViewGroup) parent.getChildAt(playPauseIndex + 2))
                            .getChildAt(index + 1);
                    mSkipPreviousView = ((ViewGroup) parent.getChildAt(playPauseIndex - 2))
                            .getChildAt(index);
                    mSkipPreviousViewIcon = ((ViewGroup) parent.getChildAt(playPauseIndex - 2))
                            .getChildAt(index + 1);
                    mControlViewsInitialized = true;
                }
                catch (Exception e) {
                    Log.e(TAG, "Exception in initializeControlViews", e);
                }
            }
        }
    }

    /**
     * Enables or disables the skip next action. Disabling an action renders the button
     * un-clickable and sets its alpha value to 30%, enabling reverses this effect.
     *
     * @param enabled True if the skip next action should be enabled; false otherwise.
     */
    protected void setSkipNextActionEnabled(boolean enabled) {

        Log.d(TAG, "setSkipNextActionEnabled enabled? " + enabled);
        initializeControlViews(); // To make sure views are initialized before we try disabling them
        setActionEnabled(mSkipNextView, mSkipNextViewIcon, enabled);
    }

    /**
     * Enables or disables the skip previous action. Disabling an action renders the button
     * un-clickable and sets its alpha value to 30%, enabling reverses this effect.
     *
     * @param enabled True if the skip previous action should be enabled; false otherwise.
     */
    protected void setSkipPreviousActionEnabled(boolean enabled) {

        Log.d(TAG, "setSkipPreviousActionEnabled enabled? " + enabled);
        initializeControlViews(); // To make sure views are initialized before we try disabling them
        setActionEnabled(mSkipPreviousView, mSkipPreviousViewIcon, enabled);
    }

    /**
     * Enables or disables the fast-forward action button. Disabling an action renders the
     * button un-clickable and sets its alpha value to 30%, enabling reverses this effect.
     *
     * @param enabled True if the fast-forward action should be enabled; false otherwise.
     */
    protected void setFastForwardActionEnabled(boolean enabled) {

        Log.d(TAG, "setFastForwardActionEnabled enabled? " + enabled);
        initializeControlViews(); // To make sure views are initialized before we try disabling them
        setActionEnabled(mFastForwardView, mFastForwardViewIcon, enabled);
    }

    /**
     * Enables or disables the rewind action button. Disabling an action renders the
     * button un-clickable and sets its alpha value to 30%, enabling reverses this effect.
     *
     * @param enabled True if the fast-forward action should be enabled; false otherwise.
     */
    protected void setRewindActionEnabled(boolean enabled) {

        Log.d(TAG, "setRewindActionEnabled enabled? " + enabled);
        initializeControlViews(); // To make sure views are initialized before we try disabling them
        setActionEnabled(mRewindView, mRewindViewIcon, enabled);
    }

    /**
     * Enables or disables a playback action. Disabling an action renders the button un-clickable
     * and sets its alpha value to 30%, enabling reverses this effect.
     *
     * @param actionView     The playback action view.
     * @param actionViewIcon The playback action view icon.
     * @param enabled        True if the action should be enabled; false otherwise.
     */
    private void setActionEnabled(View actionView, View actionViewIcon, boolean enabled) {

        if (actionView != null) {
            actionView.setClickable(enabled);
        }
        if (actionViewIcon != null) {
            actionViewIcon.setAlpha(enabled ? ENABLED_BUTTON_ALPHA_VALUE :
                                            DISABLED_BUTTON_ALPHA_VALUE);
        }
    }

    /**
     * Determine if the control views are initialized. This is needed so we can adjust the state of
     * some of the playback controls once we have determined where the views for the playback
     * controls are.
     *
     * @return True if controls initialized; false otherwise.
     */
    protected boolean areControlViewsInitialized() {

        return mControlViewsInitialized;
    }

    private boolean areControlsHidden() {

        return mFadingStatus == IDLE && mBgAlpha == 0;
    }

    private boolean onInterceptInputEvent(InputEvent event) {

        final boolean controlsHidden = areControlsHidden();
        if (DEBUG) Log.v(TAG, "onInterceptInputEvent hidden " + controlsHidden + " " + event);
        Log.d(TAG, "onInterceptInputEvent hidden " + controlsHidden + " " + event);
        boolean consumeEvent = false;
        int keyCode = KeyEvent.KEYCODE_UNKNOWN;

        if (mInputEventHandler != null) {
            consumeEvent = mInputEventHandler.handleInputEvent(event);
        }
        if (event instanceof KeyEvent) {

            keyCode = ((KeyEvent) event).getKeyCode();
        }

        // We will consume the key event if another is already pressed and held by the user except
        // when the Button A is pressed
        if (mKeyPressed
                && mKeyCode != keyCode
                && keyCode != KeyEvent.KEYCODE_BUTTON_A
                && mKeyCode != KeyEvent.KEYCODE_DPAD_CENTER) {
            return true;
        }

        // Set the key pressed to true if the action is DOWN and remember which key is pressed
        if (((KeyEvent) event).getAction() == KeyEvent.ACTION_DOWN) {
            mKeyPressed = true;
            mKeyCode = keyCode;
        }
        else {
            mKeyPressed = false;
            mKeyCode = -1;
        }

        switch (keyCode) {
            case KeyEvent.KEYCODE_BUTTON_A:
                // Device gets KEYCODE_BUTTON_A and KEYCODE_DPAD_CENTER key events when Button A
                // on game controller is pressed. Mark keypressed false so that the overlay can
                // consume the following CENTER keyevent and ignore BUTTON_A key event.
                mKeyPressed = false;
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
            case KeyEvent.KEYCODE_DPAD_DOWN:
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                // Event may be consumed; regardless, if controls are hidden then these keys will
                // bring up the controls.
                if (controlsHidden) {
                    consumeEvent = true;
                }
                tickle();
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                // If fading is disabled then do not stop it again.
                if (!isFadingEnabled() || controlsHidden || ((KeyEvent) event).getAction() ==
                        KeyEvent.ACTION_DOWN) {
                    break;
                }
                stopFadeTimer();
                if (!mOtherRowDisplayed) {
                    fade(false);
                }
                else {
                    mOtherRowDisplayed = false;
                    startFadeTimer();
                }
                break;
            case KeyEvent.KEYCODE_BACK:
            case KeyEvent.KEYCODE_ESCAPE:
                consumeEvent = handleEscapeKey((KeyEvent) event);
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                // Handle play/pause key.  The focus shifts to the "Play/Pause" button. We also
                // handle the button press effect for a more realistic button experience.

                // If any other media button is being pressed, ignore this event!
                consumeEvent = handleMediaKey((KeyEvent) event, mPlayPauseView, mRewindView,
                                              mFastForwardView);
                break;
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                // Handle shift back key.  The focus shifts to the "Rewind" button. We also
                // handle the button press effect for a more realistic button experience.

                // If any other media button is pressed, ignore this event
                consumeEvent = handleMediaKey((KeyEvent) event, mRewindView, mPlayPauseView,
                                              mFastForwardView);
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                // Handle skip forward key.  The focus shifts to the "FF" button. We also
                // handle the button press effect for a more realistic button experience.

                // If any other media is pressed, ignore this event!
                consumeEvent = handleMediaKey((KeyEvent) event, mFastForwardView, mPlayPauseView,
                                              mRewindView);
                break;
            default:
                if (consumeEvent) {
                    tickle();
                }
        }
        return consumeEvent;
    }

    /**
     * Handles the Escape key event.  This either hides the controls if playback is in session
     * and if the playback controls are visible or returns to the previous activity otherwise.
     *
     * @param event The input key event
     * @return If the event is consumed
     */
    private boolean handleEscapeKey(KeyEvent event) {

        boolean consumeEvent = false;
        boolean controlsHidden = areControlsHidden();
        // If fading enabled and controls are not hidden, back will be consumed to fade
        // them out (even if the key was consumed by the handler).
        // Fixed a bug in leanback code where the back button takes the user back to the
        // detail page.  The back button should ideally dismiss the the playback controls if
        // the video playback is in session (not paused).  I consume the "KEY_DOWN" event so
        // the system acts only on the "KEY_UP" event.
        if (event.getAction() == KeyEvent.ACTION_DOWN &&
                mFadingEnabled && !controlsHidden) {
            return true;
        }
        if (mFadingEnabled && !controlsHidden) {
            consumeEvent = true;
            mHandler.removeMessages(START_FADE_OUT);
            fade(false);
        }
        else if (consumeEvent) {
            tickle();
        }
        return consumeEvent;
    }

    /**
     * Handles the Media key event.  The key event is consumed if the user is pressing and holding
     * another media key.  Otherwise, the selected media key is focused and is either pressed or
     * unpressed depending upon whether the action is either a key down or a key up.
     *
     * @param event              The input key event
     * @param focusedMediaKey    The media key that is currently pressed (PLAY, REW or FF)
     * @param unfocusedMediaKey1 One of the other unfocused media keys
     * @param unfocusedMediaKey2 The second of the unfocused media keys
     * @return If the event is consumed.  This could either be because the user is pressing and
     * holding another media key or if the secondary row is visible.
     */
    private boolean handleMediaKey(KeyEvent event, View focusedMediaKey, View unfocusedMediaKey1,
                                   View unfocusedMediaKey2) {

        if ((unfocusedMediaKey1 != null && unfocusedMediaKey1.isPressed())
                || (unfocusedMediaKey2 != null && unfocusedMediaKey2.isPressed())) {
            return true;
        }
        if (getVerticalGridView() != null) {
            ItemBridgeAdapter.ViewHolder adapterVh = (ItemBridgeAdapter.ViewHolder)
                    getVerticalGridView().findViewHolderForPosition(0);
            resetControlsToPrimaryActions(adapterVh, false);
        }
        else {
            Log.e(TAG, "getVerticalGridView is null in handleMediaKey");
        }
        tickle();
        if (focusedMediaKey != null) {
            focusedMediaKey.requestFocus();
            focusedMediaKey.requestLayout();
            if (event.getAction() == KeyEvent.ACTION_DOWN) {
                focusedMediaKey.setPressed(true);
                stopFadeTimer();
            }
            else {
                focusedMediaKey.setPressed(false);
                startFadeTimer();
            }
        }
        else {
            Log.e(TAG, "Media key is not focused");
        }
        return false;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onResume() {

        super.onResume();
        if (mFadingEnabled) {
            setBgAlpha(0);
            fade(true);
        }
        getVerticalGridView().setOnTouchInterceptListener(mOnTouchInterceptListener);
        getVerticalGridView().setOnKeyInterceptListener(mOnKeyInterceptListener);
    }

    /**
     * Updates content details section whenever content is updated
     */
    protected void updateContentDetailsView() {

        if (mVideoDetailsSectionView == null) {
            Log.e(TAG, "mVideoDetailsSectionView is null in updateContentDetailsView");
            return;
        }
        setVideoDetailsSectionTextViewsText(R.id.videoTitle, getVideoTitle());
        setVideoDetailsSectionTextViewsText(R.id.videoSubtitle, getVideoSubtitle());
    }

    private void setVideoDetailsSectionTextViewsText(int textViewId, String text) {

        TextView textView = (TextView) mVideoDetailsSectionView.findViewById(textViewId);
        if (textView != null) {
            textView.setText(text);
        }
        else {
            Log.i(TAG, "TextView is not found in setVideoDetailsSectionTextViewsText");
        }
    }

    private void startFadeTimer() {

        if (!mFadingEnabled) {
            Log.d(TAG, "Fading is disabled");
            return;
        }
        if (mHandler != null) {
            mHandler.removeMessages(START_FADE_OUT);
            mHandler.sendEmptyMessageDelayed(START_FADE_OUT, mShowTimeMs);
        }
    }

    /**
     * Stops the fade timer.  This is useful when the user presses and holds the
     * media buttons.
     */
    private void stopFadeTimer() {

        if (mHandler != null) {
            mHandler.removeMessages(START_FADE_OUT);
        }
    }

    private static ValueAnimator loadAnimator(Context context, int resId) {

        ValueAnimator animator = (ValueAnimator) AnimatorInflater.loadAnimator(context, resId);
        animator.setDuration(animator.getDuration() * ANIMATION_MULTIPLIER);
        return animator;
    }

    private void loadBgAnimator() {

        ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                setBgAlpha((Integer) arg0.getAnimatedValue());
            }
        };

        mBgFadeInAnimator = loadAnimator(getActivity(), R.animator.lb_playback_bg_fade_in);
        mBgFadeInAnimator.addUpdateListener(listener);
        mBgFadeInAnimator.addListener(mFadeListener);

        mBgFadeOutAnimator = loadAnimator(getActivity(), R.animator.lb_playback_bg_fade_out);
        mBgFadeOutAnimator.addUpdateListener(listener);
        mBgFadeOutAnimator.addListener(mFadeListener);
    }

    private TimeInterpolator mLogDecelerateInterpolator = new LogDecelerateInterpolator(100, 0);
    private TimeInterpolator mLogAccelerateInterpolator = new LogAccelerateInterpolator(100, 0);

    private View getControlRowView() {

        if (getVerticalGridView() == null) {
            Log.e(TAG, "getVerticalGridView() is null in getControlRowView");
            return null;
        }
        RecyclerView.ViewHolder vh = getVerticalGridView().findViewHolderForPosition(0);
        if (vh == null) {
            Log.e(TAG, "RecyclerView.ViewHolder is null in getControlRowView");
            return null;
        }
        return vh.itemView;
    }

    private void loadControlRowAnimator() {

        final AnimatorListener listener = new AnimatorListener() {
            @Override
            void getViews(ArrayList<View> views) {

                View view = getControlRowView();
                if (view != null) {
                    views.add(view);
                }
                else {
                    Log.e(TAG, "View is null in loadControlRowAnimator");
                }
            }
        };
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator
                .AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                View view = getControlRowView();
                if (view != null) {
                    final float fraction = (Float) arg0.getAnimatedValue();
                    if (DEBUG) Log.v(TAG, "fraction " + fraction);
                    view.setAlpha(fraction);
                    view.setTranslationY((float) mAnimationTranslateY * (1f - fraction));
                }
                else {
                    Log.e(TAG, "View is null in onAnimationUpdate");
                }
            }
        };

        mControlRowFadeInAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_controls_fade_in);
        mControlRowFadeInAnimator.addUpdateListener(updateListener);
        mControlRowFadeInAnimator.addListener(listener);
        mControlRowFadeInAnimator.setInterpolator(mLogDecelerateInterpolator);

        mControlRowFadeOutAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_controls_fade_out);
        mControlRowFadeOutAnimator.addUpdateListener(updateListener);
        mControlRowFadeOutAnimator.addListener(listener);
        mControlRowFadeOutAnimator.setInterpolator(mLogAccelerateInterpolator);
    }

    /**
     * loads the animator for the video title
     * The title will accelerate into the screen and decelerate out of the screen
     */
    private void loadVideoTitleAnimator() {

        final AnimatorListener listener = new AnimatorListener() {
            @Override
            void getViews(ArrayList<View> views) {

                if (mVideoDetailsSectionView != null) {
                    views.add(mVideoDetailsSectionView);
                }
                else {
                    Log.e(TAG, "mVideoDetailsSectionView is null");
                }
            }
        };
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator
                .AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                if (mVideoDetailsSectionView != null) {
                    if (isAdded()) {
                        final int initialHeight = getResources().getDimensionPixelSize(R.dimen
                                                                                               .playback_video_details_margin);
                        final float fraction = (Float) arg0.getAnimatedValue();
                        mVideoDetailsSectionView.setAlpha(fraction);
                        mVideoDetailsSectionView.setY(initialHeight * fraction);
                    }
                    else {
                        Log.e(TAG, "Trying to animate mVideoDetailsSectionView while the fragment" +
                                " is not attached to the activity");
                    }
                }
            }
        };

        mVideoTitleFadeInAnimator = loadAnimator(
                getActivity(), R.animator.playback_video_title_fade_in);
        mVideoTitleFadeInAnimator.addUpdateListener(updateListener);
        mVideoTitleFadeInAnimator.addListener(listener);
        mVideoTitleFadeInAnimator.setInterpolator(mLogDecelerateInterpolator);

        mVideoTitleFadeOutAnimator = loadAnimator(
                getActivity(), R.animator.playback_video_title_fade_out);
        mVideoTitleFadeOutAnimator.addUpdateListener(updateListener);
        mVideoTitleFadeOutAnimator.addListener(listener);
        mVideoTitleFadeOutAnimator.setInterpolator(mLogAccelerateInterpolator);
    }

    private void loadOtherRowAnimator() {

        final AnimatorListener listener = new AnimatorListener() {
            @Override
            void getViews(ArrayList<View> views) {

                if (getVerticalGridView() == null) {
                    Log.e(TAG, "getVerticalGridView() returned null in getViews");
                    return;
                }
                final int count = getVerticalGridView().getChildCount();
                for (int i = 0; i < count; i++) {
                    View view = getVerticalGridView().getChildAt(i);
                    if (view != null) {
                        views.add(view);
                    }
                }
            }
        };
        final ValueAnimator.AnimatorUpdateListener updateListener = new ValueAnimator
                .AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                if (getVerticalGridView() == null) {
                    Log.e(TAG, "getVerticalGridView() returned null in onAnimationUpdate");
                    return;
                }
                final float fraction = (Float) arg0.getAnimatedValue();
                for (View view : listener.mViews) {
                    if (getVerticalGridView().getChildPosition(view) > 0) {
                        view.setAlpha(fraction);
                        view.setTranslationY((float) mAnimationTranslateY * (1f - fraction));
                    }
                }
            }
        };

        mOtherRowFadeInAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_controls_fade_in);
        mOtherRowFadeInAnimator.addListener(listener);
        mOtherRowFadeInAnimator.addUpdateListener(updateListener);
        mOtherRowFadeInAnimator.setInterpolator(mLogDecelerateInterpolator);

        mOtherRowFadeOutAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_controls_fade_out);
        mOtherRowFadeOutAnimator.addListener(listener);
        mOtherRowFadeOutAnimator.addUpdateListener(updateListener);
        mOtherRowFadeOutAnimator.setInterpolator(new AccelerateInterpolator());
    }

    private void loadDescriptionAnimator() {

        ValueAnimator.AnimatorUpdateListener listener = new ValueAnimator.AnimatorUpdateListener() {
            @Override
            public void onAnimationUpdate(ValueAnimator arg0) {

                if (getVerticalGridView() == null) {
                    Log.e(TAG, "getVerticalGridView() returned null in loadDescriptionAnimator");
                    return;
                }
                ItemBridgeAdapter.ViewHolder adapterVh = (ItemBridgeAdapter.ViewHolder)
                        getVerticalGridView().findViewHolderForPosition(0);
                if (adapterVh != null && adapterVh.getViewHolder()
                        instanceof PlaybackControlsRowPresenter.ViewHolder) {
                    final Presenter.ViewHolder vh = ((PlaybackControlsRowPresenter.ViewHolder)
                            adapterVh.getViewHolder()).mDescriptionViewHolder;
                    if (vh != null) {
                        vh.view.setAlpha((Float) arg0.getAnimatedValue());
                    }
                }
            }
        };

        mDescriptionFadeInAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_description_fade_in);
        mDescriptionFadeInAnimator.addUpdateListener(listener);
        mDescriptionFadeInAnimator.setInterpolator(mLogDecelerateInterpolator);

        mDescriptionFadeOutAnimator = loadAnimator(
                getActivity(), R.animator.lb_playback_description_fade_out);
        mDescriptionFadeOutAnimator.addUpdateListener(listener);
    }

    private void fade(boolean fadeIn) {

        if (DEBUG) Log.v(TAG, "fade " + fadeIn);
        if (getView() == null) {
            Log.e(TAG, "getView() returned null");
            return;
        }
        if ((fadeIn && mFadingStatus == IN) || (!fadeIn && mFadingStatus == OUT)) {
            if (DEBUG) Log.v(TAG, "requested fade in progress");
            return;
        }
        if ((fadeIn && mBgAlpha == 255) || (!fadeIn && mBgAlpha == 0)) {
            if (DEBUG) Log.v(TAG, "fade is no-op");
            return;
        }

        mAnimationTranslateY = getVerticalGridView().getSelectedPosition() == 0 ?
                mMajorFadeTranslateY : mMinorFadeTranslateY;
        Log.d(TAG, "mFadingStatus: " + mFadingStatus + " and fadeIn: " + fadeIn);
        if (mFadingStatus == IDLE) {
            if (fadeIn) {
                mBgFadeInAnimator.start();
                mControlRowFadeInAnimator.start();
                mOtherRowFadeInAnimator.start();
                mDescriptionFadeInAnimator.start();
                mVideoTitleFadeInAnimator.start();
            }
            else {
                mBgFadeOutAnimator.start();
                mControlRowFadeOutAnimator.start();
                mOtherRowFadeOutAnimator.start();
                mDescriptionFadeOutAnimator.start();
                mVideoTitleFadeOutAnimator.start();
            }
        }
        else {
            if (fadeIn) {
                mBgFadeOutAnimator.reverse();
                mControlRowFadeOutAnimator.reverse();
                mOtherRowFadeOutAnimator.reverse();
                mDescriptionFadeOutAnimator.reverse();
                mVideoTitleFadeOutAnimator.reverse();
            }
            else {
                mBgFadeInAnimator.reverse();
                mControlRowFadeInAnimator.reverse();
                mOtherRowFadeInAnimator.reverse();
                mDescriptionFadeInAnimator.reverse();
                mVideoTitleFadeInAnimator.reverse();
            }
        }

        // If fading in while control row is focused, set initial translationY so
        // views slide in from below.
        if (fadeIn && mFadingStatus == IDLE) {
            final int count = getVerticalGridView().getChildCount();
            for (int i = 0; i < count; i++) {
                getVerticalGridView().getChildAt(i).setTranslationY(mAnimationTranslateY);
            }
        }

        mFadingStatus = fadeIn ? IN : OUT;
    }

    /**
     * Sets the list of rows for the fragment.
     *
     * @param adapter The object adapter.
     */
    @Override
    public void setAdapter(ObjectAdapter adapter) {

        if (getAdapter() != null) {
            getAdapter().unregisterObserver(mObserver);
        }
        else {
            Log.e(TAG, "getAdapter() returned null");
        }
        super.setAdapter(adapter);
        if (adapter != null) {
            adapter.registerObserver(mObserver);
        }
        else {
            Log.e(TAG, "Adapter is null");
        }
    }

    @Override
    void setVerticalGridViewLayout(VerticalGridView listview) {

        if (listview == null) {
            return;
        }
        // Padding affects alignment when last row is focused
        // (last is first when there's only one row).
        setBottomPadding(listview, mPaddingBottom);

        // Item alignment affects focused row that isn't the last.
        listview.setItemAlignmentOffset(mAlignPosition);
        listview.setItemAlignmentOffsetPercent(100);

        // Push rows to the bottom.
        listview.setWindowAlignmentOffset(0);
        listview.setWindowAlignmentOffsetPercent(100);
        listview.setWindowAlignment(VerticalGridView.WINDOW_ALIGN_HIGH_EDGE);
    }

    private static void setBottomPadding(View view, int padding) {

        view.setPadding(view.getPaddingLeft(), view.getPaddingTop(),
                        view.getPaddingRight(), padding);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mAlignPosition =
                getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_align_bottom);
        mPaddingBottom =
                getResources().getDimensionPixelSize(R.dimen.lb_playback_controls_padding_bottom);
        mBgDarkColor =
                getResources().getColor(R.color.lb_playback_controls_background_dark);
        mBgLightColor =
                getResources().getColor(R.color.lb_playback_controls_background_light);
        mShowTimeMs =
                getResources().getInteger(R.integer.lb_playback_controls_show_time_ms);
        mMajorFadeTranslateY =
                getResources().getDimensionPixelSize(R.dimen.lb_playback_major_fade_translate_y);
        mMinorFadeTranslateY =
                getResources().getDimensionPixelSize(R.dimen.lb_playback_minor_fade_translate_y);

        loadBgAnimator();
        loadControlRowAnimator();
        loadVideoTitleAnimator();
        loadOtherRowAnimator();
        loadDescriptionAnimator();
    }

    /**
     * Sets the background type.
     *
     * @param type One of BG_LIGHT, BG_DARK, or BG_NONE.
     */
    protected void setBackgroundType(int type) {

        switch (type) {
            case BG_LIGHT:
            case BG_DARK:
            case BG_NONE:
                if (type != mBackgroundType) {
                    mBackgroundType = type;
                    updateBackground();
                }
                break;
            default:
                throw new IllegalArgumentException("Invalid background type");
        }
    }

    /**
     * Gets the background type.
     *
     * @return The background type
     */
    public int getBackgroundType() {

        return mBackgroundType;
    }

    private void updateBackground() {

        if (mRootView != null) {
            int color = mBgDarkColor;
            switch (mBackgroundType) {
                case BG_DARK:
                    break;
                case BG_LIGHT:
                    color = mBgLightColor;
                    break;
                case BG_NONE:
                    color = Color.TRANSPARENT;
                    break;
            }
            mRootView.setBackground(new ColorDrawable(color));
        }
        else {
            Log.e(TAG, "Failed to get the root view for this fragment");
        }
    }

    private void updateControlsBottomSpace(ItemBridgeAdapter.ViewHolder vh) {
        // Add extra space between rows 0 and 1
        if (vh == null && getVerticalGridView() != null) {
            vh = (ItemBridgeAdapter.ViewHolder)
                    getVerticalGridView().findViewHolderForPosition(0);
        }
        if (vh != null && vh.getPresenter() instanceof PlaybackControlsRowPresenter) {
            final int adapterSize = getAdapter() == null ? 0 : getAdapter().size();
            ((PlaybackControlsRowPresenter) vh.getPresenter()).showBottomSpace(
                    (PlaybackControlsRowPresenter.ViewHolder) vh.getViewHolder(),
                    adapterSize > 1);
        }
    }

    private final ItemBridgeAdapter.AdapterListener mAdapterListener =
            new ItemBridgeAdapter.AdapterListener() {
                @Override
                public void onAttachedToWindow(ItemBridgeAdapter.ViewHolder vh) {

                    if (DEBUG) Log.v(TAG, "onAttachedToWindow " + vh.getViewHolder().view);
                    if ((mFadingStatus == IDLE && mBgAlpha == 0) || mFadingStatus == OUT) {
                        if (DEBUG) Log.v(TAG, "setting alpha to 0");
                        vh.getViewHolder().view.setAlpha(0);
                    }
                    if (vh.getPosition() == 0 && mResetControlsToPrimaryActionsPending) {
                        resetControlsToPrimaryActions(vh, true);
                    }
                    customActionsOnAttachedToWindow();
                }

                @Override
                public void onDetachedFromWindow(ItemBridgeAdapter.ViewHolder vh) {

                    if (DEBUG) Log.v(TAG, "onDetachedFromWindow " + vh.getViewHolder().view);
                    // Reset animation state
                    vh.getViewHolder().view.setAlpha(1f);
                    vh.getViewHolder().view.setTranslationY(0);
                    if (vh.getViewHolder() instanceof PlaybackControlsRowPresenter.ViewHolder) {
                        Presenter.ViewHolder descriptionVh = ((PlaybackControlsRowPresenter
                                .ViewHolder)
                                vh.getViewHolder()).mDescriptionViewHolder;
                        if (descriptionVh != null) {
                            descriptionVh.view.setAlpha(1f);
                        }
                    }
                }

                @Override
                public void onBind(ItemBridgeAdapter.ViewHolder vh) {

                    if (vh.getPosition() == 0) {
                        updateControlsBottomSpace(vh);
                    }
                }
            };

    /**
     * De-activate the view by marking it non-focusable
     */
    private void disableDummyActionView(View view) {

        view.setFocusable(false);
        view.setFocusableInTouchMode(false);
    }

    /**
     * Implements any custom actions to be performed on window attach time. The child classes
     * should extend this method to add there own custom actions
     */
    protected void customActionsOnAttachedToWindow() {
        // Not doing anything in default implementation
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        mRootView = super.onCreateView(inflater, container, savedInstanceState);
        if (mRootView == null) {
            Log.e(TAG, "could not find root view for this fragment");
            return null;
        }
        ViewGroup fragment_root = (ViewGroup) mRootView.findViewById(R.id.details_fragment_root);
        addVideoDetailsView(inflater, fragment_root);
        mBgAlpha = 255;
        updateBackground();
        getRowsFragment().setExternalAdapterListener(mAdapterListener);
        return mRootView;
    }

    private void addVideoDetailsView(LayoutInflater inflater, ViewGroup viewGroup) {

        View videoDetailsView = inflater.inflate(R.layout.video_detail, viewGroup, false);
        mVideoDetailsSectionView = videoDetailsView.findViewById(R.id.videoDetails);

        TextView videoTitle = (TextView) videoDetailsView.findViewById(R.id.videoTitle);
        videoTitle.setText(getVideoTitle());
        CalligraphyUtils.applyFontToTextView(getActivity(), videoTitle, ConfigurationManager
                .getInstance(getActivity()).getTypefacePath(ConfigurationConstants.BOLD_FONT));

        TextView videoSubtitle = (TextView) videoDetailsView.findViewById(R.id.videoSubtitle);
        videoSubtitle.setText(getVideoSubtitle());
        CalligraphyUtils.applyFontToTextView(getActivity(), videoSubtitle, ConfigurationManager
                .getInstance(getActivity()).getTypefacePath(ConfigurationConstants.LIGHT_FONT));

        viewGroup.addView(videoDetailsView, 0);
    }

    /**
     * Returns title of the current video, Users should override this method to set the actual
     * title,
     * the default implementation returns a default value
     *
     * @return title of current video
     */
    protected String getVideoTitle() {

        if (isAdded()) { // Check if the fragment is attached to activity
            return getResources().getString(R.string.video_title);
        }
        else {
            Log.e(TAG, "Fragment is not attached to the activity");
            return "";
        }
    }

    /**
     * Returns subtitle of the current video, Users should override this method to set the actual
     * subtitle, the default implementation returns a default value
     *
     * @return subtitle of current video
     */
    protected String getVideoSubtitle() {

        if (isAdded()) { // Check if the fragment is attached to activity
            return getResources().getString(R.string.video_subtitle);
        }
        else {
            Log.e(TAG, "Fragment is not attached to the activity");
            return "";
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onDestroyView() {

        mRootView = null;
        super.onDestroyView();
    }

    private final ObjectAdapter.DataObserver mObserver = new ObjectAdapter.DataObserver() {
        public void onChanged() {

            updateControlsBottomSpace(null);
        }
    };

    /**
     * Abstract class extending AnimatorListener according to TenFootUI UX requirements
     */
    static abstract class AnimatorListener implements Animator.AnimatorListener {

        ArrayList<View> mViews = new ArrayList<>();
        ArrayList<Integer> mLayerType = new ArrayList<>();

        public void onAnimationCancel(Animator animation) {

        }

        public void onAnimationRepeat(Animator animation) {

        }

        public void onAnimationStart(Animator animation) {

            getViews(mViews);
            for (View view : mViews) {
                mLayerType.add(view.getLayerType());
                view.setLayerType(View.LAYER_TYPE_HARDWARE, null);
            }
        }

        public void onAnimationEnd(Animator animation) {

            for (int i = 0; i < mViews.size(); i++) {
                mViews.get(i).setLayerType(mLayerType.get(i), null);
            }
            mLayerType.clear();
            mViews.clear();
        }

        abstract void getViews(ArrayList<View> views);
    }

}
