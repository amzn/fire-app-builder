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
 * Copyright (C) 2015 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package android.support.v17.leanback.app;

import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.utils.Helpers;
import com.bumptech.glide.Glide;

import android.content.Context;
import android.content.res.Resources;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v17.leanback.widget.BaseCardView;
import android.support.v17.leanback.widget.ImageCardView;
import android.support.v17.leanback.widget.Presenter;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

// TODO: this file will be removed when we get fragment injection for UAMP in. This will be in
// sync with {@link CardPresenter}
/**
 * A CardPresenter is used to generate Views and bind Objects to them on demand.
 * It contains an ImageCardView.
 */
public class CardPresenter extends Presenter {

    private static final String TAG = "CardPresenter";

    private int mCardWidthDp;
    private int mCardHeightDp;

    private Drawable mDefaultCardImage;
    private static Drawable sFocusedFadeMask;
    private View mInfoField;

    /**
     * {@inheritDoc}
     */
    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent) {

        Context context = parent.getContext();
        try {
            mDefaultCardImage = new ColorDrawable(0x00000000);
            sFocusedFadeMask = new ColorDrawable(0x00000000);
        }
        catch (Resources.NotFoundException e) {
            Log.e(TAG, "Could not find resource ", e);
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

        TextView subtitle = (TextView) cardView.findViewById(R.id.content_text);
        if (subtitle != null) {
            subtitle.setEllipsize(TextUtils.TruncateAt.END);
        }

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
    public void onBindViewHolder(ViewHolder viewHolder, Object item) {

        ImageCardView cardView = (ImageCardView) viewHolder.view;

        if (item instanceof Content) {
            Content content = (Content) item;

            if (content.getCardImageUrl() != null) {
                if (content.getSubtitle() != null) {
                    // The word 'Title' is not logically correct in setTitleText, the 'TitleText'
                    // is actually smaller text compared to 'ContentText', so we are using
                    // TitleText to show subtitle and ContentText to show the actual Title.
                    cardView.setTitleText(content.getSubtitle());
                }

                cardView.setContentText(content.getTitle());
                cardView.setMainImageDimensions(mCardWidthDp, mCardHeightDp);
                Glide.with(viewHolder.view.getContext())
                     .load(content.getCardImageUrl())
                     .centerCrop()
                     .error(mDefaultCardImage)
                     .into(cardView.getMainImageView());
            }
        }
        else if (item instanceof ContentContainer) {
            ContentContainer contentContainer = (ContentContainer) item;
            cardView.setContentText(contentContainer.getName());
            cardView.setMainImageDimensions(mCardWidthDp, mCardHeightDp);
            cardView.getMainImageView().setImageDrawable(mDefaultCardImage);
        }
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onUnbindViewHolder(ViewHolder viewHolder) {

        ImageCardView cardView = (ImageCardView) viewHolder.view;
        // Remove references to images so that the garbage collector can free up memory.
        cardView.setBadgeImage(null);
        cardView.setMainImage(null);
    }
}

