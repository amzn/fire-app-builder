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

import android.text.TextUtils;
import android.util.Log;

import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class HttpTools {

    private static final String TAG = HttpTools.class.getName();

    /**
     * Http get request.
     *
     * @param url url need to be used for http get request.
     */
    public static void httpGetURL(final String url) {

        if (!TextUtils.isEmpty(url)) {
            new Thread() {
                @Override
                public void run() {

                    HttpURLConnection conn = null;
                    try {
                        Log.d(TAG, "connection to URL:" + url);
                        URL httpUrl = new URL(url);

                        HttpURLConnection.setFollowRedirects(true);
                        conn = (HttpURLConnection) httpUrl.openConnection();
                        conn.setConnectTimeout(5000);
                        conn.setRequestProperty("Connection", "close");
                        conn.setRequestMethod("GET");

                        int code = conn.getResponseCode();
                        Log.d(TAG, "response code:" + code
                                + ", for URL:" + url);
                    }
                    catch (Exception e) {
                        Log.e(TAG, url + ": " + e.getMessage() + ":"
                                + e.toString());
                    }
                    finally {
                        if (conn != null) {
                            conn.disconnect();
                        }
                    }
                }
            }.start();
        }
        else {
            Log.e(TAG, "url is null or empty");

        }

    }

    /**
     * Request all urls provided in list.
     *
     * @param urls urls need to be requested using http request.
     */
    public static void fireUrls(List<String> urls) {

        Log.i(TAG, "entered fireUrls");

        if (urls != null) {
            for (String url : urls) {
                Log.i(TAG, "\tfiring url:" + url);
                httpGetURL(url);
            }
        }
        else {
            Log.i(TAG, "\turl list is null");
        }
    }
}
