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
package com.amazon.ads.android.freewheel;

import com.amazon.ads.IAds;
import com.amazon.android.module.IImplCreator;

/**
 * This lets modules follow the same protocol for creating an instance.
 */
public class FreeWheelAdsImplCreator implements IImplCreator<IAds> {

    /**
     * Create the FreeWheelAds implementation. This doesn't need init.
     *
     * @return Ads interface.
     */
    @Override
    public IAds createImpl() {

        return new FreeWheelAds();
    }
}
