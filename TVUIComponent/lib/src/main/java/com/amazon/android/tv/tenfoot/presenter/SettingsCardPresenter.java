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
package com.amazon.android.tv.tenfoot.presenter;

import com.amazon.android.model.Action;
import com.amazon.android.tv.tenfoot.base.TenFootApp;
import com.amazon.android.utils.Helpers;
import com.amazon.android.tv.tenfoot.R;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

/**
 * A CardPresenter used to generate Views and bind SettingsItems to them on demand.
 */
public class SettingsCardPresenter extends Presenter {

    private static final String TAG = SettingsCardPresenter.class.getSimpleName();

    private int mCardWidthDp;
    private int mCardHeightDp;
    private static Drawable sFocusedFadeMask;
    private View mInfoField;

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        Context context = parent.getContext();
        try {
            sFocusedFadeMask = ContextCompat.getDrawable(context, R.drawable.content_fade_focused);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found", e);
            throw e;
        }

        ImageCardView cardView = new ImageCardView(context) {
            @Override
            public void setSelected(boolean selected) {

                super.setSelected(selected);
                if (mInfoField != null) {
                    mInfoField.setBackground(sFocusedFadeMask);
                }
            }
        };
        cardView.setFocusable(true);
        cardView.setFocusableInTouchMode(true);

        // Set the type and visibility of the info area.
        cardView.setCardType(BaseCardView.CARD_TYPE_INFO_OVER);
        cardView.setInfoVisibility(BaseCardView.CARD_REGION_VISIBLE_ALWAYS);


        int CARD_WIDTH_PX = 160;
        mCardWidthDp = Helpers.convertPixelToDp(context, CARD_WIDTH_PX);
        int CARD_HEIGHT_PX = 120;
        mCardHeightDp = Helpers.convertPixelToDp(context, CARD_HEIGHT_PX);

        mInfoField = cardView.findViewById(R.id.info_field);
        if (mInfoField != null) {
            mInfoField.setBackground(sFocusedFadeMask);
        }

        return new ViewHolder(cardView);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBindViewHolder(Presenter.ViewHolder viewHolder, Object item) {

        Action settingsItem = (Action) item;
        ImageCardView cardView = (ImageCardView) viewHolder.view;

        cardView.setContentText(settingsItem.getLabel1());
        cardView.setMainImageScaleType(ImageView.ScaleType.CENTER);
        cardView.setMainImageDimensions(mCardWidthDp, mCardHeightDp);
        try {
            cardView.setMainImage(ContextCompat.getDrawable(TenFootApp.getInstance()
                                                                      .getApplicationContext(),
                                                            settingsItem.getIconResourceId()));
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Resource not found", e);
            throw e;
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}

