/**
 * Copyright 2015-present Amazon.com, Inc. or its affiliates. All Rights Reserved.
 *
 * Licensed under the Amazon Software License (the "License");
 * you may not use this file except in compliance with the License.
 * A copy of the License is located at
 *
 *     https://aws.amazon.com/asl/
 *
 * or in the "license" file accompanying this file. This file is distributed
 * on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language governing
 * permissions and limitations under the License.
 */
package com.amazon.android.minibrowser;

import com.amazon.android.webkit.AmazonCookieManager;
import com.amazon.android.webkit.AmazonSslErrorHandler;
import com.amazon.android.webkit.AmazonWebChromeClient;
import com.amazon.android.webkit.AmazonWebKitFactories;
import com.amazon.android.webkit.AmazonWebKitFactory;
import com.amazon.android.webkit.AmazonWebSettings;
import com.amazon.android.webkit.AmazonWebView;
import com.amazon.android.webkit.AmazonWebViewClient;

import android.app.Activity;
import android.content.Context;

import android.graphics.Bitmap;
import android.net.http.SslError;

import android.util.Log;
import android.view.KeyEvent;
import android.view.View;

/**
 * A web view provider. Uses AmazonWebViewClient to provide a web view.
 */
public class WebViewProvider {

    private static final String TAG = WebViewProvider.class.getName();

    protected AmazonWebView mWebView;
    private Context mContext;
    private String mRequestedUrl;
    private String mRequestCompleteUrl;
    private WebViewEventsListener mWebViewEventsListener;
    private boolean mInMouseMode = false;

    /**
     * Set mouse mode
     *
     * @param mouseMode true if mouse mode is on, false otherwise
     */
    public void setMouseMode(boolean mouseMode) {

        this.mInMouseMode = mouseMode;
    }

    /**
     * Interface to listen to events from the Webview
     */
    public interface WebViewEventsListener {

        /**
         * Method to listen for result of page load request. The result is SUCCESS if the
         * requestCompleteURl was reached, FAILURE otherwise.
         *
         * @param initialUrl         url to load page
         * @param requestCompleteUrl url to be called on load request completed
         * @param result             result of page load request
         */
        void result(String initialUrl, String requestCompleteUrl, String result);

        /**
         * Method to listen for page starts
         */
        void onPageStarted();

        /**
         * Method to listen for page finishes
         */
        void onPageFinished();

        /**
         * Method to listen for progress changes
         *
         * @param newProgress new progress
         */
        void onProgressChanged(int newProgress);

        /**
         * Method to listen layout changes
         *
         * @param v view which changed
         */
        void onLayoutChange(View v);
    }

    /**
     * WebViewClient for the web view.
     */
    private final AmazonWebViewClient webViewClient = new AmazonWebViewClient() {
        @Override
        public boolean shouldOverrideUrlLoading(AmazonWebView view, String url) {

            String cookies = AmazonCookieManager.getInstance().getCookie(url);
            Log.d(TAG, "shouldOverrideUrlLoading All the cookies in a string:" + url + " " +
                    cookies);
            if (url != null && url.equals(mRequestCompleteUrl)) {
                closeWebView("Success");
            }
            return false;
        }

        @Override
        public void onPageStarted(AmazonWebView view, String url, Bitmap favicon) {

            String cookies = AmazonCookieManager.getInstance().getCookie(url);
            Log.d(TAG, "onPageStarted All the cookies in a string:" + url + " " + cookies);
            if (mWebViewEventsListener != null) {
                mWebViewEventsListener.onPageStarted();
            }
        }

        @Override
        public void onPageFinished(AmazonWebView view, String url) {

            String cookies = AmazonCookieManager.getInstance().getCookie(url);
            Log.d(TAG, "onPageFinished All the cookies in a string:" + url + " " + cookies);
            if (mWebViewEventsListener != null) {
                mWebViewEventsListener.onPageFinished();
            }
        }

        @Override
        public void onReceivedError(AmazonWebView view, int errorCode, String description, String
                failingUrl) {

            Log.i(TAG, "onReceivedError errorCode " + errorCode + " description " + description +
                    " failingUrl " + failingUrl);
            super.onReceivedError(view, errorCode, description, failingUrl);
        }

        @Override
        public void onReceivedSslError(AmazonWebView webView, AmazonSslErrorHandler
                sslErrorHandler, SslError sslError) {

            Log.i(TAG, "onReceivedSslError " + String.valueOf(sslError));
            sslErrorHandler.proceed();
        }

        @Override
        public boolean shouldOverrideKeyEvent(AmazonWebView view, KeyEvent event) {

            if (event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_REWIND ||
                    event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_FAST_FORWARD ||
                    event.getKeyCode() == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE) {
                return true;
            }
            if (mInMouseMode && (event.getKeyCode() == KeyEvent.KEYCODE_DPAD_LEFT ||
                    event.getKeyCode() == KeyEvent.KEYCODE_DPAD_UP ||
                    event.getKeyCode() == KeyEvent.KEYCODE_DPAD_RIGHT ||
                    event.getKeyCode() == KeyEvent.KEYCODE_DPAD_DOWN ||
                    event.getKeyCode() == KeyEvent.KEYCODE_DPAD_CENTER)) {
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_MENU) {
                return true;
            }

            if (event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_L1 ||
                    event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_R1 ||
                    event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBL ||
                    event.getKeyCode() == KeyEvent.KEYCODE_BUTTON_THUMBR) {
                return true;
            }
            return false;
        }
    };

    /**
     * Constructor
     *
     * @param context               Context object
     * @param webViewEventsListener webViewListener object to listen to WebView events.
     */
    public WebViewProvider(Context context, WebViewEventsListener webViewEventsListener) {

        this.mContext = context.getApplicationContext();
        this.mWebViewEventsListener = webViewEventsListener;
        mWebView = new AmazonWebView(context.getApplicationContext());
    }

    /**
     * Method to set web view settings
     *
     * @param userDefinedExtraParameters User defined extra parameters to be set
     */
    public void setUp(String userDefinedExtraParameters) {

        AmazonWebKitFactory factory = AmazonWebKitFactories.getDefaultFactory();
        factory.initialize(mContext.getApplicationContext());
        factory.initializeWebView(mWebView, 0xFFFFFF, false, null);
        mWebView.setVerticalScrollBarEnabled(true);
        mWebView.setHorizontalScrollBarEnabled(true);

        AmazonWebSettings settings = mWebView.getSettings();
        settings.setJavaScriptEnabled(true);
        settings.setJavaScriptCanOpenWindowsAutomatically(true);
        settings.setSaveFormData(false);
        settings.setUseWideViewPort(true);
        settings.setBuiltInZoomControls(false);
        settings.setSupportZoom(true);
        settings.setLoadWithOverviewMode(true);
        settings.setAllowFileAccessFromFileURLs(true);
        settings.setAllowFileAccess(true);
        settings.setAllowContentAccess(true);
        settings.setAllowUniversalAccessFromFileURLs(true);

        String currentUserAgentString = settings.getUserAgentString();
        if (userDefinedExtraParameters != null) {
            settings.setUserAgentString(currentUserAgentString + userDefinedExtraParameters);
            Log.d(TAG, "User agent string set to " + settings.getUserAgentString());
        }

        AmazonCookieManager.getInstance().setAcceptCookie(true);

        this.mWebView.setWebViewClient(this.webViewClient);

        mWebView.setWebChromeClient(new AmazonWebChromeClient() {
            @Override
            public void onProgressChanged(AmazonWebView view, int newProgress) {

                if (mWebViewEventsListener != null) {
                    mWebViewEventsListener.onProgressChanged(newProgress);
                }
            }
        });

        mWebView.addOnLayoutChangeListener(new View.OnLayoutChangeListener() {
            @Override
            public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                       int oldLeft, int oldTop, int oldRight, int oldBottom) {

                if (mWebViewEventsListener != null) {
                    mWebViewEventsListener.onLayoutChange(v);
                }
            }
        });
    }

    /**
     * Method to request loading URL
     *
     * @param requestedUrl url to be loaded
     */
    public void loadUrl(String requestedUrl) {

        this.mRequestedUrl = requestedUrl;
        this.mWebView.loadUrl(requestedUrl);
    }

    /**
     * Utility to close the web view through external source. It does not send any response via
     * WebViewEventListener.
     */
    public void closeWebView() {

        if (mWebView != null) {
            mWebView.removeAllViews();
            mWebView = null;
        }
    }

    /**
     * Getter for web view
     *
     * @return web view
     */
    public AmazonWebView getWebView() {

        return mWebView;
    }

    /**
     * Closes the web view and sends the result via the WebViewEventListener
     *
     * @param result Result to be sent
     */
    private void closeWebView(String result) {

        closeWebView();
        if (mWebViewEventsListener != null) {
            mWebViewEventsListener.result(mRequestedUrl, mRequestCompleteUrl, result);
        }
    }

    /**
     * Method to attach a UI element with the web view
     *
     * @param webView WebView element to be attached
     */
    public void setUI(AmazonWebView webView) {

        this.mWebView = webView;
    }


}