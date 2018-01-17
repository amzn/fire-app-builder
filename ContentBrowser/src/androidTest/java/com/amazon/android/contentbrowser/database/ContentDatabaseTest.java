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
package com.amazon.android.contentbrowser.database;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import android.content.Context;
import android.support.test.InstrumentationRegistry;
import android.support.test.runner.AndroidJUnit4;

import java.io.File;
import java.lang.reflect.Field;


@SuppressWarnings("unchecked")
@RunWith(AndroidJUnit4.class)
/**
 * Test class for {@link ContentDatabase}.
 */
public class ContentDatabaseTest {
    
    /**
     * Clear out the singleton instance of the database helper so each test has a clean slate.
     */
    @Before
    public void resetDatabase() throws NoSuchFieldException, IllegalAccessException {
        
        Field instance = ContentDatabase.class.getDeclaredField("sInstance");
        instance.setAccessible(true);
        instance.set(null, null);
    }
    
    /**
     * Test getting the content database instance.
     */
    @Test
    public void testGetInstance() throws Exception {
        
        ContentDatabase contentDatabase = ContentDatabase.getInstance(InstrumentationRegistry
                                                                              .getContext());
        assertNotNull("ContentDatabase should not be null", contentDatabase);
    }
    
    /**
     * Test getting the SQLite Database instance.
     */
    @Test
    public void testGetDatabaseInstance() throws Exception {
        
        ContentDatabase contentDatabase = ContentDatabase.getInstance(InstrumentationRegistry
                                                                              .getContext());
        assertNotNull("ContentDatabase should not be null", contentDatabase);
        assertNotNull("Database instance should not be null.", contentDatabase
                .getDatabaseInstance());
        
    }
    
    /**
     * Test deleting the database.
     */
    @Test
    public void testDeleteDatabase() throws Exception {
        
        Context context = InstrumentationRegistry.getContext();
        
        ContentDatabase contentDatabase = ContentDatabase.getInstance(context);
        assertNotNull(contentDatabase);
        
        String databaseFilePath = contentDatabase.getDatabasePath(context);
        assertTrue(contentDatabase.deleteDatabase(context));
        
        File databaseFile = new File(databaseFilePath);
        assertFalse(databaseFilePath + " should not exist.", databaseFile.exists());
        
        contentDatabase.close();
    }
    
    
}
