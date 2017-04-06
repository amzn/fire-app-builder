package android.support.v17.leanback.widget;
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

import com.amazon.android.tv.tenfoot.R;

import android.graphics.drawable.Drawable;
import android.text.TextUtils;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

/**
 * A presenter class for an {@link Action} item.
 */
public class TenFootActionPresenterSelector extends PresenterSelector {

    private final Presenter mOneLineActionPresenter =
            new TenFootActionPresenterSelector.OneLineActionPresenter();

    private final Presenter mTwoLineActionPresenter =
            new TenFootActionPresenterSelector.TwoLineActionPresenter();

    private final Presenter[] mPresenters;

    public TenFootActionPresenterSelector() {

        this.mPresenters = new Presenter[]{this.mOneLineActionPresenter,
                this.mTwoLineActionPresenter};
    }

    public Presenter getPresenter(Object item) {

        Action action = (Action) item;
        return TextUtils.isEmpty(action.getLabel2()) ?
                this.mOneLineActionPresenter : this.mTwoLineActionPresenter;
    }

    public Presenter[] getPresenters() {

        return this.mPresenters;
    }

    class TwoLineActionPresenter extends Presenter {

        TwoLineActionPresenter() {

        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_action_2_lines,
                                                                      parent, false);
            v.requestFocus();
            return new ActionPresenterSelector.ActionViewHolder(v, parent.getLayoutDirection());
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {

            Action action = (Action) item;

            final ActionPresenterSelector.ActionViewHolder vh =
                    (ActionPresenterSelector.ActionViewHolder) viewHolder;

            Drawable icon = action.getIcon();
            vh.mAction = action;
            int line1;
            if (icon != null) {
                line1 = vh.view.getResources()
                               .getDimensionPixelSize(R.dimen.lb_action_with_icon_padding_start);
                int line2 = vh.view.getResources()
                                   .getDimensionPixelSize(R.dimen.lb_action_with_icon_padding_end);
                vh.view.setPaddingRelative(line1, 0, line2, 0);
            }
            else {
                line1 = vh.view.getResources()
                               .getDimensionPixelSize(R.dimen.lb_action_padding_horizontal);
                vh.view.setPaddingRelative(line1, 0, line1, 0);
            }

            if (vh.mLayoutDirection == 1) {
                vh.mButton.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable)
                        null, icon, (Drawable) null);
            }
            else {
                vh.mButton.setCompoundDrawablesWithIntrinsicBounds(icon, (Drawable) null,
                                                                   (Drawable) null, (Drawable)
                                                                           null);
            }

            CharSequence line11 = action.getLabel1();
            CharSequence line21 = action.getLabel2();
            if (TextUtils.isEmpty(line11)) {
                vh.mButton.setText(line21);
            }
            else if (TextUtils.isEmpty(line21)) {
                vh.mButton.setText(line11);
            }
            else {
                vh.mButton.setText(line11 + "\n" + line21);
            }
            vh.view.requestFocus();
            vh.mButton.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE &&
                            event.getAction() == KeyEvent.ACTION_DOWN) {
                        vh.mButton.performClick();
                    }
                    return false;
                }
            });
        }

        public void onUnbindViewHolder(ViewHolder viewHolder) {

            ActionPresenterSelector.ActionViewHolder vh = (ActionPresenterSelector
                    .ActionViewHolder) viewHolder;
            vh.mButton.setCompoundDrawablesWithIntrinsicBounds((Drawable) null, (Drawable) null,
                                                               (Drawable) null, (Drawable) null);
            vh.view.setPadding(0, 0, 0, 0);
            vh.mAction = null;
        }
    }

    class OneLineActionPresenter extends Presenter {

        OneLineActionPresenter() {

        }

        public ViewHolder onCreateViewHolder(ViewGroup parent) {

            View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.lb_action_1_line,
                                                                      parent, false);
            v.requestFocus();
            return new ActionPresenterSelector.ActionViewHolder(v, parent.getLayoutDirection());
        }

        public void onBindViewHolder(ViewHolder viewHolder, Object item) {

            Action action = (Action) item;
            final ActionPresenterSelector.ActionViewHolder vh = (ActionPresenterSelector
                    .ActionViewHolder) viewHolder;
            vh.mAction = action;
            vh.mButton.setText(action.getLabel1());
            vh.view.requestFocus();
            vh.mButton.setOnKeyListener(new View.OnKeyListener() {
                @Override
                public boolean onKey(View v, int keyCode, KeyEvent event) {

                    if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE &&
                            event.getAction() == KeyEvent.ACTION_DOWN) {
                        vh.mButton.performClick();
                    }
                    return false;
                }
            });
            //  setCommomButtonProperties(vh.mButton);
        }

        public void onUnbindViewHolder(Presenter.ViewHolder viewHolder) {

            ((ActionPresenterSelector.ActionViewHolder) viewHolder).mAction = null;
        }
    }

    /**
     * sets common properties of all buttons
     */
    private void setCommomButtonProperties(final Button button) {

        button.requestFocus();
        button.setOnKeyListener(new View.OnKeyListener() {
            @Override
            public boolean onKey(View v, int keyCode, KeyEvent event) {

                if (keyCode == KeyEvent.KEYCODE_MEDIA_PLAY_PAUSE &&
                        event.getAction() == KeyEvent.ACTION_DOWN) {
                    button.performClick();
                }
                return false;
            }
        });
    }

    static class ActionViewHolder extends Presenter.ViewHolder {

        Action mAction;
        Button mButton;
        int mLayoutDirection;

        public ActionViewHolder(View view, int layoutDirection) {

            super(view);
            this.mButton = (Button) view.findViewById(R.id.lb_action_button);
            this.mLayoutDirection = layoutDirection;
        }
    }
}