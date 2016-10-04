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

package com.amazon.mediaplayer.module;

import com.amazon.android.module.IImplCreator;
import com.amazon.android.uamp.UAMP;
import com.amazon.mediaplayer.glue.AMZNPlayer;

/**
 * AMZNMediaPlayerImplCreator class.
 */
public class AMZNMediaPlayerImplCreator implements IImplCreator<UAMP> {

    /**
     * Create the AMZNMediaPlayer instance, init needs to be called for usage.
     *
     * @return AMZNMediaPlayer instance.
     */
    @Override
    public UAMP createImpl() {
        return new AMZNPlayer();
    }
}
