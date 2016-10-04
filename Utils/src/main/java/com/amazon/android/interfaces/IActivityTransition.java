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
package com.amazon.android.interfaces;

import android.os.Bundle;

/**
 * Activity transition interface.
 */
public interface IActivityTransition {

    /**
     * This method will be called when activity switch requires an animation transition.
     *
     * @return Scene transition animation bundle.
     */
    Bundle getSceneTransitionAnimationBundle();

    /**
     * This method will be called post start activity.
     */
    void onPostStartActivity();

    /**
     * This method will be called before activity transition.
     */
    void onBeforeActivityTransition();
}
