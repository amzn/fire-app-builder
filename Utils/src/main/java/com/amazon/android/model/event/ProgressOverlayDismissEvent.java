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
package com.amazon.android.model.event;

/**
 * Progress overlay dismiss event class, this class for event broadcasting.
 */
public class ProgressOverlayDismissEvent {

    /**
     * Dismiss flag.
     */
    private boolean mDismiss = false;

    /**
     * Constructor
     *
     * @param flag Dismiss flag.
     */
    public ProgressOverlayDismissEvent(boolean flag) {

        mDismiss = flag;
    }
}
