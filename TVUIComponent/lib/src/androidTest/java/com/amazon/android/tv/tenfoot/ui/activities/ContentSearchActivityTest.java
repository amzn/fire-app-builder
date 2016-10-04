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
package com.amazon.android.tv.tenfoot.ui.activities;

import com.amazon.android.tv.tenfoot.R;
import com.amazon.android.tv.tenfoot.ui.fragments.ContentSearchFragment;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import android.test.ActivityInstrumentationTestCase2;

/**
 * This test Class exercises {@link ContentSearchActivity}
 */
public class ContentSearchActivityTest extends
        ActivityInstrumentationTestCase2<ContentSearchActivity> {

    // Create our local Search Activity under test.
    public ContentSearchActivity mContentSearchActivity;

    public ContentSearchActivityTest() {

        super(ContentSearchActivity.class);
    }

    @Before
    public void setUp() throws Exception {

        mContentSearchActivity = getActivity();

    }

    @After
    public void tearDown() throws Exception {

    }

    /**
     * This method tests the onCreate method by checking that the ContentSearchFragment is
     * visible once the activity is created.
     */
    @Test
    public void testOnCreate() throws Exception {


        // Create the local TenFootUI Search Fragment.
        ContentSearchFragment mFragment = (ContentSearchFragment) mContentSearchActivity
                .getFragmentManager().findFragmentById(R.id.content_search_fragment);

        // Assert that the TenFootUI fragment is visible.
        assertTrue(mFragment.isVisible());

    }

}
