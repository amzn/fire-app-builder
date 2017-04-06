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
package com.amazon.android.tv.tenfoot.ui.fragments;

import com.amazon.android.contentbrowser.ContentBrowser;
import com.amazon.android.model.content.Content;
import com.amazon.android.model.content.ContentContainer;
import com.amazon.android.tv.tenfoot.ui.activities.ContentSearchActivity;
import com.amazon.android.utils.Preferences;
import com.robotium.solo.Solo;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.support.v17.leanback.widget.ListRow;
import android.test.ActivityInstrumentationTestCase2;

import java.util.ArrayList;
import java.util.Collections;


/**
 * This test class exercises {@link ContentSearchFragment}.
 */
public class ContentSearchFragmentTest extends
        ActivityInstrumentationTestCase2<ContentSearchActivity> {

    // Create the Robotium driver
    private Solo solo;

    /**
     * Creates an {@link ActivityInstrumentationTestCase2}.
     */
    public ContentSearchFragmentTest() {

        super(ContentSearchActivity.class);
    }


    /**
     * This setup method will create the Robotium driver and populate the apps movie database.
     */
    @Before
    public void setUp() throws Exception {

        // Setup the Robotium driver with the current activity and instrumentation
        solo = new Solo(getInstrumentation(), getActivity());

        // Set the context for the Preferences so ContentBrowser can be initialized properly.
        Preferences.setContext(getActivity());

        // Initialize and populate the test container that we will send to the
        // ContentSearchFragment.
        initContentContainer();
    }

    /**
     * This helper method will setup the test content container.
     */
    private void initContentContainer() {

        // Create three items of content
        Content item1 = new Content();
        item1.setId("2323");
        item1.setTitle("Cooking Show 1");
        item1.setDescription("a great cooking show!");
        item1.setExtraValue("duration", "358");
        item1.setCardImageUrl("http://www.gstatic.com/webp/gallery/5.jpg");
        item1.setUrl("http://html5demos.com/assets/dizzy.mp4");
        item1.setExtraValue("categories", new ArrayList<>(Collections.singletonList
                ("International Cuisine")));
        item1.setExtraValue("channelId", 6341);

        Content item2 = new Content();
        item2.setId("232323");
        item2.setTitle("Cooking Show 2");
        item2.setDescription("a better cooking show!");
        item1.setExtraValue("duration", "358");
        item2.setCardImageUrl("http://www.gstatic.com/webp/gallery/5.jpg");
        item2.setUrl("http://html5demos.com/assets/dizzy.mp4");
        item2.setExtraValue("categories", new ArrayList<>(Collections.singletonList
                ("International Cuisine")));
        item2.setExtraValue("channelId", 6341);

        Content item3 = new Content();
        item3.setId("232323232");
        item3.setTitle("Penne arrabbiata");
        item3.setDescription("A cooking show where they make Penne arrabbiata");
        item3.setExtraValue("duration", "127");
        item3.setCardImageUrl("http://www.gstatic.com/webp/gallery/5.jpg");
        item3.setUrl("http://html5demos.com/assets/dizzy.mp4");
        item3.setExtraValue("categories", new ArrayList<>(Collections.singletonList
                ("International Cuisine")));
        item3.setExtraValue("channelId", 6341);

        // Add the items to the ContentBrowser root container
        ContentBrowser.getInstance(getActivity()).getRootContentContainer().addContent(item1);
        ContentBrowser.getInstance(getActivity()).getRootContentContainer().addContent(item2);
        ContentBrowser.getInstance(getActivity()).getRootContentContainer().addContent(item3);

    }

    /**
     * This teardown method will finish opened Activities.
     */
    @After
    public void tearDown() throws Exception {

        solo.finishOpenedActivities();
    }


    /**
     * This test method will act as an user and manually input a search term.
     */
    @Test
    public void testOnQueryTextChangeWithInput() throws Exception {

        // Delay check for a bit.
        solo.sleep(3000);

        // Check to make sure no items come up with a blank search item.
        assertTrue("There should not be any items in the row", getActivity().mFragment
                .getResultsAdapter().size() == 0);

        // Enter the search term "r".
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.ENTER);
        // Delay check for a bit.
        solo.sleep(3000);

        // Create a list row to find its contents.
        ListRow mListRow = (ListRow) getActivity().mFragment.getResultsAdapter().get(0);

        // Check to make sure we are getting the expected 3 items.
        assertTrue("There should be 3 items", mListRow.getAdapter().size() == 3);

        // Enter the rest of the search term "abbi".
        solo.sendKey(Solo.RIGHT);
        solo.sendKey(Solo.RIGHT);
        solo.sendKey(Solo.RIGHT);
        solo.sendKey(Solo.UP);
        solo.sendKey(Solo.ENTER);
        solo.sendKey(Solo.RIGHT);
        solo.sendKey(Solo.ENTER);
        solo.sendKey(Solo.ENTER);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.LEFT);
        solo.sendKey(Solo.ENTER);

        // Close the search soft keyboard.
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.DOWN);
        solo.sendKey(Solo.ENTER);


        // Update for the new list row.
        mListRow = (ListRow) getActivity().mFragment.getResultsAdapter().get(0);

        // Check that we are getting only one item back.
        assertTrue("There should only be one item", mListRow.getAdapter().size() == 1);

        // Verify we are getting the right movie.
        Content foundContent = (Content) mListRow.getAdapter().get(0);

        // Test that we have the right movie in the row.
        assertTrue("Title does not match", foundContent.getTitle().equals("Penne arrabbiata"));

    }

}
