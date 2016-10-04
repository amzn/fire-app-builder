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
import android.content.Intent;
import android.support.annotation.Nullable;

/**
 * This class holds the data received by {@link Activity#onActivityResult(int, int, Intent)}.
 *
 * @author kumagai
 */
public class ActivityResult {

    public final int resultCode;
    @Nullable
    public final Intent data;

    ActivityResult(int resultCode, @Nullable Intent data) {
        this.resultCode = resultCode;
        this.data = data;
    }

    public boolean isOk() {
        return resultCode == Activity.RESULT_OK;
    }

    public boolean isCanceled() {
        return resultCode == Activity.RESULT_CANCELED;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof ActivityResult)) return false;

        ActivityResult that = (ActivityResult) o;

        if (resultCode != that.resultCode) return false;
        return !(data != null ? !data.equals(that.data) : that.data != null);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public int hashCode() {
        int result = resultCode;
        result = 31 * result + (data != null ? data.hashCode() : 0);
        return result;
    }
}
