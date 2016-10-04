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
package com.amazon.tv;

/**
 * Constants required for limiting video quality.
 * IMPORTANT NOTE: This file is copied from amazon_app_settings.jar from 'Amazon.com:FireOS 5 SDK
 * (clean):22'. FireOS SDK uses SDK 22 but we need SDK 23 for compilation, hence cannot use FireOS
 * SDK for compiling.
 * These constants might change in future!!! Whenever FireOS is updated to use 23, replace this file
 * with Settings class from FireOS.
 */
public final class Settings {

    public static final class Global {

        public static final String VIDEO_QUALITY = "com.amazon.tv.settings.VIDEO_QUALITY";
        public static final int VIDEO_QUALITY_GOOD = 0;
        public static final int VIDEO_QUALITY_BETTER = 1;
        public static final int VIDEO_QUALITY_BEST = 2;
    }
}
