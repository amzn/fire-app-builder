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
package com.amazon.android.ads.vast;

import com.amazon.android.ads.vast.model.vast.ClickElement;
import com.amazon.android.ads.vast.model.vast.LinearAd;
import com.amazon.android.ads.vast.model.vast.VideoClicks;
import com.amazon.android.ads.vast.util.HttpTools;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;

import static android.support.v4.app.ActivityCompat.startActivity;

/**
 * This implementation handles various click uris available in a vast ad <VideoClicks> container
 * which includes opening an activity with web view for <ClickThrough> element.
 */
public class AdVideoClickHandler {

    private static final String TAG = AdVideoClickHandler.class.getSimpleName();

    /**
     * String to get the browser class name from resources.
     */
    private static final String BROWSER_CLASS_NAME = "browser_class_name";

    /**
     * String to identify the string resource.
     */
    private static final String STRING_RESOURCE = "string";

    /**
     * Key to set the click through url in intent.
     */
    private static final String URL_KEY = "url";

    private Context mContext;
    private String mClickThroughUri;
    private String mCustomClickUri;
    private String mClickTrackingUri;
    private OnWebBrowserActivityListener mAdPlayerCallback;
    private Class mWebViewClass;

    /**
     * Constructor.
     *
     * @param context Current activity context.
     * @param adPlayerCallback Callback of AdPlayer.
     */
    public AdVideoClickHandler(Context context, OnWebBrowserActivityListener adPlayerCallback) {

        mContext = context;
        mAdPlayerCallback = adPlayerCallback;
        setBrowserClassObject();
    }

    /**
     * Vast Ad Player must implement this interface
     */
    public interface OnWebBrowserActivityListener {

        void onWebBrowserActivityLaunch();
    }

    /**
     * Set Video Click URIs for given linear ad.
     *
     * @param linearAd Linear Ad for which we need to handle the VideoClicks.
     */
    void initVideoClickURIs(LinearAd linearAd) {

        if (linearAd != null) {

            VideoClicks videoClicks = linearAd.getVideoClicks();
            setClickThroughUri(getVideoClickUri(videoClicks, VideoClicks.CLICK_THROUGH_KEY));
            setCustomClickUri(getVideoClickUri(videoClicks, VideoClicks.CUSTOM_CLICK_KEY));
            setClickTrackingUri(getVideoClickUri(videoClicks, VideoClicks.CLICK_TRACKING_KEY));
        }
    }

    /**
     * Get uri of video click element of given type.
     *
     * @param videoClicks VideoClicks object containing click elements.
     * @param clickType   Click type for which uri has been requested.
     * @return The video click uri available in linear ad with given type.
     */
    private String getVideoClickUri(VideoClicks videoClicks, String clickType) {

        String videoClickUri = null;

        if (videoClicks != null) {
            if (videoClicks.getVideoClickElements().containsKey(clickType)) {

                ClickElement clickThrough = videoClicks.getVideoClickElements().get
                        (clickType);
                videoClickUri = clickThrough.getUri();
            }
        }
        return videoClickUri;
    }

    /**
     * Set the default browser class instance.
     */
    private void setBrowserClassObject() {

        int browserClassId = mContext.getResources().getIdentifier("browser_class_name",
                                                                   "string", mContext
                .getPackageName());

        if (browserClassId != 0) {
            String browserClass = mContext.getResources().getString(browserClassId);
            try {
                mWebViewClass = Class.forName(browserClass);
            }
            catch (ClassNotFoundException e) {
                Log.e(TAG, "Browser class not found", e);
            }
        }
    }

    /**
     * Create intent with uri and start web view activity.
     * Currently this is the default implementation provided from Fire App Builder. Customer can use
     * their own activity/web view/browser to handle the uri.
     *
     * @param url url of the web page need to be launched.
     */
    private void startWebViewActivity(String url) {

        if (mWebViewClass != null) {
            mAdPlayerCallback.onWebBrowserActivityLaunch();
            Intent intent = new Intent(mContext, mWebViewClass);
            intent.putExtra("url", url);
            startActivity((Activity) mContext, intent, null);
        }
    }

    /**
     * Handle Video click elements available in current ad.
     *
     * @param isBrowserSupportEnabled if customer configured web browser support.
     */
    void handleAdVideoClicks(boolean isBrowserSupportEnabled) {

        // ClickThrough and CustomClick elements should never be requested together
        // ClickThrough element opens a new web view/activity other two don't.
        List<String> videoClickURIs = new ArrayList<>();

        if (mClickThroughUri != null && isBrowserSupportEnabled) {
            // Handle ClickThrough uri here. Fire app builder default is opening with webView in
            // a new activity.
            Log.i(TAG, "Click Through Uri: " + mClickThroughUri);
            startWebViewActivity(mClickThroughUri);
        }
        else if (mCustomClickUri != null) {
            Log.i(TAG, "Custom Click Uri: " + mCustomClickUri);
            videoClickURIs.add(mCustomClickUri);
        }

        if (mClickTrackingUri != null) {
            Log.i(TAG, "Click Tracking Uri: " + mClickTrackingUri);
            videoClickURIs.add(mClickTrackingUri);
        }
        HttpTools.fireUrls(videoClickURIs);
    }

    /**
     * Get click through uri.
     *
     * @return The uri.
     */
    public String getClickThroughUri() {

        return mClickThroughUri;
    }

    /**
     * Set the click through uri.
     *
     * @param clickThroughUri The uri.
     */
    public void setClickThroughUri(String clickThroughUri) {

        mClickThroughUri = clickThroughUri;
    }

    /**
     * Get custom click uri.
     *
     * @return The uri.
     */
    public String getCustomClickUri() {

        return mCustomClickUri;
    }

    /**
     * Set the custom click uri.
     *
     * @param customClickUri The uri.
     */
    public void setCustomClickUri(String customClickUri) {

        mCustomClickUri = customClickUri;
    }

    /**
     * Get click tracking uri.
     *
     * @return The uri.
     */
    public String getClickTrackingUri() {

        return mClickTrackingUri;
    }

    /**
     * Set the click tracking uri.
     *
     * @param clickTrackingUri The uri.
     */
    public void setClickTrackingUri(String clickTrackingUri) {

        mClickTrackingUri = clickTrackingUri;
    }

    @Override
    public String toString() {

        return "AdsVideoClickHandler{" +
                "mClickThroughUri='" + mClickThroughUri + '\'' +
                ", mCustomClickUri='" + mCustomClickUri + '\'' +
                ", mClickTrackingUri='" + mClickTrackingUri + '\'' +
                '}';
    }
}
