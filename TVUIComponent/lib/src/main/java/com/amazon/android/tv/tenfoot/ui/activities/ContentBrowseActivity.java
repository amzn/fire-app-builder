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

package com.amazon.android.tv.tenfoot.ui.activities;

import com.amazon.android.configuration.ConfigurationManager;
import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.Action;
import com.amazon.android.model.content.Content;
import com.amazon.android.tv.tenfoot.utils.BrowseHelper;
import com.amazon.android.ui.constants.ConfigurationConstants;
import com.amazon.android.ui.fragments.LogoutSettingsFragment;
import com.amazon.android.utils.GlideHelper;
import com.amazon.android.ui.utils.BackgroundImageUtils;
import com.amazon.android.utils.Helpers;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.base.BaseActivity;
import com.amazon.android.tv.tenfoot.ui.fragments.ContentBrowseFragment;

import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.content.ContextCompat;
import android.view.Display;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import java.util.concurrent.TimeUnit;

import rx.Observable;
import rx.Subscription;
import rx.android.schedulers.AndroidSchedulers;
import uk.co.chrisjenx.calligraphy.CalligraphyUtils;

/**
 * ContentBrowseActivity class that loads the ContentBrowseFragment.
 */
public class ContentBrowseActivity extends BaseActivity implements ContentBrowseFragment
        .OnBrowseRowListener {

    private final String TAG = ContentBrowseActivity.class.getSimpleName();

    private static final int CONTENT_IMAGE_CROSS_FADE_DURATION = 1000;
    private static final int ACTIVITY_ENTER_TRANSITION_FADE_DURATION = 1500;
    private static final int UI_UPDATE_DELAY_IN_MS = 0;

    private TextView mContentTitle;
    private TextView mContentDescription;
    private ImageView mContentImage;
    private Subscription mContentImageLoadSubscription;

    // View that contains the background
    private View mMainFrame;
    private Drawable mBackgroundWithPreview;

    @Override
    public void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);

        setContentView(R.layout.content_browse_activity_layout);

        Helpers.handleActivityEnterFadeTransition(this, ACTIVITY_ENTER_TRANSITION_FADE_DURATION);

        mContentTitle = (TextView) findViewById(R.id.content_detail_title);

        CalligraphyUtils.applyFontToTextView(this, mContentTitle, ConfigurationManager
                .getInstance(this).getTypefacePath(ConfigurationConstants.BOLD_FONT));

        mContentDescription = (TextView) findViewById(R.id.content_detail_description);
        CalligraphyUtils.applyFontToTextView(this, mContentDescription, ConfigurationManager
                .getInstance(this).getTypefacePath(ConfigurationConstants.LIGHT_FONT));

        mContentImage = (ImageView) findViewById(R.id.content_image);

        mContentImage.setImageURI(Uri.EMPTY);

        // Get display/background size
        Display display = getWindowManager().getDefaultDisplay();
        Point windowSize = new Point();
        display.getSize(windowSize);
        int imageWidth = (int) getResources().getDimension(R.dimen.content_image_width);
        int imageHeight = (int) getResources().getDimension(R.dimen.content_image_height);
        int gradientSize = (int) getResources().getDimension(R.dimen.content_image_gradient_size);
        // Create the background
        Bitmap background =
                BackgroundImageUtils.createBackgroundWithPreviewWindow(
                        windowSize.x,
                        windowSize.y,
                        imageWidth,
                        imageHeight,
                        gradientSize,
                        ContextCompat.getColor(this, R.color.browse_background_color));
        mBackgroundWithPreview = new BitmapDrawable(getResources(), background);
        // Set the background
        mMainFrame = findViewById(R.id.main_frame);
        mMainFrame.setBackground(mBackgroundWithPreview);
    }

    /**
     * {@inheritDoc}
     * Called by the browse fragment ({@link ContentBrowseFragment}. Switches the content
     * title, description, and image.
     */
    @Override
    public void onItemSelected(Object item) {

        if (item instanceof Content) {
            Content content = (Content) item;
            callImageLoadSubscription(content.getTitle(),
                                      content.getDescription(),
                                      content.getBackgroundImageUrl());
        }
        else if (item instanceof Action) {
            Action settingsAction = (Action) item;
            // Terms of use action.
            if (ContentBrowser.TERMS.equals(settingsAction.getAction())) {
                callImageLoadSubscription(getString(R.string.terms_title),
                                          getString(R.string.terms_description),
                                          null);
            }
            // Login and logout action.
            else if (ContentBrowser.LOGIN_LOGOUT.equals(settingsAction.getAction())) {

                if (settingsAction.getState() == LogoutSettingsFragment.TYPE_LOGOUT) {
                    callImageLoadSubscription(getString(R.string.logout_label),
                                              getString(R.string.logout_description),
                                              null);
                }
                else {
                    callImageLoadSubscription(getString(R.string.login_label),
                                              getString(R.string.login_description),
                                              null);
                }
            }
        }
    }

    /**
     * Helper method to subscribe the selected item to the observable that will load the content
     * image into the background. It is okay for the background image URL to be null. A null URL
     * will result in showing the default background.
     *
     * @param title       The title to display.
     * @param description The description to display.
     * @param bgImageUrl  The URL of the image to display.
     */
    public void callImageLoadSubscription(String title, String description, String bgImageUrl) {

        mContentImageLoadSubscription = Observable
                .timer(UI_UPDATE_DELAY_IN_MS, TimeUnit.MILLISECONDS)
                .observeOn(AndroidSchedulers.mainThread()) // This is a must for timer.
                .subscribe(c -> {
                    mContentTitle.setText(title);
                    mContentDescription.setText(description);
                    GlideHelper.loadImageWithCrossFadeTransition(this,
                                                                 mContentImage,
                                                                 bgImageUrl,
                                                                 CONTENT_IMAGE_CROSS_FADE_DURATION,
                                                                 R.color.browse_background_color);

                    // If there is no image, remove the preview window
                    if (bgImageUrl != null && !bgImageUrl.isEmpty()) {
                        mMainFrame.setBackground(mBackgroundWithPreview);
                    }
                    else {
                        mMainFrame.setBackgroundColor(Color.TRANSPARENT);
                    }
                });

    }

    @Override
    protected void onDestroy() {

        super.onDestroy();
        if (mContentImageLoadSubscription != null) {
            mContentImageLoadSubscription.unsubscribe();
        }
    }

    @Override
    protected void onResume() {

        super.onResume();
        if (ContentBrowser.getInstance(this).getAuthHelper() != null) {
            ContentBrowser.getInstance(this).getAuthHelper()
                          .loadPoweredByLogo(this, (ImageView) findViewById(R.id.mvpd_logo));
        }

        reportFullyDrawn();
    }

    @Override
    public void setRestoreActivityValues() {

        BrowseHelper.saveBrowseActivityState(this);
    }
}
