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
 * Copyright (c) 2014, Nexage, Inc. All rights reserved.
 * Copyright (C) 2016 Amazon Inc.
 *
 * Provided under BSD-3 license as follows:
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted
 * provided that the following conditions are met:
 *
 * Redistributions of source code must retain the above copyright notice, this list of conditions
 * and the following disclaimer.
 *
 * Redistributions in binary form must reproduce the above copyright notice, this list of conditions
 *  and the following
 * disclaimer in the documentation and/or other materials provided with the distribution.
 *
 * Neither the name of Nexage nor the names of its contributors may be used to endorse or promote
 * products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR
 * IMPLIED WARRANTIES,
 * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE
 * LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA,
 * OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN
 * CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF
 * THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package com.amazon.android.ads.vast.util;

import android.util.Log;

public class VASTLog {

    private static String TAG = "VAST";

    public enum LOG_LEVEL {

        verbose(1),
        debug(2),
        info(3),
        warning(4),
        error(5),
        none(6);

        private int value;

        LOG_LEVEL(int value) {

            this.value = value;

        }

        public int getValue() {

            return value;
        }
    }

    private static LOG_LEVEL LEVEL = LOG_LEVEL.verbose;

    public static void v(String tag, String msg) {

        if (LEVEL.getValue() <= LOG_LEVEL.verbose.getValue()) {
            Log.v(tag, msg);
        }
    }

    public static void d(String tag, String msg) {

        if (LEVEL.getValue() <= LOG_LEVEL.debug.getValue()) {
            Log.d(tag, msg);
        }
    }

    public static void i(String tag, String msg) {

        if (LEVEL.getValue() <= LOG_LEVEL.info.getValue()) {
            Log.i(tag, msg);
        }
    }

    public static void w(String tag, String msg) {

        if (LEVEL.getValue() <= LOG_LEVEL.warning.getValue()) {
            Log.w(tag, msg);
        }
    }
    
    public static void e(String tag, String msg) {

        if (LEVEL.getValue() <= LOG_LEVEL.error.getValue()) {
            Log.e(tag, msg);
        }
    }

    public static void e(String tag, String msg, Throwable tr) {

        if (LEVEL.getValue() <= LOG_LEVEL.error.getValue()) {
            Log.e(tag, msg, tr);
        }
    }

    public static void setLoggingLevel(LOG_LEVEL logLevel) {

        Log.i(TAG, "Changing logging level from :" + LEVEL + ". To:" + logLevel);
        LEVEL = logLevel;
    }

    public static LOG_LEVEL getLoggingLevel() {

        return LEVEL;
    }

}
