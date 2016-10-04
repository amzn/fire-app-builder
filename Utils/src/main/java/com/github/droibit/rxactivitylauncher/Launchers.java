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
Copyright 2015 droibit

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

   http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
 */
package com.github.droibit.rxactivitylauncher;

import android.app.Activity;
import android.app.Fragment;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;


/**
 * Factory class of {@link Launchable}.
 *
 * @author kumagai
 */
class Launchers {

    /**
     * Class to start another {@link Activity}.
     */
    static class SourceActivity extends Launchable {

        private final Activity mActivity;
        private final RxLauncher mLauncher;

        SourceActivity(RxLauncher launcher, Activity activity) {
            if (activity == null) {
                throw new IllegalArgumentException("Activity should not be null.");
            }
            mActivity = activity;
            mLauncher = launcher;
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@Nullable Observable<?> trigger,
                                                                 @NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, trigger, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startActivity(@NonNull Intent intent, int requestCode, @Nullable Bundle options) {
            mActivity.startActivityForResult(intent, requestCode, options);
        }
    }

    /**
     * Class to start another {@link Activity} from {@link Fragment}
     */
    static class SourceFragment extends Launchable {

        private final Fragment mFragment;
        private final RxLauncher mLauncher;

        SourceFragment(RxLauncher launcher, Fragment fragment) {
            if (fragment == null) {
                throw new IllegalArgumentException("Fragment should not be null.");
            }
            mFragment = fragment;
            mLauncher = launcher;
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@Nullable Observable<?> trigger,
                                                                 @NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, trigger, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startActivity(@NonNull Intent intent, int requestCode, @Nullable Bundle options) {
            mFragment.startActivityForResult(intent, requestCode, options);
        }
    }

    /**
     * Class to start another {@link Activity} from {@link android.support.v4.app.Fragment}
     */
    static class SourceSupportFragment extends Launchable {

        private final android.support.v4.app.Fragment mFragment;
        private final RxLauncher mLauncher;

        SourceSupportFragment(RxLauncher launcher, android.support.v4.app.Fragment fragment) {
            if (fragment == null) {
                throw new IllegalArgumentException("Fragment should not be null.");
            }
            mFragment = fragment;
            mLauncher = launcher;
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @CheckResult
        @Override
        public Observable<ActivityResult> startActivityForResult(@Nullable Observable<?> trigger,
                                                                 @NonNull Intent intent,
                                                                 int requestCode,
                                                                 @Nullable Bundle options) {
            return mLauncher.startActivityForResult(this, trigger, intent, requestCode, options);
        }

        /**
         * {@inheritDoc}
         */
        @Override
        protected void startActivity(@NonNull Intent intent, int requestCode, /* ignore */ @Nullable Bundle options) {
            mFragment.startActivityForResult(intent, requestCode);
        }
    }
}
