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
package com.amazon.android.utils;

import com.amazon.android.model.Action;

/**
 * Class for Utils which are related to Leanback.
 */
public class LeanbackHelpers {

    /**
     * Translate a LeanBack action to Amazon action.
     *
     * @param leanbackAction LeanBack action object.
     * @return Amazon Action object.
     */
    public static Action translateLeanBackActionToAction(
            android.support.v17.leanback.widget.Action leanbackAction) {

        Action action = new Action();
        action.setId(leanbackAction.getId())
              .setIcon(leanbackAction.getIcon())
              .setLabel1(leanbackAction.getLabel1())
              .setLabel2(leanbackAction.getLabel2());

        return action;
    }

    /**
     * Translate an Amazon action to LeanBack action.
     *
     * @param action Amazon Action object.
     * @return LeanBack action object.
     */
    public static android.support.v17.leanback.widget.Action translateActionToLeanBackAction(
            Action action) {

        android.support.v17.leanback.widget.Action leanbackAction =
                new android.support.v17.leanback.widget.Action(action.getId(),
                                                               action.getLabel1(),
                                                               action.getLabel2());

        return leanbackAction;
    }

    /**
     * Translate an object from the action array adapter to an {@link Action}.
     *
     * @param actionObject The action object.
     * @return Amazon Action object.
     */
    public static Action translateActionAdapterObjectToAction(Object actionObject) {

        android.support.v17.leanback.widget.Action leanbackAction = (android.support.v17.leanback
                .widget.Action) actionObject;

        return translateLeanBackActionToAction(leanbackAction);
    }
}
