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
package com.amazon.ads.android.freewheel;

import com.amazon.ads.IAds;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.SystemClock;
import android.util.Log;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;

import tv.freewheel.ad.AdManager;
import tv.freewheel.ad.interfaces.IAdContext;
import tv.freewheel.ad.interfaces.IAdManager;
import tv.freewheel.ad.interfaces.IConstants;
import tv.freewheel.ad.interfaces.IEvent;
import tv.freewheel.ad.interfaces.IEventListener;
import tv.freewheel.ad.interfaces.ISlot;

/**
 * FreeWheelAds implementation.
 */
public class FreeWheelAds implements IAds {

    /**
     * Debug TAG.
     */
    private final static String TAG = FreeWheelAds.class.getSimpleName();

    /**
     * Name used for implementation creator registration to Module Manager.
     */
    public final static String IMPL_CREATOR_NAME = FreeWheelAds.class.getSimpleName();

    /**
     * Timeout value in ms.
     */
    private final static int REQUEST_TIMEOUT = 3000;

    /**
     * Internal reference to Activity.
     */
    private Activity mActivity;

    /**
     * Frame layout reference.
     */
    private FrameLayout mFrameLayout;

    /**
     * FreeWheel ad manager.
     */
    private IAdManager mAdManager;

    /**
     * FreeWheel context.
     */
    private IAdContext mAdContext;

    /**
     * FreeWheel AdsConstants.
     */
    private IConstants mAdConstants;

    /**
     * Ad event interface.
     */
    private IAdsEvents mIAdsEvents;

    /**
     * FreeWheel server url.
     */
    private String mAdUrl = "http://demo.v.fwmrm.net/";

    /**
     * FreeWheel network id.
     */
    private int mNetworkId = 90750;

    /**
     * FreeWheel profile.
     */
    private String mProfile = "3pqa_android";

    /**
     * FreeWheel site section.
     */
    private String mSiteSectionId = "3pqa_section_nocbp";

    /**
     * FreeWheel current video ad id type.
     */
    private String mVideoIdType = "custom"; // or "fw"

    /**
     * FreeWheel current video ad id.
     */
    private String mCurrentVideoAdId = "3pqa_video";

    /**
     * Current video duration.
     */
    private long mCurrentVideoDuration = 1000;

    /**
     * List for PreRoll ad slots.
     */
    private List<ISlot> mPreRollSlots = new ArrayList<>();

    /**
     * List for MidRoll ad slots.
     */
    private List<ISlot> mMidRollSlots = new ArrayList<>();

    /**
     * Ordered set for getting ad slots before a given video position.
     */
    private TreeSet<Long> mMidRollAdsSegment;

    /**
     * List for MidRoll ad slot played flags.
     */
    private List<Boolean> mMidrollSlotsIsPlayedList = new ArrayList<>();

    /**
     * Ad start time.
     */
    private long mAdSlotStartTime;

    /**
     * Flag to keep track of current Ad state.
     */
    private boolean mAdInProgress;

    /**
     * Init FreeWheel instance.
     *
     * @param context     Activity which Ads consumed in.
     * @param frameLayout Layout for Ads.
     * @param extras      Extra bundle to pass through data.
     */
    @Override
    public void init(Context context, FrameLayout frameLayout, Bundle extras) {
        // Set internal frameLayout reference.
        mFrameLayout = frameLayout;
        // Set internal Activity reference.
        mActivity = (Activity) context;
        // Set extras
        mExtras = extras;

        mAdUrl = context.getString(R.string.ad_url);
        mNetworkId = context.getResources().getInteger(R.integer.ad_network_id);
        mProfile = context.getString(R.string.profile);
        mSiteSectionId = context.getString(R.string.site_section_id);
        mVideoIdType = context.getString(R.string.video_id_type);

        Log.v(TAG, "AdUrl is " + mAdUrl);
        Log.v(TAG, "AdNetworkId is " + mNetworkId);
        Log.v(TAG, "Profile is " + mProfile);
        Log.v(TAG, "SiteSectionId is " + mSiteSectionId);
        Log.v(TAG, "VideoIdType is " + mVideoIdType);

        // Create an ad manager instance.
        mAdManager = AdManager.getInstance(mActivity.getApplicationContext());
        // Set FreeWheel ad server url.
        mAdManager.setServer(mAdUrl);
        // Set FreeWheel network id.
        mAdManager.setNetwork(mNetworkId);
    }

    /**
     * Set ad events interface.
     *
     * @param iAdsEvents AdsEvents interface
     */
    @Override
    public void setIAdsEvents(IAdsEvents iAdsEvents) {

        mIAdsEvents = iAdsEvents;
    }

    /**
     * Set Current video position to detect mid roll ad slots.
     *
     * @param position Current video position.
     */
    @Override
    public void setCurrentVideoPosition(double position) {

        Log.d(TAG, "set current video pos:" + position);

        if (mAdContext != null && mMidRollSlots != null && mMidRollSlots.size() > 0) {
            int i = 0;
            // Traverse mid roll slots.
            for (ISlot slot : mMidRollSlots) {
                Log.d(TAG, "Mid roll " + i + " played=" + mMidrollSlotsIsPlayedList.get(i) +
                        " " +
                        "compare :" + slot.getTimePosition() + " == " + (int) (position /
                        1000));
                // If time is matching and mid roll is not played before then play ad slot.
                if (!mMidrollSlotsIsPlayedList.get(i) && slot.getTimePosition() == (int)
                        (position / 1000)) {
                    Log.d(TAG, "Mid roll matches at:" + position);
                    mMidrollSlotsIsPlayedList.set(i, true);
                    startSlot(IAds.MID_ROLL_AD);
                    slot.play();
                    break;
                }
                i++;
            }
        }
    }

    /**
     * Return true if post roll ads are available; false otherwise. Currently this component does
     * not support post roll ads so this method always returns false.
     *
     * @return False because post roll ads are not supported.
     */
    @Override
    public boolean isPostRollAvailable() {

        return false;
    }

    /**
     * Set activity state.
     *
     * @param activityState Activity state.
     */
    @Override
    public void setActivityState(ActivityState activityState) {

        int state = -1;

        if (mAdContext != null) {
            switch (activityState) {
                case START:
                    state = mAdConstants.ACTIVITY_STATE_START();
                    break;
                case RESUME:
                    state = mAdConstants.ACTIVITY_STATE_RESUME();
                    break;
                case PAUSE:
                    state = mAdConstants.ACTIVITY_STATE_PAUSE();
                    break;
                case STOP:
                    state = mAdConstants.ACTIVITY_STATE_STOP();
                    break;
                case DESTROY:
                    state = mAdConstants.ACTIVITY_STATE_DESTROY();
                    break;
            }

            mAdContext.setActivityState(state);

            if(activityState == ActivityState.DESTROY) {
                cleanUpAdContext();
            }
        }
    }

    /**
     * Set Player state.
     *
     * @param playerState Player state.
     */
    @Override
    public void setPlayerState(PlayerState playerState) {

        int state = -1;

        if (mAdContext != null) {
            switch (playerState) {
                case PLAYING:
                    state = mAdConstants.VIDEO_STATE_PLAYING();
                    break;
                case PAUSED:
                    state = mAdConstants.VIDEO_STATE_PAUSED();
                    break;
                case COMPLETED:
                    state = mAdConstants.VIDEO_STATE_COMPLETED();
                    break;
            }
            mAdContext.setVideoState(state);
        }
    }

    /**
     * {@inheritDoc}
     * Show pre roll Ads in the FrameLayout provided.
     */
    public void showAds() {

        Log.d(TAG, "showAds called");
        // Ad context is available, no need to cleanup and start with pre-roll ads.
        if (mAdContext != null && mAdInProgress) {

            if (mIAdsEvents != null) {
                mIAdsEvents.onAdSlotStarted(getBasicAdDetailBundle());
            }
            return;
        }
        //Start with a new ad context and pre roll ads.
        showPreRollAds();
    }

    /**
     * Clean up previous ad context.
     */
    private void cleanUpAdContext() {

        Log.d(TAG, "entered cleanUpAdContext ");
        if (mAdContext != null) {
            mAdContext.dispose();
        }
        mAdContext = null;
        mMidRollAdsSegment = new TreeSet<>();
        mAdInProgress = false;
    }

    /**
     * Show pre roll Ads in the FrameLayout provided.
     */
    private void showPreRollAds() {

        // AdContext is single use so get rid of it.
        cleanUpAdContext();

        // Get extra video data.
        try {
            Bundle videoBundle = mExtras.getBundle("video");
            assert (videoBundle != null);
            mCurrentVideoAdId = String.valueOf(videoBundle.getString("adId"));
            mCurrentVideoDuration = videoBundle.getLong("duration");
            // Ad cue points are coming from FreeWheel profile for a certain video id.
            // Keeping the code below for reference.
            // int[] adCuePoints = videoBundle.getIntArray("adCuePoints");
        }
        catch (Exception e) {
            Log.e(TAG, "Using default video info!!!", e);
        }

        // Each AdContext instance can only submit ad request once.
        // Setup Ads Context
        mAdContext = mAdManager.newContext();
        mAdConstants = mAdContext.getConstants();

        // Set FreeWheel profile.
        mAdContext.setProfile(mProfile, null, null, null);
        // Set FreeWheel site section.
        mAdContext.setSiteSection(mSiteSectionId,
                                  (int) Math.floor(Math.random() * Integer.MAX_VALUE),
                                  0,
                                  mAdConstants.ID_TYPE_CUSTOM(),
                                  0);

        // Find the video id type.
        int intVideoIdType = mAdConstants.ID_TYPE_CUSTOM();
        if (mVideoIdType.equals("fw")) {
            intVideoIdType = mAdConstants.ID_TYPE_FW();
        }

        // Set video asset.
        mAdContext.setVideoAsset(mCurrentVideoAdId,
                                 mCurrentVideoDuration / 1000,
                                 null,
                                 mAdConstants.VIDEO_ASSET_AUTO_PLAY_TYPE_ATTENDED(),
                                 (int) Math.floor(Math.random() * Integer.MAX_VALUE),
                                 0,
                                 intVideoIdType,
                                 0,
                                 mAdConstants.VIDEO_ASSET_DURATION_TYPE_EXACT());

        // Set context activity.
        mAdContext.setActivity(mActivity);
        // Let AdContext render in the frame layout provided.
        mAdContext.registerVideoDisplayBase(mFrameLayout);

        // Add event listener for request complete event.
        mAdContext.addEventListener(mAdContext.getConstants().EVENT_REQUEST_COMPLETE(), new
                IEventListener() {
                    public void run(IEvent e) {

                        Log.d(TAG, "EVENT_REQUEST_COMPLETE");

                        String eType = e.getType();
                        String eSuccess = e.getData().get(mAdConstants.INFO_KEY_SUCCESS())
                                           .toString();

                        if (mAdConstants != null) {
                            if (mAdConstants.EVENT_REQUEST_COMPLETE().equals(eType) &&
                                    Boolean.valueOf(eSuccess)) {
                                Log.d(TAG, "Request completed successfully");
                                handleAdManagerRequestComplete();
                            }
                            else {
                                Log.e(TAG, "Request failed. Playing main content.");
                                // Ad request failed, continue with the content.
                                startSlot(null);
                                endSlot(null);
                                cleanUpAdContext();
                            }
                        }
                    }
                });

        // Create a new thread.
        Thread t = new Thread(new Runnable() {
            @Override
            public void run() {
                // Submit request, it must be in a separate thread.
                mAdContext.submitRequest(REQUEST_TIMEOUT);
            }
        });
        // Start thread.
        t.start();
    }

    /**
     * Handle ad manager request complete event.
     */
    private void handleAdManagerRequestComplete() {

        Log.d(TAG, "Playing preroll slots");
        // Get pre roll ad slots.
        mPreRollSlots = mAdContext.getSlotsByTimePositionClass(
                mAdConstants.TIME_POSITION_CLASS_PREROLL());
        // Get mid roll ad slots.
        mMidRollSlots = mAdContext.getSlotsByTimePositionClass(
                mAdConstants.TIME_POSITION_CLASS_MIDROLL());

        // If there are mid roll slots to play set their flags false initially.
        if (mMidRollSlots.size() > 0) {
            int i = 0;
            for (ISlot slot : mMidRollSlots) {
                Log.d(TAG, "Midroll position " + i + ":" + (int) slot.getTimePosition());
                mMidrollSlotsIsPlayedList.add(i, false);
            }
        }

        // Start listening slot start message.
        mAdContext.addEventListener(mAdConstants.EVENT_SLOT_STARTED(), new IEventListener() {
            public void run(IEvent e) {
                mAdInProgress = true;
            }
        });

        // Start listening slot end message.
        mAdContext.addEventListener(mAdConstants.EVENT_SLOT_ENDED(), new IEventListener() {
            public void run(IEvent e) {

                String completedSlotID =
                        (String) e.getData().get(mAdConstants.INFO_KEY_CUSTOM_ID());

                ISlot completedSlot = mAdContext.getSlotByCustomId(completedSlotID);
                Log.d(TAG, "Completed playing slot: " + completedSlotID);

                mAdInProgress = false;

                // EVENT_SLOT_ENDED could be fired for several types of slots
                // (pre-, mid-, post-, pause, overlay, display)
                if (completedSlot.getTimePositionClass() ==
                        mAdConstants.TIME_POSITION_CLASS_PREROLL()) {
                    playNextPreroll();
                }

                if (completedSlot.getTimePositionClass() ==
                        mAdConstants.TIME_POSITION_CLASS_MIDROLL()) {
                    // Set mid roll flag and end the ad slot.
                    endSlot(IAds.MID_ROLL_AD);
                }
            }
        });

        // Play next pre roll slot.
        playNextPreroll();
    }

    /**
     * Plays next pre roll slot if it exists.
     */
    private void playNextPreroll() {

        if (mPreRollSlots != null) {
            if (mPreRollSlots.size() > 0) {
                ISlot nextSlot = mPreRollSlots.remove(0);
                Log.d(TAG, "Playing preroll slot: " + nextSlot.getCustomId());
                // Play next pre roll slot.
                startSlot(IAds.PRE_ROLL_AD);
                nextSlot.play();
            }
            else {
                Log.d(TAG, "Finished all prerolls. Starting main content.");
                endSlot(IAds.PRE_ROLL_AD);
            }
        }
        else {
            endSlot(null);
        }
    }

    /**
     * Start Ad slot.
     * Notifies listener and captures ad slot start time.
     *
     * @param adType type of ad currently being played
     */
    private void startSlot(final String adType) {

        new Handler(mActivity.getMainLooper()).post(new Runnable() {
            public void run() {
                // Let listener know about Ad slot start event.
                if (mIAdsEvents != null) {
                    // Put ad metadata in a Bundle.
                    Bundle extras = getBasicAdDetailBundle();
                    if(adType != null) {
                        extras.putString(AD_TYPE, adType);
                    }
                    mIAdsEvents.onAdSlotStarted(extras);
                }
            }
        });

        // Capture ad start time.
        mAdSlotStartTime = SystemClock.elapsedRealtime();
    }

    /**
     * End Ad slot.
     * Notifies listener, sends duration and disposes AdContext.
     *
     * @param adType type of ad currently being played
     */
    private void endSlot(final String adType) {

        new Handler(mActivity.getMainLooper()).post(new Runnable() {
            public void run() {

                if (mIAdsEvents != null) {
                    // Calculate how long Ads played.
                    long adSlotTime = SystemClock.elapsedRealtime() - mAdSlotStartTime;
                    // Put calculated time and ad metadata in a Bundle.
                    Bundle extras = getBasicAdDetailBundle();
                    extras.putLong(DURATION_PLAYED, adSlotTime);
                    if(adType != null) {
                        extras.putString(AD_TYPE, adType);
                    }
                    // Let listener know about Ad slot stop event.
                    mIAdsEvents.onAdSlotEnded(extras);
                }
            }
        });
    }

    /**
     * Store pass through data in here.
     */
    private Bundle mExtras;

    /**
     * {@inheritDoc}
     * Set pass through data.
     *
     * @param extra Pass through bundle
     */
    public void setExtra(Bundle extra) {

        mExtras = extra;
    }

    /**
     * Get pass through data.
     *
     * @return Pass through bundle
     */
    public Bundle getExtra() {

        return mExtras;
    }

    @Override
    public int getNumberOfSegments(){

        if(mMidRollSlots != null)
        {
            return mMidRollSlots.size() + 1;
        }
        return 1;
    }

    /**
     * Get the current segment number of the content based on the mid roll ads list.
     *
     * @param position playback location of current Content.
     * @param duration total duration of the current Content.
     * @return the current segment of the content media. Start with value 1 and based on mid roll
     * ads.
     */
    public int getCurrentContentSegmentNumber(long position, long duration) {

        if (mMidRollAdsSegment.isEmpty() && mMidRollSlots != null && !mMidRollSlots.isEmpty()) {
            for (ISlot slot : mMidRollSlots) {
                mMidRollAdsSegment.add((long) slot.getTimePosition());
            }
        }
        if (!mMidRollAdsSegment.isEmpty()) {
            Long lowerValue = mMidRollAdsSegment.lower(position);
            if (lowerValue != null) {
                return mMidRollAdsSegment.headSet(lowerValue, true).size() + 1;
            }
        }
        return 1;
    }

    /**
     * provide basic ad details
     *
     * @return bundle containing ad details.
     */
    private Bundle getBasicAdDetailBundle() {

        Bundle extras = new Bundle();
        extras.putString(ID, mCurrentVideoAdId);
        return extras;
    }
}
