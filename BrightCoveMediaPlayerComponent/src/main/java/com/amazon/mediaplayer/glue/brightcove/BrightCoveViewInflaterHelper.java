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
package com.amazon.mediaplayer.glue.brightcove;

import android.content.Context;

import com.brightcove.player.view.BrightcoveExoPlayerVideoView;

/**
 * This class is for creating BrightCoveView programmatically as inflater was calling finishInflate.
 */
public class BrightCoveViewInflaterHelper extends BrightcoveExoPlayerVideoView {

    /**
     *  Constructor.
     *
     * @param context   Context
     */
    public BrightCoveViewInflaterHelper(Context context) {
        super(context);
    }

    /**
     * BrightCoveView does it's initializing in finishInflate.
     */
    public void finishInflate() {
        this.onFinishInflate();
    }
}
