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
package com.amazon.android.ads.vast.module;

import com.amazon.ads.IAds;
import com.amazon.android.ads.vast.VASTAdsPlayer;
import com.amazon.android.module.IImplCreator;

/**
 * VastAdsImpCreator class.
 */
public class VastAdsImplCreator implements IImplCreator<IAds> {

    /**
     * Create the Vast Ads instance, init needs to be called for usage.
     *
     * @return Vast Ads instance.
     */
    @Override
    public IAds createImpl() {
        return new VASTAdsPlayer();
    }
}
