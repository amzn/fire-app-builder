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
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.VisibleForTesting;

import java.util.HashMap;
import java.util.Map;

import rx.Observable;
import rx.Observer;
import rx.functions.Func1;
import rx.subjects.PublishSubject;

/**
 * Provide a way to receive the results of the {@link Activity} by RxJava.
 * <p>
 * <b>Must call {@link #activityResult(int, int, Intent)} method in Activity or Fragment.</b><br>
 * </p>
 * <p>
 * If you use the implicit Intent, {@link ActivityNotFoundException} or {@link SecurityException}
 * might occur.
 * So it is recommended that you use the {@link Observable#subscribe(Observer)}.
 * </p>
 *
 * @author kumagai
 */
public class RxLauncher {

    public static RxLauncher getInstance() {

        return mInstance;
    }

    private static final RxLauncher mInstance = new RxLauncher();

    private final Map<Integer, PublishSubject<ActivityResult>> mSubjects;

    @VisibleForTesting
    RxLauncher() {

        mSubjects = new HashMap<>(3);
    }

    /**
     * Set the launch source component({@link Activity}) of other activity.
     *
     * @param source The source activity.
     * @return New {@link Launchable} instance.
     */
    @CheckResult
    public Launchable from(Activity source) {

        return new Launchers.SourceActivity(this, source);
    }

    /**
     * Set the launch source component({@link Fragment}) of other activity.
     *
     * @param source The source fragment.
     * @return {@link Launchable} instance.
     */
    @CheckResult
    public Launchable from(Fragment source) {

        return new Launchers.SourceFragment(this, source);
    }

    /**
     * Set the launch source component({@link android.support.v4.app.Fragment}) of other activity.
     *
     * @param source The source fragment.
     * @return {@link Launchable} instance.
     */
    @CheckResult
    public Launchable from(android.support.v4.app.Fragment source) {

        return new Launchers.SourceSupportFragment(this, source);
    }

    /**
     * Launch an activity for which you would like a result when it finished.
     *
     * @param source      The source {@link Launchable}.
     * @param intent      The intent.
     * @param requestCode The request code.
     * @param options     The option bundle.
     * @return The {@link Observable} for the {@link ActivityResult}.
     * @see Activity#startActivityForResult(Intent, int, Bundle)
     * @see Fragment#startActivityForResult(Intent, int, Bundle)
     */
    Observable<ActivityResult> startActivityForResult(@NonNull Launchable source,
                                                      @NonNull Intent intent,
                                                      int requestCode,
                                                      @Nullable Bundle options) {

        return startActivityForResult(source, null, intent, requestCode, options);
    }


    /**
     * Launch an activity for which you would like a result when it finished.
     * <p>
     * After other activity launched, you use this method if the screen might rotate.
     * </p>
     *
     * @param source      The source {@link Launchable}.
     * @param trigger     The observable to trigger.
     * @param intent      The intent.
     * @param requestCode The request code.
     * @param options     The options bundle.
     * @return The {@link Observable} for the {@link ActivityResult}.
     */
    Observable<ActivityResult> startActivityForResult(@NonNull final Launchable source,
                                                      @Nullable Observable<?> trigger,
                                                      @NonNull final Intent intent,
                                                      final int requestCode,
                                                      @Nullable final Bundle options) {

        return triggerObservable(trigger, requestCode)
                .flatMap(new Func1<Object, Observable<ActivityResult>>() {
                    @Override
                    public Observable<ActivityResult> call(Object o) {

                        return startActivityObservable(source, intent, requestCode, options);
                    }
                });
    }

    /**
     * Receive a result from the started activity.
     * Should call in any of the reference methods.
     *
     * @param requestCode The request code.
     * @param resultCode  The result code.
     * @param data        The data intent.
     * @see Activity#onActivityResult(int, int, Intent)
     * @see Fragment#onActivityResult(int, int, Intent)
     * @see android.support.v4.app.Fragment#onActivityResult(int, int, Intent)
     */
    public void activityResult(int requestCode, int resultCode, @Nullable Intent data) {

        final PublishSubject<ActivityResult> subject = mSubjects.remove(requestCode);
        // There is no subjects, If an error occurs.
        if (subject == null) {
            return;
        }

        subject.onNext(new ActivityResult(resultCode, data));
        subject.onCompleted();
    }

    private Observable<?> triggerObservable(Observable<?> trigger, int requestCode) {

        if (trigger == null) {
            return Observable.just(null);
        }

        if (mSubjects.containsKey(requestCode)) {
            return Observable.merge(trigger, Observable.just(null));
        }
        return trigger;
    }

    private Observable<ActivityResult> startActivityObservable(Launchable source,
                                                               Intent intent,
                                                               int requestCode,
                                                               Bundle options) {

        PublishSubject<ActivityResult> subject = mSubjects.get(requestCode);
        final boolean existSubject = subject != null;
        if (subject == null) {
            subject = PublishSubject.create();
            mSubjects.put(requestCode, subject);
        }

        if (!existSubject) {
            try {
                source.startActivity(intent, requestCode, options);
            }
            catch (Exception e) {
                return Observable.error(e);
            }
        }
        return subject;
    }
}
