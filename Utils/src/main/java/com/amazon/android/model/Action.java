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
 * Copyright (C) 2014 The Android Open Source Project
 * Copyright (C) 2016 Amazon Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except
 * in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License
 * is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express
 * or implied. See the License for the specific language governing permissions and limitations under
 * the License.
 */
/**
 * Tried to be as close as LeanBack's Action class.
 */
package com.amazon.android.model;

import android.graphics.drawable.Drawable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

/**
 * This class is built on top of Leanback's Action class, with extra functionality of states.
 * It can be used without any state as well, in which case it works with an internal default
 * state. This class is not thread safe.
 */
public class Action {

    /**
     * Default state of the Action.
     */
    private static final int DEFAULT_STATE = 0;

    /**
     * Data that can change with states.
     */
    private class ActionData {

        /**
         * Icon drawable.
         */
        Drawable mIcon;

        /**
         * First label.
         */
        CharSequence mLabel1;

        /**
         * Second label.
         */
        CharSequence mLabel2;
        /**
         * Action object name.
         */
        String mName;

        /**
         * Action hint, can be used for hinting buttons.
         */
        String mHint;

        /**
         * Icon resource id.
         */
        int mIconResourceId;

        /**
         * {@inheritDoc}
         */
        @Override
        public String toString() {

            return "ActionData{" +
                    "icon=" + mIcon +
                    ", label1=" + mLabel1 +
                    ", label2=" + mLabel2 +
                    ", name='" + mName + '\'' +
                    ", hint='" + mHint + '\'' +
                    ", iconResourceId=" + mIconResourceId +
                    '}';
        }
    }

    /**
     * No id value.
     */
    public static final int NO_ID = -1;

    /**
     * Id value.
     */
    private long mId = NO_ID;

    /**
     * Key code list.
     */
    private ArrayList mKeyCodes = new ArrayList();

    /**
     * Action value.
     */
    private String mAction;

    /**
     * State of the Action.
     */
    private int mState = DEFAULT_STATE;
    /**
     * Map of ActionData for each action state
     */
    private Map<Integer, ActionData> mActionDataPerState = new HashMap<>();

    /**
     * A specific constructor for creating new actions.
     *
     * @param id The id for the new action.
     * @param tag The tag for the new action.
     * @param iconResourceID The icon resource ID for the new Action.
     */
    public Action(long id, String tag, int iconResourceID) {

        mId = id;
        ActionData actionData = new ActionData();
        mActionDataPerState.put(DEFAULT_STATE, actionData);
        actionData.mLabel1 = tag;
        actionData.mName = tag;
        actionData.mIconResourceId = iconResourceID;

    }

    /**
     * Constructor for Action class.
     */
    public Action() {

        mActionDataPerState.put(DEFAULT_STATE, new ActionData());
    }

    /**
     * Set action value.
     *
     * @param action Action value.
     * @return Action object reference.
     */
    public Action setAction(String action) {

        this.mAction = action;
        return this;
    }

    /**
     * Set hint string for current state.
     *
     * @param hint Hint string.
     * @return Action object reference.
     */
    public Action setHint(String hint) {

        return setHint(mState, hint);
    }

    /**
     * Set hint string for state.
     *
     * @param state The state to set the value of.
     * @param hint The hint string.
     * @return Action object reference.
     */
    public Action setHint(int state, String hint) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mHint = hint;
        return this;
    }

    /**
     * Returns whether the state exist or not.
     *
     * @param state State to validate.
     */
    private boolean doesStateExist(int state) {

        return mActionDataPerState.containsKey(state);
    }

    /**
     * Validates that the state exists for this action. Creates the state if it does not already
     * exist.
     *
     * @param state The state to validate.
     * @return True if the state is valid; false otherwise.
     */
    private boolean validateAndAddState(int state) {

        if (!mActionDataPerState.containsKey(state)) {
            mActionDataPerState.put(state, new ActionData());
            return false;
        }
        return true;
    }

    /**
     * Set icon resource id.
     *
     * @param iconResourceId Icon resource id.
     * @return Action object reference.
     */
    public Action setIconResourceId(int iconResourceId) {

        return setIconResourceId(mState, iconResourceId);
    }

    /**
     * Set icon resource id for the state.
     *
     * @param state The state to set the value of.
     * @param iconResourceId Icon resource id.
     * @return Action object reference.
     */
    public Action setIconResourceId(int state, int iconResourceId) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mIconResourceId = iconResourceId;
        return this;
    }

    /**
     * Set action object name.
     *
     * @param name Action object name.
     * @return Action object reference.
     */
    public Action setName(String name) {

        return setName(mState, name);
    }

    /**
     * Set action object name for this state.
     *
     * @param state The state to set the value of.
     * @param name Action object name.
     * @return Action object reference.
     */
    public Action setName(int state, String name) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mName = name;
        return this;
    }

    /**
     * Get action object name.
     *
     * @return Action object name.
     */
    public String getName() {

        return getName(mState);
    }

    /**
     * Get action object name for state.
     * @param state The state to get the name of.
     * @return Action object name for state.
     */
    public String getName(int state) {

        if (!doesStateExist(state)) {
            return null;
        }
        return mActionDataPerState.get(state).mName;
    }

    /**
     * Get hint string for state.
     * @param state The state to get the hint of.
     * @return Hint string for state.
     */
    public String getHint(int state) {

        if (!doesStateExist(state)) {
            return null;
        }
        return mActionDataPerState.get(state).mHint;
    }

    /**
     * Get hint string.
     *
     * @return Hint string.
     */
    public String getHint() {

        return getHint(mState);
    }

    /**
     * Get icon resource id.
     *
     * @return Icon resource id.
     */
    public int getIconResourceId() {

        return getIconResourceId(mState);
    }

    /**
     * Get icon resource id for state.
     * @param state The state to get the icon resource id of.
     * @return Icon resource id.
     */
    public int getIconResourceId(int state) {

        if (!doesStateExist(state)) {
            return 0;
        }
        return mActionDataPerState.get(state).mIconResourceId;
    }

    /**
     * Get action string.
     *
     * @return Action string.
     */
    public String getAction() {

        return mAction;
    }

    /**
     * Sets the id for this Action.
     *
     * @param id The action id.
     * @return The action.
     */
    public final Action setId(long id) {

        mId = id;
        return this;
    }

    /**
     * Returns the id for this Action.
     *
     * @return The action id.
     */
    public final long getId() {

        return mId;
    }

    /**
     * Sets the first line label for this Action.
     *
     * @param label The string to use as the first label.
     * @return The action.
     */
    public final Action setLabel1(CharSequence label) {

        return setLabel1(mState, label);
    }

    /**
     * Sets the first line label for this Action.
     *
     * @param state The state of the action.
     * @param label The string to use as the first label.
     * @return The action.
     */
    public final Action setLabel1(int state, CharSequence label) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mLabel1 = label;
        return this;
    }

    /**
     * Returns the first line label for this Action.
     *
     * @return The first label.
     */
    public final CharSequence getLabel1() {

        return getLabel1(mState);
    }

    /**
     * Returns the first line label for this Action at the given state.
     *
     * @param state The state to get the label for.
     * @return The first label.
     */
    public final CharSequence getLabel1(int state) {

        if (!doesStateExist(state)) {
            return null;
        }
        return mActionDataPerState.get(state).mLabel1;
    }

    /**
     * Sets the second line label for this Action.
     *
     * @param label The second label.
     * @return The action.
     */
    public final Action setLabel2(CharSequence label) {

        return setLabel2(mState, label);
    }

    /**
     * Sets the second line label for this Action.
     *
     * @param state The state to get the label for.
     * @param label The second label.
     * @return The action.
     */
    public final Action setLabel2(int state, CharSequence label) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mLabel2 = label;
        return this;
    }

    /**
     * Returns the second line label for this Action.
     *
     * @return The second label.
     */
    public final CharSequence getLabel2() {

        return getLabel2(mState);
    }

    /**
     * Returns the second line label for this Action at the given state.
     *
     * @param state The state to get the label for.
     * @return The first label.
     */
    public final CharSequence getLabel2(int state) {

        if (!doesStateExist(state)) {
            return null;
        }
        return mActionDataPerState.get(state).mLabel2;
    }

    /**
     * Sets the icon drawable for this Action.
     *
     * @param icon The icon Drawable.
     * @return The action.
     */
    public final Action setIcon(Drawable icon) {

        return setIcon(mState, icon);
    }

    /**
     * Sets the icon drawable for this Action.
     *
     * @param state The state of the action.
     * @param icon The icon Drawable.
     * @return The action.
     */
    public final Action setIcon(int state, Drawable icon) {

        validateAndAddState(state);
        mActionDataPerState.get(state).mIcon = icon;
        return this;
    }

    /**
     * Returns the icon drawable for this Action.
     *
     * @return The icon Drawable.
     */
    public final Drawable getIcon() {

        return getIcon(mState);
    }

    /**
     * Returns the icon drawable for this Action.
     *
     * @param state The state of the action.
     * @return The icon Drawable.
     */
    public final Drawable getIcon(int state) {

        if (!doesStateExist(state)) {
            return null;
        }
        return mActionDataPerState.get(state).mIcon;
    }

    /**
     * Adds a keycode used to invoke this Action.
     *
     * @param keyCode The keycode.
     * @return The action.
     */
    public final Action addKeyCode(int keyCode) {

        mKeyCodes.add(keyCode);
        return this;
    }

    /**
     * Removes a keycode used to invoke this Action.
     *
     * @param keyCode The keycode.
     */
    public final void removeKeyCode(int keyCode) {

        mKeyCodes.remove(keyCode);
    }

    /**
     * Returns true if the Action should respond to the given keycode.
     *
     * @param keyCode The keycode.
     * @return True if the action responds to the keycode; false otherwise.
     */
    public final boolean respondsToKeyCode(int keyCode) {

        return mKeyCodes.contains(keyCode);
    }

    /**
     * Returns mState of the action, default is 0.
     *
     * @return State of the action.
     */
    public int getState() {

        return mState;
    }

    /**
     * Sets mState of the action.
     *
     * @param state State of the action.
     */
    public Action setState(int state) {

        this.mState = state;
        return this;
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public String toString() {

        return "Action{" +
                "id=" + mId +
                ", keyCodes=" + mKeyCodes +
                ", action='" + mAction + '\'' +
                ", state=" + mState +
                ", actionDataPerState=" + mActionDataPerState +
                '}';
    }
}
