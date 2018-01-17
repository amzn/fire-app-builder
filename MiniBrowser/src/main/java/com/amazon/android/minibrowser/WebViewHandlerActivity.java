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

import com.amazon.android.webkit.AmazonWebView;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import android.os.SystemClock;
import android.util.Log;
import android.view.InputDevice;
import android.view.KeyEvent;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.MotionEvent.PointerProperties;
import android.view.View;
import android.view.Window;
import android.widget.FrameLayout.LayoutParams;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

/**
 * A web view implementation. It serves requests to load URL.
 */
public class WebViewHandlerActivity extends Activity implements WebViewProvider
        .WebViewEventsListener {

    private static final String TAG = WebViewHandlerActivity.class.getName();

    /**
     * Key to get the url to be launched in intent.
     */
    private static final String URL_KEY = "url";

    private static final int MOUSE_WIDTH = 30;
    private static final int MOUSE_HEIGHT = 44;
    private static final int MOUSE_IN_BOUNDS_MIN = 12;
    private static final int MOUSE_MOVEMENT_MAX = 16;
    private static final int MOUSE_MOVEMENT_START = 6;
    private static final int MOUSE_SCROLL_CHANGE = 20;
    private static final int MOUSE_SCROLL_MIN = 1;

    private static final String FOCUS_JAVASCRIPT = "javascript:var css = document.createElement" +
            "(\"style\");css.type = \"text/css\";"
            + "css.innerHTML = \":focus { outline: solid 6px #FFA724; outline-offset: 3px;}\";"
            + "document.body.appendChild(css);";

    private WebViewProvider mWebViewProvider;

    private boolean mInMouseMode = false;

    private ProgressBar mProgress;
    private LayoutParams mCursorParams;
    private ImageView mCursor;

    private int mWebViewHeight;
    private int mWebViewWidth;
    private int mXPosition;
    private int mYPosition;
    private int mMouseMovement;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");

        requestWindowFeature(Window.FEATURE_OPTIONS_PANEL);

        setContentView(R.layout.activity_amazon_webview);
        mProgress = (ProgressBar) findViewById(R.id.progress_bar);
        setWebViewProvider();
        Intent intent = getIntent();
        loadUrl(intent.getStringExtra(URL_KEY));
    }


    @Override
    public void onNewIntent(Intent intent) {

        super.onNewIntent(intent);
        setIntent(intent);
        Log.d(TAG, "onNewIntent " + intent.toString());
    }

    /**
     * Method to set the WebView Provider
     */
    private void setWebViewProvider() {

        mWebViewProvider = new WebViewProvider(this, this);
        mWebViewProvider.setUI((AmazonWebView) findViewById(R.id.calypso_webview));
        mWebViewProvider.setUp(null);
    }

    @Override
    public void onPageStarted() {
        //Update the progress bar
        mProgress.setProgressDrawable(getResources().getDrawable(R.drawable.progress_bar));
    }

    @Override
    public void onPageFinished() {

        if (mWebViewProvider.getWebView() != null) {
            mWebViewProvider.getWebView().loadUrl(FOCUS_JAVASCRIPT);
            mProgress.setProgressDrawable(getResources().getDrawable(R.drawable
                                                                             .progress_bar_invisible));
        }
    }

    @Override
    public void onProgressChanged(int newProgress) {

        mProgress.setProgress(newProgress);
    }

    @Override
    public void onLayoutChange(View v) {

        mWebViewWidth = v.getWidth();
        mWebViewHeight = v.getHeight();
    }

    /**
     * Method to load the url
     *
     * @param requestUrl url to load
     */
    public void loadUrl(String requestUrl) {

        this.mWebViewProvider.loadUrl(requestUrl);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void result(String requestUrl, String requestCompleteUrl, String result) {

        finish();
    }

    @Override
    public void onBackPressed() {

        mWebViewProvider.closeWebView();
        finish();
    }

    /**
     * Initializes the mouse
     */
    private void initMouse() {

        mXPosition = mWebViewWidth / 2;
        mYPosition = mWebViewHeight / 2;
        mMouseMovement = MOUSE_MOVEMENT_START;
        mCursor = new ImageView(this);
        mCursor.setImageResource(R.drawable.cursor);
        mCursor.setVisibility(View.INVISIBLE);
        mCursorParams = new LayoutParams(MOUSE_WIDTH, MOUSE_HEIGHT);
        mCursorParams.leftMargin = mXPosition;
        mCursorParams.topMargin = mYPosition;
        mWebViewProvider.getWebView().addView(mCursor, mCursorParams);
    }

    /**
     * Overriding remote keys for custom actions
     * {@inheritDoc}
     */
    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {

        boolean handled = true;
        switch (keyCode) {
            case KeyEvent.KEYCODE_MEDIA_REWIND:
                mWebViewProvider.getWebView().goBack();
                break;
            case KeyEvent.KEYCODE_MEDIA_FAST_FORWARD:
                mWebViewProvider.getWebView().goForward();
                break;
            case KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE:
                mWebViewProvider.getWebView().reload();
                break;
            case KeyEvent.KEYCODE_DPAD_LEFT:
            case KeyEvent.KEYCODE_DPAD_UP:
            case KeyEvent.KEYCODE_DPAD_RIGHT:
            case KeyEvent.KEYCODE_DPAD_DOWN:
                if (mInMouseMode) {
                    moveMouse(keyCode);
                    if (mMouseMovement < MOUSE_MOVEMENT_MAX) {
                        mMouseMovement++;
                    }
                }
                break;
            case KeyEvent.KEYCODE_DPAD_CENTER:
                if (mInMouseMode) {
                    performClick();
                    break;
                }
            case KeyEvent.KEYCODE_MENU:
                changeNavigationMode();
                break;

            default:
                handled = false;
        }
        return handled || super.onKeyDown(keyCode, event);
    }

    @Override
    public boolean onKeyUp(int keyCode, KeyEvent event) {

        mMouseMovement = MOUSE_MOVEMENT_START;
        return super.onKeyUp(keyCode, event);
    }

    /**
     * Defining mouse movement
     * @param keyCode keycode entered
     */
    private void moveMouse(int keyCode) {

        switch (keyCode) {
            case KeyEvent.KEYCODE_DPAD_LEFT:
                final int scrollLeft = mWebViewProvider.getWebView().getScrollX();
                if (scrollLeft >= mXPosition) {
                    scrollHorizontally(-MOUSE_SCROLL_CHANGE);
                    break;
                }
                mXPosition -= mMouseMovement;
                if (mXPosition < 0) {
                    mXPosition = 0;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_UP:
                final int scrollTop = mWebViewProvider.getWebView().getScrollY();
                if (scrollTop >= mYPosition) {
                    scrollVertically(-MOUSE_SCROLL_CHANGE);
                    break;
                }
                mYPosition -= mMouseMovement;
                if (mYPosition < 0) {
                    mYPosition = 0;
                }
                break;
            case KeyEvent.KEYCODE_DPAD_RIGHT:
                final int cursorRight = mWebViewWidth + mWebViewProvider.getWebView().getScrollX();
                if (mXPosition == (cursorRight - MOUSE_IN_BOUNDS_MIN)) {
                    scrollHorizontally(MOUSE_SCROLL_CHANGE);
                    break;
                }
                mXPosition += mMouseMovement;
                if (mXPosition > (cursorRight - MOUSE_IN_BOUNDS_MIN)) {
                    mXPosition = (cursorRight - MOUSE_IN_BOUNDS_MIN);
                }
                break;
            case KeyEvent.KEYCODE_DPAD_DOWN:
                final int cursorBottom = mWebViewHeight + mWebViewProvider.getWebView()
                                                                          .getScrollY();
                if (mYPosition == (cursorBottom - MOUSE_IN_BOUNDS_MIN)) {
                    scrollVertically(MOUSE_SCROLL_CHANGE);
                    break;
                }
                mYPosition += mMouseMovement;
                if (mYPosition > (cursorBottom - MOUSE_IN_BOUNDS_MIN)) {
                    mYPosition = (cursorBottom - MOUSE_IN_BOUNDS_MIN);
                }
                break;
            default:
                break;
        }
        mCursorParams.leftMargin = mXPosition;
        mCursorParams.topMargin = mYPosition;
        mCursor.setLayoutParams(mCursorParams);
    }

    /**
     * Settings to move mouse horizontally
     * @param change change in position
     */
    private void scrollHorizontally(int change) {

        if (!mWebViewProvider.getWebView().canScrollHorizontally(change) && Math.abs(change) <=
                MOUSE_SCROLL_MIN) {
            return;
        }
        else if (mWebViewProvider.getWebView().canScrollHorizontally(change)) {
            mWebViewProvider.getWebView().scrollBy(change, 0);
            return;
        }
        else {
            scrollHorizontally(change / 2);
            return;
        }
    }

    /**
     * Settings to move mouse vertically
     * @param change change in position
     */
    private void scrollVertically(int change) {

        if (!mWebViewProvider.getWebView().canScrollVertically(change) && Math.abs(change) <=
                MOUSE_SCROLL_MIN) {
            return;
        }
        else if (mWebViewProvider.getWebView().canScrollVertically(change)) {
            mWebViewProvider.getWebView().scrollBy(0, change);
            return;
        }
        else {
            scrollVertically(change / 2);
            return;
        }
    }

    /**
     * Settings to perform mouse click
     */
    private void performClick() {

        long downTime = SystemClock.uptimeMillis();
        long eventTime = SystemClock.uptimeMillis();
        int metaState = 0;
        MotionEvent.PointerProperties p = new PointerProperties();
        p.toolType = MotionEvent.TOOL_TYPE_FINGER;
        p.id = 0;
        MotionEvent.PointerProperties[] properties = {p};

        MotionEvent.PointerCoords c = new MotionEvent.PointerCoords();
        c.x = mXPosition - mWebViewProvider.getWebView().getScrollX();
        c.y = mYPosition - mWebViewProvider.getWebView().getScrollY();
        c.x = Math.max(c.x, 0);
        c.y = Math.max(c.y, 0);
        c.orientation = 0f;
        c.pressure = 1f;
        c.size = 1f;

        MotionEvent.PointerCoords[] coords = {c};
        int buttonState = 0;
        float precisionX = 1.0f;
        float precisionY = 1.0f;
        int deviceId = 1;
        int edgeFlags = 0;
        int flags = 0;

        MotionEvent downEvent = MotionEvent.obtain(downTime, eventTime,
                                                   MotionEvent.ACTION_DOWN,
                                                   1, properties, coords,
                                                   metaState, buttonState,
                                                   precisionX, precisionY,
                                                   deviceId, edgeFlags,
                                                   InputDevice.SOURCE_TOUCHSCREEN,
                                                   flags);
        mWebViewProvider.getWebView().dispatchTouchEvent(downEvent);

        MotionEvent upEvent = MotionEvent.obtain(downTime, eventTime + 90,
                                                 MotionEvent.ACTION_UP,
                                                 1, properties, coords,
                                                 metaState, buttonState,
                                                 precisionX, precisionY,
                                                 deviceId, edgeFlags,
                                                 InputDevice.SOURCE_TOUCHSCREEN,
                                                 flags);
        mWebViewProvider.getWebView().dispatchTouchEvent(upEvent);
    }

    /**
     * Change navigation mode
     */
    private void changeNavigationMode() {

        TextView prompt = (TextView) findViewById(R.id.menu_description);
        TextView directions = (TextView) findViewById(R.id.directions);
        if (mInMouseMode) {
            mCursor.setVisibility(View.INVISIBLE);
            prompt.setText(getString(R.string.cursor_mode_description));
            directions.setText(getText(R.string.directional_instructions));
            mInMouseMode = false;
            mWebViewProvider.setMouseMode(mInMouseMode);
        }
        else {
            initMouse();
            mCursor.setVisibility(View.VISIBLE);
            prompt.setText(getString(R.string.turn_off_cursor));
            directions.setText(getText(R.string.mouse_instructions));
            mInMouseMode = true;
            mWebViewProvider.setMouseMode(mInMouseMode);
        }

    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {

        if (mInMouseMode) {
            menu.findItem(R.id.turn_off_mouse).setVisible(true);
            menu.findItem(R.id.turn_on_mouse).setVisible(false);
        }
        else {
            menu.findItem(R.id.turn_off_mouse).setVisible(false);
            menu.findItem(R.id.turn_on_mouse).setVisible(true);
        }
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        int id = item.getItemId();

        if (id == R.id.turn_on_mouse) {
            changeNavigationMode();
        }
        else if (id == R.id.turn_off_mouse) {
            changeNavigationMode();
        }
        return super.onOptionsItemSelected(item);
    }
}
