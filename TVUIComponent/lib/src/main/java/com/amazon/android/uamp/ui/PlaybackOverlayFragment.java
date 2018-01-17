/**
 * This file was modified by Amazon:
 * Copyright 2015-2016 Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *      http://aws.amazon.com/apache2.0/
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
package com.amazon.android.uamp.ui;

import com.amazon.analytics.AnalyticsTags;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.contentbrowser.helper.AnalyticsHelper;
import com.amazon.android.model.content.Content;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.presenter.CardPresenter;
import com.amazon.android.tv.tenfoot.utils.ContentHelper;
import com.amazon.android.uamp.mediaSession.MediaSessionController;
import com.amazon.utils.StringManipulation;
import com.amazon.android.utils.GlideHelper;
import com.bumptech.glide.load.resource.drawable.GlideDrawable;
import com.bumptech.glide.request.animation.GlideAnimation;
import com.bumptech.glide.request.target.SimpleTarget;

import android.app.Activity;
import android.content.Context;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.v17.leanback.app.TenFootPlaybackOverlayFragment;
import android.support.v17.leanback.widget.AbstractDetailsDescriptionPresenter;
import android.support.v17.leanback.widget.Action;
import android.support.v17.leanback.widget.ArrayObjectAdapter;
import android.support.v17.leanback.widget.ClassPresenterSelector;
import android.support.v17.leanback.widget.ControlButtonPresenterSelector;
import android.support.v17.leanback.widget.HeaderItem;
import android.support.v17.leanback.widget.ListRow;
import android.support.v17.leanback.widget.ListRowPresenter;
import android.support.v17.leanback.widget.OnActionClickedListener;
import android.support.v17.leanback.widget.OnItemViewClickedListener;
import android.support.v17.leanback.widget.PlaybackControlsRow;
import android.support.v17.leanback.widget.PlaybackControlsRow.FastForwardAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.PlayPauseAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RepeatAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.RewindAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ShuffleAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipNextAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.SkipPreviousAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsDownAction;
import android.support.v17.leanback.widget.PlaybackControlsRow.ThumbsUpAction;
import android.support.v17.leanback.widget.PlaybackControlsRowPresenter;
import android.support.v17.leanback.widget.Presenter;
import android.support.v17.leanback.widget.Row;
import android.support.v17.leanback.widget.RowHeaderPresenter;
import android.support.v17.leanback.widget.RowPresenter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;

import java.util.ArrayList;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

/**
 * Class for video playback with media control.
 */
public class PlaybackOverlayFragment extends TenFootPlaybackOverlayFragment
        implements MediaSessionController.OnMediaSessionEventListener {

    private static final String TAG = PlaybackOverlayFragment.class.getSimpleName();
    private static final boolean SHOW_DETAIL = false;
    private static final int PRIMARY_CONTROLS = 5;
    private static final boolean SHOW_IMAGE = false;
    private static final int BACKGROUND_TYPE = PlaybackOverlayFragment.BG_LIGHT;
    private static final int CARD_WIDTH = 150;
    private static final int CARD_HEIGHT = 240;
    private static final int DEFAULT_UPDATE_PERIOD = 1000;
    private static final int CLICK_TRACKING_DELAY = 1000;
    private static final int INITIAL_SPEED = 10000;

    private Context mContext;
    private final Handler mClickTrackingHandler = new Handler();
    private OnPlayPauseClickedListener mCallback;
    private ArrayObjectAdapter mRowsAdapter;
    private ArrayObjectAdapter mPrimaryActionsAdapter;
    private ArrayObjectAdapter mSecondaryActionsAdapter;
    private PlayPauseAction mPlayPauseAction;

    private FastForwardAction mFastForwardAction;
    private RewindAction mRewindAction;
    private SkipNextAction mSkipNextAction;
    private SkipPreviousAction mSkipPreviousAction;
    private PlaybackControlsRow.ClosedCaptioningAction mClosedCaptioningAction;
    private PlaybackControlsRow mPlaybackControlsRow;
    private List<Content> mRelatedContentList;
    private int mCurrentItem;
    private int mDuration;
    private Handler mHandler;
    private Runnable mRunnable;
    private Content mSelectedContent;
    private int mFfwRwdSpeed = INITIAL_SPEED;
    private Timer mClickTrackingTimer;
    private int mClickCount;
    private boolean mFadeOutComplete;
    private boolean mShowRelatedContent;
    private boolean mHideMoreActions;

    /**
     * Drawable to show for CC on state.
     */
    private Drawable mCloseCaptionOnDrawable;

    /**
     * Drawable to show for CC off state.
     */
    private Drawable mCloseCaptionOffDrawable;

    /**
     * Drawable to show for CC disabled state.
     */
    private Drawable mCloseCaptionDisabledDrawable;

    /**
     * CC button state.
     */
    private boolean mCCButtonState = false;

    /**
     * Is CC button disabled, emulate 3 button state.
     */
    private boolean mCCButtonDisabled = false;

    /**
     * Overriding this method to return null since we do not want the title view to be available
     * in Playback page.
     * {@inheritDoc}
     */
    @Override
    protected View inflateTitle(LayoutInflater inflater, ViewGroup parent,
                                Bundle savedInstanceState) {

        return null;
    }

    /**
     * If current running content is changed  during playback via external components,
     * we want to update the metadata like content title, current running content Id but we do not
     * want to update the recommendations list. This method achieves the desired behaviour.
     *
     * @param newContent new content being played
     */
    public void updateCurrentContent(Content newContent) {

        mSelectedContent = newContent;
        if (mShowRelatedContent) {
            // Find the index of the selected content in the related content list only if
            // recommendations are enabled
            mCurrentItem = getCurrentContentIndex(newContent);
        }
        else {
            //initializing to empty list to avoid null checks later
            mRelatedContentList = new ArrayList<>();
            Log.d(TAG, "Recommendation is turned off");
        }
        updateContentDetailsView();
    }

    /**
     * Find the index of the selected content in the related content list.
     */
    private int getCurrentContentIndex(Content content) {

        if (content == null) {
            return -1;
        }
        int index = -1;
        for (int j = 0; j < mRelatedContentList.size(); j++) {
            final Content currentContent = mRelatedContentList.get(j);
            if (StringManipulation.areStringsEqual(content.getId(), currentContent.getId())) {
                index = j;
                break;
            }
        }
        return index;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        mSelectedContent = (Content) getActivity().getIntent()
                                                  .getSerializableExtra(
                                                          Content.class.getSimpleName());

        mShowRelatedContent = ContentBrowser.getInstance(getActivity()).isShowRelatedContent();

        mHideMoreActions = true;

        mCurrentItem = -1;
        if (mShowRelatedContent) {
            mRelatedContentList = ContentBrowser.getInstance(getActivity())
                                                .getRecommendedListOfAContentAsAContainer
                                                        (mSelectedContent)
                                                .getContents();
            // Find the index of the selected content in the related content list only if
            // recommendations are enabled.
            mCurrentItem = getCurrentContentIndex(mSelectedContent);
        }
        else {
            Log.d(TAG, "Recommendation is turned off");
            // Initializing to empty list to avoid null checks later.
            mRelatedContentList = new ArrayList<>();
        }

        mHandler = new Handler();

        setBackgroundType(BACKGROUND_TYPE);
        setFadingEnabled(false);

        setupRows();

        mRunnable = new Runnable() {
            @Override
            public void run() {

                int updatePeriod = getUpdatePeriod();
                if (!mFadeOutComplete) {
                    updateUI();
                }
                mHandler.postDelayed(this, updatePeriod);
            }
        };

        setOnItemViewClickedListener(new ItemViewClickedListener());

        setFadeCompleteListener(new OnFadeCompleteListener() {
            @Override
            public void onFadeOutComplete() {

                mFadeOutComplete = true;
            }

            @Override
            public void onFadeInComplete() {

                mFadeOutComplete = false;
                updateUI();
            }
        });
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle
            savedInstanceState) {

        View rootView = super.onCreateView(inflater, container, savedInstanceState);
        if (rootView == null) {
            Log.e(TAG, "could not find root view for this fragment");
            return null;
        }
        final ViewGroup layout = (ViewGroup) rootView.findViewById(R.id.details_fragment_root);
        if (layout != null) {
            ViewTreeObserver viewTreeObserver = layout.getViewTreeObserver();
            viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver
                    .OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {

                    setActionsInitialStates();
                    // Continue getting called until we are able to initialize the control views
                    // as we have to watch the details fragment this may take a few updates.
                    if (areControlViewsInitialized()) {
                        layout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                    }
                }
            });
        }

        return rootView;
    }

    private void updateUI() {

        int index = mPlayPauseAction.getIndex();
        if (index == PlayPauseAction.PAUSE) {
            mPlaybackControlsRow.setCurrentTime(mCallback.getCurrentPosition());
        }
        mPlaybackControlsRow.setBufferedProgress(mCallback.getBufferProgressPosition());
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onAttach(Activity activity) {

        super.onAttach(activity);

        // This makes sure that the container activity has implemented the callback interface. If
        // not, it throws an exception.
        try {
            mCallback = (OnPlayPauseClickedListener) activity;
        }
        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                                                 + " must implement OnPlayPauseClickedListener: "
                                                 + e);
        }
        mContext = getActivity();
    }

    @Override
    public void onResume() {

        super.onResume();
        togglePlayback(((PlaybackActivity) mContext).isPlaying());
    }

    private void setupRows() {

        ClassPresenterSelector ps = new ClassPresenterSelector();
        PlaybackControlsRowPresenter playbackControlsRowPresenter;
        if (SHOW_DETAIL) {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter(
                    new DescriptionPresenter());
        }
        else {
            playbackControlsRowPresenter = new PlaybackControlsRowPresenter();
        }
        playbackControlsRowPresenter.setOnActionClickedListener(new OnActionClickedListener() {
            public void onActionClicked(Action action) {

                if (action.getId() == mPlayPauseAction.getId()) {
                    boolean actionIndex = mPlayPauseAction.getIndex() == PlayPauseAction.PLAY;
                    togglePlayback(actionIndex);
                    if (actionIndex) {
                        trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_PLAY);
                    }
                    else {
                        trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_PAUSE);
                    }
                }
                else if (action.getId() == mSkipNextAction.getId()) {
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_NEXT);
                    ContentBrowser.getInstance(getActivity()).verifyScreenSwitch(ContentBrowser
                                                                                         .CONTENT_RENDERER_SCREEN,
                                                                                 mRelatedContentList.get(mCurrentItem + 1), extra -> next(),
                                                                                 errorExtra ->
                                                                                         ContentBrowser.getInstance(getActivity()).showAuthenticationErrorDialog(errorExtra));
                }
                else if (action.getId() == mClosedCaptioningAction.getId()) {
                    toggleCloseCaption();
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_TOGGLE_CC);
                }
                else if (action.getId() == mSkipPreviousAction.getId()) {
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_PRE);
                    ContentBrowser.getInstance(getActivity()).verifyScreenSwitch(ContentBrowser
                                                                                         .CONTENT_RENDERER_SCREEN,
                                                                                 mRelatedContentList.get(mCurrentItem - 1), extra -> prev(),
                                                                                 errorExtra ->
                                                                                         ContentBrowser.getInstance(getActivity()).showAuthenticationErrorDialog(errorExtra));
                }
                else if (action.getId() == mFastForwardAction.getId()) {
                    fastForward();
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_FF);
                }
                else if (action.getId() == mRewindAction.getId()) {
                    fastRewind();
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_REWIND);
                }
                else if (action instanceof PlaybackControlsRow.MultiAction) {
                    ((PlaybackControlsRow.MultiAction) action).nextIndex();
                    notifyChanged(action);
                    trackAnalyticsAction(AnalyticsTags.ACTION_PLAYBACK_CONTROL_MULTI_ACTION);
                }
            }
        });

        playbackControlsRowPresenter.setBackgroundColor(
                ContextCompat.getColor(getActivity(), R.color.lb_playback_background_color));

        playbackControlsRowPresenter.setProgressColor(
                ContextCompat.getColor(getActivity(), R.color.lb_playback_progress_color_no_theme));

        // Secondary actions when *not hidden* shall not display the ellipsis.
        playbackControlsRowPresenter.setSecondaryActionsHidden(!mHideMoreActions);

        ps.addClassPresenter(PlaybackControlsRow.class, playbackControlsRowPresenter);

        ListRowPresenter listRowPresenter = new ListRowPresenter();
        listRowPresenter.setHeaderPresenter(new RowHeaderPresenter());
        ps.addClassPresenter(ListRow.class, listRowPresenter);

        mRowsAdapter = new ArrayObjectAdapter(ps);

        addPlaybackControlsRow();

        if (mShowRelatedContent) {
            addOtherRows();
        }
        Log.d(TAG, "Hide more actions ? " + mHideMoreActions);
        // Checking here ensures that the secondary actions adapter is already be setup.
        if (mHideMoreActions) {
            if (mSecondaryActionsAdapter != null) {
                mSecondaryActionsAdapter.clear();
            }
        }

        setAdapter(mRowsAdapter);
    }

    /**
     * Helper method to track a playback action.
     *
     * @param action The playback action to track.
     */
    private void trackAnalyticsAction(String action) {

        trackAnalyticsAction(action, mSelectedContent);
    }


    /**
     * Helper method to track a playback action.
     *
     * @param action  The playback action to track.
     * @param content The content.
     */
    private void trackAnalyticsAction(String action, Content content) {

        if (isAdded() && getActivity() != null) {

            AnalyticsHelper.trackPlaybackControlAction(action, content,
                                                       ((PlaybackActivity) getActivity())
                                                               .getCurrentPosition());
        }
    }

    /**
     * Toggles play-pause state fo the current playback.
     *
     * @param playPause Pause the video if true, else plays it.
     */
    public void togglePlaybackUI(boolean playPause) {

        Log.d(TAG, "playpause in togglePlaybackUI? " + playPause);
        if (playPause) {
            setFadingEnabled(true);
            mPlayPauseAction.setIndex(PlayPauseAction.PAUSE);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PAUSE));
            notifyChanged(mPlayPauseAction);
        }
        else {
            setFadingEnabled(false);
            mPlayPauseAction.setIndex(PlayPauseAction.PLAY);
            mPlayPauseAction.setIcon(mPlayPauseAction.getDrawable(PlayPauseAction.PLAY));
            notifyChanged(mPlayPauseAction);
        }
        int currentTime = mCallback.getCurrentPosition();
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(mCallback.getBufferProgressPosition());
    }

    /**
     * Toggles play-pause state fo the current playback and updates the fragment.
     *
     * @param playPause Pause the video if true, else plays it.
     */
    public void togglePlayback(boolean playPause) {

        togglePlaybackUI(playPause);
        mCallback.onFragmentPlayPause(playPause);
    }

    /**
     * Triggers update of playback row to {@link #mCurrentItem}.
     */
    public void updatePlayback() {

        updatePlaybackRow(mCurrentItem);
    }

    private void addPlaybackControlsRow() {

        Log.d(TAG, "Show details ? " + SHOW_DETAIL);
        if (SHOW_DETAIL) {
            mPlaybackControlsRow = new PlaybackControlsRow(mSelectedContent);
        }
        else {
            mPlaybackControlsRow = new PlaybackControlsRow();
        }
        mRowsAdapter.add(mPlaybackControlsRow);

        updatePlaybackRow(mCurrentItem);

        ControlButtonPresenterSelector presenterSelector = new ControlButtonPresenterSelector();
        mPrimaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mSecondaryActionsAdapter = new ArrayObjectAdapter(presenterSelector);
        mPlaybackControlsRow.setPrimaryActionsAdapter(mPrimaryActionsAdapter);
        mPlaybackControlsRow.setSecondaryActionsAdapter(mSecondaryActionsAdapter);

        mPlayPauseAction = new PlayPauseAction(mContext);

        RepeatAction mRepeatAction = new RepeatAction(mContext);
        ThumbsUpAction mThumbsUpAction = new ThumbsUpAction(mContext);
        ThumbsDownAction mThumbsDownAction = new ThumbsDownAction(mContext);
        ShuffleAction mShuffleAction = new ShuffleAction(mContext);
        mSkipNextAction = new SkipNextAction(mContext);
        mSkipPreviousAction = new SkipPreviousAction(mContext);
        mFastForwardAction = new FastForwardAction(mContext);
        mRewindAction = new RewindAction(mContext);
        mClosedCaptioningAction = new PlaybackControlsRow.ClosedCaptioningAction(mContext);

        mCloseCaptionOnDrawable =
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_closed_caption_on);
        mCloseCaptionOffDrawable =
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_closed_caption_off);
        mCloseCaptionDisabledDrawable =
                ContextCompat.getDrawable(getActivity(), R.drawable.ic_closed_caption_disabled);

        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsUpAction);
        }
        else {
            mSecondaryActionsAdapter.add(mThumbsUpAction);
        }
        mPrimaryActionsAdapter.add(mSkipPreviousAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new RewindAction(mContext));
        }
        mPrimaryActionsAdapter.add(mPlayPauseAction);
        if (PRIMARY_CONTROLS > 3) {
            mPrimaryActionsAdapter.add(new FastForwardAction(mContext));
        }
        mPrimaryActionsAdapter.add(mSkipNextAction);

        // Adding closed caption action button.
        mPrimaryActionsAdapter.add(mClosedCaptioningAction);

        // Disable CC button initially.
        updateCCButtonState(false, false);

        mSecondaryActionsAdapter.add(mRepeatAction);
        mSecondaryActionsAdapter.add(mShuffleAction);
        if (PRIMARY_CONTROLS > 5) {
            mPrimaryActionsAdapter.add(mThumbsDownAction);
        }
        else {
            mSecondaryActionsAdapter.add(mThumbsDownAction);
        }
        mSecondaryActionsAdapter.add(new PlaybackControlsRow.HighQualityAction(mContext));
    }

    /**
     * Update state of CC button.
     *
     * @param state              New state.
     * @param isContentSupportCC The state of if closed captions is supported or not.
     */
    public void updateCCButtonState(boolean state, boolean isContentSupportCC) {

        mCCButtonState = state;
        // Leanback CC button state is reversed?
        if (state) {
            mClosedCaptioningAction.setIcon(mCloseCaptionOnDrawable);
            Log.d(TAG, "CC On State");
            mCCButtonDisabled = false;
        }
        else {
            if (!isContentSupportCC) {
                mClosedCaptioningAction.setIcon(mCloseCaptionDisabledDrawable);
                mCCButtonDisabled = true;
                Log.d(TAG, "CC Disabled State");
            }
            else {
                mClosedCaptioningAction.setIcon(mCloseCaptionOffDrawable);
                Log.d(TAG, "CC Off State");
                mCCButtonDisabled = false;
            }
        }
        notifyChanged(mClosedCaptioningAction);
    }

    /**
     * Toggle player's CC state, triggered by CC action/button.
     */
    private void toggleCloseCaption() {

        if (mCCButtonDisabled) {
            Log.d(TAG, "CC button is disabled so not toggling the state:" + mCCButtonState);
            return;
        }

        mCCButtonState = !mCCButtonState;
        Log.d(TAG, "toggleCloseCaption to " + mCCButtonState);
        if (mCallback != null) {
            mCallback.onCloseCaptionButtonStateChanged(mCCButtonState);
        }
    }

    /**
     * {@inheritDoc}
     */
    protected String getVideoTitle() {

        return mSelectedContent.getTitle();
    }

    /**
     * {@inheritDoc}
     */
    protected String getVideoSubtitle() {

        return ContentHelper.getDescriptiveSubtitle(mContext, mSelectedContent);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    protected void customActionsOnAttachedToWindow() {

        setActionsInitialStates();
    }

    private void setActionsInitialStates() {
        // If current content is the first content in the list, disable Previous Action.
        setSkipPreviousActionEnabled(mCurrentItem > 0);
        // If current content is the last content in the list, disable Next Action.
        setSkipNextActionEnabled(mCurrentItem < (mRelatedContentList.size() - 1));
        // If the current content is live, disable fast-forward and rewind action.
        disableActionsForLiveContent(mSelectedContent.getExtraValueAsBoolean(Content.LIVE_TAG));
    }

    /**
     * Disables the fast forward and rewind action buttons if the selected content is live.
     * @param isLive True if the content is live; false otherwise.
     */
    private void disableActionsForLiveContent(boolean isLive) {

        setFastForwardActionEnabled(!isLive);
        setRewindActionEnabled(!isLive);
    }

    private void notifyChanged(Action action) {

        ArrayObjectAdapter adapter = mPrimaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
            return;
        }
        adapter = mSecondaryActionsAdapter;
        if (adapter.indexOf(action) >= 0) {
            adapter.notifyArrayItemRangeChanged(adapter.indexOf(action), 1);
        }
    }

    private void updatePlaybackRow(int index) {

        if (mPlaybackControlsRow.getItem() != null) {
            Content item = (Content) mPlaybackControlsRow.getItem();
            if (!mRelatedContentList.isEmpty() && mCurrentItem >= 0) {
                item.setTitle(mRelatedContentList.get(mCurrentItem).getTitle());
                item.setStudio(mRelatedContentList.get(mCurrentItem).getStudio());
            }
        }
        else {
            Log.e(TAG, "mPlaybackControlRow.getItem() is null in updatePlaybackRow");
        }
        if (SHOW_IMAGE) {
            if (!mRelatedContentList.isEmpty() && mCurrentItem >= 0) {
                updateVideoImage(mRelatedContentList.get(mCurrentItem).getCardImageUrl());
            }
        }
        mRowsAdapter.notifyArrayItemRangeChanged(0, 1);
        mDuration = mCallback.getDuration();
        mPlaybackControlsRow.setTotalTime(mDuration);
        mPlaybackControlsRow.setCurrentTime(0);
        mPlaybackControlsRow.setBufferedProgress(0);
    }

    private void addOtherRows() {

        ArrayObjectAdapter listRowAdapter = new ArrayObjectAdapter(new CardPresenter());
        for (Content content : mRelatedContentList) {
            // Do not show the selected content under recommendation list.
            if (!StringManipulation.areStringsEqual(content.getId(), mSelectedContent.getId())) {
                listRowAdapter.add(content);
            }
        }
        HeaderItem header = new HeaderItem(0, getString(R.string.related_contents));
        mRowsAdapter.add(new ListRow(header, listRowAdapter));
    }

    private int getUpdatePeriod() {

        return DEFAULT_UPDATE_PERIOD;
    }

    /**
     * Starts progress automation
     */
    public void startProgressAutomation() {

        if (mHandler != null && mRunnable != null) {
            updateUI();
            mHandler.removeCallbacks(mRunnable);
            mHandler.postDelayed(mRunnable, getUpdatePeriod());
        }
    }

    private void next() {

        // Current content is already the last content in list.
        if (mCurrentItem == (mRelatedContentList.size() - 1)) {
            Log.d(TAG, "Current content is the last content");
            setSkipNextActionEnabled(false);
            // This will close the playback activity post playback completion of the last content
            // in the list.
            ((Activity) mCallback).finish();
            return;
        }
        // Update current content.
        ++mCurrentItem;
        mCallback.changeContent(mRelatedContentList.get(mCurrentItem));

        // The new 'current' content is the last in list, disabling the next button.
        if (mCurrentItem == (mRelatedContentList.size() - 1)) {
            Log.d(TAG, "The selected content is the last content, so disable next button");
            setSkipNextActionEnabled(false);
        }

        // The new current content is not the first content in list, enabling the previous button.
        if (mCurrentItem > 0) {
            setSkipPreviousActionEnabled(true);
            Log.d(TAG, "Current content is not the first content, so enable prev button");
        }

        disableActionsForLiveContent(mRelatedContentList.get(mCurrentItem)
                                                        .getExtraValueAsBoolean(Content.LIVE_TAG));

        mFfwRwdSpeed = INITIAL_SPEED;
        updatePlaybackRow(mCurrentItem);
    }

    private void prev() {

        // Current content is already the first content in the list.
        if (mCurrentItem <= 0) {
            Log.d(TAG, "Current content is already the first content");
            setSkipPreviousActionEnabled(false);
            return;
        }
        --mCurrentItem;
        mCallback.changeContent(mRelatedContentList.get(mCurrentItem));

        // The new 'current' content is the first in list, disabling the pre button.
        if (mCurrentItem == 0) {
            Log.d(TAG, "The selected content is the first content, so disable prev button");
            setSkipPreviousActionEnabled(false);
        }

        // Current content is not the last content in list, enabling the next button
        if (mCurrentItem < (mRelatedContentList.size() - 1)) {
            Log.d(TAG, "Current content is not the last content, so enable next button");
            setSkipNextActionEnabled(true);
        }

        disableActionsForLiveContent(mRelatedContentList.get(mCurrentItem)
                                                        .getExtraValueAsBoolean(Content.LIVE_TAG));

        mFfwRwdSpeed = INITIAL_SPEED;
        updatePlaybackRow(mCurrentItem);
    }

    /**
     * Fast forward video.
     */
    public void fastForward() {

        startClickTrackingTimer();
        int currentTime = mCallback.getCurrentPosition() + mFfwRwdSpeed;
        if (currentTime > mDuration) {
            currentTime = mDuration;
        }
        seekTo(currentTime);
    }

    /**
     * Fast rewind video.
     */
    public void fastRewind() {

        startClickTrackingTimer();
        int currentTime = mCallback.getCurrentPosition() - mFfwRwdSpeed;
        if (currentTime < 0 || currentTime > mDuration) {
            currentTime = 0;
        }
        seekTo(currentTime);
    }

    private void seekTo(int currentTime) {

        tickle();
        mCallback.onFragmentFfwRwd(currentTime);
        mPlaybackControlsRow.setCurrentTime(currentTime);
        mPlaybackControlsRow.setBufferedProgress(mCallback.getBufferProgressPosition());
    }

    /**
     * Stops automation.
     */
    public void stopProgressAutomation() {

        if (mHandler != null && mRunnable != null) {
            mHandler.removeCallbacks(mRunnable);
        }
    }

    /**
     * Performs actions required on finishing playback.
     */
    public void playbackFinished() {

        togglePlaybackUI(false);
        next();
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onStop() {

        stopProgressAutomation();
        super.onStop();
    }

    private void updateVideoImage(String uri) {

        SimpleTarget<GlideDrawable> simpleTarget = new SimpleTarget<GlideDrawable>(CARD_WIDTH,
                                                                                   CARD_HEIGHT) {
            @Override
            public void onResourceReady(GlideDrawable resource, GlideAnimation<? super
                    GlideDrawable> glideAnimation) {

                mPlaybackControlsRow.setImageDrawable(resource);
                mRowsAdapter.notifyArrayItemRangeChanged(0, mRowsAdapter.size());
            }
        };

        GlideHelper.loadImageIntoSimpleTarget(mContext, uri, new GlideHelper.LoggingListener<>(),
                                              simpleTarget);


    }

    private void startClickTrackingTimer() {

        if (null != mClickTrackingTimer) {
            mClickCount++;
            mClickTrackingTimer.cancel();
        }
        else {
            mClickCount = 0;
            mFfwRwdSpeed = INITIAL_SPEED;
        }
        mClickTrackingTimer = new Timer();
        mClickTrackingTimer.schedule(new UpdateFfwRwdSpeedTask(), CLICK_TRACKING_DELAY);
    }

    /**
     * Container Activity must implement this interface
     */
    public interface OnPlayPauseClickedListener {

        void onFragmentPlayPause(boolean playPause);

        void onFragmentFfwRwd(int position);

        int getDuration();

        int getCurrentPosition();

        int getBufferProgressPosition();

        void changeContent(Content content);

        void onCloseCaptionButtonStateChanged(boolean state);
    }

    private static class DescriptionPresenter extends AbstractDetailsDescriptionPresenter {

        @Override
        protected void onBindDescription(ViewHolder viewHolder, Object item) {

            viewHolder.getTitle().setText(((Content) item).getTitle());
            viewHolder.getSubtitle().setText(((Content) item).getStudio());
        }
    }

    private class UpdateFfwRwdSpeedTask extends TimerTask {

        @Override
        public void run() {

            mClickTrackingHandler.post(new Runnable() {
                @Override
                public void run() {

                    if (mClickCount == 0) {
                        mFfwRwdSpeed = INITIAL_SPEED;
                    }
                    else if (mClickCount == 1) {
                        mFfwRwdSpeed *= 2;
                    }
                    else if (mClickCount >= 2) {
                        mFfwRwdSpeed *= 4;
                    }
                    mClickCount = 0;
                    mClickTrackingTimer = null;
                }
            });
        }
    }

    private final class ItemViewClickedListener implements OnItemViewClickedListener {

        /**
         * Change to recommended content user selected with handling of next/prev buttons.
         *
         * @param content Recommended content user selected.
         */
        private void changeToRecommendedContent(Content content) {

            mCallback.changeContent(content);

            // Find the index of the selected content in the related content list only if
            // recommendations are enabled.
            mCurrentItem = getCurrentContentIndex(content);

            // check for the next button state.
            if (mCurrentItem == (mRelatedContentList.size() - 1)) {
                Log.d(TAG, "The selected content is the last content, so disable next button");
                setSkipNextActionEnabled(false);
            }
            else {
                Log.d(TAG, "Current content is not the last content, so enable next button");
                setSkipNextActionEnabled(true);
            }

            // check for the previous button state.
            if (mCurrentItem == 0) {
                Log.d(TAG, "The selected content is the first content, so disable prev button");
                setSkipPreviousActionEnabled(false);
            }
            else {
                Log.d(TAG, "Current content is not the first content, so enable prev button");
                setSkipPreviousActionEnabled(true);
            }

            disableActionsForLiveContent(content.getExtraValueAsBoolean(Content.LIVE_TAG));
        }

        @Override
        public void onItemClicked(Presenter.ViewHolder itemViewHolder, Object item,
                                  RowPresenter.ViewHolder rowViewHolder, Row row) {

            if (item instanceof Content) {
                Content content = (Content) item;
                trackAnalyticsAction(AnalyticsTags.ACTION_RECOMMENDED_CONTENT_CLICKED, content);

                ContentBrowser.getInstance(getActivity()).verifyScreenSwitch(ContentBrowser
                                                                                     .CONTENT_RENDERER_SCREEN,
                                                                             content,
                                                                             extra ->
                                                                                     changeToRecommendedContent(content),
                                                                             errorExtra ->
                                                                                     ContentBrowser.getInstance(getActivity()).showAuthenticationErrorDialog(errorExtra));
            }
        }
    }

    /**
     * Implementation of OnMediaSessionControllerCallback
     */
    @Override
    public void onMediaSessionPlayPause(boolean playPause) {

        togglePlayback(playPause);
    }

    /**
     * Implementation of OnMediaSessionControllerCallback
     */
    @Override
    public void onMediaSessionSeekTo(int position) {

        if (position < 0) {
            position = 0;
        }
        else if (position > mDuration) {
            position = mDuration;
        }
        seekTo(position);
    }

    /**
     * Implementation of OnMediaSessionControllerCallback
     */
    @Override
    public void onMediaSessionSkipToNext() {

        next();
    }

    /**
     * Implementation of OnMediaSessionControllerCallback
     */
    @Override
    public void onMediaSessionSkipToPrev() {

        prev();
    }

    /**
     * Implementation of OnMediaSessionControllerCallback
     */
    @Override
    public int getCurrentPosition() {

        int position = 0;
        if (mCallback != null) {
            position = mCallback.getCurrentPosition();
        }
        return position;
    }
}
