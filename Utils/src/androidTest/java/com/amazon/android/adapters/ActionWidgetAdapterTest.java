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


package com.amazon.android.adapters;

import android.os.Handler;

import android.support.test.InstrumentationRegistry;
import android.support.test.filters.FlakyTest;
import android.support.test.rule.ActivityTestRule;
import android.support.test.runner.AndroidJUnit4;

import android.test.suitebuilder.annotation.LargeTest;

import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.runner.RunWith;

import com.amazon.utils.R;
import com.amazon.android.adapters.testResources.DummyActionWidgetContainer;
import com.amazon.android.model.Action;
import com.robotium.solo.Solo;

import junit.framework.AssertionFailedError;

import java.util.ArrayList;
import java.util.NoSuchElementException;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

/**
 * This class tests the ActionWidgetAdapter.
 */
@RunWith(AndroidJUnit4.class)
public class ActionWidgetAdapterTest {

    // The Robotium driver.
    private Solo solo;

    @Rule
    public ActivityTestRule<DummyActionWidgetContainer> mActivityRule = new ActivityTestRule<>(
            DummyActionWidgetContainer.class);

    /**
     * This setup method will create the Robotium driver
     */
    @Before
    public void setUp() throws Exception {

        // Setup the Robotium driver with the current activity and instrumentation.
        solo = new Solo(InstrumentationRegistry.getInstrumentation(), mActivityRule.getActivity());
    }

    /**
     * This teardown method will finish opened Activities
     */
    @After
    public void tearDown() throws Exception {

        /**
         * The below code has been commented out as finishOpenedActivities()
         * could create a race condition that causes test cases to fail.
         *
         * finishOpenedActivities() finishing the current Activity and sends
         * the Back button once when config.trackActivities is set to true.
         * However, this process is not instantaneous and takes place on
         * a different thread. As a result, the Back button could be sent
         * after the next test case starts, causing the Activity to close
         * and rendering Robotium unable to interact with it.
         */
        // solo.finishOpenedActivities();
    }

    /**
     * This method will add two actions one-by-one to the adapter
     */
    @Test
    @FlakyTest
    public void testAddItemsToActionWidget() throws Exception {

        // Create a two actions.
        final Action search = new Action(0, "Search", R.drawable.action_button_focused);
        final Action logout = new Action(1, "Logout", R.drawable.action_button_focused);

        assertTrue(mActivityRule.getActivity().mActionWidgetAdapter.getItemCount() == 0);

        // Get a handler that can be used to post to the main thread.
        Handler mainHandler = new Handler(mActivityRule.getActivity().getBaseContext()
                                                                     .getMainLooper());

        Runnable myRunnable = () -> {

            // Add the two items to the adapter.
            mActivityRule.getActivity().mActionWidgetAdapter.addAction(search);
            mActivityRule.getActivity().mActionWidgetAdapter.addAction(logout);
        };
        mainHandler.post(myRunnable);

        assertTrue(solo.getImageButton(0).getTag().equals("Search"));
        assertTrue(solo.getImageButton(1).getTag().equals("Logout"));

        assertTrue(mActivityRule.getActivity().mActionWidgetAdapter.getItemCount() == 2);

    }

    /**
     * This test ensures that an empty adapter can be displayed and will not react to key press
     * events.
     */
    @Test
    public void testEmptyActionWidget() throws Exception {

        assertTrue(mActivityRule.getActivity().mActionWidgetAdapter.getItemCount() == 0);
        // Enter key input.
        solo.sendKey(Solo.LEFT);
        pressUpDownEnter();

    }

    /**
     * Tests the removal of an action from an empty {@link com.amazon.android.adapters
     * .ActionWidgetAdapter}. Expecting a {@link NoSuchElementException}.
     */
    @FlakyTest
    public void testRemoveActionFromEmptyAdapter() throws Exception {

        // Get a handler that can be used to post to the main thread.
        Handler mainHandler = new Handler(mActivityRule.getActivity().getBaseContext()
                                                                     .getMainLooper());

        Runnable myRunnable = () -> {

            Throwable e = null;

            // Remove object that doest not exist.
            try {
                mActivityRule.getActivity().mActionWidgetAdapter.removeAction((long) 0);
            }
            catch (NoSuchElementException error) {
                e = error;
            }
            assertTrue("NoSuchElementException for item not found in collection caught", e != null);
        };
        mainHandler.post(myRunnable);
    }


    /**
     * Tests the removal of a non-existent action from an {@link com.amazon.android.adapters
     * .ActionWidgetAdapter}. Expecting a {@link NoSuchElementException}.
     */
    public void testRemoveActionErrorCases() throws Exception {

        // Get a handler that can be used to post to the main thread
        Handler mainHandler = new Handler(mActivityRule.getActivity().getBaseContext()
                                                                     .getMainLooper());

        Runnable myRunnable = () -> {

            // Create a list of actions.
            final Action search = new Action(0, "Search", R.drawable.action_button_focused);
            final Action logout = new Action(1, "Logout", R.drawable.action_button_focused);

            final ArrayList<Action> actions = new ArrayList<>();

            actions.add(search);
            actions.add(logout);

            // Add the actions to the adapter.
            mActivityRule.getActivity().mActionWidgetAdapter.addActions(actions);

            Throwable e = null;

            // Remove object that doest not exist.
            try {
                mActivityRule.getActivity().mActionWidgetAdapter.removeAction((long) 2);
            }
            catch (NoSuchElementException error) {
                e = error;
            }

            assertTrue("NoSuchElementException for item not found in collection caught", e != null);

        };
        mainHandler.post(myRunnable);
    }

    /**
     * This method tests removal of action items.
     * This method will add two action items and remove one of them.
     */
    public void testActionRemoval() throws Exception {

        // Create a list of actions.
        final Action search = new Action(0, "Search", R.drawable.action_button_focused);
        final Action logout = new Action(1, "Logout", R.drawable.action_button_focused);

        final ArrayList<Action> actions = new ArrayList<>();

        actions.add(search);
        actions.add(logout);

        // Get a handler that can be used to post to the main thread.
        Handler mainHandler = new Handler(mActivityRule.getActivity().getBaseContext()
                                                                     .getMainLooper());

        Runnable myRunnable = () -> {
            // Add the actions to the adapter.
            mActivityRule.getActivity().mActionWidgetAdapter.addActions(actions);

            assertTrue("Total items should be 2", mActivityRule.getActivity().mActionWidgetAdapter
                    .getItemCount() == 2);

            Action removedAction = mActivityRule.getActivity().mActionWidgetAdapter.removeAction(1);

            assertTrue("Removed action should be the logout button", removedAction.getLabel1
                    ().equals("Logout"));

            assertTrue("Total items should be 1", mActivityRule.getActivity().mActionWidgetAdapter
                    .getItemCount() == 1);
        };
        mainHandler.post(myRunnable);

        assertTrue(solo.getImageButton(0).getTag().equals("Search"));

        Throwable e = null;

        // This should cause an exception since the second action of the list was removed.
        try {
            solo.getImageButton(1);
        }
        catch (AssertionFailedError error) {
            e = error;
        }

        assertTrue("AssertionFailedError for item not found in collection caught", e != null);
    }

    /**
     * This test focuses on edge case input. Note: This tests takes a while to run.
     */
    @Test
    @LargeTest
    @FlakyTest
    public void testMaliciousInput() throws Exception {

        final ArrayList<Action> actions = new ArrayList<>();

        // Create the of actions.
        final Action search = new Action(0, "Search", R.drawable.action_button_focused);
        final Action logout = new Action(1, "Logout", R.drawable.action_button_focused);
        final Action login = new Action(0, "Login", R.drawable.action_button_focused);
        final Action legal = new Action(1, "Legal", R.drawable.action_button_focused);


        // Add the actions to the array list.
        actions.add(search);
        actions.add(logout);
        actions.add(login);
        actions.add(legal);

        // Get a handler that can be used to post to the main thread.
        Handler mainHandler = new Handler(mActivityRule.getActivity().getBaseContext()
                                                                     .getMainLooper());

        Runnable myRunnable = () -> {

            // Add the actions to the widget adapter.
            mActivityRule.getActivity().mActionWidgetAdapter.addActions(actions);
        };
        mainHandler.post(myRunnable);

        solo.sleep(1000);


        assertTrue(mActivityRule.getActivity().mActionWidgetAdapter.getItemCount() == 4);

        // Perform user inputs and navigate all the way to the left.
        solo.sendKey(Solo.LEFT);
        pressUpDownEnter();
        solo.sendKey(Solo.LEFT);
        pressUpDownEnter();
        solo.sendKey(Solo.LEFT);
        pressUpDownEnter();
        solo.sendKey(Solo.LEFT);
        pressUpDownEnter();
        solo.sendKey(Solo.LEFT);

        assertTrue(solo.getImageButton(0).isFocused());
        assertFalse(solo.getImageButton(2).isFocused());

        // Perform user inputs and navigate all the way to the right.
        solo.sendKey(Solo.RIGHT);
        pressUpDownEnter();
        solo.sendKey(Solo.RIGHT);
        pressUpDownEnter();
        solo.sendKey(Solo.RIGHT);
        pressUpDownEnter();
        solo.sendKey(Solo.RIGHT);
        pressUpDownEnter();
        solo.sendKey(Solo.RIGHT);
        pressUpDownEnter();
        solo.sendKey(Solo.RIGHT);

        assertFalse(solo.getImageButton(0).isFocused());
        assertTrue(solo.getImageButton(3).isFocused());
    }

    /**
     * Helper method to send the following key presses in order: UP, DOWN, ENTER.
     */
    private void pressUpDownEnter() {

        solo.sendKey(Solo.UP);
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.ENTER);
    }
}
