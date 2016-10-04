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

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.CheckResult;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;

import rx.Observable;

/**
 * Interface to delegate the launch of the activity.
 *
 * @author kumagai
 */
public abstract class Launchable {

    /**
     * Launch an activity for which you would like a result when it finished.
     *
     * @param intent      The intent.
     * @param requestCode The request code.
     * @param options     The option bundle.
     * @return The {@link Observable} for the {@link ActivityResult}.
     **/
    @CheckResult
    public abstract Observable<ActivityResult> startActivityForResult(@NonNull Intent intent,
                                                                      int requestCode,
                                                                      @Nullable Bundle options);

    /**
     * Launch an activity for which you would like a result when it finished.
     * <p>
     * After other activity launched, you use this method if the screen might rotate.
     * </p>
     *
     * @param trigger     The observable to trigger.
     * @param intent      The intent.
     * @param requestCode The request code.
     * @param options     The options bundle.
     * @return The {@link Observable} for the {@link ActivityResult}.
     */
    @CheckResult
    public abstract Observable<ActivityResult> startActivityForResult(@Nullable Observable<?>
                                                                              trigger,
                                                                      @NonNull Intent intent,
                                                                      int requestCode,
                                                                      @Nullable Bundle options);

    protected abstract void startActivity(@NonNull Intent intent,
                                          int requestCode,
                                          @Nullable Bundle options);
}
